package eu.novi.api.request.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.Before;
import org.junit.Test;

import eu.novi.api.request.queue.RequestQueueListener;
import eu.novi.im.core.Topology;
import eu.novi.im.util.IMRepositoryUtilImpl;

public class PiotrTest {

	@Before
	public void setup() throws Exception{
		BrokerService broker = new BrokerService();
		broker.setUseJmx(false);
		broker.addConnector("tcp://localhost:61616");
		broker.setUseShutdownHook(false);
		broker.start();
	}
	
	@Test
	public void handleCreateSliceTest() throws IOException, JMSException{
		RequestQueueListener rql = new RequestQueueListener();
		URL url = this.getClass().getResource("/req1.owl");
		File file = new File(url.getPath());
		String owlFileContent = readFile(file);
		
		TextMessage textMessage = new TextMessageImpl();
		textMessage.setText(owlFileContent);
	
		rql.handleCreateSlice(textMessage);
	}
	
	
	private static String readFile(File file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String currentLine = reader.readLine();
		StringBuilder result = new StringBuilder();
		while(currentLine != null){
				result.append(currentLine);
				result.append("\n");
				currentLine = reader.readLine();
		}
		return result.toString();
	}
	
	private Topology getTopologyFromFile(String fileName) throws IOException{
		IMRepositoryUtilImpl im = new IMRepositoryUtilImpl();
		String owlFileContent;
		try {
			URL url = this.getClass().getResource("/"+fileName);
			File file = new File(url.getPath());
			owlFileContent = readFile(file);
		} catch (IOException e) {
			throw e;
		}
		return im.getTopologyFromFile(owlFileContent);
	}
	
}
