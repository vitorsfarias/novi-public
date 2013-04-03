/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.clients;

import java.io.Serializable;

import java.util.List;

import eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException;


/**
 * XMRLPC Client for contanting the AM
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 * @author <a href="mailto:steluta.gheorghiu@i2cat.net">Steluta Gheorghiu - i2CAT</a>
 */
public interface XMLRPCClient {
	final int TIMEOUT = 1800;

   /**
    * Calls the testbed Registry. Registry keeps information about slices 
    * (but no resources in the slice) and users
    * @param commandName: according with the expected by the registry: add, remove, list...
    * @param params
    * @return an object that varies depending on the action
    * @throws XMLRPCClientException if something goes wrong
    */
	Object execXMLRPCRegistry(String commandName, List<Serializable> params) throws XMLRPCClientException;
	
	/**
	 * Calls the testbed Aggregate Manager (AM). These calls are more testbed related and related with the slices.
	 * @param commandName: according with the expected by the AM: add, remove, list...
	 * @param params
	 * @return
	 * @throws XMLRPCClientException
	 */
	Object execXMLRPCAggregate(String commandName, List<Serializable> params) throws XMLRPCClientException;
	
}
