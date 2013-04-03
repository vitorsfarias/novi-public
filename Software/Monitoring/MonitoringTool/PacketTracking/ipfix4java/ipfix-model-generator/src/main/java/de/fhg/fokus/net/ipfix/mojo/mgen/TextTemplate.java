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

package de.fhg.fokus.net.ipfix.mojo.mgen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple text template engine using {@link Matcher}. 
 * Tokens are specified by %{sometoken}% . The predefined 
 * tokens are:
 * <pre>
 * %{dateTime}     current date time
 * 
 * </pre>
 * 
 * @author FhG-FOKUS NETwork Research
 * 
 */
public final class TextTemplate {
	// -- constants --
	private enum PredefinedTags {
		dateTime
	}
	// -- model --
	private final String template;
	private final Pattern pattern = Pattern.compile("\\%\\{(.+?)\\}\\%");
	public abstract class Tag {
		private final String name;
		public Tag(String name) {
			this.name = name;
		}
		/**
		 * 
		 * @return tag name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @return tag value
		 */
		public abstract String getValue();
	}
	private final Map<String, Tag> mapPredefinedTags = new ConcurrentHashMap<String, Tag>();

	public TextTemplate(String template ){
		this.template = template;
	}
	public TextTemplate(File templateFile) throws IOException {
		this.template = readFileContents(templateFile);
		setupPredefinedTags();
	}
	private void setupPredefinedTags(){
		// DateTime
		registerDynamicTag(new Tag(PredefinedTags.dateTime+"") {
			@Override
			public String getValue() {
				return new Date().toString();
			}
		});
	}
	public void registerDynamicTag(Tag tag){
		mapPredefinedTags.put(tag.getName(), tag);
	}

	public static String readFileContents(File file) throws IOException {
		StringBuilder buf = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null) {
			buf.append(line);
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * Generate file using the replacements map
	 * 
	 * @param replacements
	 * @param destFile
	 * @throws IOException
	 */
	public void generateFile(Map<String, String> replacements, File destFile)
			throws IOException {
		writeFile(destFile, process(replacements));
	}

	/**
	 * 
	 * From http://stackoverflow.com/questions/959731/how-to-replace-a-set-of-
	 * tokens-in-a-java-string
	 * 
	 * @param replacements
	 * @return generated text
	 */
	private String process(Map<String, String> replacements) {
		Matcher matcher = pattern.matcher(template);

		// populate the replacements map ...
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String token = matcher.group(1);
			String replacement = replacements.get(token);
			// process predefined tags
			if(replacement==null){
				Tag tag = mapPredefinedTags.get(token);
				if(tag!=null){
					replacement = tag.getValue();
				}
			}
			if(replacement!=null){
				matcher.appendReplacement(sb, replacement);
			}
		}
		matcher.appendTail(sb);
		return sb.toString();

	}

	private void writeFile(File dstFile, String contents) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(dstFile));
		out.write(contents);
		out.close();
	}
	@Override
	public String toString() {
		return template;
	}
}
