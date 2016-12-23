(ns rtk.components.rtks-result-screen
  (:require [rum.core :as rum]))

(rum/defc rtks-result-screen [state]
  (let [result-list (map first (-> state :rtks-detail :orderedlist))]
    [:.container
     [:h1 "Result"]
     (if (empty? result-list)
       [:.container "EMPTY"]
       [:.container
        [:ol.list-group
         (for [[idx collection] (map vector (iterate inc 0) result-list)]
           [:li.list-group-item {:class "container" :key (str "result-" idx)}
            [:span (str (inc idx) ". " collection)]
            ])]])]))
