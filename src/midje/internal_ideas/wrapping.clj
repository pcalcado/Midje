;; -*- indent-tabs-mode: nil -*-

(ns midje.internal-ideas.wrapping
  (:use
    [midje.util.form-utils :only [first-named?]]
    [utilize.seq :only (separate)]
    [midje.util.thread-safe-var-nesting :only [namespace-values-inside-out 
                                               set-namespace-value
                                               with-pushed-namespace-values]])
  (:require [clojure.zip :as zip] 
  	        [midje.util.unify :as unify]))

(defn midje-wrapped
  "This is used to prevent later wrapping passes from processing 
   the code-that-produces-the-value."
  [value] value)

(defn already-wrapped? [form]
  (first-named? form "midje-wrapped"))

(defn multiwrap [form [wrapper & more-wrappers]]
  (if wrapper
    (recur (unify/inject-form wrapper form) more-wrappers)
    `(midje-wrapped ~form)))

;; stashing wrapping targets

(defn wrappers []
  (namespace-values-inside-out :midje/wrappers))

(defn with-wrapping-target [what target]
  (vary-meta what merge {:midje/wrapping-target target}))

(defn for-wrapping-target? [target]
  (fn [actual] (= target (:midje/wrapping-target (meta actual)))))

(defmacro with-additional-wrappers [final-wrappers form]
  `(with-pushed-namespace-values :midje/wrappers ~final-wrappers
    ~form))

(defn put-wrappers-into-effect [wrappers]
  (let [[immediates deferred] (separate (for-wrapping-target? :contents) wrappers)]
    (set-namespace-value :midje/wrappers (list wrappers))
    (multiwrap "unimportant-value" immediates)))

(defn forms-to-wrap-around [wrapping-target]
  (filter (for-wrapping-target? wrapping-target) (wrappers)))