(ns reagent.util
  (:require [clojure.string :as str]))

(defn set-key-if-some [js-props key]
  (if (nil? key)
    js-props
    (let [js-props (or js-props #js {})]
      (set! (.-key js-props) (str key))
      js-props)))

(defn compute-fn-display-name [f]
  (let [name-fragments (-> (.-name ^js f)
                           (demunge)
                           (str/split "/"))]
    (str (str/join "." (butlast name-fragments))
         "/"
         (last name-fragments))))

(defn- capitalize [s]
  (if (< (count s) 2)
    (str/upper-case s)
    (str (str/upper-case (subs s 0 1)) (subs s 1))))

(defn- camelize-prop-key [prop-key]
  (if (string? prop-key)
    prop-key
    (let [prop-name (name prop-key)]
      (if (or (str/starts-with? prop-name "data")
              (str/starts-with? prop-name "aria"))
        prop-name
        (let [[start & parts] (str/split prop-name "-")]
          (apply str start (map capitalize parts)))))))

(defn clj->camel-js-props [props]
  (-> props
      (update-keys camelize-prop-key)
      (clj->js)))
