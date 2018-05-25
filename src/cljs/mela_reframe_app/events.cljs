(ns mela-reframe-app.events
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db]
            ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

;; search input handler
(defn handle-search-input-entered
  [{:keys [db]} [_ word]]
  {:db (assoc-in db [:search-input] word)})

(re-frame/reg-event-fx
 :search-input-entered
 handle-search-input-entered)
