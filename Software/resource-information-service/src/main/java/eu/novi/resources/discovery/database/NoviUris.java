package eu.novi.resources.discovery.database;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.novi.im.util.UrisUtil;


/**
 *a class for creation and manipulation of NOVI URIs
 * 
 * @author <a href="mailto:c.pittaras@uva.nl">Chariklis Pittaras</a>
 *
 */
public class NoviUris {
	
	private static final transient Logger log = 
			LoggerFactory.getLogger(NoviUris.class);
	
	//base addresses
	public static final String NOVI_IM_BASE_ADDRESS = UrisUtil.getNoviImBaseAddress();
	public static final String NOVI_POLICY_BASE_ADDRESS = UrisUtil.getNoviPolicyBaseAddress();
	public static final String RDF_PREFIX = UrisUtil.getRdfPrefix();
	public static final String NOVI_UNIT_ADDRESS = UrisUtil.getNoviUnitAddress();

	
	////////specific prefixed used in RIS//////
	//the uri of the lifetime is the  sliceURI + LIFETIME_SUFFIX, i.e. slice_34837_expiration-lifetime
	private final static String LIFETIME_SUFFIX = "_expiration-lifetime";
	private final static String SLICE_MANIFEST_SUFFIX = "_manifest";
	
	
	/**
	 * It create an URI appended the name at the
	 * novi URI "http://fp7-novi.eu/im.owl#"
	 * @param name -- the name to append
	 * @return the URI
	 */
	protected static final URI createNoviURI(String name)
	{
		return NoviUris.createURI(NOVI_IM_BASE_ADDRESS + name);
		
	}


	/**
	 * It create an URI appended the name at the
	 * novi URI "http://fp7-novi.eu/im.owl#"
	 * @param name -- the name to append
	 * @return the URI
	 */
	protected static final URI createPolicyURI(String name)
	{
		return NoviUris.createURI(NOVI_POLICY_BASE_ADDRESS + name);
		
	}


	/**
	 * It create an URI appended the name at the
	 * rdf URI "http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	 * @param name-- the name to append
	 * @return the URI
	 */
	protected static final URI createRdfURI(String name)
	{
		return NoviUris.createURI(RDF_PREFIX + name);
	}
	
	
	/**in reserve slice the slice manifest, that is got by request handler, 
	 * it is stored with this context
	 * @param sliceUri the URI of the slice
	 * @return the manifest context of this slice
	 */
	public static final URI getSliceManifestContextUri(String sliceUri)
	{
		return createURI(sliceUri + SLICE_MANIFEST_SUFFIX);
	}


	/**
	 * It create an URI with the given string
	 * @param name -- the name of the URI
	 * @return the URI or null if an error occur
	 */
	protected static final URI createURI(String name)
	{
		URI uri = null;
		if (name == null)
		{
			log.debug("The URI {} is null", name);
			return null;
		}
		
		try
		{
			ValueFactory f = ConnectionClass.getValueFactory();
			uri = f.createURI(name);
			
		}
		catch (IllegalArgumentException e)
		{
			ConnectionClass.logErrorStackToFile(e);
			
		}
	
		return uri;
		
	}


	/**replace the abbreviation im, rdf, pl and unit with the full URL
	 * @param uri
	 * @return the the full URI, or null of the parameter was null
	 */
	protected static String checkAbbreviation(String uri)
	{
		if (uri == null)
		{
			return null;
		}
		
		String answer = uri;
		if (uri.startsWith("im:"))
		{
			log.debug("The URI {} start with im:", uri);
			answer = uri.replaceFirst("im:", NOVI_IM_BASE_ADDRESS);
			
		}
		else if (uri.startsWith("rdf:"))
		{
			log.debug("The URI {} start with rdf:", uri);
			answer = uri.replaceFirst("rdf:", RDF_PREFIX);
		}
		else if (uri.startsWith("pl:"))
		{
			log.debug("The URI {} start with pl:", uri);
			answer = uri.replaceFirst("pl:", NOVI_POLICY_BASE_ADDRESS);
		}
		else if (uri.startsWith("unit:"))
		{
			log.debug("The URI {} start with unit:", uri);
			answer = uri.replaceFirst("unit:", NOVI_UNIT_ADDRESS);
		}
		else
			log.debug("The URI {} doesn't have abbreviation", uri);
		
		return answer;
	}
	
	
	/**given the sliceID it creates the slice URI.
	 * It calls the function in reserveSlice
	 * @param sliceID the slice id, is a 32 bits number
	 * @return
	 */
	public static String createSliceURI(String sliceID)
	{
		return ReserveSlice.createSliceURI(sliceID);
	}
	
	/**all the information that are got from the testbed are stored with this contexts URI
	 * @return
	 */
	public static URI getSubstrateURI()
	{
		return ManipulateDB.TESTBED_CONTEXTS;
	}
	
	/**The name of the substrate context.
	 * all the information that are got from the testbed are stored with this contexts
	 * @return
	 */
	public static String getSubstrateContextName()
	{
		return ManipulateDB.TESTBED_CONTEXTS_STR;
	}
	
	
	/**get the slice lifetime for the given slice URI.
	 * Every slice has a lifetime that indicate when this slice will expire 
	 * @param sliceURI
	 * @return
	 */
	public static URI getSliceLifetimeURI(String sliceURI)
	{
		return createURI(sliceURI + LIFETIME_SUFFIX);
		
	}
	
	
	

}
