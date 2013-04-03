package eu.novi.requesthandler.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import eu.novi.im.policy.impl.NOVIUserImpl;
import eu.novi.requesthandler.sfa.SFAConstants;
import eu.novi.requesthandler.sfa.clients.NoviplXMLRPCClient;

/**
 * Util methos for Request Handler.
 * @author <a href="mailto:celia.velayos@i2cat.net">Celia Velayos - i2CAT</a>
 */
public class RHUtils {
	private final static String inSufix = "-in";
	private final static String outSufix = "-out";

	public static boolean isSetEmpty(Collection<? extends Object> collection) {
		  return (collection == null || collection.isEmpty());
	}
	
	public static String removeNOVIURIprefix(String original) {
		return original.replace(SFAConstants.NOVI_IM_URI_BASE, "");
	}
	
	public static String removeInterfacesSufixes(String interfaceName) {
		return interfaceName.toLowerCase().replace(inSufix, "").replace(outSufix, "");
	}
	
	public static String removeInterfacePrefixAndSufix(String interfaceName) {
		return removeNOVIURIprefix(removeInterfacesSufixes(interfaceName));
	}
	
	public static String removePolicyURIPrefix(String user) {
		return user.replace(SFAConstants.NOVI_POLICY_URI_BASE, "");
	}
	
	public static String getUserMailFromNOVIUser(NOVIUserImpl user) {
		return removePolicyURIPrefix(user.toString());
	}
	

	
	public static String readFileAsString(String filePath)
	throws java.io.IOException {
		InputStream inputStream = getFilePathFromBundleorResource(filePath);
		return inputStream.toString();
	}

	
	public static InputStream getFilePathFromBundleorResource(String fileName) {
		Bundle bundle = FrameworkUtil.getBundle(NoviplXMLRPCClient.class);
		InputStream result = null;
		try {
			if (bundle != null) {
				result = bundle.getEntry(fileName).openStream();
			} else {
				result = new FileInputStream(NoviplXMLRPCClient.class.getResource("/" + fileName).getPath());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
