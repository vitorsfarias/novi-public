#!/bin/sh
# matcher helper script
#
CMD=$1
cd `dirname $0`/..; prjhome=$PWD/packetmatcher
M2CLASSPATH=$prjhome/.m2classpath 

#
# Usage
#
usage () {
cat <<EOT
#
#  Matcher
#

USAGE: matcher.sh start [OPTIONS] 
       matcher.sh clean  

Use "matcher.sh start -h" for help.

EOT
}
#
# Start Packet Matcher
#
start (){
 if  [ ! -r $M2CLASSPATH ]; then
    echo "=== Extracting classpath (.m2classpath) ===" 
    cd $prjhome
    mvn compile exec:exec -Dmaven.test.skip=true
 fi
 cd $prjhome
# ../scripts/m2run de.fhg.fokus.net.packetmatcher.Matcher
  ../scripts/m2run -Dmainclass=de.fhg.fokus.net.packetmatcher.Matcher org.kohsuke.args4j.Starter $*
}


#
# MAIN
#


case $CMD in
  clean)
  echo "Removing $M2CLASSPATH" 
  rm -f $M2CLASSPATH
  ;;
  start)
    shift
    start $*
  ;;
  *)
    usage
esac
