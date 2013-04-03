package eu.novi.monitoring.util;

public interface MonitoringQuery {

	public void addFeature( String queryName, String feature);
	public void addResource( String queryName, String resourceName, String resourceType);
	public void addInterface( String resourceName, String interfaceName, String interfaceType);
	public void defineInterface(String interfaceName, String address, String addressType);
	public String serialize();
}

