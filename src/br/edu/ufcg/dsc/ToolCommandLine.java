package br.edu.ufcg.dsc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.naming.ConfigurationException;
import org.eclipse.jdt.core.JavaModelException;
import br.cin.ufpe.br.alloy.products.AlloyProductGenerator;
import br.cin.ufpe.br.approaches.AllProductPairs;
import br.cin.ufpe.br.approaches.AllProducts;
import br.cin.ufpe.br.approaches.ImpactedClasses;
import br.cin.ufpe.br.approaches.ImpactedProducts;
import br.cin.ufpe.br.clean.ProductsCleaner;
import br.cin.ufpe.br.fileProperties.FilePropertiesObject;
import br.cin.ufpe.br.matching.ProductMatching;
import br.cin.ufpe.br.wf.WellFormedness;
import br.edu.ufcg.dsc.ast.ASTComparator;
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
import br.edu.ufcg.dsc.util.FileManager;
import edu.mit.csail.sdg.alloy4.Err;

public class ToolCommandLine {

	private ProductMatching productMatching;
	
	private ProductsCleaner productsCleaner;
	
	private AlloyProductGenerator alloyProductGenerator;
	
	private WellFormedness wellFormedness;
	
	/*A string collection of changed classes.*/
	private Collection<String> classesModificadas;
	
	private ProductBuilder productBuilder;
	
	private long testsCompileTimeout;
	
	private long testsExecutionTimeout;
	private long testsGenerationTimeout;
	
	/* This variable will store the changed assets - mofified files/classes.*/
	private HashSet<String> changedAssets;

	private ASTComparator astComparator;

	private HashMap<String, HashSet<HashSet<String>>> productsCache;

	public ToolCommandLine() {
		this.productsCache = new HashMap<String, HashSet<HashSet<String>>>();
		this.astComparator = new ASTComparator();
		this.wellFormedness = new WellFormedness();
		this.productsCleaner = new ProductsCleaner();
		
		try {
			this.astComparator.setUpProject();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
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
		this.classesModificadas = null;
		this.testsCompileTimeout = 0;
		this.testsExecutionTimeout = 0;
		this.testsGenerationTimeout = 0;
		this.changedAssets = null;
		/* Removes all of the mappings from this map. The map will be empty after this call returns. */
		XMLReader.getInstance().reset();
		/* Cleans the generated products folder. */
		this.productsCleaner.cleanProductsFolder();
		souceLine.setup();
		targetLine.setup();
	}

	/**
	 * Verifica se tanto mapeamento quanto conteudo das classes eh igual.
	 * 
	 * @return
	 */
	private boolean isAssetMappingEqual(ProductLine sourceLine, ProductLine targetLine) {
		boolean assetsEqual = false;
		assetsEqual = this.isSameAssets(sourceLine, targetLine);
		return assetsEqual;
	}

	

	private HashSet<String> getChangedFeatureNames(ProductLine targetLine) {

		HashSet<String> output = new HashSet<String>();

		if (this.changedAssets == null) {
			return null;
		}

		HashSet<String> changedAssetNames = new HashSet<String>();

		for (String asset : this.changedAssets) {
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

	/**
	 * This methods answers whether the assets of the SOURCE product line is completely equal to the TARGET product line. 
	 * It means, that it looks for the modified assets.  <br></br>
	 * @param sourceLine  <br></br>
	 * @param targetLine  <br></br>
	 * @return returns is both product lines have the same assets. Without changes.
	 */
	public boolean isSameAssets(ProductLine sourceLine, ProductLine targetLine) {
		boolean result = true;
		
		/* Get all SOURCE product line classes. */
		Set<String> sourceKeySet = sourceLine.getMappingClassesSistemaDeArquivos().keySet();
		
		/* Get all TARGET product line classes. */
		Set<String> targetKeySet = targetLine.getMappingClassesSistemaDeArquivos().keySet();

		/* Initialize the modified classes variable. */
		this.classesModificadas = new HashSet<String>();
		
		/* Initialize the changed assets variable. */
		this.changedAssets = new HashSet<String>();

		/* walk through all assets of the SOURCE product line classes. */
		for (String asset : sourceKeySet) {
			/* location source file*/
			String locationSource = sourceLine.getMappingClassesSistemaDeArquivos().get(asset);
			/* location target file*/
			String locationTarget = targetLine.getMappingClassesSistemaDeArquivos().get(asset);
			if (locationSource != null && locationTarget != null) {
				/* build the two files.*/
				File sourceFile = new File(locationSource); // source asset File.
				File targetFile = new File(locationTarget); // target asset File.
				try {
					boolean equals;
					if (asset.endsWith("java")) {
						/* This methods compares two textual files and return whether they are equal.
						 * It means, there is no refactoring in the second when compared to the first one. */
						this.astComparator.setInputs(sourceFile, targetFile);
						equals = this.astComparator.isIsomorphic();
					} else if (asset.endsWith("aj")) {
						/* This methods compares two textual files and return whether they are equal. 
						 * It means, there is no refactoring in the second when compared to the first one. */
						equals = this.isTextualFileContentsEquals(sourceFile, targetFile);
					} else {
						equals = true;
					}
					if (!equals) {
						result = false;
						/*Put the asset in the modified classes.*/
						this.classesModificadas.add(asset);
						this.changedAssets.add(FileManager.getInstance().getPath("src." + asset));
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (!targetKeySet.containsAll((sourceKeySet))) {
			result = false;
		}
		System.out.println("\nHave Source and Target the same assets ?:" + result +" \n");
		return result;
	}

	/**
	 * This methods compares two textual files and return whether they are equal. 
	 * It means, there is no refactoring in the second when compared to the first one.
	 * @param sourceFile
	 * @param targetFile
	 * @return
	 */
	private boolean isTextualFileContentsEquals(File sourceFile, File targetFile) {
		boolean result = true;
		String linhaSource = "";
		String linhaTarget = "";
		try {
			FileReader readerSource = new FileReader(sourceFile);
			FileReader readerTarget = new FileReader(targetFile);
			BufferedReader inSource = new BufferedReader(readerSource);
			BufferedReader inTarget = new BufferedReader(readerTarget);
			while (result && ((linhaSource = inSource.readLine()) != null & (linhaTarget = inTarget.readLine()) != null)) {
				if (!linhaSource.trim().equals(linhaTarget.trim())) {
					result = false;
				}
			}
			if (linhaSource != null || linhaTarget != null) {
				result = false;
			}
			inSource.close();
			inTarget.close();
			readerSource.close();
			readerTarget.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public long getTestsCompileTimeout() {
		return testsCompileTimeout;
	}

	public long getTestsExecutionTimeout() {
		return testsExecutionTimeout;
	}

	public long getTestsGenerationTimeout() {
		return testsGenerationTimeout;
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
		sourceSPL.setSetsOfFeatures(this.productsCache.get(sourceSPL.getPath()));
		targetSPL.setSetsOfFeatures(this.productsCache.get(targetSPL.getPath()));
		
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
		System.out.println("areAllProductsMatched: " + areAllProductsMatched);
		boolean isAssetMappingsEqual = this.isAssetMappingEqual(sourceSPL, targetSPL);
		System.out.println("\n AM Equal: " + isAssetMappingsEqual);
		
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
			ImpactedProducts ip = new ImpactedProducts(this.productBuilder, this.classesModificadas);
			System.out.println("Refactoring ? " + (isRefinement = ip.evaluate(sourceSPL, targetSPL, in, wf, areAllProductsMatched)));
		}else if(in.getApproach().equals(Approach.IC)){
			System.out.println("\nIMPACTED ClASSES\n");
			ImpactedClasses ic = new ImpactedClasses(productBuilder, in, this.classesModificadas);
			System.out.println("Refactoring ? " + (isRefinement = ic.evaluate(sourceSPL, targetSPL, changedFeatures, wf, areAllProductsMatched)));
		}else if(in.getApproach().equals(Approach.EIC)){
			System.out.println("\nEXTENDED IMPACTED ClASSES\n");
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
}
