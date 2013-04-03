package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class OwlCreatorTest {
	//for more test looks in ReservationSliceTest
	
	private static final transient Logger log =
			LoggerFactory.getLogger(ReservationSliceTest.class);
	


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}



	
	
	@Test
	public void testImportOntology() throws RepositoryException
	{
		String st1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<rdf:RDF\n" +
					"xmlns=\"http://fp7-novi.eu/im.owl#\"\n" +
				    "xmlns:im=\"http://fp7-novi.eu/im.owl#\">";
					
		String st2 = "\n\n<rdf:Description rdf:about=\"http://fp7-novi.eu/im.owl#PlanetLab\">";
		
		log.debug("initial string: \n{}", st1 + st2);
		log.debug("After the import: \n{},", OwlCreator.importOntology(st1 + st2));
		assertTrue(OwlCreator.importOntology(st1 + st2).equals(st1+ "\n" + OwlCreator.getHeaderOntol()  + st2));

		
	}
}
