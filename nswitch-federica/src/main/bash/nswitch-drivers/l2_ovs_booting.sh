#!/bin/sh
#Open vSwitch init-script

#Enabling eth7 (Federica side)
/sbin/ifconfig eth7 up

#Inserting kernel module of the Open vSwitch
/sbin/insmod /root/openvswitch/datapath/linux/openvswitch_mod.ko

#Running Open vSwitch database service
/usr/local/sbin/ovsdb-server --remote=punix:/usr/local/var/run/openvswitch/db.sock --remote=db:Open_vSwitch,manager_options --private-key=db:SSL,private_key --certificate=db:SSL,certificate --bootstrap-ca-cert=db:SSL,ca_cert --pidfile --detach

#
#Before starting ovs-vswitchd itself, you need to start its
#configuration database, ovsdb-server.  Each machine on which Open
#vSwitch is installed should run its own copy of ovsdb-server
#

#Starting ovs-vswitchd, which creates the interfaces and bridges in kernel space (gre interfaces are not shown with ifconfig command)
#If the Open vSwitch database (persistent) is describing bridges that  include linux-implemented vlan interfaces erros will occur if these interfaces
#have not been created before ovs-vswitch initiation.

#Example of error
#2012-03-07T16:26:28Z|00015|netdev_linux|WARN|/sys/class/net/eth7.2830/carrier: open failed: No such file or directory
#2012-03-07T16:26:28Z|00016|netdev|WARN|failed to get flags for network device eth7.2830: No such device
#2012-03-07T16:26:28Z|00017|bridge|WARN|could not open network device eth7.2830 (No such device)
#2012-03-07T16:26:28Z|00018|bridge|WARN|eth7.2830 port has no interfaces, dropping
#2012-03-07T16:26:28Z|00019|bridge|INFO|destroyed port eth7.2830 on bridge br1

/usr/local/sbin/ovs-vswitchd --pidfile --detach
