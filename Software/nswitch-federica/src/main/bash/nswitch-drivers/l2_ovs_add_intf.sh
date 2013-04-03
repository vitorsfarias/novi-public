#!/bin/sh
#v0.6 - Cuted remote IP for the GRE interface due to name length limitation

ETH_VLAN_INTF=eth7
LOG_FILE="/var/log/l2_ovs.log"

SLICE_ID=$1
REMOTE_IP=$2
VLAN_ID=$3


if [ $# -ne 3 ]; then
	echo "USAGE: $0 SLICE_ID(GRE key) REMOTE_GRE_TUNNEL_IP VLAN_ID"
        exit 1
fi

#Check if the ovsdb-server is up & running

if ps ax | grep -v grep | grep ovsdb-server > /dev/null
then
	echo "`/bin/date 2>&1` ovsdb-server running" | tee -a $LOG_FILE
else
	echo "`/bin/date 2>&1` ovsdb-server is not running, trying to run" | tee -a $LOG_FILE
	/usr/local/sbin/ovsdb-server \
	--remote=punix:/usr/local/var/run/openvswitch/db.sock \
	--remote=db:Open_vSwitch,manager_options \
	--private-key=db:SSL,private_key \
	--certificate=db:SSL,certificate \
	--bootstrap-ca-cert=db:SSL,ca_cert \
	--pidfile \
	--detach
    
	sleep 1

	if ps ax | grep -v grep | grep ovsdb-server > /dev/null
        then
		echo "`/bin/date 2>&1` ovsdb-server is now up" | tee -a $LOG_FILE
	else
		echo "`/bin/date 2>&1` ovsdb-server failed to start" | tee -a $LOG_FILE 
		exit 1	
	fi
fi

#Check if the ovs-vswitchd is up & running

if ps ax | grep -v grep | grep ovs-vswitchd > /dev/null
then
	echo "`/bin/date 2>&1` ovs-vswitchd running" | tee -a $LOG_FILE
else
	echo "`/bin/date 2>&1` ovs-vswitchd is not running, trying to run" | tee -a $LOG_FILE
	/usr/local/sbin/ovs-vswitchd --pidfile --detach

	sleep 1

	if ps ax | grep -v grep | grep ovs-vswitchd > /dev/null
	then
		echo "`/bin/date 2>&1` ovs-vswitchd is now running" | tee -a $LOG_FILE
	else
		echo "`/bin/date 2>&1` ovs-vswitchd failed to start" | tee -a $LOG_FILE
		exit 1
	fi
fi

CUT_REMOTE_IP=`echo $REMOTE_IP | awk -F. '{print $3"."$4;}'` 
GRE_INTF=gr$SLICE_ID-$CUT_REMOTE_IP


if /usr/local/bin/ovs-vsctl list-br | grep -E "^br$SLICE_ID\$" > /dev/null
then

#Bridge exists, adding specific port

	/usr/local/bin/ovs-vsctl -- add-port br$SLICE_ID $GRE_INTF -- set interface $GRE_INTF type=gre options=remote_ip=$REMOTE_IP,key=$SLICE_ID,csum=true
else

#Bridge does not exist, adding vlan intf plus gre intf

	/sbin/vconfig add $ETH_VLAN_INTF $VLAN_ID &&
	/sbin/ifconfig "$ETH_VLAN_INTF.$VLAN_ID" up &&
	/usr/local/bin/ovs-vsctl add-br br$SLICE_ID &&
	/usr/local/bin/ovs-vsctl -- add-port "br$SLICE_ID" "$ETH_VLAN_INTF.$SLICE_ID" &&
	/usr/local/bin/ovs-vsctl -- add-port br$SLICE_ID $GRE_INTF -- set interface $GRE_INTF type=gre options=remote_ip=$REMOTE_IP,key=$SLICE_ID,csum=true &&
	/sbin/ifconfig br$SLICE_ID up
fi

if [ $? -eq 0 ]; then
	echo "GRE Interface $GRE_INTF created successfully" | tee -a $LOG_FILE
fi
