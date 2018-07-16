(ns mela-reframe-app.db
  (:require [re-frame.core :as re-frame]
            [clojure.spec.alpha :as spec]
            [mela-reframe-app.subs :as subs]
            [cljs.pprint :as pp]))

;; Specs

(spec/def ::word string?)
(spec/def ::la string?)
(spec/def ::comment string?)
(spec/def ::cur-lang string?)

(spec/def ::title string?)
(spec/def ::body string?)
(spec/def ::comment string?)
(spec/def ::cur-grammar-card-info (spec/keys :req-un [::title ::body ::comment] ))

(spec/def ::card (spec/keys :req-un [::word ::la ::comment]))
(spec/def ::words (spec/coll-of ::card))
(spec/def ::db (spec/keys :req-un [::words ]))

(defn spec-it
  "Throws an exception if `value` doesn't match the Spec `a-spec`."
  [a-spec value]
  (when-not (spec/valid? a-spec value)
    (js/console.log (str "spec check failed: " (spec/explain-str a-spec value)))))

;; DB

(def default-db
  {:words                   [],

   :show-menu?              false,

   :grammar-cards           [],

   :first-letters           {"Mela" [],
                             "English" ["a"]}, ;; since it is called on the first render

   :cur-lang                "English"

   :cur-grammar-card-info   {:title ""
                             :body ""
                             :comment ""
                             :category ""}

   :grammar-card-show?      false

   :search-input            ""})

;; Spec for below reg-fx :set-first-letters
(spec/def ::arg1-set-first-letters
  (spec/or
   :English #(= "English" %)
   :Mela #(= "Mela" %)))

;; coeffects and effects

(re-frame/reg-fx
 :set-first-letters
 (fn [[lang letter]] ;; destructure
   ;; spec-it
   (spec-it ::arg1-set-first-letters lang)
   ;;
   "Ensures that db is not overloaded by checking that words count is less than 1000"
   "Watches count of first-letters to prevent unnesassary calls to server"
   "Empties words and first-letters if overloaded"
   (if (< (count @(re-frame/subscribe [::subs/words])) 1000 )
     ;;true
     (swap! re-frame.db/app-db update-in [:first-letters lang] conj letter)
     ;;false
     (do
       (swap! re-frame.db/app-db update-in [:first-letters] empty)
       (swap! re-frame.db/app-db update-in [:words] empty)))))
