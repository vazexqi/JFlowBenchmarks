#!/bin/bash

gradle jar

java -Xms512M -Xmx512M -cp .:build/libs/RayTracer.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar edu.illinois.jflow.benchmark.RayTracerRun
java -Xms512M -Xmx512M -cp .:build/libs/RayTracer.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar edu.illinois.jflow.benchmark.RayTracerRun
java -Xms512M -Xmx512M -cp .:build/libs/RayTracer.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar edu.illinois.jflow.benchmark.RayTracerRun

