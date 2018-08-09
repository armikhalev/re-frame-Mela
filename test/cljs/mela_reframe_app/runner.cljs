(ns mela-reframe-app.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [mela-reframe-app.core-test]))

(enable-console-print!)

(doo-tests 'mela-reframe-app.core-test
           'mela-reframe-app.koyla-test)
