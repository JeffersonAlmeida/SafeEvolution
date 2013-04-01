package br.cin.ufpe.br.approaches;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Pattern;
import soot.Main;
import br.cin.ufpe.br.fileProperties.FilePropertiesObject;
import br.cin.ufpe.br.wf.WellFormedness;
import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.Lines;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.builders.ProductBuilder;
import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
import br.edu.ufcg.dsc.evaluation.SPLOutcomes;
import br.edu.ufcg.dsc.saferefactor.CommandLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FileManager;

public class ImpactedClasses {
	
	private WellFormedness wellFormedness;
	private ProductBuilder productBuilder;
	
	/** A string collection of changed classes.*/
	private Collection<String> modifiedClasses;
	
	/** Aspects List*/
	private HashSet<String> aspectList;
	
	/** Hashset of dependencies to the compile the file. */
	private HashSet<String> dependencies;
	
	private HashMap<HashSet<String>, HashSet<String>> mapeamentoFeaturesAspectosSource;
	
	private HashMap<HashSet<String>, HashSet<String>> mapeamentoFeaturesAspectosTarget;
	
	private HashSet<String> constantsPreProcessor;
	
	private String uniqueProductPath;
	private File sourceProductFile;
	private File targetProductFile;
	private FilePropertiesObject input;
	private String classes;
	private String classeToGenerateTestes;
	private ArrayList<Product> productsThatHaveModifiedClasses;
	
	public ImpactedClasses(WellFormedness wellFormedness, ProductBuilder productBuilder, FilePropertiesObject in) {
		super();
		this.wellFormedness = wellFormedness;
		this.productBuilder = productBuilder;
		this.input = in;
		this.dependencies = new HashSet<String>();
		this.aspectList = new HashSet<String>();
		this.productsThatHaveModifiedClasses = new ArrayList<Product>();
	}

	public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws AssetNotFoundException, IOException, DirectoryException{
		return false;
		
	}
	
	private void createProductFolderStructure(ProductLine sourceSPL, ProductLine targetSPL){
		this.uniqueProductPath = Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR + "Product0" + Constants.FILE_SEPARATOR; // This creates product0 directory
		this.sourceProductFile = FileManager.getInstance().createDir( this.uniqueProductPath + "source" + Constants.FILE_SEPARATOR + (input.getLine() == Lines.MOBILE_MEDIA ? ProductBuilder.SRCPREPROCESS : "src")); 
		this.targetProductFile = FileManager.getInstance().createDir( this.uniqueProductPath + "target" + Constants.FILE_SEPARATOR + (input.getLine() == Lines.MOBILE_MEDIA ? ProductBuilder.SRCPREPROCESS : "src"));
		FileManager.getInstance().createDir(this.uniqueProductPath + "source" + Constants.FILE_SEPARATOR + "bin"); // This creates bin folder for SOURCE PRODUCT
		FileManager.getInstance().createDir(this.uniqueProductPath + "target" + Constants.FILE_SEPARATOR + "bin"); // This creates bin folder for TARGET PRODUCT.
	}
	
	/**
	 * Copy modified class and its dependencies to the SPL product folder.
	 */
	private void processModifiedClass(String clazz, ProductLine SPL, File splFileDirectory) throws AssetNotFoundException, DirectoryException {
		System.out.println(" - Modified Class: " + clazz);
		String modifiedClassDirectory = SPL.getMappingClassesSistemaDeArquivos().get(clazz); // Get the whole path of the modified class in SOURCE spl file system
		if (!(modifiedClassDirectory.isEmpty())){
			String destinationDirectory = splFileDirectory.getAbsolutePath() + FileManager.getInstance().getPathAPartirDoSrc(modifiedClassDirectory).replaceFirst("src", "");
			File destinationFile = new File(destinationDirectory);
			FileManager.getInstance().createDir(destinationFile.getParent());
			FileManager.getInstance().copyFile(modifiedClassDirectory, destinationFile.getAbsolutePath());
			this.copyDependencies(destinationFile, splFileDirectory, SPL.getMappingClassesSistemaDeArquivos(), null, SPL.getDependencias());
			isThisClazzFileAspect(destinationFile); // if the answer is YES, add this aspect file to the aspectList
			setClassToGenerateTests();
		}
	}

	private void setClassToGenerateTests() {
		if (this.input.getLine() == Lines.TARGET){
			classeToGenerateTestes = FileManager.getInstance().getPathAPartirDoSrc(classeToGenerateTestes).replaceFirst(Pattern.quote("src.java."), "");
		}
		if (classes.equals("")){
			classes = classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
		} else if (!classes.contains(classeToGenerateTestes.replaceAll(".java", ""))) {
			classes = classes + "|" + classeToGenerateTestes.replaceAll(".java", "").replaceAll(".aj", "");
		}
	}

	private void isThisClazzFileAspect(File destinationFile) {
		if (destinationFile.getAbsolutePath().endsWith("aj")) { 
			this.aspectList.add(destinationFile.getAbsolutePath()); 	// As aspects are not preprocessed, it will be necessary to manually move the aspects to the src folder after the preprocessing.
		}
	}
	
	private boolean checkAssetMappingBehavior(ProductLine sourceSPL, ProductLine targetSPL, HashSet<String> changedFeatures, FilePropertiesObject in) throws IOException, AssetNotFoundException, DirectoryException {
		boolean sameBehavior = false;
		this.printListofModifiedClasses();
		long startedTime = System.currentTimeMillis();
		SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(1); // Set the amout of compiled products to 1
		createProductFolderStructure(sourceSPL, targetSPL);
		copyLibrariesFromSplToProductFolder(sourceSPL, targetSPL, this.uniqueProductPath);

		for (String clazz : this.modifiedClasses) { 
			this.processModifiedClass(clazz,sourceSPL, this.sourceProductFile);
			this.processModifiedClass(clazz,targetSPL, this.targetProductFile);
		}
		checkModifiedProducts(sourceSPL); // heck products that have any of the modified classes. 

		//Se tiver aspectos ou tags de pre-processamento no codigo, faz um for com todos os produtos possiveis.
		//Filtrar entre os produtos poss�veis, removendo os que tiverem mesmos conjuntos de aspectos que interferem

		//		**Eh melhor entrar aqui se temAspectos, mesmo que nao tenha preprocess
		//		**Quando tiver soh preprocess entra no else
		//		**
		
		if (sourceSPL.temAspectos()) {  // Does SOURCE product line contains aspectos ? 
			HashSet<HashSet<String>> products = new HashSet<HashSet<String>>();
			productsThatHaveModifiedClasses.clear(); // Removes all of the elements from this list. 
			for (Product product : sourceSPL.getProducts()) {
				/* Is this product contains at least one modified asset ? */
				if (product.containsSomeAsset(this.modifiedClasses, sourceSPL.getMappingClassesSistemaDeArquivos())) {
					productsThatHaveModifiedClasses.add(product);
					products.add(product.getFeaturesList());
				}
			}

			/* if it has aspects, but there are no classes with more than one version in the SPL. */
			HashSet<HashSet<String>> pseudoProductsToBePreprocessed = new HashSet<HashSet<String>>();

			this.mapeamentoFeaturesAspectosSource = new HashMap<HashSet<String>, HashSet<String>>();
			this.mapeamentoFeaturesAspectosTarget = new HashMap<HashSet<String>, HashSet<String>>();

			for (HashSet<String> product : products) {
				HashSet<String> prodToBuild = this.makeCombination(product, sourceSPL, targetSPL);
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
					String destinationPath = this.sourceProductFile.getAbsolutePath() + aspecto.split("src")[1];
					if (!this.modifiedClasses.contains(aspecto) && !this.aspectList.contains(destinationPath)) {
						File fileDestination = new File(destinationPath);
						FileManager.getInstance().createDir(fileDestination.getParent());
						FileManager.getInstance().copyFile(sourceSPL.getMappingClassesSistemaDeArquivos().get(FileManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());
						filesToTrash.add(fileDestination);
						filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
						this.aspectList.add(fileDestination.getAbsolutePath());
						this.copyDependencies(fileDestination, this.sourceProductFile, sourceSPL.getMappingClassesSistemaDeArquivos(), filesToTrash, sourceSPL.getDependencias());
					}
				}

				for (String aspecto : aspectosDaConfiguracaoTarget) {
					String destinationPath = this.targetProductFile.getAbsolutePath() + aspecto.split("src")[1];

					if (!this.modifiedClasses.contains(aspecto) && !this.aspectList.contains(aspecto)) {
						File fileDestination = new File(destinationPath);

						FileManager.getInstance().createDir(fileDestination.getParent());
						FileManager.getInstance().copyFile(targetSPL.getMappingClassesSistemaDeArquivos().get( FileManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());

						filesToTrash.add(fileDestination);
						filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
						this.aspectList.add(fileDestination.getAbsolutePath());

						this.copyDependencies(fileDestination, this.targetProductFile, targetSPL.getMappingClassesSistemaDeArquivos(), filesToTrash, targetSPL.getDependencias());
					}
				}

				if (in.getLine() == Lines.TARGET || in.getLine() == Lines.DEFAULT) {
					//Para a TaRGeT, os arquivos sao pre processados com o Velocity dentro da propria
					//pasta, sem copias. Com isso, eh necessario renomear a pasta para src manualmente.

					if (prod.size() > 0/* && !(prod.size() == 1 && prod.iterator().next().contains("fake"))*/) {
						this.productBuilder.preprocessVelocity(prod, this.sourceProductFile, sourceSPL, this.sourceProductFile.getParent());
						this.productBuilder.preprocessVelocity(prod, this.targetProductFile, targetSPL, this.targetProductFile.getParent());
					}
					/*Rename srcpreprocess folder to src*/
					/*testSourceDirectory.renameTo(new File(testSourceDirectory.getParent()+ System.getProperty("file.separator") + "src"));
					testTargetDirectory.renameTo(new File(testTargetDirectory.getParent()+ System.getProperty("file.separator") + "src"));*/
					
				} else {
					//Para o MobileMedia, pre processa com Antenna, que copia o codigo da pasta
					//srcprecess para src.
					this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), this.sourceProductFile.getParent());

					this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), this.targetProductFile.getParent());

					System.out.println("########################################################" + prod);

					//Como o Antenna nao pre processa aspectos, eles tambem nao sao copiados para 
					//a pasta src. Eh necessario copia-los manualmente.
					for (String aspect : this.aspectList) {
						String dir = new File(aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src")).getParentFile()
								.getAbsolutePath();
						FileManager.getInstance().createDir(dir);
						FileManager.getInstance().copyFile(aspect, aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src"));
					}
				}

				System.out.println("\nClasses que receberao testes JUNIT: " + classes);
				// int sourceProductId, int targetProductId, String sourceProductPath, String targetProductPath, String classes, FilePropertiesObject propertiesObject, boolean sourceIsCompiled, boolean targetIsCompiled
				sameBehavior = sameBehavior && CommandLine.isRefactoring(0, 0, this.sourceProductFile.getParent(), this.targetProductFile.getParent(), classes, in , false, false);
				for (File file : filesToTrash) {
					file.delete();
					this.aspectList.remove(file.getAbsolutePath());
					this.dependencies.remove(file.getAbsolutePath());
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
			this.getPreProcessorConstantes(this.sourceProductFile);

			if (this.constantsPreProcessor.size() > 0) {
				HashSet<HashSet<String>> products = sourceSPL.getSetsOfFeatures();

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
					this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), this.sourceProductFile.getParent());

					this.productBuilder.preprocess(this.productBuilder.getSymbols(prod), this.targetProductFile.getParent());

					sameBehavior = sameBehavior	&& CommandLine.isRefactoring(0, 0, this.sourceProductFile.getParent(), this.targetProductFile.getParent(), classes, in , false, false);

					System.out.println("###########################" + sameBehavior);
				}
			} else {
				//Se nem tem aspectos nem preprocessamento.
				this.sourceProductFile.renameTo(new File(this.sourceProductFile.getParent() + Constants.FILE_SEPARATOR + "src"));
				this.targetProductFile.renameTo(new File(this.targetProductFile.getParent() + Constants.FILE_SEPARATOR + "src"));
				sameBehavior = CommandLine.isRefactoring(0, 0, this.sourceProductFile.getParent(), this.targetProductFile.getParent(), classes, in , false, false);
				System.out.println("###########################" + sameBehavior);
			}
		}

		long finishedTime = System.currentTimeMillis();
		System.out.println("Asset mapping verificado em: " + String.valueOf((finishedTime - startedTime) / 1000) + " segundos.");
		return sameBehavior;
	}

	/**
	 *  Check which products have any of the modified classes. 
	 *	Products that have at least one modified asset.   
	 */
	 private void checkModifiedProducts(ProductLine sourceSPL) {
		for (Product product : sourceSPL.getProducts()){
			if (product.containsSomeAsset(this.modifiedClasses, sourceSPL.getMappingClassesSistemaDeArquivos())) {
				productsThatHaveModifiedClasses.add(product);  // if the answer is YES, add this product to the modified products variable.
			}
		}
	 }

	

	/**
	 * This copies all library files for SOURCE PRODUCT and TARGET PRODUCT lib path. 
	 *  */
	private void copyLibrariesFromSplToProductFolder(ProductLine sourceLine, ProductLine targetLine, String productPath) throws AssetNotFoundException, DirectoryException {
		String sourceLibDirectory = sourceLine.getLibPath(); 
		String targetLibDirectory = targetLine.getLibPath();
		if (!(sourceLibDirectory.isEmpty()) && !(targetLibDirectory.isEmpty())) {
			FileManager.getInstance().copyLibs(sourceLibDirectory, productPath + "source" + Constants.FILE_SEPARATOR + "lib");
			FileManager.getInstance().copyLibs(targetLibDirectory, productPath + "target" + Constants.FILE_SEPARATOR + "lib");
		}
	}

	private void printListofModifiedClasses() {
		Iterator<String> i = this.modifiedClasses.iterator();
		System.out.println("\nList of Modified Classes: " + this.modifiedClasses.size());
		while(i.hasNext()){
			System.out.println(i.next());
		}
		System.out.println("\n--------------------------");
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

					if (this.dependencies.add(fileDestination.getAbsolutePath())) {
						FileManager.getInstance().createDir(fileDestination.getParent());

						if (!fileDestination.exists()) {
							FileManager.getInstance().copyFile(file.getAbsolutePath(), fileDestination.getAbsolutePath());

							isThisClazzFileAspect(fileDestination);

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
	
	private HashSet<String> getAspectosQueInterferemNaClasseModificada(ArrayList<String> aspectosDoProduto, HashMap<String, String> mapping) {

		HashSet<String> aspectosQueInterferemNaClasseModificada = new HashSet<String>();

		for (String aspecto : aspectosDoProduto) {
			//As classes em que um aspecto interferem sao informadas na primeira
			//linha do arquivo aj.

			String path = mapping.get(FileManager.getInstance().getCorrectName(this.replaceBarrasPorSeparator(aspecto)));

			Collection<String> classesEmQueOAspectoInterfe = FileManager.getInstance().getDependenciasAspectos(new File(path));

			for (String classeModificada : this.modifiedClasses) {

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
	
	/*..........................................................  Getters and Setters */
	public void setProductBuilder(ProductBuilder productBuilder) {
		this.productBuilder = productBuilder;
	}
	public ProductBuilder getProductBuilder() {
		return productBuilder;
	}
	public void setWellFormedness(WellFormedness wellFormedness) {
		this.wellFormedness = wellFormedness;
	}
	public WellFormedness getWellFormedness() {
		return wellFormedness;
	}
}
