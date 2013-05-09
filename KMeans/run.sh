#!/bin/bash

gradle jar

# The nthreads=1 parameter will not affect the parallel performance

java -Xms512M -Xmx512M -cp .:build/libs/KMeans.jar:libs/gpars-1.1-jflow.jar:libs/groovy-all-2.1.3.jar:libs/jsr166y.jar edu.illinois.jflow.benchmark.KMeans -m 40 -n 40 -t 0.00001 -i inputs/random-n65536-d32-c16.txt -nthreads 1
java -Xms512M -Xmx512M -cp .:build/libs/KMeans.jar:libs/gpars-1.1-jflow.jar:libs/groovy-all-2.1.3.jar:libs/jsr166y.jar edu.illinois.jflow.benchmark.KMeans -m 40 -n 40 -t 0.00001 -i inputs/random-n65536-d32-c16.txt -nthreads 1
java -Xms512M -Xmx512M -cp .:build/libs/KMeans.jar:libs/gpars-1.1-jflow.jar:libs/groovy-all-2.1.3.jar:libs/jsr166y.jar edu.illinois.jflow.benchmark.KMeans -m 40 -n 40 -t 0.00001 -i inputs/random-n65536-d32-c16.txt -nthreads 1

