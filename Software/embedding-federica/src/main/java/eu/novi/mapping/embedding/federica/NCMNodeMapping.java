/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 * @author <a href="mailto:simon.vocella@garr.it">Simon Vocella</a>
 * @author <a href="mailto:fabio.farina@garr.it">Fabio Farina</a>
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 */
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
import eu.novi.im.core.Node;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.ResourceImpl;
import eu.novi.im.core.impl.VirtualNodeImpl;
import eu.novi.mapping.embedding.federica.utils.EmbeddingConstants;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;



/**
 * Embedding algorithm for FEDERICA testbed.
 */
public final class NCMNodeMapping{
	
	/** Main interface to report user feedback, initialized in blueprint based on service provided by novi-api. */
	private ReportEvent userFeedback;

	/** The session id. Temporary to keep the value obtained from NOVI-API,
	  * to be used when reporting user feedback.*/
	private String sessionID;
	
	/** Local logging. */
    private static final transient Logger LOG = LoggerFactory.getLogger(NCMNodeMapping.class);
    
	private String testbedName;
	private AugSubstrate augSub;
	private LogService logService;

	public NCMNodeMapping(AugSubstrate augSub, ReportEvent userFeedback, LogService logService,String sessionID, String TestbedName) {
	
	this.augSub=augSub;
	this.userFeedback= userFeedback;
	this.sessionID=sessionID;
	this.testbedName = TestbedName;	
	this.logService = logService;
	}

	public  Map<ResourceImpl, ResourceImpl> mipnm() {
		setJNI(":/usr/local/lib:/usr/local/lib/jni:/usr/lib:/usr/lib/jni");
	
	    int subNodeCount = augSub.getSubNodelist().size();
	    int reqLinkCount = augSub.getReqLinkList().size();
	    
	    Map<ResourceImpl, ResourceImpl> nodeMapping = null;
		
		/* getting augmented substrate's capacity table */
		int augNodeCount = (augSub.getAugSub()).getVertexCount();
		LOG.debug("augGraphNodeCount: "+augNodeCount);
		
		String name = null;
		int[][][] f = new int[reqLinkCount][][];
		int[][][] x = new int[reqLinkCount][][];
		
		LOG.debug("find out how many node types we have");
		LinkedHashMap<String, Integer> tmpReq = (LinkedHashMap<String, Integer>)getVirtualType(augSub.getReqNodeList());
		int vTypes =  tmpReq.size();
		LinkedHashMap<String, Integer> tmpSub = (LinkedHashMap<String, Integer>) getPhysicalType(augSub.getSubNodelist());
		int sTypes =  tmpSub.size();
		ArrayList<ArrayList<Integer>> vV = (ArrayList<ArrayList<Integer>>)createVirtualArray(augSub.getReqNodeList(), tmpReq);
		ArrayList<ArrayList<Integer>> vS = (ArrayList<ArrayList<Integer>>)createPhysicalArray(augSub.getSubNodelist(), tmpSub);
		float[][] vCost = EmbeddingOperations.getCapacities(augSub.getReqNodeList());
		float[][] sCost = EmbeddingOperations.getAvailableCapacities(augSub.getSubNodelist());
		
		

		int[] o = augSub.getSourceDest().get(0);
		int[] d = augSub.getSourceDest().get(1);	
			
		float[][] reqCapTable = EmbeddingOperations.getCapTable(augSub.getReq());
		
		try {
			glp_prob prob = GLPK.glp_create_prob();
			GLPK.glp_set_prob_name(prob, "NCM-MIP");
			GLPK._glp_lpx_set_real_parm(prob, GLPK.LPX_K_TMLIM, EmbeddingConstants.CPLEX_TIMEOUT);
			GLPK._glp_lpx_set_real_parm(prob, GLPK.LPX_K_OBJUL, Double.MAX_VALUE);
			GLPK._glp_lpx_set_real_parm(prob, GLPK.LPX_K_OBJLL, -1*Double.MAX_VALUE);
			SWIGTYPE_p_int ind;
		    SWIGTYPE_p_double val;
		
			LOG.debug("Create x,f continuous variables, all with bounds lb and ub");	
			for(int k=0; k<reqLinkCount; k++) {
				f[k] = new int[augNodeCount][];
				x[k] = new int[augNodeCount][];
				for (int u=0; u<augNodeCount; u++){
					f[k][u] = new int[augNodeCount];
					x[k][u] = new int[augNodeCount];
					for (int v=0; v<augNodeCount; v++){
						if(u==v) {continue;}
						f[k][u][v] = GLPK.glp_add_cols(prob, 1);
	                    name = "f[" + k + "," + u + "," + v + "]";
	                    GLPK.glp_set_col_name(prob, f[k][u][v], name);
	                    GLPK.glp_set_col_kind(prob, f[k][u][v], GLPKConstants.GLP_CV);
	                    GLPK.glp_set_col_bnds(prob, f[k][u][v], GLPKConstants.GLP_DB, 0, Double.MAX_VALUE);
	                    
	                    x[k][u][v] = GLPK.glp_add_cols(prob, 1);
	                    name = "x[" + k + "," + u + "," + v + "]";
	                    GLPK.glp_set_col_name(prob, x[k][u][v], name);
					    GLPK.glp_set_col_kind(prob, x[k][u][v], GLPKConstants.GLP_CV);
						GLPK.glp_set_col_bnds(prob, x[k][u][v], GLPKConstants.GLP_DB, 0, 1);
						// GLPK.glp_set_col_kind(prob, x[k][u][v], GLPKConstants.GLP_BV);
					}
				}
			}
			
			LOG.debug("Objective coefficient: minimize flows (1a)");	
			 for (int u=0; u<subNodeCount; u++) { 
				 for (int v=0; v<subNodeCount; v++) {
					 for (int k =0; k<reqLinkCount; k++) {
						LOG.debug("coef f["+u+"]["+v+"]: "+augSub.getAugCapTable()[u][v]+" "+Double.MIN_VALUE+" -> "+(1.0/(augSub.getAugCapTable()[u][v]+Double.MIN_VALUE)));
						 GLPK.glp_set_obj_coef(prob, f[k][u][v], 1.0/(augSub.getAugCapTable()[u][v]+0.00000000001));
					 }
				 }
			 }
			 
		
			LOG.debug("Objective coefficient: minimize computational capacities (1b)");	
			for (int k=0;k<reqLinkCount;k++) {
				for (int i=0;i<vTypes;i++) {
					for (int w : vS.get(i)) {
						for (int m : vV.get(i)){
							double totalCap=0;
							for (int j=0;j<EmbeddingConstants.NODE_MAX_CAPACITY_TYPES;j++){
								totalCap = (totalCap+vCost[j][m]/(sCost[j][w]+Double.MIN_VALUE));
								LOG.debug("coef x: "+totalCap+" "+Double.MIN_VALUE+" -> "+(vCost[j][m]/(sCost[j][w]+Double.MIN_VALUE)));
							}
							GLPK.glp_set_obj_coef(prob, x[k][m+subNodeCount][w], totalCap);
						}				
					}
				}	
			}
			
			LOG.debug("Objective coefficient: minimize hops (1c)");	
			 for (int k=0;k<reqLinkCount;k++) {
				 for (int u=0; u<subNodeCount; u++){
					 for (int v=0; v<subNodeCount; v++){
						 double value = GLPK.glp_get_obj_coef(prob, x[k][u][v]);
						 GLPK.glp_set_obj_coef(prob, x[k][u][v], 1+value);
						 LOG.debug("x["+k+"]["+u+"]["+v+"]: "+(1+value));
					 }
				 }	
			 }
		 
			
			LOG.debug("constraint 4.1");	
			for (int k=0;k<reqLinkCount; k++){
				for (int i=0;i<augNodeCount; i++){ 
					if (i!=(o[k]) && i!=(d[k])){
						int constraint = GLPK.glp_add_rows(prob, 1);
						GLPK.glp_set_row_name(prob, constraint, "r_(4.1)_"+constraint);
						GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_FX, 0, 0);
						ind = GLPK.new_intArray(augNodeCount*2+1);
						val = GLPK.new_doubleArray(augNodeCount*2+1);
						int idx = 1;
						for (int j=i+1; j<augNodeCount; j++){
							GLPK.intArray_setitem(ind, idx, f[k][i][j]);
							GLPK.intArray_setitem(ind, idx+1, f[k][j][i]);
							GLPK.doubleArray_setitem(val, idx, 1);
							GLPK.doubleArray_setitem(val, idx+1, -1);
							idx += 2;
						}
						GLPK.glp_set_mat_row(prob, constraint, idx-1, ind, val);
					}
				}
			}
			
			LOG.debug("constraint 4.2");
			for (int k=0;k<reqLinkCount; k++){
				int constraint = GLPK.glp_add_rows(prob, 1);
				GLPK.glp_set_row_name(prob, constraint, "r_(4.2)_"+constraint);
				GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_FX, reqCapTable[o[k]-subNodeCount][d[k]-subNodeCount], reqCapTable[o[k]-subNodeCount][d[k]-subNodeCount]);
				ind = GLPK.new_intArray(augNodeCount*2+1);
				val = GLPK.new_doubleArray(augNodeCount*2+1);
				int idx = 1;
				for (int w=0;w<augNodeCount;w++){
					if((o[k]) == w) {continue;}
					GLPK.intArray_setitem(ind, idx, f[k][o[k]][w]);
					GLPK.intArray_setitem(ind, idx+1, f[k][w][o[k]]);
					GLPK.doubleArray_setitem(val, idx, 1);
					GLPK.doubleArray_setitem(val, idx+1, -1);
					idx += 2;
				}
				GLPK.glp_set_mat_row(prob, constraint, idx-1, ind, val);
			}
			
			LOG.debug("constraint 4.3");
			for (int k=0;k<reqLinkCount; k++){
				int constraint = GLPK.glp_add_rows(prob, 1);
				GLPK.glp_set_row_name(prob, constraint, "r_(4.3)_"+constraint);
				GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_FX, -reqCapTable[o[k]-subNodeCount][d[k]-subNodeCount], -reqCapTable[o[k]-subNodeCount][d[k]-subNodeCount]);
				ind = GLPK.new_intArray(augNodeCount*2+1);
				val = GLPK.new_doubleArray(augNodeCount*2+1);
				int idx = 1;
				for (int w=0;w<augNodeCount;w++){
					if((d[k]) == w) {continue;}
					GLPK.intArray_setitem(ind, idx, f[k][d[k]][w]);
					GLPK.intArray_setitem(ind, idx+1, f[k][w][d[k]]);
					GLPK.doubleArray_setitem(val, idx, 1);
					GLPK.doubleArray_setitem(val, idx+1, -1);
					idx += 2;
				}
				GLPK.glp_set_mat_row(prob, constraint, idx-1, ind, val);
			}
		
			LOG.debug("constraint (5)");
			for (int k=0;k<reqLinkCount;k++){
				for (int i=0;i<vTypes;i++){
					for (int m : vV.get(i)){
						for (int w : vS.get(i)){
							int index=1;
							if (i==0) {index = 3;}
							for (int j=0;j<index;j++){			
								double cpuSub = sCost[j][w];
								int constraint = GLPK.glp_add_rows(prob, 1);
								GLPK.glp_set_row_name(prob, constraint, "r_(5)_"+constraint);
								GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_UP, 0, cpuSub);
								ind = GLPK.new_intArray(2);
								GLPK.intArray_setitem(ind, 1, x[k][m+subNodeCount][w]);
							    val = GLPK.new_doubleArray(2);
							    GLPK.doubleArray_setitem(val, 1, vCost[j][m]);
							    GLPK.glp_set_mat_row(prob, constraint, 1, ind, val);
							}
						}
						
					}
				}
			}
		
			 LOG.debug("Costraint (6)"); 
			 for (int k=0;k<reqLinkCount;k++){
				 for (int i=0;i<subNodeCount;i++){
					 for (int j=0;j<subNodeCount;j++){
						 if (i!=j){
						 double capacity = augSub.getAugCapTable()[i][j];
						 int constraint = GLPK.glp_add_rows(prob, 1);
						 GLPK.glp_set_row_name(prob, constraint, "r_(6)_"+constraint);
						 GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_UP, 0, 0);
						 ind = GLPK.new_intArray(4);
						 val = GLPK.new_doubleArray(4);
						 GLPK.intArray_setitem(ind, 1, x[k][i][j]);
						 GLPK.intArray_setitem(ind, 2, f[k][i][j]);
						 GLPK.intArray_setitem(ind, 3, f[k][j][i]);
						 GLPK.doubleArray_setitem(val, 1, -capacity);
						 GLPK.doubleArray_setitem(val, 2, 1);
						 GLPK.doubleArray_setitem(val, 3, 1);
						 GLPK.glp_set_mat_row(prob, constraint, 3, ind, val);
						 }
					 }
				 }
			 }
		
			LOG.debug("Costraint (7)"); 
			for (int u=0; u<augNodeCount; u++){
				for (int v=u+1; v<augNodeCount; v++){
					double capacity = augSub.getAugCapTable()[u][v];
					int constraint = GLPK.glp_add_rows(prob, 1);
					GLPK.glp_set_row_name(prob, constraint, "r_(7)_"+constraint);
					GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_UP, 0, capacity);
					ind = GLPK.new_intArray(2*reqLinkCount+1);
					val = GLPK.new_doubleArray(2*reqLinkCount+1);
					
					for (int k=0; k<reqLinkCount; k++) {
						int idx = (k+1)*2;
					    GLPK.intArray_setitem(ind, idx-1, f[k][u][v]);
					    GLPK.intArray_setitem(ind, idx, f[k][v][u]);
					    GLPK.doubleArray_setitem(val, idx-1, 1);
					    GLPK.doubleArray_setitem(val, idx, 1);
					}
					
					GLPK.glp_set_mat_row(prob, constraint, 2*reqLinkCount, ind, val);
				}		
			}
			
			LOG.debug("constraint (8)");
			for (int k=0;k<reqLinkCount;k++){
				for (int i=0;i<sTypes;i++){
					for (int w : vS.get(i)){
						if(vV.get(i).size() > 0) {
							int constraint = GLPK.glp_add_rows(prob, 1);
							GLPK.glp_set_row_name(prob, constraint, "r_(8)_"+constraint);
							GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_UP, 0, 1);
							ind = GLPK.new_intArray(vV.get(i).size()+1);
							val = GLPK.new_doubleArray(vV.get(i).size()+1);
							int idx = 1;
							for (int m : vV.get(i)){
								GLPK.intArray_setitem(ind, idx, x[k][m+subNodeCount][w]);
								LOG.debug("r_(8)_"+constraint+": x["+k+"]["+(m+subNodeCount)+"]["+w+"]: "+x[k][m+subNodeCount][w]);
							    GLPK.doubleArray_setitem(val, idx, 1);
							    idx++;
							}
							GLPK.glp_set_mat_row(prob, constraint, vV.get(i).size(), ind, val);
						}
					}
				}
			}
		
			LOG.debug("constraint 9");
			for (int k=0;k<reqLinkCount;k++){
				for (int m=subNodeCount; m<augNodeCount; m++){
					int constraint = GLPK.glp_add_rows(prob, 1);
					GLPK.glp_set_row_name(prob, constraint, "r_(9)_"+constraint);
					GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_UP, 0, 1);
					ind = GLPK.new_intArray(subNodeCount+1);
					val = GLPK.new_doubleArray(subNodeCount+1);
					int idx = 1;
					for (int w=0; w<subNodeCount; w++){
						GLPK.intArray_setitem(ind, idx, x[k][m][w]);
					    GLPK.doubleArray_setitem(val, idx, 1);
					    idx++;
					}
					GLPK.glp_set_mat_row(prob, constraint, subNodeCount, ind, val);
				}
			}
			
			LOG.debug("constraint 10");
			for (int k=0;k<reqLinkCount;k++){
				for (int i=0;i<sTypes;i++){
					for (int m : vV.get(i)){
						/* Begin add by Simon */
						if(vS.get(i).size() > 0) {
							/* End add */
							int constraint = GLPK.glp_add_rows(prob, 1);
							GLPK.glp_set_row_name(prob, constraint, "r_(10)_"+constraint);
							GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_FX, 1, 1);
							ind = GLPK.new_intArray(vS.get(i).size()+1);
							val = GLPK.new_doubleArray(vS.get(i).size()+1);
							int idx = 1;
							for (int w : vS.get(i)){
								GLPK.intArray_setitem(ind, idx, x[k][m+subNodeCount][w]);
							    GLPK.doubleArray_setitem(val, idx, 1);
							    idx++;
							}
							GLPK.glp_set_mat_row(prob, constraint, vS.get(i).size(), ind, val);
						}
					}
				}
			}
					
			LOG.debug("constraint 11");
			for (int k=0;k<reqLinkCount;k++){
				for (int i=0;i<augNodeCount; i++){
					for (int j=i+1;j<augNodeCount; j++) {
						int constraint = GLPK.glp_add_rows(prob, 1);
						GLPK.glp_set_row_name(prob, constraint, "r_(11)_"+constraint);
						GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_FX, 0, 0);
						ind = GLPK.new_intArray(3);
						val = GLPK.new_doubleArray(3);
						GLPK.intArray_setitem(ind, 1, x[k][i][j]);
						GLPK.intArray_setitem(ind, 2, x[k][j][i]);
						GLPK.doubleArray_setitem(val, 1, 1);
						GLPK.doubleArray_setitem(val, 2, -1);
						GLPK.glp_set_mat_row(prob, constraint, 2, ind, val);
					}
				}
			}
			
			 LOG.debug("constraint 12");
			 for (int k1=0;k1<reqLinkCount;k1++){
				 for (int k2=0;k2<reqLinkCount;k2++){
					 if(k1 == k2){
					 continue;
					 }
					  for (int i=subNodeCount;i<augNodeCount; i++){
						 for (int j=0;j<subNodeCount; j++){	
						 int constraint = GLPK.glp_add_rows(prob, 1);
						 GLPK.glp_set_row_name(prob, constraint, "r_(13)_"+constraint);
						 GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_FX, 0, 0);
						 ind = GLPK.new_intArray(3);
						 val = GLPK.new_doubleArray(3);
						 GLPK.intArray_setitem(ind, 1, x[k1][i][j]);
						 GLPK.intArray_setitem(ind, 2, x[k2][i][j]);
						 GLPK.doubleArray_setitem(val, 1, 1);
						 GLPK.doubleArray_setitem(val, 2, -1);
						 GLPK.glp_set_mat_row(prob, constraint, 2, ind, val);					
						 }
					 }
				 }
			 }
			
			LOG.debug("constraint 13");
			for (int k=0;k<reqLinkCount;k++){
				for (int i=0;i<augNodeCount;i++){
					for (int j=i+1;j<augNodeCount;j++){
						int constraint = GLPK.glp_add_rows(prob, 1);
						GLPK.glp_set_row_name(prob, constraint, "r_(13)_"+constraint);
						GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_LO, 0, 0);
						ind = GLPK.new_intArray(4);
						val = GLPK.new_doubleArray(4);
						GLPK.intArray_setitem(ind, 1, x[k][i][j]);
						GLPK.intArray_setitem(ind, 2, f[k][i][j]);
						GLPK.intArray_setitem(ind, 3, f[k][j][i]);
						GLPK.doubleArray_setitem(val, 1, -1);
						GLPK.doubleArray_setitem(val, 2, 1);
						GLPK.doubleArray_setitem(val, 3, 1);
						GLPK.glp_set_mat_row(prob, constraint, 3, ind, val);
					}
				}
			}
			
			LOG.debug("14th constraint");
			LOG.debug("Additional constraint: Virtual nodes used in federation are allocated on federable physical nodes");
			for (int k=0;k<reqLinkCount;k++){
				for (Node virt: augSub.getReqNodeList()){
					if (EmbeddingOperations.checkFederable(virt)){
						for (Node phys: augSub.getSubNodelist()){
							if (EmbeddingOperations.checkFederable(phys)){
								int constraint = GLPK.glp_add_rows(prob, 1);
								GLPK.glp_set_row_name(prob, constraint, "r_(13)_"+constraint);
								GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_FX, 0, 0);
								ind = GLPK.new_intArray(2);
								val = GLPK.new_doubleArray(2);
								GLPK.intArray_setitem(ind, 1, x[k][augSub.getFakeNodeIDs().get(virt)][augSub.getFakeNodeIDs().get(phys)]);
								LOG.debug("r_(14)_"+constraint+": x["+k+"]["+(augSub.getFakeNodeIDs().get(virt))+"]["+augSub.getFakeNodeIDs().get(phys)+"]: "+x[k][augSub.getFakeNodeIDs().get(virt)][augSub.getFakeNodeIDs().get(phys)]);
								GLPK.doubleArray_setitem(val, 1, 1);
								GLPK.glp_set_mat_row(prob, constraint, 1, ind, val);
							}
						}
					}
				}
			}
			
			LOG.debug("15th constraint");
			LOG.debug("Additional constraint: Support partial bound requests");
			for (Node node: augSub.getReqNodeList()){
				 if (!EmbeddingOperations.isSetEmpty(((NodeImpl)node).getImplementedBy())) {
				   for (int k=0;k<reqLinkCount;k++){
					String pNodeId = ((NodeImpl)node).getImplementedBy().iterator().next().toString();
					Node pNode = EmbeddingOperations.getNode(pNodeId, augSub.getSubNodelist());
					int constraint = GLPK.glp_add_rows(prob, 1);
					GLPK.glp_set_row_name(prob, constraint, "r_(14)_"+constraint);
					GLPK.glp_set_row_bnds(prob, constraint, GLPKConstants.GLP_FX, 1, 1);
					   ind = GLPK.new_intArray(2);
					   val = GLPK.new_doubleArray(2);
					   GLPK.intArray_setitem(ind, 1, x[k][augSub.getFakeNodeIDs().get(node)][augSub.getFakeNodeIDs().get(pNode)]);
					   GLPK.doubleArray_setitem(val, 1, 1);
					   GLPK.glp_set_mat_row(prob, constraint, 1, ind, val);
					}
				 }
			  }
			

			GLPK.glp_write_lp(prob, (new glp_cpxcp()), "ncm.problem.txt");
			GLPK.glp_set_obj_dir(prob, GLPKConstants.GLP_MIN);
			GLPK.glp_set_obj_name(prob, "ncm obj");
			
			glp_iocp iocp = new glp_iocp();
			GLPK.glp_init_iocp(iocp);
			iocp.setPresolve(GLPKConstants.GLP_ON);
			int ret = GLPK.glp_intopt(prob, iocp);
			
			LOG.debug("ret: "+ret);
			String nameProb = GLPK.glp_get_obj_name(prob);
			double valProb =  GLPK.glp_get_obj_val(prob);
			LOG.debug(nameProb+" = "+valProb);
			
			if (ret != 0) {
				GLPK.glp_delete_prob(prob);
				return null;
			} else {
			nameProb = GLPK.glp_get_obj_name(prob);
			valProb =  GLPK.glp_get_obj_val(prob);
	
			Double[][][] xVar = new Double[reqLinkCount][augNodeCount][augNodeCount];
			Double[][][] fVar = new Double[reqLinkCount][augNodeCount][augNodeCount];
				for (int k=0; k<reqLinkCount; k++){
					for (int u=0; u<augNodeCount; u++){
						for (int v=0; v<augNodeCount; v++){
							if(x[k][u][v] != 0) {
								xVar[k][u][v] = GLPK.glp_mip_col_val(prob, x[k][u][v]); 
							}
							else { 
								xVar[k][u][v] = null;
							}		
							if(f[k][u][v] != 0) {
								fVar[k][u][v] = GLPK.glp_mip_col_val(prob, f[k][u][v]);
							}
							else { 
								fVar[k][u][v] = null;
							}
							//LOG.info("xVar["+k+"]["+u+"]["+v+"]: "+xVar[k][u][v]);
							LOG.debug("fVar["+k+"]["+u+"]["+v+"]: "+fVar[k][u][v]);
						}
					}
				}		
			nodeMapping = myNodeMapping(augSub.getSubNodelist(), augSub.getReqNodeList(), reqLinkCount, xVar, fVar);	
			GLPK.glp_delete_prob(prob);
			}
		} catch (Exception e) {
			EmbeddingOperations.printErrorFeedback(sessionID, logService, userFeedback, testbedName, 
					"NCM Concert exception caught: " + e.getMessage(), 
					"NCM Error: Algorithm exception caught. Try to run the request again");	
			
		}
		
	
		return nodeMapping;
	}
	

	
	/**
	 * Separate PHYSICAL nodes by type *.
	 *
	 * @param subNodeList the substrate node list
	 * @param types the types
	 * @return the array list
	 */
	private List<ArrayList<Integer>> createPhysicalArray(List<Node> subNodeList, LinkedHashMap<String, Integer> types) {
		List<ArrayList<Integer>>  tmp = new  ArrayList<ArrayList<Integer>>();
		String[] keys = new String[types.keySet().size()];
		types.keySet().toArray(keys);
		
		for (int i=0;i<keys.length;i++) {
			tmp.add(new ArrayList<Integer>());
		}
		
		for (int i=0; i<subNodeList.size(); i++) {
			for (int j=0; j<keys.length; j++) {
				if (subNodeList.get(i).getHardwareType().equals(keys[j])) {
					tmp.get(j).add(i);
				}
			}
		}
		return tmp;
	}
	
	/**
	 * Separate VIRTUAL nodes by type *.
	 *
	 * @param reqNodeList the request node list
	 * @param types the types
	 * @return the array list
	 */
	private List<ArrayList<Integer>> createVirtualArray(List<Node> reqNodeList, LinkedHashMap<String, Integer> types) {
		List<ArrayList<Integer>>  tmp = new  ArrayList<ArrayList<Integer>>();
		String[] keys = new String[types.keySet().size()];
		types.keySet().toArray(keys);
		
		for (int i=0;i<keys.length;i++) {
			tmp.add(new ArrayList<Integer>());
		}
		
		for (int i=0; i<reqNodeList.size(); i++) {
			if (reqNodeList.get(i) instanceof VirtualNodeImpl) {
				for (int j=0; j<keys.length; j++) {
					if (((VirtualNodeImpl) reqNodeList.get(i)).getVirtualRole().equals(keys[j])) {
						tmp.get(j).add(i);
					}
				}
			}
		}
		return tmp;
	}
	
	/**
	 * Find how many PHYSICAL types are (switches, routers or servers)*.
	 *
	 * @param reqNodeList the request node list
	 * @return the amount of virtual types
	 */
	private Map<String, Integer> getVirtualType (List<Node> reqNodeList) {
		Map<String, Integer>  tmp = new LinkedHashMap<String, Integer> ();
		
		//TODO Remove this
		tmp.put(EmbeddingConstants.VM_VIRTUAL_ROLE, 0);
		tmp.put(EmbeddingConstants.ROUTER_VIRTUAL_ROLE, 0);

	    for (Node node : reqNodeList) {
	    	if (node instanceof VirtualNodeImpl) {
		    	if (!tmp.containsKey(((VirtualNodeImpl) node).getVirtualRole())) {
	    	 		tmp.put(((VirtualNodeImpl) node).getVirtualRole(), 1);
		    	}
	    	 	else {
	    	 		tmp.put(((VirtualNodeImpl) node).getVirtualRole(), tmp.get(((VirtualNodeImpl) node).getVirtualRole())+1);
	    	 	}
	    	}
	    }
		    
	    return tmp;
	}
	
	/** Find how many PHYSICAL types are (switches, routers or servers)**/
	private Map<String, Integer> getPhysicalType (List<Node> subNodeList) {
		Map<String, Integer>  tmp = new LinkedHashMap<String, Integer> ();
		
		//TODO Remove this
		tmp.put(EmbeddingConstants.VM_HW_TYPE_1, 0);
		tmp.put(EmbeddingConstants.ROUTER_HW_TYPE, 0);

	    for (Node node : subNodeList) {
	    	if (!tmp.containsKey(node.getHardwareType())) {
    	 		tmp.put(node.getHardwareType(), 1);
	    	}
    	 	else { 	    	 
    	 		tmp.put(node.getHardwareType(), tmp.get(node.getHardwareType())+1);
    	 	}
	    }
		    
	    return tmp;
	}
	

	public Map<ResourceImpl, ResourceImpl> myNodeMapping(List<Node> subNodeList, List<Node> reqNodeList, int reqLinkCount, Double[][][] xVar, Double[][][] fVar){
		Map<ResourceImpl, ResourceImpl> phi = new LinkedHashMap<ResourceImpl, ResourceImpl>(); // request-real
		double[] fai = new double[subNodeList.size()];
		double[] pai = new double[subNodeList.size()];
		boolean ctrl = true;
		
		for (int i=0; i<reqNodeList.size(); i++) {
			Node node = reqNodeList.get(i);
			int i2 = i + subNodeList.size();
			double max=0;
			int indexMax=0;
			Node selected=null;
			for (int j=0; j<subNodeList.size(); j++){
				Node subNode = subNodeList.get(j); 
				LOG.debug("Node: "+subNode+" with id  "+j);
				if (EmbeddingOperations.areCompatible((VirtualNode) node,subNode)){
					for (int k=0; k<reqLinkCount; k++){
						LOG.debug("xVar["+k+"]["+i2+"]["+j+"]: "+xVar[k][i2][j]);
						pai[j] += (fVar[k][i2][j]+fVar[k][j][i2])*xVar[k][i2][j];
					}
					LOG.debug("pai["+j+"]: "+pai[j]);
					LOG.debug("fai["+j+"]: "+fai[j]);
					if (pai[j]>max && fai[j]==0){
						max = pai[j];
						indexMax = j;
						selected=subNode;
						LOG.debug("max: "+max);
					}
				}
			}
			if (max!=0) {
				fai[indexMax]=1;
				LOG.debug("selected: "+selected);
				phi.put((ResourceImpl)node, (ResourceImpl)selected);
			} else {
				LOG.debug("node can not be mapped");
				ctrl=false;
			}
		}
		if (ctrl) {
			return phi;
		}
		else { 
			return null;
		}
	}
		

		/**
	 * Sets the correct library path for glph
	 * @param path to library
	*/
	public void setJNI(String glpkPath) {
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
