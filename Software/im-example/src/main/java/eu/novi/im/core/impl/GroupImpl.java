package eu.novi.im.core.impl;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlSeeAlso;

import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;

import eu.novi.im.core.Group;
import eu.novi.im.core.Lifetime;
import eu.novi.im.core.Resource;
import eu.novi.im.util.IMUtil;

@XmlSeeAlso({ PlatformImpl.class, TopologyImpl.class, ReservationImpl.class})
public class GroupImpl implements Group, RDFObject {

	private String uri;
	private Set<? extends Resource> contains;
	private Set<? extends Lifetime> hasLifetimes;
	private Boolean autoUpdateOnfailure = false;
	
	/**the id of the object with out the novi IM base address
	 * @param uri
	 */
	public GroupImpl(String uri)
	{
		this.uri = Variables.checkURI(uri);
		
	}
	
	/** default constructor **/
	public GroupImpl(){}
	
	@Override
	public Boolean getAutoUpdateOnfailure() {
		return this.autoUpdateOnfailure;
	}

	@Override
	public void setAutoUpdateOnfailure(Boolean autoUpdateOnfailure) {
		this.autoUpdateOnfailure = autoUpdateOnfailure;
		
	}
	
	
	@Override
	public Set<Resource> getContains() {
		return (Set<Resource>) this.contains;
	}

	
	/**for each resource in the Set it sets also the reverse
	 * relation Resource isContainedIn thisGroup. 
	 * If you want to add new resources then you have to add the 
	 * resources to the set and to call again the setContains,
	 * otherwise the data will be inconsistent
	 * @param contains
	 */
	@Override
	public void setContains(Set<? extends Resource> contains) {
		//To make sure that this contains relation and iscontained in is synchronized
		//Whenever we set a new resource to be contained here, 
		//we make sure isContainedIn is also filled with this value.
		
		/*if (this.contains != null)
		{
			//the set is not null, remove the links from the
			//previous resources
			for (Resource r : this.contains)
			{
				Set<Group> group = r.getIsContainedIn();
				if (group != null)
				{
					group.remove(this);
				}
			}
		}

		if (contains != null)
		{
			for(Resource r : contains){
				Set<Group> g = r.getIsContainedIn();
				if(g == null){
					g = new HashSet<Group>();
				}
				g.add(this);
				r.setIsContainedIn(g);

			}
			
		}*/


		IMUtil.reverseProperty(contains, this.contains, this, "IsContainedIn");
		
	
		this.contains = contains;
	}
	
	

	@Override
	public Set<Lifetime> getHasLifetimes() {
		return (Set<Lifetime>) this.hasLifetimes;
	}

	@Override
	public void setHasLifetimes(Set<? extends Lifetime> hasLifetimes) {
		this.hasLifetimes = hasLifetimes;

	}
	
	@Override
	public String toString() {
		return uri;
	}

	public void copy(Group toCopy) {
		Set<LifetimeImpl> lifeTimeImpls = new HashSet<LifetimeImpl>();
		for (Lifetime lt : toCopy.getHasLifetimes()) {
			LifetimeImpl lti = new LifetimeImpl(getId(lt.toString()));
			lti.copy(lt);
			lifeTimeImpls.add(lti);
		}
		Set<ResourceImpl> groupResources = new HashSet<ResourceImpl>();
		for (Resource res : toCopy.getContains()) {
			ResourceImpl resImpl = new ResourceImpl(getId(res.toString()));
			resImpl.copy(res);
			groupResources.add(resImpl);
		}
		this.setHasLifetimes(lifeTimeImpls);
		this.setContains(groupResources);
	}
	
	private String getId(String string) {
		String[] components = string.toString().split("\\#");
		return components[components.length-1];
	}


	@Override
	public ObjectConnection getObjectConnection() {
		
		return null;
	}


	@Override
	public URIResourceImpl getResource() {
		return new URIResourceImpl(uri);
	}
	
	public void setResource(URIResourceImpl uriImpl) {}


	
}
