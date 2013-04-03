package br.edu.ufcg.dsc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.ConfigurationException;
import org.eclipse.jdt.core.JavaModelException;
import soot.Main;
import br.cin.ufpe.br.alloy.products.AlloyProductGenerator;
import br.cin.ufpe.br.approaches.ImpactedClasses;
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
import br.edu.ufcg.dsc.saferefactor.CommandLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FileManager;
import edu.mit.csail.sdg.alloy4.Err;

public class ToolCommandLine {

	private ProductMatching productMatching;
	
	private ProductsCleaner productsCleaner;
	
	private AlloyProductGenerator alloyProductGenerator;
	
	private static final int MAX_CLASSES_MODIFICADAS = 1000;

	private WellFormedness wellFormedness;
	
	/*Hashset of dependencies to the compile the file. */
	private HashSet<String> dependenciasCopiadas;

	/*A string collection of changed classes.*/
	private Collection<String> classesModificadas;
	
	/**/
	private ProductBuilder productBuilder;
	
	private long testsCompileTimeout;
	
	private long testsExecutionTimeout;
	private long testsGenerationTimeout;
	
	/* This variable will store the changed assets - mofified files/classes.*/
	private HashSet<String> changedAssets;

	private ASTComparator astComparator;

	private HashMap<String, HashSet<HashSet<String>>> productsCache;

	private HashSet<String> constantsPreProcessor;
	private HashSet<String> listaAspectos;

	private HashMap<HashSet<String>, HashSet<String>> mapeamentoFeaturesAspectosSource;
	private HashMap<HashSet<String>, HashSet<String>> mapeamentoFeaturesAspectosTarget;

	private Lines line;

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
		this.line = line;
		if (line.equals(Lines.MOBILE_MEDIA)) {
			this.productBuilder = new MobileMediaBuilder();
		} else if (line.equals(Lines.TARGET)  || line.equals(Lines.DEFAULT)) {
			this.productBuilder = TargetBuilder.getInstance();
		}
		this.productMatching = new ProductMatching(this.productBuilder);
		this.alloyProductGenerator = new AlloyProductGenerator(wellFormedness,this.productBuilder);
	}

	private void setup(ProductLine souceLine, ProductLine targetLine) throws IOException, AssetNotFoundException {
		this.dependenciasCopiadas = null;
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
	 * One of the main methods of this class. It is responsible to check the SPL. <br></br>
	 * @param sourceLine Source SPL.  <br></br>
	 * @param targetLine Target SPL.  <br></br>
	 * @param timeout  <br></br>
	 * @param qtdTestes Amount of applied tests per methods. <br></br>
	 * @param approach The approach used to check the SPL Evolution. <br></br>
	 * @param criteria  <br></br>
	 * @param resultado SPL Evolution Results/Report  <br></br>
	 * @return Returns whether the SPL evolution is a refinement  <br></br>
	 * @throws IOException Case this is not successful, throws IO Exception.  <br></br>
	 * @throws AssetNotFoundException Case this is not successful, throws IO Asset Not Found Exception. <br></br>
	 * @throws DirectoryException  Case this is not successful, throws Directory Exception. <br></br>
	 */
	public boolean checkLPS(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws IOException, AssetNotFoundException, DirectoryException {
		
		/* Till here this is not a refinement yet. */
		boolean isRefinement = false;
		
		ConfigurationKnowledge configurationKnoledge = sourceLine.getCk();
		System.out.println("\n\nSOURCE PRODUCT LINE CONFIGURATION KNOWLEDGE:\n\n");
		
		configurationKnoledge.print("SOURCE");
		
		sourceLine.getPreprocessProperties();

		System.out.println("\n\nSOURCE PRODUCT LINE PRE-PROCESS PROPERTIES:\n\n");
		sourceLine.printPreprocessProperties("SOURCE");

		System.out.println("\n\nSOURCE PRODUCT LINE PRE-PROCESS FILES:\n\n");
		sourceLine.printFilesListToPreProcess();
		
		generateFilesWithoutPreProcessTags(sourceLine);
		
		/* Check whether the Software Product Line is well formed. */
		System.out.println("\n\n\n\n\t\tLet's check if the SPL is well formed.\n");
		
		boolean isWF = this.wellFormedness.isWF(sourceLine, targetLine); 
		
		System.out.println("\n\n\n\n\t\tOk. We have already checked the well formedness.\n");
		System.out.println("\n\t -> WF: " + isWF+ "\n");
		/* Set this property in Results Class - To be used as a report later.*/
		SPLOutcomes.getInstance().setWF(isWF);

		/*Verify Feature Model -  FM is not verified for the Naive Approach, ONLY Well Formedness*/
		if (isWF) {
			
			/* Verify whether Configuration knowledge and Feature Model is a refinement */
			
			boolean areAllProductsMatched = this.productMatching.areAllProductsMatched(sourceLine, targetLine); 
			
			System.out.println("FM and CK are refinement:- " + areAllProductsMatched );
			
			/*Set this information in the Results/Short Report.*/
			SPLOutcomes.getInstance().setFMAndCKRefinement(areAllProductsMatched);

			/*which approach is settled ? */
			if (propertiesObject.getApproach() == Approach.APP || propertiesObject.getApproach() == Approach.AP || areAllProductsMatched) {

				/* verify whether two AM's is equal. */
				boolean isAssetMappingsEqual = this.isAssetMappingEqual(sourceLine, targetLine);
				/* Set it down in the results. */
				SPLOutcomes.getInstance().setAssetMappingsEqual(isAssetMappingsEqual);

				if (propertiesObject.getApproach() == Approach.APP || propertiesObject.getApproach() == Approach.AP || !isAssetMappingsEqual) {

					HashSet<String> changedFeatures = null;

					if (propertiesObject.getApproach() == Approach.IP || propertiesObject.getApproach() == Approach.IC || propertiesObject.getApproach()==Approach.EIC) {
						/* Generate tests only for impacted features. */
						changedFeatures = getChangedFeatureNames(targetLine);
					}

					SPLOutcomes.getInstance().getMeasures().getTempoExecucaoAbordagem().startContinue();

					if (propertiesObject.getApproach() == Approach.IC || propertiesObject.getApproach()==Approach.EIC) {
						/* Only generates tests for modified classes and do not generate products. */
						/* Generates two tests per method. */
						isRefinement = this.isAssetMappingRefinement(sourceLine, targetLine, changedFeatures, propertiesObject);
						/*Ja supoe que o comportamento tamb�m n�o foi preservado.*/
						SPLOutcomes.getInstance().setCompObservableBehavior(isRefinement);
						if(isRefinement){System.out.println("\nAsset Mapping Refined.\n");} else {System.out.println("\nSorry to inform you that Asset Mapping was not refined.\n");}
					} else {
						/* Generate tests for all classes and all products. */
						isRefinement = this.testProducts(sourceLine, targetLine, propertiesObject);
						SPLOutcomes.getInstance().setCompObservableBehavior(isRefinement);
						if(isRefinement){System.out.println("\nCompatible Observable Behavior\n");} else {System.out.println("\nSource and Target do not have compatible observable behavior.\n");} 
					}
				
					SPLOutcomes.getInstance().getMeasures().getTempoExecucaoAbordagem().pause();
					/*Set whether the SPL evolution is a refinement. */
					SPLOutcomes.getInstance().getMeasures().setResult(isRefinement);

					if (isRefinement) {
						System.out.println("\nThe Software Product Line is a refinement.\n");
					} else {
						System.out.println("The Software Product Line is NOT a refinement.\n");
					}
					SPLOutcomes.getInstance().setRefinement(isRefinement);
				}else {
					/*Set whether the SPL evolution is a refinement. */
					SPLOutcomes.getInstance().getMeasures().setResult(true);
					System.out.println("\n\nThe Software Product Line was refined.\n");
					isRefinement = true;
					SPLOutcomes.getInstance().setRefinement(true);
				}
			} 
			/* The approaches that can be applied to check the refinement is NAIVE_2_ICTAC and NAIVE_1_APROXIMACAO. */
			/* However, if CK and FM is a refinement, it is possible to check it out with the two others aproaches. */
			else {
				/* This approach can not be applied. */
				System.out.println("The " + propertiesObject.getApproach() + " approach can not be applied.\nFM' and CK' don't refine FM and CK.");
				SPLOutcomes.getInstance().setObservation("The " + propertiesObject.getApproach() + " approach can not be applied.\nFM' and CK' don't refine FM and CK.");
			}
		} else {
			System.out.println("\n Software Product Line Short Report :\n");
		    System.out.println("- It is NOT a refinement.\n");
		    System.out.println("- It is NOT well formed.\n");
		    SPLOutcomes.getInstance().setWF(false);
		    SPLOutcomes.getInstance().setRefinement(false);
			/* Software Product Line Short Report : */
			/* It is not a refinement. It is not well formed. */
		}
		return isRefinement;
	}

	private void generateFilesWithoutPreProcessTags(ProductLine sourceLine) {
		Iterator<String> i = sourceLine.getFilesToPreProcess().iterator();
		while(i.hasNext()){
			String relativeDirectory = (String) i.next();
			relativeDirectory = relativeDirectory.replaceAll("\\\\", System.getProperty("file.separator"));			
			System.out.println("\nrelativeDirectory = " + relativeDirectory);
			String fullDirectory = sourceLine.getPath() + Constants.FILE_SEPARATOR + relativeDirectory;
			System.out.println("\nFull File Directory: " + fullDirectory);
			getConditionalCompilationTags(new File(fullDirectory));
		}
	}

	private Set<String> getConditionalCompilationTags(File file) {
		Set<String> ccTags = new HashSet();
		try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", fileContent = "";
            while((line = reader.readLine()) != null){
                fileContent += line + "\r\n";
            }
            reader.close();
           
            Pattern regex = Pattern.compile("\\$\\w+");
    	    Matcher regexMatcher = regex.matcher(fileContent);
    	    int i = 1;
    	    while (regexMatcher.find()){
    	    	String s = regexMatcher.group();
    	      	System.out.println("s: " + (i++) + " - " + s);
    	      	/* Add the conditional Compilation tag without dollar symbol. */
    	      	ccTags.add(s.substring(1));
    	    } 
    	    System.out.println("\nsize: " + ccTags.size());
    	    Iterator<String> it = ccTags.iterator();
    	    while(it.hasNext()){
    	    	System.out.println("-> " + it.next());
    	    }
		}catch (Exception e) {
			e.printStackTrace();
		}
		return ccTags;
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

	private boolean testProducts(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws IOException, DirectoryException {
		
		System.out.println("\n\nTesting Products: ");
		boolean isRefactoring = true;
		Approach approach = propertiesObject.getApproach();

		try {
			SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(0);

			for (Product productSource : sourceLine.getProducts()) {
				productSource.printSetOfFeatures();
				if (approach == Approach.APP || approach == Approach.AP || (approach == Approach.IP && productSource.containsSomeAsset(this.classesModificadas, sourceLine.getMappingClassesSistemaDeArquivos()))) {
					
					this.productBuilder.generateProduct(productSource, sourceLine.getPath());

					Product provavelCorrespondente = productSource.getLikelyCorrespondingProduct();

					if (provavelCorrespondente != null) {
						this.productBuilder.generateProduct(provavelCorrespondente, targetLine.getPath());
						isRefactoring = isRefactoring && CommandLine.isRefactoring(productSource, provavelCorrespondente, sourceLine.getControladoresFachadas(), propertiesObject);
					} else {
						/* If the source product does not have a correspondent target product it is NOT considered a refactoring.
						 * It means, that the behavior was not preserved once we can not find even a correspondent target product.*/
						System.out.println("This product does not have a correspondent target product.");
						isRefactoring = false;
					}

					if (propertiesObject.getApproach() != Approach.AP && propertiesObject.getApproach() != Approach.IP) {

						//Testa se o comportamento nao bate com nenhum outro destino. Exceto para o caso de NAIVE_WITHOUT_RENAMING.
						if (!isRefactoring) {
							for (Product productTarget : targetLine.getProducts()) {
								if (productTarget != provavelCorrespondente) {
									this.productBuilder.generateProduct(productTarget, targetLine.getPath());

									isRefactoring = CommandLine.isRefactoring(productSource, productTarget, sourceLine.getControladoresFachadas(), propertiesObject);

									//Para de procurar se encontrar um par com mesmo comportamento.
									if (isRefactoring) {
										break;
									}
								}
							}
						}
					}

					/* If one method is not a refactoring, we can break this loop and let the user know about the refactoring was not applied successfully. */
					/* Source and Target does not have compatible observable behavior. */
					if (!isRefactoring) {
						break;
					}
				}
			}
		} catch (AssetNotFoundException e) {
			e.printStackTrace();
		}

		System.out.println("This evolution is a refactoring ?: " + "< " + isRefactoring + " >");

		return isRefactoring;
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
	 * Asks if the asset mapping is a refinement .	 <br></br>
	 * @param sourceLine
	 * @param targetLine
	 * @param timeout
	 * @param qtdTestes
	 * @param approach
	 * @param changedFeatures
	 * @param criteria
	 * @param resultado
	 * @return
	 * @throws IOException
	 */
	private boolean isAssetMappingRefinement(ProductLine sourceLine, ProductLine targetLine,HashSet<String> changedFeatures, FilePropertiesObject propertiesObject) throws IOException {
		boolean ehAssetMappingRefinement = false;
		try {
			ehAssetMappingRefinement = this.checkAssetMappingBehavior(sourceLine, targetLine, changedFeatures , propertiesObject);
		} catch (AssetNotFoundException e1) {
			e1.printStackTrace();
		} catch (DirectoryException e1) {
			e1.printStackTrace();
		}
		return ehAssetMappingRefinement;
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


	private Collection<String> getAboveDependencies(File classe, HashSet<String> dependenciesOfModifiedClasses) {
		if (classe.isDirectory() && !classe.getAbsolutePath().contains(".svn") ) { 
			System.out.println("\nDirectory: " + classe.getAbsolutePath());
			File[] files = classe.listFiles();
			for (File subFile : files) {
				this.getAboveDependencies(subFile, dependenciesOfModifiedClasses);
			}
		} else {
			if (classe.getAbsolutePath().endsWith("java") && !classe.getAbsolutePath().contains("ProjectManagerController.java")) {
				System.out.println("\nFile: " + classe.getAbsolutePath());
				// Get All Dependencies of this Class
				Collection<String> dependencias = Main.v().getDependences(classe.getName().replaceAll(".java", ""), classe.getParent()); 
				dependenciesOfModifiedClasses = belongToModifiedClasses(getPackageName(classe),dependencias, dependenciesOfModifiedClasses);
			}
		}
		return dependenciesOfModifiedClasses;
	}

	private String getPackageName(File classe) {
		String words[] = classe.getAbsolutePath().split("src/");
		String packagePath =  "";
		if(words.length>2){
			/* Target Case */
			packagePath = words[words.length-2] + "src/" +words[words.length-1];
		}else if (words.length==2){
			packagePath =  words[1];
		}
		return packagePath.replaceAll("/", ".");
	}

	private HashSet<String> belongToModifiedClasses(String classe, Collection<String> dependencias, HashSet<String> dependenciesOfModifiedClasses) {
		Iterator<String> i = dependencias.iterator();
		while(i.hasNext()){
			String s = i.next();
			System.out.println("\nDependencia: " + s);
			Iterator<String> iterator2 = this.classesModificadas.iterator();
			while(iterator2.hasNext()){
				String string2 = iterator2.next();
				String[] words = string2.split("\\.");//words[words.length-1];
				String w = words[words.length-2];
				if(s.equals(w)){
					// Add class in the dependencies of modified classes set.
					dependenciesOfModifiedClasses.add(classe);
					break;
				}
			}
		}
		return dependenciesOfModifiedClasses;
	}

	/**
	 * Checa apenas classes modificadas. Se mais de MAX_CLASSES_MODIFICADAS
	 * forem alteradas, a otimizacao eh descartada. This methods also is
	 * responsible to answer whether the evolution is a refactoring or not. It
	 * means, is the evolution preserving behavior ? <br>
	 * </br>
	 * 
	 * @param sourcePath
	 * @param targetPath
	 * @param timeout
	 * @param changedFeatures
	 * @param criteria
	 * @param totalAnalysisTime
	 * @return
	 * @throws IOException
	 * @throws AssetNotFoundException
	 * @throws DirectoryException
	 */
	private boolean checkAssetMappingBehavior(ProductLine sourceLine, ProductLine targetLine,HashSet<String> changedFeatures, FilePropertiesObject in) throws IOException, AssetNotFoundException, DirectoryException {
		boolean sameBehavior = false;
		
		/* Looking for classes that depends of the modified classes to compile. */
		if(in.getApproach()==Approach.EIC){
			this.classesModificadas = this.getAboveDependencies(new File(sourceLine.getPath()+"/src"),new HashSet<String>());
		}
	    /*____________________________________*/
		
		printListofModifiedClasses();
		
		long startedTime = System.currentTimeMillis();

		/* The amount of modified classes are greater than MAX_CLASSES_MODIFICADAS */
		/* The optimization for AM won't be realized. */
		if (this.classesModificadas.size() > MAX_CLASSES_MODIFICADAS) {
			System.out.println("\n\n ! Warning: The amount of modified classes are greater than" + MAX_CLASSES_MODIFICADAS);
			System.out.println("The optimization for AM won't be realized.\n");
		} else {
			/* Set the amout of compiled products to 1*/
			SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(1);

			String productPath = Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR + "Product0" + Constants.FILE_SEPARATOR;

			/* Creating folders for source and target products, which will be the same for all modified classes and their dependencies. */
			File testSourceDirectory = FileManager.getInstance().createDir( productPath + "source" + Constants.FILE_SEPARATOR + (line == Lines.MOBILE_MEDIA ? ProductBuilder.SRCPREPROCESS : "src"));
			File testTargetDirectory = FileManager.getInstance().createDir( productPath + "target" + Constants.FILE_SEPARATOR + (line == Lines.MOBILE_MEDIA ? ProductBuilder.SRCPREPROCESS : "src"));

			/* This creates bin folder for SOURCE PRODUCT and TARGET PRODUCT. */
			FileManager.getInstance().createDir(productPath + "source" + Constants.FILE_SEPARATOR + "bin");
			FileManager.getInstance().createDir(productPath + "target" + Constants.FILE_SEPARATOR + "bin");

			/* Get library path of the SOURCE and TARGET SPL*/
			String libPathSource = sourceLine.getLibPath();
			String libPathtarget = targetLine.getLibPath();

			if (libPathSource != null && libPathtarget != null) {
				/* This copies all library files for SOURCE PRODUCT and TARGET PRODUCT lib path. */
				FileManager.getInstance().copyLibs(libPathSource, productPath + "source" + Constants.FILE_SEPARATOR + "lib");
				FileManager.getInstance().copyLibs(libPathtarget, productPath + "target" + Constants.FILE_SEPARATOR + "lib");
			}

			/* Classes that will have generated tests. */
			String classes = "";
			String classeToGenerateTestes = "";

			this.listaAspectos = new HashSet<String>();

			System.out.println("\n Amount of modified classes: " + classesModificadas.size()+"\n");
			/* walk through all changed classes. */
			for (String classe : this.classesModificadas) {
				System.out.println(" - Modified: " + classe);
				
				/* Get the whole path of the modified class. */
				String fileSourcePath = sourceLine.getMappingClassesSistemaDeArquivos().get(classe);
				String fileTargetPath = targetLine.getMappingClassesSistemaDeArquivos().get(classe);

				if(fileSourcePath!=null && fileTargetPath!=null){
					/*Copying source version of the modified file. */
					String destinationPath = null;
					File fileDestination = null;
					destinationPath = testSourceDirectory.getAbsolutePath() + FileManager.getInstance().getPathAPartirDoSrc(fileSourcePath).replaceFirst("src", "");
					fileDestination = new File(destinationPath);
					classeToGenerateTestes = classe;
					System.out.println("$classeToGenerateTestes Antes: " + classeToGenerateTestes);
					
					if (this.line == Lines.TARGET){
						classeToGenerateTestes = FileManager.getInstance().getPathAPartirDoSrc(classeToGenerateTestes).replaceFirst(Pattern.quote("src.java."), "");
					}
					System.out.println("*classeToGenerateTestes Depois: " + classeToGenerateTestes);
					FileManager.getInstance().createDir(fileDestination.getParent());
					FileManager.getInstance().copyFile(fileSourcePath, fileDestination.getAbsolutePath());

					/* Catch the class path in source version. */
					File fileSource = fileDestination;
					destinationPath = testTargetDirectory.getAbsolutePath()	+ FileManager.getInstance().getPathAPartirDoSrc(fileTargetPath).replaceFirst("src", "");
					fileDestination = new File(destinationPath);
					FileManager.getInstance().createDir(fileDestination.getParent());
					FileManager.getInstance().copyFile(fileTargetPath, fileDestination.getAbsolutePath());

					/* Catch the class path in source version. */
					File fileTarget = fileDestination;

					this.dependenciasCopiadas = new HashSet<String>();

					this.copyDependencies(fileSource, testSourceDirectory, sourceLine.getMappingClassesSistemaDeArquivos(), null, sourceLine.getDependencias());
					this.copyDependencies(fileTarget, testTargetDirectory, targetLine.getMappingClassesSistemaDeArquivos(), null, targetLine.getDependencias());

					/* As aspects are not preprocessed, it will be necessary to manually move the aspects to the src folder after the preprocessing. */
					if (fileSource.getAbsolutePath().endsWith("aj")) {
						this.listaAspectos.add(fileSource.getAbsolutePath());
					}
					if (fileTarget.getAbsolutePath().endsWith("aj")) {
						this.listaAspectos.add(fileTarget.getAbsolutePath());
					}
					if (classes.equals("")){
						classes = classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
					} else if (!classes.contains(classeToGenerateTestes.replaceAll(".java", ""))) {
						classes = classes + "|" + classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
					}
				}
				
			}

			/* Check only products that have any of the modified classes. */
			/* This variable will store modified products. It means, products that have at least one modified asset. */ 
			ArrayList<Product> produtosQueContemClassesModificadas = new ArrayList<Product>();
			
			for (Product product : sourceLine.getProducts()){
				/* Is this product contains at least one modified asset ? */
				if (product.containsSomeAsset(this.classesModificadas, sourceLine.getMappingClassesSistemaDeArquivos())) {
					/* if the answer is YES, add this product to the modified products variable. */
					produtosQueContemClassesModificadas.add(product);
				}
			}

			//Se tiver aspectos ou tags de pre-processamento no codigo, faz um for com todos os produtos possiveis.
			//Filtrar entre os produtos poss�veis, removendo os que tiverem mesmos conjuntos de aspectos que interferem

			//		**Eh melhor entrar aqui se temAspectos, mesmo que nao tenha preprocess
			//		**Quando tiver soh preprocess entra no else
			//		**
			
			/* Does SOURCE product line contains aspectos ? */
			if (sourceLine.temAspectos()) {
				HashSet<HashSet<String>> products = new HashSet<HashSet<String>>();
				
				/* Removes all of the elements from this list. */
				produtosQueContemClassesModificadas.clear();

				for (Product product : sourceLine.getProducts()) {
					/* Is this product contains at least one modified asset ? */
					if (product.containsSomeAsset(this.classesModificadas, sourceLine.getMappingClassesSistemaDeArquivos())) {
						produtosQueContemClassesModificadas.add(product);
						products.add(product.getFeaturesList());
					}
				}

				/* if it has aspects, but there are no classes with more than one version in the SPL. */
				HashSet<HashSet<String>> pseudoProductsToBePreprocessed = new HashSet<HashSet<String>>();

				this.mapeamentoFeaturesAspectosSource = new HashMap<HashSet<String>, HashSet<String>>();
				this.mapeamentoFeaturesAspectosTarget = new HashMap<HashSet<String>, HashSet<String>>();

				for (HashSet<String> product : products) {
					HashSet<String> prodToBuild = this.makeCombination(product, sourceLine, targetLine);
					if (prodToBuild.size() > 0) {
						pseudoProductsToBePreprocessed.add(prodToBuild);
					}
				}

				SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(pseudoProductsToBePreprocessed.size());

				sameBehavior = true;

				System.out.println("\n products size: " + pseudoProductsToBePreprocessed.size());
				int i = 0;
				for (HashSet<String> prod : pseudoProductsToBePreprocessed) {
					System.out.print("&PRODUCT: " + (i++) + " -> ");
					Iterator<String> it = prod.iterator();
					String s = "";
					while(it.hasNext()){
						s = s + "[ " + it.next() + " ]";
						
					}
					System.out.println(s);
					HashSet<String> aspectosDaConfiguracaoSource = this.mapeamentoFeaturesAspectosSource.get(prod);
					HashSet<String> aspectosDaConfiguracaoTarget = this.mapeamentoFeaturesAspectosTarget.get(prod);

					ArrayList<File> filesToTrash = new ArrayList<File>();

					for (String aspecto : aspectosDaConfiguracaoSource) {
						String destinationPath = testSourceDirectory.getAbsolutePath() + aspecto.split("src")[1];
						if (!this.classesModificadas.contains(aspecto) && !this.listaAspectos.contains(destinationPath)) {
							File fileDestination = new File(destinationPath);
							FileManager.getInstance().createDir(fileDestination.getParent());
							FileManager.getInstance().copyFile(sourceLine.getMappingClassesSistemaDeArquivos().get(FileManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());
							filesToTrash.add(fileDestination);
							filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
							this.listaAspectos.add(fileDestination.getAbsolutePath());
							this.copyDependencies(fileDestination, testSourceDirectory, sourceLine.getMappingClassesSistemaDeArquivos(), filesToTrash, sourceLine.getDependencias());
						}
					}

					for (String aspecto : aspectosDaConfiguracaoTarget) {
						String destinationPath = testTargetDirectory.getAbsolutePath() + aspecto.split("src")[1];

						if (!this.classesModificadas.contains(aspecto) && !this.listaAspectos.contains(aspecto)) {
							File fileDestination = new File(destinationPath);

							FileManager.getInstance().createDir(fileDestination.getParent());
							FileManager.getInstance().copyFile(targetLine.getMappingClassesSistemaDeArquivos().get( FileManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());

							filesToTrash.add(fileDestination);
							filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
							this.listaAspectos.add(fileDestination.getAbsolutePath());

							this.copyDependencies(fileDestination, testTargetDirectory, targetLine.getMappingClassesSistemaDeArquivos(), filesToTrash, targetLine.getDependencias());
						}
					}

					if (this.line == Lines.TARGET || this.line == Lines.DEFAULT) {
						//Para a TaRGeT, os arquivos sao pre processados com o Velocity dentro da propria
						//pasta, sem copias. Com isso, eh necessario renomear a pasta para src manualmente.

						if (prod.size() > 0/* && !(prod.size() == 1 && prod.iterator().next().contains("fake"))*/) {
							this.productBuilder.preprocessVelocity(prod, testSourceDirectory, sourceLine, testSourceDirectory.getParent());
							this.productBuilder.preprocessVelocity(prod, testTargetDirectory, targetLine, testTargetDirectory.getParent());
						}
						/*Rename srcpreprocess folder to src*/
						/*testSourceDirectory.renameTo(new File(testSourceDirectory.getParent()+ System.getProperty("file.separator") + "src"));
						testTargetDirectory.renameTo(new File(testTargetDirectory.getParent()+ System.getProperty("file.separator") + "src"));*/
						
					} else {
						//Para o MobileMedia, pre processa com Antenna, que copia o codigo da pasta
						//srcprecess para src.
						this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), testSourceDirectory.getParent());

						this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), testTargetDirectory.getParent());

						System.out.println("########################################################" + prod);

						//Como o Antenna nao pre processa aspectos, eles tambem nao sao copiados para 
						//a pasta src. Eh necessario copia-los manualmente.
						for (String aspect : this.listaAspectos) {
							String dir = new File(aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src")).getParentFile().getAbsolutePath();
							FileManager.getInstance().createDir(dir);
							FileManager.getInstance().copyFile(aspect, aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src"));
						}
					}

					System.out.println("\nClasses que receberao testes JUNIT: " + classes);
					// int sourceProductId, int targetProductId, String sourceProductPath, String targetProductPath, String classes, FilePropertiesObject propertiesObject, boolean sourceIsCompiled, boolean targetIsCompiled
					sameBehavior = sameBehavior && CommandLine.isRefactoring(0, 0, testSourceDirectory.getParent(), testTargetDirectory.getParent(), classes, in , false, false);
					for (File file : filesToTrash) {
						file.delete();
						this.listaAspectos.remove(file.getAbsolutePath());
						this.dependenciasCopiadas.remove(file.getAbsolutePath());
					}

					filesToTrash.clear();
					
					System.out.println("###########################" + sameBehavior);
					
					if(!sameBehavior){
						break;
					}
				}
			} else {
				//Entra aqui sempre que nao tem Aspectos.
				//Pode haver pre processamento.
				//Soh MobileMediaOO entra aqui

				this.constantsPreProcessor = new HashSet<String>();
				this.getPreProcessorConstantes(testSourceDirectory);

				if (this.constantsPreProcessor.size() > 0) {
					HashSet<HashSet<String>> products = sourceLine.getSetsOfFeatures();

					products = this.productBuilder.filter(products, changedFeatures);

					HashSet<HashSet<String>> pseudoProductsToBePreprocessed = new HashSet<HashSet<String>>();

					for (HashSet<String> product : products) {
						HashSet<String> prodToBuild = this.getFeaturesEmComum(product, this.constantsPreProcessor);

						if (prodToBuild.size() > 0) {
							pseudoProductsToBePreprocessed.add(prodToBuild);
						}
					}

					SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(pseudoProductsToBePreprocessed.size());

					sameBehavior = true;

					for (HashSet<String> prod : pseudoProductsToBePreprocessed) {
						this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), testSourceDirectory.getParent());

						this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), testTargetDirectory.getParent());

						sameBehavior = sameBehavior	&& CommandLine.isRefactoring(0, 0, testSourceDirectory.getParent(), testTargetDirectory.getParent(), classes, in , false, false);

						System.out.println("###########################" + sameBehavior);
					}
				} else {
					//Se nem tem aspectos nem preprocessamento.
					testSourceDirectory.renameTo(new File(testSourceDirectory.getParent() + Constants.FILE_SEPARATOR + "src"));
					testTargetDirectory.renameTo(new File(testTargetDirectory.getParent() + Constants.FILE_SEPARATOR + "src"));
					sameBehavior = CommandLine.isRefactoring(0, 0, testSourceDirectory.getParent(), testTargetDirectory.getParent(), classes, in , false, false);
					System.out.println("###########################" + sameBehavior);
				}
			}

			long finishedTime = System.currentTimeMillis();

			System.out.println("Asset mapping verificado em: " + String.valueOf((finishedTime - startedTime) / 1000) + " segundos.");
		}
		return sameBehavior;
	}

	private void printListofModifiedClasses() {
		Iterator<String> i = this.classesModificadas.iterator();
		System.out.println("\nList of Modified Classes: ");
		while(i.hasNext()){
			System.out.println(i.next());
		}
		System.out.println("\n--------------------------");
	}

	private HashSet<String> getFeaturesEmComum(HashSet<String> product, HashSet<String> constantsPreProcessor) {
		HashSet<String> result = new HashSet<String>();

		for (String constant : constantsPreProcessor) {
			String feat = this.productBuilder.getPreprocessConstantsToFeatures().get(constant);

			if (product.contains(feat)) {
				result.add(feat);
			}
		}

		return result;
	}

	/**
	 * @param product
	 * @param sourceLine
	 * @param targetLine
	 * @return
	 */
	private HashSet<String> makeCombination(HashSet<String> product, ProductLine sourceLine, ProductLine targetLine) {
		
		HashSet<String> result = new HashSet<String>();

		//Como o mapeamento nao mudou, os aspectos da versao source e da target sao os mesmos.
		ArrayList<String> aspectosDoProdutoSource = this.getAspectosDoProduto(product, sourceLine);

		//Os que interferem podem ter mudado j� que o conte�do deles mudou, podento ter pointcuts e advices novos.
		HashSet<String> aspectosQueInterferemNaClasseModificadaSource = this.getAspectosQueInterferemNaClasseModificada(aspectosDoProdutoSource, sourceLine.getMappingClassesSistemaDeArquivos());
		HashSet<String> aspectosQueInterferemNaClasseModificadaTarget = this.getAspectosQueInterferemNaClasseModificada(aspectosDoProdutoSource, targetLine.getMappingClassesSistemaDeArquivos());

		for (String feature : product) {
			HashMap<String, String> FeaturesConstants = this.productBuilder.getPreprocessFeaturesToConstants();
			String feat = FeaturesConstants.get(feature);
			System.out.println("\n feat: " + feat);
			if (feat != null) {
				result.add(feature);
			}
		}

		boolean jaExiste = false;

		//Verifica se o mesmo conjunto de features j� foi mapeado em outro conjunto de aspectos.
		if (!this.mapeamentoFeaturesAspectosSource.values().contains(aspectosQueInterferemNaClasseModificadaSource)) {
			if (this.mapeamentoFeaturesAspectosSource.containsKey(result) || result.isEmpty()) {
				result.add("fake" + Math.random());
			}

			this.mapeamentoFeaturesAspectosSource.put(result, aspectosQueInterferemNaClasseModificadaSource);
			this.mapeamentoFeaturesAspectosTarget.put(result, aspectosQueInterferemNaClasseModificadaTarget);
		} else {
			for (HashSet<String> prodKey : this.mapeamentoFeaturesAspectosSource.keySet()) {
				if (this.mapeamentoFeaturesAspectosSource.get(prodKey).equals(aspectosQueInterferemNaClasseModificadaSource) && this.mapeamentoFeaturesAspectosTarget.get(prodKey).equals(aspectosQueInterferemNaClasseModificadaTarget)) {
					jaExiste = this.equivalent(prodKey, result);
				}
			}
			if (!jaExiste) {
				if (this.mapeamentoFeaturesAspectosSource.containsKey(result) || result.isEmpty()) {
					result.add("fake" + Math.random());
				}
				this.mapeamentoFeaturesAspectosSource.put(result, aspectosQueInterferemNaClasseModificadaSource);
				this.mapeamentoFeaturesAspectosTarget.put(result, aspectosQueInterferemNaClasseModificadaTarget);
			}
		}

		return result;
	}

	private boolean equivalent(HashSet<String> prodKeyAntiga, HashSet<String> resultNovo) {
		//A prodKeyAntida pode ter feature fake.
		boolean result = true;

		for (String feat : prodKeyAntiga) {
			if (!resultNovo.contains(feat) && !feat.contains("fake")) {
				result = false;
			}

			if (!result) {
				break;
			}
		}

		if (result) {
			for (String feat : resultNovo) {
				if (!prodKeyAntiga.contains(feat)) {
					result = false;
				}

				if (!result) {
					break;
				}
			}
		}

		return result;
	}

	private HashSet<String> getAspectosQueInterferemNaClasseModificada(ArrayList<String> aspectosDoProduto, HashMap<String, String> mapping) {

		HashSet<String> aspectosQueInterferemNaClasseModificada = new HashSet<String>();

		for (String aspecto : aspectosDoProduto) {
			//As classes em que um aspecto interferem sao informadas na primeira
			//linha do arquivo aj.

			String path = mapping.get(FileManager.getInstance().getCorrectName(this.replaceBarrasPorSeparator(aspecto)));

			Collection<String> classesEmQueOAspectoInterfe = FileManager.getInstance().getDependenciasAspectos(new File(path));

			for (String classeModificada : this.classesModificadas) {

				for (String classeEmQueAspectoInterfere : classesEmQueOAspectoInterfe) {
					if (classeModificada.contains(classeEmQueAspectoInterfere)) {
						aspectosQueInterferemNaClasseModificada.add(aspecto);
					}
				}
			}
		}

		return aspectosQueInterferemNaClasseModificada;
	}

	private String replaceBarrasPorSeparator(String aspecto) {
		String result = "";

		String[] parts = aspecto.split(Pattern.quote("/"));

		for (String part : parts) {
			if (result.equals("")) {
				result = part;
			} else {
				result = result + Constants.FILE_SEPARATOR + part;
			}

		}

		return result;
	}

	private ArrayList<String> getAspectosDoProduto(HashSet<String> product, ProductLine sourceLine) {
		ArrayList<String> aspectosDoProduto = new ArrayList<String>();

		ConfigurationKnowledge sourceCK = sourceLine.getCk();
		HashMap<String, String> evalCKSource = sourceCK.evalCKDestinos(product);

		for (String constant : evalCKSource.keySet()) {
			//Considerando que o mapeamento seja se 1:1.

			String asset = sourceLine.getAssetMapping().get(constant.trim());

			if (asset.endsWith("aj")) {
				aspectosDoProduto.add(asset);
			}
		}

		return aspectosDoProduto;
	}

	private void getPreProcessorConstantes(File file) throws IOException {
		if (file.isDirectory()) {
			for (File fileFromList : file.listFiles()) {
				this.getPreProcessorConstantes(fileFromList);
			}
		} else {
			String linha = "";

			FileReader reader = new FileReader(file);

			BufferedReader in = new BufferedReader(reader);

			while ((linha = in.readLine()) != null) {
				if (linha.contains("#if") || linha.contains("#elif")) {
					linha = linha.trim();

					String[] parts = linha.split(" ");

					for (String part : parts) {

						//Se nao eh um ifdef, elif ou simbolo logico (||, &&, etc)
						if (!part.contains("#") && part.length() > 2) {
							this.constantsPreProcessor.add(part);
						}
					}
				}
			}

			in.close();
			reader.close();
		}
	}

	private void copyDependencies(File classe, File destinationDirectory, HashMap<String, String> mapping, ArrayList<File> filesToTrash, HashMap<String, Collection<String>> dependenciasCache) throws AssetNotFoundException, DirectoryException {

		String pathDependencia = FileManager.getInstance().getPathAPartirDoSrc( classe.getAbsolutePath().replaceFirst("srcpreprocess", "src"));

		if (!pathDependencia.startsWith("/")) {
			pathDependencia = "/" + pathDependencia;
		}

		Collection<String> dependencias = dependenciasCache.get(pathDependencia.replaceAll(Pattern.quote(Constants.FILE_SEPARATOR), "/"));

		if (dependencias == null) {
			if (classe.getAbsolutePath().endsWith(".java")) {
				
				
				dependencias = Main.v().getDependences(classe.getName().replaceAll(".java", ""), classe.getParent());

				//Classes podem ser dependentes de aspectos
				//Ocorre quando excecoes lancadas em classes sao tratadas apenas em Aspectos.
				dependencias.addAll(FileManager.getInstance().getDependenciasAspectos(classe));
			} else {
				//Dependencias de aspectos serao identificadas pelo import deles.
				dependencias = FileManager.getInstance().getDependenciasDeAspectosPeloImport(classe);
			}
		}

		for (String dependecia : dependencias) {
			if (!classe.getAbsolutePath().contains(dependecia)
					|| (!dependecia.contains(".") && !classe.getAbsolutePath().contains("." + dependecia))) {

				String path = this.getPathClassMapping(dependecia, mapping);

				if (path != null) {
					File file = new File(path);

					// destinationFolder inclui as pastas intermediarias ate o arquivo.
					// destinationDirectory soh vai ate source ou target.
					String destinationFolder = destinationDirectory.getAbsolutePath()
							+ FileManager.getInstance().getPathAPartirDoSrc(file.getAbsolutePath()).replaceFirst("src", "");

					File fileDestination = new File(destinationFolder);

					if (this.dependenciasCopiadas.add(fileDestination.getAbsolutePath())) {
						FileManager.getInstance().createDir(fileDestination.getParent());

						if (!fileDestination.exists()) {
							FileManager.getInstance().copyFile(file.getAbsolutePath(), fileDestination.getAbsolutePath());

							if (fileDestination.getAbsolutePath().endsWith("aj")) {
								this.listaAspectos.add(fileDestination.getAbsolutePath());
							}

							if (filesToTrash != null) {
								filesToTrash.add(fileDestination);
								filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS,
										"src")));
							}

							this.copyDependencies(file, destinationDirectory, mapping, filesToTrash, dependenciasCache);
						}
					}
				}
			}
		}
	}

	private String getPathClassMapping(String dependecia, HashMap<String, String> mapping) {
		String result = null;

		// A dependencia nem sempre eh um nome de classe com pacote. As chaves do
		// mapping sempre sao.
		// Eh necessario checar se dependencia faz parte de alguma key.
		for (String key : mapping.keySet()) {
			if ((dependecia.contains(".") && key.endsWith(dependecia + ".java")) || key.endsWith("." + dependecia + ".java")
					|| (dependecia.contains(".") && key.endsWith(dependecia + ".aj")) || key.endsWith("." + dependecia + ".aj")) {
				result = mapping.get(key);

				break;
			}
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
		
		/*boolean areAllProductsMatched = this.productMatching.areAllProductsMatched(sourceLine, targetLine); 
		System.out.println("areAllProductsMatched: " + areAllProductsMatched);
		
		AllProductPairs app = new AllProductPairs(wellFormedness, this.builder);
		System.out.println("Refactoring ? " + app.evaluate(sourceLine, targetLine, propertiesObject));
		
		AllProducts ap = new AllProducts(wellFormedness, this.builder);
		System.out.println("Refactoring ? " + ap.evaluate(sourceLine, targetLine, propertiesObject));
		
		boolean isAssetMappingsEqual = this.isAssetMappingEqual(sourceLine, targetLine);
		System.out.println("\n AM changed: " + isAssetMappingsEqual);
		ImpactedProducts ip = new ImpactedProducts(wellFormedness, builder, this.classesModificadas);
		System.out.println("Refactoring ? " + ip.evaluate(sourceLine, targetLine, propertiesObject));*/
		
		
		/*boolean isAssetMappingsEqual = this.isAssetMappingEqual(sourceSPL, targetSPL);
		System.out.println("\n AM changed: " + isAssetMappingsEqual);
		HashSet<String> changedFeatures = getChangedFeatureNames(targetSPL);
		this.productMatching.areAllProductsMatched(sourceSPL, targetSPL); 
		ImpactedClasses ic = new ImpactedClasses(wellFormedness, productBuilder, in, this.classesModificadas);
		System.out.println("Refactoring ? " + ic.evaluate(sourceSPL, targetSPL, changedFeatures));*/
		
		
		/*  It is responsible to check the SPL: Well-Formedness and Refinment.*/
		boolean isRefinement = this.checkLPS(sourceSPL, targetSPL, in);

		/*Report Variables: Pause total time to check the SPL.*/
		sOutcomes.getMeasures().getTempoTotal().pause();
		sOutcomes.getMeasures().print();

		return isRefinement;
	}
}
