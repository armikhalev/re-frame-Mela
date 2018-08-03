(ns mela-reframe-app.common
  (:require [re-frame.core :as re-frame]
            [reagent.dom :as reagent-dom]
            [reagent.core :as reagent]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [clojure.spec.alpha :as s]
            [cljs.pprint :as pp]))

;; Specs

(s/def ::placeholder string?)
;; TODO: figure out how spec works with functions passed as argument
;; (s/fdef >dis-search-input-entered
;;   :args (s/cat :value int?))

;; Components

(defn search-field
  "Pure function: on-change calls passed in function with one value to dispatch"
  [placeholder
   >dis-search-input-entered
   search-input]
  ;; spec-it
  (spec-it ::placeholder placeholder)
  ;; (spec-it ::dis-search-input-entered >dis-search-input-entered)
  ;;
  (let []
    (reagent/create-class
     {:display-name "search-field"

      ;; Using 3d reagent form to make cursor go to the end of the input value
      :component-did-mount (fn [self] (let [inputLen (-> self
                                                         reagent-dom/dom-node
                                                         .-value
                                                         count)]
                                        ;; setSelectionRange is js function that is a bit hacky here
                                        (-> self
                                            reagent-dom/dom-node
                                            (.setSelectionRange inputLen (* inputLen 2)))))

      :reagent-render
      (fn
        [placeholder
         >dis-search-input-entered
         search-input]
        [:input.word-filter-input
         {:placeholder placeholder
          :auto-focus true
          :value search-input
          :on-change #(>dis-search-input-entered (-> % .-target .-value))}])})))

(defn text-book-comp
  [{:keys [title body comment] :as args}
   >dis-show-grammar-card
   <sub-grammar-card-show?]
  ;; spec-it
  (spec-it ::db/cur-grammar-card-info args)
  ;;
  [:div.text-book-component-container
   {:class (if <sub-grammar-card-show?
             "text-book-component-show"
             "text-book-component-hide")}

   [:img.text-book-component-hide-btn
    {:on-click #(>dis-show-grammar-card false)
     :src "images/cancel_button.png"
     :alt "hide textbook component card button"}]
   [:div.text-book-component-info
    [:header
     [:strong "Title: "]
     title]
    body
    [:div
     [:strong "Comment: "]
     comment]]])
