package eu.novi.framework;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.waitForFrameworkStartup;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.karaf.testing.Helper;
import org.ops4j.pax.exam.Option;

/**
 * 
 * Helper class for integration testing.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 *
 */
public class IntegrationTesting {
	
	/**
	 * Returns the default configuration for the integration testing framework.
	 * It is possible to specify bundles that are to be installed by providing
	 * their Maven artifactIds. Bundle under test is automatically installed.
	 */
	public static Option[] createConfigurationWithBundles(String... bundleArtifactIds) {
		return combine(
	        Helper.getDefaultOptions(systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("ERROR")),
	        workingDirectory("target/paxrunner"),
	        waitForFrameworkStartup(), felix(),
	        bundle(findBundle()),
	        scanFeatures(new File("target/features.xml").toURI().toString(),
	            bundleArtifactIds
	        )
	    );
	}

	private static String findBundle() {
		final FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches("[a-zA-Z0-9.-]+.jar")
				  && (!name.matches("[a-zA-Z0-9.-]+-(tests|javadoc).jar"));
			}
			
		};
		return new File("target").listFiles(filter)[0].toURI().toString();
	}
}