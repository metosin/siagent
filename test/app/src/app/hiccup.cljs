(ns app.hiccup)

(defn hiccup-demo []
  [:section {:data-testid "hiccup"}
   [:h2 "Basic hiccup features"]

   [:article {:data-testid "element-1"}
    [:h3 "Heavily defined hiccup element"]
    [:span.my-class1.my-class2#my-id {:class [:my-class3 :my-class4]}
     "Some text"]]

   ["article" {:data-testid "element-2"}
    ["h3" "The same should work with strings"]
    ["span.my-class1.my-class2#my-id" {:class ["my-class3" "my-class4"]}
     "Some text"]]

   [:article {:data-testid "element-3"}
    [:h3 "The default element is a \"div\""]
    [:#some-id "This is in a div."]
    [:.some-class "This too."]]

   [:article {:data-testid "inline-style"
              :style {:color "pink"}}
    [:h3 "Inline style"]
    "Some pink text"]

   [:article {:data-testid "sequence"}
    [:h3 "Sequences are inlined"]
    [:ul
     (for [x (range 2)]
       ^{:key x} [:li "^{:key " x "} [:li " x "]"])
     [:li "[:li single element in the middle]"]
     (for [y (range 2)]
       [:li {:key y} "[:li {:key " y "} " y "]"])]]

   [:article {:data-testid "fragment"}
    [:h3 "React fragment"]
    [:ul
     (for [x (range 2)]
       [:<> {:key x}
        [:li "element " x "a"]
        [:li "element " x "b"]])]]])
