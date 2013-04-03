package eu.novi.mapping.embedding.federica;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.mapping.embedding.federica.utils.EmbeddingOperations;
import eu.novi.mapping.embedding.federica.utils.NodeComparator;
import eu.novi.mapping.embedding.federica.utils.LinkComparator;


public final class AugSubstrate {
	
	/** Local logging. */
    private static final transient Logger LOG = LoggerFactory.getLogger(AugSubstrate.class);
	
	private SparseMultigraph<NodeImpl, LinkImpl> req;
	private SparseMultigraph<NodeImpl, LinkImpl> sub;
	private Map<Node, Integer> fakeIDs;
	private Map<Link, Integer> fakeLinkIDs;
	private List<Node> subNodeList;
	private List<Link> subLinkList;
	private List<Node> reqNodeList;
	private List<Link> reqLinkList;
	private Graph<NodeImpl, LinkImpl> augmentedSubstrate;
	private List<Node> augNodeList;
	private List<int[]>  listSD;
	private float[][] augCapTable;
	
	public AugSubstrate(SparseMultigraph<NodeImpl, LinkImpl> req, SparseMultigraph<NodeImpl, LinkImpl> sub) {
	this.req = req;
	this.sub = sub;
	this.subNodeList = EmbeddingOperations.getNodes(sub);	
	this.subLinkList= EmbeddingOperations.getLinks(sub);
	this.reqNodeList = EmbeddingOperations.getNodes(req);
	this.reqLinkList = EmbeddingOperations.getLinks(req);
	this.augmentedSubstrate = buildAugmentedSubstrate(sub, reqNodeList, subNodeList, subLinkList);
	this.augNodeList = EmbeddingOperations.getNodes((SparseMultigraph<NodeImpl, LinkImpl>) augmentedSubstrate);
	this.fakeIDs = new HashMap<Node, Integer> (setFakeNodeIDs(subNodeList, reqNodeList));
	this.fakeLinkIDs = new HashMap<Link, Integer> (setFakeLinkIDs(reqLinkList));
	Collections.sort(this.subNodeList,new NodeComparator(this.fakeIDs));
	Collections.sort(this.reqNodeList,new NodeComparator(this.fakeIDs));	
	Collections.sort(this.reqLinkList,new LinkComparator(this.fakeLinkIDs));	
	this.listSD = EmbeddingOperations.getSourceDest(req, reqLinkList,fakeIDs,fakeLinkIDs);
	this.augCapTable = EmbeddingOperations.getAvailableCapTable((SparseMultigraph<NodeImpl, LinkImpl>) augmentedSubstrate,subNodeList,fakeIDs);
	}

	public List<Node> getSubNodelist(){
	return this.subNodeList;
	}
	
	public  List<Link> getSubLinkList(){
	return this.subLinkList;
	}
	
	public List<Node> getReqNodeList(){
	return this.reqNodeList;
	}
	
	public  List<Link> getReqLinkList(){
	return this.reqLinkList;
	}
	
	public  SparseMultigraph<NodeImpl, LinkImpl> getReq(){
	return this.req;
	}
	
	public  SparseMultigraph<NodeImpl, LinkImpl> getSub(){
	return this.sub;
	}
	
	public  Map<Node, Integer> getFakeNodeIDs(){
	return this.fakeIDs;
	}
			
	public  Map<Link, Integer> getFakeLinkIDs(){
	return this.fakeLinkIDs;
	}
	
	public Graph<NodeImpl, LinkImpl> getAugSub(){
	return this.augmentedSubstrate;
	}
	
	public List<Node> getAugSubNodeList(){
	return this.augNodeList;
	}
	
	public List<int[]> getSourceDest(){
	return this.listSD;
	}
	
	public float[][] getAugCapTable() {
	return this.augCapTable;
	}
	
	/** 
	 * Build a augmented graph getting request nodes and substrate nodes and links
	 * @param sub 
	 * @param reqNodeList
	 * @param subNodeList
	 * @param subLinkList
	 * @return
	 */
	private Graph<NodeImpl, LinkImpl> buildAugmentedSubstrate(
			SparseMultigraph<NodeImpl, LinkImpl> sub, List<Node> reqNodeList, List<Node> subNodeList,
			List<Link> subLinkList) {
		
		Graph<NodeImpl, LinkImpl> augmentedSubstrate =	new SparseMultigraph<NodeImpl, LinkImpl>();
		
		
		LOG.debug("**************************************************************************");
	    LOG.debug("Add request nodes to augmented link with infinite bw");

		/* add request nodes to the augmented graph */
		for (Node x : reqNodeList) {
			augmentedSubstrate.addVertex((NodeImpl) x);
			for (Node subNode : subNodeList) {
				LinkImpl l = new LinkImpl("aug-Link-"+x+"-"+subNode);
				l.setHasAvailableCapacity((float) 2.147483647E5);
				LOG.debug("Node "+x.toString());
				LOG.debug("Node "+subNode.toString());
				augmentedSubstrate.addEdge((LinkImpl)l, (NodeImpl)x, (NodeImpl) subNode, EdgeType.UNDIRECTED);
			}
		}
		
		LOG.debug("**************************************************************************");
	    LOG.debug("Add substrate to augmented link");
		
		/* add substrate to augmented graph */
		for (Link current : subLinkList){
			Pair<NodeImpl> x =  sub.getEndpoints((LinkImpl) current);
			augmentedSubstrate.addEdge((LinkImpl) current, x.getFirst(), x.getSecond(), EdgeType.UNDIRECTED);
			augmentedSubstrate.addVertex(x.getFirst());
			augmentedSubstrate.addVertex(x.getSecond());
			
			LOG.debug("Node "+x.getFirst());
			LOG.debug("Node "+x.getSecond());
			LOG.debug("Link "+current+" has available capacity: "+current.getHasAvailableCapacity());
		}

		LOG.debug ("augmentedSubstrate Nodes num is:" + augmentedSubstrate.getVertexCount());
        LOG.debug ("augmentedSubstrate Links num is: "+ augmentedSubstrate.getEdgeCount());
		LOG.debug("**************************************************************************");
		
		
		return augmentedSubstrate;
	}
	
		/**
	 * Constructs consecutive Fake IDs for substrate and requested Node resources 
	 * @param subResList the list of substrate nodes e.g. physical nodes
	 * @param subResList the list of requested nodes e.g. requested nodes
	 * @return a HashMap containing the resource and their IDs.
	 */
	private Map<Node, Integer> setFakeNodeIDs( List<Node> subNodeList,  List<Node> reqNodeList) {
		Map<Node, Integer> fakeIDs = new HashMap<Node, Integer>();
		
		int id = 0;
		for (Node x: subNodeList){
		fakeIDs.put(x,id);
			id++;
		}
		for (Node x: reqNodeList){
			fakeIDs.put(x,id);
			id++;
		}
		
		return fakeIDs;
	 
	 }
	 
	 	/**
	 * Constructs consecutive Fake IDs for requested Link resources 
	 * @param reqLinkList the list of requested nodes e.g. requested links
	 * @return a HashMap containing the resource and their IDs.
	 */
	private Map<Link, Integer> setFakeLinkIDs(List<Link> reqLinkList) {
		Map<Link, Integer> fakeLinkIDs =  new HashMap<Link, Integer>();
		
		int id = 0;
		for (Link x: reqLinkList){
			fakeLinkIDs.put(x,id);
			id++;
		}
	
		return fakeLinkIDs;
	 
	 }

}