package program;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import mvcController.*;
import mvcModel.ModelFunctions;
import mvcView.ViewFunctions;

public class Decide 
{
	public static String PO2 = "http://opendata.inra.fr/PO2/";
	public static String OBO = "http://purl.obolibrary.org/obo/";
	public static String core = "http://opendata.inra.fr/resources/core#";
	public static String OM = "http://www.wurvoc.org/vocabularies/om-1.6/";
	public static String IAO = "http://purl.obolibrary.org/obo/";
	public static String skos = "http://www.w3.org/2004/02/skos/core#";
	public static String echantillon_po2 = "data/Echantillon_PO2_CellExtraDry.rdf";
	public static String exemple_jus = "data/exemple_jus.owl";

	public static void main(String[] args) 
	{	
		////////////////TIME/////////////////
		System.out.println("---- START PROGRAM ----  ");
		long startTime = System.currentTimeMillis();
		/////////////////////////////////////



		ModelFunctions theModel = new ModelFunctions();
		ViewFunctions theView = new ViewFunctions();
		ControllerFunctions theController = new ControllerFunctions(theModel, theView);

		// this HashMap serves at discarding some unwanted properties in the lattice
		// the first column indicates the class in which we want to discard the list of properties from the second column
		// the string "all" indicates to remove a property from all identiConTo classes

		ArrayList<String> discardedClasses = new ArrayList<>();
		discardedClasses.add(PO2+"observation");
		//discardedClasses.add(PO2+"step");
		
		HashMap<String, ArrayList<String>> discardedProperties = new HashMap<>();
		ArrayList<String> discProps = new ArrayList<>();
		discProps.add(skos+"altLabel");
		discProps.add(skos+"prefLabel");
		discProps.add(PO2+"databaseID");
		discProps.add(PO2+"minSupport");
		discProps.add(PO2+"minKernel");
		discProps.add(PO2+"maxKernel");
		discProps.add(PO2+"isSingularMeasureOf");
		discardedProperties.put("all", discProps);
	
/*		ArrayList<ArrayList<String>> indispensableProps = new ArrayList<ArrayList<String>>();
		
		ArrayList<String> firstPairedProps = new ArrayList<>();
		firstPairedProps.add(PO2+ "maxKernel");
		firstPairedProps.add(PO2+ "minKernel");
		firstPairedProps.add(PO2+ "maxSupport");
		firstPairedProps.add(PO2+ "minSupport");
		firstPairedProps.add(PO2+ "hasForUnitOfMeasure");
		
		ArrayList<String> secondPairedProps = new ArrayList<>();
		secondPairedProps.add(PO2+ "hasForValue");
		secondPairedProps.add(PO2+ "hasForMeasurementScale");		
		
		indispensableProps.add(firstPairedProps);
		indispensableProps.add(secondPairedProps);*/


		


		String targetClass = PO2+"mixture";	
		theController.decide(echantillon_po2, targetClass, discardedClasses, discardedProperties, null);
		//String targetClass = "http://www.semanticweb.org/ontoJus#Jus";		
		//theController.decide(exemple_jus, targetClass, null, null, null);


		////////////////TIME/////////////////
		System.out.print("---- END PROGRAM ----  ");
		Long endTime = System.currentTimeMillis();
		Long totalTimeMs = (endTime - startTime) ;
		System.out.println(String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(totalTimeMs),
				TimeUnit.MILLISECONDS.toSeconds(totalTimeMs) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTimeMs))));
		/////////////////////////////////////

	}

}
