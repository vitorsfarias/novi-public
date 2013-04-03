package eu.novi.resources.discovery.util;


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
 * It contains the testbeds of the federation
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class Testbeds {
	
    public static final String PLANETLAB = "PlanetLab";
    public static final String FEDERICA = "FEDERICA";
    
    private static String testbed; //the current testbed, get from blueprint
    
    private static Boolean isSharedTestbed = null;
    
  


	private static final transient Logger log = 
			LoggerFactory.getLogger(Testbeds.class);

    
    /**if the testbed is federica it returns true, otherwise false
     * @param testbedName
     * @return
     */
    public static boolean isFederica(String testbedName)
    {
    	if (FEDERICA.equals(testbedName))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
    
    
    /**if the testbed is shared like planetlab
     * (the virtual machines share the resources of the physical machines)
     * then it return true, or if it like federica it returns false
     * @return
     */
    public static boolean isSharedTestbed()
    {
    	if (isSharedTestbed != null)
    		return isSharedTestbed;
    	
    	if (PLANETLAB.equals(testbed))
    	{
    		log.info("The current testbed: {} is a shared testbed");
    		return true;
    		
    	}
    	else 
    	{
    		log.info("The current testbed: {} is not a shared testbed");
    		return false;
    		
    	}
    }
    
    
    
    /**it check if this physical machine belongs to the current testbed
     * @param uri URI of a physical machine
     * @return true if the physical machine belongs the current testbed
     */
    public static boolean isThisTestbedUri(String uri)
    {
    	String planetlab = "novipl:novi";
    	String federica = "federica";
    	
    	if (uri == null)
    	{
    		log.warn("Null argument in the: isThisTestbedUri");
    		return false;
    	}
    	
    	if (PLANETLAB.equals(testbed))
    	{
    		if (uri.contains(planetlab))
    		{
    			return true;
    		}
    	}
    	else if (FEDERICA.equals(testbed))
    	{
    		if (uri.contains(federica))
    		{
    			return true;
    		}
 
    	}
    	return false;
    	
    }
    
    public static String takeCurrentTestbed()
    {
    	return testbed;
    }


	public String getTestbed() {
		return testbed;
	}


	public void setTestbed(String testbed) {
		Testbeds.testbed = testbed;
	}

	public static Boolean getIsSharedTestbed() {
		return isSharedTestbed;
	}


	public static void setIsSharedTestbed(Boolean isSharedTestbed) {
		Testbeds.isSharedTestbed = isSharedTestbed;
	}

}
