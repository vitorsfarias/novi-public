package eu.novi.im.util;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;

import eu.novi.im.core.Topology;
import eu.novi.im.policy.impl.NOVIUserImpl;


/**
 * 
 * 
 * class that provide validation function for the NOVI IM java topologies
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ValidationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testHasSinkHasSource() throws RepositoryException, IOException
	{
		IMRepositoryUtil imRep = new IMRepositoryUtilImpl();

		String requestTest = readFile("src/test/resources/2slivers_1router_bound.owl");
		Topology reqTopo = imRep.getIMObjectFromString(requestTest, Topology.class);
		Validation validation = new Validation();
		String st = validation.checkLinksForSinkSource(reqTopo);
		System.out.println(st);
		assertTrue(st.equals("")); //valid
		
		requestTest = readFile("src/test/resources/MidtermWorkshopRequest_bound_slice2_v8.owl");
		reqTopo = imRep.getIMObjectFromString(requestTest, Topology.class);
		String st2 = validation.checkLinksForSinkSource(reqTopo);
		System.out.println(st2);
		assertTrue(st2.equals("The link http://fp7-novi.eu/im.owl#link3-lrouter-lrouter2 doesn't have hasSource\n"+
				"The link urn:publicid:IDN+federica.eu+link+psnc.poz.router1.ge-0/0/0-" +
				"dfn.erl.router1.ge-0/1/0 doesn't have hasSink\n")); //not valid


	}
	
	
	public static String readFile(String path) throws IOException{
		File file = new File(path);
		StringBuffer result = new StringBuffer();
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String currentLine = reader.readLine();
		while(currentLine != null)
		{
				result.append(currentLine);
				currentLine = reader.readLine();
		}
		
		return result.toString();
	}

}
