(ns reagent.ratom
  (:refer-clojure :exclude [atom])
  (:require [signaali.reactive :as sr]
            [signaali.mutable.stack :as stack]))

(defn atom [x]
  (sr/create-state x {:propagation-filter-fn not=}))

(defn add-on-dispose! [a-ratom f]
  (sr/add-on-dispose-callback a-ratom f))

(defn dispose! [x]
  (sr/dispose x))

(defn make-reaction [f]
  (sr/create-memo f {:propagation-filter-fn not=}))

(defn reactive? []
  (pos? (stack/count sr/observer-stack)))
