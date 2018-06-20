(ns mela-reframe-app.subs
  (:require [re-frame.core :as re-frame :refer [reg-sub subscribe dispatch]]))

(def <sub (comp deref subscribe))
(def >dis dispatch)

(reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(reg-sub
 ::search-input
 (fn [db _]
   (:search-input db)))

(reg-sub
 ::words
 (fn [db _]
   (:words db)))

(reg-sub
 ::first-letters
 (fn [db _]
   (:first-letters db)))

(reg-sub
 ::cur-lang
 (fn [db _]
   (:cur-lang db)))

(reg-sub
 ::target-lang
 :<- [::cur-lang]
 (fn [lang _]
   (if (= lang "English")
     "Mela"
     "English")))
