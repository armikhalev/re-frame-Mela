(ns mela-reframe-app.db
  (:require [re-frame.core :as re-frame]
            [cljs.pprint :as pp]))

(def default-db
  {:name          "re-frame",
   :words         [],
   :first-letters [],
   :search-input  ""})

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
   (swap! re-frame.db/app-db update-in [:first-letters] conj letter)))
