(ns rtk.api
  (:require [httpurr.status :as s]
            [httpurr.client.xhr :as xhr]
            [promesa.core :as p]
            [potok.core :as potok]
            [cuerdas.core :as str]))

(def base-url "http://rtk.icarus.live:8001")

(defrecord FetchError [error]
  potok/UpdateEvent
  (update [_ state]
    (println ">>> ERROR " error)
    state))

(def uris
  {:lists       "/lists"
   :list-detail "/lists/%(id)s"
   :solvers     "/solvers"
   :rtks        "/rtks"
   :rtks-detail "/rtks/%(id)s"
   :new-pair    "/rtks/%(id)s/newpair"
   :choice      "/rtks/%(id)s/choice"})

(defn get-url
  ([key]
   (get-url key {}))

  ([key params]
   (let [result (str base-url (str/format (-> uris key) params))]
     (println ">> GEnerated URI" result params)
     result)))

(defn parse-json [data]
  (-> data js/JSON.parse (js->clj :keywordize-keys true)))

(defn to-json [data]
  (let [json (-> data (clj->js) js/JSON.stringify)]
    (println ">>> tojson: " json)
    json))

(defn json-response
  [response]
  (println "AMIGOOO " response)
  (condp = (:status response)
    s/ok (p/resolved (-> response :body parse-json))
    (do
      (println "rejecting:" (:status response) "'" (str/trim (:body response)) "'")
      (p/rejected (str "ERROR: " (:status response) (:body response))))))

(defn fetch-collection-list []
  (-> (get-url :lists)
      (xhr/get)
      (p/then json-response)
      #_(p/catch (fn [e]
                 (println "CATCH " e)
                 (->FetchError e))))
  #_(p/resolved [{:id "1" :name "Collection one"}
               {:id "2" :name "Collection two"}])
  )

(defn fetch-collection-list-detail [id]
  (p/resolved {:id id :name (str "Collection " id)}))

(defn fetch-solver-list []
  (-> (get-url :solvers)
      (xhr/get)
      (p/then json-response)
      (p/catch #(->FetchError %)))
  #_(p/resolved [{:name "DEFAULT"}
               {:name "Solver two"}
               {:name "Solver three"}]))

(defn fetch-rtks-detail [id]
  (-> (get-url :rtks-detail {:id id})
      (xhr/get)
      (p/then json-response))
  #_(p/resolved [{:id id
                :name (str "rtk-" id)
                :listid "1"
                :solvertype "1"
                :progress 0.5
                :elements []
                :orderedlist []
                :partialocclusion false}]))


(defn fetch-rtks-list []
  (-> (get-url :rtks)
      (xhr/get)
      (p/then json-response)
      (p/catch #(->FetchError %)))
  #_(p/resolved (for [i (range 0 10)] {:id i
                                     :name (str "rtk-" i)
                                     :progress (if (= (rand-int 2) 0) 0.5 1.0)} )))


(defn create-collection [data]
  (println (str ">> CREATED" (to-json data)))
  #_(p/resolved data)
  (-> (get-url :lists)
      (xhr/post {:headers {"Content-Type" "application/json"}
                 :body (to-json data)})
      (p/then json-response)
      (p/catch #(->FetchError %))))

(defn create-rtks [data]
  (println (str ">> CREATED" (to-json data)))
  #_(p/resolved (assoc data :rtksId "1"))
  (-> (get-url :rtks)
      (xhr/post {:headers {"Content-Type" "application/json"}
                 :body (to-json data)})
      (p/then json-response)
      (p/catch #(->FetchError %)))
  )

(defn ask-for-pair [collection-id rtks-id]
  (-> (get-url :new-pair {:id rtks-id})
      (xhr/get)
      (p/then json-response)
      (p/catch #(->FetchError %)))
  #_(p/resolved {:id "1"
               :firstOption (str "First Option" (rand-int 1000))
               :secondOption (str "Second Option" (rand-int 1000))
               :progress (rand 0.2)}))

(defn send-choice [id data]
  (println (str ">> CREATED" (to-json data)))
  #_(p/resolved {:progress (if (= (rand-int 3) 0) 1 (rand))})
  (-> (get-url :choice {:id id})
      (xhr/post {:headers {"Content-Type" "application/json"}
                 :body (to-json data)})
      (p/then json-response)
      (p/catch #(->FetchError %)))
  )
