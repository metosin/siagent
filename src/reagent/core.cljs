(ns reagent.core
  (:require-macros [reagent.core])
  (:refer-clojure :exclude [atom])
  (:require ["react" :as react]
            [clojure.string :as str]
            [reagent.ratom :as ra]
            [signaali.reactive :as sr]))

(defn atom [x]
  (ra/atom x))

(defn dispose! [x]
  (ra/dispose! x))

(defn use-reactive-node [reactive-node]
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

(defn- compute-fn-display-name [f]
  (let [name-fragments (-> (.-name ^js f)
                           (demunge)
                           (str/split "/"))]
    (str (str/join "." (butlast name-fragments))
         "/"
         (last name-fragments))))

(defn- set-key-if-some [js-props key]
  (cond
    (nil? key) js-props
    (nil? js-props) #js {:key (str key)}
    :else (js* "({...~{}, key: ~{}})" js-props (str key))))

(declare as-element)

(defn- get-react-wrapper [^js reagent-component]
  (when (nil? (.-reactWrapper reagent-component))
    (let [^js wrapper (fn [^js props]
                        (let [hiccup (use-reactive-node
                                       (ra/make-reaction
                                         (fn []
                                           (apply (.-comp props) (.-args props)))))]
                          (as-element hiccup)))]
      (set! (.-displayName wrapper) (compute-fn-display-name reagent-component))
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
                                   (set-key-if-some key))))

        (= x :f>) ;; It invokes a Reagent component and make sure that we can call the hooks inside and still deref Ratoms.
        (let [[_f> reagent-component & args] hiccup]
          (react/createElement (get-react-wrapper reagent-component)
                               (-> #js {:comp reagent-component
                                        :args args}
                                   (set-key-if-some key))))

        (= x :>)
        (let [[_> react-component & args] hiccup
              [props & children] (if (map? (first args))
                                   args
                                   (cons nil args))]
          (apply react/createElement react-component
                                     (-> (clj->js props)
                                         (set-key-if-some key))
                                     (mapv as-element children)))

        (= x :r>) ;; "r" means "raw". It calls React components.
        (let [[_r> react-component js-props & children] hiccup]
          (apply react/createElement react-component
                                     (-> js-props
                                         (set-key-if-some key))
                                     (mapv as-element children)))

        (= x :<>)
        (let [[_<> & args] hiccup
              [props & children] (if (map? (first args))
                                   args
                                   (cons nil args))]
          (apply react/createElement react/Fragment
                                     (-> (clj->js props)
                                         (set-key-if-some key))
                                     (mapv as-element children)))

        :else ;; Representation of a DOM element, like :div or "div"
        (let [[dom-element & args] hiccup
              [props & children] (if (map? (first args))
                                   args
                                   (cons nil args))]
          (apply react/createElement (name dom-element)
                                     (-> (clj->js props)
                                         (set-key-if-some key))
                                     (mapv as-element children)))))

    (seq? hiccup)
    (let [children hiccup]
      (to-array (mapv as-element children)))

    :else
    hiccup))
