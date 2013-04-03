package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.novi.resources.discovery.database.communic.MonitoringServCommun;

public class JsonLib {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper m = new ObjectMapper();
		// can either use mapper.readTree(JsonParser), or bind to JsonNode
		JsonNode rootNode = m.readValue(new File("src/test/resources/user.json"), JsonNode.class);
		// ensure that "last name" isn't "Xmler"; if is, change to "Jsoner"
		JsonNode nameNode = rootNode.path("name");
		String lastName = nameNode.path("last").textValue();
		if ("xmler".equalsIgnoreCase(lastName)) {
			((ObjectNode)nameNode).put("last", "Jsoner");
		}
		// and write it out:
		//m.writeValue(new File("user-modified.json"), rootNode);
		System.out.println("Last Name: " + lastName + " first name: " + nameNode.path("first").textValue());

	}
	
	@Test
	public void testMon() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper m = new ObjectMapper();
		// can either use mapper.readTree(JsonParser), or bind to JsonNode
		JsonNode rootNode = m.readValue(new File("src/test/resources/monitoring.json"), JsonNode.class);
		// ensure that "last name" isn't "Xmler"; if is, change to "Jsoner"
		JsonNode dataNode = rootNode.path("DATA");
		int secVal = dataNode.get(0).get(1).asInt();

		System.out.println("An inside value is : " + rootNode.path("HDR").path("HDRINFO").get(1).path("NAME").asText());
		System.out.println("0,0 = " + dataNode.get(0).get(0).asInt());
		System.out.println("1,2 = " + dataNode.get(1).get(2).asInt());
		
		
		String getField = "Free Memory";
		int index = 0;
		for(int i = 0 ; ;i++ )
		{
			if (rootNode.path("HDR").path("HDRINFO").get(i) == null)
			{
				System.out.println("I reach the end of the table, the field was not found");
				break;

			}
			else if (rootNode.path("HDR").path("HDRINFO").get(i).path("NAME").asText().equals(getField))
			{
				System.out.println("The index is :" + i);
				index = i;
				break;
			}
		}
		
		System.out.println("The value is : " + rootNode.path("DATA").get(0).get(index));
		

	}
	
	@Test
	public void testMon2() throws JsonParseException, JsonMappingException, IOException {
		String type = "http://fp7-novi.eu/monitoring_features.owl#FreeDiskSpace";
		String getField = "Available Disk Size";
		
		ObjectMapper m = new ObjectMapper();
		// can either use mapper.readTree(JsonParser), or bind to JsonNode
		JsonNode rootNode = m.readValue(new File("src/test/resources/monitoring2.json"), JsonNode.class);

		JsonNode typeNode = rootNode.path(type);
		JsonNode dataNode = typeNode.path("DATA");
		int secVal = dataNode.get(0).get(1).asInt();

		System.out.println("An inside value is : " + typeNode.path("HDR").path("HDRINFO").get(1).path("NAME").asText());
		System.out.println("0,0 = " + dataNode.get(0).get(0).asInt());
		
		
		
		int index = 0;
		for(int i = 0 ; ;i++ )
		{
			if (typeNode.path("HDR").path("HDRINFO").get(i) == null)
			{
				System.out.println("I reach the end of the table, the field was not found");
				break;

			}
			else if (typeNode.path("HDR").path("HDRINFO").get(i).path("NAME").asText().equals(getField))
			{
				System.out.println("The index is :" + i);
				index = i;
				break;
			}
		}
		
		System.out.println("The value is : " + typeNode.path("DATA").get(0).get(index));
		

	}
	
	/*@Test
	public void testGetValue2() throws JsonParseException, JsonMappingException, IOException {
		String type = "http://fp7-novi.eu/monitoring_features.owl#FreeDiskSpace";
		String getField = "Available Disk Size";
		


		MonitoringServCommun mon = new MonitoringServCommun();
		JsonNode n = mon.getValue("src/test/resources/monitoring2.json");
		System.out.print(n.asInt());
	}*/
	

}
