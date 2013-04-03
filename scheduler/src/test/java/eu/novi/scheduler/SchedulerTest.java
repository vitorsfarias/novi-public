package eu.novi.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;

public class SchedulerTest {
	private final ScheduledThreadPoolExecutor scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10, new ThreadFactory());
	private final Callable<String> task = new Callable<String>() {
		@Override
		public String call() throws Exception {
			return "hello";
		}
	};
	
	Future<String> taskResult;
	
	@Before
	public void whenSubmittingTask() {
		taskResult = scheduler.submit(task);
	}
	
	@Test
	public void shouldReturnHello() throws Exception {
		assertEquals("hello", taskResult.get());
	}
	
	@Test
	public void nameShouldStartWithNOVI() {
		assertTrue(new ThreadFactory().newThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			} }).getName().startsWith("NOVI"));
	}
}
