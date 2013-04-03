package eu.novi.mapping.embedding.federica;

import java.lang.reflect.Field;
import java.util.*;

import org.gnu.glpk.GLPK;
import org.gnu.glpk.GLPKConstants;
import org.gnu.glpk.SWIGTYPE_p_double;
import org.gnu.glpk.SWIGTYPE_p_int;
import org.gnu.glpk.glp_cpxcp;
import org.gnu.glpk.glp_iocp;
import org.gnu.glpk.glp_prob;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.novi.feedback.event.ReportEvent;
import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.Path;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.PathImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.mapping.embedding.federica.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;



public final class MCFLinkMapping { 
	/** Main interface to report user feedback, initialized in blueprint based on service provided by novi-api. */
	private ReportEvent userFeedback;

	/** The session id. Temporary to keep the value obtained from NOVI-API,
	  * to be used when reporting user feedback.*/
	private String sessionID;
	
	/** Local logging. */
    private static final transient Logger LOG = LoggerFactory.getLogger(MCFLinkMapping.class);
    
	private String testbedName;
	private AugSubstrate augSub;
	private Map<ResourceImpl, ResourceImpl>  nodeMapping;
	private LogService logService;

	public MCFLinkMapping(AugSubstrate augSub, Map<ResourceImpl, ResourceImpl>  nodeMapping, 
			ReportEvent userFeedback, LogService logService,String sessionID, String TestbedName) {
	
	this.augSub=augSub;
	this.userFeedback= userFeedback;
	this.sessionID=sessionID;
	this.testbedName = TestbedName;	
	this.nodeMapping=nodeMapping;
	this.logService = logService;
	}

										
	public  Map<ResourceImpl, ResourceImpl> mcflm() {									
		 Map<ResourceImpl, ResourceImpl> linkMapping = null ;			

		setJNI(":/usr/local/lib:/usr/local/lib/jni:/usr/lib:/usr/lib/jni");
		
		int reqLinkCount = augSub.getReqLinkList().size();
		int subNodeCount = augSub.getSubNodelist().size();
		int[] o = augSub.getSourceDest().get(0);
		int[] d = augSub.getSourceDest().get(1);	
		double[][][] fi = new double[reqLinkCount][subNodeCount][subNodeCount];
		float[][] reqCapTable = EmbeddingOperations.getCapTable(augSub.getReq());
		
		try {
			LOG.debug("NodeMapping is not null and there are links to embed");
			String name = null;
			SWIGTYPE_p_int ind;
		    SWIGTYPE_p_double val;
			glp_prob prob1 = GLPK.glp_create_prob();
			GLPK.glp_set_prob_name(prob1, "rVine Intern");
			GLPK._glp_lpx_set_real_parm(prob1, GLPK.LPX_K_TMLIM, EmbeddingConstants.CPLEX_TIMEOUT);
			GLPK._glp_lpx_set_real_parm(prob1, GLPK.LPX_K_OBJUL, Double.MAX_VALUE);
			GLPK._glp_lpx_set_real_parm(prob1, GLPK.LPX_K_OBJLL, -1*Double.MAX_VALUE);
					
			int[][][] fMcf = new int[reqLinkCount][][];
			
			for(int k=0; k<reqLinkCount; k++) {
				fMcf[k] = new int[subNodeCount][];
				for (int u=0; u<subNodeCount; u++){
					fMcf[k][u] = new int[subNodeCount];
					for (int v=0; v<subNodeCount; v++){
						fMcf[k][u][v] = GLPK.glp_add_cols(prob1, 1);
						name = "fMcf[" + k + "," + u + "," + v + "]";
						GLPK.glp_set_col_name(prob1, fMcf[k][u][v], name);
						GLPK.glp_set_col_kind(prob1, fMcf[k][u][v], GLPKConstants.GLP_CV);
						GLPK.glp_set_col_bnds(prob1, fMcf[k][u][v], GLPKConstants.GLP_DB, 0, Double.MAX_VALUE);
					}
				}
			}
					
					for (int i=0; i<subNodeCount; i++) { 
						for (int j=0; j<subNodeCount; j++) {
							for (int k =0; k<reqLinkCount; k++) {
								LOG.debug("coef fMcf["+i+"]["+j+"]: "+augSub.getAugCapTable()[i][j]+" -> "+(1.0/(augSub.getAugCapTable()[i][j]+Double.MIN_VALUE)));
								GLPK.glp_set_obj_coef(prob1, fMcf[k][i][j], 1.0/(augSub.getAugCapTable()[i][j]+Double.MIN_VALUE));
							}
						}
					}
	
					int[] o2=new int[reqLinkCount];
					int[] d2=new int[reqLinkCount];

					LOG.info("Node mapping: check out Virtual-Pysical Mapping");
					
					for (int i=0; i<o.length;i++) {
						LOG.debug("info :" + o[i] + " " + augSub.getReqNodeList().get(o[i]-subNodeCount).toString());
						if (nodeMapping.containsKey((ResourceImpl)augSub.getReqNodeList().get(o[i]-subNodeCount))) {
							o2[i] = augSub.getSubNodelist().indexOf(nodeMapping.get(augSub.getReqNodeList().get(o[i]-subNodeCount)));
							LOG.debug("Origin id: " + o2[i]);
						}
					}
					for (int i=0; i<d.length;i++) {
						if (nodeMapping.containsKey(augSub.getReqNodeList().get(d[i]-subNodeCount))){
							d2[i] = augSub.getSubNodelist().indexOf(nodeMapping.get(augSub.getReqNodeList().get(d[i]-subNodeCount)));
							LOG.debug("Destination id:  "+ d2[i] );
						}
					}
	
					//flow reservation1
					for(int k=0;k<reqLinkCount; k++) {
						for(int i=0;i<subNodeCount; i++) {
							if(o2[k]!=i && d2[k]!=i) {
								int constraint = GLPK.glp_add_rows(prob1, 1);
								GLPK.glp_set_row_name(prob1, constraint, "r_(f1)_"+constraint);
								GLPK.glp_set_row_bnds(prob1, constraint, GLPKConstants.GLP_FX, 0, 0);
								ind = GLPK.new_intArray(subNodeCount*2+1);
								val = GLPK.new_doubleArray(subNodeCount*2+1);
								int len = 1;
								for(int j=0; j<subNodeCount; j++) {
									if (i!=j){
										GLPK.intArray_setitem(ind, len, fMcf[k][i][j]);
										GLPK.intArray_setitem(ind, len+1, fMcf[k][j][i]);
										GLPK.doubleArray_setitem(val, len, 1);
										GLPK.doubleArray_setitem(val, len+1, -1);
										len += 2;
									}
								}
								GLPK.glp_set_mat_row(prob1, constraint, len-1, ind, val);
							}
						}
					}

					//flow reservation2
					for(int k=0;k<reqLinkCount; k++) {
						int constraint = GLPK.glp_add_rows(prob1, 1);
						GLPK.glp_set_row_name(prob1, constraint, "r_(f2)_"+constraint);
						GLPK.glp_set_row_bnds(prob1, constraint, GLPKConstants.GLP_FX, reqCapTable[o[k]-subNodeCount][d[k]-subNodeCount], reqCapTable[o[k]-subNodeCount][d[k]-subNodeCount]);
						ind = GLPK.new_intArray(subNodeCount*2+1);
						val = GLPK.new_doubleArray(subNodeCount*2+1);
						
						int idx = 1;
						for(int i=0;i<subNodeCount;i++) {
							if(o2[k] == i) {
								continue;
							}
							GLPK.intArray_setitem(ind, idx, fMcf[k][o2[k]][i]);
							GLPK.intArray_setitem(ind, idx+1, fMcf[k][i][o2[k]]);
							GLPK.doubleArray_setitem(val, idx, 1);
							GLPK.doubleArray_setitem(val, idx+1, -1);
							idx += 2;
						}
						GLPK.glp_set_mat_row(prob1, constraint, idx-1, ind, val);
					}
					
					//flow reservation3
					for (int k=0;k<reqLinkCount; k++){
						int constraint = GLPK.glp_add_rows(prob1, 1);
						GLPK.glp_set_row_name(prob1, constraint, "r_(f3)_"+constraint);
						GLPK.glp_set_row_bnds(prob1, constraint, GLPKConstants.GLP_FX, -reqCapTable[o[k]-subNodeCount][d[k]-subNodeCount], -reqCapTable[o[k]-subNodeCount][d[k]-subNodeCount]);
						ind = GLPK.new_intArray(subNodeCount*2+1);
						val = GLPK.new_doubleArray(subNodeCount*2+1);
						int idx = 0;
						for (int i=0;i<subNodeCount;i++){
							if(d2[k] == i) {
								continue;
							}
							GLPK.intArray_setitem(ind, idx, fMcf[k][d2[k]][i]);
							GLPK.intArray_setitem(ind, idx+1, fMcf[k][i][d2[k]]);
							GLPK.doubleArray_setitem(val, idx, 1);
							GLPK.doubleArray_setitem(val, idx+1, -1);
							idx += 2;
						}
						GLPK.glp_set_mat_row(prob1, constraint, idx-1, ind, val);
					}
			
					//Link constraint
					for (int i=0;i<subNodeCount; i++){
						for (int j=0;j<subNodeCount; j++){
							if (i == j) {
								continue;
							}
							double capacity = augSub.getAugCapTable()[i][j];
							int constraint = GLPK.glp_add_rows(prob1, 1);
							GLPK.glp_set_row_name(prob1, constraint, "r_(lc)_"+constraint);
							GLPK.glp_set_row_bnds(prob1, constraint, GLPKConstants.GLP_UP, 0, capacity);
							ind = GLPK.new_intArray(2*reqLinkCount+1);
							val = GLPK.new_doubleArray(2*reqLinkCount+1);
							int idx = 0;
							
							for (int k=0; k<reqLinkCount; k++) {
								GLPK.intArray_setitem(ind, idx, fMcf[k][i][j]);
							    GLPK.intArray_setitem(ind, idx+1, fMcf[k][j][i]);
							    GLPK.doubleArray_setitem(val, idx, 1);
							    GLPK.doubleArray_setitem(val, idx+1, 1);
							    idx += 2;
							}
							
							GLPK.glp_set_mat_row(prob1, constraint, idx-1, ind, val);
						}		
					}
		
					GLPK.glp_write_lp(prob1, (new glp_cpxcp()), "rvine.intern.problem.txt");
					GLPK.glp_set_obj_name(prob1, "rVine Intern Problem obj");
					GLPK.glp_set_obj_dir(prob1, GLPKConstants.GLP_MIN);
					
					glp_iocp iocp1 = new glp_iocp();
					GLPK.glp_init_iocp(iocp1);
					iocp1.setPresolve(GLPKConstants.GLP_ON);
					int ret1 = GLPK.glp_intopt(prob1, iocp1);
					
					// Solve the Model
					if (ret1 == 0) {
						for(int k=0;k<reqLinkCount;k++) {
							for(int i=0;i<subNodeCount;i++) {
								for(int j=0;j<subNodeCount;j++) {
									if(fMcf[k][i][j] != 0) {
										fi[k][i][j] = GLPK.glp_mip_col_val(prob1, fMcf[k][i][j]);
										LOG.debug("fi["+k+"]["+i+"]["+j+"]: "+fi[k][i][j]);
									} else {
										fi[k][i][j] = 0;
									}
								}
							}
						}
						/** Link mapping is not working properly. I think flows table is not correct,
						 * because there isn't present the paths between the nodes assigned
						 * in the node mapping phase.
						 */
						linkMapping = buildLinkMapping(augSub, fi, nodeMapping);
						LOG.debug("linkMapping: "+linkMapping);
					}
					
					GLPK.glp_delete_prob(prob1);
		} catch (Exception e) {
			EmbeddingOperations.printErrorFeedback(sessionID, logService, userFeedback,testbedName,
					"MCF Concert exception caught: " + e.getMessage(), 
					"MCF Error: Algorithm exception caught. Try to run the request again");	
		}
		
		return linkMapping;
	}								
			
	/**
	 * Creates the mapping of the links in the flows table
	 * 
	 * flows parameter is not correct.
	 * 
	 * @param req
	 * @param sub
	 * @param reqNodeList
	 * @param subNodeList
	 * @param reqLinkList
	 * @param flows
	 * @param nodeMapping
	 * @return the link mapping
	 */
	public Map<ResourceImpl, ResourceImpl> buildLinkMapping(AugSubstrate augSub, double[][][] flows, Map<ResourceImpl, ResourceImpl> nodeMapping){
		
		if (augSub.getReqLinkList().size()!= flows.length) {return null;}
		
		Map<ResourceImpl, ResourceImpl> linkMapping = new LinkedHashMap<ResourceImpl, ResourceImpl>();
		
		for (Link reqLink: augSub.getReqLinkList()) {	
			int i = augSub.getFakeLinkIDs().get(reqLink);
			// Creating Path
			Path subPath = new PathImpl("path-"+i);
			subPath.setHasCapacity(reqLink.getHasCapacity());
			subPath.setExclusive(reqLink.getExclusive());
			subPath.setHasLifetimes(reqLink.getHasLifetimes());
			subPath.setContains(new HashSet<ResourceImpl>());
			
			// Getting request endpoints
			Node reqSourceNode = augSub.getReq().getEndpoints((LinkImpl) reqLink).getFirst();
			Node reqTargetNode = augSub.getReq().getEndpoints((LinkImpl) reqLink).getSecond();
			LOG.debug("Request source Node: "+reqSourceNode);
			LOG.debug("Request target Node: "+reqTargetNode);
			
			// Getting substrate nodes mapped to req nodes
			int sourceNodeID = augSub.getFakeNodeIDs().get(nodeMapping.get(reqSourceNode));
			int targetNodeID = augSub.getFakeNodeIDs().get(nodeMapping.get(reqTargetNode));
			LOG.debug("Substarte source Node ID: "+sourceNodeID);
			LOG.debug("Substarte target Node ID: "+targetNodeID);
			
			// Getting substarte path from source to target for the current request link
			LOG.debug("Building path for link: "+ augSub.getFakeLinkIDs().get(reqLink));
			ArrayList<Integer> pathNodeIDs = (ArrayList<Integer>) buildPathIDs(sourceNodeID,targetNodeID,flows[i]);
			
			if (pathNodeIDs==null) {
				LOG.error("Incorrect path mapping for request link "+ i );
				return null;
			}
			
			LOG.debug("Path Node ID's: "+pathNodeIDs);
			
			if (pathNodeIDs.size()<2) {return null;}
    		
    		// Do the work
			if(!EmbeddingOperations.buildPath(reqLink,pathNodeIDs,augSub.getSubNodelist(),subPath)){
				return null;
			}
    		
    		linkMapping.put((LinkImpl)reqLink, (ResourceImpl) subPath);
    		LOG.debug("Path "+subPath+" added to the Link Mapping");
    		
    		// Creating reverse path (if needed)
    		
    		Link reverseLink = EmbeddingOperations.getReverseLink(augSub.getReq(),reqLink,linkMapping);
    		
    		if (reverseLink == null) { continue; }
    		
    		LOG.debug("Creating reverse Path...");
    		Path reversePath = new PathImpl("path-"+i+"-reverse");
    		reversePath.setHasCapacity(augSub.getReqLinkList().get(i).getHasCapacity());
    		reversePath.setExclusive(augSub.getReqLinkList().get(i).getExclusive());
    		reversePath.setHasLifetimes(augSub.getReqLinkList().get(i).getHasLifetimes());
    		reversePath.setContains(new HashSet<ResourceImpl>());
    		// revert pathNodeIDs
    		Collections.reverse(pathNodeIDs);
    	
    		// Do the reverse work
			if(!EmbeddingOperations.buildPath(reverseLink,pathNodeIDs,augSub.getSubNodelist(),reversePath)){
				return null;
			}
    		
    		linkMapping.put((LinkImpl)reverseLink, (ResourceImpl) reversePath);
    		LOG.debug("Path "+reversePath+" added to the Link Mapping");
    		
    	}

		return linkMapping;
		
	}
	
	/**
	 * Build the substrate path according the flows table
	 * @param sourceNodeID
	 * @param targetNodeID
	 * @param linkFlows
	 * @return
	 */
	private List<Integer> buildPathIDs(int sourceNodeID, int targetNodeID,
			double[][] linkFlows) {
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		result.add(sourceNodeID);
		int current = sourceNodeID;
		
		while (current!=targetNodeID) {
			boolean found=false;
			for (int i=0; i<linkFlows[current].length;i++) {
				if (linkFlows[current][i]!=0 && !result.contains(i)) {
					result.add(i);
					current=i;
					found=true;
					break;
				}
			}
			if (!found) {
				for (int i=0;i<linkFlows.length;i++) {
					if (linkFlows[i][current]!=0 && !result.contains(i)) {
						result.add(i);
						current=i;
						found=true;
						break;
					}
				}
			}
			if (!found) {
				return null;
			}
		}
		
		return result;
	}			

	/**
	 * Sets the correct library path for glph
	 * @param path to library
	*/
	public static void setJNI(String glpkPath) {
		LOG.info("Set JNI for Glpk Java");
		try {
			String javaLibraryPath = System.getProperty("java.library.path");
			javaLibraryPath = javaLibraryPath + glpkPath;
			System.setProperty("java.library.path", javaLibraryPath); 
			
			Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
			fieldSysPath.setAccessible( true );
			fieldSysPath.set( null, null );
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}
	
}