(ns mela-reframe-app.events
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [cljs.spec.alpha :as spec]
            [mela-reframe-app.subs :as subs :refer [<sub]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [cljs.pprint :as pp]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]))


;; now we create an interceptor using `after`
(def check-spec-interceptor (re-frame/after (partial spec-it ::db/db)))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))


;; call database funcs

;; interceptor
(def trim-event
  (re-frame.core/->interceptor
   :id      :trim-event
   :before  (fn [context]
              (let [trim-fn (fn [event] (-> event rest vec))]
                (update-in context [:coeffects :event] trim-fn)))))

;; helper
(defn reduce-response [response]
  "Helper fn used in filter-response & :process-response event-handler"
  (reduce (fn [acc data]
            (conj acc
                  (assoc (:attributes data) :id (:id data)))) ;; get id that comes from api
          []
          response))

;; interceptor
(def filter-response
  "Filter out data that is already present in db"
  (re-frame.core/->interceptor
   :id     :filter-response
   :before (fn [context]
             (let [response (set (reduce-response (:data (first (get-in context [:coeffects :event])))))
                   words (set (get-in context [:coeffects :db :words]))]
               (assoc-in context [:coeffects :event] (into [] (clojure.set/union words response)))
               ))))

;; Handle current language state
(reg-event-db
 :change-lang
 [trim-event]
 (fn [db [cur-lang]]
   (assoc db :cur-lang cur-lang)))

(reg-event-db
 :process-response
 [check-spec-interceptor
  trim-event
  filter-response]
 (fn [db response]           ;; destructure the response from the event vector
   (assoc-in db [:words]
              (js->clj response))))

 (reg-event-db
  :bad-response
  [trim-event]
  (fn
    [db [response]]
    (js/console.log response)))

(reg-event-fx
 :request-words
 (fn [{db :db} [_ lang first-letter]]     ;; <-- 1st argument is coeffect, from which we extract db
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :get
                 :api (js/XMLHttpRequest.)
                 :headers {"Accept" "application/vnd.api+json"}
                 :uri             (str "http://melasi.pythonanywhere.com/koyla/"
                                       lang ;; words - english / las - mela
                                       "?letter="
                                       first-letter)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:process-response]
                 :on-failure      [:bad-response]}}))

(defn handle-search-input-entered
  [{:keys [db]} [_ word]]
  (if (and (= 1 (count word))
           (not
            (some #(= word %)
                  ;; gets :first-letters by :cur-lang - "English" || "Mela"
                  (get (:first-letters db) (:cur-lang db))))) 
    ;; true
    (let [cur-lang (:cur-lang db)
          lang (if (= cur-lang "English")
                 "words"
                 "las")]
      {:db (assoc-in db [:search-input] word)
       :dispatch [:request-words lang word]
       :set-first-letters [cur-lang word]})
    ;; else
    {:db (assoc-in db [:search-input] word)})
  )

(re-frame/reg-event-fx
 :search-input-entered
 handle-search-input-entered)
