package eu.novi.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

public class RESTCommunication {

	private String response = "";
	

	/**
	 * Methods executes POST communication on the given address where REST service is running. 
	 * @param restAddress URL address of the REST service to call. For example http://planetlab:8080/request/handler/delete
	 * @param requestParameters The pairs contains keys and values of the request parameters. For example for POST message in URL "http://planetlab:8080/request/handler/delete?sliceId=1234" the key is "sliceId" and
	 * @throws HttpException 
	 * @throws IOException
	 */
	public void executePostMethod(String restAddress, Map<String, String> requestParameters) throws HttpException, IOException {
		PostMethod postMethod = preparePostMethod(restAddress, requestParameters);
		response = getResponseFromService(postMethod);
	}

	/**
	 * Prepare POST method with the given parameters
	 * @param restAddress
	 * @param key
	 * @param value
	 * @return
	 */
	private PostMethod preparePostMethod(String restAddress, Map<String, String> requestParameters) {
		Part[] parts = generateParts(requestParameters);
		PostMethod postMethod = new PostMethod(restAddress);
		postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));

		return postMethod;
	}
	
	private Part[] generateParts(Map<String, String> keyValuePairs) {
		Part[] parts = new Part[keyValuePairs.size()];
		int i = 0;
		for(String key : keyValuePairs.keySet()){
			parts[i++] = new StringPart(key, keyValuePairs.get(key));
		}
		return parts;
	}

	private String getResponseFromService(PostMethod postMethod) throws HttpException, IOException {
		String resp = "";
		final HttpClient client = new HttpClient();
		if (client.executeMethod(postMethod) != -1) {
			resp = getResponseFromStream(postMethod.getResponseBodyAsStream());
			postMethod.releaseConnection();
		}
		return resp;
	}


	public String getResponseFromStream(InputStream responseBodyAsStream) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(responseBodyAsStream));
		StringBuilder responseBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
				responseBuilder.append(line);
		}
		return responseBuilder.toString();
	}
	
	
	public String getResponse() {
		return response;
	}

}
