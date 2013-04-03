#!/bin/sh
#
#Ethernet logical interface over GRE removal using vsys scripts
#v0.5

SLICE_NAME=$1
PRIVATE_IP=$2

IF_FILE="/root/interfaces.txt"
PYTHON="/usr/bin/python"
VIF_DOWN="/vsys/vif_down"
IF_TYPE=tap
LOG_FILE="/var/log/L2_tunnels.log"


if [ $# -ne 2 ]; then
	echo "USAGE: $0 SLICE_NAME PRIVATE_IP" 	
	exit 1
fi

LOCAL_SID=`/bin/grep -E "^$SLICE_NAME:" /etc/passwd | /bin/awk -F: '{print $3;}'`

if [ -z $LOCAL_SID ]; then
	echo -n `/bin/date` 2>&1 | tee -a $LOG_FILE
	echo " ERROR: Slice name $SLICE_NAME does not exist!" 2>&1 | tee -a $LOG_FILE
	exit 1
fi


INTERFACE=`/sbin/ifconfig -a | grep -B1 $PRIVATE_IP | head -1 | awk '{print $1;}'`

if [ -z $INTERFACE ]; then
	echo -n `/bin/date` 2>&1 | tee -a $LOG_FILE
	echo " ERROR: No such GRE Interface with IP $PRIVATE_IP !" 2>&1 | tee -a $LOG_FILE
	exit 1
fi 

IF_NUM=`/bin/grep -E "^$SLICE_NAME " "$IF_FILE" | /bin/awk '{print $2}'`

if [ -z $IF_NUM ]; then
	echo -n `/bin/date` 2>&1 | tee -a $LOG_FILE
	echo " ERROR: No GRE Interface for slice $SLICE_NAME !" 2>&1 | tee -a $LOG_FILE
	exit 1
fi


echo -n `/bin/date` 2>&1 | tee -a $LOG_FILE
echo " Deleting GRE interface $INTERFACE (Slice=$SLICE_NAME)" 2>&1 | tee -a $LOG_FILE
/bin/cat << EOF | $PYTHON $VIF_DOWN $SLICE_NAME 2>&1 | tee -a $LOG_FILE
$INTERFACE
EOF

if [ $? -eq 0 ]; then
	if [ $IF_NUM -eq 1 ]; then
		/bin/sed -i "/$SLICE_NAME $IF_NUM/d" $IF_FILE
	else 
		DEV_NUM=`echo $INTERFACE | cut -d- -f2`
		if [ $IF_NUM -eq $DEV_NUM ]; then
			let NEW_NUM=IF_NUM-1
 			/bin/sed -i -e "s/^$SLICE_NAME $IF_NUM/$SLICE_NAME $NEW_NUM/" $IF_FILE		
		fi	

	fi
	exit 0

fi

exit 1
