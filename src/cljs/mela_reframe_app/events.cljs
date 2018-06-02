(ns mela-reframe-app.events
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [cljs.pprint :as pp]
            [re-frame.core :refer [reg-event-db reg-event-fx]]))

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

(def trim-event
  (re-frame.core/->interceptor
   :id      :trim-event
   :before  (fn [context]
              (let [trim-fn (fn [event] (-> event rest vec))]
                (update-in context [:coeffects :event] trim-fn)))))

(reg-event-db
 :process-response
 [trim-event]
 (fn [db [response]]           ;; destructure the response from the event vector
   (assoc db :words (reduce (fn [acc data] (conj acc (:attributes data))) [] (:data (js->clj response))))))

 (reg-event-db
  :bad-response
  [trim-event]
  (fn
    [db [response]]
    (js/console.log response)))

(reg-event-fx
 :request-it
 (fn [{db :db} _]     ;; <-- 1st argument is coeffect, from which we extract db
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :get
                 :api (js/XMLHttpRequest.)
                 :headers {"Accept" "application/vnd.api+json"}
                 :uri             "http://melasi.pythonanywhere.com/koyla/words?letter=a"
                 :response-format (ajax/json-response-format {:keywords? true}) 
                 :on-success      [:process-response]
                 :on-failure      [:bad-response]}}))
