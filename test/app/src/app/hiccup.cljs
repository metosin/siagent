(ns app.hiccup)

(defn hiccup-demo []
  [:section {:data-testid "hiccup"}
   [:h2 "Basic hiccup features"]

   [:article
    [:h3 "Heavily defined hiccup element"]
    [:span.my-class1.my-class2#my-id {:class [:my-class3 :my-class4]}
     "Some text"]]

   ["article"
    ["h3" "The same should work with strings"]
    ["span.my-class1.my-class2#my-id" {:class ["my-class3" "my-class4"]}
     "Some text"]]

   [:article
    [:h3 "The default element is a \"div\""]
    [:#some-id "This is in a div."]
    [:.some-class "This too."]]

   [:article {:style {:color "pink"}}
    [:h3 "Inline style"]
    "Some pink text"]

   [:article
    [:h3 "Sequences are inlined"]
    [:ul
     (for [x (range 3)]
       ^{:key x} [:li "^{:key " x "} [:li " x "]"])
     [:li "[:li single element in the middle]"]
     (for [y (range 3)]
       [:li {:key y} "[:li {:key " y "} " y "]"])]]

   [:article
    [:h3 "React fragment"]
    (for [x (range 3)]
      [:<> {:key x}
       [:div "element " x "a"]
       [:div "element " x "b"]])]])
