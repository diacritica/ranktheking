(ns rtk.components.home-screen
  (:require [rum.core :as rum]))

(rum/defc home-screen [state]
  [:div.container
   [:div.starter-template
    [:h1 "Rank the King"]
    [:h2 "¿Cuántas veces habéis querido ordenar cosas?"]
    [:p (str "Bacon ipsum dolor amet salami porchetta pig chuck. Andouille "
             "doner sirloin kevin. Tongue strip steak pancetta, pastrami tri-tip "
             "pork loin shoulder leberkas landjaeger frankfurter hamburger meatball. "
             "Shank landjaeger picanha salami, turducken doner sirloin kevin "
             "turkey bacon andouille. Pork ham hock turkey, t-bone ground "
             "round strip steak turducken pork loin porchetta.")]]])
