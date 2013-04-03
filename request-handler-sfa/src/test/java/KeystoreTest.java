import static org.junit.Assert.*;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;

import org.junit.Ignore;
import org.junit.Test;

import eu.novi.requesthandler.utils.Keystore;



public class KeystoreTest {

	@Test
	public void shouldProvideKey() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		Keystore ple = Keystore.PLE_STORE;
		Keystore fed = Keystore.FEDERICA_STORE;
		
		ple.createKeyStore();
		ple.createTrustStore();
		fed.createKeyStore();
		fed.createTrustStore();
		ple.createKeyStore();
		ple.createTrustStore();
		assertEquals(1, ple.getKsKey().size());
		assertEquals(Arrays.asList("pi certificate"), toList(ple.getKsKey().aliases()));
//		assertEquals("ASAS", ple.getKsKey().getCertificate("pi certificate").toString());
	}

	private List<String> toList(Enumeration<String> aliases) {
		final List<String> result = new ArrayList<String>();
		for (; aliases.hasMoreElements(); ) {
			String elem = aliases.nextElement();
			result.add(elem);
		}
		return result;
	}
	
	@Test
	public void getKsKey() {
		Keystore pleK = Keystore.PLE_STORE;
		
		KeyStore k = pleK.getKsKey();
		assertNotNull(k);
		
		KeyStore t = pleK.getKsTrust();
		assertNotNull(t);
	}
}
