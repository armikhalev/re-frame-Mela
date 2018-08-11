(defproject mela-reframe-app "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [org.clojure/test.check "0.9.0"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.5"]
                 [secretary "1.2.3"]
                 [venantius/accountant "0.2.4"]
                 [ns-tracker "0.3.1"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [day8.re-frame/test "0.1.5"]
                 [cljs-ajax "0.7.3"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler mela-reframe-app.dev-server/handler}

  :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}

  :aliases {"dev" ["do" "clean"
                   ["pdo" ["figwheel" "dev"]]]
            "build" ["with-profile" "+prod,-dev" "do"
                     ["clean"]
                     ["cljsbuild" "once" "min"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.10"]
                   [figwheel-sidecar "0.5.16"]
                   [cider/piggieback "0.3.1"]
                   [day8.re-frame/re-frame-10x "0.3.3"]]

    :plugins      [[lein-figwheel "0.5.16"]
                   [lein-doo "0.1.8"]
                   [lein-pdo "0.1.1"]]}
   :prod {}}

  :doo {:build "test"}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "mela-reframe-app.core/mount-root"}
     :compiler     {:main                 mela-reframe-app.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "/js/compiled/out"
                    :source-map-timestamp true
                    :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true, goog.DEBUG true}
                    :preloads             [day8.re-frame-10x.preload, devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}

    {:id           "min"
     :source-paths ["src/cljs"]
     :compiler     {:main            mela-reframe-app.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :none
                    :closure-defines {goog.DEBUG true}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src/cljs" "test/cljs"]
     :compiler     {:main          mela-reframe-app.runner
                    :output-to     "resources/public/js/compiled/test.js"
                    :output-dir    "resources/public/js/compiled/test/out"
                    :optimizations :none
                    }}

    ]})
