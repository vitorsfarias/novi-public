import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.SFAConstants;
import eu.novi.requesthandler.sfa.exceptions.TestbedException;
import eu.novi.requesthandler.utils.RHUtils;


public class RHUtilsTest {

	@Test
	public void testRemoveURIprefix() {
		String removedURI = RHUtils.removeNOVIURIprefix(SFAConstants.NOVI_IM_URI_BASE + "testRemoveURI");
		assertEquals("testRemoveURI", removedURI);
	}
	
	@Test
	public void testRemovePolicyURIprefix() {
		String removedPolicyURI = RHUtils.removePolicyURIPrefix(SFAConstants.NOVI_POLICY_URI_BASE + "testRemovePolicyURI");
		assertEquals("testRemovePolicyURI", removedPolicyURI);
	}
	
	@Test
	public void testRemoveInSufix() {
		String removedSufix = RHUtils.removeInterfacesSufixes("hola.interface-in");
		assertEquals("hola.interface", removedSufix);
	}
	
	@Test
	public void testRemoveOutSufix() {
		String removedSufix = RHUtils.removeInterfacesSufixes("adios.interface-out");
		assertEquals("adios.interface", removedSufix);
	}
	
	@Test
	public void testRemovePrefixAndSufix() {
		String removedAll = RHUtils.removeInterfacePrefixAndSufix(SFAConstants.NOVI_IM_URI_BASE + "todo-out");
		assertEquals("todo", removedAll);
	}
	
	@Test
	public void testIsEmptyCollectionNull() {
		assertTrue(RHUtils.isSetEmpty(null));
	}
	
	@Test
	public void testIsEmptyCollectionEmpty() {
		assertTrue(RHUtils.isSetEmpty(new HashSet<String>()));
	}
	
	@Test
	public void testIsEmptyCollectionFull() {
		Set<String> set = new HashSet<String>();
		set.add("first");
		assertFalse(RHUtils.isSetEmpty(set));
	}
	
	@Test
	public void testGetMailFromUser() {
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos@i2cat.net");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		String mail = RHUtils.getUserMailFromNOVIUser(user);
		assertEquals("celia.velayos@i2cat.net", mail);
	}
	
	@Test
	public void testGetMailFromUserWithoutMail() {
		NOVIUserImpl user = new NOVIUserImpl("celia.velayos");
		Set<String> publicKeys = new HashSet<String>();
		publicKeys.add("this a fake public key");
		user.setPublicKeys(publicKeys);
		
		String mail = RHUtils.getUserMailFromNOVIUser(user);
		assertEquals("celia.velayos", mail);
	}	
	

	

}
