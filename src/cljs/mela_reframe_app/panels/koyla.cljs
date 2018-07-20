(ns mela-reframe-app.panels.koyla
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [mela-reframe-app.common :as common :refer [search-field]]
            [clojure.spec.alpha :as spec]
            [cljs.pprint :as pp]
            [mela-reframe-app.subs :as subs :refer [>dis]]))

;; view

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

(defn english-card-comp
  [{:keys [word la comment grammar-card]}
   >dis-grammar-card-info-clicked]
  ;;
  [:ul.koyla-result-ul
   [:li [:strong "English: "] word]
   [:li [:strong "Mela: "] la]
   [:li [:strong "Comment: "] comment]
   (if (some? grammar-card)
     [:div.koyla-info-icon
      {:on-click #(>dis-grammar-card-info-clicked (:id grammar-card))}
      [:img.info-icon
       {:src "images/info_icon.png"
        :alt "info icon"}]])])

(defn mela-card-comp
  [{:keys [word la comment grammar-card]}
   >dis-grammar-card-info-clicked]
  ;;
  [:ul.koyla-result-ul
   [:li [:strong "Mela: "] la]
   [:li [:strong "Engila: "] word]
   [:li [:strong "Dasayna: "] comment]

  (if (some? grammar-card)
    [:div.koyla-info-icon
     {:on-click #(>dis-grammar-card-info-clicked (:id grammar-card))}
     [:img.info-icon
      {:src "images/info_icon.png"
       :alt "info icon"}]])])

(defn text-book-comp
  [{:keys [title body comment] :as args}
   >dis-show-grammar-card
   <sub-grammar-card-show?]
  ;; spec-it
  (spec-it ::db/cur-grammar-card-info args)
  ;;
  [:div.text-book-component-container
   {:class (if <sub-grammar-card-show?
             "text-book-component-show"
             "text-book-component-hide")}

   [:img.text-book-component-hide-btn
    {:on-click #(>dis-show-grammar-card false)
     :src "images/cancel_button.png"
     :alt "hide textbook component card button"}]
   [:div.text-book-component-info
    [:header
     [:strong "Title: "]
     title]
    body
    [:div
     [:strong "Comment: "]
     comment]]])

;; Main

(defn koyla-panel [words
                   search-input
                   cur-lang
                   cur-grammar-card-info
                   target-lang
                   placeholder
                   >dis-search-input-entered
                   >dis-change-lang
                   >dis-grammar-card-info-clicked
                   >dis-show-grammar-card
                   <sub-grammar-card-show?]
  [:div
   [:label.koyla-source-label cur-lang]
   [search-field placeholder >dis-search-input-entered]

   [:div.word-results-row
    [:label.koyla-target-label (if (= cur-lang "English")
                                 target-lang
                                 "Engila")]
    [:button.koyla-change-button
     {:type "submit"
      :on-click #(>dis-change-lang target-lang)}
     (if (= cur-lang "English")
       "Change to Mela"
       "Ali tu Engila")]

    ;; word cards
    (let [card-comp (if (= cur-lang "English")
                      english-card-comp
                      mela-card-comp)]

      (for [card (find-word search-input words cur-lang)]
        ^{:key (str (:word card)"-"(:id card)"-"cur-lang)}

        [card-comp

         card
         >dis-grammar-card-info-clicked
         ]))]

   [text-book-comp

    cur-grammar-card-info
    >dis-show-grammar-card
    <sub-grammar-card-show?]])

