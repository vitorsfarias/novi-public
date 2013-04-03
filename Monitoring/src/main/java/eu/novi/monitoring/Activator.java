package eu.novi.monitoring;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

//import eu.novi.policy.interfaces.InterfaceForMonitoring;
//import eu.novi.policy.monitoringevents.MonitoringEvents;

//public class Activator implements BundleActivator, ServiceTrackerCustomizer {
public class Activator implements BundleActivator {
	private ServiceTracker resourceTracker;
//	private ServiceTracker policyTracker;
	private BundleContext ctx;

	/**
	 * Implements BundleActivator.start().
	 * 
	 * @param bundleContext
	 *            - the framework context for the bundle.
	 **/
	public void start(BundleContext bundleContext) {
		System.out.println(bundleContext.getBundle().getSymbolicName()
				+ " started");
		MonSrv service = null;
		try {
			service = MonSrvFactory.create(bundleContext);
			//startTrackingResourceService(bundleContext);
		} catch (Exception ex) {
			System.err.println("Resource:" + ex.getMessage());
			ex.printStackTrace();
		}

//		try {
//			startTrackingPolicyService(bundleContext);
//		} catch (Exception ex) {
//			System.err.println("Policy:" + ex.getMessage());
//			ex.printStackTrace();
//		}
		
		try {
			bundleContext
					.registerService(MonSrv.class.getName(), service, null);
		} catch (Exception ex) {
			System.err.println("Register MonSrv:" + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/*(private void startTrackingResourceService(BundleContext context) throws Exception {
		ctx = context;
		resourceTracker = new ServiceTracker(context, Resource.class.getName(), this);
		resourceTracker.open();
	}*/

//	private void startTrackingPolicyService(BundleContext context) throws Exception {
//		ctx = context;
//		policyTracker = new ServiceTracker(context, InterfaceForMonitoring.class.getName(), this);
//		policyTracker.open();
//	}


	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		resourceTracker.close();
		resourceTracker = null;
//		policyTracker.close();
//		policyTracker = null;
		ctx = null;
	}
	
//	private void setupPolicy(InterfaceForMonitoring monevents) {
//		final ServiceTracker monSrvTracker = new ServiceTracker(ctx, MonSrv.class.getName(), null);
//		monSrvTracker.open();
//		MonSrv service = (MonSrv) monSrvTracker.getService();
//		service.setPolicy(monevents);
//		monSrvTracker.close();
//	}

	/*
	 * THIS IS QUITE OLD CODE, SHOULD IT BE REMOVED?
	 * 
	private void setupResource(Resource resource) {
		final ServiceTracker monSrvTracker = new ServiceTracker(ctx, MonSrv.class.getName(), null);
		monSrvTracker.open();
		MonSrv service = (MonSrv) monSrvTracker.getService();
		service.setResource(resource);
		monSrvTracker.close();
	}
	
	@Override
	public Object addingService(ServiceReference ref) {
		final Object obj = ctx.getService(ref);
		if ( obj instanceof Resource ) {
			final Resource resource = (Resource) obj;
			setupResource(resource);
		}
//		else if ( obj instanceof InterfaceForMonitoring ) {
//			final InterfaceForMonitoring monevents = (InterfaceForMonitoring) obj;
//			setupPolicy(monevents);
//		}
		return obj;
	}

	@Override
	public void modifiedService(ServiceReference ref, Object service) {
		if ( service instanceof Resource ) {
			setupResource((Resource) service);
		}
//		else if ( service instanceof InterfaceForMonitoring ) {
//			setupPolicy((InterfaceForMonitoring) service);
//		}
	}

	@Override
	public void removedService(ServiceReference ref, Object service) {
		if ( service instanceof Resource ) {
			setupResource((Resource) service);
		}
//		else if ( service instanceof InterfaceForMonitoring ) {
//			setupPolicy((InterfaceForMonitoring) service);
//		}
	}*/
}
