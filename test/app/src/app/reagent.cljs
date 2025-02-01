(ns app.reagent
  (:require [reagent.core :as r]))

(defonce global-atom-1 (r/atom 0))
(defonce global-atom-2 (r/atom 0))
(defonce global-atom-3 (r/atom 0))
(defonce global-atom-4 (r/atom 0))
(defonce global-atom-5 (r/atom 0))
(defonce global-atom-6 (r/atom 0))

(defn counter [label atom]
  [:div
   label " = " @atom " "
   [:button {:on-click (fn [] (swap! atom inc))} "inc"]])

(defn sum-atom-value [atom-a value-b]
  [:div "atom-a + value-b = " (+ @atom-a value-b)])

(defn fn-in-fn [_value-a]
  (let [atom-b (r/atom 0)]
    (fn []
      (let [atom-c (r/atom 0)]
        (fn [value-a]
          [:<>
           "b and c are atoms defined in the wrapping fns."
           [counter "b" atom-b]
           [counter "c" atom-c]
           [:div "value-a + atom-b + atom-c = " (+ value-a @atom-b @atom-c)]])))))

(defn with-let-component [value-a]
  (r/with-let [atom-b (r/atom 0)
               atom-c (r/atom 0)]
    [:<>
     "b and c are atoms defined in the wrapping with-let."
     [counter "b" atom-b]
     [counter "c" atom-c]
     [:div "value-a + atom-b + atom-c = " (+ value-a @atom-b @atom-c)]]))

(defn with-let-finally-component [value-a]
  (r/with-let [atom-b (r/atom 0)
               atom-c (r/atom 0)]
    [:<>
     "b and c are atoms defined in the wrapping with-let."
     [counter "b" atom-b]
     [counter "c" atom-c]
     [:div "value-a + atom-b + atom-c = " (+ value-a @atom-b @atom-c)]]
    (finally
      (swap! global-atom-6 inc))))

(defn closable-section [_name _children]
  (let [is-showing (r/atom false)]
    (fn [name children]
      [:<>
       [:div
        [:label
         [:input {:type "checkbox"
                  :value @is-showing
                  :on-change #(swap! is-showing not)}]
         name]]
       (when @is-showing
         children)])))

(defn reagent-demo []
  [:section {:data-testid "reagent"}
   [:h2 "Reagent features"]

   [:article
    [:h3 "Component reactivity"]
    [:div
     [counter "a" global-atom-1]
     [counter "b" global-atom-2]
     [sum-atom-value global-atom-1 @global-atom-2]]]

   [:article
    [:h3 "fn in a fn ..."]
    [counter "a" global-atom-3]
    [fn-in-fn @global-atom-3]]

   [:article
    [:h3 "with-let"]
    [counter "a" global-atom-4]
    [with-let-component @global-atom-4]]

   [:article
    [:h3 "with-let & finally"]
    [counter "a" global-atom-5]
    [:div "The content was finalized " @global-atom-6 " times."]
    [closable-section "Make the content exist"
     [with-let-finally-component @global-atom-5]]]])
