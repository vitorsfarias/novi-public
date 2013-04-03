"""
Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
Copyright according to BSD License
For full text of the license see: ./novi/Software/Monitoring/MonitoringTool/PacketTracking/license.txt

@author <a href="mailto:ramon.masek@fokus.fraunhofer.de">Ramon Masek</a>, Fraunhofer FOKUS
@author <a href="mailto:c.henke@tu-berlin.de">Christian Henke</a>, Technical University Berlin
@author <a href="mailto:carsten.schmoll@fokus.fraunhofer.de">Carsten Schmoll</a>, Fraunhofer FOKUS
@author <a href="mailto:Julian.Vetter@campus.tu-berlin.de">Julian Vetter</a>, Fraunhofer FOKUS
@author <a href="mailto:">Jens Krenzin</a>, Fraunhofer FOKUS
@author <a href="mailto:">Michael Gehring</a>, Fraunhofer FOKUS
@author <a href="mailto:">Tacio Grespan Santos</a>, Fraunhofer FOKUS
@author <a href="mailto:">Fabian Wolff</a>, Fraunhofer FOKUS
"""

from sys import exit, stdout
from os import system
from inspect import getargspec, getmembers
import readline

from Task.Passive.Monitoring.DataTypes import Node
from Task.Passive.Monitoring.PassiveMonitoring import PassiveMonitoring
from Return import Return

class CommandLine(object):    
    def __init__(self):
        self.Options = {"clear" : self.__clear,
                        "exit"  : self.__exit,
                        "help"  : self.__help}

    def __Helptext(self):
        return ("\n"
                " 888888ba  d888888P .d88888b  dP                dP dP\n"
                " 88    `8b    88    88.    \"' 88                88 88\n"
                "a88aaaa8P'    88    `Y88888b. 88d888b. .d8888b. 88 88\n"
                " 88           88          `8b 88'  `88 88ooood8 88 88\n"
                " 88           88    d8'   .8P 88    88 88.  ... 88 88\n"
                " dP           dP     Y88888P  dP    dP `88888P' dP dP\n"
                "oooooooooooooooooooooooooooooooooooooooooooooooooooooo\n"
                "\n"
                "Type 'help' for a list of commands\n"
                "")
        
    def Input(self):
        PM = PassiveMonitoring()
        
        print self.__Helptext()
        while(1):
            try:
                Input = raw_input("-->")
            except EOFError:
                self.__exit()       
            try:
                self.Options[Input]()
            except KeyError:
                try:
                    Return(eval("PM." + Input))
                except TypeError, m:
                    print "Error: " + str(m)
                except (NameError, SyntaxError, AttributeError):
                    try:
                        exec(Input)
                    except Exception:
                        print ("Unknown function, type 'help' to get the list "
                               "of functions:")
 
    def __clear(self):
        system("clear")
        print self.__Helptext()
        
    def __exit(self):
        print ""
        exit(0)
        
    
    def __help(self):
        list = getmembers(PassiveMonitoring)
        print("cmds:")
        for funcName in list:
            if funcName[0][0:1] != "_":
                function = getattr(PassiveMonitoring, str(funcName[0]))
                argSpec = getargspec(function)
                print "    " + funcName[0] + "(",
                for i in range(len(argSpec.args)):
                    if argSpec.args[i] != "self":
                        stdout.write(argSpec.args[i])
                        if i != len(argSpec.args)-1:
                            stdout.write(", ")
                stdout.write(")\n")