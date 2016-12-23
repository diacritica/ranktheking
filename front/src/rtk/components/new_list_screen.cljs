(ns rtk.components.new-list-screen
  (:require [rum.core :as rum]
            [cuerdas.core :as str]
            [rtk.events :as events]
            [potok.core :as potok]
            [rtk.state :refer [store]]))

(rum/defcs new-list-screen [is state]
  (let [solver-list (-> state :solver-list)
        submit-form
        (fn [e]
          (let [name    (str/trim (.-value (rum/ref-node is "name")))
                options (str/trim (.-value (rum/ref-node is "options")))]
            (if (and (not (= name "")) (not (= options "")))
              (potok/emit! store (events/->CreateCollection name (str/split options #"\n")))
              #_(-> (api/create-collection {:name name
                                          :options (str/split options #"\n")})
                  (p/then (fn [result]
                            (potok/emit! store (->Navigate :collection-list))))))))]

    [:div.container
     [:h1 "New collection"]
     [:.form-group
      [:label {:for "title-input"} "Nombre"]
      [:input {:ref "name" :type "text" :class "form-control" :name "title-input"}]]

     #_[:.form-group
      [:label {:for "sorting-type"} "Solver type"]
      [:select {:class "form-control" :name "sorting-type"}
       (for [[idx solver] (map vector (iterate inc 0) solver-list)]
         [:option {:key (str "solver-" idx)} (:name solver)])]]

     [:.form-group
      [:label {:for "options-area"} "Options"]
      [:textarea {:ref "options" :name "options-area" :class "form-control" :rows 10}]]

     [:button {:type "submit" :class "btn btn-primary"
               :on-click submit-form} "Create"]

     ]))
