(ns pipeline.core
  (import [java.util.concurrent
           ThreadPoolExecutor
           ArrayBlockingQueue
           TimeUnit
           ThreadPoolExecutor$CallerRunsPolicy
           Callable
           Executors
           ExecutorService
           Future]))

(defn queue [capacity]
  (ArrayBlockingQueue. capacity))

(defn thread-pool [queue pool-size]
  (ThreadPoolExecutor. pool-size pool-size
                       60 TimeUnit/SECONDS
                       queue
                       (ThreadPoolExecutor$CallerRunsPolicy.)))

(defn unit [in-capacity run-fn pool-size next-unit]
  {:run-fn run-fn
   :t-pool (thread-pool (queue in-capacity) pool-size)
   :next-unit next-unit})

(defn make-callable [run-fn input]
  (cast Callable
        (fn []
          (let [i (if (instance? Future input)
                    (.get input)
                    input)]
            (run-fn i)))))

(defn run-one [{:keys [run-fn t-pool next-unit] :as unit} input]
  (let [^Callable task (make-callable run-fn input)
        result (.submit ^ExecutorService t-pool
                        ^Callable task)]
    (if (some? next-unit)
      (do-one next-unit result)
      result)))

(defn run [unit inputs]
  (for [result (map (partial do-one unit) inputs)]
    (.get result)))

(defn create [& [unit-spec & more]]
  (if (empty? more)
    (apply unit (conj (vec unit-spec) nil))
    (apply unit (conj (vec unit-spec) (apply create more)))))

(defn stop [{:keys [t-pool next-unit]}]
  (.shutdownNow t-pool)
  (when next-unit (stop-unit next-unit)))
