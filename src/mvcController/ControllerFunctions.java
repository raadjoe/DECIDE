package mvcController;

import mvcView.*;
import mvcModel.*;
import java.util.ArrayList;
import java.util.HashMap;


public class ControllerFunctions 
{
	private ModelFunctions theModel;
	private ViewFunctions theView;

	public ControllerFunctions(ModelFunctions theModel, ViewFunctions theView)
	{
		this.theModel = theModel;
		this.theView = theView;
	}


	//================================== DECIDE ================================== 

	public void decide(String modelURI, String identiConToClass, ArrayList<String> discardedClasses, HashMap<String,ArrayList<String>> discardedProperties, ArrayList<ArrayList<String>> coupleProperties, ArrayList<ArrayList<String>> necessaryProperties)
	{
		loadModel(modelURI); //read the model
		loadModelWithInference(modelURI); // read the model with inference
		outputOntologyStats(); // output some ontology stats
		constructDependantClassesAndLattices(identiConToClass, discardedClasses, discardedProperties, coupleProperties, necessaryProperties ); // construct all local contexts of the connex graph 
		checkIdentiConToRelations(theModel.identiConToClass, theModel.dependentIdentiConToClasses);
		writeOWLfile("data/Echantillon_PO2_CellExtraDry_Resultats.ttl"); // write the model with the generated identity links
	}


	public void constructDependantClassesAndLattices(String identiConToClass, ArrayList<String> discardedClasses, HashMap<String,ArrayList<String>> discardedProperties, ArrayList<ArrayList<String>> indispensableProperties, ArrayList<ArrayList<String>> necessaryProperties)
	{
		setDiscardedClasses(discardedClasses);
		setDiscardedProperties(discardedProperties);
		setCoupleProperties(indispensableProperties);
		createIdentiConToClassWithProperties(identiConToClass);
		outputIdentiConToClassWithProperties(theModel.identiConToClass);
		outputIdentiConToClassWithLatticeProperties(theModel.identiConToClass);
		createAllDependentIdentiConToClasses(theModel.identiConToClass);
		outputDependentClassesWithLatticeProperties(theModel.dependentIdentiConToClasses);
		theModel.connexGraphStats();
		constructAllLocalLattices(theModel.dependentIdentiConToClasses);
		outputAllLattices(theModel.dependentIdentiConToClasses);
	}


	// set the discarded properties in the Model
	public void setDiscardedProperties(HashMap<String, ArrayList<String>> discardedProperties)
	{
		if(discardedProperties!=null)
			theModel.discardedProperties.putAll(discardedProperties); 
	}

	// set the discarded properties in the Model
	public void setDiscardedClasses(ArrayList<String> discardedClasses)
	{
		if(discardedClasses!=null)
			for(String uri : discardedClasses)
			{
				theModel.disregardClass(uri);
			}		
	}

	// set the couple properties in the Model
	public void setCoupleProperties(ArrayList<ArrayList<String>> listOfCoupleProperties)
	{
		if(listOfCoupleProperties!=null)
			theModel.setCoupleProperties(listOfCoupleProperties); 
	}


	// create and load the identiConTo class
	public void createIdentiConToClassWithProperties(String identiConToClassURI)
	{
		IdentiConToClass cl = theModel.createIdentiConToClass(identiConToClassURI);
		if(cl!= null)
		{
			theModel.identiConToClass = cl;
		}
	}

	// create and load all the classes dependent on the main identiConTo class
	public void createAllDependentIdentiConToClasses(IdentiConToClass cl)
	{
		if(cl != null)
		{
			theModel.createDependentIdentiConToClasses(cl);
		}
		else
		{
			System.out.println("The identiConTo class is not loaded!");
		}
	}

	//construct all the local lattices (lattice of the identiConTo class + lattices of each dependent class)
	public void constructAllLocalLattices(ArrayList<IdentiConToClass> dependentCl)
	{
		ArrayList<IdentiConToClass> dependentClasses = new ArrayList<>();
		if(dependentCl != null)
		{
			dependentClasses.add(theModel.identiConToClass);	
			dependentClasses.addAll(1, dependentCl);
			theModel.createAllLattices(dependentClasses);
		}
		else
		{
			System.out.println("No IdentiConTo classes available!");
		}
	}



	public void checkIdentiConToRelations(IdentiConToClass cl, ArrayList<IdentiConToClass> dependantClasses)
	{
		if(cl != null)
		{
			theModel.insertIdentiConToRelationships(cl, dependantClasses );
		}
		else
		{
			System.out.println("The identiConTo class is not loaded!");
		}
	}


	public void outputOntologyStats()
	{
		System.out.println("");
		System.out.println("Number of statements: " + theModel.countStatements());
		System.out.println("Number of classes: " + theModel.countClasses());
		System.out.println("Number of individuals: " + theModel.countIndividuals());
		System.out.println("Number of properties: " + theModel.countProperties());
		System.out.println("	Object properties: " + theModel.countObjectProperties());
		System.out.println("	Data properties: " + theModel.countDataTypeProperties());
		System.out.println("	Annotation properties: " + theModel.countAnnotationProperties());
		System.out.println("");
	}






	//================================== READ AND SAVE THE MODEL FUNCTIONS ================================== 

	// load the raw model in the mvcModel
	public void loadModel(String modelURI)
	{
		theModel.ReadModel(modelURI);
	}

	// load the inferred model in the mvcModel
	public void loadModelWithInference(String modelURI)
	{
		theModel.ReadModelWithInference(modelURI);
	}

	// write the model in OWL File
	public void writeOWLfile(String ontologyOutputURI) 
	{
		theModel.writeOWLFile(theModel.model, ontologyOutputURI);
	}




	//================================== OUTPUT FUNCTIONS ================================== 

	// output all the properties of the identiConTo class
	public void outputIdentiConToClassWithProperties(IdentiConToClass cl)
	{
		if(cl != null)
		{
			theView.outputIdentiConToClassWithProperties(cl);
		}
		else
		{
			System.out.println("The identiConTo class is not loaded!");
		}
	}

	// output the lattice properties of the identiConTo class
	public void outputIdentiConToClassWithLatticeProperties(IdentiConToClass cl)
	{
		if(cl != null)
		{
			theView.outputIdentiConToClassWithLatticeProperties(cl);
		}
		else
		{
			System.out.println("The identiConTo class is not loaded!");
		}
	}

	// output the lattice properties of all the dependent identiConTo class
	public void outputDependentClassesWithLatticeProperties(ArrayList<IdentiConToClass> dependentCl)
	{
		if(dependentCl != null)
		{
			theView.outputDependentClassesWithLatticeProperties(dependentCl);
		}
		else
		{
			System.out.println("No dependent IdentiConTo classes!");
		}
	}

	//output all the local lattices (lattice of the identiConTo class + lattices of each dependent class)
	public void outputAllLattices(ArrayList<IdentiConToClass> dependentCl)
	{
		ArrayList<IdentiConToClass> dependentClasses = new ArrayList<>();
		if(dependentCl != null)
		{
			dependentClasses.add(theModel.identiConToClass);	
			dependentClasses.addAll(1, dependentCl);
			theView.outputAllLocalLattices(dependentClasses);
		}
		else
		{
			System.out.println("No dependent IdentiConTo classes!");
		}
	}
}
