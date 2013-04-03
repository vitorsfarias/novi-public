package eu.novi.im.ris.test;

import java.util.Set;

import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

import eu.novi.im.core.InterfaceOrNode;
import eu.novi.im.core.Node;
import eu.novi.im.util.IMRepositoryUtilImpl;
import eu.novi.im.util.IMUtil;

public class SimpleLoopTest {

	
	/*@Test
	public void testLoop() throws RepositoryException{
		IMRepositoryUtilImpl util = new IMRepositoryUtilImpl();
			//Platform federica = util.createObject("FEDERICA", Platform.class);
			
			Node X0 =  util.createObject("X0",Node.class);
			Node X1 =  util.createObject("X1",Node.class);
			
			//federica.setContains(IMUtil.createSetWithOneValue(X0));
			
			util.getConnection().addObject(X0.toString(),X0);
			util.getConnection().addObject(X1.toString(),X1);
			//util.getConnection().addObject(federica.toString(),federica);
			
			X1.setConnectedTo(IMUtil.createSetWithOneValue(X0));
			X0.setConnectedTo(IMUtil.createSetWithOneValue(X1));
			
			util.getConnection().commit();
			
			assert(X0.getConnectedTo().size()==1);
			assert(X1.getConnectedTo().size()==1);
			
			
			
			RepositoryResult<Statement> res = util.getConnection().getStatements(null, null,null,null);
			while(res.hasNext()){
				System.out.println(res.next());
			}
			
			IMRepositoryUtilImpl anotherUtil = new IMRepositoryUtilImpl();
		
			//Platform anotherPlatform =anotherUtil.createObject("OtherPlatform",Platform.class);
			//anotherPlatform.setContains(federica.getContains());
			Node X2 = anotherUtil.createObject("X2", Node.class);
			X2.setHardwareType("SomeHardware");
			
			anotherUtil.getConnection().clear(null);
//		
//			X2.setConnectedTo(X0.getConnectedTo());
//			System.out.println("Another one");
			res = anotherUtil.getConnection().getStatements(null, null,null,null);
			while(res.hasNext()){
				System.out.println(res.next());
			}
			
			System.out.println("The repository is cleared but this object is still here " + X2+ "  " + X2.getHardwareType());
	}*/
	
	@Test
	public void testClearRepository() throws RepositoryException{
		IMRepositoryUtilImpl util = new IMRepositoryUtilImpl();
		
		Node firstNode = util.createObject("firstNode", Node.class);
		firstNode.setHardwareType("FirstHardware");
		
		Node secondNode = util.createObject("secondNode", Node.class);
		secondNode.setHardwareType("SecondHardware");
		
		firstNode.setConnectedTo(IMUtil.createSetWithOneValue(secondNode));
		
		util.getConnection().addObject(firstNode.toString(),firstNode);
		util.getConnection().addObject(secondNode.toString(),secondNode);
		
		
		System.out.println("\nBefore  clearing ");
		RepositoryResult<Statement> resultStatement = util.getConnection().getStatements(null, null,null,null);
		while(resultStatement.hasNext()){
			System.out.println(resultStatement.next());
		}
		
		util.getConnection().clear(null);

		System.out.println("\nAfter clearing ");
		resultStatement = util.getConnection().getStatements(null, null,null,null);
		while(resultStatement.hasNext()){
			System.out.println(resultStatement.next());
		}
		
		System.out.println("\nThe repository is cleared but what about the objects ? ");
		Set<InterfaceOrNode> connectedTo = firstNode.getConnectedTo();
		
		System.out.println(firstNode+ " " + firstNode.getHardwareType() + " "+connectedTo.size());
		System.out.println(secondNode+ " " + secondNode.getHardwareType() + " ");
		
		Node fromSet = (Node)connectedTo.iterator().next();
		System.out.println(fromSet + " "+fromSet.getHardwareType());
		
		
	}
}
