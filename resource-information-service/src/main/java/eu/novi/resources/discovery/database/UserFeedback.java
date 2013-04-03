package eu.novi.resources.discovery.database;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import eu.novi.im.core.CPU;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;
import eu.novi.im.core.VirtualNode;

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
 * It implements the user feedback response, in the case that the find resources doesn't find 
 * any available resources
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class UserFeedback {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(UserFeedback.class);
	
	//the maximum available metrics
	private int maxCpuCores = 0;
	private float maxCpuSpeed = 0;
	private float maxFreeMem = 0;
	private float maxFreeDiscSpace = 0;
	
	//the requirement from the user
	private int askCpuCores = 0;
	private float askCpuSpeed = 0;
	private float askFreeMem = 0;
	private float askFreeDiscSpace = 0;
	
	
	//the current value from a specific physical machine
	private int curCpuCores = 0;
	private float curCpuSpeed = 0;
	private float curFreeMem = 0;
	private float curFreeDiscSpace = 0;
	
	//lists with the values, that if they change the request can succeed
	private List<Integer> cpuCoresList = null;
	private List<Float> cpuSpeedList = null;
	private List<Float> freeMemList = null;
	private List<Float> freeDiscSpaceList = null;

	
	
	private List<MachineChar> machinesChar;
	
	
	
	/**create the feedback message. For the given available machines and the user 
	 * request, it creates the user feedback
	 * @param physicalMachines the available physical machine in the DB for this request
	 * @param virtualMachineName the user request
	 * @return the feedback messages
	 */
	public String createFeedback(Set<String> physicalMachines, VirtualNode virtualNode)
	{
		String feedback = "///////////USER FEEDBACK ON THE CURRENT AVAILABILITY OF: " +
				"" + virtualNode.toString() + "///////////////\n";
		log.info("I will find the availability of the following machines: ", physicalMachines.toString());
		
		//initialize the variables
		if (!initialize(virtualNode))
		{
			log.warn("I can not create the feedback");
			return feedback;
			
		}
		
		for (String st : physicalMachines)
		{
			curCpuCores = 0;
			curCpuSpeed = 0;
			curFreeMem = 0;
			curFreeDiscSpace = 0;
			
			
			Node nod = (Node) LocalDbCalls.getLocalResource(st);
			if (nod == null)
			{
				log.warn("I can not find the physical node {} in the DB", st);
			}
			else
			{

				Set<NodeComponent> components = nod.getHasComponent();
				if (components == null || components.isEmpty())
				{
					log.warn("I can not find any node components for the node {}", st);
				} else
				{
					for (NodeComponent comp : components)
					{
						if (comp instanceof Memory)
						{
							Float m = ((Memory)comp).getHasAvailableMemorySize();
							if (m == null)
							{
								log.warn("The machine {} doesn't have available memory value", nod.toString());
								//the curFreeMem stays 0
							}
							else
							{
								curFreeMem = m;
								if (m > maxFreeMem)
								{
									log.debug("The available memory {} is the biggest so far," +
											" I will replace the value {}", m, maxFreeMem);
									maxFreeMem = m;
								}
							}
							

						}
						else if (comp instanceof Storage)
						{
							Float s = ((Storage)comp).getHasAvailableStorageSize();
							if (s == null)
							{
								log.warn("The machine {} doesn't have available storage value", nod.toString());
							}
							else
							{
								curFreeDiscSpace = s;
								if (s > maxFreeDiscSpace)
								{
									log.debug("The available disc space {} is the biggest so far," +
											" I will replace the value {}", s, maxFreeDiscSpace);
									maxFreeDiscSpace = s;
								}
								
							}
							

						}
						else if (comp instanceof CPU)
						{
							BigInteger cC = ((CPU)comp).getHasAvailableCores();
							if (cC == null)
							{
								log.warn("The machine {} doesn't have available CPU core number", nod.toString());
							}
							else
							{
								curCpuCores = cC.intValue();
								if (cC.intValue() > maxCpuCores)
								{
									log.debug("The CPU cores number {} is the biggest so far," +
											" I will replace the value {}", cC, maxCpuCores);
									maxCpuCores = cC.intValue();
								}
								
							}
							

							Float cS = ((CPU)comp).getHasCPUSpeed();
							if (cS == null)
							{
								log.warn("The machine {} doesn't have available CPU speed value", nod.toString());
								
							} else
							{
								curCpuSpeed = cS;
								if (cS > maxCpuSpeed)
								{
									log.debug("The CPU speed {} is the biggest so far," +
											" I will replace the value {}", cS, maxCpuSpeed);
									maxCpuSpeed = cS;
								}
								
							}

						}
						else
							log.warn("The node component {} is unknown", comp.toString());
					}
				}
			}
			//update the lists of one value change
			updateLists();
		}// end of for
		
		feedback += getUserRequestString();
		feedback += "From all the available machines the maximum values are:\n";
		feedback += "Maximum CPU cores number: " + maxCpuCores + "\n";
		feedback += "Maximum CPU speed: " + maxCpuSpeed + " GHz\n";
		feedback += "Maximum free memory: " + maxFreeMem + " GB\n";
		feedback += "Maximum free disc space: " + maxFreeDiscSpace + " GB\n";
		feedback += getOneValueChangeSuggestions();
		feedback += getClosestMachine();

		return feedback;
	}
	
	
	private String getUserRequestString()
	{
		return "This request is:\n" +
				"CPU Cores: " + askCpuCores +
				"\nCPU Speed: " + askCpuSpeed + " GHz\n" +
				"Free memory: " + askFreeMem + " GB\n" +
				"Free disk space: " + askFreeDiscSpace + " GB\n";
	}
	
	/**using euclidia distance, it gives the closest to the user request phisical machine.
	 * before to calculate the distance, it normalize the vlaues in the machineChar list
	 * @return
	 */
	private String getClosestMachine()
	{
		/*//normalize values
		for (MachineChar m : machinesChar)
		{
			m.setCpuCoresNorm(m.getCpuCores()/(float)maxCpuCores);
			m.setCpuSpeedNorm(m.getCpuSpeed()/maxCpuSpeed);
			m.setFreeMemNorm(m.getFreeMem()/maxFreeMem);
			m.setFreeDiscSpaceNorm(m.getFreeDiscSpace()/maxFreeDiscSpace);
		}*/
		
		//find closest
		double dist = Double.MAX_VALUE;
		MachineChar closest = null;
		for (MachineChar m : machinesChar)
		{
			double curDist = m.distance(askCpuCores, askCpuSpeed, askFreeMem, askFreeDiscSpace);
			if (curDist < dist)
			{
				log.debug("The current distance {} of the machine is smaller than {}", curDist, dist);
				dist = curDist;
				closest = m;
				
			}
		}
		
		if (closest == null)
		{
			return "Was not found any close machine";
		}
		
		return "The closest to your request, available machine is:\n" +
				"CPU core: " + closest.getCpuCores() +
				"\nCPU speed: " + closest.getCpuSpeed() +
				"\nFree Memory: " + closest.getFreeMem() +
				"\nFree disc Space: " + closest.getFreeDiscSpace() + "\n";
	}
	
	/**get the suggestions to change one value, in order the request to be successful 
	 * @return
	 */
	private String getOneValueChangeSuggestions()
	{
		String sugg = "ONE VALUE CHANGE SUGGESTIONS (You can choose at least one)\n";
		if (!freeMemList.isEmpty())
		{
			Collections.sort(freeMemList);
			sugg += "Free memory value: change to: " + freeMemList.get(freeMemList.size()-1) + 
					" in order to meet the requirement for at least one machine, change to: " + freeMemList.get(0) +
					" in order to get the maximum available machines, changing only one value\n";
		}
		
		if (!freeDiscSpaceList.isEmpty())
		{
			Collections.sort(freeDiscSpaceList);
			sugg += "Free disc space value: change to: " + freeDiscSpaceList.get(freeDiscSpaceList.size()-1) + 
					" in order to meet the requirement for at least one machine, change to: "
					+ freeDiscSpaceList.get(0) + 
					" in order to get the maximum available machines, changing only one value\n";
		}
		
		if (!cpuCoresList.isEmpty())
		{
			Collections.sort(cpuCoresList);
			sugg += "CPU cores number: change to: " + cpuCoresList.get(cpuCoresList.size()-1) + 
					" in order to meet the requirement for at least one machine, change to: "
					+ cpuCoresList.get(0) + 
					" in order to get the maximum available machines, changing only one value\n";
		}
		
		if (!cpuSpeedList.isEmpty())
		{
			Collections.sort(cpuSpeedList);
			sugg += "CPU speed value: change to: " + cpuSpeedList.get(cpuSpeedList.size()-1) + 
					" in order to meet the requirement for at least one machine, change to: "
					+ cpuSpeedList.get(0) +
					" in order to get the maximum available machines, changing only one value\n";
		}
		
		return sugg;
	}
	
	
	/**
	 * update the lists with the one value change
	 */
	private void updateLists()
	{
		int change = 0;
		int index = 0;
		MachineChar machine = new MachineChar(curCpuCores, curCpuSpeed, curFreeMem, curFreeDiscSpace);
		machinesChar.add(machine);
		
		if (askCpuCores > curCpuCores)
		{
			log.debug("asked cpu cores : {}, current {}", askCpuCores, curCpuCores);
			change++;
			index = 1;
		}
		
		if (askCpuSpeed > curCpuSpeed)
		{
			log.debug("Asked cpu speed {}, current {}", askCpuSpeed, curCpuSpeed);
			change++;
			index = 2;
		}
		
		if (askFreeMem > curFreeMem)
		{
			log.debug("Asked free memory {}, current {}", askFreeMem, curFreeMem);
			change++;
			index = 3;
		}
		
		if (askFreeDiscSpace > curFreeDiscSpace)
		{
			log.debug("Asked free storage {}, current {}", askFreeDiscSpace, curFreeDiscSpace);
			change++;
			index = 4;
		}
		
		
		//consider only the case that you need only to change one value
		if (change == 1)
		{
			if (index == 1)
			{
				log.debug("I found the one value cpu core {}", curCpuCores);
				cpuCoresList.add(curCpuCores);
			}
			else if (index == 2)
			{
				log.debug("I found the one value cpu speed {}", curCpuSpeed);
				cpuSpeedList.add(curCpuSpeed);
			}
			else if (index == 3)
			{
				log.debug("I found the one value free memory {}", curFreeMem);
				freeMemList.add(curFreeMem);
			}
			else if (index == 4)
			{
				log.debug("I found the one value free storage {}", curFreeDiscSpace);
				freeDiscSpaceList.add(curFreeDiscSpace);
			}
		}
		
	}
	
	
	
	/**set the variables
	 * @param virtualNode
	 * @return true if the variables are initialized successfully
	 */
	private boolean initialize(VirtualNode virtualNode)
	{
		maxCpuCores = 0;
		maxCpuSpeed = 0;
		maxFreeMem = 0;
		maxFreeDiscSpace = 0;
		
		cpuCoresList = new ArrayList<Integer>();
		cpuSpeedList = new ArrayList<Float>();
		freeMemList = new ArrayList<Float>();
		freeDiscSpaceList = new ArrayList<Float>();
		
		machinesChar = new ArrayList<MachineChar>();

		
		Set<NodeComponent> components = virtualNode.getHasComponent();
		if (components == null || components.isEmpty())
		{
			log.warn("I can not find any node components for the virtual node {}", virtualNode.toString());
			return false;
		} else
		{
			for (NodeComponent comp : components)
			{
				if (comp instanceof Memory)
				{
					Float m = ((Memory)comp).getHasMemorySize();
					if (m != null)
					{
						log.debug("The asked memory size: {}", m);
						askFreeMem = m;
					}
					else
					{
						log.debug("The asked memory size: 0");
						askFreeMem = 0;
						
					}

				}
				else if (comp instanceof Storage)
				{
					Float s = ((Storage)comp).getHasStorageSize();
					if (s != null)
					{
						log.debug("The asked disc space: {}", s);
						askFreeDiscSpace = s;
					}
					else
					{
						log.debug("The asked disc space: 0");
						askFreeDiscSpace = 0;
						
					}

				}
				else if (comp instanceof CPU)
				{
					BigInteger cC = ((CPU)comp).getHasCores();
					if (cC != null)
					{
						log.debug("The asked CPU cores number: {} ", cC);
						askCpuCores = cC.intValue();
					}
					else
					{
						log.debug("The asked CPU cores number: 0");
						askCpuCores = 0;
						
					}

					Float cS = ((CPU)comp).getHasCPUSpeed();
					if (cS != null)
					{
						log.debug("The asked CPU speed: {}", cS);
						askCpuSpeed = cS;
					}
					else
					{
						log.debug("The asked CPU speed: 0");
						askCpuSpeed = 0;
					}



				}
				else
					log.warn("The node component {} is unknown", comp.toString());
			}
		}
		
		return true;
		
	}
	
	
	
	private class MachineChar
	{
		
		private int cpuCores = 0;
		private float cpuSpeed = 0;
		private float freeMem = 0;
		private float freeDiscSpace = 0;
		
		
		

		/**
		 * @param cpuCores
		 * @param cpuSpeed
		 * @param freeMem
		 * @param freeDiscSpace
		 */
		private MachineChar(int cpuCores, float cpuSpeed, float freeMem,
				float freeDiscSpace) {
			super();
			this.cpuCores = cpuCores;
			this.cpuSpeed = cpuSpeed;
			this.freeMem = freeMem;
			this.freeDiscSpace = freeDiscSpace;
		}
		
		/**calculate the euclidia distance, the values are normalized using the current max values
		 * @param cpuCores
		 * @param cpuSpead
		 * @param freeMem
		 * @param freeDisk
		 * @return the distance as a float
		 */
		double distance(int cpuCores, float cpuSpead, float freeMem, float freeDisk)
		{
			
			return  Math.sqrt(
					Math.pow((cpuCores/(float)maxCpuCores) - (this.cpuCores/(float)maxCpuCores), 2) +
					Math.pow((cpuSpead/maxCpuSpeed) - (this.cpuSpeed/maxCpuSpeed), 2) + 
					Math.pow((freeMem/maxFreeMem) - (this.freeMem/maxFreeMem), 2) + 
					Math.pow((freeDisk/maxFreeDiscSpace) - (this.freeDiscSpace/maxFreeDiscSpace), 2));
			
			
		}
		
		public int getCpuCores() {
			return cpuCores;
		}

		public float getCpuSpeed() {
			return cpuSpeed;
		}

		public float getFreeMem() {
			return freeMem;
		}

		public float getFreeDiscSpace() {
			return freeDiscSpace;
		}

		
	}

}
