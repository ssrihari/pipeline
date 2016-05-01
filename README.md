# pipeline

Create assembly lines with ease.

## Usage

One unit in the pipeline requires the size of the input queue, a function to run, and a thread-pool-size. This is represented as a vector:

``` Clojure
[input-queue-size function-to-run thread-pool-size]
```

You can then `create`, `run` and `stop` pipelines, which chains multiple such units together.

``` Clojure
(require '[pipeline.core :as p])

(let [pipeline (p/create
                [10 #(- % 3) 10]
                [10 #(* 2 %) 10])]
  (p/run pipeline [1 2 3]) ;; (-4 -2 0)
  (p/stop pipeline))
```

## License

Copyright © 2016 Srihari Sriraman.
Released under the MIT license.
