package eu.novi.resource.discovery.scheduler;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.resources.discovery.scheduler.PeriodicUpdate;


public class ThreadManagementTest {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(ThreadManagementTest.class);

	//@Test
	public void test() {
		Future<String>  task1Ans = PeriodicUpdate.executeNewThread(new Task1());
		Future<String>  task2Ans = PeriodicUpdate.executeNewThread(new Task2());
		
		try {
			log.info("Results from 2: {}", task2Ans.get());
			log.info("Results from 1: {}", task1Ans.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("This is father, my job is finished, bye bye");
	}
	
	
	private  class Task1 implements Callable<String>
	{

		@Override
		public String call() throws Exception {
			log.debug("This is task 1 and now I am going to sleep");
			Thread.sleep(12000);
			return "This is task 1 and I am done";
		}
		
	}
	
	
	private  class Task2 implements Callable<String>
	{

		@Override
		public String call() throws Exception {
			log.debug("This is task 2 and I will not sleep");
			return "This is task 2 and I am done";
		}
		
	}

}
