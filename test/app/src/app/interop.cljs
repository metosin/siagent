(ns app.interop
  (:require [reagent.core :as r]
            [uix.core :as uix :refer [defui $]]))

(defonce global-atom-1 (r/atom 0))
(defonce global-atom-2 (r/atom 0))

(defn atom-counter [label atom]
  [:div
   label " = " @atom " "
   [:button {:on-click (fn [] (swap! atom inc))} "inc"]])

(defn state-counter [label value setter]
  [:div
   label " = " value " "
   [:button {:on-click (fn [] (setter inc))} "inc"]])

(defn reagent-sum-component-with-hooks [value-a]
  (r/with-let [atom-b (r/atom 0)]
    (let [[value-c set-c] (uix/use-state 0)]
      [:<>
       [atom-counter "atom b" atom-b]
       [state-counter "state c" value-c set-c]
       [:div "value-a + atom-b + state-c = " (+ value-a @atom-b value-c)]])))

;; This is a React component
(defn react-component-with-children [^js props]
  ($ :div
     (.-title props)
     ($ :div {:style {:margin-left "1em"
                      :padding "0.5em"
                      :border "1px"
                      :border-style "solid"
                      :color (.-color props)}}
        (.-children props))))

;; This is a React component
(defn react-component-doubling-global-atom-value [^js _props]
  (let [a (r/use-reactive global-atom-2)]
    ($ :div "a + a = " (+ a a))))

(defn interop-demo []
  [:section {:data-testid "interop"}
   [:h2 "Interop with React"]

   [:article
    [:h3 "Reagent components with hooks"]
    [atom-counter "a" global-atom-1]

    [:h4 "The form [reagent-comp ,,,]"]
    [reagent-sum-component-with-hooks @global-atom-1]

    [:h4 "The form [:f> reagent-comp ,,,]"]
    [:f> reagent-sum-component-with-hooks @global-atom-1]]

   [:article
    [:h3 "Calling React components with props and children"]

    [:h4 "The form [:> react-comp {,,,} & hiccup-children]"]
    [:> react-component-with-children {:title "My title"
                                       :color "lightSeaGreen"}
     [:div "Child 1"]
     [:div "Child 2"]]

    [:h4 "The form [:r> react-comp #js {,,,} & hiccup-children]"]
    [:r> react-component-with-children #js {:title "My title"
                                            :color "mediumOrchid"}
     [:div "Child 1"]
     [:div "Child 2"]]]

   [:article
    [:h3 "React component reacting to an atom via r/use-reactive hook"]
    [atom-counter "a" global-atom-2]
    [:> react-component-doubling-global-atom-value]]

   [:article
    [:h3 "Reactify a Reagent component"]
    (let [react-comp (r/reactify-component
                       (fn reagent-component [{:keys [title color children]}]
                         [:div title
                          [:div {:style {:marginLeft "1em"
                                         :padding "0.5em"
                                         :border "1px"
                                         :borderStyle "solid"
                                         :color color}}
                           children]]))]
      [:> react-comp {:title "My title"
                      :color "dodgerBlue"}
        [:div "Child 1"]
        [:div "Child 2"]])]

   [:article
    [:h3 "Reagentify a React component"]
    (let [reagent-comp (r/adapt-react-class
                         (fn react-component [^js props]
                           ($ :div (.-title props)
                              ($ :div {:style {:marginLeft "1em"
                                               :padding "0.5em"
                                               :border "1px"
                                               :borderStyle "solid"
                                               :color (.-color props)}}
                                 (.-children props)))))]
      [reagent-comp {:title "My title"
                     :color "darkSalmon"}
        [:div "Child 1"]
        [:div "Child 2"]])]])
