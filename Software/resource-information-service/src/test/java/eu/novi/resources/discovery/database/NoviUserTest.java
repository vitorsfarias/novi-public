package eu.novi.resources.discovery.database;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.URI;

import eu.novi.im.policy.NOVIUser;
import eu.novi.im.policy.Role;
import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.im.policy.impl.RoleImpl;

public class NoviUserTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConnectionClass.startStorageService(false);
		ManipulateDB.clearTripleStore();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		ConnectionClass.stopStorageService();
	}

	@Test
	public void testStoreUser () {
		String pl = "http://fp7-novi.eu/NOVIPolicyService.owl#";
		
		NOVIUser user = createUser("user1234");
		URI uri = NoviUris.createNoviURI("slice34");
		
		NOVIUser user2 = new NOVIUserImpl("myUser");
		user2.setHasSessionKey("seassion key2");
		user2.setHasNoviRole(new RoleImpl("noviRoleUser2"));
		URI uri2 = NoviUris.createNoviURI("slice3456");
		URI uri3 = NoviUris.createNoviURI("slice333");
		URI uri4 = NoviUris.createNoviURI("slice444");

		assertNull(NOVIUserClass.getNoviUserSlice(uri.toString()));
		
		NOVIUserClass.storeNoviUserSlice(user, uri.toString());
		NOVIUserClass.storeNoviUserSlice(user2, uri2.toString());
		NOVIUserClass.storeNoviUserSlice(new NOVIUserImpl("empty user"), uri3.toString());
		
		LocalDbCalls.execPrintStatement(null, null, null, true, uri);
		NOVIUser getUser = NOVIUserClass.getNoviUserSlice(uri.toString());
		assertNotNull(getUser);
		assertTrue(getUser.toString().
				equals(pl+"user1234"));
		assertTrue(getUser.getHasSessionKey().
				equals("seassion key"));
		assertEquals(2, getUser.getPublicKeys().size());
		Role role = getUser.getHasNoviRole();
		assertEquals(pl+"noviRole", role.toString());
		
		NOVIUser user2db = NOVIUserClass.getNoviUserSlice(uri2.toString());
		assertNotNull(user2db);
		assertTrue(user2db.toString().equals(pl+"myUser"));
		assertTrue(user2db.getHasSessionKey().equals("seassion key2"));
		
		//update the user2
		System.out.println("Updating user 2");
		user2.setHasSessionKey("seassion key2new");
		NOVIUserClass.storeNoviUser(user2);
		user2db = NOVIUserClass.getNoviUserSlice(uri2.toString());
		assertNotNull(user2db);
		assertTrue(user2db.toString().equals(pl+"myUser"));
		assertTrue(user2db.getHasSessionKey().equals("seassion key2new"));
		
		


		//store a slice with an existing user
		NOVIUserClass.storeNoviUserSlice(user, uri4.toString());
		NOVIUser getUser4 = NOVIUserClass.getNoviUserSlice(uri4.toString());
		assertNotNull(getUser4);
		assertTrue(getUser4.toString().
				equals(pl+"user1234"));
		assertTrue(getUser4.getHasSessionKey().
				equals("seassion key"));
		assertEquals(2, getUser4.getPublicKeys().size());
		role = getUser4.getHasNoviRole();
		assertEquals(pl+"noviRole", role.toString());
		
		
		//LocalDbCalls.showAllContentOfDB();
		//store novi user using the user context
	
		NOVIUser userNew1 = createUser("userNew1");
		NOVIUser userNew2 = createUser("userNew2");
		NOVIUserClass.storeNoviUser(userNew1);
		NOVIUserClass.storeNoviUser(userNew2);
		
		LocalDbCalls.execPrintStatement(null, null, null, true, NOVIUserClass.USER_CONTEXT_URI);
		NOVIUser getUser1b = NOVIUserClass.getNoviUser(userNew1.toString());
		assertNotNull(getUser1b);
		assertTrue(getUser1b.toString().
				equals(pl+"userNew1"));
		assertTrue(getUser1b.getHasSessionKey().
				equals("seassion key"));
		assertEquals(2, getUser1b.getPublicKeys().size());
		Role role2 = getUser1b.getHasNoviRole();
		assertEquals(pl+"noviRole", role2.toString());
		
		
		assertNotNull(NOVIUserClass.getNoviUser(userNew2.toString()));
		assertTrue(NOVIUserClass.getNoviUser(userNew2.toString()).toString().
				equals(pl+"userNew2"));
		
		//update the user
		user.setHasSessionKey("seassion keynew");
		Set<String> pubKeys = new HashSet<String>();
		pubKeys.add("publicKey1.1");
		user.setPublicKeys(pubKeys);
		user.setHasNoviRole(new RoleImpl("noviRolenew"));
		NOVIUserClass.storeNoviUser(user);

		//LocalDbCalls.execPrintStatement(null, null, null, true, NoviUris.createNoviURI("novi_users"));
		getUser = NOVIUserClass.getNoviUserSlice(uri.toString());
		assertNotNull(getUser);
		assertTrue(getUser.toString().
				equals(pl+"user1234"));
		assertTrue(getUser.getHasSessionKey().
				equals("seassion keynew"));
		assertEquals(1, getUser.getPublicKeys().size());
		assertTrue(getUser.getPublicKeys().iterator().next().equals("publicKey1.1"));
		role = getUser.getHasNoviRole();
		assertEquals(pl+"noviRolenew", role.toString());

	}
	
	
	private NOVIUser createUser(String uri)
	{
		NOVIUser user = new NOVIUserImpl(uri);
		user.setHasSessionKey("seassion key");
		Set<String> pubKeys = new HashSet<String>();
		pubKeys.add("publicKey1");
		pubKeys.add("publicKey2");
		user.setPublicKeys(pubKeys);
		user.setHasNoviRole(new RoleImpl("noviRole"));
		
		return user;
	}

}
