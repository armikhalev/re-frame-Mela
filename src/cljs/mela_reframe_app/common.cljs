(ns mela-reframe-app.common
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [clojure.spec.alpha :as s]
            [cljs.pprint :as pp]))

;; Specs
(s/def ::placeholder string?)
;; TODO: figure out how spec works with functions passed as argument
;; (s/fdef >dis-search-input-entered
;;   :args (s/cat :value int?))

;; funcs
(defn search-field [placeholder >dis-search-input-entered]
  "Pure function: on-change calls passed in function with one value to dispatch"
  ;; spec-it
  (spec-it ::placeholder placeholder)
  ;; (spec-it ::dis-search-input-entered >dis-search-input-entered)
  ;;
  [:input.word-filter-input
   {:placeholder placeholder
    :autoFocus "autoFocus"
    :on-change
    #(>dis-search-input-entered (-> % .-target .-value))}])
