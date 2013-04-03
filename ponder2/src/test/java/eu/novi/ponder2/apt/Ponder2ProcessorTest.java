package eu.novi.ponder2.apt;

import java.lang.reflect.Field;

import junit.framework.TestCase;


import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;

import eu.novi.ponder2.apt.Ponder2Processor;

public class Ponder2ProcessorTest extends TestCase {

	public Ponder2ProcessorTest(String name) {
		super(name);
	}

	public void testPonder2Processor() {
		Mockery m = new Mockery() {
			{
				setImposteriser(ClassImposteriser.INSTANCE);
			}
		};
		final AnnotationProcessorEnvironment env = m
				.mock(AnnotationProcessorEnvironment.class);
		final Filer filer = m.mock(Filer.class);
		try {
			m.checking(new Expectations() {
				{
					one(env).getFiler();
					will(returnValue(filer));
				}
			});
			Ponder2Processor p = new Ponder2Processor(env);
			m.assertIsSatisfied();
			Field f = Ponder2Processor.class.getDeclaredField("env");
			f.setAccessible(true);
			assertEquals(f.get(p), env);
			f = Ponder2Processor.class.getDeclaredField("filer");
			f.setAccessible(true);
			assertEquals(f.get(p), filer);
		} catch (Exception e) {
			fail(e.toString());
		}
	}

	public void testProcess() {
		// fail("Not yet implemented");
	}

}
