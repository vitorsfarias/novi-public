'''
Created on Nov 20, 2012

@author: steger
'''
from Service.mock_framework import Framework

baseurl = '../../information-model/monitoring-model'
FRAMEWORK = Framework(baseurl, {'PlanetLab': 'config_planetlab.owl', 'FEDERICA': 'config_federica.owl'})
