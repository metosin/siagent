(ns reagent.core
  (:require-macros [reagent.core :refer [reaction]])
  (:refer-clojure :exclude [atom])
  (:require ["react" :as react]
            [clojure.set :as set]
            [clojure.string :as str]
            [reagent.ratom :as ra]
            [reagent.util :as ru]
            [signaali.reactive :as sr]))

(defn atom [x]
  (ra/atom x))

(defn dispose! [x]
  (ra/dispose! x))

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
    (react/useSyncExternalStore subscribe get-snapshot)))

(declare as-element)

(defn- get-react-wrapper [^js reagent-component]
  (when (nil? (.-reactWrapper reagent-component))
    (let [^js wrapper (fn [^js props]
                        (let [hiccup (use-reactive
                                       (reaction
                                         (apply (.-comp props) (.-args props))))]
                          (as-element hiccup)))]
      (set! (.-displayName wrapper) (ru/compute-fn-display-name reagent-component))
      (set! (.-reactWrapper reagent-component) wrapper)))

  (.-reactWrapper reagent-component))

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
                                   (ru/set-key-if-some key))))

        (= x :f>) ;; It invokes a Reagent component and make sure that we can call the hooks inside and still deref Ratoms.
        (let [[_f> reagent-component & args] hiccup]
          (react/createElement (get-react-wrapper reagent-component)
                               (-> #js {:comp reagent-component
                                        :args args}
                                   (ru/set-key-if-some key))))

        (= x :>)
        (let [[_> react-component & args] hiccup
              [props & children] (if (map? (first args))
                                   args
                                   (cons nil args))]
          (apply react/createElement react-component
                                     (-> (ru/clj->camel-js-props props)
                                         (ru/set-key-if-some key))
                                     (mapv as-element children)))

        (= x :r>) ;; "r" means "raw". It calls React components.
        (let [[_r> react-component js-props & children] hiccup]
          (apply react/createElement react-component
                                     (-> js-props
                                         (ru/set-key-if-some key))
                                     (mapv as-element children)))

        (= x :<>)
        (let [[_<> & args] hiccup
              [props & children] (if (map? (first args))
                                   args
                                   (cons nil args))]
          (apply react/createElement react/Fragment
                                     (-> (clj->js props)
                                         (ru/set-key-if-some key))
                                     (mapv as-element children)))

        :else ;; Representation of a DOM element, like :div or "div"
        (let [[dom-element & args] hiccup
              {:keys [element id classes]} (ru/parse-dom-element (name dom-element))
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
                                         (ru/clj->camel-js-props)
                                         (ru/set-key-if-some key))
                                     (mapv as-element children)))))

    (seq? hiccup)
    (let [children hiccup]
      (to-array (mapv as-element children)))

    :else
    hiccup))
