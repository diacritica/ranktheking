(ns rtk.components.new-rtks-screen
  (:require [rum.core :as rum]
            [potok.core :as potok]
            [cuerdas.core :as str]
            [rtk.events :as events]
            [rtk.state :refer [store]]))

(rum/defcs new-rtks-screen [is state]
  (let [solver-list (-> state :solver-list)
        submit-form
        (fn [e]
          (let [name      (str/trim (.-value (rum/ref-node is "name")))
                solver    (str/trim (.-value (rum/ref-node is "solver")))
                criterion (str/trim (.-value (rum/ref-node is "criterion")))
                ocluded   (.-checked (rum/ref-node is "ocluded"))]
            (if (and (not (= name "")) (not (= solver "")))
              (potok/emit! store (events/->CreateRtks name solver criterion ocluded))
              #_(-> (api/create-collection {:name name
                                          :options (str/split options #"\n")})
                  (p/then (fn [result]
                            (potok/emit! store (->Navigate :collection-list))))))))]

    [:div.container
     [:h1 "New RTKS"]
     [:.form-group
      [:label {:for "title-input"} "Nombre"]
      [:input {:ref "name" :type "text" :class "form-control" :name "title-input"}]]

     [:.form-group
      [:label {:for "sorting-type"} "Solver type"]
      [:select {:ref "solver" :class "form-control" :name "sorting-type"}
       (for [[idx solver] (map vector (iterate inc 0) solver-list)]
         [:option {:key (str "solver-" idx)} (:name solver)])]]

     [:.form-group
      [:label {:for "criterion-input"} "Criterio"]
      [:input {:ref "criterion" :type "text" :class "form-control" :name "criterion-input"}]]

     [:.checkbox
      [:label {:for "ocluded-input"}
       [:input {:ref "ocluded" :type "checkbox" :name "ocluded-input"}]
       "Ocluded?"]]

     [:button {:type "submit" :class "btn btn-primary"
               :on-click submit-form} "Create"]

     ]))
