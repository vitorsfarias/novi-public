package eu.novi.ponder2.apt;

import java.lang.reflect.Field;
import java.util.Collection;

import junit.framework.TestCase;


import org.jmock.Expectations;
import org.jmock.Mockery;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

import eu.novi.ponder2.apt.Ponder2Factory;
import eu.novi.ponder2.apt.Ponder2Processor;

public class Ponder2FactoryTest extends TestCase {

	public Ponder2FactoryTest(String name) {
		super(name);
	}

	public void testSupportedAnnotationTypes() {
		Ponder2Factory fact = new Ponder2Factory();
		Collection<String> c = fact.supportedAnnotationTypes();
		assertEquals(c.size(), 2);
		assertTrue(c.contains("eu.novi.ponder2.apt.Ponder2op"));
		assertTrue(c.contains("eu.novi.ponder2.apt.Ponder2notify"));
	}

	public void testSupportedOptions() {
		assertTrue(new Ponder2Factory().supportedOptions().isEmpty());
	}

	public void testGetProcessorFor() {
		Mockery m = new Mockery();
		final AnnotationProcessorEnvironment env = m
				.mock(AnnotationProcessorEnvironment.class);
		m.checking(new Expectations() {
			{
				one(env).getFiler();
				will(returnValue(null));
			}
		});
		Ponder2Processor p = (Ponder2Processor) new Ponder2Factory()
				.getProcessorFor(null, env);
		m.assertIsSatisfied();
		assertNotNull(p);
		try {
			Field f = Ponder2Processor.class.getDeclaredField("env");
			f.setAccessible(true);
			assertEquals(f.get(p), env);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

}
