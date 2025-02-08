(ns reagent.impl.core
  (:require #?(:cljs ["react" :as react])
            [clojure.set :as set]
            [clojure.string :as str]))

#?(:cljs
   (defn set-key-if-some [js-props key]
     (if (nil? key)
       js-props
       (let [js-props (or js-props #js {})]
         (set! (.-key js-props) (str key))
         js-props))))

#?(:cljs
   (defn compute-fn-display-name [f]
     (let [name-fragments (-> (.-name ^js f)
                              (demunge)
                              (str/split "/"))]
       (str (str/join "." (butlast name-fragments))
            "/"
            (last name-fragments)))))

(defn parse-dom-element [s]
  (reduce (fn [acc part]
            (case (subs part 0 1)
              "." (update acc :classes conj (subs part 1))
              "#" (assoc acc :id (subs part 1))
              (assoc acc :element part)))
          {:element "div"
           :id nil
           :classes []}
          (re-seq #"[#.]?[^#.]+" s)))

(defn- capitalize [s]
  (if (< (count s) 2)
    (str/upper-case s)
    (str (str/upper-case (subs s 0 1)) (subs s 1))))

(defn- camelize-prop-key [prop-key]
  (if (string? prop-key)
    prop-key
    (let [prop-name (name prop-key)]
      (if (or (str/starts-with? prop-name "data-")
              (str/starts-with? prop-name "aria-"))
        prop-name
        (let [[start & parts] (str/split prop-name "-")]
          (apply str start (map capitalize parts)))))))

#?(:cljs
   (defn clj-props->js-props [props meta-key id classes]
      (let [react-key (or meta-key (:key props))
            classes (-> classes
                        (conj (get props :class))
                        flatten
                        (->> (filter some?)
                             (map name)))]
        (-> props
            (cond->
              (some? react-key)
              (assoc :key react-key))
            (cond->
              (some? id)
              (assoc :id id))
            (dissoc :class)
            (cond->
              (seq classes)
              (assoc :className (str/join " " classes)))
            (set/rename-keys {:for :htmlFor})
            (update-keys camelize-prop-key)
            (clj->js)))))

#?(:cljs
   (defn use-eval-once [f]
     ;; We do not use built-in `react/useMemo` or `react/useEffect`
     ;; because f may both contain side effects and return a value.
     (let [result-ref (react/useRef nil)]
       (when (nil? (.-current result-ref))
         (set! (.-current result-ref) (f)))
       (.-current result-ref))))

#?(:cljs
   (defn use-finally [f]
     (react/useEffect (fn [] f)
                      #js [])))
