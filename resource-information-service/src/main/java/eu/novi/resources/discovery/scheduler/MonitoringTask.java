package eu.novi.resources.discovery.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.resources.discovery.database.UpdateAvailability;
import eu.novi.resources.discovery.util.RisSystemVariables;

public class MonitoringTask implements Runnable {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(MonitoringTask.class);
	
	@Override
	public void run() {
		
		if (!RisSystemVariables.isUpdateMonValuesPeriodic())
		{
			log.info("The UpdateMonValuesPeriodic variable is false, I will not update the " +
					"monitoring values");
			return ;
		}

		System.out.println("I will run the updateAllMonitoringValues ");
		UpdateAvailability update = new UpdateAvailability();
		update.updateAllMonitoringValues();
	}

}
