(ns mela-reframe-app.panels.koyla
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [clojure.spec.alpha :as spec]
            [mela-reframe-app.subs :as subs :refer [>dis]]))
;; Specs
(spec/def ::placeholder string?)

;; view

(defn search-field [cur-lang placeholder >dis-search-input-entered]
  "Pure function: on-change calls passed in function with one value to dispatch"
  ;; spec-it
  (spec-it ::db/cur-lang cur-lang)
  (spec-it ::placeholder placeholder)
  ;;
  [:input.word-filter-input
   {:placeholder placeholder
    :autoFocus "autoFocus"
    :on-change
    #(>dis-search-input-entered (-> % .-target .-value))}])

(defn find-word [word dict lang]
  "Takes 'word' string and 'words' vector of maps that should contain :word key with string."
  "Returns vector of maps with :word :la :comment keys"
  ;; spec-it
  (spec-it ::db/word word)
  (spec-it ::db/words dict)
  ;;
  (let [key-search-by (if (= lang "English") :word :la)]
      (filter
       #(re-find (re-pattern word) (key-search-by %))
       dict)))

;; Cards

(defn english-card-comp [{:keys [word la comment]}]
  [:ul.koyla-result-ul
   [:li [:strong "English: "] word]
   [:li [:strong "Mela: "] la]
   [:li [:strong "Comment: "] comment]])

(defn mela-card-comp [{:keys [word la comment]}]
  [:ul.koyla-result-ul
   [:li [:strong "Mela: "] la]
   [:li [:strong "Engila: "] word]
   [:li [:strong "Dasayna: "] comment]])

;; Main

(defn koyla-panel [words
                   search-input
                   cur-lang
                   target-lang
                   placeholder
                   >dis-search-input-entered]
  [:div
   [:label.koyla-source-label cur-lang]
   [ search-field cur-lang placeholder >dis-search-input-entered]

   [:div.word-results-row
    [:label.koyla-target-label target-lang]
    [:button.koyla-change-button
     {:type "submit"
      :on-click #(>dis [:change-lang target-lang])}
     (str "Change to " target-lang)]

    ;; word cards
    (let [card-comp (if (= cur-lang "English")
                      english-card-comp
                      mela-card-comp)]

      (for [card (find-word search-input words cur-lang)]
        ^{:key (str (:word card)-(random-uuid))} ;; random-uuid technically impure but it is worth the simplicity
        [card-comp card]))]
   ])

