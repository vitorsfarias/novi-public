package eu.novi.im.util;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.Group;
import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.LinkOrPath;
import eu.novi.im.core.Path;
import eu.novi.im.core.Resource;
import eu.novi.im.core.VirtualLink;


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
 * class that provide validation function for the NOVI IM java topologies
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class Validation {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(Validation.class);
	
	private String linkCheck = "";
	
	
	
	/**it checks all the links in the topology for hasSource, hasSink
	 * @param top a Group containing links
	 * @return an empty string if everything is OK, or the error message
	 */
	public String checkLinksForSinkSource(Group top) throws IllegalArgumentException
	{
		linkCheck = "";
		
		if (top == null)
		{
			log.warn("checkLinkForSinkSource: The topology is null");
			return "null topology";
		}
			
		log.info("Checking the link hasSource, hasSink for the topology {}", top.toString());
		boolean foundLinks = false;
		
		for (Resource res: top.getContains())
		{
			if (res instanceof VirtualLink)
			{
				foundLinks = true;
				log.debug("The resource {}, is a virtual link ", res.toString());
				VirtualLink vLink = (VirtualLink) res;
				checkALink(vLink);
				
				Set<LinkOrPath> paths = vLink.getProvisionedBy();
				if (paths == null)
				{
					log.warn("The link {} doesn't have provisionedBy paths",
							vLink.toString());
				}
				else
				{
					log.debug("The link {} has provisionedBy {}", vLink.toString(),
							paths.toString());
					
					for (LinkOrPath path :paths)
					{
						if (path instanceof Path)
						{
							
							Set<Resource> resLinks = ((Path) path).getContains();
							if (resLinks == null)
							{
								log.warn("The path {} doesn't have contains", path.toString());
							}
							else
							{
								log.debug("The path {}, contains {}",
										path.toString(), resLinks.toString());
								for (Resource resL: resLinks)
								{
									if (resL instanceof Link) {
										log.debug("The resource {} is a link", resL.toString());
										checkALink((Link)resL);
										
									}
									else if (resL instanceof Interface)
									{
										log.debug("The resource {} is an interface", resL.toString());
										
										
									}
									else
									{
										log.warn("The resource {} is not a link or interface", resL.toString());
									}
									
								}
								
							}
							
							
						} else
						{//the object is not path
							log.warn("The object {} is not a path", path.toString());
							
						}
						
						
					}
				}
				
			}
			
		}
		
		if (!foundLinks)
		{
			log.warn("Was not found any links in the topology {}", top.toString());
		}
		
		return linkCheck;
		
	}
	
	
	
	/**check and print the hasSink and hasSource of the link
	 * @param link
	 */
	private void checkALink(Link link)
	{
		//check hasSink
		Set<Interface> interfLink = link.getHasSink();
		if (interfLink == null)
		{
			log.warn("The link {} doesn't have hasSink", link.toString());
			linkCheck += "The link " + link.toString() + " doesn't have hasSink\n";
		}
		else
		{
			log.debug("The link {} hasSink : {}", link.toString(), interfLink.toString());
			
		}
		
		//check hasSource
		interfLink = link.getHasSource();
		if (interfLink == null)
		{
			log.warn("The link {} doesn't have hasSource", link.toString());
			linkCheck += "The link " + link.toString() + " doesn't have hasSource\n";
		}
		else
		{
			log.debug("The link {} hasSource : {}", link.toString(), interfLink.toString());
			
		}
	}
	

}
