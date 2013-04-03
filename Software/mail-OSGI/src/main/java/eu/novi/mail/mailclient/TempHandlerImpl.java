package eu.novi.mail.mailclient;
public class TempHandlerImpl implements TempHandler {
	private static final String SYSTEM_INSTANCE_LOCATION = "http://localhost:8080";
	//private static final transient Logger log = 
	//		LoggerFactory.getLogger(TempHandlerImpl.class);
	@Override
	public int sentEmailEvent(String receiver, String information) {
	//	log.info("CXF REST endpoint Accepting Event from the MAIL component");
		SSendEmail emailClient = new SSendEmail();
		//emailClient.SendEmail("ykryftis@netmode.ece.ntua.gr","Testing the email");
		emailClient.SendEmail(receiver,information);
		System.out.println("DONE");
		return 0;
	}
}
