(ns rtk.components.rtks-list-screen
  (:require [rum.core :as rum]
            [potok.core :as potok]
            [rtk.events :as events]
            [rtk.state :refer [store]]))

(rum/defc rtks-list-screen [state]
  (let [rtks-list (-> state :rtks-list)]
    [:.container
     [:h1 "RTKS List"]
     [:.container
      [:ul.list-group
       (for [[idx rtks] (map vector (iterate inc 0) rtks-list)]
         (let [progress-percent (.round js/Math (* 100 (:orderprogress rtks)))]
           [:li.list-group-item {:key (str "rtks-" idx)}
            (if (= progress-percent 100)
              [:a.badge {:style {:background-color "#2c822c"}
                         :on-click (fn [e]
                                     (potok/emit! store (events/->ShowRtksResult (:id rtks))))}
               "finished"]
              [:a.badge {:on-click (fn [e]
                                     (potok/emit! store (events/->StartAskForPair nil (:id rtks)))
                                     )}
               (str "continue ("progress-percent "%)")])
            [:b (:name rtks)]
            #_[:span
             [:a {:href "#"
                  :on-click (fn [e]
                              (potok/emit! store (events/->ShowRtksResult (:id rtks))))}
              (str (:id rtks))]
             [:span (str " - " (:name rtks))]]]))]]]))
