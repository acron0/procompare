(defproject procompare "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta1"]
                 [enlive "1.1.6"]
                 [aleph "0.4.3"]
                 [byte-streams "0.2.3"]
                 [environ "1.1.0"]
                 [cheshire "5.8.0"]
                 [incanter "1.5.7"]]
  :main ^:skip-aot procompare.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
