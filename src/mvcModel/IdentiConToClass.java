package mvcModel;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.jena.ontology.*;
import org.apache.jena.rdf.model.*;



public class IdentiConToClass 
{
	public OntClass idClass;  // the onto class in which this identiConTo class is based on
	public HashMap<Property, ArrayList<RDFNode>> identiConToClassProperties;  // all the properties of this class with their possible range(s)
	public ArrayList<Property> latticeProperties; // the properties that are used for the construction of this class' lattice
	public ArrayList<Property> discardedProperties; // the properties that are discarded for the construction of this class' lattice
	public Lattice localLattice; // the local lattice of this identiConTo class 
	public IdentiConToProperty theIdentiConToProperty; 


	public IdentiConToClass(OntClass instClass)
	{
		this.idClass = instClass;
		identiConToClassProperties = new HashMap<>();
		latticeProperties = new ArrayList<Property>();	
		discardedProperties = new ArrayList<Property>();	
		localLattice = null;
		theIdentiConToProperty = new IdentiConToProperty(null);
	}

	// add an instantiated class property and its class range to the instantiated class properties
	public void addIdentiConToClassProperty(Property prop, RDFNode node)
	{
		if(!identiConToClassProperties.containsKey(prop))
		{
			ArrayList<RDFNode> nodes = new ArrayList<>();
			nodes.add(node);
			identiConToClassProperties.put(prop, nodes);
		}
		else
		{
			if(!identiConToClassProperties.get(prop).contains(node))
			{
				identiConToClassProperties.get(prop).add(node);
			}
		}		
	}


	//prepare the properties of the lattice for this class
	//it adds all the sub properties to the latticeProperties while checking if it is discarded or not
	public void prepareLocalLatticeProperties()
	{
		for(Property parentProp : this.identiConToClassProperties.keySet())
		{
			if(!discardedProperties.contains(parentProp))
			{
				this.latticeProperties.add(parentProp);
			}
		}		
	}

	//construct the lattice of this class
	//a limit of the levels of the lattice can be set in order to avoid huge number of unwanted contexts in the lattice
	public Lattice constructClassLattice()
	{
		if(!latticeProperties.isEmpty())
		{
			localLattice = new Lattice(this);
			localLattice.constructLattice();
		}
		else
		{
			System.out.println("There's no lattice properties for the class " + idClass.getURI());
		}	
		return localLattice;
	}




	public Context getNextValidContext(Context cnt)
	{				
		Context nextContext = null;
		int index = 0;
		index = this.localLattice.validContexts.indexOf(cnt);
		if(index != -1 && index != this.localLattice.validContexts.size()-1)
		{
			nextContext = this.localLattice.validContexts.get(index+1);
		}
		return nextContext;
	}






}
