package mvcModel;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;



public class GlobalContext 
{
	public IdentiConToClass idClass;
	public String id;
	public String description;
	public HashMap<Lattice, Context> allLocalContexts;
	public ArrayList<GlobalContext> subGlobalContexts;
	public ArrayList<GlobalContext> directParentContexts;
	public Property identiConToProperty;
	public ArrayList<Statement> statements;
	public ArrayList<String> pairNumbers;


	public GlobalContext()
	{
		idClass = null;
		id = "";
		description = "";
		allLocalContexts = new HashMap<>();
		subGlobalContexts = new ArrayList<>();
		directParentContexts = new ArrayList<>();
		statements = new ArrayList<>();
		pairNumbers = new ArrayList<>();
		
	}	

	public void addParentGlobalContext(GlobalContext parentGlobalContext)
	{
		if(!this.directParentContexts.contains(parentGlobalContext))
		{
			this.directParentContexts.add(parentGlobalContext);
			parentGlobalContext.subGlobalContexts.add(this);
		}
	}


	public void outputGlobalContext()
	{

		System.out.println("Global Context ");
		System.out.println("--------------");
		System.out.println("(" + this.id + ")");
		for(Lattice cl : this.allLocalContexts.keySet())
		{
			System.out.print(cl.latticeClass.idClass.getLocalName());
			System.out.print(" -- ");
			System.out.println(this.allLocalContexts.get(cl).contextID);
		}	
	}

	public void outputStatements()
	{
		int i = 1;
		for(Statement st : statements)
		{
			System.out.print(i + ") ");
			System.out.print(st.getSubject().getLocalName());
			//System.out.print("  " + st.getPredicate() + "  ");
			System.out.print("identiConTo<"+this.id+">");
			System.out.println(st.getObject().asResource().getLocalName());
			i++;
		}
	}


	public Statement addStatement(OntModel model, Resource res1, Resource res2)
	{
		Statement st = null;
		if(identiConToProperty != null)
		{
			st = model.createStatement(res1, identiConToProperty, res2);
			model.add(st);
			statements.add(st);
		}
		return st;
	}
	
	public void addPair(String pair)
	{
		if(!pairNumbers.contains(pair))
		{
			pairNumbers.add(pair);
		}
	}

	public Boolean checkIfSubGlobalContext(GlobalContext gcChild, GlobalContext gcParent)
	{		
		for(Lattice lat : gcChild.allLocalContexts.keySet())
		{
			if(!gcParent.allLocalContexts.containsKey(lat))
			{
				return false;
			}
		}

		for(Lattice lat : gcChild.allLocalContexts.keySet())
		{
			if(lat.checkIfSubContextOf(gcChild.allLocalContexts.get(lat), gcParent.allLocalContexts.get(lat)) == false)
			{
				return false;
			}				
		}			
		return true;
	}
	
	public Boolean searchIfParentContext(GlobalContext parentContext)
	{
		Boolean result;
		if(this.directParentContexts.contains(parentContext))
		{
			return true;
		}
		else
		{
			for(GlobalContext directParent : this.directParentContexts)
			{
				result = directParent.searchIfParentContext(parentContext);
				if(result == true)
				{
					return true;
				}
			}
		}	
		return false;
	}
	
	
	public void addDirectParentContext(GlobalContext parentContext)
	{
		if(parentContext.id.equals("GC_9"))
		{
			System.out.println("blabla");
		}
		if(this.searchIfParentContext(parentContext) == false)
		{
			this.directParentContexts.add(parentContext);
		}
	}
	
	
	public void addParentContext(GlobalContext src, GlobalContext newGC)
	{		
		if(newGC.id.equals("GC_9"))
		{
			System.out.println("blabla");
		}
		if(checkIfSubGlobalContext(this, newGC) == true) 
		{
			if(this.directParentContexts.isEmpty())
			{
				this.addDirectParentContext(newGC);
			}
			else
			{
				ArrayList<GlobalContext> x = new ArrayList<>();
				x.addAll(this.directParentContexts);
				for(GlobalContext parentContext: x)
				{
					parentContext.addParentContext(this, newGC);
				}	
			}		
		}
		else
		{
			if(checkIfSubGlobalContext(newGC, this) == true) 
			{
				newGC.addDirectParentContext(this);
				src.directParentContexts.remove(this);
				if(!src.directParentContexts.contains(newGC))
				{
					src.addDirectParentContext(newGC);
				}
						
			}
			else
			{
				if(!src.directParentContexts.contains(newGC))
				{
					src.addDirectParentContext(newGC);
				}
			}
		}
	}
	
	

	
	public void outputAllParentontexts()
	{
		if(this.directParentContexts.isEmpty())
		{
			System.out.println(this.id + " (no Parent Context)");
		}
		else
		{
			for(GlobalContext parentContext : this.directParentContexts)
			{
				System.out.println(this.id + " hasParentContext " + parentContext.id);
				parentContext.outputAllParentontexts();
			}
		}
	}	
	
	
	public ArrayList<GlobalContext> getAllParentContexts(ArrayList<GlobalContext> parentContexts)
	{
		for(GlobalContext parentGC : this.directParentContexts)
		{
			if(!parentContexts.contains(parentGC))
			{
				parentContexts.add(parentGC);
				parentGC.getAllParentContexts(parentContexts);
			}	
		}
		return parentContexts;
	}
	
	/*public int getGlobalContextSize(int size)
	{
		ArrayList<GlobalContext> parentContexts = new ArrayList<>();
		parentContexts = getAllParentContexts(parentContexts);		
		size = size + this.statements.size();
		for(GlobalContext parentGC : parentContexts)
		{
			size = size + parentGC.statements.size();
		}
		return size;
	}*/
	
	public int getGlobalContextSize(int size)
	{
		//HashMap<Integer, Statement> st = new HashMap<>();
		ArrayList<GlobalContext> parentContexts = new ArrayList<>();
		ArrayList<String> pairs = new ArrayList<>();
		parentContexts = getAllParentContexts(parentContexts);		
		//size = size + this.statements.size();
		pairs.addAll(this.pairNumbers);
		for(GlobalContext parentGC : parentContexts)
		{
			for(String pair : parentGC.pairNumbers)
			{
				if(!pairs.contains(pair))
				{
					pairs.add(pair);
				}
			}
			
		 // st.putAll(parentGC.statementsPerCouple);
			//size = size + parentGC.statements.size();
		}	
		return pairs.size();
	}
	
}
