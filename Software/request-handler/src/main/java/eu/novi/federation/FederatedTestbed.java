package eu.novi.federation;

/**
 * Represents the combined testbed.
 * 
 * Federation strategies must implement it e.g. SFA.
 * 
 * @author <a href="mailto:blazej.pietrzak@man.poznan.pl">Blazej Pietrzak</a>
 *
 */
public interface FederatedTestbed {

	void configure(Slice slice);
}
