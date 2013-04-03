package eu.novi.nswitch.exceptions;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
public class ExceptionTest {

	@Test
	public void createFederationException(){
		FederationException e = new FederationException();
		assertNotNull(e);
	}
	
	@Test
	public void createFederationExceptionWithMessage(){
		FederationException e = new FederationException("Message");
		assertNotNull(e);
	}
	
	@Test
	public void createIncorectTopologyException(){
		IncorrectTopologyException e = new IncorrectTopologyException();
		assertNotNull(e);
	}
	
	@Test
	public void createIncorectTopologyExceptionWithMessge(){
		IncorrectTopologyException e = new IncorrectTopologyException("Message");
		assertNotNull(e);
	}
}
