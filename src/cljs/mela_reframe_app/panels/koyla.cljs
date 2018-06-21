(ns mela-reframe-app.panels.koyla
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [clojure.spec.alpha :as spec]
            [mela-reframe-app.subs :as subs :refer [>dis]]))

;; (def check-spec-interceptor (re-frame/after (partial check-and-throw :re-frame.db/app-db)))

;; view

(defn search-field [cur-lang]
  [:input.word-filter-input
   {:placeholder (if (= cur-lang "English")
                   (str "Type any English word to translate")
                   (str "Ta fasayla e la day lapey fe Mela"))
    :autoFocus "autoFocus"
    :on-change
    #(>dis [:search-input-entered (-> % .-target .-value)])}]) ;; side-effect TODO: figure out a pure way

(defn find-word [word dict]
  "Takes 'word' string and 'words' vector of maps that should contain :word key with string."
  "Returns vector of maps with :word :la :comment keys"
  ;; spec-it
  (spec-it ::db/word word)
  (spec-it ::db/words dict)
  ;;
  (filter
   #(re-find (re-pattern word) (:word %))
   dict))

;; TODO: should show Mela to English card as well
(defn card-comp [{:keys [word la comment]}]
  [:ul.koyla-result-ul
   [:li [:strong "English: "] word]
   [:li [:strong "Mela: "] la]
   [:li [:strong "Comment: "] comment]])

(defn koyla-panel [koyla cur-input cur-lang target-lang]
  [:div
   [:label.koyla-source-label cur-lang]
   [ search-field cur-lang ]

   [:div.word-results-row
    [:label.koyla-target-label target-lang]
    [:button.koyla-change-button
     {:type "submit"
      :on-click #(>dis [:change-lang target-lang])}
     (str "Change to " target-lang)]

    ;; word cards
    (for [card (find-word cur-input koyla)]
      ^{:key (str (:word card)-(random-uuid))} ;; random-uuid technically impure but it is worth the simplicity
      [card-comp card])]
   ])

;; Specs
