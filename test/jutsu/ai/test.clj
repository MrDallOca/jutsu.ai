(ns jutsu.ai.test
  (:require [clojure.test :refer :all]
            [jutsu.ai.core :as ai]))

(def topology1
  [{:in 1 :out 50 :activation :relu :loss nil}
   {:in 50 :out 50 :activation :relu :loss nil}
   {:in 50 :out 1 :activation :softmax :loss :mse}])

(def topology2
  [[1 50 :relu]
   [50 50 :relu]
   [50 1 :softmax :mse]])

(deftest shorthand
  (is (= topology1 (ai/parse-shorthand topology2))))

(def layer-config-test [{:in 4 :out 4 :activation :relu}
                        {:in 4 :out 4 :activation :relu}
                        {:in 4 :out 10 :activation :softmax :loss :negative-log-likelihood}])

(def test-net (ai/network layer-config-test
               (ai/default-classification-options)))

(deftest init-classification-net
  (is (= org.deeplearning4j.nn.multilayer.MultiLayerNetwork (class test-net))))

(defn train-classification-net []
  (let [dataset-iterator (ai/classification-csv-iterator "classification_data.csv" 4 4 10)]
    (-> test-net
        (ai/initialize-net!)
        (ai/train-net! 10 dataset-iterator)
        (ai/save-model "classnet.zip"))))

(train-classification-net)

(def autoencoder-config 
  [{:in 2000 :out 1000 :loss :kl-divergence}
   {:in 1000 :out 500 :loss :kl-divergence}
   {:in 500 :out 250 :loss :kl-divergence}
   {:in 250 :out 100 :loss :kl-divergence}
   {:in 100 :out 30 :loss :kl-divergence}
   {:in 30 :out 100 :loss :kl-divergence}
   {:in 100 :out 250 :loss :kl-divergence}
   {:in 250 :out 500 :loss :kl-divergence}
   {:in 500 :out 1000 :loss :kl-divergence}
   {:in 1000 :out 2000 :activation :sigmoid :loss :mse}])

(def test-encoder (ai/network autoencoder-config
                    (merge (ai/default-classification-options)
                      {:optimization-algo :line-gradient-descent
                       :layer-builder :rbm
                       :output-loss-function :mse})))

(deftest init-autoencoder
  (is (= org.deeplearning4j.nn.multilayer.MultiLayerNetwork (class test-encoder))))


