(ns mela-reframe-app.events
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [cljs.spec.alpha :as spec]
            [mela-reframe-app.subs :as subs :refer [<sub >dis]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [cljs.pprint :as pp]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]))


;; now we create an interceptor using `after`
(def check-spec-interceptor (re-frame/after (partial spec-it ::db/db)))

;; interceptor
(def trim-event
  (re-frame.core/->interceptor
   :id      :trim-event
   :before  (fn [context]
              (let [trim-fn (fn [event] (-> event rest vec))]
                (update-in context [:coeffects :event] trim-fn)))))

(reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(reg-event-db
 :set-show-menu
 [trim-event]
 (fn [db [set-show-menu]]
   ;; spec
   (spec/valid? boolean? set-show-menu)
   ;;
   (assoc db :show-menu? set-show-menu)))

;;--------------------;;
;; Call database funcs
;;--------------------;;

;; Handle current language state
(reg-event-db
 :change-lang
 [trim-event]
 (fn [db [cur-lang]]
   (assoc db :cur-lang cur-lang)))

;; WORDS CARDS HANDLERS

;; helper
(defn reduce-words-response [response]
  "Helper fn used in filter-response & :process-response event-handler"
  (reduce (fn [acc data]
            (as-> data d
              (:id d)
              (assoc (:attributes data) :id d)
              (get-in data [:relationships :grammar-card :data])
              (assoc (:attributes data) :grammar-card d)
              (conj acc d)))
          []
          response))

;; interceptor
(def filter-words-response
  "Filter out data that is already present in db"
  (re-frame.core/->interceptor
   :id     :filter-words-response
   :before (fn [context]
             (let [response (set (reduce-words-response (-> context
                                                            (get-in , [:coeffects :event])
                                                            first
                                                            (:data))))
                   words (set (get-in context [:coeffects :db :words]))]
               (assoc-in context [:coeffects :event] (into [] (clojure.set/union words response)))))))

;; Process response from 'request-words' event-fx
(reg-event-db
 :process-request-words-response
 ;; interceptors
 [check-spec-interceptor
  trim-event
  filter-words-response]
 ;; don't need to destructure response, since it is done in filter-response interceptor
 (fn [db response]
   (assoc-in db [:words]
              (js->clj response))))

 (reg-event-db
  :bad-response
  [trim-event]
  (fn
    [db [response]]
    (js/console.log response)))

;; Api response event handler
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
                 :on-success      [:process-request-words-response]
                 :on-failure      [:bad-response]}}))

(defn handle-search-input-entered
  [{db :db} [_ letter]]
  (if (and (= 1 (count letter))
           (not
            (some #(= letter %)
                  ;; gets :first-letters by :cur-lang - "English" || "Mela"
                  ;; if letter is not in :first-letters then it is added
                  (get (:first-letters db) (:cur-lang db)))))
    ;; true
    (let [cur-lang (:cur-lang db)
          lang (if (= cur-lang "English")
                 "words"
                 "las")]
      {:db (assoc-in db [:search-input] letter)
       :dispatch [:request-words lang letter]
       :set-first-letters [cur-lang letter]})
    ;; else
    {:db (assoc-in db [:search-input] letter)}))

(reg-event-fx
 :search-input-entered
 handle-search-input-entered)

;; GRAMMAR CARDS HANDLERS

(reg-event-db
 :show-grammar-card
 (fn [db [_ show?]]
   (assoc db :grammar-card-show? show?)))

;; interceptor
(def add-grammar-cards-to-db
  (re-frame.core/->interceptor
   :id     :add-grammar-cards-to-db
   :before (fn [context]
             (let [response (-> context
                                (get-in , [:coeffects :event])
                                first
                                :data)]
               (assoc-in context [:coeffects :db :grammar-cards] (into [] response))))))

(reg-event-db
 :process-request-grammar-cards-response
 ;; interceptors
 [check-spec-interceptor
  trim-event
  add-grammar-cards-to-db]
 ;;
 (fn [db response] db))

(reg-event-fx
 :request-grammar-cards
 (fn [{:keys [db]} _]
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :get
                 :api (js/XMLHttpRequest.)
                 :headers {"Accept" "application/vnd.api+json"}
                 :uri             "http://melasi.pythonanywhere.com/koyla/grammar-cards"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:process-request-grammar-cards-response]
                 :on-failure      [:bad-response]}}))

;; interceptor
(def show-grammar-card
  (re-frame.core/->interceptor
   :id     :show-grammar-card
   :after (fn [context]
            (assoc-in context [:effects :db :grammar-card-show?] true))))

(reg-event-db
 :grammar-card-info-clicked
 ;; interceptors
 [show-grammar-card]
 ;;
 (fn [db [_ id]]
   (assoc-in db [:cur-grammar-card-info]
             (get-in (->> (:grammar-cards db)
                          (filter #(= id (:id %)))
                          first)
                     [:attributes]))))

;; LATAY

;; interceptor
(def add-basic-words-to-db
  (re-frame.core/->interceptor
   :id     :add-basic-words-to-db
   :before (fn [context]
             (let [response (-> context
                                (get-in , [:coeffects :event])
                                first
                                :data)]
               (assoc-in context [:coeffects :db :basic-words] (into [] response))))))

(reg-event-db
 :process-request-basic-words-response
 ;; interceptors
 [check-spec-interceptor
  trim-event
  add-basic-words-to-db]
 ;;
 (fn [db response] db))

(reg-event-fx
 :request-basic-words
 (fn [{:keys [db]} _]
   ;; we return a map of (side) effects
   {:http-xhrio {:method          :get
                 :api (js/XMLHttpRequest.)
                 :headers {"Accept" "application/vnd.api+json"}
                 :uri             "http://melasi.pythonanywhere.com/koyla/cards"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [:process-request-basic-words-response]
                 :on-failure      [:bad-response]}}))

(reg-event-db
 :basic-words-search-input-entered
 ;; interceptors
 [check-spec-interceptor
  trim-event]
 ;;
 (fn [db [letter]]
   (assoc-in db [:basic-words-search-input] letter)))

(reg-event-db
 :flip-card
 ;; interceptors
 [check-spec-interceptor
  trim-event]
 ;;
 (fn [db [flip? id]]
   (prn flip? id (:basic-words db))
   db
   #_(assoc-in db [:basic-words] )))
