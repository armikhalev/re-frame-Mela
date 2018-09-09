(ns mela-reframe-app.panels.textbook)

(def alphabet [{:letter "Aa", :name "a", :example "Spa"}
               {:letter "Dd", :name "da", :example "Do"}])

;; View

(defn textbook-panel [grammar-cards]
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
    (for [g     (sort-by #(:category (:attributes %) ) grammar-cards)
          :let [g-card   (:attributes g)
                category (:category g-card)
                title    (:title g-card)
                body     (:body g-card)
                comment  (:comment g-card)]]

      ^{:key (:id g)}
      [:div
       [:h2 {:id category}
        "Category: " category]
       [:div.textbook-card
        [:ul
         [:li [:b title]]
         [:li body]
         [:li comment]]]])]
   ])

;; {{slide-down-navbar titles=grammarCards}}
