package eu.novi.im.util;



import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import eu.novi.im.core.Interface;
import eu.novi.im.core.Link;
import eu.novi.im.core.NetworkElementOrNode;
import eu.novi.im.core.Node;
import eu.novi.im.core.Path;
import eu.novi.im.core.Platform;
import eu.novi.im.core.Resource;
import eu.novi.im.core.Topology;
import eu.novi.im.core.impl.InterfaceImpl;
import eu.novi.im.core.impl.LinkImpl;
import eu.novi.im.core.impl.NodeImpl;
import eu.novi.im.core.impl.PlatformImpl;
import eu.novi.im.core.impl.TopologyImpl;

public class IMUtilTest {



	@Test
	public void test() {
		
		Set<Integer> set = IMUtil.createSetWithOneValue(3);
		assert(set.size()==1);
		int i = IMUtil.getOneValueFromSet(set);
		assert(i==3);
	}
	
	@Test
	public void testCreateGenericObjects(){
		IMRepositoryUtil imUtil = new IMRepositoryUtilImpl();
		Topology topo = imUtil.createTopology("MyTopology");
		assert(topo != null);
		Path path = imUtil.createObject("MyPath",Path.class);
		assert(path != null);
		Set<NetworkElementOrNode> nexts = path.getNexts();
		assert(nexts!=null);
		
	}
	
	@Test
	public void testCloneInterfaceToImpl(){
		IMRepositoryUtil imUtil = new IMRepositoryUtilImpl();
		Topology srcInterface = imUtil.createTopology("MyTopology");
		TopologyImpl tgtImpl = new TopologyImpl(srcInterface.toString());
		imUtil.cloneInterfaceToImplementation(srcInterface, tgtImpl);
		
	}
	
	@Test 
	public void testIMNSwitch() throws UnknownHostException{
			
			//final Router r = ctx.getService(ctx.getServiceReference( "eu.novi.resources.Router","(testbed=FEDERICA)")).clone();
			//		r.setIpAddress(InetAddress.getByName(„router1”));
			Node router = new NodeImpl("federica.router1");
			Interface routerInterface = new InterfaceImpl("federica.router1.interface");
			//routerInterface.setHasIPv4Address(IMUtil.createSetWithOneValue(InetAddress.getByName("router1").toString()));
			router.setHasInboundInterfaces(IMUtil.createSetWithOneValue(routerInterface));
			
			//final Node sliver = ctx.getService(ctx.getServiceReference("eu.novi.resources.Node","(testbed=PlanetLab)")).clone();
			//		  sliver.setIpAddress(InetAddress.getByName(„host1”));
			Node sliver = new NodeImpl("planetlab.host1");
			Interface sliverInterface = new InterfaceImpl("planetlab.host1.interface");
			//sliverInterface.setHasIPv4Address(IMUtil.createSetWithOneValue(InetAddress.getByName("host1").toString()));
			sliver.setHasInboundInterfaces(IMUtil.createSetWithOneValue(sliverInterface));

			//final Node vm = ctx.getService(ctx.getServiceReference("eu.novi.resources.Node","(testbed=FEDERICA)")).clone();
			//		  vm.setIpAddress(InetAddress.getByName(„host2”));
			Node vm   = new NodeImpl("federica.host2");
			Interface vmInterface = new InterfaceImpl("federica.host2.interface");
			//vmInterface .setHasIPv4Address(IMUtil.createSetWithOneValue(InetAddress.getByName("host2").toString()));
			vm.setHasInboundInterfaces(IMUtil.createSetWithOneValue(vmInterface));
			
		    //r.connectWith(sliver);
			Link linkRouterSliver = new LinkImpl("router.sliver.link");
			routerInterface.setIsSource(IMUtil.createSetWithOneValue(linkRouterSliver));
			sliverInterface.setIsSink(IMUtil.createSetWithOneValue(linkRouterSliver));
		
			//r.connectWith(vm);
			Link linkRouterVM = new LinkImpl("router.vm.link");
			//routerInterface.setIsSource(IMUtil.createSetWithOneValue(linkRouterVM));
			routerInterface.getIsSource().add(linkRouterVM);
			vmInterface.setIsSink(IMUtil.createSetWithOneValue(linkRouterVM));
		
			//vm.connectWith(r);
			vmInterface.setIsSource(IMUtil.createSetWithOneValue(linkRouterVM));
			routerInterface.setIsSink(IMUtil.createSetWithOneValue(linkRouterVM));
		
			//sliver.connectWith(r);
			sliverInterface.setIsSource(IMUtil.createSetWithOneValue(linkRouterSliver));
			//routerInterface.setIsSink(IMUtil.createSetWithOneValue(linkRouterSliver));
			routerInterface.getIsSink().add(linkRouterSliver);
			
			Platform pFederica  = new PlatformImpl("FEDERICA");
			Set<Resource> federicaMembers = new HashSet<Resource>();
			federicaMembers.add(router);
			pFederica.setContains(federicaMembers);
			
			Platform pPlanetlab = new PlatformImpl("PlanetLab");
			Set<Resource> planetlabMembers = new HashSet<Resource>();
			planetlabMembers.add(sliver);
			planetlabMembers.add(vm);
			pPlanetlab.setContains(planetlabMembers);
			
			//final Slice slice = new Slice();
			Topology slice =new TopologyImpl("slice");
	
			Set<Resource> sliceMembers = new HashSet<Resource>();
			sliceMembers.add(router);
			sliceMembers.add(sliver);
			sliceMembers.add(vm);
			
			slice.setContains(sliceMembers);
			
	}
	
}
