#!/bin/sh
#Ethernet logical interface over GRE creation using vsys scripts
#v0.5

SLICE_ID=$1
SLICE_NAME=$2
REMOTE_IP=$3
PRIVATE_IP=$4
NETMASK=$5


IF_FILE="/root/interfaces.txt"
PYTHON="/usr/bin/python"
VIF_UP="/vsys/vif_up"
IF_TYPE=tap
VNET_FILE="/etc/planetlab/vsys-attributes/$SLICE_NAME/vsys_vnet"
LOG_FILE="/var/log/L2_tunnels.log"


if [ $# -ne 5 ]; then
	echo "USAGE: $0 SLICE_ID SLICE_NAME REMOTE_IP PRIVATE_IP NETMASK" 	
	exit 1
fi

POINTOPOINT=`$PYTHON /root/cidr_netmask.py $PRIVATE_IP $NETMASK`

LOCAL_SID=`/bin/grep -E "^$SLICE_NAME:" /etc/passwd | /bin/awk -F: '{print $3;}'`

if [ -z $LOCAL_SID ]; then
	echo -n `/bin/date` 2>&1 | tee -a $LOG_FILE
	echo " ERROR: Slice name $SLICE_NAME does not exist!" 2>&1 | tee -a $LOG_FILE
	exit 1
fi

if [ ! -r $VNET_FILE ]; then
	echo -n `/bin/date` 2>&1 | tee -a $LOG_FILE
	echo " ERROR: File $VNET_FILE does not exist!" 2>&1 | tee -a $LOG_FILE
	exit 1
fi

IF_NUM=`/bin/grep -E "^$SLICE_NAME " "$IF_FILE" | /bin/awk '{print $2}'`


# First tunnel interface for this slice
if [ -z $IF_NUM ]; then
	IF_NUM=1
else 
	OLD_NUM=$IF_NUM
	let IF_NUM=IF_NUM+1

fi

echo -n `/bin/date` 2>&1 | tee -a $LOG_FILE
echo " Creating GRE interface $IF_TYPE$LOCAL_SID-$IF_NUM $PRIVATE_IP/$NETMASK gre=$SLICE_ID remote=$REMOTE_IP pointopoint=$POINTOPOINT" 2>&1 | tee -a $LOG_FILE
/bin/cat << EOF | $PYTHON $VIF_UP $SLICE_NAME 2>&1 | tee -a $LOG_FILE
$IF_TYPE$LOCAL_SID-$IF_NUM
$PRIVATE_IP
$NETMASK
gre=$SLICE_ID
remote=$REMOTE_IP
pointopoint=$POINTOPOINT
EOF

if [ $? -eq 0 ]; then
	if [ $IF_NUM -eq 1 ]; then
		echo "$SLICE_NAME $IF_NUM" >> $IF_FILE
	else
		/bin/sed -i -e "s/^$SLICE_NAME $OLD_NUM/$SLICE_NAME $IF_NUM/" $IF_FILE
	fi


	/sbin/route del -host $POINTOPOINT
	/sbin/route add -net $POINTOPOINT/$NETMASK gw $PRIVATE_IP
	exit 0

fi

exit 1


