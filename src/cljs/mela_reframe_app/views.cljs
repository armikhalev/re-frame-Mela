(ns mela-reframe-app.views
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.panels.koyla :refer [koyla-panel]]
            [mela-reframe-app.subs :as subs]
            ))


;; home
(defn home-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div (str "Hello from " @name ". This is the Home Page.")]))


;; latay

(defn latay-panel []
  [:div "This is the Basic Words (Latay)Page."])

;; Textbook
(defn textbook-panel []
  [:div "This is the Textbook Page."])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :latay-panel [latay-panel]
    :koyla-panel [koyla-panel]
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
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
