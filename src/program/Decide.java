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


	public static void main(String[] args) 
	{	
		////////////////TIME/////////////////
		System.out.println("---- START PROGRAM ----  ");
		long startTime = System.currentTimeMillis();
		/////////////////////////////////////


		ModelFunctions theModel = new ModelFunctions();
		ViewFunctions theView = new ViewFunctions();
		ControllerFunctions theController = new ControllerFunctions(theModel, theView);

		
		// the classes that can be discarded from the algorithm
		ArrayList<String> discardedClasses = new ArrayList<>();
		discardedClasses.add(PO2+"observation");
		
		// this HashMap serves at discarding some unwanted properties in the lattice
		// the first column indicates the class in which we want to discard the list of properties from the second column
		// the string "all" indicates to remove a property from all identiConTo classes
			HashMap<String, ArrayList<String>> listOfDiscardedProperties = new HashMap<>();
			/*ArrayList<String> discardedProps = new ArrayList<>();
		discardedProps.add(skos+"altLabel");
		discardedProps.add(skos+"prefLabel");
		discardedProps.add(PO2+"databaseID");
		listOfDiscardedProperties.put("all", discardedProps);*/
		
		ArrayList<ArrayList<String>> listOfCoupleProperties = new ArrayList<ArrayList<String>>();
		/*ArrayList<String> firstCoupleProps = new ArrayList<>();
		firstCoupleProps.add(PO2+ "hasForValue");
		firstCoupleProps.add(PO2+ "hasForMeasurementScale");
		ArrayList<String> secondCoupleProps = new ArrayList<>();
		secondCoupleProps.add(PO2+ "hasValue");
		secondCoupleProps.add(PO2+ "hasForUnitOfMeasure");	
		listOfCoupleProperties.add(firstCoupleProps);
		listOfCoupleProperties.add(secondCoupleProps);*/

		String targetClass = PO2+"mixture";	
		// the algorithm takes as input the dataset, the target class, the discarded classes, the discarded properties, and the 
		theController.decide(echantillon_po2, targetClass, discardedClasses, listOfDiscardedProperties, listOfCoupleProperties, null);


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
