(ns reagent.ratom
  #?(:cljs (:require-macros [reagent.ratom :refer [reaction]]))
  (:refer-clojure :exclude [atom])
  (:require [signaali.reactive :as sr]))

(defn atom [x]
  (sr/create-state x {:propagation-filter-fn not=}))

(defn add-on-dispose! [a-ratom f]
  (sr/add-on-dispose-callback a-ratom f))

(defn dispose! [x]
  (sr/dispose x))

(defn make-reaction [f & {:keys [auto-run on-set on-dispose]}]
  (assert (nil? auto-run)) ;; because not supported
  (assert (nil? on-set))   ;; because not supported
  (let [r (sr/create-memo f {:propagation-filter-fn not=})]
    (when (some? on-dispose)
      (sr/add-on-dispose-callback r on-dispose))
    r))

(defn reactive? []
  (some? (sr/get-current-observer)))

(defmacro reaction [& bodies]
  `(make-reaction
     (fn [] ~@bodies)))
