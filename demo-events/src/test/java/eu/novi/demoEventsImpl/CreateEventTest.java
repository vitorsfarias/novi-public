package eu.novi.demoEventsImpl;

import org.junit.Test;

import eu.novi.demoEventsImpl.CreateEvent;

public class CreateEventTest {
	@Test
	public void createNothingTest()
	{
		CreateEvent event= new CreateEvent();
		event.create();
	}

}
