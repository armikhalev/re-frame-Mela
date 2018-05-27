(ns mela-reframe-app.panels.koyla
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.subs :as subs]))
(defn search-field []
  [:input {:on-change #(re-frame/dispatch [:search-input-entered (-> % .-target .-value)])}])

(defn find-word [word dict]
  (filter
   #(re-find (re-pattern word) (:word %))
   dict))

(defn koyla-panel []
  [:div 
   [ search-field ]
   (let [koyla (re-frame/subscribe [::subs/words])
         cur-input (re-frame/subscribe [::subs/search-input])]
     [:p "Looking for: "]
     (let [{:keys [word la comment]} (first (find-word @cur-input @koyla))]
        [:div
         [:div "English: " word]
         [:div "Mela: " la]
         [:div "Comment: " comment]
         #_[:div (str @koyla)]])
     )])
