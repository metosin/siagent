(ns reagent.core
  #?(:cljs (:require-macros [reagent.core :refer [reaction with-let]]))
  (:refer-clojure :exclude [atom])
  (:require #?(:cljs ["react" :as react])
            [clojure.set :as set]
            [clojure.string :as str]
            [reagent.impl.core :as impl]
            [reagent.ratom :as ra]
            [signaali.reactive :as sr]))

(defn atom [x]
  (ra/atom x))

(defn dispose! [x]
  (ra/dispose! x))

#?(:cljs
   (defn use-reactive [reactive-node]
     (let [[subscribe get-snapshot] (react/useMemo (fn []
                                                     ;;(prn "in react/useMemo")
                                                     (let [subscribe (fn [ping-react-that-something-might-have-changed]
                                                                       (let [signal-watcher (reify
                                                                                              sr/ISignalWatcher
                                                                                              (notify-signal-watcher [this is-for-sure signal-source]
                                                                                                (ping-react-that-something-might-have-changed)))]
                                                                         ;;(prn "subscribe")
                                                                         (sr/add-signal-watcher reactive-node signal-watcher)
                                                                         (fn []
                                                                           ;;(prn "unsubscribe")
                                                                           (sr/remove-signal-watcher reactive-node signal-watcher))))
                                                           get-snapshot (fn []
                                                                          ;;(prn "in get-snapshot")
                                                                          ;; https://stackoverflow.com/questions/76474940/why-does-the-usesyncexternalstore-example-call-getsnapshot-6-times-on-store
                                                                          @reactive-node)]
                                                       [subscribe get-snapshot]))
                                                   #js [reactive-node])]
       (react/useSyncExternalStore subscribe get-snapshot))))

#?(:cljs
   (declare as-element))

#?(:cljs
   (defn- get-react-wrapper [^js reagent-component]
     (when (nil? (.-reactWrapper reagent-component))
       (let [^js wrapper (fn [^js props]
                           (let [component-fn-ref (react/useRef nil)
                                 hiccup (use-reactive
                                          (if (nil? (.-current component-fn-ref))
                                            (loop [component-fn (.-comp props)]
                                              (let [reactive (reaction
                                                               (apply component-fn (.-args props)))
                                                    x @reactive]
                                                (if (fn? x)
                                                  ;; Loop until x is not a function.
                                                  (do
                                                    (dispose! reactive)
                                                    (recur x))
                                                  (do
                                                    (set! (.-current component-fn-ref) component-fn)
                                                    reactive))))
                                            (let [reactive (reaction
                                                             (apply (.-current component-fn-ref) (.-args props)))]
                                              ;; To keep the order of the React hooks consistent,
                                              ;; we need to run the reactive before use-reactive,
                                              ;; the same as we did in the first run defined above.
                                              @reactive
                                              reactive)))]
                             (as-element hiccup)))]
         (set! (.-displayName wrapper) (impl/compute-fn-display-name reagent-component))
         (set! (.-reactWrapper reagent-component) wrapper)))

     (.-reactWrapper reagent-component)))

#?(:cljs
   (defn as-element [hiccup]
     (cond
       (vector? hiccup)
       (let [x (first hiccup)
             key (-> hiccup meta :key)]
         (cond
           (fn? x)
           (let [[reagent-component & args] hiccup]
             (react/createElement (get-react-wrapper reagent-component)
                                  (-> #js {:comp reagent-component
                                           :args args}
                                      (impl/set-key-if-some key))))

           (= x :f>) ;; It invokes a Reagent component and make sure that we can call the hooks inside and still deref Ratoms.
           (let [[_f> reagent-component & args] hiccup]
             (react/createElement (get-react-wrapper reagent-component)
                                  (-> #js {:comp reagent-component
                                           :args args}
                                      (impl/set-key-if-some key))))

           (= x :>)
           (let [[_> react-component & args] hiccup
                 [props & children] (if (map? (first args))
                                      args
                                      (cons nil args))]
             (apply react/createElement react-component
                                        (-> (impl/clj->camel-js-props props)
                                            (impl/set-key-if-some key))
                                        (mapv as-element children)))

           (= x :r>) ;; "r" means "raw". It calls React components.
           (let [[_r> react-component js-props & children] hiccup]
             (apply react/createElement react-component
                                        (-> js-props
                                            (impl/set-key-if-some key))
                                        (mapv as-element children)))

           (= x :<>)
           (let [[_<> & args] hiccup
                 [props & children] (if (map? (first args))
                                      args
                                      (cons nil args))]
             (apply react/createElement react/Fragment
                                        (-> (clj->js props)
                                            (impl/set-key-if-some key))
                                        (mapv as-element children)))

           :else ;; Representation of a DOM element, like :div or "div"
           (let [[dom-element & args] hiccup
                 {:keys [element id classes]} (impl/parse-dom-element (name dom-element))
                 [props & children] (if (map? (first args))
                                      args
                                      (cons nil args))
                 classes (into classes (map name) (:class props))]
             (apply react/createElement element
                                        (-> props
                                            (cond-> id (assoc :id id))
                                            (cond->
                                              (seq classes)
                                              (-> (dissoc :class)
                                                  (assoc :className (str/join " " classes))))
                                            (set/rename-keys {:for :htmlFor})
                                            (impl/clj->camel-js-props)
                                            (impl/set-key-if-some key))
                                        (mapv as-element children)))))

       (seq? hiccup)
       (let [children hiccup]
         (to-array (mapv as-element children)))

       :else
       hiccup)))

(defmacro reaction [& bodies]
  `(reagent.ratom/make-reaction
     (fn [] ~@bodies)))

(defmacro with-let [bindings & bodies]
  (assert (vector? bindings)
          (str "with-let bindings must be a vector, not " (pr-str bindings)))
  (let [binding-vars (for [[var _expr] (partition 2 bindings)] var)
        binding-exprs (for [[_var expr] (partition 2 bindings)] expr)

        ;; Looks for a potential `finally` clause at the end of `bodies`
        [body-exprs finally-exprs] (let [last-expr (last bodies)]
                                     (if (and (list? last-expr)
                                              (= (first last-expr) 'finally))
                                       [(butlast bodies) (next last-expr)]
                                       [bodies nil]))]
    `(let [[~@binding-vars] (impl/use-eval-once
                              (fn []
                                [~@binding-exprs]))]
       ~(when (some? finally-exprs)
          `(impl/use-finally
             (fn []
               ~@finally-exprs)))
       ~@body-exprs)))

(defn adapt-react-class [react-component]
  (fn reagent-component [& args]
    (let [[props & children] (if (map? (first args))
                               args
                               (cons nil args))]
      (apply react/createElement react-component
                                 (clj->js props)
                                 (mapv as-element children)))))

(defn reactify-component [reagent-component]
  (fn react-component [^js props]
    (as-element [reagent-component (js->clj props)])))
