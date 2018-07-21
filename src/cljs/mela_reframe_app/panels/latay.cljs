(ns mela-reframe-app.panels.latay
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.common :as common :refer [search-field]]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [clojure.spec.alpha :as spec]
            [cljs.pprint :as pp]
            [mela-reframe-app.subs :as subs :refer [>dis]]))

;; Components

(defn find-basic-word [word dict]
  "Takes 'word' string and 'dict' vector of maps that should contain patch to word [:attributes :front]"
  "Returns vector of maps that have 'word' as value of 'word [:attributes :front]'"
  (spec-it ::db/front word)
  (spec-it ::db/basic-words dict)
  (filter
   #(re-find
     (re-pattern word) (clojure.string/lower-case (get-in % [:attributes :front])))
   dict))

(defn basic-card-comp
  [attributes id flip? >dis-flip-card]
  ;;
  [:div.b-w-card-flip-container
   {:on-click #(>dis-flip-card flip? id)}
   [:div.b-w-card-flipper
    {:class (when flip? "b-w-card-flip")}
    [:div.b-w-card-card.b-w-card-frontCard (:front attributes)]
    [:div.b-w-card-card.b-w-card-backCard (:back attributes)]]])

;; Main

(defn latay-panel [>dis-basic-words-search-input-entered
                   basic-words
                   basic-words-search-input
                   >dis-flip-card]
  [:div
   [search-field "Type Mela basic word, then click on card" >dis-basic-words-search-input-entered]
   [:div.word-results-row
    (for [{:keys [id attributes]} (find-basic-word basic-words-search-input basic-words)]
      ^{:key id}
      [basic-card-comp
       ;;
       attributes
       id
       (:flip attributes)
       >dis-flip-card])]])
