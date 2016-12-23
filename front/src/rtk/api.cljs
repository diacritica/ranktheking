(ns rtk.api
  (:require [httpurr.status :as s]
            [httpurr.client.xhr :as xhr]
            [promesa.core :as p]
            [potok.core :as potok]
            [cuerdas.core :as str]))

(def base-url "http://rtk.icarus.live:8001")

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
   (str base-url (str/format (-> uris key) params))))

(defn parse-json [data]
  (-> data js/JSON.parse (js->clj :keywordize-keys true)))

(defn to-json [data]
  (-> data (clj->js) js/JSON.stringify))

(defn json-response
  [response]
  (condp = (:status response)
    s/ok    (p/resolved (-> response :body parse-json))
    #_:else (p/rejected (str "ERROR: " (:status response) (:body response)))))

(defn fetch-collection-list []
  (-> (get-url :lists)
      (xhr/get)
      (p/then json-response)))

(defn fetch-collection-list-detail [id]
  (p/resolved {:id id :name (str "Collection " id)}))

(defn fetch-solver-list []
  (-> (get-url :solvers)
      (xhr/get)
      (p/then json-response)))

(defn fetch-rtks-detail [id]
  (-> (get-url :rtks-detail {:id id})
      (xhr/get)
      (p/then json-response)))

(defn fetch-rtks-list []
  (-> (get-url :rtks)
      (xhr/get)
      (p/then json-response)))

(defn create-collection [data]
  (-> (get-url :lists)
      (xhr/post {:headers {"Content-Type" "application/json"}
                 :body (to-json data)})
      (p/then json-response)))

(defn create-rtks [data]
  (-> (get-url :rtks)
      (xhr/post {:headers {"Content-Type" "application/json"}
                 :body (to-json data)})
      (p/then json-response)))

(defn ask-for-pair [collection-id rtks-id]
  (-> (get-url :new-pair {:id rtks-id})
      (xhr/get)
      (p/then json-response)))

(defn send-choice [id data]
  (-> (get-url :choice {:id id})
      (xhr/post {:headers {"Content-Type" "application/json"}
                 :body (to-json data)})
      (p/then json-response)))
