(ns mela-reframe-app.css
  (:require [garden.def :refer [defstyles]]))

(defstyles screen
  [:nav {:margin-bottom 23}
   [:ul {:display "flex"
         :justify-content "space-around"
         :list-style "none"}
    [:a {:text-decoration "none"}]]])
