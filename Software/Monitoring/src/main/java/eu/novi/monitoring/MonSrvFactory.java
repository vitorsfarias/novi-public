package eu.novi.monitoring;

import org.osgi.framework.BundleContext;

import eu.novi.python.integration.JythonObjectFactory;

public class MonSrvFactory {

	public static MonSrv create(BundleContext ctx) throws ClassNotFoundException {
		return JythonObjectFactory.createObject(MonSrv.class, "Service.MonSrvImpl", ctx);
	}
}
