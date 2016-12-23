(ns rtk.state
  (:require [potok.core :as potok]))

(defn- on-error
  [error]
  (js/console.error error))

(defonce initial-state
  {:screen [:home]
   :solver-list []
   :collection-list []
   :rtks-list []})

(defonce store (potok/store {:state initial-state
                             :on-error on-error}))

(defn send [event]
  (potok/emit! store event))

(defn event-cb [event]
  (fn [e] (potok/emit! store event)))
