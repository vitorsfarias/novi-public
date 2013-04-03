#!/bin/sh
# NetView helper script
#
CMD=$1

#
# Usage
#
usage () {
cat <<EOT
#
#  NetView maven helper script
#

USAGE:   netview.sh {clean|build|start}

EOT
}
#
# Clean all
#
cleanall() {
 save_dir=`pwd`
 cd $buildhome/pt-api
 if [ -r "./pom.xml" ]; then
    echo "# cleaning pt-api # "
    mvn clean
    echo "# cleaning NetView"
    cd $buildhome/netview
    mvn clean
 else
  curr_dir=`pwd`
  cat<<EOT 
ERROR: Could not find $curr_dir/pom.xml 
       Please checkout pt-api in the indicated location and try again. 

EOT
 fi
 cd $save_dir



}
#
# Build all dependencies using maven
#
buildall () {
 save_dir=`pwd`
 cd $buildhome/netview
 if [ -r "./pom.xml" ]; then
    echo "#####################"
    echo "# Building PT-API   #"
    echo "#####################"
    cd $buildhome/pt-api
    mvn compile install -Dmaven.test.skip=true
    echo ""
    echo "####################"
    echo "# Building NetView #"
    echo "####################"
    cd $buildhome/netview
    mvn compile exec:exec -Dmaven.test.skip=true
 else
  curr_dir=`pwd`
  cat<<EOT 
ERROR: Could not find $curr_dir/pom.xml 
       Please checkout netview in the indicated location and try again. 

EOT
 fi
 cd $save_dir

}
#
# Start NetView
#
start (){
 if [ -r $buildhome/netview/.m2classpath ]; then
  echo "Starting NetView"
  cd $buildhome/netview
   ../scripts/m2run de.fhg.fokus.net.netview.control.MainController 
 else
  cat <<EOT
ERROR: project not built. Assure you have mvn in the path and
       run "netview.sh build"
EOT
 fi
}


#
# MAIN
#
cd `dirname $0`/..;
buildhome=$PWD

case $CMD in
  build)
    buildall
  ;;
  clean)
    cleanall
  ;;
  start)
    start
  ;;
  *)
    usage
esac
