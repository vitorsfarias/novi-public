package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.util.IMRepositoryUtil;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.resources.discovery.response.FRResponse;


/**
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ******************************************************************************
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class FindResourcesDemoTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
		ManipulateDB.loadOWLFile("PLETopology.owl", "RDFXML", ManipulateDB.TESTBED_CONTEXTS);
		
		//ManipulateDB.loadOWLFile("PLEtopologyModified3_v1.owl",
			//	LocalDbCalls.createNoviURI("secondContext"));

		//ManipulateDB.loadOWLFile("PLEtopologyModified3.owl",
			//	LocalDbCalls.createNoviURI("thirdContext"));
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Test
	public void test() throws IOException {
		/*LocalDbCalls.execPrintStatement(
				LocalDbCalls.createNoviURI("PlanetLab_planetlab1-novi.lab.netmode.ece.ntua.gr"), null, null);
		LocalDbCalls.execPrintStatement(
				LocalDbCalls.createNoviURI("planetlab1-novi.lab.netmode.ece.ntua.gr-cpu"), null, null);
		LocalDbCalls.execPrintStatement(
				LocalDbCalls.createNoviURI("PlanetLab_smilax1.man.poznan.pl"), null, null);
		LocalDbCalls.execPrintStatement(
				LocalDbCalls.createNoviURI("smilax1.man.poznan.pl-cpu"), null, null);*/
		
		IMRepositoryUtil imRep = new IMRepositoryUtilImpl();
		String requestTest = readFile("src/test/resources/MidtermWorkshopRequest_unbound.owl");
		Topology reqTopo = imRep.getIMObjectFromString(requestTest, Topology.class);
		NOVIUser user = new NOVIUserImpl("noviUSer34");
		FindLocalResources findResou = new FindLocalResources();
		FRResponse respo = findResou.findLocalResources(reqTopo, user);
		assertFalse(respo.hasError());
		assertEquals(4, respo.getTopology().getContains().size());
		
		for (Resource r : respo.getTopology().getContains())
		{
			System.out.println(r);
			Node n = (Node) r;
			assertNotNull(n.getHasComponent());
			//System.out.println(n.getHasComponent());
		}
		
		assertNotNull(LocalDbCalls.getLocalResource(
				"http://fp7-novi.eu/im.owl#PlanetLab_planetlab2-novi.lab.netmode.ece.ntua.gr"));
		

	}
	
	
	public static String readFile(String path) {
		File file = new File(path);
		StringBuffer result = new StringBuffer();
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		String currentLine = null;
		try {
			currentLine = reader.readLine();
			while(currentLine != null)
			{
					result.append(currentLine);
					currentLine = reader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return result.toString();
	}

}
