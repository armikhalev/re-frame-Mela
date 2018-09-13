(ns mela-reframe-app.common
  #:ghostwheel.core{:check     true
                    :num-tests 100}
  (:require [re-frame.core :as re-frame]
            [reagent.dom :as reagent-dom]
            [reagent.core :as reagent]
            [mela-reframe-app.db :as db :refer [spec-it]]
            [cljs.spec.alpha :as s]
            [ghostwheel.core :as g
             :refer [>defn >defn- >fdef => | <- ?]]
            [cljs.pprint :as pp]))


;; Helpers

(defn flatten-cards
  "Merges `id` and `attributes` of a map (in vector of maps) into one map"
  [grammar-cards]
  (reduce (fn [acc card]
            (conj acc
                  (merge
                   {:id (:id card)}
                   (:attributes card))))
          [] grammar-cards))


(defn group-by-category
  "Takes vector of maps where maps have `:category` key.
  Fn groups maps by category
  creating new maps with two keys `:category` and `:grammar-cards`,
  where having category and flattened data as values of corresponding keys.
  NOTE: Depends on `flatten-cards` fn."
  [grammar-cards]
  (let [flat (flatten-cards grammar-cards)]
    (for [[k v]
          (group-by :category flat)]
      {:category      k,
       :grammar-cards v})))


(>defn sanitize-input
  "Ensures that `input` string is alphanumeric, apostroph, dash or space.
  Otherwise returns empty string."
  [input]
  ;; spec
  [string?
   => string?]
  ;;
  (let [sanitized (re-find #"[a-zA-Z0-9'-]*\s*[a-zA-Z0-9'-]*" input)]
    (if (some? sanitized)
      sanitized
      "")))


;; Components


(>defn search-field
  "Pure function: `on-change` calls passed in function with one value to dispatch.
  `search-input` string, current value that will be replaced by `on-change` event."
  ;; {::g/trace 4}
  [placeholder
   >dis-search-input-entered
   search-input]
  ;; spec
  [string?
   (s/fspec :args (s/cat :value string?))
   string?
   => fn?]
  ;;
  (let []
    (reagent/create-class
     {:display-name "search-field"

      ;; Using 3d reagent form to make cursor go to the end of the input value
      :component-did-mount (fn [self]
                             (let [inputLen (-> self
                                                reagent-dom/dom-node
                                                .-value
                                                count)]
                               ;; setSelectionRange is js function that is a bit hacky here
                               (-> self
                                   reagent-dom/dom-node
                                   (.setSelectionRange inputLen (* inputLen 2)))))

      :reagent-render
      (fn [placeholder >dis-search-input-entered search-input]
        [:input.word-filter-input
         {:placeholder placeholder
          :auto-focus true
          :value search-input
          :on-change #(>dis-search-input-entered (sanitize-input (-> % .-target .-value)))}])})))


(>defn text-book-comp
  [{:keys [title body comment] :as args}
   >dis-show-grammar-card
   <sub-grammar-card-show?]
  ;; spec-it
  [::db/cur-grammar-card-info
   (s/fspec :args (s/cat :show? boolean?))
   boolean?
   => vector?]
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


;; (g/check)
