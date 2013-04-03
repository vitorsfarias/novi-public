package eu.novi.resources.discovery.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * A class that provides the IP for the NOVI physical machines
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class NoviIPs {
	
	private static final transient Logger log =
			LoggerFactory.getLogger(NoviIPs.class);
	
	private static final Map<String, String> PUBLIC_IP = new HashMap<String, String>();
	private static final Map<String, String> PUBLIC_FEDERICA_IP = new HashMap<String, String>();
	

	static {
		//Planetlab
		PUBLIC_IP.put("smilax1", "150.254.160.19");
		PUBLIC_IP.put("smilax2", "150.254.160.20");
		PUBLIC_IP.put("smilax3", "150.254.160.21");
		PUBLIC_IP.put("smilax4", "150.254.160.22");
		PUBLIC_IP.put("smilax5", "150.254.160.23");
		PUBLIC_IP.put("planetlab1-novi.lab.netmode.ece.ntua.gr", "147.102.22.66");
		PUBLIC_IP.put("planetlab2-novi.lab.netmode.ece.ntua.gr", "147.102.22.67");
		PUBLIC_IP.put("novilab.elte.hu", "157.181.175.243");
		PUBLIC_IP.put("dfn-novi-ple1.x-win.dfn.de", "188.1.240.194");
		//PUBLIC_IP.put("www.test.pl", "212.85.97.133");

		//Federica
		PUBLIC_FEDERICA_IP.put("dfn.erl.router1", "194.132.52.2");
		PUBLIC_FEDERICA_IP.put("dfn.erl.vserver1", "194.132.52.166");
		PUBLIC_FEDERICA_IP.put("dfn.erl.vserver2", "194.132.52.174");
		PUBLIC_FEDERICA_IP.put("garr.mil.router1", "194.132.52.3");
		PUBLIC_FEDERICA_IP.put("garr.mil.vserver1", "194.132.52.182");
		PUBLIC_FEDERICA_IP.put("garr.mil.vserver2", "194.132.52.190");
		PUBLIC_FEDERICA_IP.put("psnc.poz.router1", "194.132.52.4");
		PUBLIC_FEDERICA_IP.put("psnc.poz.uas", "194.132.52.246");
		PUBLIC_FEDERICA_IP.put("psnc.poz.vserver1", "194.132.52.230");
		PUBLIC_FEDERICA_IP.put("psnc.poz.vserver2", "194.132.52.218");
		PUBLIC_FEDERICA_IP.put("psnc.poz.vserver3", "194.132.52.238");
	}


    

    /**return the public ip for the specific hostname, which 
     * can be either in federica or planetlab
     * @param hostname or URI
     * @return the public IP or null if it is not found
     */
    public static String getPublicIP(String hostname)
    {

    	
    	String ip = getIPfromMap(PUBLIC_IP, hostname, false);
    	if (ip != null)
    	{
    		log.info("The hostname is in planetlab and its public IP is {}", ip);
    		return ip;
    	}
    	
    	ip = getIPfromMap(PUBLIC_FEDERICA_IP, hostname, true);
    	if (ip != null)
    	{
    		log.info("The hostname is in federica and its public IP is {}", ip);
    	}
    	return ip;
    }
    
    
    
    /**return the public ip for the specific hostname in planetlab
     * @param hostname or URI
     * @return the public IP or null if it is not found
     */
    public static String getPublicPlanetLabIP(String hostname)
    {
    	return getIPfromMap(PUBLIC_IP, hostname, true);
    }

    /**return the public ip for the specific hostname in federica
     * @param hostname or URI
     * @return the public IP or null if it is not found
     */
    public static String getPublicFedericaIP(String hostname)
    {
    	return getIPfromMap(PUBLIC_FEDERICA_IP, hostname, true);
    }


	/**from the given map, find the public IP
	 * @param map the map contain the keywords and IPs
	 * @param hostname
	 * @param printWarns if true then it print warning if the IP was not found
	 * @return the IP or null if it is not found
	 */
	private static String getIPfromMap(Map<String, String> map, String hostname, boolean printWarns)
	{
		Set<String> keys = map.keySet();
		for (String s : keys)
		{
			if (hostname.contains(s))
			{
				String ip = map.get(s);
				log.debug("Matching the hostname {} to {}. The IP is : " + ip, hostname, s);
				return ip;
			}
		}
		if (printWarns)
			log.warn("I can not find the public IP for the hostname : {}", hostname);
		return null;
		
	}
    


}
