(ns mela-reframe-app.views
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.panels.koyla :refer [koyla-panel]]
            [mela-reframe-app.subs :as subs :refer [<sub >dis]]
            [mela-reframe-app.dispatchers :as disps
             :refer [>dis-search-input-entered
                     >dis-change-lang
                     >dis-grammar-card-info-clicked
                     >dis-hide-grammar-card
                     >dis-set-show-menu]]))


;; home
(defn home-panel []
  (let [name "Home"]
    [:div (str "Hello from " name ". This is the Home Page.")]))


;; latay

(defn latay-panel []
  [:div "This is the Basic Words (Latay)Page."])

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
                  >dis-hide-grammar-card
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
        ;; TODO: make data structure out of nav items and loop
        [:li [:a {:href "#/"
                  :on-click #(>dis-set-show-menu false)} "Home"]]
        [:li [:a {:href "#/latay"
                  :on-click #(>dis-set-show-menu false)} "Basic Words"]]
        [:li [:a {:href "#/koyla"
                  :on-click #(>dis-set-show-menu false)} "Translator"]]
        [:li [:a {:href "#/textbook"
                  :on-click #(>dis-set-show-menu false)} "Textbook"]]]]]
   [:main.main-container
    {:on-click #(>dis-set-show-menu false)}
    [panels panel-name]]])

(defn main-panel []
    [show-panel (<sub [::subs/active-panel])])
