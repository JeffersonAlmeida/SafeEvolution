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

	public static Map<String, Object> getProvidedRequired(ProductLine productLine, String transformation, String args) throws IOException, AssetNotFoundException {
		Map<String, Object> result = new HashMap<String, Object>();
		
		HashMap<String,String> provided = new HashMap<String,String>();
		Set<String> required = null;

		if (transformation.equalsIgnoreCase("selectAndMoveComponent")) {
			String[] assets = args.split(",");
			
			String constante = assets[0].trim();
			String path = assets[1].trim();
			
			provided.put(constante, path);
			
			required = XMLReader.getRequiredClasses(productLine, provided);
		}
		result.put("provided", provided);
		result.put("required", required);
		
		return result;
	}

	public static ConfigurationKnowledge readCK(String ckFile, Properties preprocessProperties, ProductLine productLine) {
		// Set<Set<String>> productsFM = new HashSet<Set<String>>();
		Xml ckXML = new Xml(ckFile, "configurationModel");
		Set<ConfigurationItem> ckItems = new HashSet<ConfigurationItem>();

		for (Xml configuration : ckXML.children("configuration")) {
			try {

				// FeatureExpression
				IFeatureExpression featExp = null;
				String fExp = configuration.child("expression").content().toLowerCase();

				scanner sc = new scanner(new StringReader(fExp));
				parser p = new parser(sc);
				Symbol sym = p.parse();
				featExp = (IFeatureExpression) sym.value;

				Set<Task> tasks = new HashSet<Task>();
				// System.out.println(featExp);

				for (Xml transformation : configuration.children("transformation")) {
					String name = transformation.child("name").content().trim();
					String args = transformation.child("args").content().trim();

					if(name.equalsIgnoreCase("selectAndMoveComponent")){
						Map<String, Object> providedWithRequired = getProvidedRequired(productLine,
								transformation.child("name").content().trim(), transformation.child("args").content().trim());

						// components - get provided and required
						HashMap<String,String> provided = (HashMap<String,String>) providedWithRequired.get("provided");
						Set<String> required = (Set<String>) providedWithRequired.get("required");
						
						SelectClass task = new SelectClass(provided, required);
						
						if (!task.isEmpty()) {
							tasks.add(task);
						}

						ConfigurationItem item = new ConfigurationItem(featExp, tasks);
						ckItems.add(item);
					}
					else if(name.equals("createBuildEntries")){
						preprocessProperties.setProperty(fExp, args);
					}
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
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ConfigurationKnowledge ck = new ConfigurationKnowledge(ckItems);
		return ck;
	}
}
