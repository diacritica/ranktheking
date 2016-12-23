(ns rtk.events
  (:require [beicon.core :as rx]
            [promesa.core :as p]
            [potok.core :as potok]
            [rtk.api :as api]))

(defrecord Reload [])
(defrecord Navigate [screen])
(defrecord Error [error])
(defrecord CloseError [])

;; Fetch lists
(defrecord SolverListFetched [list])
(defrecord CollectionListFetched [list])
(defrecord RtksListFetched [list])

;; Create elements
(defrecord CreateCollection [name items])
(defrecord CreateRtks [name solver criterion ocluded])

;;
(defrecord StartAskForPair [collection-id rtks-id])
(defrecord AskForPairResponse [data])

;;
(defrecord StartRtksCreation [collection-id])
(defrecord ShowRtksResult [id])
(defrecord SendChoice [winner losser])
(defrecord RtksDetailResult [data])


;; EVENTS
(extend-type Reload
  potok/UpdateEvent
  (update [_ state]
    (println "Reloading...")
    state))

(extend-type SolverListFetched
  potok/UpdateEvent
  (update [{:keys [list]} state]
    (assoc state :solver-list list)))

(extend-type CollectionListFetched
  potok/UpdateEvent
  (update [{:keys [list]} state]
    (assoc state :collection-list list)))

(extend-type RtksListFetched
  potok/UpdateEvent
  (update [{:keys [list]} state]
    (assoc state :rtks-list list)))

(extend-type Navigate
  potok/UpdateEvent
  (update [{:keys [screen]} state]
    (-> state
        (assoc :screen [screen :loading])
        (assoc :error nil)))

  potok/WatchEvent
  (watch [{:keys [screen]} state stream]
    (condp = screen
      :new-collection  (-> (api/fetch-solver-list)
                           (p/then #(->SolverListFetched %)))

      :collection-list (-> (api/fetch-collection-list)
                           (p/then #(->CollectionListFetched %))
                           (p/catch #(->Error %)))

      :rtks-list       (-> (api/fetch-rtks-list)
                           (p/then #(->RtksListFetched %)))
      (rx/empty))
    )
  )

(extend-type CreateCollection
  potok/UpdateEvent
  (update [{:keys [name items]} state]
    (-> state
        (update :collection-list conj {:id 1 :name name})))

  potok/WatchEvent
  (watch [{:keys [name items]} state stream]
    (-> {:name name :elements items}
        api/create-collection
        (p/then #(->Navigate :collection-list)))))

(extend-type AskForPairResponse
  potok/UpdateEvent
  (update [{:keys [data]} state]
    (let [{:keys [pair progress criterion rtk]} data
          [firstoption secondoption] pair]
      (-> state
          (assoc :error nil)
          (assoc :screen [:new-pair])
          (assoc :current-pair {:first firstoption
                                :second secondoption
                                :progress progress
                                :criterion criterion
                                :rtk rtk})))))

(extend-type StartAskForPair
  potok/UpdateEvent
  (update [{:keys [collection-id rtks-id]} state]
    (-> state
        (assoc :selected-rtks rtks-id)))

  potok/WatchEvent
  (watch [{:keys [collection-id rtks-id]} state stream]
    (-> (api/ask-for-pair collection-id rtks-id)
        (p/then (fn [data]
                  (if (>= (:progress data) 1.0)
                    (->ShowRtksResult rtks-id)
                    (->AskForPairResponse data)))))))

(extend-type CreateRtks
  potok/WatchEvent
  (watch [{:keys [name solver criterion ocluded]} state stream]
    (-> {:name name
         :listid (:selected-collection state)
         :criterion criterion
         :solvertype solver
         :partialocclusion ocluded}
        api/create-rtks
        (p/then #(->StartAskForPair
                  (:selected-collection state)
                  %))
        (p/catch #(->Error %)))))

(extend-type StartRtksCreation
  potok/UpdateEvent
  (update [{:keys [collection-id]} state]
    (-> state
        (assoc :screen [:new-rtks])
        (assoc :selected-collection collection-id)))

  potok/WatchEvent
  (watch [{:keys [collection-id]} state stream]
    (-> (api/fetch-solver-list)
        (p/then #(->SolverListFetched %)))))

(extend-type ShowRtksResult
  potok/UpdateEvent
  (update [{:keys [id]} state]
    (-> state
        (assoc :select-rtks id)
        (assoc :screen [:rtks-result])))

  potok/WatchEvent
  (watch [{:keys [id]} state stream]
    (-> (api/fetch-rtks-detail id)
        (p/then (fn [data]
                  (->RtksDetailResult data)))))
  )


(extend-type SendChoice
  potok/WatchEvent
  (watch [{:keys [rtks-id winner losser]} state stream]
    (-> (api/send-choice (:selected-rtks state)
                         {:winner winner
                          :loser losser})
        (p/then (fn [result]
                  (if (>= (:progress result) 1)
                    (->ShowRtksResult rtks-id)
                    (->StartAskForPair (:selected-collection state)
                                       (:selected-rtks state))
                    ))))))


(extend-type Error
  potok/UpdateEvent
  (update [{:keys [error]} state]
    (assoc state :error error))

  potok/WatchEvent
  (watch [_ state stream]
    (rx/timeout 10000 (->CloseError)))
  )

(extend-type CloseError
  potok/UpdateEvent
  (update [_ state]
    (assoc state :error nil))
  )

(extend-type RtksDetailResult
  potok/UpdateEvent
  (update [{:keys [data]} state]
    (assoc state :rtks-detail data)))
