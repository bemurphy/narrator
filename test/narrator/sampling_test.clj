(ns narrator.sampling-test
  (:use
    [clojure test]
    [narrator core query]
    [narrator.utils
     sampling])
  (:require
    [criterium.core :as c]))

(def num-sanity-checks 1)

(deftest sanity-check-samples
  (testing "sample"
    (dotimes [_ num-sanity-checks]
      (let [s (compile-operators* sample)
            _ (process-all! s (range 1e6))
            val @s
            avg (double (/ (reduce + val) (count val)))]
        (is (< 4e5 avg 6e5)))))

  (testing "moving-sample"
    (dotimes [_ num-sanity-checks]
      (binding [*now-fn* (constantly 0)]
        (let [s (compile-operators* moving-sample)
              _ (process-all! s (range 1e6))
            val @s
              avg (double (/ (reduce + val) (count val)))]
          (is (< 4e5 avg 6e5)))))))

(deftest ^:benchmark benchmark-samplers
  (println "sample")
  (let [s (compile-operators* sample)]
    (c/quick-bench
      (process-all! s (range 1e6))))
  (println "moving-sample")
  (binding [*now-fn* (constantly 0)]
    (let [s (compile-operators* moving-sample)]
      (c/quick-bench
        (process-all! s (range 1e6))))))
