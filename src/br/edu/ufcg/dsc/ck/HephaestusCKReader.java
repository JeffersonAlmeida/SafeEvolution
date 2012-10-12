package br.edu.ufcg.dsc.ck;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import java_cup.runtime.Symbol;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.ck.featureexpression.IFeatureExpression;
import br.edu.ufcg.dsc.ck.parser.parser;
import br.edu.ufcg.dsc.ck.parser.scanner;
import br.edu.ufcg.dsc.ck.tasks.SelectClass;
import br.edu.ufcg.dsc.ck.tasks.Task;
import br.edu.ufcg.dsc.ck.xml.XMLReader;
import br.edu.ufcg.dsc.ck.xml.Xml;
import br.edu.ufcg.dsc.util.AssetNotFoundException;

public class HephaestusCKReader {
	
	/**
	 * Get provided and required classes to a "selectAndMoveComponent" node of the HEPAHESTUS CK FORMAT. <br></br>
	 * @param productLine The SPL. <br></br>
	 * @param transformation The "transformation" node of the XML Hephaestus CK. Actually, content of the child node: "name" : String<br></br>
	 * @param args The "transformation" node of the XML Hephaestus CK. Actually, content of the child node: "args" : String <br></br>
	 * @return returns a Map<String, Object>  <br></br>
	 * @throws IOException  <br></br>
	 * @throws AssetNotFoundException <br></br>
	 */
	public static Map<String, Object> getProvidedRequired(ProductLine productLine, String transformation, String args) throws IOException, AssetNotFoundException {
		
		/* This variable will store the provided and required classes. */
		Map<String, Object> result = new HashMap<String, Object>();
		
		/* This variable will store the provided classes.*/
		HashMap<String,String> provided = new HashMap<String,String>();
		
		/* This variable will store the required classes. */
		Set<String> required = null;

		/* what do we expect ? -> <transformation><name>selectAndMoveComponent</name><args>Game, src\pack\Game.java</args></transformation> */
		if (transformation.equalsIgnoreCase("selectAndMoveComponent")) {
			String[] assets = args.split(",");
			
			/* Asset Name */
			String constante = assets[0].trim();
			/* path = Asset Path */
			String path = assets[1].trim();
			
			/* Insert into  provided HashMap*/
			provided.put(constante, path);
			
			/* Get required classes from provided classes */ 
			/* These dependencies is found with soot framework. */
			required = XMLReader.getRequiredClasses(productLine, provided);
		}
		/*put provided and required classes into Map<String, Object> result*/
		result.put("provided", provided);
		result.put("required", required);
		
		/*returns provided and required classes found automatically.*/
		return result;
	}

	/**
	 * Read Hephaestus CK and returns an abstract representation of Configuration Knowledge.<br></br>
	 * @param ckFile String CK File <br></br>
	 * @param preprocessProperties Properties File. <br></br>
	 * @param productLine SPL representation.<br></br>
	 * @return returns an abstract representation of Configuration Knowledge. <br></br>
	 * @see ConfigurationKnowledge <br></br>
	 */
	public static ConfigurationKnowledge readCK(String ckFile, Properties preprocessProperties, ProductLine productLine) {

		/*BUILD an XML FILE representation of the CK File*/
		Xml ckXML = new Xml(ckFile, "configurationModel");
		Set<ConfigurationItem> ckItems = new HashSet<ConfigurationItem>();

		/* "configuration variable" is a representation of the configuration node.*/
		/* walk through all configurations node of the XML HEPHAESTUS CK file. */
		for (Xml configuration : ckXML.children("configuration")) {
			try {
				/* FeatureExpression Representation */
				IFeatureExpression featExp = null;
				/* get content of the child node: "expression" -> Feature Expression.*/
				String fExp = configuration.child("expression").content().toLowerCase();

				/* Creates a new scanner */
				scanner sc = new scanner(new StringReader(fExp));
				parser p = new parser(sc);
				Symbol sym = p.parse();
				featExp = (IFeatureExpression) sym.value;

				/* A TASK has all operations you can do with the set of provided and required classes. */
				/*A task is composed of a set of required and provided classes.*/
				Set<Task> tasks = new HashSet<Task>();
				
				/* "transformation variable" is a representation of the transformation node.*/
				/* walk through all transformation node of the XML HEPHAESTUS CK file. */
				for (Xml transformation : configuration.children("transformation")) {
					/* get content of the child node: "name" */
					String name = transformation.child("name").content().trim();
					
					/* get content of the child node: "args" */	
					String args = transformation.child("args").content().trim();

					/* is the name selectAndMoveComponent ? */
					if(name.equalsIgnoreCase("selectAndMoveComponent")){
						/* This variable will store the provided and required classes. */
						Map<String, Object> providedWithRequired = getProvidedRequired(productLine, transformation.child("name").content().trim(), transformation.child("args").content().trim());

						/* This variable will store the provided classes.*/
						HashMap<String,String> provided = (HashMap<String,String>) providedWithRequired.get("provided");
						
						/* This variable will store the required classes. */
						Set<String> required = (Set<String>) providedWithRequired.get("required");
						
						/* Initializes the set of provided and required classe */
						SelectClass task = new SelectClass(provided, required);
						tasks.add(task);
						System.out.println("\n\nClass Name: " + args);
						task.printProvided();
						task.printRequired();
						
						/*  is the set of required and provided classes empty. */
						if (!task.isEmpty()) {
							/* Add to task the first provided and required classes. */
						}
						
						/* Constructs a configuration Item with a feature expression and a set of tasks. */
						ConfigurationItem item = new ConfigurationItem(featExp, tasks);
						ckItems.add(item);
					}
					/* is the name createBuildEntries ? */
					else if(name.equals("createBuildEntries")){
						preprocessProperties.setProperty(fExp, args);
					}
					/* is the name preprocessFiles ? */
					else if(name.equals("preprocessFiles")){
						String[] fileNames = args.split(",");
						String preprocess = preprocessProperties.getProperty("preprocess");
						for(String fileName : fileNames){
							if(preprocess == null){
								preprocess = fileName;
							}
							else{
								preprocess = preprocess + "," + fileName;
							}
						}
						preprocessProperties.setProperty(fExp, args);
					}
				} /* FOR end */
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/* Create a Configuration Knowledge and returns it. */
		ConfigurationKnowledge ck = new ConfigurationKnowledge(ckItems);
		return ck;
	}
}
