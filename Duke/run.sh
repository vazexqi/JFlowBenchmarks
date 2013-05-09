#!/bin/bash

gradle jar

# Cold start since Duke does some caching
java -Xms512M -Xmx512M -cp .:build/libs/Duke.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar:lib/commonj-twm.jar:lib/foo-commonj-1.1.0.jar:lib/lucene-analyzers-common-4.0.0.jar:lib/lucene-core-4.0.0.jar:lib/servlet-api-2.4.jar no.priv.garshol.duke.Duke --progress deichmann.xml

time java -Xms512M -Xmx512M -cp .:build/libs/Duke.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar:lib/commonj-twm.jar:lib/foo-commonj-1.1.0.jar:lib/lucene-analyzers-common-4.0.0.jar:lib/lucene-core-4.0.0.jar:lib/servlet-api-2.4.jar no.priv.garshol.duke.Duke --progress deichmann.xml
time java -Xms512M -Xmx512M -cp .:build/libs/Duke.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar:lib/commonj-twm.jar:lib/foo-commonj-1.1.0.jar:lib/lucene-analyzers-common-4.0.0.jar:lib/lucene-core-4.0.0.jar:lib/servlet-api-2.4.jar no.priv.garshol.duke.Duke --progress deichmann.xml
time java -Xms512M -Xmx512M -cp .:build/libs/Duke.jar:lib/gpars-1.1-jflow.jar:lib/groovy-all-2.1.3.jar:lib/jsr166y.jar:lib/commonj-twm.jar:lib/foo-commonj-1.1.0.jar:lib/lucene-analyzers-common-4.0.0.jar:lib/lucene-core-4.0.0.jar:lib/servlet-api-2.4.jar no.priv.garshol.duke.Duke --progress deichmann.xml

