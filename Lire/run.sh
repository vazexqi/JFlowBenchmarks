#!/bin/bash

gradle jar

java -Xms512M -Xmx512M -cp .:build/libs/Lire.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar:lib/JOpenSurf.jar:lib/commons-math-2.2.jar:lib/lucene-core-3.3.0.jar edu.illinois.jflow.benchmark.LireIndexingExample
java -Xms512M -Xmx512M -cp .:build/libs/Lire.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar:lib/JOpenSurf.jar:lib/commons-math-2.2.jar:lib/lucene-core-3.3.0.jar edu.illinois.jflow.benchmark.LireIndexingExample
java -Xms512M -Xmx512M -cp .:build/libs/Lire.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar:lib/JOpenSurf.jar:lib/commons-math-2.2.jar:lib/lucene-core-3.3.0.jar edu.illinois.jflow.benchmark.LireIndexingExample

