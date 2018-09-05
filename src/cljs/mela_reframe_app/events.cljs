(ns mela-reframe-app.events
  #:ghostwheel.core{:check     true
                    :num-tests 20}
  (:require [re-frame.core :as re-frame]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [mela-reframe-app.common :as common :refer [sanitize-input]]
            [mela-reframe-app.subs :as subs :refer [<sub >dis]]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [ghostwheel.core :as g
             :refer [>defn >defn- >fdef => | <- ?]]
            [cljs.pprint :as pp]
            [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]))

;; we create an interceptor using `after`
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

;; STARTS with specs for handle-koyla-url-contains-searched-word

(s/def :koyla-url/search-input string?)

(s/def :koyla-url/input (s/tuple
                         keyword?
                         #{"mela" "english"}
                         :koyla-url/search-input))

(s/def :event-handler/event vector?)
(s/def :event-handler/db (s/keys :req-un [:event-handler/event ::db/db]))

(s/def :koyla-url/cur-lang #{"Mela" "English"})
(s/def :koyla-url/request-words keyword?)
(s/def :koyla-url/first-letter (s/and string? #(= 1 (count %))))

(s/def :koyla-url/dispatch (s/tuple
                            :koyla-url/request-words
                            #{"las" "words"}
                            :koyla-url/first-letter))
(s/def :koyla-url/set-cur-lang :koyla-url/cur-lang)
(s/def :koyla-url/set-first-letters (s/tuple
                                     :koyla-url/cur-lang
                                     :koyla-url/first-letter))
(s/def :koyla-url/return
  (s/keys :req-un
          [::db/db
           :koyla-url/dispatch
           :koyla-url/set-cur-lang
           :koyla-url/set-first-letters]))

(>defn handle-koyla-url-contains-searched-word
  "Event handler.
  Requires as args `db`,
  `lang` string, which either `mela` or `english`,
  `letter` string of any length.
  Requests api for Mela word if `lang` is `mela`, otherwise requests  English words.
  Sets first-letter of the `letter` to `first-letters` in `db` to the relevant language."
  ;; {::g/trace 4}
  [{db :db} [_ lang word]]     ;; <-- 1st argument is coeffect, from which we extract db
  ;; spec
  [:event-handler/db :koyla-url/input | #(> (count word) 0)
   => :koyla-url/return]
  ;;
  (let [first-letter (first word)
        cur-lang     (clojure.string/capitalize lang)
        lang-req     (if (= lang "mela")
                       "las"
                       "words")]

    {:db (assoc-in db [:search-input] word)
     :dispatch [:request-words lang-req first-letter]
     :set-cur-lang cur-lang
     :set-first-letters [cur-lang first-letter]}))

(reg-event-fx
 :koyla-url-contains-searched-word
 handle-koyla-url-contains-searched-word)

;; ENDs koyla-url-contains-searched-word


(reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(>defn handle-set-show-menu
  [db [set-show-menu]]
  ;; spec
  [::db/db vector? | #(boolean? set-show-menu)
   => ::db/db]
  ;;
  (assoc db :show-menu? set-show-menu))

(reg-event-db
 :set-show-menu
 [trim-event]
 handle-set-show-menu)


;;--------------------;;
;; Call database funcs
;;--------------------;;


(>defn handle-change-lang
  [db [cur-lang]]
  [::db/db vector? | #(string? cur-lang)
   => ::db/db]
  (assoc db :cur-lang cur-lang))

;; Handle current language state
(reg-event-db
 :change-lang
 [trim-event]
 handle-change-lang)


;; WORD CARDS HANDLERS


;; --> Starts: process-request-words-response

(s/def :context/card (s/keys :req-un [::db/word
                                      ::db/la
                                      ::db/comment]))
(s/def :context/attributes :context/card)

(s/def :context/relationships (s/keys :req-un [::db/grammar-card]))

(s/def :context/word-card (s/keys :req-un [::db/id
                                           :context/attributes
                                           :context/relationships]))
(s/def :context/data (s/coll-of :context/word-card))

(s/def :context/effect (s/keys :req-un [:context/data]))
(s/def :context/event (s/coll-of :context/effect))
(s/def :context/coeffects (s/keys :req-un [:context/event
                                           ::db/db]))
(s/def :context/context (s/keys :req-un [:context/coeffects]))

(s/def :response/handled-word-card (s/keys :req-un [::db/word
                                                    ::db/la
                                                    ::db/comment
                                                    ::db/grammar-card
                                                    ::db/id]))
(s/def :response/handled-word-cards (s/coll-of :response/handled-word-card))

(s/def :response/event (s/coll-of :response/handled-word-card))
(s/def :response/coeffects (s/keys :req-un [:response/event]))
(s/def :response/response (s/keys :req-un [:response/coeffects]))

(>defn reduce-words-response
  "Helper fn used in :filter-words-response interceptor.
   Adds `:id` and `:grammar-card` attributes to `:words`."
  ;; {::g/trace 4}
  [response]
  [:context/data
   => :response/handled-word-cards]
  (reduce (fn [acc data]
            (as-> data d
              (:id d)
              (assoc (:attributes data) :id d)
              (assoc d :grammar-card
                     (get-in data [:relationships :grammar-card :data],
                             {})) ;; otherwise return empty map
              (conj acc d)))
          []
          response))

(>defn before-filter-words-response-interceptor
  "Fn used in `:before` of `filter-words-response` interceptor"
  [context]
  [:context/context
   => :response/response]
  (let [response (set (reduce-words-response (-> context
                                                 (get-in , [:coeffects :event])
                                                 first
                                                 (:data))))
        words (set (get-in context [:coeffects :db :words]))]
    (assoc-in context [:coeffects :event] (into [] (clojure.set/union words response)))))

;; interceptor
(def filter-words-response
  "Filter out data that is already present in db"
  (re-frame.core/->interceptor
   :id     :filter-words-response
   :before before-filter-words-response-interceptor))

;; Process response from 'request-words' event-fx
(reg-event-db
 :process-request-words-response
 ;; interceptors
 [check-spec-interceptor
  trim-event
  filter-words-response]
 ;; don't need to destructure response, since it is done in filter-words-response interceptor
 (fn [db response]
   (assoc-in db [:words]
             (js->clj response))))

;; <-- Ends: process-request-words-response


(reg-event-db
 :bad-response
 [trim-event]
 (fn [db [response]]
   ;; TODO: create view that shows message that data was not loaded? Try reload page?
   (do
     (js/console.log "Badly handled: -> " response)
     db)))

;; Api response event handler
(reg-event-fx
 :request-words
 (fn [_ [_ lang first-letter]]
   ;; we return a map of (side) effects
   (let [sanitized-letter (sanitize-input first-letter)]
     {:http-xhrio {:method          :get
                   :api (js/XMLHttpRequest.)
                   :headers {"Accept" "application/vnd.api+json"}
                   :uri             (str "http://melasi.pythonanywhere.com/koyla/"
                                         lang ;; words - english / las - mela
                                         "?letter="
                                         sanitized-letter)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success      [:process-request-words-response]
                   :on-failure      [:bad-response]}})))


;; Starts handle-search-input-entered

(s/def :search-input/update-url-with-current-koyla-search-input string?)
(s/def :search-input-entered/return (s/keys  :req-un [::db/db
                                                      :search-input/update-url-with-current-koyla-search-input]))

;; TODO: should be tested with a unit test for `letter` = 1 and the opposite
(>defn handle-search-input-entered
  ;; {::g/trace 4}
  [{db :db} [_ letter]]
  [:event-handler/db vector? | #(string? letter)
   => :search-input-entered/return]
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
       :update-url-with-current-koyla-search-input letter
       :set-first-letters [cur-lang letter]})
    ;; else
    {:db (assoc-in db [:search-input] letter)
     :update-url-with-current-koyla-search-input letter}))

(reg-event-fx
 :search-input-entered
 handle-search-input-entered)

;; ENDs handle-search-input-entered


;; GRAMMAR CARDS HANDLERS


(reg-event-db
 :show-grammar-card
 (fn [db [_ show?]]
   (assoc db :grammar-card-show? show?)))


;; STARTS process-request-grammar-cards-response

;; Spec GrammarCard

(s/def :grammar-cards/title string?)
(s/def :grammar-cards/body string?)
(s/def :grammar-cards/comment string?)

(s/def :grammar-cards/category (s/and
                                string?
                                ;; Checks if category starts with an integer which app relies on in Textbook panel
                                #(int? (js/parseInt %))))

(s/def :grammar-cards/attributes (s/keys :req-un [:grammar-cards/title
                                                  :grammar-cards/body
                                                  :grammar-cards/comment
                                                  :grammar-cards/category]))
(s/def :grammar-cards/id string?)
(s/def :grammar-cards/type #{"GrammarCard"})

(s/def :grammar-cards/grammar-card (s/keys :req-un [:grammar-cards/attributes
                                                    :grammar-cards/id
                                                    :grammar-cards/type]))

;; Spec input args of `process-request-grammar-cards-response-handler`

;; db
(s/def :grammar-cards-input/grammar-cards vector?)
(s/def :grammar-cards-input/db (s/keys :req-un [::db/words
                                                ::db/basic-words
                                                ::db/show-menu?
                                                :grammar-cards-input/grammar-cards]))

;; response
(s/def :grammar-cards-input/data (s/coll-of :grammar-cards/grammar-card))
(s/def :grammar-cards-input/effect (s/keys :req-un [:grammar-cards-input/data]))
(s/def :grammar-cards-input/response (s/coll-of :grammar-cards-input/effect))


;; Spec return value of `process-request-grammar-cards-response-handler`

(s/def :grammar-cards-return/grammar-cards (s/coll-of :grammar-cards/grammar-card))
(s/def :grammar-cards-return/db (s/keys :req-un [::db/words
                                                 ::db/basic-words
                                                 ::db/show-menu?
                                                 :grammar-cards-return/grammar-cards]))


(>defn process-request-grammar-cards-response-handler
       ;; {::g/trace 4}
       [db response]
       ;; Spec
       [:grammar-cards-input/db :grammar-cards-input/response
        => :grammar-cards-return/db]
       ;;
       (assoc-in db [:grammar-cards] (into [] (-> response
                                                  first
                                                  :data))))

(reg-event-db
 :process-request-grammar-cards-response
 ;; interceptors
 [check-spec-interceptor
  trim-event]
 ;;
 process-request-grammar-cards-response-handler)

;; ENDs process-request-grammar-cards-response


(reg-event-fx
 :request-grammar-cards
 (fn [_ _]
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
             ;; get event data, it should contain :basic-words
             (let [response (-> context
                                (get-in , [:coeffects :event])
                                first
                                :data)]

               ;; add more convenient way of getting grammar-card's id associated with a basic-word
               (assoc-in context
                         [:coeffects :db :basic-words]
                         ;; :basic-words is vector but reduce returns list, 'into' turns it into vector
                         (into []
                               ;; iterate over :basic-words extracting id and putting it back with :grammar-card key
                               (reduce
                                (fn [acc data]
                                  (as-> data d
                                    (get-in d [:relationships :grammar-card :data])
                                    (if (contains? d :id) (:id d) false)
                                    (assoc data :grammar-card d)
                                    (conj acc d)))
                                [] response)))))))

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
 (fn [_ _]
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
   (assoc-in db
             [:basic-words]
             (map
              (fn [card*]
                (if (= id (get-in card* [:id]))
                  (update-in card* [:attributes :flip] not)
                  card*))
              (get-in db [:basic-words])))))

(reg-event-db
 :flip-all-basic-words->front
 ;; interceptors
 [check-spec-interceptor
  trim-event]
 ;;
 (fn [db]
   (assoc-in db
             [:basic-words]
             (map
              #(update-in % [:attributes] assoc :flip false)
              (get-in db [:basic-words])))))

(reg-event-db
 :flip--all-basic-words->opposite-side
 ;; interceptors
 [check-spec-interceptor
  trim-event]
 ;;
 (fn [db]
   (assoc-in db
             [:basic-words]
             (map
              #(update-in % [:attributes :flip] not)
              (get-in db [:basic-words])))))

;; (g/check)
