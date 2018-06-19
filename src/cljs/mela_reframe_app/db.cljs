(ns mela-reframe-app.db
  (:require [re-frame.core :as re-frame]
            [clojure.spec.alpha :as spec]
            [mela-reframe-app.subs :as subs]
            [cljs.pprint :as pp]))

;; DB

(def default-db
  {:name          "re-frame",
   :words         [],
   :first-letters [],
   :search-input  ""})

;; coeffects and effects

;; (re-frame/reg-cofx
;;  :set-first-letters
;;  (fn [cofx _]
;;    (let [[_ letter] (:event cofx),
;;          first-letters (get-in cofx [:db :first-letters])]
;;      ;; put first letter in :first-letters vector to make logic to decrease calls to database
;;      ;; if not in :first-letters then add it, otherwise just pass through
;;      (if (and (= 1 (count letter))
;;               (not (some #(= letter %) first-letters)))
;;        ;; true
;;        (update-in cofx [:db :first-letters] conj letter)
;;        ;; else
;;        cofx))))

(re-frame/reg-fx
 :set-first-letters
 (fn [letter]
   "Ensures that db is not overloaded by checking that words count is less than 1000"
   "Watches count of first-letters to prevent unnesassary calls to server"
   "Empties words and first-letters if overloaded"
   (if (< (count @(re-frame/subscribe [::subs/words])) 1000 )
     ;;true
     (swap! re-frame.db/app-db update-in [:first-letters] conj letter)
     ;;false
     (do
       (swap! re-frame.db/app-db update-in [:first-letters] empty)
       (swap! re-frame.db/app-db update-in [:words] empty)
))))
;; Specs

(spec/def ::word string?)
(spec/def ::la string?)
(spec/def ::comment string?)

(spec/def ::card (spec/keys :req-un [::word ::la ::comment]))
(spec/def ::words (spec/coll-of ::card))
(spec/def ::db (spec/keys :req-un [::words]))

(defn spec-it
  "Throws an exception if `value` doesn't match the Spec `a-spec`."
  [a-spec value]
  (when-not (spec/valid? a-spec value)
    (js/console.log (str "spec check failed: " (spec/explain-str a-spec value)))))
