(ns mela-reframe-app.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::search-input
 (fn [db _]
   (:search-input db)))

(re-frame/reg-sub
 ::words
 (fn [db _]
   (:words db)))

(re-frame/reg-sub
 ::first-letters
 (fn [db _]
   (:first-letters db)))
