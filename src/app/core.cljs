(ns app.core
  (:require ["react" :as react]
            [clojure.string :as str]
            [signaali.reactive :as sr]
            [uix.core :as uix :refer [defui $]]
            [uix.dom :as dom]))

(defn use-reactive-node [reactive-node]
  (let [[subscribe get-snapshot] (uix/use-memo (fn []
                                                 ;;(prn "in use-memo")
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
                                               [reactive-node])]
    (react/useSyncExternalStore subscribe get-snapshot)))

(declare as-element)

(defn- compute-fn-display-name [f]
  (let [name-fragments (-> (.-name ^js f)
                           (demunge)
                           (str/split "/"))]
    (str (str/join "." (butlast name-fragments))
         "/"
         (last name-fragments))))

(defn- get-react-wrapper [^js reagent-component]
  (when (nil? (.-reactWrapper reagent-component))
    (let [^js wrapper (fn [^js props]
                        (let [hiccup (use-reactive-node
                                       (sr/create-memo
                                         (fn []
                                           (apply (.-comp props) (.-args props)))))]
                          (as-element hiccup)))]
      (set! (.-displayName wrapper) (compute-fn-display-name reagent-component))
      (set! (.-reactWrapper reagent-component) wrapper)))

  (.-reactWrapper reagent-component))

(defn as-element [hiccup]
  (cond
    (vector? hiccup)
    (let [x (first hiccup)]
      (cond
        (fn? x)
        (let [[reagent-component & args] hiccup]
          (react/createElement (get-react-wrapper reagent-component)
                               #js {:comp reagent-component
                                    :args args}))

        (= x :f>) ;; It invokes a Reagent component and make sure that we can call the hooks inside and still deref Ratoms.
        (let [[_f> reagent-component & args] hiccup]
          (react/createElement (get-react-wrapper reagent-component)
                               #js {:comp reagent-component
                                    :args args}))

        (= x :>)
        (let [[_> react-component & args] hiccup
              [props & children] (if (map? (first args))
                                   args
                                   (cons nil args))]
          (apply react/createElement react-component
                                     (clj->js props)
                                     (mapv as-element children)))

        (= x :r>) ;; "r" means "raw". It calls React components.
        (let [[_r> react-component props & children] hiccup]
          (apply react/createElement react-component
                                     props ;; Raw, no conversions on the props. The user should pass a #js {}.
                                     (mapv as-element children)))

        (= x :<>)
        (let [[_<> & args] hiccup
              [props & children] (if (map? (first args))
                                   args
                                   (cons nil args))]
          (apply react/createElement react/Fragment
                                     (clj->js props)
                                     (mapv as-element children)))

        :else ;; Representation of a DOM element, like :div or "div"
        (let [[dom-element & args] hiccup
              [props & children] (if (map? (first args))
                                   args
                                   (cons nil args))]
          (apply react/createElement (name dom-element)
                                     (clj->js props)
                                     (mapv as-element children)))))

    (seq? hiccup)
    (let [children hiccup]
      (to-array (mapv as-element children)))

    :else
    hiccup))


(defn reagent-sum-component [a b c]
  [:div "reagent sum = " (+ @a @b c)])

(defn reagent-sum-component-with-hooks [a b c]
  (let [[d set-d] (uix/use-state 1000)]
    [:<> {:key "xxx"} ;; Fragments can only have a "key" in their props.
     [:button {:onClick (fn [] (set-d inc))} "inc"]
     " state-d = " d
     [:div "reagent sum with hooks = " (+ @a @b c d)]]))

(defn react-sum-component [^js props]
  (let [hiccup (use-reactive-node
                 (sr/create-memo
                   (fn []
                     [:div "react sum = " (+ @(.-a props) @(.-b props) (.-c props))])))]
    (as-element hiccup)))

(defui react-component-with-children [{:keys [children]}]
  ($ :div "My children are:"
     ($ :div children)))

(def state-a (sr/create-state 10))
(def state-b (sr/create-state 0))

(defui app []
  (let [[state-c set-state-c] (uix/use-state 100)]
    ($ :<>
       ($ :main "Hello, world!")
       (as-element
         [:ul
          [:li
           [:button {:onClick (fn [] (swap! state-a inc))} "inc"]
           " state-a = " (use-reactive-node state-a)]
          ["li" {:style {:color "pink"}}
           [:button {:onClick (fn [] (swap! state-b inc))} "inc"]
           " state-b = " (use-reactive-node state-b)]
          [:li
           [:button {:onClick (fn [] (set-state-c inc))} "inc"]
           " state-c = " state-c]
          [:li
           "Sequence:"
           (for [index (range 3)]
             [:div {:key index} "index = " index])]
          [:li
           [:> react-component-with-children
            [:div "child 1"]
            [:div "child 2"]]]
          [:li
           ;; Reagent component invocations
           [reagent-sum-component state-a state-b state-c]
           [:f> reagent-sum-component-with-hooks state-a state-b state-c]

           ;; React component invocations
           [:> react-sum-component {:a state-a
                                    :b state-b
                                    :c state-c}]
           [:r> react-sum-component #js {:a state-a
                                         :b state-b
                                         :c state-c}]]]))))

(defonce root
  (dom/create-root (js/document.getElementById "app")))

(defn render []
  (dom/render-root ($ app) root))

(defn ^:export init []
  (render))
