(ns rerecognizer.core-test
  (:use clojure.test
        rerecognizer.core)
  (:require [clj-time.core :as t]))

(def spotify-dates ["11/03/2014" "10/03/2014" "9/03/2014" "8/03/2014" "7/03/2014"])

(deftest yearly-is-not-monthly-periodic-test
  (testing "Yearly periodic date ranges shouldn't be monthly"
    (is (not (is-monthly-periodic? [(t/date-time 1985) (t/date-time 1986)])))))

(deftest monthly-periodic-data-is-monthly-periodic-test
  (testing "Spotify billing data should be monthly"
    (is (is-monthly-periodic? (map parse-date spotify-dates)))))
