package eu.novi.nswitch.exceptions;

/**
 * Exception thrown when NSwitch components finds errors in Topology structure
 * @author pikusa
 *
 */
public class IncorrectTopologyException extends FederationException {

	public IncorrectTopologyException() {
	}

	public IncorrectTopologyException(String description) {
		super(description);
	}
}
