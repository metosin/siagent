(ns app.core
  (:require [reagent.core :as r]
            [uix.core :as uix :refer [defui $]]
            [uix.dom :as dom]))

(defn reagent-sum-component [a b c]
  [:div "reagent sum = " (+ @a @b c)])

(defn reagent-sum-component-fn-in-fn [a b c]
  (let [d (r/atom 1000)]
    (fn [a b c]
      [:<>
       [:button {:on-click (fn [] (swap! d inc))} "inc"]
       " state-d = " @d
       [:div "reagent sum fn-in-fn = " (+ @a @b c @d)]])))

(defn reagent-sum-component-with-let [a b c]
  (r/with-let [_ (prn "with-let starts")]
    [:div "reagent sum with let = " (+ @a @b c)]
    (finally (prn "with-let finishes"))))

(defn reagent-sum-component-with-hooks [a b c]
  (let [[d set-d] (uix/use-state 1000)]
    [:<> {:key "xxx"} ;; Fragments can only have a "key" in their props.
     [:button {:on-click (fn [] (set-d inc))} "inc"]
     " state-d = " d
     [:div "reagent sum with hooks = " (+ @a @b c d)]]))

(defn react-sum-component [^js props]
  (let [a (r/use-reactive (.-a props))
        b (r/use-reactive (.-b props))
        c (.-c props)]
    ($ :div "react sum = " (+ a b c))))

(defui react-component-with-children [{:keys [children]}]
  ($ :div "My children are:"
     ($ :div children)))

(def state-a (r/atom 10))
(def state-b (r/atom 0))

(defn section [_name _children]
  (let [is-showing (r/atom false)]
    (fn [name children]
      [:div
       [:label
        [:input {:type "checkbox"
                 :value @is-showing
                 :onChange #(swap! is-showing not)}]
        name]
       (when @is-showing
         children)])))

(defui app []
  (let [[state-c set-state-c] (uix/use-state 100)]
    ($ :<>
       ($ :main "Hello, world!")
       (r/as-element
         [:ul.my-class1.my-class2#my-id {:class [:my-class3 :my-class4]}
          [:li
           [:#some-id "This is in a div."]
           [:.some-class "This too."]]
          [:li
           [:button {:on-click (fn [] (swap! state-a inc))} "inc"]
           " state-a = " (r/use-reactive state-a)]
          ["li" {:style {:color "pink"}}
           [:button {:on-click (fn [] (swap! state-b inc))} "inc"]
           " state-b = " (r/use-reactive state-b)]
          [:li
           [:button {:on-click (fn [] (set-state-c inc))} "inc"]
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
           [reagent-sum-component-fn-in-fn state-a state-b state-c]
           [section "With let"
            [reagent-sum-component-with-let state-a state-b state-c]]
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
