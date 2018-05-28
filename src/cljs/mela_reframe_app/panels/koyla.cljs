(ns mela-reframe-app.panels.koyla
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.subs :as subs]))
(defn search-field []
  [:input.word-filter-input {:on-change #(re-frame/dispatch [:search-input-entered (-> % .-target .-value)])}])

(defn find-word [word dict]
  (filter
   #(re-find (re-pattern word) (:word %))
   dict))

(defn card-comp []
  (let []
      (fn [{:keys [word la comment]}]
        [:ul.koyla-result-ul
         ^{:key (:id word)} 
         [:li "English: " word]
         [:li "Mela: " la]
         [:li "Comment: " comment]]
        #_[:div (str @koyla)])))

(defn koyla-panel []
  [:div 
   [ search-field ]
   (let [koyla (re-frame/subscribe [::subs/words])
         cur-input (re-frame/subscribe [::subs/search-input])]
        [:div
         (for [card (find-word @cur-input @koyla)]
           [card-comp card])])])
