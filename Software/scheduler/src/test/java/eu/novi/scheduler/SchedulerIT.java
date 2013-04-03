package eu.novi.scheduler;

import static org.junit.Assert.assertTrue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;

import eu.novi.framework.IntegrationTesting;

@RunWith(JUnit4TestRunner.class)
public class SchedulerIT {

	private static void waitSafe() {
		try {
			 Thread.currentThread().sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Configuration 
	public static Option[] configuration() throws Exception {
		return IntegrationTesting.createConfigurationWithBundles("org.osgi.compendium");
	}

	@Test
	public void testInParallelRunnableClass(BundleContext ctx) throws Exception {
		waitSafe();
		ctx.getServiceReferences(null, null);
		
		ScheduledExecutorService scheduler = (ScheduledExecutorService) ctx.getService(ctx.getServiceReference(ScheduledExecutorService.class.getName()));
		scheduler.submit(new NoviTaskRunnable(1));
		System.out.println("End of main thread after NOVI Task 1 run");
	}
	
	@Test
	public void testInParallelRunnableClassWithResultValue(BundleContext ctx) throws Exception {
		String result = "Main Thread: the result that says NOVI Task 2 is done";
		waitSafe();
		ctx.getServiceReferences(null, null);
		
		ScheduledExecutorService scheduler = (ScheduledExecutorService) ctx.getService(ctx.getServiceReference(ScheduledExecutorService.class.getName()));
		Future<String> task = scheduler.submit(new NoviTaskRunnable(2), result);
		System.out.println("Main thread after NOVI Task 2 run");
		System.out.println(task.get());
		
		assertTrue(task.isDone());
	}

	
	@Test
	public void testInParallelCallableClassWithReturningValue(BundleContext ctx) throws Exception {
		waitSafe();
		ctx.getServiceReferences(null, null);
		
		ScheduledExecutorService scheduler = (ScheduledExecutorService) ctx.getService(ctx.getServiceReference(ScheduledExecutorService.class.getName()));
		Future<String> task = scheduler.submit(new NoviTaskCallable(3));
		System.out.println("Main thread after NOVI Task 3 run");
		System.out.println("Main thread: Result returned by NOVI Task 3:" + task.get());
		
		assertTrue(task.isDone());
	}
	
	@Test
	public void testInParallelScheduletdTask(BundleContext ctx) throws Exception{
		waitSafe();
		ctx.getServiceReferences(null, null);
		
		ScheduledExecutorService scheduler = (ScheduledExecutorService) ctx.getService(ctx.getServiceReference(ScheduledExecutorService.class.getName()));
		scheduler.schedule(new NoviTaskRunnable(4) , 2000 , TimeUnit.MILLISECONDS);
		System.out.println("Main thread after NOVI Task 4 scheduled");
	}
	
	@Test
	public void testInParallelScheduledPeriodicTaskAndCancelItAfterSomeTime(BundleContext ctx) throws Exception{
		waitSafe();
		ctx.getServiceReferences(null, null);
		
		ScheduledExecutorService scheduler = (ScheduledExecutorService) ctx.getService(ctx.getServiceReference(ScheduledExecutorService.class.getName()));
		ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(new PeriodicTask() , 1, 2, TimeUnit.SECONDS);
		System.out.println("Main thread after NOVI Task 5 scheduled");
		Thread.sleep(7000);
		task.cancel(true);
		
		assertTrue(task.isCancelled());
		assertTrue(task.isDone());
	}
	 
	
	@Test
	public void testInParallelNewRunnableClass(BundleContext ctx) throws Exception {
		waitSafe();
		ctx.getServiceReferences(null, null);
		
		ScheduledExecutorService scheduler = (ScheduledExecutorService) ctx.getService(ctx.getServiceReference(ScheduledExecutorService.class.getName()));
		scheduler.submit(new Runnable() {			
			@Override
			public void run() {
				System.out.println("NOVI Task started");
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("NOVI Task done");
				
			}
		});
		
		System.out.println("End of main thread after NOVI Task run");
	}
	
	
	private  class NoviTaskRunnable implements Runnable{
		private int i;
		
		public NoviTaskRunnable(int i){
			this.i = i;
		}
		
		@Override
		public void run() {
			System.out.println("NOVI Task "+i+" started");
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("NOVI Task "+i+" done");
		}
	}

	
	
	private class NoviTaskCallable implements Callable<String>{
		private int i;
		
		public NoviTaskCallable(int i){
			this.i = i;
			
		}
		
		@Override
		public String call() throws Exception {
			System.out.println("NOVI Task "+i+" started");
			Thread.sleep(4000);
			return "NOVI Task "+i+" done";
		}	
	}
	
	private class PeriodicTask implements Runnable{
		@Override
		public void run() {
			System.out.println("NOVI periodic task started");
			System.out.println("NOVI periodic task done");
		}

	}

}
