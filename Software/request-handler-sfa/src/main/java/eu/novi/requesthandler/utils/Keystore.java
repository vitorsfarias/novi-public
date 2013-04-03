package eu.novi.requesthandler.utils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

public class Keystore {

	private static final String PLE_TRUSTSTORE = "newPL.keystore";
	private static final String PLE_KEYSTORE = "sfatest.p12";
	private static final char[] passphrase = "123456".toCharArray();
	private String keystore ;
	private String truststore;
	
	private KeyStore ksKey;

	private KeyStore ksTrust;
	
	public static final Keystore PLE_STORE = new Keystore(PLE_KEYSTORE, PLE_TRUSTSTORE);
	private static final String FEDERICA_TRUSTSTORE = "firexpNovi.keystore";
	private static final String FEDERICA_KEYSTORE = "firexpNovi.p12";
	public static final Keystore FEDERICA_STORE = new Keystore(FEDERICA_KEYSTORE, FEDERICA_TRUSTSTORE);
	
	private Keystore(String keystore, String truststore) {
		this.keystore = keystore;
		this.truststore = truststore;
	}
	
	public HostnameVerifier createHostnameVerifier() throws KeyStoreException,
			IOException, NoSuchAlgorithmException, CertificateException,
			UnrecoverableKeyException, KeyManagementException {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}

		};
		return hv;
	}

	public TrustManagerFactory createTrustStore()
			throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException {
		// Initialize the trustore:
		ksTrust = KeyStore.getInstance("JKS");
		ksTrust.load(RHUtils.getFilePathFromBundleorResource(truststore ),
				passphrase);

		// TrustManagers decide whether to allow connections
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ksTrust);

		return tmf;
	}

	public KeyManagerFactory createKeyStore()
			throws KeyStoreException, IOException, NoSuchAlgorithmException,
			CertificateException, UnrecoverableKeyException {
		ksKey = KeyStore.getInstance("PKCS12");
		ksKey.load(RHUtils.getFilePathFromBundleorResource(keystore), passphrase);

		// KeyManager's decide which key material to use:
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ksKey, passphrase);

		return kmf;
	}

	
	public KeyStore getKsKey() {
		return ksKey;
	}

	public KeyStore getKsTrust() {
		return ksTrust;
	}
}
