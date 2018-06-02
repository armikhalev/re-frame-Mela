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
         [:li "English: " word]
         [:li "Mela: " la]
         [:li "Comment: " comment]]
        #_[:div (str @koyla)])))

#_(defn test-comp [el] (js/console.log (str (:word el))))
(defn request-it-button
  []
  [:button {:class "button-class"
         :on-click  #(re-frame/dispatch [:request-it])}  ;; get data from the server !!
   "I want it, now!"])

(defn koyla-panel []
  [:div 
   [request-it-button]
   [ search-field ]
   (let [koyla (re-frame/subscribe [::subs/words])
         cur-input (re-frame/subscribe [::subs/search-input])]
        [:div
         (for [card (find-word @cur-input @koyla)]
           ^{:key (:word card)} 
           [card-comp card])])])

