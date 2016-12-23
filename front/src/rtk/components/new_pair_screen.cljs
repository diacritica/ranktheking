(ns rtk.components.new-pair-screen
  (:require [rum.core :as rum]
            [potok.core :as potok]
            [rtk.events :as events]
            [rtk.state :refer [store]]))

(rum/defc new-pair-screen [{:keys [current-pair] :as state}]
  (println (str "> " current-pair))
  (let [{:keys [first second progress]} current-pair
        progress-percent (.round js/Math (* 100 progress))]
    [:.container
     [:h1 (:rtk current-pair)]
     [:h2 (or (str "Criterio: " (:criterion current-pair)) "CHOOSE WISELY")]
     [:.progress
      [:.progress-bar {:role "progressbar" :style {:width (str progress-percent "%")}}
       [:span {:class "sr-only"} (str progress-percent "% complete")]]]
     [:.row
      [:.col-xs-6
       [:a {:class "choice" :href "#"
            :on-click #(potok/emit! store (events/->SendChoice first second))} first]]
      [:.col-xs-6
       [:a {:class "choice" :href "#"
            :on-click #(potok/emit! store (events/->SendChoice second first))} second]]]]))
