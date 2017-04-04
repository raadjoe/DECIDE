package mvcView;

import java.util.ArrayList;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

import mvcModel.Context;
import mvcModel.IdentiConToClass;


public class ViewFunctions 
{	
	
	public void outputIdentiConToClassWithProperties(IdentiConToClass instClass)
	{
		int j=1;
		int k;
		System.out.println(instClass.idClass.getLocalName());
		for(Property p : instClass.identiConToClassProperties.keySet())
		{
			k=1;
			System.out.print("  (" + j + ") ");
			System.out.print(p.getLocalName());
			System.out.print(" --> (");
			for(RDFNode n : instClass.identiConToClassProperties.get(p))
			{
				if(k>1)
				{
					System.out.print(" - ");
				}
				if(n!=null)
				{							
					System.out.print(n.asNode().getLocalName());
				}
				else
				{
					System.out.print("Data or Annotation Property");
				}
				k++;
			}
			System.out.println(")");			
			j++;
		}	
	}

	// show in the console the identiConTo class with its lattice properties
	public void outputIdentiConToClassWithLatticeProperties(IdentiConToClass identiConToClass)
	{
		int j=1;
		System.out.println(identiConToClass.idClass.getLocalName());
		for(Property p : identiConToClass.latticeProperties)
		{
			System.out.print("  (" + j + ") ");
			System.out.println(p.getLocalName());	
			j++;
		}
	}

	// show in the console all identiConTo classes with their lattice properties
	public void outputDependentClassesWithLatticeProperties(ArrayList<IdentiConToClass> dependentClasses)
	{
		int i =1;
		for(IdentiConToClass cl : dependentClasses)
		{
			System.out.print("[" + i + "] ");
			outputIdentiConToClassWithLatticeProperties(cl);
			i++;
			System.out.println("-----------------------------------");
		}				
		System.out.println("#### DONE #### outputDependentClassesWithLatticeProperties");
	}

	// show in the console an identiConTo local lattice
	public void outputLocalLattice(IdentiConToClass cl)
	{
		System.out.println("");
		System.out.print("======= LATTICE - " +  cl.idClass.getLocalName());
		System.out.println(" ======= (" + cl.localLattice.getTotalNumberOfContexts() + " contexts)");
		if(!cl.localLattice.allContexts.isEmpty())
		{
			for(int level: cl.localLattice.allContexts.keySet())
			{
				System.out.println("Level " + level + ": ");
				for(Context cnt : cl.localLattice.allContexts.get(level))
				{
					cnt.outputContext();
				}
			}
		}	
	}
	
	// show in the console all identiConTo local lattices
	public void outputAllLocalLattices(ArrayList<IdentiConToClass> allClasses)
	{
		for(IdentiConToClass cl : allClasses)
		{
			outputLocalLattice(cl);
		}
	}



	
	
}
