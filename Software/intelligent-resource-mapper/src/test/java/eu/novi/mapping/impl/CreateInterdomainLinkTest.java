/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Interface;
import eu.novi.im.core.NSwitch;
import eu.novi.im.core.Path;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualLink;
import eu.novi.im.core.impl.NSwitchImpl;
import eu.novi.im.util.IMCopy;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.mapping.utils.IRMOperations;

public class CreateInterdomainLinkTest {
	
	IRMEngine irm;
	
	IMRepositoryUtilImpl imru;
	IMCopy imc = new IMCopy();
	
	// logging
    private static final transient Logger log = 
    	LoggerFactory.getLogger(CreateInterdomainLinkTest.class);
	
	@Before
	public <T> void initialize() throws IOException {
		
		log.debug("Initializing CreateInterdomainLinkTest...");
		
		// Creating mock classes
		LogService logService = mock(LogService.class);

		log.debug("Setting up local environment for IRM...");
		irm = new IRMEngine();
		irm.setLogService(logService);
		
		log.debug("CreateInterdomainLinkTest initialized");
		
	}

	@Test
	public void createInterdomainLinkTest() throws IOException {
		log.debug("Running createInterdomainLinkTest...");
		
		Topology topology = createTopology("src/main/resources/CreateInterdomainLinkRequest.owl");
		Set<Path> paths = new HashSet<Path>();
		for (Resource res : topology.getContains()) {
			if (res instanceof VirtualLink) {
				Path result = IRMOperations.createInterDomainLink((VirtualLink) res);
				assertNotNull(result);
				paths.add(result);
			}
		}
		
		// 4 NSwitch Paths created
		assertEquals(4,paths.size());
		for (Path path : paths) {
			// 3 Resources per Path
			assertEquals(3, path.getContains().size());
			for (Resource res : path.getContains()) {
				if (res instanceof NSwitch) {
					// NSwitch has 1 out iface & 1 in iface
					assertEquals(1, ((NSwitchImpl) res).getHasSource().size());
					assertEquals(1, ((NSwitchImpl) res).getHasSink().size());
					// And belongs to 1 path
					assertEquals(1, ((NSwitchImpl) res).getInPaths().size());
				} else if (res instanceof Interface) {
					if (res.toString().equals("http://fp7-novi.eu/im.owl#urn:publicid:IDN novipl:novi interface planetlab2-novi.lab.netmode.ece.ntua.gr:eth0-in")) {
						log.info("planetlab2 NSwitch interface in");
						// belongs to 1 path
						assertEquals(1, ((Interface) res).getInPaths().size());
						assertEquals(path, ((Interface) res).getInPaths().iterator().next());
						// is sink of 1 nswitch
						assertEquals(1, ((Interface) res).getIsSink().size());
					} else if (res.toString().equals("http://fp7-novi.eu/im.owl#urn:publicid:IDN novipl:novi interface planetlab2-novi.lab.netmode.ece.ntua.gr:eth0-out")) {
						log.info("planetlab2 NSwitch interface out");
						// belongs to 1 path
						assertEquals(1, ((Interface) res).getInPaths().size());
						assertEquals(path, ((Interface) res).getInPaths().iterator().next());
						// is source of 1 nswitch
						assertEquals(1, ((Interface) res).getIsSource().size());
						// has 1 next relation to nswitch
						assertEquals(1, ((Interface) res).getNexts().size());
					} else if (res.toString().equals("http://fp7-novi.eu/im.owl#urn:publicid:IDN federica.eu interface psnc.poz.router1:ge-0/2/9-out")) {
						log.info("psnc NSwitch interface out");
						// belongs to 2 paths
						assertEquals(2, ((Interface) res).getInPaths().size());
						assertTrue(((Interface) res).getInPaths().contains(path));
						// is source of 2 nswitches
						assertEquals(2, ((Interface) res).getIsSource().size());
						// has 2 next relation to nswitches
						assertEquals(2, ((Interface) res).getNexts().size());
					} else if (res.toString().equals("http://fp7-novi.eu/im.owl#urn:publicid:IDN federica.eu interface psnc.poz.router1:ge-0/2/9-in")) {
						log.info("psnc NSwitch interface in");
						// belongs to 2 paths
						assertEquals(2, ((Interface) res).getInPaths().size());
						assertTrue(((Interface) res).getInPaths().contains(path));
						// is sink of 2 nswitches
						assertEquals(2, ((Interface) res).getIsSink().size());
					} else if (res.toString().equals("http://fp7-novi.eu/im.owl#urn:publicid:IDN novipl:novi interface smilax1.man.poznan.pl:eth0-out")) {
						log.info("smilax1 NSwitch interface out");
						// belongs to 1 path
						assertEquals(1, ((Interface) res).getInPaths().size());
						assertEquals(path, ((Interface) res).getInPaths().iterator().next());
						// is source of 1 nswitch
						assertEquals(1, ((Interface) res).getIsSource().size());
						// has 1 next relation to nswitch
						assertEquals(1, ((Interface) res).getNexts().size());
					} else if (res.toString().equals("http://fp7-novi.eu/im.owl#urn:publicid:IDN novipl:novi interface smilax1.man.poznan.pl:eth0-in")) {
						log.info("smilax1 NSwitch interface in");
						// belongs to 1 path
						assertEquals(1, ((Interface) res).getInPaths().size());
						assertEquals(path, ((Interface) res).getInPaths().iterator().next());
						// is sink of 1 nswitch
						assertEquals(1, ((Interface) res).getIsSink().size());
					}
				}
			}
		}
		
		log.debug("Result of the createInterdomainLinkTest: "+paths.toString());

	}
	
	private Topology createTopology(String path) throws IOException {
		log.debug("creating mock available Physical Resources...");
		// loading and getting from imrespository util
		imru = new IMRepositoryUtilImpl();
		String stringOwl = readFileAsString(path);
		Topology result = imru.getTopologyFromFile(stringOwl);
		return result;
	}
	
	private static String readFileAsString(String filePath)
			throws java.io.IOException {
    	StringBuffer fileData = new StringBuffer(1000);
    	BufferedReader reader = new BufferedReader(
            new FileReader(filePath));
    	char[] buf = new char[1024];
    	int numRead=0;
    	while((numRead=reader.read(buf)) != -1){
    		String readData = String.valueOf(buf, 0, numRead);
	    	fileData.append(readData);
	        buf = new char[1024];
	    }
	    reader.close();
	    return fileData.toString();
	}
}
