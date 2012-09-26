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
	
	/**A HashMap of Configurations Knowledge */
	private HashMap<String, ConfigurationKnowledge> cacheCKs;
	
	/**A XML Reader Singleton*/
	private static XMLReader instance;
	
	/*XML Reader Constructor*/
	private XMLReader() {
		/* Initialize the HashMap of Configurations Knowledge */
		this.cacheCKs = new HashMap<String, ConfigurationKnowledge>();
	}
	
	/*Get the singleton instance of XML Reader.*/
	public static XMLReader getInstance(){
		if(instance == null){
			instance = new XMLReader();
		}
		return instance;
	}

	/**
	 * Get representation of a Configuration Knowledge from a product line.  <br></br>
	 * @param productLine The SPL  <br></br>
	 * @return a ConfigurationKnowledge <br></br>
	 * @see ConfigurationKnowledge <br></br>
	 */
	public ConfigurationKnowledge getCK(ProductLine productLine) {
		
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

						NodeList featExpElmntLst = fstElmnt.getElementsByTagName("expression");
						Element featExpElmnt = (Element) featExpElmntLst.item(0);
						NodeList fExp = featExpElmnt.getChildNodes();
						String featureExpression = ((Node) fExp.item(0)).getNodeValue();
						
						featureExpression = featureExpression.toLowerCase();

						NodeList provElmntLst = fstElmnt.getElementsByTagName("provided");
						Element providedElmnt = (Element) provElmntLst.item(0);
						NodeList providedList = providedElmnt.getChildNodes();
						String provideds = ((Node) providedList.item(0)).getNodeValue();
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
						/* Get required classes automatically. */
						/* It only works for java classes and aspects. */
						Set<String> required = getRequiredClasses(productLine, provided);

						NodeList reqElmntLst = fstElmnt.getElementsByTagName("required");
						Element requiredElmnt = (Element) reqElmntLst.item(0);
						NodeList requiredList = requiredElmnt.getChildNodes();
						
						if (requiredList.getLength() > 0) {
							String requireds = ((Node) requiredList.item(0)).getNodeValue().trim();
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
						scanner sc = new scanner(new StringReader(featureExpression));
						parser p = new parser(sc);
						Symbol sym = p.parse();
						featExp = (IFeatureExpression) sym.value;

						ConfigurationItem item = new ConfigurationItem(featExp, tasks);
						ck.addCKitem(item);
					}
				}
				
				this.cacheCKs.put(productLine.getCkPath(), ck);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ck;
	}

	/**
	 * Get required classes from provided classes to compile the product.  <br></br>
	 * @param productLine The product line.  <br></br> 
	 * @param providedClasses The provided classes  <br></br>
	 * @return returns a HashSet<String> of required classes.   <br></br>
	 * @throws IOException
	 * @throws AssetNotFoundException
	 */
	public static Set<String> getRequiredClasses(ProductLine productLine, HashMap<String, String> providedClasses) throws IOException, AssetNotFoundException {
		/* Initialize this HashSet to store the required classes. */
		HashSet<String> requiredClasses = new HashSet<String>();
		/* walk through all provided classes.*/
		for(String providedClass : providedClasses.keySet()){
			/* Get the relative path of the class. */
			String classeRelativePath = productLine.getAssetMapping().get(providedClass.trim());
			/* Replace "\" to "/" */
			classeRelativePath = classeRelativePath.replaceAll(Pattern.quote(Constants.FILE_SEPARATOR), "/");
			if(classeRelativePath != null){
				/*Construct a Class File */
				File classFile = new File(productLine.getPath() + Constants.FILE_SEPARATOR + classeRelativePath);
				
				/* Initialize this Collection<String> to store dependencies. */
				Collection<String> dependencias = null;
				
				/* Is the file a Java Class ? */
				if(classeRelativePath.endsWith(".java")){
					/* Get the java class dependencies with soot framework. */
					dependencias = Main.v().getDependences(classFile.getName().replaceAll(".java", ""), classFile.getParent());
					/* Catch the aspects needed to compile the class. This class can depend on aspects. Who are these aspects ? */
					dependencias.addAll(FilesManager.getInstance().getDependenciasAspectos(classFile));
				}
				/* Is the file an aspect ? */
				else if(classeRelativePath.endsWith(".aj")){
					/* get the aspect dependencies looking for the imports. */
					dependencias = FilesManager.getInstance().getDependenciasDeAspectosPeloImport(classFile);
				}
				
				/* Does it have dependencies ? */
				if(dependencias != null){
					productLine.getDependencias().put(classeRelativePath, dependencias);
					/* walk through all dependencies classes.*/
					for(String dependence : dependencias){
						/*Get the required classes*/
						String constantRequired = getConstantFromAssetMapping(dependence, productLine.getAssetMapping());
						if(constantRequired != null){
							/* ... and put it in the required HashSet<String>*/
							requiredClasses.add(constantRequired);
						}
					}
				}
			}
		}/*FOR end*/
		
		/*return it!*/
		return requiredClasses;
	}

	/**
	 *  Get the required class  <br></br>
	 * @param dependecia
	 * @param assetMapping
	 * @return
	 * @throws IOException
	 * @throws AssetNotFoundException
	 */
	private static String getConstantFromAssetMapping(String dependecia, HashMap<String,String> assetMapping) throws IOException, AssetNotFoundException {
		String result = null;
		Set<String> assetNames = assetMapping.keySet();
		for (String constant : assetNames) {
			String classeRelativePath = assetMapping.get(constant.trim());
			classeRelativePath = classeRelativePath.replaceAll(Pattern.quote("/src/"), "").replaceAll(Pattern.quote("/"), ".");
			if ((dependecia.contains(".") && classeRelativePath.endsWith(dependecia + ".java")) || classeRelativePath.endsWith("." + dependecia + ".java") || (dependecia.contains(".") && classeRelativePath.endsWith(dependecia + ".aj"))	|| classeRelativePath.endsWith("." + dependecia + ".aj")) {
				result = constant;
				break;
			}
		}
		return result;
	}

	public void reset() {
		this.cacheCKs.clear();
	}
}