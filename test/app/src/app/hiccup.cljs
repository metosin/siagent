(ns app.hiccup)

(defn item [props & children]
  (into [:li props] children))

(defn hiccup-demo []
  [:section {:data-testid "hiccup"}
   [:h2 "Basic hiccup features"]

   [:article {:data-testid "element-1"}
    [:h3 "Heavily defined hiccup element"]
    [:span#my-id.my-class1.my-class2 {:class [:my-class3 :my-class4]}
     "Some text"]]

   ["article" {:data-testid "element-2"}
    ["h3" "The same should work with strings"]
    ["span#my-id.my-class1.my-class2" {:class ["my-class3" "my-class4"]}
     "Some text"]]
   
   [:article {:data-testid "inline-style"
              :style {:color "pink"}}
    [:h3 "Inline style"]
    "Some pink text"]

   [:article {:data-testid "sequence"}
    [:h3 "Sequences are inlined"]
    [:ul
     (for [x (range 2)]
       ^{:key x} [:li "^{:key " x "} [:li " x "]"])
     (for [x (range 2)]
       [:li {:key x} "[:li {:key " x "} " x "]"])
     [:li "[:li single element in the middle]"]
     (for [x (range 2)]
       ^{:key x} [item "^{:key " x "} [item " x "]"])
     (for [x (range 2)]
       [item {:key x} "[item {:key " x "} " x "]"])]]

   [:article {:data-testid "fragment"}
    [:h3 "React fragment"]
    [:ul
     (for [x (range 2)]
       [:<> {:key x}
        [:li "element " x "a"]
        [:li "element " x "b"]])]]])
