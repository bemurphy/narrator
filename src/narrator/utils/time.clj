(ns narrator.utils.time
  (:require
    [clojure.string :as str])
  (:import
    [java.util
     Calendar
     TimeZone]))

(defn nanoseconds
  "Converts nanoseconds -> milliseconds"
  [n]
  (/ n 1e6))

(defn microseconds
  "Converts microseconds -> milliseconds"
  [n]
  (/ n 1e3))

(defn milliseconds
  "Converts milliseconds -> milliseconds"
  [n]
  n)

(defn seconds
  "Converts seconds -> milliseconds"
  [n]
  (* n 1e3))

(defn minutes
  "Converts minutes -> milliseconds"
  [n]
  (* n 6e4))

(defn hours
  "Converts hours -> milliseconds"
  [n]
  (* n 36e5))

(defn days
  "Converts days -> milliseconds"
  [n]
  (* n 864e5))

(defn hz
  "Converts frequency -> period in milliseconds"
  [n]
  (/ 1e3 n))

(let [intervals (partition 2
                  ["d" (days 1)
                   "h" (hours 1)
                   "m" (minutes 1)
                   "s" (seconds 1)])]

  (defn format-duration
    "Returns a formatted string describing an interval, i.e. '5d 3h 1m'"
    [n]
    (loop [s "", n n, intervals intervals]
      (if (empty? intervals)
        (if (empty? s)
          "0s"
          (str/trim s))
        (let [[desc val] (first intervals)]
          (if (>= n val)
            (recur
              (str s (int (/ n val)) desc " ")
              (rem n val)
              (rest intervals))
            (recur s n (rest intervals))))))))

(let [sorted-units [:millisecond Calendar/MILLISECOND
                    :second Calendar/SECOND
                    :minute Calendar/MINUTE
                    :hour Calendar/HOUR
                    :day Calendar/DAY_OF_YEAR
                    :week Calendar/WEEK_OF_MONTH
                    :month Calendar/MONTH]
      unit->calendar-unit (apply hash-map sorted-units)
      units (->> sorted-units (partition 2) (map first))
      unit->cleared-fields (zipmap
                             units
                             (map
                               #(->> (take % units) (map unit->calendar-unit))
                               (range (count units))))]
  
  (defn floor
    "Given a timestamp and a unit, rounds down to the nearest even multiple of that unit."
    [timestamp unit]
    (assert (contains? unit->calendar-unit unit))
    (let [^Calendar cal (doto (Calendar/getInstance (TimeZone/getTimeZone "UTC"))
                          (.setTimeInMillis timestamp))]
      (doseq [field (unit->cleared-fields unit)]
        (.set cal field 0))
      (.getTimeInMillis cal)))

  (defn add
    "Adds `n`-many `units` to the timestamp."
    [timestamp n unit]
    (assert (contains? unit->calendar-unit unit))
    (let [^Calendar cal (doto (Calendar/getInstance (TimeZone/getTimeZone "UTC"))
                          (.setTimeInMillis timestamp))]
      (.add cal (unit->calendar-unit unit) n)
      (.getTimeInMillis cal))))
