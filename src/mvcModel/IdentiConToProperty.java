package mvcModel;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;


public class IdentiConToProperty 
{
	public Property prop;
	public String id;
	public String description;
	public ArrayList<Statement> statements;
	public ArrayList<IdentiConToProperty> subProperties;
	public ArrayList<IdentiConToProperty> directParentProperties;

	public IdentiConToProperty(IdentiConToProperty parentProp)
	{
		prop = null;
		id = null;
		description = null;
		statements = new ArrayList<>();
		subProperties = new ArrayList<>();
		directParentProperties = new ArrayList<>();		
		if(parentProp != null)
		{
			this.addParentProperty(parentProp);			
		}		
	}


	public void addParentProperty(IdentiConToProperty parentProp)
	{		
		if(!this.directParentProperties.contains(parentProp))
		{
			directParentProperties.add(parentProp);			
		}
	}






}
