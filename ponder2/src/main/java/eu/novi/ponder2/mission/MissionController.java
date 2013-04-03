/**
 * Copyright 2007 Kevin Twidle, Imperial College, London, England.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 *
 * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
 *
 * Created on Apr 7, 2008
 *
 * $Log:$
 */

package eu.novi.ponder2.mission;

import java.util.Map;


import eu.novi.ponder2.ManagedObject;
import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Array;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Null;
import eu.novi.ponder2.objects.P2Object;

/**
 * A MissionController is responsible for managing missions in its SMC. It can
 * load, unload, start and stop missions.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class MissionController implements ManagedObject {

	private int missionNumber = 0;
	private String mname = "Mission";
	private P2Object missionDomain;
	private P2Object policyfactory;
	private P2Object domainFactory;
	private P2Object myP2Object;

	/**
	 * Creates a MissionController using aDomain as the main mission root.
	 * 
	 * @throws Ponder2Exception
	 * 
	 */
	@Ponder2op("domainFactory:policyFactory:")
	public MissionController(P2Object myP2Object, P2Object aDomainFactory,
			P2Object anEcaPolicyFactory) throws Ponder2Exception {
		this.myP2Object = myP2Object;
		policyfactory = anEcaPolicyFactory;
		domainFactory = aDomainFactory;
		missionDomain = domainFactory.operation(myP2Object, "create");
	}

	/**
	 * Loads aMission into the local SMC and returns the mission name
	 * 
	 * @param aMission
	 * @return
	 * @throws Ponder2Exception
	 */
	@Ponder2op("load:")
	String load(P2Object aMission) throws Ponder2Exception {
		return load(aMission, new P2Hash());
	}

	/**
	 * Loads aMission into the local SMC and returns the mission name.
	 * Interfaces are supplied in aHash.
	 * 
	 * @param aMission
	 * @param aHash
	 * @return
	 * @throws Ponder2Exception
	 */
	@Ponder2op("load:with:")
	String load(P2Object aMission, P2Object aHash) throws Ponder2Exception {
		// Get a copy of the hash, we don't want to alter the original
		P2Hash argHash = new P2Hash(aHash.asHash().asMap());
		missionNumber++;
		String missionName = mname + missionNumber;

		// Make up a new domain
		P2Object domain = domainFactory.operation(myP2Object, "create");
		missionDomain.operation(myP2Object, "at:put:",
				P2Object.create(missionName), domain);

		// Set up a chroot environment
		argHash.put("root", domain);
		argHash.put("missionroot", domain);
		// Check that the needed interfaces are present and subscribe to their
		// events
		for (P2Object anInterface : aMission
				.operation(myP2Object, "interfaces").asArray()) {
			if (argHash.asHash().containsKey(anInterface.asString())) {
				P2Object iface = argHash.asHash().get(anInterface.asString());
				iface.operation(myP2Object, "subscribe:", domain);
			} else {
				throw new Ponder2ArgumentException("Mission " + missionName
						+ " missing interface \"" + anInterface + "\"");
			}
		}
		// Grab the policies from the mission and instantiate them
		P2Hash policies = (P2Hash) aMission.operation(myP2Object, "policies");
		for (Map.Entry<String, P2Object> entry : policies.asMap().entrySet()) {
			String pname = entry.getKey();
			P2Object pblock = entry.getValue();
			// Create a new policy
			P2Object newPolicy = policyfactory.operation(myP2Object, "create");
			argHash.operation_at_put("policy", newPolicy);
			// Attach it to the domain for receiving events
			newPolicy.operation(myP2Object, "attach:", domain);
			// Build the policy using the block from the mission
			pblock.operation(myP2Object, "valueHash:", argHash);
			domain.operation(myP2Object, "at:put:", P2Object.create(pname),
					newPolicy);
			newPolicy.operation(myP2Object, "active:", P2Object.create(true));
		}

		// start(missionName);
		P2Object block = aMission.operation(myP2Object, "onStart");
		if (block != P2Null.Null)
			block.operation(myP2Object, "valueHash:", argHash);

		return missionName;
	}

	/**
	 * Unloads aMissionName from the SMC. If the mission is running then it is
	 * stopped first.
	 * 
	 * @param aMissionName
	 * @throws Ponder2Exception
	 */
	@Ponder2op("unload:")
	void unload(String aMissionName) throws Ponder2Exception {
		stop(aMissionName);
	}

	/**
	 * Starts aMissionName running. This involves making sure all the policies
	 * are hooked up to the event bus and that all the policies active.
	 * 
	 * @param aMissionName
	 * @throws Ponder2Exception
	 */
/*	@Ponder2op("start:")
	void start(String aMissionName) throws Ponder2Exception {
		P2Object mission = missionDomain.operation(myP2Object, "at:",
				P2Object.create(aMissionName));
		mission.operation(myP2Object, "start");
	}*/
	@Ponder2op("start:")
	   void start(String aMissionName) throws Ponder2Exception {
	     P2Object domain = missionDomain.operation(myP2Object, "at:", P2Object.create(aMissionName));
	     //then find the policies in the domain (list them and then activate them)
	     System.out.println( domain.operation(myP2Object, "listNames"));
	     P2Array thepolicies = (P2Array) domain.operation(myP2Object, "listNames");
	         for (int i = 0; i < thepolicies.size(); i++) {
	             System.out.println(thepolicies.at(i).toString());
	             P2Object oldPolicy = domain.operation(myP2Object, "at:", thepolicies.at(i).toString());
	             oldPolicy.operation(myP2Object, "active:", P2Object.create(true));
	            }
	      
	  }
	/**
	 * Stops aMissionName. This involves making all the policies inactive.
	 * 
	 * @param aMissionName
	 * @throws Ponder2Exception
	 */
	/*@Ponder2op("stop:")
	void stop(String aMissionName) throws Ponder2Exception {
		missionDomain.operation(myP2Object, "at:",
				P2Object.create(aMissionName));
	}*/
	
	@Ponder2op("stop:")
	  void stop(String aMissionName) throws Ponder2Exception {
	      //We have to get the domain
	      P2Object domain = missionDomain.operation(myP2Object, "at:", P2Object.create(aMissionName));
	      //then find the policies in the domain (list them and then deactivate them)
	      System.out.println( domain.operation(myP2Object, "listNames"));
	      P2Array thepolicies = (P2Array) domain.operation(myP2Object, "listNames");
	           for (int i = 0; i < thepolicies.size(); i++) {
	                 System.out.println(thepolicies.at(i).toString());
	                 P2Object oldPolicy = domain.operation(myP2Object, "at:", thepolicies.at(i).toString());
	                 oldPolicy.operation(myP2Object, "active:", P2Object.create(false));
	                 }
	   }

	/**
	 * Answers an array containing all the loaded mission names
	 * 
	 * @return
	 */
	/*@Ponder2op("missions")
	P2Object missions() {
		return P2Object.create();
	}*/
	@Ponder2op("missions")
	  P2Object missions() throws Ponder2Exception {
	  return missionDomain.operation(myP2Object,"listNames");
	  }
	
	

}
