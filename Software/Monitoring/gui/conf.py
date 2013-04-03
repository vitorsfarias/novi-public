'''
Created on Feb 12, 2013

@author: steger
'''
from model_keyring import keyring

defaultfolder = "./sample"
cachefolder = "/tmp/.cache"
timeout = 10

#url_monitoring_service_root = "http://150.254.160.28:8080/slice/monitoring/"
url_monitoring_service_root = "http://localhost:1234"
url_slice_service = "http://150.254.160.28:8080/request/handler/listSliceUserRIS"
url_owl_service =  "http://150.254.160.28:8080/request/handler/getslice"


ontology = { 
    'unit': ('../../information-model/monitoring-model/unit.owl', "http://fp7-novi.eu/unit.owl#"),
    'param': ('../../information-model/monitoring-model/monitoring_parameters.owl', "http://fp7-novi.eu/monitoring_parameter.owl#"),
    'feature': ('../../information-model/monitoring-model/monitoring_features.owl', "http://fp7-novi.eu/monitoring_features.owl#"),
    'query': ('../../information-model/monitoring-model/monitoring_query.owl', "http://fp7-novi.eu/monitoring_query.owl#"),   #('monitoring_query.owl', ...)
    'stat': ('../../information-model/monitoring-model/monitoring_stat.owl', 'http://fp7-novi.eu/monitoring_stat.owl#'),
    'core': ('../../information-model/monitoring-model/novi-im.owl', "http://fp7-novi.eu/im.owl#"),
}

#FIXME: 'steger@complex.elte.hu'
credential = ("ykryftis@netmode.ece.ntua.gr",
 [
  {'cred_type': keyring.UNAMEPKEY, 'username': 'root', 'keyfilename': '/home/steger/Private/ssh/novi_rsa'},
 ])

preload_topology = "sample/1node.owl"
savequery = "/tmp/slice_query.owl" 