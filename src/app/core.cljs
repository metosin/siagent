(ns app.core
  (:require ["react" :as react]
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

(defn reagent-component-wrapper [^js props]
  (let [hiccup (use-reactive-node
                 (sr/create-memo
                   (fn []
                     (apply (.-comp props) (.-args props)))))]
    (as-element hiccup)))

(defn as-element [hiccup]
  (cond
    (vector? hiccup)
    (let [tag (first hiccup)]
      (cond
        (fn? tag)
        (react/createElement reagent-component-wrapper
                             #js {:comp tag
                                  :args (next hiccup)})

        (= tag :f>) ;; It invokes a Reagent component and make sure that we can call the hooks inside and still deref Ratoms.
        (react/createElement reagent-component-wrapper
                             #js {:comp (fnext hiccup)
                                  :args (nnext hiccup)})

        (= tag :>)
        (let [tag (fnext hiccup)
              [props children] (if (and (> (count hiccup) 2)
                                        (map? (nth hiccup 2)))
                                 [(nth hiccup 2) (next (nnext hiccup))]
                                 [nil (nnext hiccup)])]
          (apply react/createElement tag
                                     (clj->js props)
                                     (mapv as-element children)))

        (= tag :r>) ;; "r" means "raw". It calls React components.
        (let [[_ tag props & children] hiccup]
          (apply react/createElement tag
                                     props ;; Raw, no conversions on the props. The user should pass a #js {}.
                                     (mapv as-element children)))

        (= tag :<>)
        (let [[props children] (if (and (> (count hiccup) 1)
                                        (map? (nth hiccup 1)))
                                 [(nth hiccup 1) (nnext hiccup)]
                                 [nil (next hiccup)])]
          (apply react/createElement react/Fragment
                                     (clj->js props)
                                     (mapv as-element children)))

        :else
        (let [[props children] (if (and (> (count hiccup) 1)
                                        (map? (nth hiccup 1)))
                                 [(nth hiccup 1) (nnext hiccup)]
                                 [nil (next hiccup)])]
          (apply react/createElement (name tag)
                                     (clj->js props)
                                     (mapv as-element children)))))

    (seq? hiccup)
    (to-array (mapv as-element hiccup))

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
