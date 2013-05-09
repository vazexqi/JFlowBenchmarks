#!/bin/bash

gradle jar

java -Xms512M -Xmx512M -cp .:build/libs/MonteCarlo.jar:libs/gpars-1.1-jflow.jar:libs/groovy-all-2.1.3.jar:libs/jsr166y.jar edu.illinois.jflow.benchmark.JGFMonteCarloBench
java -Xms512M -Xmx512M -cp .:build/libs/MonteCarlo.jar:libs/gpars-1.1-jflow.jar:libs/groovy-all-2.1.3.jar:libs/jsr166y.jar edu.illinois.jflow.benchmark.JGFMonteCarloBench
java -Xms512M -Xmx512M -cp .:build/libs/MonteCarlo.jar:libs/gpars-1.1-jflow.jar:libs/groovy-all-2.1.3.jar:libs/jsr166y.jar edu.illinois.jflow.benchmark.JGFMonteCarloBench

