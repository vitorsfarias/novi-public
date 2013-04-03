package eu.novi.SynchWithRIS;

import static org.junit.Assert.*;
//import junit.framework.TestCase;
import eu.novi.SynchWithRIS.AuthorizationSearch;
//import .junit.Test;
import org.junit.Test;

public class AuthorizationSearchTest2{

	@Test
	public void testAuthorizedForResource() {
		String auth="AUTH";
		String notauth="NOAUTH";
		String response;
		AuthorizationSearch authsearch= new AuthorizationSearch();
		response= authsearch.AuthorizedForResource("test1" , "test2");
		if (response.equals(auth)) 
			System.out.println("Resource aurhorized for User");
		else fail("Not yet implemented");
		
	}


}
