package mvcModel;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;



public class Context 
{
	public ArrayList<Property> contextProperties; // the properties of this context
	public Property optionalProperty; // the property which can be empty but not different
	public int contextLevel; // the level in which this context exists 
	public int contextNumber; // the number of this context in this level 
	public String contextID; // the context id which is composed from its level + its number
	public ArrayList<Context> subContexts; // a list of the contexts which are sub contexts to this context
	public ArrayList<Context> directContextParents; // a list of the contexts which are direct parent contexts to this context
	public ArrayList<RDFNode> dependentNodes;
	public ArrayList<IdentiConToClass> dependantClasses;

	public Context(Context parentContext, int contextNumber)
	{
		contextProperties = new ArrayList<>();
		subContexts = new ArrayList<>();
		directContextParents = new ArrayList<>();
		dependentNodes = new ArrayList<>();
		optionalProperty = null;
		this.contextNumber = contextNumber;	
		this.dependantClasses = new ArrayList<>();
		if(parentContext!= null)
		{
			this.contextLevel = parentContext.contextLevel + 1;
			this.contextID = this.contextLevel + "." + this.contextNumber;
			this.setContextProperties(parentContext.getContextProperties());
			this.addParentContext(parentContext);
			this.setDependentNodes(parentContext.dependentNodes);
		}
	}


	// get the properties of this context
	public ArrayList<Property> getContextProperties()
	{
		return this.contextProperties;
	}

	// assign the properties to this context
	public void setContextProperties(ArrayList<Property> Properties)
	{
		for(Property p : Properties)
		{
			this.contextProperties.add(p);
		}
	}

	// add a new parent to this context to the list of direct context parents
	public void addParentContext(Context parentContext)
	{
		if(!this.directContextParents.contains(parentContext))
		{
			this.directContextParents.add(parentContext);
		}
	}

	// add a sub context to this context to the list of sub contexts
	public void addSubContext(Context son) 
	{
		if(!this.subContexts.contains(son))
		{
			this.subContexts.add(son);
		}
	}

	// assign the dependent nodes to this context
	public void setDependentNodes(ArrayList<RDFNode> parentNodes)
	{
		for(RDFNode node : parentNodes)
		{
			this.dependentNodes.add(node);
		}
	}

	// set the optional property
	public void setOptionalProperty(Property optionalProperty)
	{
		this.optionalProperty = optionalProperty;
	}

	// check if this context has an optional property or not
	public Boolean hasOptionalProperty()
	{
		if(this.optionalProperty == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	// remove a certain property from the context properties
	public void removeProperty(Property p)
	{
		Property removedProp = null;
		for(Property contProp : contextProperties)
		{
			if(contProp.getURI().equals(p.getURI()))
			{
				removedProp = contProp;
			}
		}
		if(removedProp != null)
		{
			this.contextProperties.remove(removedProp);
			if(removedProp.hasProperty(RDF.type, OWL.ObjectProperty))
			{
				if(removedProp.hasProperty(RDFS.range))
				{
					this.dependentNodes.remove(removedProp.getPropertyResourceValue(RDFS.range));
				}
			}
		}
	}

	// output the properties of this context in addition to the optional property if it exists
	public void outputContext()
	{
		if(!this.contextProperties.isEmpty())
		{
			int i = 0;
			System.out.print(contextID + " :");
			for (Property prop : this.contextProperties )
			{
				i++;
				System.out.print("  " + i + ")");
				System.out.print(prop.getLocalName());
			}
			if(this.optionalProperty != null)
			{
				System.out.print("  *)");
				System.out.print(optionalProperty.getLocalName());
			}
			System.out.println();
		}
	}


	// check if a context has instances in the ontology or not
	public ArrayList<Resource> getContextResources(ArrayList<Resource> listOfIndividuals)
	{
		ArrayList<Resource> individualsOfContext = new ArrayList<>();
		Boolean relevant = true;
		for (Resource res :listOfIndividuals) 
		{
			relevant = true;
			for(Property prop : this.getContextProperties())
			{
				if(res.getProperty(prop) == null)
				{
					relevant = false;
					break;
				}
			}
			if(relevant == true)
			{
				individualsOfContext.add(res);
			}
		}
		return individualsOfContext;
	}
	
	
	public Boolean checkIfLastContext()
	{
		if(this.contextProperties.isEmpty())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	



	

}
