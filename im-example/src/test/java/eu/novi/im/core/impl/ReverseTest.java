package eu.novi.im.core.impl;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.Node;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.VirtualNode;
import eu.novi.im.util.IMUtil;

public class ReverseTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testSinkSource() {
		Link link = new LinkImpl("link1");
		Link link2 = new LinkImpl("link2");
		Interface inter = new InterfaceImpl("intrface1");

		
		inter.setIsSource(IMUtil.createSetWithOneValue(link));
		assertNotNull(link.getHasSource());
		assertEquals(1, link.getHasSource().size());
		assertTrue(IMUtil.getOneValueFromSet(link.getHasSource()).toString().endsWith("intrface1"));
		
		inter.setIsSink(IMUtil.createSetWithOneValue(link));
		assertNotNull(link.getHasSink());
		assertEquals(1, link.getHasSink().size());
		assertTrue(IMUtil.getOneValueFromSet(link.getHasSink()).toString().endsWith("intrface1"));
		
		//change interface
		inter.setIsSource(IMUtil.createSetWithOneValue(link2));
		assertEquals(0, link.getHasSource().size());
		assertNotNull(link2.getHasSource());
		assertEquals(1, link2.getHasSource().size());
		assertTrue(IMUtil.getOneValueFromSet(link2.getHasSource()).toString().endsWith("intrface1"));
		
		inter.setIsSink(IMUtil.createSetWithOneValue(link2));
		assertEquals(0, link.getHasSink().size());
		assertNotNull(link2.getHasSink());
		assertEquals(1, link2.getHasSink().size());
		assertTrue(IMUtil.getOneValueFromSet(link2.getHasSink()).toString().endsWith("intrface1"));
		
		//set null
		inter.setIsSource(null);
		assertEquals(0, link.getHasSource().size());
		assertEquals(0, link2.getHasSource().size());
		
		inter.setIsSink(null);
		assertEquals(0, link.getHasSink().size());
		assertEquals(0, link2.getHasSink().size());
		
		//set two links
		Set<Link> links = new HashSet<Link>();
		links.add(link2);
		links.add(link);
		inter.setIsSource(links);
		assertEquals(1, link.getHasSource().size());
		assertEquals(1, link2.getHasSource().size());
		assertTrue(IMUtil.getOneValueFromSet(link2.getHasSource()).toString().endsWith("intrface1"));
		assertTrue(IMUtil.getOneValueFromSet(link.getHasSource()).toString().endsWith("intrface1"));
		
		
	}
	
	@Test
	public void testContain() {
		Resource r1 = new ResourceImpl("resource1");
		Resource r2 = new ResourceImpl("resource2");
		Resource r3 = new ResourceImpl("resource3");
		Topology top1 = new TopologyImpl("topology1");
		Topology top2 = new TopologyImpl("topology2");
		
		//add the resources to  topology1
		Set<Resource> reSet = new HashSet<Resource>();
		reSet.add(r1);
		reSet.add(r2);
		top1.setContains(reSet);
		assertEquals(1, r1.getIsContainedIn().size());
		assertEquals(1, r2.getIsContainedIn().size());
		assertTrue(r1.getIsContainedIn().toString().contains("topology1"));
		assertTrue(r2.getIsContainedIn().toString().contains("topology1"));
		
		//add the resources to  topology2
		top2.setContains(reSet);
		assertEquals(2, r1.getIsContainedIn().size());
		assertEquals(2, r2.getIsContainedIn().size());
		//System.out.println(r1.getIsContainedIn().toString() + ", " + r2.getIsContainedIn().toString());
		assertTrue(r1.getIsContainedIn().toString().contains("topology2"));
		assertTrue(r1.getIsContainedIn().toString().contains("topology1"));
		
		//add also resource3 to topology1
		reSet.add(r3);
		top1.setContains(reSet);
		assertEquals(2, r1.getIsContainedIn().size());
		assertEquals(2, r2.getIsContainedIn().size());
		assertEquals(1, r3.getIsContainedIn().size());
		assertTrue(r1.getIsContainedIn().toString().contains("topology2"));
		assertTrue(r1.getIsContainedIn().toString().contains("topology1"));
		assertTrue(r3.getIsContainedIn().toString().contains("topology1"));
		
		//set the topology1 contains to null
		top1.setContains(null);
		assertEquals(1, r1.getIsContainedIn().size());
		assertEquals(1, r2.getIsContainedIn().size());
		assertEquals(0, r3.getIsContainedIn().size());
		//System.out.println(r1.getIsContainedIn().toString() + ", " + r2.getIsContainedIn().toString());
		assertTrue(r1.getIsContainedIn().toString().contains("topology2"));
		assertFalse(r1.getIsContainedIn().toString().contains("topology1"));
		assertFalse(r2.getIsContainedIn().toString().contains("topology1"));

		
	}
	
	@Test
	public void testAll()
	{
		Node nd1 = new NodeImpl("node1");
		Node nd2 = new NodeImpl("node2");
		Set<Node> setN = new HashSet<Node>();
		setN.add(nd1);
		setN.add(nd2);
		
		VirtualNode vn = new VirtualNodeImpl("virtualNode");
		vn.setImplementedBy(setN);
		assertEquals(1, nd1.getImplements().size());
		assertEquals(1, nd2.getImplements().size());
		
		
		VirtualNode vn2 = new VirtualNodeImpl("virtualNode2");
		vn2.setImplementedBy(setN);
		assertEquals(2, nd1.getImplements().size());
		assertEquals(2, nd2.getImplements().size());
		
		Interface in1 = new InterfaceImpl("interface1");

		
		////hasInboundInterface, hasOutboundInterface
		Interface in2 = new InterfaceImpl("interface2");
		Set<Interface> setIn = new HashSet<Interface>();
		setIn.add(in1);
		setIn.add(in2);
		nd1.setHasInboundInterfaces(setIn);
		assertEquals(1, in1.getIsInboundInterfaceOf().size());
		assertEquals(1, in2.getIsInboundInterfaceOf().size());
		assertNull(in1.getIsOutboundInterfaceOf());
		assertNull(in2.getIsOutboundInterfaceOf());
		
		nd1.setHasOutboundInterfaces(setIn);
		assertEquals(1, in1.getIsOutboundInterfaceOf().size());
		assertEquals(1, in2.getIsOutboundInterfaceOf().size());
		
	}

}
