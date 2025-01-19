(ns app.core
  (:require [reagent.core :as r]
            [reagent.ratom :as ra]
            [uix.core :as uix :refer [defui $]]
            [uix.dom :as dom]))

(defn reagent-sum-component [a b c]
  [:div "reagent sum = " (+ @a @b c)])

(defn reagent-sum-component-with-hooks [a b c]
  (let [[d set-d] (uix/use-state 1000)]
    [:<> {:key "xxx"} ;; Fragments can only have a "key" in their props.
     [:button {:onClick (fn [] (set-d inc))} "inc"]
     " state-d = " d
     [:div "reagent sum with hooks = " (+ @a @b c d)]]))

(defn react-sum-component [^js props]
  (let [hiccup (r/use-reactive
                 (ra/make-reaction
                   (fn []
                     [:div "react sum = " (+ @(.-a props) @(.-b props) (.-c props))])))]
    (r/as-element hiccup)))

(defui react-component-with-children [{:keys [children]}]
  ($ :div "My children are:"
     ($ :div children)))

(def state-a (r/atom 10))
(def state-b (r/atom 0))

(defui app []
  (let [[state-c set-state-c] (uix/use-state 100)]
    ($ :<>
       ($ :main "Hello, world!")
       (r/as-element
         [:ul
          [:li
           [:button {:onClick (fn [] (swap! state-a inc))} "inc"]
           " state-a = " (r/use-reactive state-a)]
          ["li" {:style {:color "pink"}}
           [:button {:onClick (fn [] (swap! state-b inc))} "inc"]
           " state-b = " (r/use-reactive state-b)]
          [:li
           [:button {:onClick (fn [] (set-state-c inc))} "inc"]
           " state-c = " state-c]
          [:li
           "Sequences and keys:"
           (for [index (range 3)]
             ^{:key index} [:div "index = " index])]
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
