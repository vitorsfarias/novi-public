package eu.novi.scheduler;

import java.util.concurrent.Executors;

/**
 * 
 * Factory for creating threads.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 *
 */
public class ThreadFactory implements java.util.concurrent.ThreadFactory {

	private java.util.concurrent.ThreadFactory factory = Executors.defaultThreadFactory();
	
	@Override
	public Thread newThread(Runnable task) {
		Thread result = factory.newThread(task);
		result.setName("NOVI " + result.getName());
		return result;
	}

}
