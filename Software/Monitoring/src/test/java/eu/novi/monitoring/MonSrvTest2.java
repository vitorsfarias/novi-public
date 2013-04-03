package eu.novi.python.integration;

import static org.junit.Assert.*;
import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import eu.novi.monitoring.*;

/**
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 */
public class MonSrvTest2 {
	static {
		//setting fake bundle repository directory location
		JythonObjectFactory.rootDirectory
	        = "src" + File.separator + "test" + File.separator + "resources";
	}
	
	protected BundleContext context;

	@Before
	public void givenBundleContext() {
		final Bundle bundle = Mockito.mock(Bundle.class);
		Mockito.when(bundle.getBundleId()).thenReturn(1L);
		context = Mockito.mock(BundleContext.class);
		Mockito.when(context.getBundle()).thenReturn(bundle);
	}
	
	@After
	public void destroy() {
		context = null;
	}
	
	@Test
	public void whenCreatingPythonObject() throws ClassNotFoundException {
		final MonSrv hello = JythonObjectFactory.createObject(MonSrv.class, "Service.MonSrvImpl", context);
		assertEquals("Hello", hello.echo("Hello"));
	}
}
