'''
Created on Oct 12, 2011

@author: steger
@summary: Here we define the nodes that can take part in monitoring procedures

@note: how to extract information
for h in novilab.elte.hu planetlab1-novi.lab.netmode.ece.ntua.gr planetlab2-novi.lab.netmode.ece.ntua.gr smilax1.man.poznan.pl smilax2.man.poznan.pl smilax3.man.poznan.pl smilax4.man.poznan.pl smilax5.man.poznan.pl; do echo -n "\"$h\", "; ssh site_admin@$h -i ~/Private/ssh/novi_rsa /sbin/ifconfig | awk '/^[^[:space:]]/ { iface = $1} /inet addr/ { printf ("(\"%s\", \"%s\"), ", iface, $2) }' | sed s,addr.,,g | sed s/', $'// ; done
'''

from Resource.node import node
from Resource.path import path
from Resource.interface import interface
from Example.Units import UM

# PL node resources
direction = interface.INGRESS | interface.EGRESS
PLnodes = []
def extendpl(hostname, ifaces):
    n = node(name = hostname, resourceid = hostname)
    for iface, ip in ifaces:
        I = interface(name = iface, resourceid = "%s:%s" % (hostname, iface))
        ipwu = ip, UM.ipv4dotted
        if iface == "eth0":
            I.setvalues(ifacename = iface, address = ipwu, ispublic = True, direction = direction, hostname = hostname)
        else:
            I.setvalues(ifacename = iface, address = ipwu, ispublic = False, direction = direction)
        n.addinterface(I)
    PLnodes.append(n)

extendpl("novilab.elte.hu", [("eth0", "157.181.175.243"), ("federica", "192.168.29.45"), ("novi", "192.168.28.97"), ("novi_monitoring", "192.168.31.21")])
extendpl("planetlab1-novi.lab.netmode.ece.ntua.gr", [("eth0", "147.102.22.66"), ("federica", "192.168.29.57"), ("novi", "192.168.28.161"), ("novi_monitoring", "192.168.31.33"), ("tun515-1", "192.168.20.1")])
extendpl("planetlab2-novi.lab.netmode.ece.ntua.gr", [("eth0", "147.102.22.67"), ("federica", "192.168.29.61"), ("novi", "192.168.28.165"), ("tap514-1", "192.168.20.3")])
extendpl("smilax1.man.poznan.pl", [("eth0", "150.254.160.19"), ("federica", "192.168.29.21"), ("novi", "192.168.28.29"), ("novi_fia_1", "192.168.32.5"), ("novi_monitoring", "192.168.31.13"), ("tap513-1", "192.168.20.4")])
#extendpl("smilax2.man.poznan.pl", [("eth0", "150.254.160.20"), ("federica", "192.168.29.25"), ("novi", "192.168.28.33"), ("novi_fia_2", "192.168.32.5")])
#extendpl("smilax3.man.poznan.pl", [("eth0", "150.254.160.21"), ("federica", "192.168.29.29"), ("novi", "192.168.28.37"), ("novi_fia_2", "192.168.32.17")])
#extendpl("smilax4.man.poznan.pl", [("eth0", "150.254.160.22"), ("federica", "192.168.29.33"), ("novi", "192.168.28.41")])
#extendpl("smilax5.man.poznan.pl", [("eth0", "150.254.160.23"), ("federica", "192.168.29.37"), ("novi", "192.168.28.45")])

PLdict = dict(map(lambda x: (x.name, x), PLnodes))

# PL are fully connected over the Internet
PLpaths = []
for s in PLdict.values():
    for d in PLdict.values():
        if s == d: continue
        name = "%s->%s" % (s.name, d.name)
        PLpaths.append( path(name = name, source = s, destination = d) )


# FED node resources
FEDnodes = []
for nick, addr in [ ("fed.psnc", '192.168.31.1'), ("fed.dfn", '192.168.31.5'), ("fed.garr", '192.168.31.9') ]:
    n = node(name = nick, resourceid = nick)
    I = interface(name = "eth0", resourceid = "%s:eth0" % nick)
    ipwu = (addr, UM.ipv4dotted)
    I.setvalues(ifacename = "eth0", address = ipwu, ispublic = False, direction = direction)
    n.addinterface(I)
    FEDnodes.append(n)

FEDdict = dict(map(lambda x: (x.name, x), FEDnodes))
