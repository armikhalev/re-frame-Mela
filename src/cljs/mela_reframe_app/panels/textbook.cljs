(ns mela-reframe-app.panels.textbook
  #:ghostwheel.core{:check     true
                    :num-tests 100}
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db]
            [mela-reframe-app.common :as common :refer [flatten-cards
                                                        group-by-category]]
            [cljs.spec.alpha :as s]
            [ghostwheel.core :as g
             :refer [>defn >defn- >fdef => | <- ?]]
            [cljs.pprint :as pp]
            [mela-reframe-app.subs :as subs :refer [>dis]]))

;; View

(defn textbook-panel [alphabet
                      grammar-cards]
  [:div

   ;; Alphabet table

   [:h2.centered-text "Alphabet"]
   [:table.textbook-alphabet
    [:thead
     [:tr
      [:th "Letter"]
      [:th "Name"]
      [:th "Example"]]]
    [:tbody
     (for [{:keys [letter name example]}
           alphabet]
       ^{:key letter}
       [:tr
        [:td letter]
        [:td name]
        [:td example]])]]

    ;; Textbook
   [:div.text-book
    (for [g (sort-by
             :category
             (group-by-category grammar-cards))
          :let [category (:category g)]]

      ^{:key category}
      [:div
       [:h2 {:id category}
        "Category: " category]

       (for [g-card (:grammar-cards g)
             :let [id       (:id g-card)
                   title    (:title g-card)
                   body     (:body g-card)
                   comment  (:comment g-card)]]

         ^{:key id}
         [:div.textbook-card
          [:ul
           [:li [:b title]]
           [:li body]
           [:li comment]]])])]
   ])

;; {{slide-down-navbar titles=grammarCards}}
