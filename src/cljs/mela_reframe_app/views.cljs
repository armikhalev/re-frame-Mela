(ns mela-reframe-app.views
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.panels.koyla :refer [koyla-panel]]
            [mela-reframe-app.panels.latay :refer [latay-panel]]
            [mela-reframe-app.subs :as subs :refer [<sub >dis]]
            [mela-reframe-app.dispatchers :as disps
             :refer [>dis-search-input-entered
                     >dis-change-lang
                     >dis-grammar-card-info-clicked
                     >dis-show-grammar-card
                     >dis-set-show-menu]]))


;; home
(defn home-panel []
  (let [name "Home"]
    [:div (str "Hello from " name ". This is the Home Page.")]))

;; Textbook
(defn textbook-panel []
  [:div "This is the Textbook Page."])

;; main
;; Keep all impurity here, all other views should be pure functions
(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :latay-panel [latay-panel]
    :koyla-panel [koyla-panel
                  (<sub [::subs/words])
                  (<sub [::subs/search-input])
                  (<sub [::subs/cur-lang])
                  (<sub [::subs/cur-grammar-card-info])
                  (<sub [::subs/target-lang])
                  (<sub [::subs/placeholder])
                  >dis-search-input-entered
                  >dis-change-lang
                  >dis-grammar-card-info-clicked
                  >dis-show-grammar-card
                  (<sub [::subs/grammar-card-show?])]
    :textbook-panel [textbook-panel]
    [:div]))

(defn show-panel [panel-name]
  [:div.app
   [:header
    [:button.navbar-menu-btn
    {:type "submit"
     :on-click #(>dis-set-show-menu true)}]

      [:nav.navbar-nav
       {:class
        (if (<sub [::subs/show-menu?])
          "show-menu")}
       [:ul
        ;; Adds new pages to navbar
        (let [page-links [{:href "#/", :title "Home"}
                          {:href "#/latay", :title "Basic Words"}
                          {:href "#/koyla", :title "Translator"}
                          {:href "#/textbook", :title "Textbook"}]]
          ;;
          (for [pl page-links]
            ^{:key (str (:href pl)"-"(:title pl)"-"panel-name)}
            [:li
             [:a
              {:href (:href pl)
               :on-click #(>dis-set-show-menu false)}
              (:title pl)]]))]]]
   ;;
   [:main.main-container
    {:on-click #(>dis-set-show-menu false)}
    [panels panel-name]]])

(defn main-panel []
    [show-panel (<sub [::subs/active-panel])])
