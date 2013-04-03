/**
 * 
 */
package eu.novi.resources.discovery.database;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.openrdf.OpenRDFException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.object.ObjectConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.core.CPU;
import eu.novi.im.core.Link;
import eu.novi.im.core.Memory;
import eu.novi.im.core.Node;
import eu.novi.im.core.NodeComponent;
import eu.novi.im.core.Storage;

/**
 * 
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
 * It construct a find resources query
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class ConstructFindResQuery {
	
	private enum Equations {
		EQUAL, CREATER_EQUAL, NOT_EQUAL
	}
	
	private enum XsdType {
		FLOAT, INTEGER
	}
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(ConstructFindResQuery.class);
	
	private String query;
	
	/**
	 * contain the variable in the select section
	 */
	private Vector<String> queryVar = new Vector<String>();
	/**
	 * the number of components that each variable has
	 */
	private Vector<Integer> varComponents = new Vector<Integer>();



	/**
	 * @param var the number of variables that the select will contain.
	 * Must be between 1 - 10. If not then will create 1 variable
	 * @param contexts the contexts to be set to the query. 
	 * MUST BE WITH OUT THE NOVI BASE ADDRESS
	 * 
	 */
	public ConstructFindResQuery(int var,  String... contexts)
	{
		setPrefix();
		setSelect(var, false, contexts);
		
	}
	
	/**
	 * @param var the number of variables that the select will contain.
	 * Must be between 1 - 10. If not then will create 1 variable
	 * @param contexts the contexts to be set to the query. 
	 * MUST BE WITH OUT THE NOVI BASE ADDRESS
	 * @param distinct set the DISTINCT modifier or not
	 * 
	 */
	public ConstructFindResQuery(int var, boolean distinct, String... contexts)
	{
		setPrefix();
		setSelect(var, distinct, contexts);
		
	}
	
	/**create an ASK query
	 * @param contexts the contexts to be set to the query. 
	 * CAN BE WITH OR WITH OUT THE NOVI BASE ADDRESS
	 */
	public ConstructFindResQuery(String... contexts)
	{
		setPrefix();
		setAsk(contexts);
		
	}
	
	
	/**set the restrictions, that the node should have all the availability values to zero.
	 * @param node
	 */
	protected void setCheck4OfflineNode(String nodeUri)
	{
		//cpu component
		String cpuVar = "?cpuVar";
		query += createLine("<" + nodeUri + ">", "im:hasComponent", cpuVar);
		query += createLine(cpuVar, "rdf:type", "im:CPU");
		//cpu speed
		//String cpuVarSpeed = cpuVar + "_speed";
		// += createLine(cpuVar, "im:hasCPUSpeed", cpuVarSpeed);
		//query += createNumFilter(XsdType.FLOAT, cpuVarSpeed, Equations.EQUAL, "0");
		//cpu cores
		String cpuVarCores = cpuVar + "_avalCores";
		query += createLine(cpuVar, "im:hasAvailableCores", cpuVarCores);
		query += createNumFilter(XsdType.INTEGER, cpuVarCores,
				Equations.EQUAL, "0");
		
		//memory component
		String memVar = "?memVar";
		query += createLine("<" + nodeUri + ">", "im:hasComponent", memVar);
		query += createLine(memVar, "rdf:type", "im:Memory");
		String avMemVar = memVar + "_avalMemSize";
		query += createLine(memVar, "im:hasAvailableMemorySize", avMemVar);
		query += createNumFilter(XsdType.FLOAT, avMemVar,
				Equations.EQUAL, "0");
		
		//storage component
		String stoVar = "?stoVar";
		query += createLine("<" + nodeUri + ">", "im:hasComponent", stoVar);
		query += createLine(stoVar, "rdf:type", "im:Storage");
		String avStoVar = stoVar + "_avalStoSize";
		query += createLine(stoVar, "im:hasAvailableStorageSize", avStoVar);
		query += createNumFilter(XsdType.FLOAT, avStoVar,
				Equations.EQUAL, "0");
		

		
		
	}



	/**print and return the query
	 *@param debug if you print in debug level then true else false
	 * @return the query string
	 */
	protected String printQuery(boolean debug)
	{
		String m = "\n/////////////////////////////\n"
	              + query +
	              "\n//////////////////////////////";
		if (debug)
		{
			log.debug(m);
		}
		else
		{
			log.info(m);
			
		}
		return query;
	}
	
	/**return the query string
	 * @return
	 */
	protected String getQuery()
	{
		return query;
	}


	/**execute the query to the repository.
	 * print the query and the results
	 * @return the number of results that were found,
	 * and -1 if an error occured
	 */
	protected int execQueryPrintResults()
	{
		Set<String> answ = execQueryPrintGetResults();
		if (answ == null)
			return -1;
		else 
			return answ.size();
	}
	
	
	
	/**execute the query to the repository.
	 * print the query and return back the results.
	 * If the query has more than one variable the it return back only the 
	 * results of the first variable
	 * @return the results that were found,
	 * or null if an error occured
	 */
	protected Set<String> execQueryPrintGetResults()
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		log.debug("Executing query...");
		printQuery(true);
		Set<String> answer = new HashSet<String>();
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			TupleQueryResult result = tupleQuery.evaluate();
			List<String> bindingNames = result.getBindingNames();
			try {
				log.debug("The results are :");
				while (result.hasNext()) {
			
					BindingSet bindingSet = result.next();
					for (int i = 0; i < bindingNames.size(); i++)
					{//print
						log.debug(bindingSet.getValue(bindingNames.get(i)) + ", ");
					}
					//add only the results for the first variable
					answer.add(bindingSet.getValue(bindingNames.get(0)).stringValue());
					log.debug("\n");
				}
				
			}
			finally {
				result.close();
			}
		}
		catch (OpenRDFException e) {
			log.error(e.getMessage());
			log.warn("Problem executing a query in construct find resource query");
			ConnectionClass.closeAConnection(con);
			return null;
		}
		ConnectionClass.closeAConnection(con);
		return answer;
	}
	
	
	
	/**execute the ask query and return the results.
	 * For offline nodes, it returns true if the node is offline
	 * @return if there is a problem, it return false
	 */
	protected boolean execAskQuery()
	{
		ObjectConnection con = ConnectionClass.getNewConnection();
		log.debug("Executing ASK query...");
		printQuery(true);
		boolean answer = false;
		try {
			BooleanQuery booleanQuery = con.prepareBooleanQuery(QueryLanguage.SPARQL, query);
			answer = booleanQuery.evaluate();
			
		}
		catch (OpenRDFException e) {
			log.error(e.getMessage());
			log.warn("Problem executing an ASK query in construct find resource query");
			//ConnectionClass.closeAConnection(con);
			//return false;
		}
		ConnectionClass.closeAConnection(con);
		return answer;
	}

	
	/**set the rdf type. 
	 * @param var the variable name you want to set the type. The first variable is 1
	 * @param type the name of the type, i.e. Node (not im:Node)
	 */
	protected void setRdfType(int var, String type)
	{
		if (!checkVarNumber(var))
		{
			log.warn("There is not variable with number {}. " +
					"I can't set the RDF Type", var);
			return;
		}
		query += createLine(queryVar.get(var - 1), "rdf:type", "im:" + type);
	}


	/**set bound constrain for the variable
	 * @param var the variable number
	 * @param boundNode the bound node URI or ID
	 */
	protected void setBoundConstrain(int var, String boundNode)
	{
		if (checkVarNumber(var))
		{
			//query += createVarFilter(queryVar.get(var -1 ), Equations.EQUAL, boundNode);
			query += createStringFilter(queryVar.get(var -1 ), Equations.EQUAL, boundNode);
		}
		else
			log.warn("The variable number {} is not valid", var);
	}


	/**it sets for a variable a node that you want to except from the match.
	 * i.e. when looking for new nodes for an existing slice
	 * @param var
	 * @param nodeUri - the URI or ID of the node
	 */
	protected void setExceptNode(int var, String nodeUri)
	{
		//String st = createVarFilter(queryVar.get(var-1), Equations.NOT_EQUAL, nodeUri);
		String st = createStringFilter(queryVar.get(var-1), Equations.NOT_EQUAL, nodeUri);
		if (st != null)
		{
			query += st;
		}
	}
	
	/**it sets for a variable a set of nodes that you want to be except from the match.
	 * @param var
	 * @param nodeURIs - the URIs or IDs of the nodes, if null it doesn't do anything
	 */
	protected void setExceptNodes(int var, Set<String> nodeURIs)
	{
		if (nodeURIs == null)
			return;
		for (String s : nodeURIs)
			setExceptNode(var, s);
	}


	/**set the functional characteristics for a Node, i.e. hardware type
	 * @param var the variable to set the characteristics
	 * @param node the node from which to read the characteristics
	 */
	protected void setFunctionalChar(int var, Node node)
	{
		String hwType = node.getHardwareType();
		if (hwType != null)
		{
			String hwTypeVar = queryVar.get(var-1) + "hwType";
			query += createLine(queryVar.get(var-1), "im:hardwareType", hwTypeVar);
			query += createStringRegexFilter(hwTypeVar, hwType, false);
		}
		
	}
	
	/**set the functional characteristic canFederate as true for 
	 * the interfaces of a Node. 
	 * Can be be consider as federade node if the inbound or 
	 * outbound interface has canFederate true
	 * @param var the node variable to set the canFederate
	 */
	protected void setCanFederadedAttrib(int var)
	{
		String inInterf = queryVar.get(var-1) + "inInterfaces";
		String outInterf = queryVar.get(var-1) + "outInterfaces";
		
		query += "{" + createLine(queryVar.get(var-1), "im:hasInboundInterface", inInterf);
		query += createLine(inInterf, "rdf:type", "im:Interface");
		query += createLine(inInterf, "im:canFederate", "true") + "}";
		
		query += "\nUNION\n";
		
		query += "{" + createLine(queryVar.get(var-1), "im:hasOutboundInterface", outInterf);
		query += createLine(outInterf, "rdf:type", "im:Interface");
		query += createLine(outInterf, "im:canFederate", "true") + "}";

	}
	
	
	/**set the functional characteristics for a Link, i.e. capacity
	 * @param var the variable to set the characteristics
	 * @param link the link from which to read the characteristics
	 */
	protected void setFunctionalCharLink(int var, Link link)
	{
		Float capacity = link.getHasCapacity();
		if (capacity != null)
		{
			String capacVar = queryVar.get(var-1) + "capacity";
			query += createLine(queryVar.get(var-1), "im:hasCapacity", capacVar);
			query += createNumFilter(XsdType.FLOAT, capacVar,
					Equations.CREATER_EQUAL, capacity.toString());
		}
	}


	/**add node component constrains, for a set of node components, to the query
	 * @param var the variable to be added the node components
	 * @param nodeComponents must be cpu, memory or storage
	 */
	protected void setNodeComponents(int var, Set<NodeComponent> nodeComponents)
	{
		if (nodeComponents == null)
		{
			return;
		}
		try {
			for (NodeComponent nComp : nodeComponents)
				setNodeComponent(var, nComp, false);
		}
		catch (ClassCastException e)
		{
			log.error("Problem in node component {} in the construct find resources query",
					nodeComponents.toString());
			ConnectionClass.logErrorStackToFile(e);
		}

	}


	/**add node component constrains to the query
	 * @param var the variable to be added the node component
	 * @param nodeComponent must be cpu, memory or storage
	 * @param normalMode if it is normal mode then every not empty value in the cpu input
	 * will be include in the query in the same field. If it is not normal then
	 * is means is findResources and then anly the available fields will be used.
	 * see each create component method for more info
	 */
	protected void setNodeComponent(int var, NodeComponent nodeComponent, boolean normalMode)
	{
		log.info("Creating the query for node component : " + nodeComponent.toString());
		if (!checkVarNumber(var))
		{
			log.warn("The variable number {}, in construct find resource query " +
					"is not valid", var);
			return;
		}

		// I will add all the statements to a vector and at the end I will add the new
		//statements to the query.
		Vector<String> statements; //the statements to be added
		if (nodeComponent instanceof CPU)
		{
			statements = createCpuComponent(var, (CPU) nodeComponent, normalMode);

		} //end of CPU
		else if (nodeComponent instanceof Memory) {
			
			statements = createMemoryComponent(var, (Memory) nodeComponent, normalMode);

		} //end of Memory
		else if (nodeComponent instanceof Storage) {

			statements = createStorageComponent(var, (Storage) nodeComponent, normalMode);
		} //end of Storage
		else
		{
			log.warn("The node component {} is not acceptable", nodeComponent.toString());
			return;
		}
		
		boolean nullStatement = false;
		for (String s : statements)
		{
			if (s == null)
				nullStatement = true;
			break;
			
		}
		
		if (nullStatement)
		{
			log.warn("There is a null statement. " +
					"I will not add the node component constrains to the query");
		}
		else
		{
			log.debug("I will add the node component statements to the query");
			for (String s : statements)
			{
				log.trace(s);
				query += s;
			}
		}
	}


	/**it creates a new component variable for the variable var.
	 * It also updates the varComponents list
	 * @param var the number of variable
	 * @return
	 */
	private String createNewVariableComp(int var)
	{
		//the current number of components that the variable have
		int i = varComponents.get(var-1);
		i++;
		//create the new variable component string
		String componVar = queryVar.get(var-1) + "_" + i;
		//update the variable number
		varComponents.set(var-1, i);
		return componVar;
		
	}


	/**it creates a new cpu components for the variable var
	 * @param var the variable in the queryVar list
	 * @param cpu
	 * @param normalMode if it is normal mode then every not empty value in the cpu input
	 * will be include in the query in the same field. If it is not normal then
	 * the cpu speed remain the same but the cpu cores number are used in the query in the
	 * available cpu cores field and the available cpu cores value is ignored
	 * @return all the statements in a list
	 */
	protected Vector<String> createCpuComponent(int var, CPU cpu, boolean normalMode )
	{
		Vector<String> statements = new Vector<String>();
		String componVar = createNewVariableComp(var);
		statements.add(createLine(queryVar.get(var-1), "im:hasComponent", componVar)); //1st statement

		log.debug("The node Component {}, is CPU", cpu.toString());
		log.debug("The mode is {}", normalMode ? "Normal" : "not Normal, " +
				"so this should be call from find resources");
		statements.add(createLine(componVar, "rdf:type", "im:CPU")); //2nt statement
		Float cpuSpeed = cpu.getHasCPUSpeed();
		BigInteger cpuCores = cpu.getHasCores();
		BigInteger cpuAvalCores = cpu.getHasAvailableCores();
		
		if (cpuSpeed != null)
		{
			String cpuVar = componVar + "_speed";
			statements.add(createLine(componVar, "im:hasCPUSpeed", cpuVar));
			statements.add(createNumFilter(XsdType.FLOAT, cpuVar, Equations.CREATER_EQUAL,
					cpuSpeed.toString()));
			
		}
		
		if (cpuCores != null)
		{
			if(normalMode)
			{
				String cpuVar = componVar + "_cores";
				statements.add(createLine(componVar, "im:hasCores", cpuVar));
				statements.add(createNumFilter(XsdType.INTEGER, cpuVar, Equations.EQUAL,
						cpuCores.toString()));
			}
			else
			{
				//if it is not normal mode, then the cpuCore value of the 
				//virtual machine it is used in the field of available cpu cores
				//when we query to find available physical machines
				String cpuVar = componVar + "_avalCores";
				statements.add(createLine(componVar, "im:hasAvailableCores", cpuVar));
				statements.add(createNumFilter(XsdType.INTEGER, cpuVar,
						Equations.CREATER_EQUAL, cpuCores.toString()));
				
				
			}
			
		}
		
		
		if (cpuAvalCores != null && normalMode)
		{
			//if it is not normal that mean find resources, then the 
			//virtual machine should not have value in the cpu available cores
			String cpuVar = componVar + "_avalCores";
			statements.add(createLine(componVar, "im:hasAvailableCores", cpuVar));
			statements.add(createNumFilter(XsdType.INTEGER, cpuVar,
					Equations.CREATER_EQUAL, cpuAvalCores.toString()));
/*			cpuAvalCores = new BigInteger("1"); //TODO create constant for that value
			log.info("Was not provided cpu available cores number. I will set that to : "
			+ cpuAvalCores);*/

		}


		return statements;
		
	}


	/**it creates a new memory components for the variable var
	 * @param var the variable in the queryVar list
	 * @param memory
	 * @param normalMode if it is normal mode then every not empty value in the memory input
	 * will be include in the query in the same field. If it is not normal then
	 * the memory size number are used in the query in the
	 * available memory size field and the available memory size value is ignored
	 * @return all the statements in a list
	 */
	protected Vector<String> createMemoryComponent(int var, Memory memory, boolean normalMode)
	{
		Vector<String> statements = new Vector<String>();
		String componVar = createNewVariableComp(var);
		statements.add(createLine(queryVar.get(var-1), "im:hasComponent", componVar)); //1st statement
		
		log.debug("The node Component {}, is Memory", memory.toString());
		log.debug("The mode is {}", normalMode ? "Normal" : "not Normal, " +
				"so this should be call from find resources");
		statements.add(createLine(componVar, "rdf:type", "im:Memory")); //2nt statement
		Float memSize = memory.getHasMemorySize();
		Float memAvalSize = memory.getHasAvailableMemorySize();
		
		Float availableMem = null;
		if (normalMode)
		{
			availableMem = memAvalSize;
			
		}
		else
		{
			availableMem = memSize;
		}
		
		if (availableMem != null)
		{ 

			String memVar = componVar + "_avalMemSize";
			statements.add(createLine(componVar, "im:hasAvailableMemorySize", memVar));
			statements.add(createNumFilter(XsdType.FLOAT, memVar,
					Equations.CREATER_EQUAL, availableMem.toString()));

			/*memAvalSize = new Float(10); //TODO create constant value
			log.info("Was not provided memory available size. I will set that to : "
			+ memAvalSize);*/

		}
		
		if (memSize != null && normalMode)
		{
			String memVar = componVar + "_memSize";
			statements.add(createLine(componVar, "im:hasMemorySize", memVar));
			statements.add(createNumFilter(XsdType.FLOAT, memVar,
					Equations.CREATER_EQUAL, memSize.toString()));
			
		}
		

		
		return statements;
		
	}


	/**it creates a new storage components for the variable var
	 * @param var the variable in the queryVar list
	 * @param storage
	 * @param normalMode if it is normal mode then every not empty value in the storage input
	 * will be include in the query in the same field. If it is not normal then
	 * the storage size number are used in the query in the
	 * available storage size field and the available storage size value is ignored
	 * @return all the statements in a list
	 */
	protected Vector<String> createStorageComponent(int var, Storage storage, boolean normalMode)
	{
		Vector<String> statements = new Vector<String>();
		String componVar = createNewVariableComp(var);
		statements.add(createLine(queryVar.get(var-1), "im:hasComponent", componVar)); //1st statement

		log.debug("The node Component {}, is Storage", storage.toString());
		log.debug("The mode is {}", normalMode ? "Normal" : "not Normal, " +
				"so this should be call from find resources");
		statements.add(createLine(componVar, "rdf:type", "im:Storage")); //2nt statement
		Float stoSize = storage.getHasStorageSize();
		Float stoAvalSize = storage.getHasAvailableStorageSize();

		
		Float availableSto = null;
		if (normalMode)
		{
			availableSto = stoAvalSize;
		}
		else
		{
			availableSto = stoSize;
		}

		
		if (availableSto != null)
		{ 
			String stoVar = componVar + "_avalStoSize";
			statements.add(createLine(componVar, "im:hasAvailableStorageSize", stoVar));
			statements.add(createNumFilter(XsdType.FLOAT, stoVar,
					Equations.CREATER_EQUAL, availableSto.toString()));
			/*stoAvalSize = new Float(100); 
			log.info("Was not provided storage available size. I will set that to : "
					+ stoAvalSize);*/

		}
		
		if (stoSize != null && normalMode)
		{
			String stoVar = componVar + "_stoSize";
			statements.add(createLine(componVar, "im:hasStorageSize", stoVar));
			statements.add(createNumFilter(XsdType.FLOAT, stoVar,
					Equations.CREATER_EQUAL, stoSize.toString()));

		}

		
	

		
		
		return statements;
		
	}


	/**finalize the query, set the final bracket
	 * 
	 */
	protected void finalizeQuery()
	{
		query += " }";
	}



	/**
	 * it sets the prefixs to the query string.
	 * This is the first function that must be called
	 */
	private void setPrefix()
	{
		query = "PREFIX im:<http://fp7-novi.eu/im.owl#> \n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
				"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> \n";
		
	}
	
	/**create and set the select part to query string
	 * @param varNumber -- the number of variables that the select will contain.
	 * Must be between 1 - 10. If not then will create 1 variable
	 * @param distinct the the DISTINCT modifier or not
	 */
	private void setSelect(int varNumber, boolean distinct,  String... contexts)
	{
		int variables;
		if (varNumber > 0 && varNumber <=10)
		{
			variables = varNumber;
		}
		else
		{
			log.warn("The number, {}, you gave in createSelect is ivalid. " +
					"Will be created only one variable", varNumber);
			variables = 1;
		}
		
		if (distinct)
			query += "SELECT DISTINCT ";
		else
			query += "SELECT ";
		
		for (int i = 1; i <= variables; i++)
		{
			String var = "?var" + i;
			query += var + " ";
			queryVar.add(var);
			varComponents.add(0); //the variable has 0 components at the moment
			
		}
		query += "\n";
		setFrom(contexts);
		query += "where { \n";
		
	}
	
	private void setAsk(String... contexts)
	{
		query += "ASK\n";
		setFrom(contexts);
		query += "WHERE { \n";
	}
	
	
	/**set the FROM statements
	 * @param contexts
	 */
	private void setFrom(String... contexts)
	{
		for (String c : contexts)
		{
			if (c.contains("http://") || c.contains("urn:"))
			{
				query += "FROM <" + c + ">\n";
			}
			else
			{
				query += "FROM im:" + c + "\n";
				
			}
	
		}
		
	}


	
	/**check if the variable name is valid.
	 * To be valid must be between 1 and the variable size in the select
	 * @param var the variable number
	 * @return true if it is valid
	 */
	private boolean checkVarNumber(int var)
	{
		if (var > 0 && var <= queryVar.size())
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	
	/**create and return a single line in the format of:
	 * " subj pred obj .\n"
	 * @param subj
	 * @param pred
	 * @param obj
	 * @return
	 */
	private String createLine(String subj, String pred, String obj)
	{
		return " " + subj + " " + pred + " " + obj + " .\n";
	}
	
	/**create string regex filter. so the variable match the pattern
	 * @param var the variable name with ? front
	 * @param value
	 * @param caseSensitive if true then match is case sensitive (default) otherwise not
	 * @return
	 */
	private String createStringRegexFilter(String var, String value, boolean caseSensitive)
	{
		if (caseSensitive)
		{
			return " FILTER regex(str(" + var + "), \"" + value + "\") .\n";
		}
		else
		{
			return " FILTER regex(str(" + var + "), \"" + value + "\", \"i\" ) .\n";
		}
		
	}
	
	/**given a variable it creates a string filter so that the variable 
	 * string format is equal or not to the given string value 
	 * @param var the variable in ? format
	 * @param equation equal or not equal
	 * @param value
	 * @return return the string line
	 */
	private String createStringFilter(String var, Equations equation, String value)
	{
		if (equation == null)
			throw new IllegalArgumentException("The Equation should not be null");
		String eq = getTheEquation(equation);
		return "FILTER (str(" + var + ") " + eq + " \"" + value +"\") .\n";
		
	}
	

	/**create a filter for the variable in the format:
	 * FILTER (var  eq  im:id) .\n
	 * @param var the variable in format ?var
	 * @param equation equal or not equal
	 * @param id the ID of the object with or with out the novi base address
	 * @return
	 */
	/*private String createVarFilter(String var, Equations equation, String id)
	{
		String eq = getTheEquation(equation);
		if (eq == null)
		{
			log.warn("create var filter: I can not create the filter.");
			return null;
			
		}
		String id_ = LocalDbCalls.getURNfromURI(id);
		return "FILTER ("+var+" " + eq + " im:"+id_+") .\n";
		
	}*/
	

	/**create a numeric filter in the format of: 
	 * FILTER (xsd:" + xsdtype + "(" + var + ") " + eq + " xsd:" + 
	 * xsdtype + "(" + value + ")) . \n
	 * @param type the type of the filter, integer or float
	 * @param var the variable
	 * @param equation =, or >=
	 * @param value 
	 * @return the filter line as a string or null if a problem occur
	 */
	private String createNumFilter(XsdType type, String var, Equations equation, String value)
	{
		String eq = getTheEquation(equation);
		if (eq == null)
		{
			log.warn("create num filter: I can not create the filter.");
			return null;
			
		}
		String xsdtype;
		if (type == XsdType.FLOAT)
			xsdtype = "float";
		else if (type == XsdType.INTEGER)
			xsdtype = "integer";
		else
		{
			log.warn("create filter: The xsd type {} is not acceptable.", type);
			return null;
		}

		return "FILTER (xsd:" + xsdtype + "(" + var + ") " + eq + " xsd:" + xsdtype + "(" + value + ")) . \n";
		
	}


	/**return the equation in a string format
	 * @param equation
	 * @return
	 */
	private String getTheEquation(Equations equation)
	{
		String eq;
		if (equation == Equations.EQUAL)
			eq = "=";
		else if (equation == Equations.CREATER_EQUAL)
			eq = ">=";
		else if (equation == Equations.NOT_EQUAL)
			eq = "!=";
		else
		{
			log.warn("construct find resource query: The equation {} is not acceptable.", equation);
			eq = null;
			
		}
		return eq;
	}
	
	public String toString()
	{
		return query;
	}

}
