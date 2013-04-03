package eu.novi.scheduler;

import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

public class DeadlockTest {

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	@Test(expected=TimeoutException.class)
	public void shouldThrowTimeoutException() throws Exception {
		scheduler.submit(createTaskA()).get(5, TimeUnit.SECONDS);
	}

	private Callable<String> createTaskA() {
		return new Callable<String>() {
			@Override
			public String call() {
				try {
					return scheduler.submit(createTaskB()).get();
				} catch (Exception ex) {
					fail(ex.toString());
				}
				
				return null;
			}
		};
	}
	
	private Callable<String> createTaskB() {
		return new Callable<String>() {

			@Override
			public String call() throws Exception {				
				return "It will not be returned.";
			}
			
		};
	}

}
