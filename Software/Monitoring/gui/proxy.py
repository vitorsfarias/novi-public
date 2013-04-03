'''
Created on Feb 1, 2013

@author: steger
'''
from httplib2 import Http
from conf import url_monitoring_service_root, cachefolder, timeout
from urllib import urlencode

class proxy(object):
    headers = {'Content-type': 'application/x-www-form-urlencoded'}
    proxy = Http(cache = cachefolder, timeout = timeout)

    def post(self, uri, form, setheaders = False):
        data = urlencode( form )
        if setheaders:
            resp, response = self.proxy.request(uri = uri, method = "POST", body = data, headers = self.headers)
        else:
            resp, response = self.proxy.request(uri = uri, method = "POST", body = data)
        print "-"*60
        print "SERVER SAYS:"
        print response
        print "-"*60
        if  resp.status != 200:
            raise Exception("Service responded with status %s" % resp.status)
        return response

class msproxy(proxy):
    def call(self, method, query):
        url = "%s/%s" % (url_monitoring_service_root, method)
        data = {'query': query}
        return self.post(uri = url, form = data, setheaders = True)
