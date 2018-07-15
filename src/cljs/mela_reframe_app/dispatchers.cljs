(ns mela-reframe-app.dispatchers
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.subs :as subs :refer [>dis]]))

(defn >dis-search-input-entered
  [value]
  (>dis [:search-input-entered value]))

(defn >dis-set-show-menu
  [set-show-menu]
  (>dis [:set-show-menu set-show-menu]))

(defn >dis-change-lang
  [lang]
  (>dis [:change-lang lang]))

(defn >dis-grammar-card-info-clicked
  [id]
  (>dis [:grammar-card-info-clicked id]))

(defn >dis-show-grammar-card
  [id]
  (>dis [:show-grammar-card]))

(defn >dis-hide-grammar-card
  [id]
  (>dis [:hide-grammar-card]))
