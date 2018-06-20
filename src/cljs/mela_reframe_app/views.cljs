(ns mela-reframe-app.views
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.panels.koyla :refer [koyla-panel]]
            [mela-reframe-app.subs :as subs :refer [<sub]]
            ))


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
                  (<sub [::subs/target-lang])]
    :textbook-panel [textbook-panel]
    [:div]))

(defn show-panel [panel-name]
  [:div.app
   [:header
    [:nav.navbar-nav
     [:ul
      [:li [:a {:href "#/"} "Home"]]
      [:li [:a {:href "#/latay"} "Basic Words"]]
      [:li [:a {:href "#/koyla"} "Koyla"]]
      [:li [:a {:href "#/textbook"} "Textbook"]]]]]
   [:main.main-container
    [panels panel-name]]])

(defn main-panel []
    [show-panel (<sub [::subs/active-panel])])
