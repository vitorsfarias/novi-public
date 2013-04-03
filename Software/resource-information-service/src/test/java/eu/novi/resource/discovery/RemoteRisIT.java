package eu.novi.resource.discovery;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import eu.novi.framework.IntegrationTesting;
import eu.novi.resources.discovery.remote.serve.RemoteRisServe;

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
 * Integration tests for remote RIS
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
@RunWith(JUnit4TestRunner.class)
public class RemoteRisIT {



		private static final String REMOTE_RIS_SERVICE = 
				"eu.novi.resources.discovery.remote.serve.RemoteRisServe";
		
		private static final String PLANETLAB = "(testbed=PlanetLab)";
		private static final String FEDERICA = "(testbed=FEDERICA)";

		//@Configuration 
		public static Option[] configuration() throws Exception {
			return IntegrationTesting.createConfigurationWithBundles();
		}


		//@Test
		public void findRis (BundleContext ctx) throws Exception {

			assert(ctx != null);
			ctx.getServiceReferences(null, null); //workaround - sometimes  service is not found

			ServiceReference[] serviceReferences = ctx.getServiceReferences(REMOTE_RIS_SERVICE, null);
			assert(serviceReferences != null);
			assertEquals(2, serviceReferences.length);
			
			ServiceReference[] serviceReferences1 = ctx.getServiceReferences(REMOTE_RIS_SERVICE, PLANETLAB);
			assert(serviceReferences1 != null);
			assertEquals(1, serviceReferences1.length);
			
			ServiceReference[] serviceReferences2 = ctx.getServiceReferences(REMOTE_RIS_SERVICE, FEDERICA);
			assert(serviceReferences2 != null);
			assertEquals(1, serviceReferences2.length);

			// Testing log service
			ServiceTracker loggerTracker = new ServiceTracker(ctx,LogService.class.getName(), null);
			loggerTracker.open();
			LogService logger = (LogService)loggerTracker.getService();
			assert(logger != null);
			logger.log(serviceReferences[0],LogService.LOG_INFO,"First!");

		}
		
		
		//@Test
		public void callRemoteRIS(BundleContext ctx) throws InvalidSyntaxException
		{
			System.out.println("Calling planetlab");
			ServiceReference[] serviceReferences = ctx.getServiceReferences(REMOTE_RIS_SERVICE, PLANETLAB);

			for (ServiceReference serv : serviceReferences)
			{
				final RemoteRisServe remoteRis = (RemoteRisServe) ctx.getService(serv);
				// planetlab
				//remoteRis.giveLocalPartitioningCost(null);

			}
			
			System.out.println("Calling federica");
			ServiceReference[] serviceReferences1 = ctx.getServiceReferences(REMOTE_RIS_SERVICE, FEDERICA);

			for (ServiceReference serv : serviceReferences1)
			{
				final RemoteRisServe remoteRis = (RemoteRisServe) ctx.getService(serv);
				//federica 
				//remoteRis.giveLocalPartitioningCost(null);

			}
			   
			
		}
		
		
}
