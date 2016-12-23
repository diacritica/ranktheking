(ns rtk.components.collection-list-screen
  (:require [rum.core :as rum]
            [rtk.events :as events]
            [rtk.state :refer [event-cb]]))

(rum/defc collection-list-screen [state]
  (let [collection-list (-> state :collection-list)]
    [:.container
     [:h1 "Collection list"]
     [:.container
      [:ul.list-group
       (for [[idx collection] (map vector (iterate inc 0) collection-list)]
         [:li.list-group-item {:class "container" :key (str "collection-" idx)}
          [:b.pull-left (str (:name collection))]
          [:a.btn {:class "btn-primary pull-right"
                   :on-click (event-cb (events/->StartRtksCreation (:id collection)))} "New RTKS"]
          [:a.btn {:class "btn-secondary pull-right"} "Detail"]
          ])]]]))
