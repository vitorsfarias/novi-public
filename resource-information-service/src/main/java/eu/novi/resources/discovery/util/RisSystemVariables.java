package eu.novi.resources.discovery.util;
/**
 * A class that contain variables that are control the functionalities and behaviour of RIS
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class RisSystemVariables {
	
	private static boolean updateMonValuesPeriodic = false;
	
	private static boolean useStoredMonUtilizationValues = false;

	
	
	/**if true then RIS, dosen't call monitoring for the utilization values but
	 * it calculate those from the  monitoring values in the ris cache.
	 * To be true need also the updateMonValuesPeriodic to be true
	 * @return
	 */
	public static boolean isUseStoredMonUtilizationValues() {
		return (useStoredMonUtilizationValues && updateMonValuesPeriodic);
	}


	/**
	 * if true then RIS, dosen't call monitoring for the utilization values but
	 * it calculate those from the  monitoring values in the ris cache.
	 * To happen this need also the updateMonValuesPeriodic to be true
	 * @param useStoredMonUtilizationValues
	 */
	public static void setUseStoredMonUtilizationValues(
			boolean useStoredMonUtilizationValues) {
		RisSystemVariables.useStoredMonUtilizationValues = useStoredMonUtilizationValues;
	}


	/**if true, then RIS updates the monitoring values periodic.
	 * @return
	 */
	public static boolean isUpdateMonValuesPeriodic() {
		return updateMonValuesPeriodic;
	}

	
	/**if true, then RIS updates the monitoring values periodic.
	 * the default is false
	 * @param updateMonValuesPeriodic
	 */
	public static void setUpdateMonValuesPeriodic(boolean updateMonValuesPeriodic) {
		RisSystemVariables.updateMonValuesPeriodic = updateMonValuesPeriodic;
	}
	
	
	

}
