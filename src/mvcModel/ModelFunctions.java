package mvcModel;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.TDBLoader;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


public class ModelFunctions 
{
	public final String rulesFile = "C:\\Users\\Joe\\Desktop\\PhD\\OWL\\contexIT\\rules.txt";
	public final String PO2 = "http://opendata.inra.fr/PO2/";
	public final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	public final String owl = "http://www.w3.org/2002/07/owl#";
	public final String OBO = "http://purl.obolibrary.org/obo/";
	public final String core = "http://opendata.inra.fr/resources/core#";
	public final String OM = "http://www.wurvoc.org/vocabularies/om-1.6/";
	public final String skos = "http://www.w3.org/2004/02/skos/core#";
	public final String time = "http://www.w3.org/2006/time#";
	public OntModel model,infModel;
	public IdentiConToClass identiConToClass;
	public ArrayList<IdentiConToClass> dependentIdentiConToClasses;
	public ArrayList<IdentiConToClass> nonConsideredIdentiConToClasses;
	public HashMap<String, ArrayList<String>> discardedProperties;
	public ArrayList<ArrayList<String>> indispensableProperties;
	public GlobalContext emptyGlobalContext;
	public ArrayList<GlobalContext> allGlobalContexts;
	public ArrayList<GlobalContext> temporaryGlobalContexts;
	public ArrayList<GlobalContext> createdContexts;
	public static int counter;
	public static int gbcounter;


	public ModelFunctions()
	{
		model = null;
		infModel = null;
		identiConToClass = null;
		dependentIdentiConToClasses = new ArrayList<>();
		nonConsideredIdentiConToClasses = new ArrayList<>();
		discardedProperties = new HashMap<>();
		indispensableProperties = new ArrayList<>();
		allGlobalContexts = new ArrayList<>();
		temporaryGlobalContexts = new ArrayList<>();
		createdContexts = new ArrayList<>();
		emptyGlobalContext = new GlobalContext();
	}


	// Read the model from a given URI
	public void ReadModel(String modelName) 
	{
		try 
		{
			OntModel thisModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
			FileManager.get().readModel(thisModel, modelName);
			System.out.println("#### DONE #### ReadModel");
			this.model = thisModel;
		} 
		catch (Exception ex) 
		{
			System.out.println("##### Error Function: readModel #####");
			System.out.println("EXCEPTION: " + ex.getMessage());
		}
	}

	// Read the model from a given URI and apply inference
	public void ReadModelWithInference(String modelName) 
	{
		try 
		{
			OntModel inferModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, null);
			FileManager.get().readModel(inferModel, modelName);
			System.out.println("#### DONE #### ReadModelWithInference");
			this.infModel = inferModel;
		} 
		catch (Exception ex) 
		{
			System.out.println("##### Error Function: ReadModelWithInference #####");
			System.out.println("EXCEPTION: " + ex.getMessage());
		}
	}

	// Write the model in OWL file
	public OutputStream writeOWLFile(OntModel model, String ontologyOutputURI) 
	{
		try 
		{
			OutputStream output = new FileOutputStream(ontologyOutputURI);
			RDFWriter ontologyWriter = (RDFWriter) model.getWriter("TURTLE");
			ontologyWriter.write(model, output, null);
			System.out.println("#### DONE #### writeOWLFile");		
			return output;
		} 
		catch (Exception ex) 
		{
			System.out.println("##### Error Fonction: writeOWLFile #####");
			System.out.println(ex.getMessage());
			return null;
		}
	}

	/*public void readDataset(String directory)
	{
		try
		{
			Dataset dataset = TDBFactory.createDataset(directory);
			dataset.begin(ReadWrite.WRITE) ; 
			Model model = dataset.getDefaultModel();
			//TDBLoader.loadModel(model, file);
			dataset.commit();
			dataset.end();
		}
		catch(Exception ex)
		{
			System.out.println("##### Error Fonction: readDataset #####");
			System.out.println(ex.getMessage());
		}		
	}*/

	// create a dataset
	public Dataset createDataset()
	{
		try
		{
			String file = "C:\\Users\\Joe\\Desktop\\BLA\\PO2_CarredasDataTesting.ttl"; 
			String directory = "C:\\Users\\Joe\\Desktop\\TRIPLE_STORE";
			Dataset dataset = TDBFactory.createDataset(directory);
			dataset.begin(ReadWrite.WRITE) ; 
			Model model = dataset.getDefaultModel();
			TDBLoader.loadModel(model, file);
			dataset.commit();
			dataset.end();
			return dataset; 
		}
		catch(Exception ex)
		{
			System.out.println("##### Error Fonction: createDataset #####");
			System.out.println(ex.getMessage());
			return null;
		}
	}

	// create the identiConTo class and get its properties
	public IdentiConToClass createIdentiConToClass(String identiConToClassURI)
	{
		OntClass identiConToCl;
		IdentiConToClass idenCl = null;
		identiConToCl = model.getOntClass(identiConToClassURI);
		if(identiConToCl != null)
		{
			idenCl = new IdentiConToClass(identiConToCl);			
			checkIndividualsProperties(idenCl, identiConToCl);
			for (ExtendedIterator<OntClass> ontCl = identiConToCl.listSubClasses(); ontCl.hasNext();) 
			{
				OntClass thisOntCl = ontCl.next();
				checkIndividualsProperties(idenCl, thisOntCl);
			}		
			prepareLatticeProperties(idenCl);
		}
		//System.out.println("#### DONE #### createIdentiConToClass " + identiConToClassURI);	
		return idenCl;
	}

	public void disregardClass(String clURI)
	{
		IdentiConToClass cl = new IdentiConToClass(model.getOntClass(clURI));
		nonConsideredIdentiConToClasses.add(cl);
	}


	// get the identiConTo class properties (the properties used by all its individuals and its subclasses' individuals)
	public void checkIndividualsProperties(IdentiConToClass identiConTocl, OntClass cl)
	{
		Property statementProperty;
		RDFNode statementObject;
		Boolean punningClass = true;
		Resource n = null;
		//ArrayList<Statement> tobeAddedStatements = new ArrayList<>();
		for (ExtendedIterator<? extends OntResource> indiv = cl.listInstances(true); indiv.hasNext();) 
		{
			punningClass = false;
			Individual thisIndiv = indiv.next().asIndividual();				
			for (StmtIterator statement = model.listStatements(thisIndiv, null, n); statement.hasNext();) 
			{	
				Statement thisStatement = statement.next();			
				statementProperty = thisStatement.getPredicate();
				if(statementProperty.hasProperty(RDF.type, OWL.ObjectProperty))
				{										
					statementObject = thisStatement.getObject();
					addPropertyToIdentiConToClass(identiConTocl, statementProperty, statementObject.asResource().getURI());
				}
				else
				{					
					identiConTocl.addIdentiConToClassProperty(statementProperty, null);
				}
			}
			for (StmtIterator statement = model.listStatements(n, null, thisIndiv); statement.hasNext();) 
			{	
				Statement thisStatement = statement.next();			
				statementProperty = thisStatement.getPredicate();
				if(statementProperty.hasProperty(RDF.type, OWL.ObjectProperty))
				{	
					Resource invProp = null;
					if(model.contains(statementProperty, OWL.inverseOf, n))
					{
						invProp = statementProperty.getPropertyResourceValue(OWL.inverseOf);
					}
					else
					{
						if(model.contains(n, OWL.inverseOf, statementProperty))
						{
							StmtIterator st1 = model.listStatements(n, OWL.inverseOf, statementProperty); statement.hasNext(); 								
							invProp = st1.next().getSubject();				
						}
					}
					Property inverseProp;
					if(invProp == null)
					{
						inverseProp = model.createObjectProperty(statementProperty.getNameSpace() + "invOf_" + statementProperty.getLocalName());
						inverseProp.addProperty(OWL.inverseOf, statementProperty);
						statementProperty.addProperty(OWL.inverseOf, inverseProp);
					}
					else
					{
						inverseProp = model.getProperty(invProp.getURI());
					}			
					statementObject = thisStatement.getSubject();
					//Statement inverseOfStatement = model.createStatement(thisIndiv, inverseProp, thisStatement.getSubject());
					//model.add(inverseOfStatement);
					addPropertyToIdentiConToClass(identiConTocl, inverseProp, statementObject.asResource().getURI());						
				}				
			}
		}
		if(punningClass == true)
		{
			Individual thisIndiv = model.getIndividual(cl.getURI());
			for (StmtIterator statement = thisIndiv.listProperties(); statement.hasNext();) 
			{					
				Statement thisStatement = statement.next();			
				statementProperty = thisStatement.getPredicate();
				if(statementProperty.hasProperty(RDF.type, OWL.ObjectProperty))
				{										
					statementObject = thisStatement.getObject();
					identiConTocl.addIdentiConToClassProperty(statementProperty, getDirectType(statementObject.asResource().getURI()));
				}
				else
				{
					identiConTocl.addIdentiConToClassProperty(statementProperty, null);
				}
			}
		}
	}


	public void addPropertyToIdentiConToClass(IdentiConToClass cl, Property prop, String res)
	{
		RDFNode thisNode = getDirectType(res);		
		if(thisNode != null)
		{
			Boolean add = true;
			for(IdentiConToClass nonConsideredCl : nonConsideredIdentiConToClasses)
			{
				if(thisNode.asResource().getURI().equals(nonConsideredCl.idClass.getURI()))
				{
					add = false;
					break;
				}
				else
				{
					if(thisNode.asResource().hasProperty(RDFS.subClassOf,nonConsideredCl.idClass))
					{
						add = false;
						break;
					}
				}
			}
			if(add == true)
			{
				cl.addIdentiConToClassProperty(prop,thisNode);	
			}	
		}			
	}

	// get the direct parent of an individual
	public RDFNode getDirectType(String indiv)
	{
		RDFNode indivType = null;
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX owl:<http://www.w3.org/2002/07/owl#> " 
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX core: <http://opendata.inra.fr/resources/core#> "
				+ "PREFIX po2: <http://opendata.inra.fr/PO2/> " 
				+ "PREFIX IAO: <http://purl.obolibrary.org/obo/> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " 
				+ "SELECT ?indivClass "
				+ "WHERE { <"
				+ indiv + "> rdf:type ?indivClass"
				+ " }";

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		while(results.hasNext())
		{
			QuerySolution thisRow = results.next();
			indivType = thisRow.get("indivClass"); 
			if(!indivType.asResource().getURI().contains("http://www.w3.org/"))
			{
				return indivType;					
			}
		}
		qe.close();		
		//punning class
		indivType = model.getOntClass(indiv);
		return indivType;
	}

	// prepare the lattice properties by taking in consideration the discarded properties
	public void prepareLatticeProperties(IdentiConToClass cl)
	{
		for(String clURI : discardedProperties.keySet())
		{
			if(clURI.equals(cl.idClass.getURI()) || clURI.equals("all"))
			{
				for(String discPropURI : discardedProperties.get(clURI))
				{
					Property discardedProp = model.getProperty(discPropURI);
					if(discardedProp != null)
					{
						if(!cl.discardedProperties.contains(discardedProp))
						{
							cl.discardedProperties.add(discardedProp);
						}						
					}
				}
			}
		}
		cl.prepareLocalLatticeProperties();	
	}

	// create all the identiConTo classes dependent to the main identiConTo class
	public void createDependentIdentiConToClasses(IdentiConToClass cl)
	{
		IdentiConToClass depIdentiConToCl = null;
		for(Property prop : cl.identiConToClassProperties.keySet())
		{
			if(cl.latticeProperties.contains(prop))
			{
				for(RDFNode dependentNode : cl.identiConToClassProperties.get(prop))
				{
					if(dependentNode != null)
					{
						if(checkInDependentClasses(dependentNode) == false)
						{
							depIdentiConToCl = createIdentiConToClass(dependentNode.asResource().getURI());
							ArrayList<IdentiConToClass> subClasses = checkForSubClass(depIdentiConToCl);
							if(subClasses != null)
							{
								for(IdentiConToClass subClass : subClasses)
								{
									dependentIdentiConToClasses.remove(subClass);	
									nonConsideredIdentiConToClasses.add(subClass);
								}								
								dependentIdentiConToClasses.add(depIdentiConToCl);
							}
							else
							{
								IdentiConToClass parentClass = checkForParentClass(depIdentiConToCl);
								if(parentClass == null)
								{
									dependentIdentiConToClasses.add(depIdentiConToCl);
								}
							}													
							createDependentIdentiConToClasses(depIdentiConToCl);				
						}						
					}
				}
			}
		}
	}

	public IdentiConToClass checkForParentClass(IdentiConToClass cl)
	{
		for(IdentiConToClass depCL : dependentIdentiConToClasses)
		{
			if(depCL.idClass.hasSubClass(cl.idClass))
			{
				return depCL;
			}
		}		
		return null;
	}

	public ArrayList<IdentiConToClass> checkForSubClass(IdentiConToClass cl)
	{
		ArrayList<IdentiConToClass> subClasses = new ArrayList<>();
		for(IdentiConToClass depCL : dependentIdentiConToClasses)
		{
			if(cl.idClass.hasSubClass(depCL.idClass))
			{
				subClasses.add(depCL);
			}
		}
		if(!subClasses.isEmpty())
		{
			return subClasses;
		}
		return null;
	}

	public Boolean checkInDependentClasses(RDFNode node)
	{
		for(IdentiConToClass dependantCl : dependentIdentiConToClasses)
		{
			if(dependantCl.idClass.getURI().equals(node.asResource().getURI()))
			{
				return true;
			}
		}
		for(IdentiConToClass dependantCl : nonConsideredIdentiConToClasses)
		{
			if(dependantCl.idClass.getURI().equals(node.asResource().getURI()))
			{
				return true;
			}
		}
		return false;
	}

	public void createAllLattices(ArrayList<IdentiConToClass> listOfIdentiConToClasses)
	{
		for(IdentiConToClass cl : listOfIdentiConToClasses)
		{
			cl.constructClassLattice();	
			System.out.println("Done: Lattice " + cl.idClass.getLocalName());
		}
	}

	public ArrayList<Context> getValidContextsByPercentage(IdentiConToClass IdenClass, int percentage)
	{
		ArrayList<Context> validContexts = new ArrayList<>();
		ArrayList<Resource> listOfIndivs = new ArrayList<>();
		listOfIndivs = getIndivsOfClass(IdenClass.idClass);	
		validContexts = IdenClass.localLattice.checkPercentageOfValidContexts(listOfIndivs, percentage);
		/*		IdenClass.localLattice.setValidContexts(validContexts);
		IdenClass.localLattice.setSimilarityContext(validContexts.get(0));
		IdenClass.localLattice.outputvalidContexts();	*/
		//System.out.println("#### DONE #### checkValidContextsByPercentage");
		return validContexts;
	}

	public ArrayList<Context> getAllValidContexts(IdentiConToClass IdenClass)
	{
		ArrayList<Context> validContexts = new ArrayList<>();
		ArrayList<Resource> listOfIndivs = new ArrayList<>();
		listOfIndivs = getIndivsOfClass(IdenClass.idClass);	
		validContexts = IdenClass.localLattice.checkAllValidContexts(listOfIndivs);
		System.out.println("#### DONE #### checkValidContextsByPercentage");
		return validContexts;
	}

	// get all the individuals from the inferred model of a specific class
	public ArrayList<Resource> getIndivsOfClass(RDFNode desClass)
	{
		Resource indiv = null;
		ArrayList<Resource> listOfIndivs = new ArrayList<>();
		for (ResIterator j = model.listSubjectsWithProperty(RDF.type, desClass); j.hasNext();) 
		{
			indiv = j.nextResource();
			indiv = infModel.getResource(indiv.getURI());
			listOfIndivs.add(indiv);	
		}
		return listOfIndivs;
	}

	public void getAllContextsDependantClasses(IdentiConToClass cl, ArrayList<IdentiConToClass> depCl)
	{
		for(Context valCont : cl.localLattice.validContexts)
		{
			valCont.dependantClasses.addAll(getContextDependantClasses(cl, valCont));
		}
		for(IdentiConToClass depClass : depCl)
		{
			for(Context depValCont : depClass.localLattice.validContexts)
			{
				depValCont.dependantClasses.addAll(getContextDependantClasses(depClass, depValCont));
			}
		}	
	}

	public ArrayList<IdentiConToClass> getContextDependantClasses(IdentiConToClass IdenClass, Context cnt)
	{
		ArrayList<IdentiConToClass> dependantClasses = new ArrayList<>();
		for(Property p : cnt.getContextProperties())
		{
			if(IdenClass.identiConToClassProperties.containsKey(p))
			{
				ArrayList<RDFNode> dependantNodes = new ArrayList<>();
				dependantNodes = IdenClass.identiConToClassProperties.get(p);
				if(dependantNodes!= null)
				{
					for(RDFNode n : dependantNodes)
					{
						if(n!=null)
						{
							dependantClasses.add(getIdentiConToClassFromNode(n));
						}

					}
				}			
			}
		}
		return dependantClasses;
	}

	// get the IdentiConTo object from an RDFNode
	public IdentiConToClass getIdentiConToClassFromNode(RDFNode rdfnode)
	{
		IdentiConToClass idCl = null;
		for(IdentiConToClass dependantClass : dependentIdentiConToClasses)
		{
			if(dependantClass.idClass.getURI().equals(rdfnode.asResource().getURI()))
			{
				return dependantClass;
			}
		}
		return idCl;
	}

	public void generateGlobalContextProperty(OntClass gC, GlobalContext globalCont)
	{
		String propName = PO2 + "identiConTo_" + gC.getLocalName();
		ObjectProperty identiConTo = model.getObjectProperty(propName);
		if(identiConTo == null)
		{
			identiConTo = model.createObjectProperty(propName);
		}
		globalCont.identiConToProperty = identiConTo;
		identiConTo.addProperty(model.getProperty(OBO+"IAO_0000115"), gC);
	}

	public GlobalContext getSimilarGlobalContext(GlobalContext globalCont)
	{	
		for(GlobalContext gC : allGlobalContexts)
		{
			if(gC.allLocalContexts.size() == globalCont.allLocalContexts.size())
			{
				Boolean exists = true;
				for(Lattice lat : globalCont.allLocalContexts.keySet())
				{
					if(gC.allLocalContexts.containsKey(lat))
					{
						if(!gC.allLocalContexts.get(lat).contextID.equals(globalCont.allLocalContexts.get(lat).contextID))
						{
							exists = false;
							break;
						}
					}
					else
					{
						exists = false;
						break;
					}
				}
				if(exists == true)
				{
					return gC;
				}
			}
		}
		return null;
	}

	public void saveGlobalContext(GlobalContext globalCont, Resource res1, Resource res2)
	{
		GlobalContext similarGC = getSimilarGlobalContext(globalCont);
		String i = Integer.toString(counter);
		if(similarGC == null)
		{
			OntClass globalContextCl = createOntologyContext(globalCont);
			generateGlobalContextProperty(globalContextCl, globalCont);
			globalCont.addStatement(model, res1, res2);
			globalCont.addPair(i);
			gbcounter++;
			globalCont.id = "GC_" + gbcounter;
			allGlobalContexts.add(globalCont);
			emptyGlobalContext.id = "GC_" + "0";
			emptyGlobalContext.addParentContext(null, globalCont);
		}
		else
		{
			similarGC.addStatement(model, res1, res2);			
			similarGC.addPair(i);
		}
	}

	public OntClass createOntologyContext(GlobalContext gc)
	{
		OntClass globalCont = null;
		if(gc!= null)
		{
			for(Lattice lat : gc.allLocalContexts.keySet())
			{
				gc.id = gc.id + gc.allLocalContexts.get(lat).contextID + "_";
			}	
			globalCont = model.getOntClass(PO2+ "gc_" + gc.idClass.idClass.getLocalName() + "_" + gc.id);
			if(globalCont == null);
			{
				for(Lattice lat : gc.allLocalContexts.keySet())
				{
					globalCont = model.createClass(PO2+ "gc_" + gc.idClass.idClass.getLocalName() + "_" + gc.id);
					Statement st = model.createStatement(globalCont, RDFS.subClassOf, model.createResource(OBO+ "BFO_0000034"));
					model.add(st);
					Resource rs = model.createResource();			
					Property IAOdefinition = model.getProperty(OBO+"IAO_0000115");
					globalCont.addProperty(IAOdefinition, rs);
					Individual something = model.getIndividual(PO2+"something");
					if(something == null)
					{
						something = model.createIndividual(PO2+"something", model.getResource("http://www.semanticweb.org/joe/ontologies/2017/2/untitled-ontology-577#Jus"));
					}
					rs.addProperty(model.getProperty(PO2+"hasForType"), lat.latticeClass.idClass);
					for(Property prop : gc.allLocalContexts.get(lat).getContextProperties())
					{
						if(!prop.equals(RDF.type))
						{
							if(prop.hasProperty(RDF.type, OWL.DatatypeProperty))
							{
								rs.addProperty(prop, "something");
							}
							else
							{
								rs.addProperty(prop, something);
							}			
						}							
					}
				}
			}
		}
		return globalCont;
	}


	public void identiConToMax(IdentiConToClass cbl, ArrayList<IdentiConToClass> Cdep, Resource res1, Resource res2)
	{
		GlobalContext globalContext = new GlobalContext();
		temporaryGlobalContexts.add(globalContext);
		counter++;
		for (ListIterator<GlobalContext> file = temporaryGlobalContexts.listIterator(); file.hasNext();)
		{
			GlobalContext gC = file.next();
			Pair visitedPairs = new Pair();
			Lattice resourceLattice = getSpecificLatticeFromIndividual(res1);
			globalContext = compareTwoResources(res1, res2, visitedPairs, null, resourceLattice, null, gC, file);			
			globalContext.idClass = identiConToClass;
			saveGlobalContext(globalContext, res1, res2);
		}		
		System.out.println("Pair " + counter + " number of global contexts --> " + temporaryGlobalContexts.size());
		temporaryGlobalContexts.clear();
	}


	public void insertIdentiConToRelationships(IdentiConToClass cl, ArrayList<IdentiConToClass> dependantClasses)
	{
		counter = 0;
		ArrayList<Resource> listOfIndivs = getIndivsOfClass(identiConToClass.idClass);
		int numbIndivs = listOfIndivs.size();
		System.out.println("Number of individuals in target class: " + numbIndivs);
		int numComb = (numbIndivs * (numbIndivs -1)) / 2;
		System.out.println("Number of pairs: " + numComb);
		ArrayList<Resource> listOfIndivs2 = new ArrayList<>();
		listOfIndivs2.addAll(listOfIndivs);
		for (Resource res1 :listOfIndivs) 
		{
			listOfIndivs2.remove(res1);
			for (Resource res2 : listOfIndivs2)  
			{	
				identiConToMax(cl, dependantClasses, res1, res2);
			}
		}	
		outputAllGlobalContexts();
		outputGlobalContextHierarchy();
		outputEachGlobalContextSize();
		outputNumberOfIdentityStatements();
		System.out.println("##### Size : " + allGlobalContexts.size());
		
	}

	public void outputGlobalContextHierarchy()
	{
		emptyGlobalContext.outputAllParentontexts();
	}

	public void outputEachGlobalContextSize()
	{
		for(GlobalContext gc : allGlobalContexts)
		{
			//System.out.println(gc.id + " proper size: " + gc.pairNumbers.size());
			System.out.println(gc.id + " total size: " + gc.getGlobalContextSize(0));
		}
	}

	public void outputNumberOfIdentityStatements()
	{
		int numberOfIdentityStatements = 0;
		for(GlobalContext gc : allGlobalContexts)
		{
			numberOfIdentityStatements = numberOfIdentityStatements + gc.statements.size();
		}
		System.out.println("Total number of statements = " + numberOfIdentityStatements);
	}

	public void outputAllGlobalContexts()
	{		
		System.out.println(" ");
		int numbOfSt = 0;
		for(GlobalContext gc : allGlobalContexts)
		{
			System.out.println("###############");
			gc.outputGlobalContext();
			gc.outputStatements();		
			numbOfSt = numbOfSt + gc.statements.size();
		}
		System.out.println(" ");
		System.out.println("Total number of Global Contexts: " + allGlobalContexts.size());
		System.out.println("Total number of IdentiConTo Statements: " + numbOfSt);	
	}

	public GlobalContext compareTwoResources(Resource res1, Resource res2, Pair visitedPairs, Property propSrc, Lattice currentLat, Lattice latSrc, GlobalContext globalContext, ListIterator<GlobalContext> file)
	{	
		Context highestCommonContext = findHighestCommonContext(res1, res2);
		if(highestCommonContext.checkIfLastContext() == false)
		{
			highestCommonContext = removeDifferentDataProperties(res1, res2, highestCommonContext);
		}	
		visitedPairs.addPair(res1, res2);
		if(globalContext.allLocalContexts.containsKey(currentLat))
		{
			Context localContextOfGlobalContext = globalContext.allLocalContexts.get(currentLat);	
			if(localContextOfGlobalContext.contextID.equals(highestCommonContext.contextID))
			{
				visitedPairs.addPair(res1, res2);
				compareObjectProperties(res1, res2, visitedPairs, propSrc, currentLat, latSrc, globalContext, file);
			}
			else
			{	
				addGlobalContextToQueue(currentLat, highestCommonContext, file);		
				if(currentLat.checkIfSubContextOf(localContextOfGlobalContext, highestCommonContext) == true)
				{				
					compareObjectProperties(res1, res2, visitedPairs, propSrc, currentLat, latSrc, globalContext, file);
				}
				else
				{					
					removePropertyFromGlobalContext(globalContext, latSrc, propSrc);	
				}			
			}
		}
		else
		{
			globalContext.allLocalContexts.put(currentLat, highestCommonContext);
			compareObjectProperties(res1, res2, visitedPairs, propSrc, currentLat, latSrc, globalContext, file);
		}

		return globalContext;
	}

	public Context removeDifferentDataProperties(Resource res1, Resource res2, Context cnt)
	{
		Context newContext = cnt;
		ArrayList<Property> newListOfProps = new ArrayList<>();
		for(Property prop : cnt.contextProperties)
		{
			if(!prop.hasProperty(RDF.type, OWL.ObjectProperty))
			{
				Boolean result = compareTwoDataPropertiesValue(res1, res2, prop);
				if(result == true)
				{
					newListOfProps.add(prop);
				}
			}
			else
			{
				newListOfProps.add(prop);
			}
		}
		if(newListOfProps.size() != cnt.contextProperties.size())
		{
			Lattice lat = getSpecificLatticeFromIndividual(res1);
			newContext = lat.searchContextByProperties(newListOfProps);
		}
		return newContext;
	}

	public void compareObjectProperties(Resource res1, Resource res2, Pair visitedPairs, Property propSrc, Lattice currentLat, Lattice latSrc, GlobalContext globalContext, ListIterator<GlobalContext> file)
	{
		Context currentContext = globalContext.allLocalContexts.get(currentLat);
		for(Property objProp : currentContext.contextProperties)
		{
			if(objProp.hasProperty(RDF.type, OWL.ObjectProperty))
			{
				HashMap<RDFNode, ArrayList<Resource>> hMap1 = new HashMap<>();
				HashMap<RDFNode, ArrayList<Resource>> hMap2 = new HashMap<>();	
				hMap1 = getObjPropsValues(res1, objProp);
				hMap1.putAll(getInvObjPropsValues(res1, objProp));
				hMap2 = getObjPropsValues(res2, objProp);
				hMap2.putAll(getInvObjPropsValues(res2, objProp));
				if(haveSameStructure(hMap1, hMap2) == false)
				{
					removePropertyFromGlobalContext(globalContext, currentLat, objProp);					
				}
				else
				{
					compareHashMapResources(hMap1, hMap2, visitedPairs, objProp, currentLat, latSrc, globalContext, file);
				}		
			}
		}
	}


	public HashMap<RDFNode, ArrayList<Resource>> getObjPropsValues(Resource res, Property objProperty)
	{
		HashMap<RDFNode, ArrayList<Resource>> hMap = new HashMap<>();
		Resource n = null;
		for(StmtIterator st = model.listStatements(res, objProperty, n); st.hasNext();)
		{
			Statement thisStatement = st.next();
			Resource thisObject = thisStatement.getObject().asResource();
			IdentiConToClass cl = getIndividualType(thisObject.getURI());
			if(cl!=null)
			{
				if(hMap.containsKey(cl.idClass))
				{
					hMap.get(cl.idClass).add(thisObject);
				}
				else
				{
					ArrayList<Resource> arrayResources = new ArrayList<>();
					arrayResources.add(thisObject);
					hMap.put(cl.idClass,arrayResources);
				}
			}			
		}
		return hMap;
	}

	public HashMap<RDFNode, ArrayList<Resource>> getInvObjPropsValues(Resource res, Property objProperty)
	{
		HashMap<RDFNode, ArrayList<Resource>> hMap = new HashMap<>();
		Resource invProp = null;
		if(model.contains(objProperty, OWL.inverseOf, invProp))
		{
			invProp = objProperty.getPropertyResourceValue(OWL.inverseOf);
		}
		else
		{
			if(model.contains(invProp, OWL.inverseOf, objProperty))
			{
				StmtIterator st1 = model.listStatements(invProp, OWL.inverseOf, objProperty); st1.hasNext(); 
				invProp = st1.next().getSubject();
			}
		}				
		if(invProp !=null)
		{			
			Property inverseProp = model.getProperty(invProp.getURI());
			for(StmtIterator st = model.listStatements(null, inverseProp, res); st.hasNext();)
			{
				Statement thisStatement = st.next();
				Resource thisSubject = thisStatement.getSubject().asResource();
				IdentiConToClass cl = getIndividualType(thisSubject.getURI());
				if(cl!=null)
				{
					if(hMap.containsKey(cl.idClass))
					{
						hMap.get(cl.idClass).add(thisSubject);
					}
					else
					{
						ArrayList<Resource> arrayResources = new ArrayList<>();
						arrayResources.add(thisSubject);
						hMap.put(cl.idClass,arrayResources);
					}
				}				
			}
		}
		return hMap;
	}

	public void compareHashMapResources(HashMap<RDFNode, ArrayList<Resource>> hMap1, HashMap<RDFNode, ArrayList<Resource>> hMap2, Pair visitedPairs, Property propSrc, Lattice currentLat, Lattice latSrc, GlobalContext globalContext, ListIterator<GlobalContext> file)
	{
		if(!hMap1.isEmpty() && !hMap2.isEmpty())
		{
			for(RDFNode n1 : hMap1.keySet())
			{
				ArrayList<Resource> list1 = hMap1.get(n1);
				ArrayList<Resource> list2 = hMap2.get(n1);
				for(int i = 0 ; i < list1.size() ; i++)
				{
					Resource res1 = list1.get(i);
					Resource res2 = list2.get(i);
					Lattice thisLat = getSpecificLatticeFromIndividual(res1);
					if(visitedPairs.checkIfPairExists(res1, res2)== false)
					{
						compareTwoResources(res1, res2, visitedPairs, propSrc, thisLat, currentLat, globalContext, file);
					}	
				}


				/*for(Resource res1: hMap1.get(n1))
				{
					for(Resource res2: hMap2.get(n1))
					{
						Lattice thisLat = getSpecificLatticeFromIndividual(res1);
						if(visitedPairs.checkIfPairExists(res1, res2)== false)
						{
							compareTwoResources(res1, res2, visitedPairs, propSrc, thisLat, currentLat, globalContext, file);
						}	
					}
				}*/
			}
		}	
	}


	public void mostLikelySimilar(ArrayList<Resource> list1, ArrayList<Resource> list2)
	{

	}


	/*public HashMap<RDFNode, ArrayList<Resource>> getObjectPropertyValues(Resource res, Property objProperty)
	{
		HashMap<RDFNode, ArrayList<Resource>> hMap = new HashMap<>();
		for (StmtIterator statement = res.listProperties(objProperty); statement.hasNext();) 
		{					
			Statement thisStatement = statement.next();	
			Resource thisObject = thisStatement.getObject().asResource();
			RDFNode n = getDirectType(thisObject.getURI());

			if(hMap.containsKey(n))
			{
				hMap.get(n).add(thisObject);
			}
			else
			{
				ArrayList<Resource> arrayResources = new ArrayList<>();
				arrayResources.add(thisObject);
				hMap.put(n,arrayResources);
			}

		}
		return hMap;
	}*/

	public Boolean haveSameStructure(HashMap<RDFNode, ArrayList<Resource>> hMap1, HashMap<RDFNode, ArrayList<Resource>> hMap2)
	{	
		if(hMap1.size() != hMap2.size())
		{
			return false;
		}
		else
		{
			for(RDFNode n1 : hMap1.keySet())
			{
				if(hMap2.containsKey(n1)==true)
				{
					if(hMap1.get(n1).size()!= hMap2.get(n1).size())
					{
						return false;
					}
				}
				else
				{
					return false;
				}
			}
		}
		return true;
	}


	/*public void compareHashMapResources(HashMap<RDFNode, ArrayList<Resource>> hMap1, HashMap<RDFNode, ArrayList<Resource>> hMap2, Pair visitedPairs, Property propSrc, Lattice currentLat, Lattice latSrc, GlobalContext globalContext, ListIterator<GlobalContext> file)
	{
		if(!hMap1.isEmpty() && !hMap2.isEmpty())
		{
			for(RDFNode n1 : hMap1.keySet())
			{
				ArrayList<Resource> list1 = hMap1.get(n1);
				ArrayList<Resource> list2 = hMap2.get(n1);
				if(list1 == null)
				{
					//Lattice thisLat = getSpecificLatticeFromIndividual(list2.get(0));
					removePropertyFromGlobalContext(globalContext, latSrc, propSrc);
					//Context lastContext = thisLat.getLastContext();
					//globalContext.allLocalContexts.replace(thisLat,globalContext.allLocalContexts.get(thisLat), lastContext);
					//globalContext.allLocalContexts.put(thisLat, lastContext);
				}
				else
				{
					if(list2 == null)
					{
						//Lattice thisLat = getSpecificLatticeFromIndividual(list1.get(0));
						removePropertyFromGlobalContext(globalContext, latSrc, propSrc);
						//Context lastContext = thisLat.getLastContext();
						//globalContext.allLocalContexts.replace(thisLat,globalContext.allLocalContexts.get(thisLat), lastContext);
						//globalContext.allLocalContexts.put(thisLat, lastContext);
					}
					else
					{
						if(list1.size() != list2.size())
						{
							//Lattice thisLat = getSpecificLatticeFromIndividual(list1.get(0));
							removePropertyFromGlobalContext(globalContext, latSrc, propSrc);
							//Context lastContext = thisLat.getLastContext();
							//globalContext.allLocalContexts.replace(thisLat,globalContext.allLocalContexts.get(thisLat), lastContext);
							//globalContext.allLocalContexts.put(thisLat, lastContext);
						}
						else
						{
							Lattice thisLat = getSpecificLatticeFromIndividual(list1.get(0));
							for(int i =0 ; i <list1.size(); i++)
							{	
								Resource res1 = list1.get(i);
								Resource res2 = list2.get(i);
								if(!visitedPairs.checkIfPairExists(res1, res2))
								{
									compareTwoResources(res1, res2, visitedPairs, propSrc, thisLat, currentLat, globalContext, file);
								}					
							}
						}
					}
				}			
			}
		}





		for(Resource res1: hMap1.get(n1))
				{
					for(Resource res2: hMap2.get(n1))
					{
						Lattice thisLat = getSpecificLatticeFromIndividual(res1);
						if(visitedPairs.checkIfPairExists(res1, res2)== false)
						{
							compareTwoResources(res1, res2, visitedPairs, propSrc, thisLat, currentLat, globalContext, file);
						}	
					}
				}

	}
	 */
	public void removePropertyFromGlobalContext(GlobalContext globalContext, Lattice latSrc, Property unwantedProperty)
	{	
		Context oldContext = globalContext.allLocalContexts.get(latSrc);
		if(oldContext!=null)
		{
			ArrayList<Property> properties = new ArrayList<>();
			properties.addAll(oldContext.getContextProperties());
			properties.remove(unwantedProperty);
			Context newContext = latSrc.searchContextByProperties(properties);
			globalContext.allLocalContexts.replace(latSrc, oldContext, newContext);	
		}

	}

	// compare two data properties values
	// similarity measures can be used later
	public Boolean compareTwoDataPropertiesValue(Resource res1, Resource res2, Property prop)
	{
		Boolean identicalInThisProperty = false;
		if(res1 != null && res2 != null && prop != null)
		{
			Statement x1 = res1.getRequiredProperty(prop);	
			RDFNode n1 = x1.getObject();
			Statement x2 = res2.getRequiredProperty(prop);
			RDFNode n2 = x2.getObject();
			if(n1.equals(n2))
			{
				if(n1 != null)
				{
					identicalInThisProperty = true;
				}
			}
		}
		return identicalInThisProperty;
	}

	public Context findHighestCommonContext(Resource res1, Resource res2)
	{
		Lattice lat = getSpecificLatticeFromIndividual(res1);
		Context highestCommonContext = null;
		ArrayList<Property> commonProperties = new ArrayList<>();
		for(Property p : lat.latticeProperties)
		{
			if(res1.hasProperty(p) && res2.hasProperty(p))
			{
				commonProperties.add(p);
			}
			else
			{				
				Resource invProp = p.getPropertyResourceValue(OWL.inverseOf);
				if(invProp!=null)
				{						
					Property invProperty = model.getProperty(invProp.getURI());
					if(model.contains(null, invProperty, res1))
					{
						if(model.contains(null, invProperty, res2))
						{
							commonProperties.add(p);
						}
					}
				}
			}
		}
		highestCommonContext = lat.searchContextByProperties(commonProperties);
		return highestCommonContext;
	}

	// get the class' lattice of a certain individual
	public Lattice getSpecificLatticeFromIndividual(Resource individual)
	{
		IdentiConToClass indType = getIndividualType(individual.getURI());
		return indType.localLattice;
		/*Lattice lat = null;
		if(identiConToClass.idClass.getURI().equals(indType.asResource().getURI()))
		{
			lat = identiConToClass.localLattice;
		}
		else
		{
			for(IdentiConToClass depClasses : dependentIdentiConToClasses)
			{
				if(depClasses.idClass.getURI().equals(indType.asResource().getURI()))
				{
					lat = depClasses.localLattice;
					break;
				}
			}
		}
		return lat;*/
	}


	public IdentiConToClass getIndividualType(String indiv)
	{
		RDFNode indivType = null;
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX owl:<http://www.w3.org/2002/07/owl#> " 
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX core: <http://opendata.inra.fr/resources/core#> "
				+ "PREFIX po2: <http://opendata.inra.fr/PO2/> " 
				+ "PREFIX IAO: <http://purl.obolibrary.org/obo/> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " 
				+ "SELECT ?indivClass "
				+ "WHERE { <"
				+ indiv + "> rdf:type ?indivClass"
				+ " }";

		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		while(results.hasNext())
		{
			QuerySolution thisRow = results.next();
			indivType = thisRow.get("indivClass"); 
			if(indivType == null)
			{
				indivType = model.getOntClass(indiv);
			}
			if(!indivType.asResource().getURI().contains("http://www.w3.org/"))
			{
				if(identiConToClass != null)
				{
					if(identiConToClass.idClass.getURI().equals(indivType.asResource().getURI()))
					{
						return identiConToClass;
					}
					else
					{
						for(IdentiConToClass depCl : dependentIdentiConToClasses)
						{
							if(depCl.idClass.getURI().equals(indivType.asResource().getURI()))
							{
								return depCl;
							}
						}
					}	
				}					
			}
		}
		qe.close();		
		//punning class





		return null;
	}


	public void addGlobalContextToQueue(Lattice lat, Context cnt, ListIterator<GlobalContext> file)
	{
		if(checkIfGlobalContextExists(lat, cnt, temporaryGlobalContexts) == false)
		{
			if(checkIfGlobalContextExists(lat, cnt, createdContexts) == false)
			{
				GlobalContext newGlobalContext = new GlobalContext();
				newGlobalContext.allLocalContexts.clear();
				newGlobalContext.allLocalContexts.put(lat, cnt);
				file.add(newGlobalContext);
				file.previous();
				//System.out.println("file next index: " + file.nextIndex());
			}
		}		

	}

	public Boolean checkIfGlobalContextExists(Lattice lat, Context cnt, ArrayList<GlobalContext> globalContextsList)
	{
		Boolean exists = false;
		for(GlobalContext globCnt: globalContextsList)
		{
			if(globCnt.allLocalContexts.containsKey(lat))
			{
				Context cnt1 = globCnt.allLocalContexts.get(lat);
				if(cnt1.contextID.equals(cnt.contextID))
				{
					exists = true;
				}
			}
			if(exists == true)
			{
				return true;
			}
		}
		return exists;
	}

	// Count Individuals
	public int countIndividuals() 
	{	
		int size = 0;
		for(ExtendedIterator<Individual> ind = model.listIndividuals(); ind.hasNext();)
		{
			ind.next();
			size++;
		}
		return size;
	}

	// Count Classes
	public int countClasses() 
	{
		int size = 0;
		for(ExtendedIterator<OntClass> cl = model.listClasses(); cl.hasNext();)
		{
			cl.next();
			size++;
		}
		return size;
	}

	// Count Properties
	public int countProperties() 
	{
		int size = 0;
		for(ExtendedIterator<OntProperty> prop = model.listAllOntProperties(); prop.hasNext();)
		{
			prop.next();
			size++;
		}
		return size;
	}

	// Count Object Properties
	public int countObjectProperties() 
	{
		int size = 0;
		for(ExtendedIterator<ObjectProperty> objProp = model.listObjectProperties(); objProp.hasNext();)
		{
			objProp.next();
			size++;
		}
		return size;
	}

	// Count Data Properties
	public int countDataTypeProperties() 
	{		
		int size = 0;
		for(ExtendedIterator<DatatypeProperty> dataProp = model.listDatatypeProperties(); dataProp.hasNext();)
		{
			dataProp.next();
			size++;
		}
		return size;
	}

	// Count Annotation Properties
	public int countAnnotationProperties() 
	{		
		int size = 0;
		for(ExtendedIterator<AnnotationProperty> annotProp = model.listAnnotationProperties(); annotProp.hasNext();)
		{
			annotProp.next();
			size++;
		}
		return size;
	}

	// Count Statements
	public int countStatements()
	{
		int size = 0;		
		for(StmtIterator st = model.listStatements(); st.hasNext();)
		{
			st.next();
			size ++;
		}
		return size;
	}

	// Count the number of classes and individuals in the connex graph (identiConToClass + all dependent Classes)
	public void connexGraphStats()
	{
		int size = 0;
		for(IdentiConToClass cl : dependentIdentiConToClasses)
		{		
			for (ExtendedIterator<? extends OntResource> ite = cl.idClass.listInstances(true); ite.hasNext();) 
			{
				ite.next();
				size ++;
			}			
		}
		System.out.println("Number of Classes in the connex graph: " + dependentIdentiConToClasses.size());
		System.out.println("Number of Individuals in the connex graph: " + size);
	}




}
