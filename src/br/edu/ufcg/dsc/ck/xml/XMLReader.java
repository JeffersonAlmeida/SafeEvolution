package br.edu.ufcg.dsc.ck.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import java_cup.runtime.Symbol;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import soot.Main;
import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.ck.ConfigurationItem;
import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
import br.edu.ufcg.dsc.ck.featureexpression.IFeatureExpression;
import br.edu.ufcg.dsc.ck.parser.parser;
import br.edu.ufcg.dsc.ck.parser.scanner;
import br.edu.ufcg.dsc.ck.tasks.SelectClass;
import br.edu.ufcg.dsc.ck.tasks.Task;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.FilesManager;

public class XMLReader {
	
	private HashMap<String, ConfigurationKnowledge> cacheCKs;
	
	private static XMLReader instance;
	
	private XMLReader() {
		this.cacheCKs = new HashMap<String, ConfigurationKnowledge>();
	}
	
	public static XMLReader getInstance(){
		if(instance == null){
			instance = new XMLReader();
		}
		
		return instance;
	}

	public ConfigurationKnowledge getCK(ProductLine productLine
			
			/*String configurationKnowledgeFileName, HashMap<String,String> assetMapping, 
			HashMap<String, Collection<String>> dependencias*/) {
		ConfigurationKnowledge ck = this.cacheCKs.get(productLine.getCkPath());
		
		if(ck == null){
			Set<ConfigurationItem> ckItems = new HashSet<ConfigurationItem>();
			ck = new ConfigurationKnowledge(ckItems);

			try {
				File file = new File(productLine.getCkPath());

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(file);
				doc.getDocumentElement().normalize();

				NodeList nodeLst = doc.getElementsByTagName("configuration");

				for (int s = 0; s < nodeLst.getLength(); s++) {
					Node fstNode = nodeLst.item(s);

					if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

						Element fstElmnt = (Element) fstNode;

						NodeList featExpElmntLst = fstElmnt
								.getElementsByTagName("expression");
						Element featExpElmnt = (Element) featExpElmntLst.item(0);
						NodeList fExp = featExpElmnt.getChildNodes();
						String featureExpression = ((Node) fExp.item(0))
								.getNodeValue();
						
						featureExpression = featureExpression.toLowerCase();

						NodeList provElmntLst = fstElmnt
								.getElementsByTagName("provided");
						Element providedElmnt = (Element) provElmntLst.item(0);
						NodeList providedList = providedElmnt.getChildNodes();
						String provideds = ((Node) providedList.item(0))
								.getNodeValue();
						String[] providedsArray = provideds.split(",");

						HashMap<String,String> provided = new HashMap<String,String>();
						
						for (String component : providedsArray) {
							if(component.contains("[")){
								String[] parts = component.split(Pattern.quote("["));
								
								provided.put(parts[0], parts[1].split(Pattern.quote("]"))[0]);
							}
							else{
								provided.put(component, null);
							}
							
						}
						
						//Pegando requireds de forma automatica.
						//Soh funciona para classes e aspectos.
						Set<String> required = getRequiredClasses(productLine, provided);

						NodeList reqElmntLst = fstElmnt
								.getElementsByTagName("required");
						Element requiredElmnt = (Element) reqElmntLst.item(0);
						NodeList requiredList = requiredElmnt.getChildNodes();
						
						if (requiredList.getLength() > 0) {
							String requireds = ((Node) requiredList.item(0))
									.getNodeValue().trim();
							String[] requiredsArray = requireds.split(",");

							for (String component : requiredsArray) {
								if (!component.trim().equals("true")) {
									required.add(component);
								}
							}
						}

						Set<Task> tasks = new HashSet<Task>();
						Task task = new SelectClass(provided, required);
						tasks.add(task);

						IFeatureExpression featExp = null;
						scanner sc = new scanner(
								new StringReader(featureExpression));
						parser p = new parser(sc);
						Symbol sym = p.parse();
						featExp = (IFeatureExpression) sym.value;

						ConfigurationItem item = new ConfigurationItem(featExp,
								tasks);
						ck.addCKitem(item);
						/*
						 * System.out.println("FeatureExpression : " + featExp);
						 * System.out.println("Provided : " + provided);
						 * System.out.println("Required : " + required); /*
						 */
						/*
						 * System.out.println("FeatureExpression : " +
						 * featureExpression); System.out.println("Provided : " +
						 * provideds); System.out.println("Required : " +
						 * requireds); /*
						 */

					}
					/**/
					// System.out.println();
				}
				// System.out.println(ck);
				
				this.cacheCKs.put(productLine.getCkPath(), ck);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ck;
	}

	public static Set<String> getRequiredClasses(ProductLine productLine, HashMap<String, String> providedClasses) throws IOException, AssetNotFoundException {
		HashSet<String> requiredClasses = new HashSet<String>();

		for(String providedClass : providedClasses.keySet()){
			String classeRelativePath = productLine.getAssetMapping().get(providedClass.trim());

			classeRelativePath = classeRelativePath.replaceAll(Pattern.quote(Constants.FILE_SEPARATOR), "/");
			
			if(classeRelativePath != null){
				File classFile = new File(productLine.getPath() + Constants.FILE_SEPARATOR + classeRelativePath);
				
				Collection<String> dependencias = null;
				
				if(classeRelativePath.endsWith(".java")){
					dependencias = Main.v().getDependences(
							classFile.getName().replaceAll(".java", ""), classFile.getParent());
					
					dependencias.addAll(FilesManager.getInstance().getDependenciasAspectos(classFile));
				}
				else if(classeRelativePath.endsWith(".aj")){
					dependencias = FilesManager.getInstance().getDependenciasDeAspectosPeloImport(classFile);
				}
				
				if(dependencias != null){
					productLine.getDependencias().put(classeRelativePath, dependencias);
					
					for(String dependence : dependencias){
						String constantRequired = getConstantFromAssetMapping(dependence, productLine.getAssetMapping());
						
						if(constantRequired != null){
							requiredClasses.add(constantRequired);
						}
					}
				}
			}
		}
		
		return requiredClasses;
	}

	private static String getConstantFromAssetMapping(String dependecia, HashMap<String,String> assetMapping) throws IOException, AssetNotFoundException {
		String result = null;
		
		Set<String> assetNames = assetMapping.keySet();

		for (String constant : assetNames) {
			String classeRelativePath = assetMapping.get(constant.trim());
			
			classeRelativePath = classeRelativePath.replaceAll(Pattern.quote("/src/"), "").replaceAll(Pattern.quote("/"), ".");
			
			// A dependencia nem sempre eh um nome de classe com pacote. As chaves do
			// mapping sempre sao.
			// Eh necessario checar se dependencia faz parte de alguma key.
			if ((dependecia.contains(".") && classeRelativePath.endsWith(dependecia + ".java")) 
					|| classeRelativePath.endsWith("." + dependecia + ".java") 
					|| (dependecia.contains(".") && classeRelativePath.endsWith(dependecia + ".aj")) 
					|| classeRelativePath.endsWith("." + dependecia + ".aj")) {
				
				
				result = constant;

				break;
			}
		}

		return result;
	}

	public void reset() {
		this.cacheCKs.clear();
	}

	// public static void main(String argv[]) {
	// long timeStart = System.currentTimeMillis();
	//		
	// ConfigurationKnowledge ck =
	// XMLReader.getCK("/Users/leopoldoteixeira/Documents/CIn/workspaces/msc/ck/src/br/ufpe/cin/lps/ck/xml/ck07.xml");
	// //ConfigurationKnowledge ck =
	// XMLReader.getCK("/Users/leopoldoteixeira/Documents/CIn/workspaces/msc/ck/src/br/ufpe/cin/lps/ck/xml/ck07_AO_alt.xml");
	// System.out.println(ck);
	// String path =
	// "/Users/leopoldoteixeira/Documents/Dropbox/cin/_work/alloy/";
	// String model = "fm07_AO.als";
	// Set<String> features = new HashSet<String>();
	// features.add("MobileMedia");
	// features.add("Sorting");
	// features.add("Favourites");
	// features.add("Copy");
	//		
	// features.add("SMS");
	// features.add("Photo");
	// features.add("Music");
	// features.add("Video");
	// features.add("CapturePhoto");
	// features.add("CaptureVideo");
	// /**/
	//		
	// AlloyFM r = new AlloyFM(path,model,features);
	// Set<Set<String>> products = null;
	// try {
	// products = r.getProductConfigurations();
	// } catch (Err e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//		
	// System.out.println(products);
	//		
	// int totalProducts = 0;
	// int totalInvalidProducts = 0;
	// Set<Set<String>> invalidProducts = new HashSet<Set<String>>();
	//		
	// for (Set<String> product : products) {
	// totalProducts++;
	// //evaluating CK
	//			
	// Hashtable<String,Set<String>> evaluation = ck.evalCK(product);
	// System.out.println(evaluation);
	//			
	// Set<String> provided = evaluation.get("provided");
	// Set<String> required = evaluation.get("required");
	// if (provided.containsAll(required)) {
	// System.out.println("Valid Product: "+product);
	// }
	// else {
	// totalInvalidProducts++;
	// required.removeAll(provided);
	// System.out.println(required);
	// System.out.println("Invalid Product: "+product);
	// invalidProducts.add(product);
	// }
	// System.out.println();
	// }
	// System.out.println("Total of products: "+totalProducts);
	// System.out.println("Total of invalid products: "+totalInvalidProducts);
	// System.out.println("Invalid products: "+invalidProducts);
	// System.out.println("finished in "+(System.currentTimeMillis() -
	// timeStart)+ " ms");
	// /**/
	// }
}