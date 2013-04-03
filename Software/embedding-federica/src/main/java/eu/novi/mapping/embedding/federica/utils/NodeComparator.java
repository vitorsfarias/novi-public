/*
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
 * Copyright according to BSD License
 * For full text of the license see: Software/LICENSE.txt
 *
 * @author <a href="mailto:alvaro.monje@entel.upc.edu">Alvaro Monje</a>
 * @author <a href="mailto:arisleeiv@netmode.ntua.gr">Aris Levadeas</a>
 * @author <a href="mailto:chrisap@noc.ntua.gr">Chrysa Papagianni</a>
 */
package eu.novi.mapping.embedding.federica.utils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import eu.novi.im.core.Node;

/**
 * The Class NodeComparator.
 */
public class NodeComparator implements Comparator<Node> {

	private Map<Node, Integer> fakeIDs =  new HashMap<Node, Integer>();
	
	public NodeComparator(Map<Node, Integer>  ids) {
		this.fakeIDs = ids;
	}
	
	@Override
	public final int compare(Node a, Node b) {
		int x = this.fakeIDs.get(a);
		int y= this.fakeIDs.get(b) ;
		return Integer.valueOf(x).compareTo(Integer.valueOf(y));
	}
	
} 