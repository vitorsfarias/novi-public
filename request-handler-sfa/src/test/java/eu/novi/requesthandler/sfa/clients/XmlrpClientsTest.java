/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.clients;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.junit.Test;

import eu.novi.requesthandler.sfa.clients.FedXMLRPCClient;
import eu.novi.requesthandler.sfa.clients.NoviplXMLRPCClient;
import eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException;

public class XmlrpClientsTest {
	
	@Test
	public void createFedericaClient() {
		FedXMLRPCClient f;
		try {
			f = new FedXMLRPCClient();
			assertNotNull(f);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void createPlanetLabClient() {
		NoviplXMLRPCClient f;
		try {
			f = new NoviplXMLRPCClient();
			assertNotNull(f);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void setClientFed() throws XMLRPCClientException {
		FedXMLRPCClient fedClient = new FedXMLRPCClient();
		XmlRpcClient client = new XmlRpcClient();
		fedClient.setClient(client);
	}
	
	@Test
	public void clientFedAMWorks() throws XMLRPCClientException {
		FedXMLRPCClient fedClient = new FedXMLRPCClient();
		XmlRpcClient client = mock(XmlRpcClient.class);

		try {			
			fedClient.setClient(client);
			List list = new ArrayList();
			when(client.execute("hola", list)).thenReturn(1);
			int o = (Integer) fedClient.execXMLRPCAggregate("hola", list);
			assertEquals(1, o);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}
	
	@Test
	public void clientFedRegWorks() throws XMLRPCClientException {
		FedXMLRPCClient fedClient = new FedXMLRPCClient();
		XmlRpcClient client = mock(XmlRpcClient.class);

		try {			
			fedClient.setClient(client);
			List list = new ArrayList();
			when(client.execute("hola", list)).thenReturn(1);
			int o = (Integer) fedClient.execXMLRPCRegistry("hola", list);
			assertEquals(1, o);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}
	
	@Test
	public void clientPLRegWorks() throws XMLRPCClientException {
		NoviplXMLRPCClient plClient = new NoviplXMLRPCClient();
		XmlRpcClient client = mock(XmlRpcClient.class);

		try {			
			plClient.setClient(client);
			List list = new ArrayList();
			when(client.execute("hola", list)).thenReturn(1);
			int o = (Integer) plClient.execXMLRPCRegistry("hola", list);
			assertEquals(1, o);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}
	
	@Test
	public void clientFedAMThrowsException() {
		try {		
			FedXMLRPCClient fedClient = new FedXMLRPCClient();
			XmlRpcClient client = mock(XmlRpcClient.class);

			fedClient.setClient(client);
			List list = new ArrayList();
			XmlRpcException e = new XmlRpcException("mock error");
			when(client.execute("hola", list)).thenThrow(e);
			int o = (Integer) fedClient.execXMLRPCAggregate("hola", list);
			assertEquals(1, o);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (XMLRPCClientException e) {
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: " +
					"ERROR executing command in FEDERICA AM XMLRPC-Client: org.apache.xmlrpc.XmlRpcException: mock error",
					e.toString());
		}
	}
	
	@Test
	public void clientPLAMThrowsException() {
		try {		
			NoviplXMLRPCClient plClient = new NoviplXMLRPCClient();
			XmlRpcClient client = mock(XmlRpcClient.class);

			plClient.setClient(client);
			List list = new ArrayList();
			XmlRpcException e = new XmlRpcException("mock error");
			when(client.execute("hola", list)).thenThrow(e);
			int o = (Integer) plClient.execXMLRPCAggregate("hola", list);
			assertEquals(1, o);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (XMLRPCClientException e) {
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: " +
					"ERROR executing command in NOVIPL AM XMLRPC-Client: org.apache.xmlrpc.XmlRpcException: mock error",
					e.toString());
		}
	}
	
	@Test
	public void clientFedRegThrowsException() {
		try {		
			FedXMLRPCClient fedClient = new FedXMLRPCClient();
			XmlRpcClient client = mock(XmlRpcClient.class);

			fedClient.setClient(client);
			List list = new ArrayList();
			XmlRpcException e = new XmlRpcException("mock error");
			when(client.execute("hola", list)).thenThrow(e);
			int o = (Integer) fedClient.execXMLRPCRegistry("hola", list);
			assertEquals(1, o);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (XMLRPCClientException e) {
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: " +
					"ERROR executing command in FEDERICA Registry XMLRPC-Client: org.apache.xmlrpc.XmlRpcException: mock error",
					e.toString());
		}
	}
	
	@Test
	public void clientPLRegThrowsException() {
		try {		
			NoviplXMLRPCClient plClient = new NoviplXMLRPCClient();
			XmlRpcClient client = mock(XmlRpcClient.class);

			plClient.setClient(client);
			List list = new ArrayList();
			XmlRpcException e = new XmlRpcException("mock error");
			when(client.execute("hola", list)).thenThrow(e);
			int o = (Integer) plClient.execXMLRPCRegistry("hola", list);
			assertEquals(1, o);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			assertFalse(true);
		} catch (XMLRPCClientException e) {
			assertEquals("eu.novi.requesthandler.sfa.exceptions.XMLRPCClientException: " +
					"ERROR executing command in NOVIPL Registry XMLRPC-Client: org.apache.xmlrpc.XmlRpcException: mock error",
					e.toString());
		}
	}
	
	@Test
	public void setClientPL() throws XMLRPCClientException {
		NoviplXMLRPCClient plClient = new NoviplXMLRPCClient();
		XmlRpcClient client = new XmlRpcClient();
		plClient.setClient(client);
	}
	
	@Test
	public void clientPLWorks() throws XMLRPCClientException {
		NoviplXMLRPCClient plClient = new NoviplXMLRPCClient();
		XmlRpcClient client = mock(XmlRpcClient.class);

		try {			
			plClient.setClient(client);
			List list = new ArrayList();
			when(client.execute("hola", list)).thenReturn(1);
			int o = (Integer) plClient.execXMLRPCAggregate("hola", list);
			assertEquals(1, o);
		} catch (XmlRpcException e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

}
