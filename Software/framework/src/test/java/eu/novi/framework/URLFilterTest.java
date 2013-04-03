package eu.novi.framework;

import static org.junit.Assert.*;

import org.junit.Test;

public class URLFilterTest {

	@Test
	public void testSampleService() {
		assertEquals("eu/novi/sample/SampleService", "eu.novi.sample.SampleService".replaceAll("[.]", "/"));
	}

}
