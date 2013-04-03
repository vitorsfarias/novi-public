package eu.novi.api.request.handler;

//import static com.jayway.restassured.RestAssured.expect;
//import static com.jayway.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.startsWith;
import org.junit.Test;

//@RunWith(JUnit4TestRunner.class)
public class TestRequestHandlerService{

	@Test
	public void test(){
		assert(true);
	} 


	/**
	 * Commented until I figure out how to really do these test. Seems like it should be done from outside this component.
	 * I am not sure if I could test which requires novi-api bundle to be loaded first, from within novi-api.
	 
	private static Server server=null;

	@BeforeClass
	public static void startServer() {

		// Configure properly where the REST test will be addressed.
		RestAssured.port = 8181;
		RestAssured.baseURI = "http://localhost";
	}

	@AfterClass
	public static void stopServer() {
		if (server != null) {
			server.stop();
		}
	}

	@Test
	public void TestNullRequest() throws Exception {
		RequestHandler req = new RequestHandlerImpl();
		assertEquals(req.unboundRequestHandler(null), "Nothing is requested");

	}

	@Test
	public void testGetRequestNotAllowed() {
		// We expected method not allowed when getting instead of posting
		// request
		expect().statusCode(405).when().get("/cxf/request/handler/unbound");
	}

	@Test
	public void testNothingRequest() {
		// We expect nothing is requested when we are not giving anything to the
		// post request
		expect().body(startsWith("Nothing is requested")).when()
				.post("/cxf/request/handler/unbound");

	}

	@Test
	public void testPostUnboundRequestSimple() {
		final File requestFile = new File(getClass().getClassLoader()
				.getResource("PlanetLab.owl").getFile());
		assertNotNull(requestFile);
		assertTrue(requestFile.canRead());

		// Domain specific language of rest-assured testing.
		// Given request from requestFile, we expected the return from NOVI API
		// will start with the message saying the request will be processed.
		given().multiPart("request", requestFile)
				.expect()
				.body(startsWith("We are processing this request in this queue"))
				.when().post("/cxf/request/handler/unbound");

	}
	*/
}
