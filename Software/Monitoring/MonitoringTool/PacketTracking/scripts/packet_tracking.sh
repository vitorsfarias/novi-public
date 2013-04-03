#!/bin/bash

# PacketTracking in NOVI
# Copyright (C) 2011 Fokus Fraunhofer <Julian.Vetter@fokus.fraunhofer.de>
#

stop_probe()
{
    for NODE in $NODES; do
        echo "Stopping probe on ${NODE} (${!NODE})"
	ssh ${SSH_PARAMS} novi_novi@${!NODE} "sudo ${IMPD_STOP}"
    done
    exit 0
}

start_probe()
{
    for NODE in $NODES; do
        echo "Starting probe on ${NODE} (${!NODE})"
        PARAMS=${NODE}_PARAMS
        PARAMS=${!PARAMS}
        #echo "ssh ${SSH_PARAMS} novi_novi@${!NODE} 'sudo ${IMPD_START} $PARAMS'"
        ssh ${SSH_PARAMS} novi_novi@${!NODE} "sudo ${IMPD_START} $PARAMS"
    done
}

transfer()
{
    for NODE in $NODES; do
        echo "Removing old dir from ${NODE} (${!NODE})"
        ssh ${SSH_PARAMS} novi_novi@${!NODE} "rm -rf ${WDIR}"
	ssh ${SSH_PARAMS} novi_novi@${!NODE} "mkdir -p /home/novi_novi/pt/packet"
        echo "Transfering data to ${NODE} (${!NODE})"

        #Copying all necessary files to the 'packet' directory
        scp -r ${SSH_PARAMS} /home/novi_novi/pt/impd4e novi_novi@${!NODE}:${WDIR} 1> /dev/null
        scp -r ${SSH_PARAMS} /home/novi_novi/pt/libipfix novi_novi@${!NODE}:${WDIR} 1> /dev/null
	scp -r ${SSH_PARAMS} /home/novi_novi/pt/scripts/start_probe.sh novi_novi@${!NODE}:${WDIR} 1> /dev/null
    done
}

helper()
{
    echo "usage: openepc-pt CONFIGURATION (start|stop|transfer|transfer_keys)"
    echo "  transfer        Transfers the whole 'pt' directory to the target nodes"
    echo "  start           Starts all probes on the devices"
    echo "  stop            Stops all probes"
    echo "  help            Show this help"
    
}

[ \! -z "$1" ] && [ -f "$1" ] && . "$1" || (echo "couldn't read configuration file"; exit 1)

case $2 in
"transfer")
    transfer;;
"start")
    start_probe;;
"stop")
    stop_probe;;
"help")
    helper;;
*)
    helper;;
esac
