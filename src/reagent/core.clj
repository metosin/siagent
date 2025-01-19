(ns reagent.core)

(defmacro reaction [& body]
  `(reagent.ratom/make-reaction
    (fn [] ~@body)))
