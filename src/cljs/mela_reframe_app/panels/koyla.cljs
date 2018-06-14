(ns mela-reframe-app.panels.koyla
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.subs :as subs]))

(defn search-field []
  [:input.word-filter-input
   {:on-change
    #(re-frame/dispatch [:search-input-entered (-> % .-target .-value)])}]) ;; side-effect TODO: figure out a pure way

(defn find-word [word dict]
  (filter
   #(re-find (re-pattern word) (:word %))
   dict))

(defn card-comp []
  (let []
      (fn [{:keys [word la comment]}]
        [:ul.koyla-result-ul
         [:li "English: " word]
         [:li "Mela: " la]
         [:li "Comment: " comment]])))

(defn koyla-panel [koyla cur-input]
  [:div
   [ search-field ]
   [:div.word-results-row
    (for [card (find-word cur-input koyla)]
      ^{:key (str (:word card)-(random-uuid))} ;; random-uuid technically impure but it is worth the simplicity
      [card-comp card])]])

