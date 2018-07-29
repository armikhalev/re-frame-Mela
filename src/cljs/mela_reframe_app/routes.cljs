(ns mela-reframe-app.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.history.Html5History)
  (:require [secretary.core :as secretary]
            [accountant.core :as accountant]
            [goog.events :as gevents]
            [goog.history.EventType :as EventType]
            [re-frame.core :as re-frame]
            [mela-reframe-app.events :as events]
            ))

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (gevents/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  ;; --------------------
  ;; define routes here
  (defroute "/" []
    (re-frame/dispatch [::events/set-active-panel :home-panel]))

  (defroute "/latay" []
    (re-frame/dispatch [::events/set-active-panel :latay-panel]))

  (defroute "/koyla" []
    (re-frame/dispatch [::events/set-active-panel :koyla-panel]))

  (defroute "/textbook" []
    (re-frame/dispatch [::events/set-active-panel :textbook-panel]))
  ;; --------------------
  (hook-browser-navigation!)
  ;;
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (secretary/dispatch! path))
    :path-exists?
    (fn [path]
      (secretary/locate-route path))})
  (accountant/dispatch-current!))
