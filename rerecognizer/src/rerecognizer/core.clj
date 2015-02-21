(ns rerecognizer.core
  (:import [java.util.Date])
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clj-time.core :as t]
            [clj-time.format :as f]))

(def column-names [:date
                   :description
                   :original-description
                   :amount
                   :transaction-type
                   :category
                   :account-name
                   :labels
                   :notes])

(def date-formatter (f/formatter "MM/dd/yyyy"))

(defn fuzzy= [a b eps]
  (<= (Math/abs (- a b)) eps))

(defn process-row [row]
  (zipmap column-names row))

(defn read-from-file [filename]
  (with-open [in-file (io/reader filename)]
    (doall
     (map process-row
          (csv/read-csv in-file)))))

(defn get-account-names [records]
  (distinct (map :account-name records)))

(defn get-uniq-transactors [records]
  (distinct (map :description records)))

(defn only-debits [records]
  (filter #(= (:transaction-type %) "debit") records))

(defn pairs [coll]
  (partition 2 1 coll))

(defn compute-intervals [dates]
  (map (fn [[a b]] (t/interval a b)) (pairs dates)))

(defn day-of-month [date]
  (.getDate date))

(defn number-on-same-day [date dates]
  (let [day (day-of-month date)]
    (->> dates
         (filter #(fuzzy= day (day-of-month %) 2) ,)
         count ,)))

(defn is-monthly-periodic? [dates]
  (let [dates (sort dates)]
    (let [intervals (compute-intervals dates)
          intervals-in-days (map #(t/in-days %) intervals)
          only-month-intervals (every? #(fuzzy= % 30 3) intervals-in-days)
          only-small-variation (->> intervals-in-days
                                    (pairs)
                                    (map #(apply - %))
                                    (every? #(fuzzy= % 0 3)))]
      (and only-month-intervals only-small-variation))))

(defn parse-date [date-string]
  (f/parse date-formatter date-string))

(defn -main []
  (let [records (read-from-file "transactions.csv")
        account-names (get-account-names records)]
    (-> (group-by :account-name records)
        (get , "PayPal Account")
        (only-debits ,)
        (get-uniq-transactors ,))))
