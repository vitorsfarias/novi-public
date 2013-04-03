/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: license.txt
 *
 */
package eu.novi.requesthandler.sfa.clients;

import static org.junit.Assert.*;

import org.junit.Test;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.parser.NullParser;


import eu.novi.requesthandler.sfa.clients.MyTypeFactory;


public class MyTypeFactoryTest {

	@Test
	public void createTypeFactoryTest(){
		XmlRpcClient client = new XmlRpcClient();
		
		MyTypeFactory factory = new MyTypeFactory(client);
		assertNotNull(factory);
	}
	
	@Test
	public void getParserShouldReturnNullParser(){
		XmlRpcClient client = new XmlRpcClient();
		
		MyTypeFactory factory = new MyTypeFactory(client);
		Object parser = factory.getParser(null, null, "", "nil");
		assertTrue(parser instanceof NullParser);		
	}
	
	@Test
	public void getParserShouldReturnNull(){
		XmlRpcClient client = new XmlRpcClient();
		
		MyTypeFactory factory = new MyTypeFactory(client);
		Object parser = factory.getParser(null, null, "", "hola");
		assertNull(parser);		
	}
	
	@Test
	public void getParserShouldReturnNull2(){
		XmlRpcClient client = new XmlRpcClient();
		
		MyTypeFactory factory = new MyTypeFactory(client);
		Object parser = factory.getParser(null, null, "hola", "nil");
		assertNull(parser);		
	}
	
	@Test
	public void getParserShouldReturnNull3(){
		XmlRpcClient client = new XmlRpcClient();
		
		MyTypeFactory factory = new MyTypeFactory(client);
		Object parser = factory.getParser(null, null, "hola", "hola");
		assertNull(parser);		
	}
}
