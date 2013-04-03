/**
*
* Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project
* Copyright according to BSD License
* For full text of the license see: ./novi/Software/Monitoring/MonitoringTool/PacketTracking/license.txt
*
* @author <a href="mailto:ramon.masek@fokus.fraunhofer.de">Ramon Masek</a>, Fraunhofer FOKUS
* @author <a href="mailto:c.henke@tu-berlin.de">Christian Henke</a>, Technical University Berlin
* @author <a href="mailto:carsten.schmoll@fokus.fraunhofer.de">Carsten Schmoll</a>, Fraunhofer FOKUS
* @author <a href="mailto:Julian.Vetter@campus.tu-berlin.de">Julian Vetter</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Jens Krenzin</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Michael Gehring</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Tacio Grespan Santos</a>, Fraunhofer FOKUS
* @author <a href="mailto:">Fabian Wolff</a>, Fraunhofer FOKUS
*
*/

package de.fhg.fokus.net.ipfix.mgen;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.fokus.net.ipfix.mojo.mgen.TextTemplate;

public class TextTemplateTest {
	private final static Logger logger = LoggerFactory.getLogger(TextTemplate.class);
	private final static File BASE_DIR = new File("target","test-classes");
	private static TextTemplate tmpl;
	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		logger.debug("setup");
		tmpl = new TextTemplate(new File(BASE_DIR,"Test.tmpl"));
		
	}
	@Test
	public void testGenerateFile() throws IOException {
		File destFile = new File(BASE_DIR,"Text.txt");
		Map<String, String> replacements = new HashMap<String, String>();
		replacements.put("replaceMe", "Hi");
		replacements.put("replaceMeToo", "there!");		
		tmpl.generateFile(replacements, destFile);
		// TODO complete test
	}

}
