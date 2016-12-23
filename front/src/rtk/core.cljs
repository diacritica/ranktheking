(ns rtk.core
  (:require [rum.core :as rum]
            [potok.core :as potok]
            [promesa.core :as p]
            [beicon.core :as rx]
            [cuerdas.core :as str]

            [rtk.api :as api]
            [rtk.events :as events]
            [rtk.state :refer [store event-cb]]

            [rtk.components.home-screen :refer [home-screen]]
            [rtk.components.new-list-screen :refer [new-list-screen]]
            [rtk.components.new-pair-screen :refer [new-pair-screen]]
            [rtk.components.rtks-list-screen :refer [rtks-list-screen]]
            [rtk.components.new-rtks-screen :refer [new-rtks-screen]]
            [rtk.components.rtks-detail-screen :refer [rtks-detail-screen]]
            [rtk.components.rtks-result-screen :refer [rtks-result-screen]]
            [rtk.components.collection-list-screen :refer [collection-list-screen]]
            ))

(enable-console-print!)

(rum/defc main [state]
  (let [[screen] (-> state :screen)]
    [:div
     [:nav {:class "navbar navbar-inverse navbar-fixed-top"}
      [:.container
       [:.navbar-header
        [:a.navbar-brand {:href "#"
                          :on-click (event-cb (events/->Navigate :home))}
         "Rank the King"]]
       [:div#navbar {:class "collapse navbar-collapse"}
        [:ul {:class "nav navbar-nav"}
         [:li {:class ""} [:a {:href "#"
                               :on-click (event-cb (events/->Navigate :collection-list))}
                           "Collections"]]
         [:li {:class ""} [:a {:href "#"
                               :on-click (event-cb (events/->Navigate :rtks-list))}
                           "RTKS's"]]
         [:li {:class ""} [:a {:href "#"
                               :on-click (event-cb (events/->Navigate :new-collection))}
                           "Create collection"]]
         ]]]]
     (when (:error state)
       [:.row
        [:div {:class "alert alert-danger alert-dismissible" :role "alert"}
         [:span {:class "glyphicon glyphicon-exclamation-sign"}]
         [:span {:class "sr-only"} "Error:"]
         (:error state)]])
     (condp = screen
       :home              (home-screen state)

       :new-collection    (new-list-screen state)
       :collection-list   (collection-list-screen state)
       :collection-detail (collection-list-screen state)

       :new-rtks          (new-rtks-screen state)
       :rtks-list         (rtks-list-screen state)
       :rtks-detail       (rtks-detail-screen state)
       :rtks-result       (rtks-result-screen state)

       :new-pair          (new-pair-screen state)

       [:h1 "NOT FOUND"])
     ])
  )

(defonce _ ;; So figwheel won't reload
  (do
    (rx/on-value
     store
     #(rum/mount
       (main %)
       (. js/document (getElementById "app"))))
    ))


(defn on-js-reload []
  (potok/emit! store (events/->Reload)))

