(ns rita.prediction-generator.probability
  "RITA Probability helper")

(defn make-probability-vector [prob & [maxcounts]]
  {:pre [(not (nil? prob)) (<= prob 1.0)]}
  (let [maxcounts (or maxcounts 1000)
        count (long (* maxcounts prob))
        v  (reduce (fn [res _]
                     (conj res true)) [] (range count))
        v (reduce (fn [res _]
                    (conj res false)) v (range (- maxcounts count)))]
    ;(println v)
    v))

(defn make-probability-vector-from
  "assumes input is {1 0.1 2 0.2} and for multiplier 1000
  it will return a vector of 100 `1s` and 200 `2s`.
   So the probability of randomly choosing a 1 will be 0.33
   and 2 will be 0.67"
  [sym-and-probs & [multiplier]]
  (let [multiplier (or multiplier 1000)]
    (reduce (fn [res [sym prob]]
              (into res (reduce (fn [res2 _]
                                  (conj res2 sym))
                                [] (range (long (* prob multiplier))))))
            [] sym-and-probs)))

(defn count-elements [v]
  (let [counts (group-by (fn [input]
                           input) v)]
    (reduce (fn [res [sym elems]]
              (conj res {sym (count elems)})) (sorted-map) counts)))

(defn yay-or-nay [v]
  (rand-nth v))

(defn check-randomness [n]
  (let [probs (into [] (map (fn [_]
                              (make-probability-vector 0.7)) (range n)))

        p-indices (range (count probs))

        actuals (reduce (fn [res _]
                          (let [idx (rand-nth p-indices)
                                va (yay-or-nay (get probs idx))]
                            (if (contains? res idx)
                              (conj res {idx (conj (get res idx) va)})
                              (conj res {idx [va]})))
                          ) {} (range (* n 1000)))]
    (println "probs" probs)
    (println "actuals" actuals)
    (doseq [[idx val] actuals]
      (println "x" val)
      (println (count-elements val)))))