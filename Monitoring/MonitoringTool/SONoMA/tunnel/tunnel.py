'''
Created on Dec 7, 2011

@author: steger
'''

from subprocess import Popen
from threading import Thread
from time import sleep
import argparse

agentmap = [
            (44101, "10.255.28.1"),
            (44102, "10.255.28.2"),
            (44103, "10.255.28.3"),
            (44104, "10.255.28.4"),
            (44105, "10.255.28.5"),
            (44106, "10.255.28.6"),
            (44107, "10.255.28.7"),

            (44201, "10.255.28.15"),
            (44202, "10.255.28.16"),
            (44203, "10.255.28.17"),
            (44204, "10.255.28.18"),
            (44205, "10.255.28.19"),
            (44206, "10.255.28.20"),
            (44207, "10.255.28.21"),

            (44301, "10.255.28.8"),
            (44302, "10.255.28.9"),
            (44303, "10.255.28.10"),
            (44304, "10.255.28.11"),
            (44305, "10.255.28.12"),
            (44306, "10.255.28.13"),
            (44307, "10.255.28.14"),
            (44401, "10.255.28.22"),
            (44402, "10.255.28.23"),
            (44403, "10.255.28.24"),
            (44404, "10.255.28.25"),
            (44405, "10.255.28.26"),
            (44406, "10.255.28.27"),
            (44407, "10.255.28.28"),

    ]

#class TunException(Exception):
#    pass

class tunnel(Thread):
    '''
    ssh -N -L60000:10.255.28.15:11122 -lnovi 194.132.52.211
    '''
    ssh = "/usr/bin/ssh"
    def __init__(self, socket, gw, sport, uname, rsa, v):
        Thread.__init__(self, target = self.tunnel, verbose = v)
        self.setDaemon(True)
        self.socket = socket
        self.argv = [ self.ssh, "-N", "-L%d:%s" % (sport, socket), "-l%s" % uname, "-i%s" % rsa, gw]
        self.v = v
        
    def tunnel(self):
        if self.v:
            print "II: %s connected" % self.socket
        self.p = Popen(args = self.argv)
        self.p.wait()
        print "WW: %s was cut off" % self.socket
        self.p = None
 #       raise TunException(self.socket)

    def join(self, timeout = None):
        Thread.join(self, timeout)
        if self.p:
            self.p.kill()
        
def cleanup(tunnels):
    while len(tunnels):
        t = tunnels.pop()
        try:
            t.join(0)
        except:
            pass

def parseargs():
    parser = argparse.ArgumentParser(description='Set up and maintain a list of ssh tunnels via a common gateway')
    parser.add_argument('--verbose', dest="v", action='store_const',
                       const=True, default=False)

    parser.add_argument('--port', type=int, 
                       help='remote destination port (default: 22)', default=22)
    
    parser.add_argument('--gw', type=str, 
                       help='gateway (default: 194.132.52.211)', default="194.132.52.211")
    
    parser.add_argument('-i', metavar="keyfile", type=str, 
                       help='private key file (default: ~/Private/ssh/novi_rsa)', default="/home/steger/Private/ssh/novi_rsa")
    
    parser.add_argument('-l', metavar="username", type=str, 
                       help='username (default: novi)', default="novi")

    parser.add_argument('--map', metavar="filename", type=str, 
                       help='a configuration file containing pairs of "local port" "destination address" in each line')
    
    return parser.parse_args()

def readmap(fn):
    m = []
    with open(fn) as fp:
        for l in fp.readlines():
            p, ip = l.split()[:2]
            m.append(int(p), ip)

if __name__ == '__main__':
    
    args = parseargs()

    if args.map:
        try:
            agentmap = readmap(args.map[0])
        except:
            print "EE: wrong or missing map file %s" % args.map[0]
            exit(0)
        
    fail = 0
    tunnels = []
    for port, ip in agentmap:
        try:
            t = tunnel("%s:%d" % (ip, args.port), args.gw, port, args.l, args.i, args.v)
            t.start()
            tunnels.append(t)
            sleep(.1)
        except:
            print "FAIL TO OPEN: %s" % ip
            fail += 1
    
    n = len(tunnels)
    if n:
        print "%d/%d tunnels set up" % (n-fail, n)
        try:
            tunnels[0].join()
        except KeyboardInterrupt:
            cleanup(tunnels)
    
    print "ok"