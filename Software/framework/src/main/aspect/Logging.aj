package eu.novi.framework;

import org.aspectj.lang.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Adds debug logs to all the codebase developed in NOVI.
 * 
 * It uses OSGi LogService to log each method entry and method exit.
 * If LogService is unavailable System.out is used to log that information.
 * It is useful in case of unit tests.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 */
public aspect Logging {
	pointcut traceMethods()
        : (execution(* *(..)) && !within(Logging));

	before() : traceMethods() {
		log("INVOKING METHOD " + thisJoinPoint.getSignature() + " AT " + thisJoinPoint.getSourceLocation() + " WITH ARGS: " + getMethodArgs(thisJoinPoint));
	}

	after() returning (Object value): traceMethods() {
		if (thisJoinPoint.getSignature().toString().startsWith("void "))
			value = "void";
		
		log("METHOD " + thisJoinPoint.getSignature() + " RETURNS " + value + " AT " + thisJoinPoint.getSourceLocation());
	}
	
	after() throwing (Exception ex) : traceMethods() {
		log("METHOD " + thisJoinPoint.getSignature() + " THROWS " + ex.toString() + " AT " + thisJoinPoint.getSourceLocation());
	}

	private void log(String text) {
		final Bundle bundle = FrameworkUtil.getBundle(LogService.class);
		if (bundle == null) {
			System.out.println(text);
			return;
		}
		final ServiceTracker tracker = new ServiceTracker(bundle.getBundleContext(),
				"org.osgi.service.log.LogService", null);
		tracker.open();
		final LogService logger = (LogService) tracker.getService();
		if (logger == null)
			System.out.println(text);
		else
			logger.log(LogService.LOG_DEBUG, text);
		tracker.close();
	}

	private String getMethodArgs(JoinPoint joinPoint) {
		final StringBuffer buffer = new StringBuffer(); 
		buffer.append("(");
		boolean firstTime = true;
		for (Object arg : joinPoint.getArgs()) {
			if (firstTime)
				firstTime = false;
			else
				buffer.append(", ");

			buffer.append(arg);
		}
		buffer.append(")");
		
		return buffer.toString();
	}
}