;; -*- indent-tabs-mode: nil -*-

(ns midje.util.laziness
  (:use [midje.util.form-utils :only [pred-cond]]))

(defn eagerly
  "Descend form, converting all lazy seqs into lists.
   Metadata is preserved. In the result all non-collections
   are identical? to those in the original form (as is
   their metadata). None of the collections are identical?
   even if they contain no lazy seqs."
  ;; Modified from clojure.walk/walk
  [form]
  (let [m #(with-meta % (meta form))]
    (pred-cond form
      (some-fn seq? list?)  (m (apply list (map eagerly form)))
      vector?               (m (vec (map eagerly form)))
      map?                  (m (into form (map eagerly form)))
      set?                  (m (into (if (sorted? form) (sorted-set) #{}) (map eagerly form)))
      :else                 form)))

