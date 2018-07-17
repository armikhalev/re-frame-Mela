(ns mela-reframe-app.panels.latay
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [clojure.spec.alpha :as spec]
            [cljs.pprint :as pp]
            [mela-reframe-app.subs :as subs :refer [>dis]]))

(defn latay-panel []
  [:div "Hoorray! This is the Basic Words (Latay)Page."])
