(ns mela-reframe-app.panels.latay
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.common :as common :refer [search-field]]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [clojure.spec.alpha :as spec]
            [cljs.pprint :as pp]
            [mela-reframe-app.subs :as subs :refer [>dis]]))
(defn basic-card-comp
  [attributes id flip? >dis-flip-card]
  ;;
  [:div.b-w-card-flip-container
   {:on-click #(>dis-flip-card flip? id)}
   [:div.b-w-card-flipper
    {:class (when flip? "b-w-card-flip")}
    [:div.b-w-card-card.b-w-card-frontCard (:front attributes)]
    [:div.b-w-card-card.b-w-card-backCard (:back attributes)]]])

(defn latay-panel [>dis-basic-words-search-input-entered
                   basic-words
                   >dis-flip-card]
  [:div
   [search-field "Type Mela basic word, then click on card" >dis-basic-words-search-input-entered]
   [:div.word-results-row
    (for [{:keys [id attributes]} basic-words]
      ^{:key id}
      [basic-card-comp
       ;;
       attributes
       id
       (:flip attributes)
       >dis-flip-card])]])
