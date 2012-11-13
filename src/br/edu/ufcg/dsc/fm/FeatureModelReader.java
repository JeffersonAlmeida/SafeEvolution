package br.edu.ufcg.dsc.fm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.ck.xml.Xml;

/**
 * 
 * @author Jefferson Almeida - jra at cin dot ufpe dot br  <br></br>
 *This class is a Feature Model Reader.
 */
public class FeatureModelReader {

	private String semanticsFM;
	private String sigs;
	private HashSet<String> features;
	private ArrayList<String> formulas;

	public FeatureModelReader() {
		semanticsFM = "";
		sigs = "";
		features = new HashSet<String>();
		formulas = new ArrayList<String>();
	}

	public String getSemanticsFM() {
		return semanticsFM;
	}

	public String getSigs() {
		return sigs;
	}

	public void processAlternative(Stack<Xml> nodes, Stack<String> fm) {
		Xml top = nodes.pop();
		String node = top.optString("id");
		for (Xml c : nodes) {
			String formula = "not( ";
			formula += "isTrue[" + node + "] and " + "isTrue["
					+ c.optString("id") + "] )";
			fm.push(formula);
			formulas.add(formula);
		}
		if (nodes.size() > 1)
			processAlternative(nodes, fm);
	}

	public void navigateFM(String parent, Xml feature, Stack<String> fm) {
		for (Xml f : feature.children("feature")) {
			String fName = f.optString("id").toLowerCase();
//			String fName = f.optString("id");
			features.add(fName);
			String formula = "( ";
			if (f.optInteger("min") == 0 && f.optInteger("max") == 1) {
				formula += "isTrue[" + fName + "] => " + "isTrue[" + parent
						+ "]";
			} else if (f.optInteger("min") == 1 && f.optInteger("max") == 1) {
				formula += "isTrue[" + fName + "] <=> " + "isTrue[" + parent
						+ "]";
			}
			formula += " )";
			formulas.add(formula);
			fm.push(formula);

			navigateFM(fName, f, fm);
		}

		for (Xml f : feature.children("featureGroup")) {
			String formula = "( " + "isTrue[" + parent + "] <=> ( ";
			Stack<Xml> children = new Stack<Xml>();
			Stack<Xml> childrenToNavigate = new Stack<Xml>();
			
			for (Xml c : f.children("feature")) {
				String optString = c.optString("id").toLowerCase();
//				String optString = c.optString("id");
				features.add(optString);
				
				children.push(c);
				childrenToNavigate.push(c);
				
				formula += "isTrue[" + optString + "] or ";
			}
			formula = formula.substring(0, formula.length() - 4) + " ) )";
			fm.push(formula);
			formulas.add(formula);

			if (f.optInteger("max") == 1) {
				formula = "";
				processAlternative(children, fm);
			}

			for (Xml c : childrenToNavigate) {
				String optString = c.optString("id").toLowerCase();
				
				navigateFM(optString, c, fm);
			}
		}
	}

	/**
	 * Read the Feature Model <br></br>
	 * @param name
	 * @param fmFile
	 * @return
	 */
	public String readFM(String name, String fmFile) {
		Xml config = new Xml(fmFile, "feature");
		Stack<String> fm = new Stack<String>();
		String rootFeature = config.optString("id").toLowerCase();
		features.add(rootFeature.toLowerCase());
		fm.push("isTrue[" + rootFeature + "]");
		navigateFM(rootFeature, config, fm);
		for (String f : fm) {
			semanticsFM += f + " and ";
		}
		semanticsFM = "pred semantica" + name + "[] { "	+ Constants.LINE_SEPARATOR + semanticsFM.substring(0, semanticsFM.length() - 5)	+ Constants.LINE_SEPARATOR + " }";
		for (String f : features) {
			sigs += f + ",";
		}
		sigs = "one sig " + sigs.substring(0, sigs.length() - 1) + " in Bool{}";
		String model = sigs + Constants.LINE_SEPARATOR + semanticsFM + Constants.LINE_SEPARATOR;
		return model;
	}

	public HashSet<String> getFeatures() {
		return features;
	}

	public int getQuantityOfFeatures() {
		return features.size();
	}

	public Set<String> featuresFM(String fmFile) {

		Xml config = new Xml(fmFile, "feature");
		Stack<String> fm = new Stack<String>();
		String rootFeature = config.optString("id");
		features.add(rootFeature);

		navigateFM(rootFeature, config, fm);

		return features;

	}

	/**
	 * Build Alloy File  <br></br>
	 * @param moduleName
	 * @param output
	 * @throws FileNotFoundException
	 */
	public void buildAlloyFile(String moduleName, String output) throws FileNotFoundException {
		if (features.isEmpty() || sigs.equals("") || semanticsFM.equals("")) {
			return;
		}
	
		File newFile = new File(output);
		PrintStream stream = new PrintStream(newFile);

		/* Check it out in Tool Path + Alloy + AlloyFile */
		stream.print("module " + moduleName + Constants.LINE_SEPARATOR);
		stream.print("open default" + Constants.LINE_SEPARATOR);
		stream.print(Constants.LINE_SEPARATOR);
		stream.print(sigs + Constants.LINE_SEPARATOR);
		stream.print(Constants.LINE_SEPARATOR);
		stream.print(semanticsFM + Constants.LINE_SEPARATOR);
		stream.print(Constants.LINE_SEPARATOR);
		String footer = "run semantica" + moduleName + " for " + features.size();
		stream.print(footer + Constants.LINE_SEPARATOR);
		stream.flush();
		stream.close();
		System.out.println("\n Alloy File has been created. Check it out in < Tool Path + Alloy + Alloy File >\n");
	}

	public ArrayList<String> getFormulas() {
		return formulas;
	}
}
