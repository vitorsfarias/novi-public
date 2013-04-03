#!/bin/bash
cd ~/pt/scripts
java -Dmainclass=de.fhg.fokus.net.packetmatcher.Matcher -jar ../jars/packetmatcher-1.0-SNAPSHOT-jar-with-dependencies.jar $*
