package br.edu.ufcg.dsc.fm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;

import br.edu.ufcg.dsc.Constants;

public class AlloyFMEvolutionBuilder {

	private StringBuilder contents;
	private HashSet<String> features;

	public AlloyFMEvolutionBuilder() {
		contents = new StringBuilder();
		features = new HashSet<String>();
	}

	public void buildAlloyFile(String moduleName, String output, String sourceName,
			String fmSourceXML, String targetName, String fmTargetXML) {

		formatHeader(moduleName);
		contents.append(Constants.LINE_SEPARATOR);
		addSemantics(sourceName, fmSourceXML);
		contents.append(Constants.LINE_SEPARATOR);
		addSemantics(targetName, fmTargetXML);
		contents.append(Constants.LINE_SEPARATOR);
		addSignatures();
		contents.append(Constants.LINE_SEPARATOR);
		addRefactoringPredicate(sourceName, targetName);
		contents.append(Constants.LINE_SEPARATOR);
		addRunClause();

		File newFile = new File(output);
		PrintStream stream = null;
		try {
			stream = new PrintStream(newFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		stream.print(contents.toString());
		stream.flush();
		stream.close();
	}

	private void addRunClause() {
		contents.append("check refactoring for 2 FM, " + features.size()
				+ " Bool" + Constants.LINE_SEPARATOR);
	}

	private void addRefactoringPredicate(String string, String string2) {

		contents.append("assert refactoring { " + Constants.LINE_SEPARATOR);
		contents.append("semantica" + string
				+ "[] => semantica" + string2 + "[]"
				+ Constants.LINE_SEPARATOR);
		contents.append("}" + Constants.LINE_SEPARATOR);
	}

	private void addSignatures() {

		contents.append("one sig ");
		String separator = "";
		for (String s : features) {
			contents.append(separator + s);
			separator = ",";
		}
		contents.append(" in Bool{}" + Constants.LINE_SEPARATOR);
	}

	private void addSemantics(String name, String XMLPath) {

		FeatureModelReader reader = new FeatureModelReader();
		reader.readFM(name, XMLPath);

		contents.append(reader.getSemanticsFM() + Constants.LINE_SEPARATOR);
		contents.append(Constants.LINE_SEPARATOR);

		contents.append("one sig " + name + " extends FM{}{"
				+ Constants.LINE_SEPARATOR);

		String separator = "";
		HashSet<String> feats = reader.getFeatures();
		contents.append("features = ");
		for (String string : feats) {
			features.add(string);
			contents.append(separator + string);
			separator = " + ";
		}
		contents.append(Constants.LINE_SEPARATOR + "}"
				+ Constants.LINE_SEPARATOR);

	}

	private void formatHeader(String output) {
		contents.append("module " + output + Constants.LINE_SEPARATOR);
		contents.append("open default" + Constants.LINE_SEPARATOR);
	}

//	public static void main(String[] args) {
//		AlloyFMBuilder builder = new AlloyFMBuilder();
//		builder.buildAlloyFile("out", "M1",
//				"/Users/Solon/Documents/workspace/FM1/fm1.xml", "M2",
//				"/Users/Solon/Documents/workspace/FM2/fm2.xml");
//	}
}
