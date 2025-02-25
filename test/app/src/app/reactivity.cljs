(ns app.reactivity
  (:require ["react" :as react]
            [reagent.core :as r]
            [uix.core :refer [$ defui]]))

(defonce count-atom-1 (r/atom 0))

(defn reactivity-in-reagent []
  (let [count @count-atom-1]
    (prn [:reactivity-in-reagent count])
    [:button {:onClick (fn [^js _event]
                         (reset! count-atom-1 (inc count)))}
     "reagent component: " count]))

(defonce count-atom-2 (r/atom 0))

(defui reactivity-in-uix []
  (let [count (r/use-reactive count-atom-2)]
    (prn [:reactivity-in-uix count])
    ($ :button {:onClick (fn [^js _event]
                           (reset! count-atom-2 (inc count)))}
       "uix component: " count)))

(defui reactivity-demo []
  (let [[counter set-counter] (react/useState 0)]
    ($ :<>
       ($ :button {:onClick (fn [_] (set-counter inc))} "parent component: " counter)
       ($ :div (r/as-element [reactivity-in-reagent]))
       ($ :div ($ reactivity-in-uix))
       ,)))
