(ns mela-reframe-app.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [mela-reframe-app.core-test]))

(doo-tests 'mela-reframe-app.core-test)
