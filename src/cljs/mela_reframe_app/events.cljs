(ns mela-reframe-app.events
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db]
            [mela-reframe-app.subs :as subs]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [cljs.pprint :as pp]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

;; call database funcs

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
   (assoc db :words
          (reduce
           (fn [acc data]
             (conj acc
                   (:attributes data)))
           []
           (:data (js->clj response))))))

 (reg-event-db
  :bad-response
  [trim-event]
  (fn
    [db [response]]
    (js/console.log response)))

(reg-event-fx
 :request-words
 (fn [{db :db} [_ language first-letter]]     ;; <-- 1st argument is coeffect, from which we extract db
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :get
                 :api (js/XMLHttpRequest.)
                 :headers {"Accept" "application/vnd.api+json"}
                 :uri             (str "http://melasi.pythonanywhere.com/koyla/"
                                       language ;; words - english / las - mela
                                       "?letter="
                                       first-letter)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:process-response]
                 :on-failure      [:bad-response]}}))

;; (reg-event-fx
;;  :request-words-starting-with
;;  (fn [{db :db} [_ lang word]]
;;    (when (#(not= word %) (:first-letters db))
;;      (get-words lang word)
;;      (prn "request-words-starting-with handler" lang word) )))

;; search input handler funcs
#_(if (= 1 (count word))
  (when
      (#(not= word %) (:first-letters db))
    (do
      (prn (:first-letters db))
      (let [fl-db (update-in db [:first-letters] conj word)]
        (prn fl-db)
        {:db (assoc-in fl-db [:search-input] word)}))))

(defn handle-search-input-entered
  [{:keys [db]} [_ word]]
  {:db (assoc-in db [:search-input] word)
   :dispatch [:request-words "words" word]}
  #_(if (and (= 1 (count word))
           (not (some #(= word %) (:first-letters db))))
    ;; true
    {:db (assoc-in db [:search-input] word)
     :dispatch [:request-words "words" word]}
    ;; else
    {:db (assoc-in db [:search-input] word)})
  )

(re-frame/reg-event-fx
 :search-input-entered
 [(inject-cofx :set-first-letters)]
 handle-search-input-entered)
