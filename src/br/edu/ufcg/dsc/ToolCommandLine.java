package br.edu.ufcg.dsc;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.JavaModelException;

import safeEvolution.alloy.products.AlloyProductGenerator;
import safeEvolution.am.verifier.AssetMappingAnalyzer;
import safeEvolution.approaches.AllProductPairs;
import safeEvolution.approaches.AllProducts;
import safeEvolution.approaches.BackwardImpactedClasses;
import safeEvolution.approaches.ForwardImpactedClasses;
import safeEvolution.approaches.ImpactedProducts;
import safeEvolution.fileProperties.FilePropertiesObject;
import safeEvolution.productMatcher.ProductMatching;
import safeEvolution.productsCleaner.ProductsCleaner;
import safeEvolution.wellFormedness.WellFormedness;
import br.edu.ufcg.dsc.builders.MobileMediaBuilder;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.builders.TargetBuilder;
import br.edu.ufcg.dsc.ck.ConfigurationItem;
import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
import br.edu.ufcg.dsc.ck.featureexpression.IFeatureExpression;
import br.edu.ufcg.dsc.ck.tasks.Task;
import br.edu.ufcg.dsc.ck.xml.XMLReader;
import br.edu.ufcg.dsc.evaluation.SPLOutcomes;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import edu.mit.csail.sdg.alloy4.Err;

public class ToolCommandLine {

	private ProductMatching productMatching;
	
	private ProductsCleaner productsCleaner;
	
	private AlloyProductGenerator alloyProductGenerator;
	
	private WellFormedness wellFormedness;
	
	private ProductBuilder productBuilder;
	
	private AssetMappingAnalyzer amAnalyzer;

	public ToolCommandLine() {
		this.wellFormedness = new WellFormedness();
		this.productsCleaner = new ProductsCleaner();
		this.amAnalyzer = new AssetMappingAnalyzer();
	}

	public ToolCommandLine(Lines line) {
		this();
		if (line.equals(Lines.MOBILE_MEDIA)) {
			this.productBuilder = new MobileMediaBuilder();
		} else if (line.equals(Lines.TARGET)  || line.equals(Lines.DEFAULT)) {
			this.productBuilder = TargetBuilder.getInstance();
		}
		this.productMatching = new ProductMatching(this.productBuilder);
		this.alloyProductGenerator = new AlloyProductGenerator(wellFormedness,this.productBuilder);
	}

	private void setup(ProductLine souceLine, ProductLine targetLine) throws IOException, AssetNotFoundException {
		/* Removes all of the mappings from this map. The map will be empty after this call returns. */
		XMLReader.getInstance().reset();
		/* Cleans the generated products folder. */
		this.productsCleaner.cleanProductsFolder();
		souceLine.setup();
		targetLine.setup();
	}

	private HashSet<String> getChangedFeatureNames(ProductLine targetLine) {

		HashSet<String> output = new HashSet<String>();

		if (this.amAnalyzer.getChangedAssetsList() == null) {
			return null;
		}

		HashSet<String> changedAssetNames = new HashSet<String>();

		for (String asset : this.amAnalyzer.getChangedAssetsList()) {
			String correspondingAssetName = getCorrespondingAssetName(targetLine.getAssetMapping(), asset);
			if (correspondingAssetName != null) {
				changedAssetNames.add(correspondingAssetName);
			}
		}

		ConfigurationKnowledge configutarionKnowledge = targetLine.getCk();

		Set<ConfigurationItem> ckItems = configutarionKnowledge.getCkItems();

		for (ConfigurationItem configurationItem : ckItems) {
			IFeatureExpression featExp = configurationItem.getFeatExp();
			Set<Task> tasks = configurationItem.getTasks();

			for (Task task : tasks) {
				Set<String> provided = task.getProvided().keySet();
				for (String string : changedAssetNames) {
					if (provided.contains(string)) {
						output.add(featExp.getCode());
					}
				}
			}
		}

		return output;
	}

	private String getCorrespondingAssetName(HashMap<String, String> assetMapping, String asset) {
		String result = null;

		asset = asset.replaceAll(Pattern.quote("\\"), "/");

		for (String assetName : assetMapping.keySet()) {
			String assetPath = assetMapping.get(assetName);

			if (assetPath.contains(asset.replaceAll(Pattern.quote("\\"), "/"))) {
				result = assetName;
			}
		}

		return result;
	}

	public boolean verifyLine(FilePropertiesObject in) throws Err, IOException, AssetNotFoundException, DirectoryException {

		String fachadaSource = null;
		String fachadaTarget = null; 
		
		String ckSource = in.getArtifactsSourceDir() + "ck.xml";
		String ckTarget = in.getArtifactsTargetDir() + "ck.xml";
		
		String fmSource = in.getArtifactsSourceDir() + "fm.xml";
		String fmTarget = in.getArtifactsTargetDir() + "fm.xml";
		
		String amSource = in.getArtifactsSourceDir() + "am.txt";
		String amTarget = in.getArtifactsTargetDir() + "am.txt";
		
		ProductLine sourceSPL = new ProductLine(in.getSourceLineDirectory(), ckSource, fmSource, amSource, in.isAspectsInSourceSPL(), fachadaSource, in.getCkFormatSourceSPL(),in.getAmFormatSourceSPL());
		ProductLine targetSPL = new ProductLine(in.getTargetLineDirectory(), ckTarget, fmTarget, amTarget, in.isAspectsInTargetSPL(), fachadaTarget, in.getCkFormatTargetSPL(), in.getAmFormatTargetSPL());
		sourceSPL.setLibPath(in.getSourceLineLibDirectory());
		targetSPL.setLibPath(in.getTargetLineLibDirectory());
		
	 	/* It cleans the generated products folder. */
		this.setup(sourceSPL, targetSPL);

		/* It Calls alloy to build source and target products and put it in cache. */
		this.alloyProductGenerator.generateProductsFromAlloyFile(sourceSPL, targetSPL);

		/* Reset results variables .*/
		SPLOutcomes sOutcomes = SPLOutcomes.getInstance();
		sOutcomes.getMeasures().reset();
		sOutcomes.getMeasures().setApproach(in.getApproach());
		sOutcomes.getMeasures().getTempoTotal().startContinue();

		WellFormedness wellFormedness =  new WellFormedness();
		boolean wf = wellFormedness.isWF(sourceSPL, targetSPL);
		
		HashSet<String> changedFeatures = getChangedFeatureNames(targetSPL);
		
		boolean areAllProductsMatched = this.productMatching.areAllProductsMatched(sourceSPL, targetSPL);
		areAllProductsMatched = true;
		System.out.println("areAllProductsMatched: " + areAllProductsMatched);
		
		boolean isAssetMappingsEqual;
		try {
			isAssetMappingsEqual = amAnalyzer.isSameAssets(sourceSPL, targetSPL);
			System.out.println("\n AM Equal: " + isAssetMappingsEqual);
			sOutcomes.setAssetMappingsEqual(isAssetMappingsEqual);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		
		boolean isRefinement = false;
		if(in.getApproach().equals(Approach.APP)){
			System.out.println("\nALL PRODUCT PAIRS\n");
			AllProductPairs app = new AllProductPairs(this.productBuilder);
			System.out.println("Refactoring ? " + (isRefinement = app.evaluate(sourceSPL, targetSPL, in, wf)));
		}else if(in.getApproach().equals(Approach.AP)){
			System.out.println("\nALL PRODUCTS\n");
			AllProducts ap = new AllProducts(this.productBuilder);
			System.out.println("Refactoring ? " + (isRefinement = ap.evaluate(sourceSPL, targetSPL, in, wf, areAllProductsMatched)));
		}else if(in.getApproach().equals(Approach.IP)){
			System.out.println("\nIMPACTED PRODUCTS\n");
			ImpactedProducts ip = new ImpactedProducts(this.productBuilder, amAnalyzer.getModifiedClassesList());
			System.out.println("Refactoring ? " + (isRefinement = ip.evaluate(sourceSPL, targetSPL, in, wf, areAllProductsMatched)));
		}else if(in.getApproach().equals(Approach.IC)){
			System.out.println("\nIMPACTED ClASSES\n");
			long startTime = System.currentTimeMillis();
			ForwardImpactedClasses ic = new ForwardImpactedClasses(productBuilder, in, amAnalyzer.getModifiedClassesList());
			System.out.println("Refactoring ? " + (isRefinement = ic.evaluate(sourceSPL, targetSPL, changedFeatures, wf, areAllProductsMatched)));
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("\n\n TIME SPENT IN THIS IC APPROACH: " + elapsedTime/1000 + " milliseconds");
		}else if(in.getApproach().equals(Approach.EIC)){
			System.out.println("\nEXTENDED IMPACTED ClASSES\n");
			long startTime = System.currentTimeMillis();
			BackwardImpactedClasses eic = new BackwardImpactedClasses(productBuilder, in, amAnalyzer.getModifiedClassesList());
			isRefinement = eic.evaluate(sourceSPL, targetSPL, changedFeatures, wf, areAllProductsMatched);
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println("\n\n TIME SPENT IN THIS EIC APPROACH: " + elapsedTime/1000 + " milliseconds");
		}
		
		/*Report Variables: Pause total time to check the SPL.*/
		sOutcomes.setWF(wf);
		
		sOutcomes.setFmAndCKRefinement(areAllProductsMatched);
		sOutcomes.setRefinement(wf && isRefinement);
		sOutcomes.setCompObservableBehavior(isRefinement);
		sOutcomes.getMeasures().getTempoTotal().pause();
		sOutcomes.getMeasures().print();

		return isRefinement;
	}

	public void setAmAnalyzer(AssetMappingAnalyzer amAnalyzer) {
		this.amAnalyzer = amAnalyzer;
	}
	public AssetMappingAnalyzer getAmAnalyzer() {
		return amAnalyzer;
	}
}
