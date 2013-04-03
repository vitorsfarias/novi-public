'''
Created on Oct 27, 2011

@author: steger
'''
from Credential.credentialtypes import UsernameRSAKey, UsernamePassword
from os import path

noviCredentialIARGS = { 'username': 'novi_novi', 'rsakey': path.expanduser("~/Private/ssh/novi_rsa") }
noviCredential = UsernameRSAKey(**noviCredentialIARGS)

novisaCredentialIARGS = { 'username': 'root', 'rsakey': path.expanduser("~/Private/ssh/novi_rsa") }
novisaCredential = UsernameRSAKey(**novisaCredentialIARGS)

novihadesCredentialIARGS = { 'username': 'novi-monitoring', 'rsakey': path.expanduser("~/Private/ssh/novimonitoring_rsa") }
novihadesCredential = UsernameRSAKey(**novihadesCredentialIARGS)

sonomaCredentialIARGS = {'username': "guest", 'password': "guest"}
sonomaCredential = UsernamePassword(**sonomaCredentialIARGS)

g3CredentialIARGS = {'username': "monitor1", 'password': "m/n.t,r1"}
g3Credential = UsernamePassword(**g3CredentialIARGS)

fedsubstrateCredentialIARGS = {'username': "novi-reader", 'password': "JS5no6vi7JS"}
fedsubstrateCredential = UsernamePassword(**fedsubstrateCredentialIARGS)


#mykeyring = [ noviCredentialIARGS, sonomaCredentialIARGS, novihadesCredentialIARGS ]

ple_credentials = [ novisaCredentialIARGS, sonomaCredentialIARGS ]
fed_credentials = [ novisaCredentialIARGS, novihadesCredentialIARGS, g3CredentialIARGS, fedsubstrateCredentialIARGS ]

if __name__ == '__main__':
    pass