package eu.novi.framework;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test case for the regular expression used to find the bundle under test.
 * 
 * @author <a href="blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 *
 */
public class FilenameFilterTest {

	private static final String TESTS_JAVADOC_JAR = "[a-zA-Z0-9.-]+-(tests|javadoc).jar";
	private static final String REGULAR_JAR = "[a-zA-Z0-9.-]+.jar";
	
	@Test
	public void checkTests() {
		assertTrue("framework-0.1.0-SNAPSHOT-tests.jar".matches(TESTS_JAVADOC_JAR));
	}
	
	@Test
	public void checkJavadoc() {
		assertTrue("framework-0.1.0-SNAPSHOT-javadoc.jar".matches(TESTS_JAVADOC_JAR));
	}
	
	@Test
	public void checkJarNotMatchesTestsOrJavadocJar() {
		assertFalse("framework-0.1.0-SNAPSHOT.jar".matches(TESTS_JAVADOC_JAR));
	}
	
	@Test
	public void checkJarMatchesRegularJar() {
		assertTrue("framework-0.1.0-SNAPSHOT.jar".matches(REGULAR_JAR));
	}

}
