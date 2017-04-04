package mvcModel;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class Lattice 
{
	public IdentiConToClass latticeClass;
	public ArrayList<Property> latticeProperties;
	public Context mainContext;	
	public HashMap<Integer,ArrayList<Context>> allContexts; // all the contexts presented in a non-hierarchical way
	static int[] contextIDs; // a counter to manage the IDs of the context
	public ArrayList<Context> validContexts;

	public Lattice(IdentiConToClass latticeClass)
	{
		this.latticeClass = latticeClass;
		this.latticeProperties = latticeClass.latticeProperties;
		this.allContexts = new HashMap<>();
		this.validContexts = new ArrayList<>();
		contextIDs = new int[latticeProperties.size() * 2];
	}



	public void constructLattice()
	{		
		mainContext = new Context(null, 1);
		mainContext.setContextProperties(this.latticeProperties);
		mainContext.contextLevel = 1;
		mainContext.contextID = mainContext.contextLevel + ".1";

		Context nullContext = new Context(null, 1);		
		nullContext.contextLevel = latticeProperties.size() + 1;
		nullContext.contextID = nullContext.contextLevel + ".1";
		for (int i =1 ; i <= latticeProperties.size() + 1 ; i++)
		{
			this.allContexts.put(i, new ArrayList<Context>());
		}
		addToAllContexts(allContexts, 1, mainContext);
		addToAllContexts(allContexts, latticeProperties.size() + 1, nullContext);
		contextIDs[0] = 1;
		createSubContexts(mainContext);		
	}

	//used by the function constructLattice to construct the sub contexts
	public void createSubContexts(Context parentContext)
	{
		Context currentContext = null;
		int j = 0;
		int propNumber = parentContext.getContextProperties().size();
		if(propNumber > 1)
		{
			for(Property prop : parentContext.getContextProperties())
			{	
				if(searchEqualContext(this.allContexts, parentContext, prop) == null)
				{
					contextIDs[parentContext.contextLevel]++;
					j = contextIDs[parentContext.contextLevel];
					currentContext = new Context(parentContext, j);	
					currentContext.removeProperty(prop);
					addToAllContexts(allContexts, currentContext.contextLevel, currentContext);
				}
				else
				{
					currentContext = searchEqualContext(this.allContexts, parentContext, prop);
					currentContext.addParentContext(parentContext);
				}	
				parentContext.addSubContext(currentContext);
				createSubContexts(currentContext);	
			}
		}
		else
		{
			if(propNumber > 0)
			{
				currentContext = getContextByLevelAndNumber(Integer.valueOf(latticeProperties.size() + 1 ), 1);
				currentContext.addParentContext(parentContext);
				parentContext.addSubContext(currentContext);
				createSubContexts(currentContext);
			}
		}
	}


	public Context searchEqualContext(HashMap<Integer,ArrayList<Context>> allContexts, Context parentContext, Property optionalProperty)
	{		
		for(Context otherContext : allContexts.get(parentContext.contextLevel + 1))
		{	
			Boolean equalContext = true;
			for(Property propArray1 : otherContext.getContextProperties())
			{
				if(optionalProperty.equals(propArray1))
				{
					equalContext = false;
					break;	
				}
				else
				{
					if(!parentContext.contextProperties.contains(propArray1))
					{								
						equalContext = false;
						break;		
					}
				}				
			}
			if(equalContext == true)
			{	
				return otherContext;
			}				
		}
		return null;
	}




	//construct all the contexts of the lattice
	//all the contexts are added under the mainContext and in the ArrayList allContexts
	public void constructLattice_allowUnknown()
	{		
		mainContext = new Context(null, 1);
		mainContext.setContextProperties(this.latticeProperties);
		mainContext.contextLevel = 1;
		mainContext.contextID = mainContext.contextLevel + ".1";

		Context nullContext = new Context(null, 1);		
		nullContext.contextLevel = latticeProperties.size() * 2;
		nullContext.contextID = nullContext.contextLevel + ".1";
		for (int i =1 ; i <= latticeProperties.size() * 2 ; i++)
		{
			this.allContexts.put(i, new ArrayList<Context>());
		}
		//this.allContexts.put(1, new ArrayList<Context>(Arrays.asList(mainContext)));
		addToAllContexts(allContexts, 1, mainContext);
		addToAllContexts(allContexts, latticeProperties.size() * 2, nullContext);
		contextIDs[0] = 1;
		createSubContexts(mainContext);		
	}

	//used by the function constructLattice to construct the sub contexts
	public void createSubContexts_allowUnknown(Context parentContext)
	{
		Context currentContext = null;
		int j = 0;
		int propNumber = parentContext.getContextProperties().size();
		if(parentContext.hasOptionalProperty() == true)
		{
			propNumber++;
		}
		if(propNumber > 1)
		{			
			if(parentContext.hasOptionalProperty() == false)
			{
				for(Property prop : parentContext.getContextProperties())
				{		
					if(searchEqualContext(this.allContexts, parentContext, prop) == null)
					{
						contextIDs[parentContext.contextLevel]++;
						j = contextIDs[parentContext.contextLevel];
						currentContext = new Context(parentContext, j);	
						currentContext.setOptionalProperty(prop);
						currentContext.removeProperty(prop);
						addToAllContexts(allContexts, currentContext.contextLevel, currentContext);						
					}
					else
					{
						currentContext = searchEqualContext(this.allContexts, parentContext, prop);
						currentContext.addParentContext(parentContext);							
					}	
					parentContext.addSubContext(currentContext);
					createSubContexts(currentContext);
				}			
			}
			else
			{					
				if(searchEqualContext(this.allContexts, parentContext, null) == null)
				{
					contextIDs[parentContext.contextLevel]++;
					j = contextIDs[parentContext.contextLevel];
					currentContext = new Context(parentContext, j);	
					currentContext.setOptionalProperty(null);
					addToAllContexts(allContexts, currentContext.contextLevel, currentContext);
				}
				else
				{
					currentContext = searchEqualContext(this.allContexts, parentContext, null);
					currentContext.addParentContext(parentContext);
				}	
				parentContext.addSubContext(currentContext);
				createSubContexts(currentContext);					
			}
		}
		else
		{
			if(propNumber > 0)
			{
				currentContext = getContextByLevelAndNumber(Integer.valueOf(latticeProperties.size() * 2), 1);
				currentContext.addParentContext(parentContext);
				parentContext.addSubContext(currentContext);
				createSubContexts(currentContext);
			}
		}
	}

	// add to All Contexts HashMap
	public void addToAllContexts(HashMap<Integer,ArrayList<Context>> allContexts, int level, Context cnt)
	{
		allContexts.get(level).add(cnt);
	}

	//check if this context was already created
	//return the context if it is already created and return null if not
	public Context searchEqualContext_allowUnknown(HashMap<Integer,ArrayList<Context>> allContexts, Context parentContext, Property optionalProperty)
	{
		Boolean equalContext;
		for(Context otherContext : allContexts.get(parentContext.contextLevel + 1))
		{
			equalContext = true;
			if(optionalProperty == null && otherContext.optionalProperty == null)
			{

			}
			else
			{
				if(optionalProperty == null || otherContext.optionalProperty == null)
				{
					equalContext = false;
				}
				else
				{
					if(!optionalProperty.getURI().equals(otherContext.optionalProperty.getURI()))
					{
						equalContext = false;
					}
				}
			}				
			if(equalContext == true)
			{
				for(Property propArray1 : otherContext.getContextProperties())
				{
					if(!parentContext.contextProperties.contains(propArray1))
					{
						equalContext = false;
						break;
					}
				}
				if(equalContext == true)
				{
					return otherContext;
				}
			}				
		}
		return null;
	}

	//search for a certain context by its Level and Number
	public Context getContextByLevelAndNumber(int contextLevel , int contextNumber)
	{
		Context specificContext = null;
		for(Context otherContext : this.allContexts.get(contextLevel))
		{
			if(otherContext.contextNumber == contextNumber)
			{
				specificContext = otherContext;
				return specificContext;
			}
		}
		return specificContext;
	}

	// count the number of contexts in a lattice
	public int getTotalNumberOfContexts()
	{
		int numberOfContexts = 0;
		if(!allContexts.isEmpty())
		{
			for(int level:  allContexts.keySet())
			{
				numberOfContexts = numberOfContexts + allContexts.get(level).size();
			}
		}	
		return numberOfContexts;
	}

	//check the first context of the lattice that has instances in the ontology
	public ArrayList<Context> checkPercentageOfValidContexts(ArrayList<Resource> listOfIndividuals, float percentage)
	{
		float threshold = listOfIndividuals.size() * (percentage/100);
		ArrayList<Context> validContexts = new ArrayList<>();
		ArrayList<Resource> coveredIndividuals = new ArrayList<>();
		ArrayList<Resource> listOfContextIndivs = new ArrayList<>();
		if(!this.allContexts.isEmpty())
		{
			for(int level:  this.allContexts.keySet())
			{
				for(Context cnt : this.allContexts.get(level))
				{
					listOfContextIndivs = cnt.getContextResources(listOfIndividuals);
					if(listOfContextIndivs.size() > 1)
					{
						validContexts.add(cnt);
						coveredIndividuals.removeAll(listOfContextIndivs);
						coveredIndividuals.addAll(listOfContextIndivs);
					}			
				}
				if(coveredIndividuals.size() >= threshold)
				{
					return validContexts;
				}	
			}
			return validContexts;
		}	
		return validContexts;
	}


	//check the first context of the lattice that has instances in the ontology
	public ArrayList<Context> checkAllValidContexts(ArrayList<Resource> listOfIndividuals)
	{
		ArrayList<Context> validContexts = new ArrayList<>();
		ArrayList<Resource> coveredIndividuals = new ArrayList<>();
		ArrayList<Resource> listOfContextIndivs = new ArrayList<>();
		if(!this.allContexts.isEmpty())
		{
			for(int level:  this.allContexts.keySet())
			{
				for(Context cnt : this.allContexts.get(level))
				{
					listOfContextIndivs = cnt.getContextResources(listOfIndividuals);
					if(listOfContextIndivs.size() > 1)
					{
						validContexts.add(cnt);
						coveredIndividuals.removeAll(listOfContextIndivs);
						coveredIndividuals.addAll(listOfContextIndivs);
					}			
				}
			}				
		}
		if(validContexts.isEmpty())
		{
			for(int level:  this.allContexts.keySet())
			{
				for(Context cnt : this.allContexts.get(level))
				{
					listOfContextIndivs = cnt.getContextResources(listOfIndividuals);
					if(listOfContextIndivs.size() >= 1)
					{
						validContexts.add(cnt);
						coveredIndividuals.removeAll(listOfContextIndivs);
						coveredIndividuals.addAll(listOfContextIndivs);
					}			
				}
			}		
		}
		return validContexts;
	}

	public Context searchContextByProperties(ArrayList<Property> listOfProps)
	{
		Context returnedContext = null;
		if(listOfProps.size() != 0 || listOfProps != null)
		{
			int diffOfProps = mainContext.contextProperties.size() - listOfProps.size();
			//int contextLevel = 1 + 2*diffOfProps;
			int contextLevel = 1 + diffOfProps;
			for(Context cnt : allContexts.get(contextLevel))
			{
				Boolean check = true;
				for(Property p : listOfProps)
				{
					if(!cnt.contextProperties.contains(p))
					{
						check = false;
						break;
					}
				}
				if(check == true)
				{
					return cnt;
				}
			}
		}
		return returnedContext;
	}

	public ArrayList<Context> returnAllSubContexts(Context cnt, int levelLimit, ArrayList<Context> levelLimitChildContexts)
	{
		for(Context subCont : cnt.subContexts)
		{
			if(subCont.contextLevel == levelLimit)
			{
				if(!levelLimitChildContexts.contains(subCont))
				{
					levelLimitChildContexts.add(subCont);
				}			
			}
			else
			{
				returnAllSubContexts(subCont, levelLimit, levelLimitChildContexts);
			}
		}
		return levelLimitChildContexts;
	}


	public Boolean checkIfSubContextOf(Context childContext, Context parentContext)
	{
		if(parentContext.contextID.equals(childContext.contextID))
		{
			return true;
		}
		else
		{
			if(parentContext.contextLevel <= childContext.contextLevel)
			{
				//ArrayList<Context> listOfChildContexts = new ArrayList<>();
				for(Property childProp : childContext.contextProperties)
				{
					if(!parentContext.contextProperties.contains(childProp))
					{
						return false;
					}
				}
				return true;
				/*listOfChildContexts = returnAllSubContexts(parentContext, childContext.contextLevel, listOfChildContexts);
				if(listOfChildContexts.contains(childContext))
				{
					return true;
				}*/
			}
			return false;
		}	
	}
	
	public Context getLastContext()
	{
		int size = this.allContexts.keySet().size();
		Context lastContext = this.allContexts.get(size-1).get(0);		
		return lastContext;
	}


}
