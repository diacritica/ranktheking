(defproject test "0.1.0-SNAPSHOT"
  :description "Rank The King (Front)"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha14" :scope "provided"]
                 [org.clojure/clojurescript "1.9.293" :scope "provided"]
                 [rum "0.10.7"]
                 [com.rpl/specter "0.13.1"]
                 [funcool/potok "1.1.0"]
                 [funcool/lentes "1.2.0"]
                 [funcool/beicon "2.8.0"]
                 [funcool/cuerdas "2.0.1"]
                 [funcool/httpurr "0.6.2"]
                 [funcool/promesa "1.7.0"]]

  :plugins [[lein-figwheel "0.5.8"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "rtk.core/on-js-reload"
                           :open-urls ["http://localhost:3333"]}
                :compiler {:main rtk.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/rtk.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/rtk.js"
                           :main rtk.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {:server-port 3333 ;; default
             :css-dirs ["resources/public/css"]}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.8.2"]
                                  [figwheel-sidecar "0.5.8"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src"]
                   :repl-options {:init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

)
