package br.edu.ufcg.dsc.ck;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java_cup.runtime.Symbol;
import org.apache.commons.io.FileUtils;
import br.edu.ufcg.dsc.am.ReadAM;
import br.edu.ufcg.dsc.ck.featureexpression.IFeatureExpression;
import br.edu.ufcg.dsc.ck.parser.parser;
import br.edu.ufcg.dsc.ck.parser.scanner;
import br.edu.ufcg.dsc.ck.tasks.GenericTask;
import br.edu.ufcg.dsc.ck.tasks.Task;
import br.edu.ufcg.dsc.ck.xml.Xml;

public class SimpleCKReader {
	
	/**
	 * 
	 */
	public static Map<String, Set<String>> getProvidedRequired(String transformation,String args,Map<String, String> cm,Set<String> components) {
		
		Map<String, Set<String>> getProvidedRequired = new HashMap<String, Set<String>>();
		Set<String> provided = new HashSet<String>();
		Set<String> required = new HashSet<String>();

		if (transformation.equalsIgnoreCase("selectComponents")) {
			String[] assets = args.split(",");
			for (String asset : assets) {
				try {
					asset = asset.trim();
					provided.add(asset);
					String filepath = cm.get(asset);
					String fileContents = FileUtils.readFileToString(new File(filepath));
					fileContents = fileContents.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
					for (String component : components) {
						if (fileContents.indexOf(component) != -1) {
							required.add(component);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		getProvidedRequired.put("provided", provided);
		getProvidedRequired.put("required", required);
		return getProvidedRequired;
	}

	/**
	 * This method reads the configuration knowledge. <br></br>
	 * @param ckFile CK File String <br></br>
	 * @param cm Component Model <br></br>
	 * @return  returns a ConfigurationKnowledge<br></br>
	 * @see ConfigurationKnowledge <br></br>
	 */
	public static ConfigurationKnowledge readCK(String ckFile, Map<String, String> cm) {

		Xml ckXML = new Xml(ckFile, "configurationModel");
		Set<ConfigurationItem> ckItems = new HashSet<ConfigurationItem>();
		for (Xml f : ckXML.children("configuration")) {
			try {

				// FeatureExpression
				IFeatureExpression featExp = null;
				String fExp = f.child("expression").content();
				scanner sc = new scanner(new StringReader(fExp));
				parser p = new parser(sc);
				Symbol sym = p.parse();
				featExp = (IFeatureExpression) sym.value;

				Set<Task> tasks = new HashSet<Task>();
				for (Xml t : f.children("transformation")) {
					Map<String, Set<String>> getProvidedRequired = SimpleCKReader.getProvidedRequired(t.child("name").content().trim(), t.child("args").content().trim(), cm, ReadAM.components(cm));

					// components - get provided and required
					Set<String> provided = getProvidedRequired.get("provided");
					Set<String> required = getProvidedRequired.get("required");
					GenericTask task = new GenericTask(provided, required);
					if (!task.isEmpty()) {
						tasks.add(task);
					}
				}

				ConfigurationItem item = new ConfigurationItem(featExp, tasks);
				ckItems.add(item);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ConfigurationKnowledge ck = new ConfigurationKnowledge(ckItems);
		return ck;
	}
}