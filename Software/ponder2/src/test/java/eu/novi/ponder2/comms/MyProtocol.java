package eu.novi.ponder2.comms;

import java.net.URI;

import eu.novi.ponder2.comms.Protocol;
import eu.novi.ponder2.exception.Ponder2RemoteException;

public class MyProtocol implements Protocol {

	public void install(URI address, URI remote) throws Ponder2RemoteException {
		URI uri = null;
		try {
			uri = new URI("http://Fred/Foo");
		} catch (Exception e) {
		}
		if (!address.equals(uri))
			throw new Ponder2RemoteException("wrong URI");
	}
}
