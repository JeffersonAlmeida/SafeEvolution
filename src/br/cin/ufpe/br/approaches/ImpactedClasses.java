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
	private HashSet<String> listaAspectos;
	
	/** Hashset of dependencies to the compile the file. */
	private HashSet<String> dependenciasCopiadas;
	
	private HashMap<HashSet<String>, HashSet<String>> mapeamentoFeaturesAspectosSource;
	
	private HashMap<HashSet<String>, HashSet<String>> mapeamentoFeaturesAspectosTarget;
	
	private HashSet<String> constantsPreProcessor;
	
	public boolean evaluate(ProductLine sourceLine, ProductLine targetLine, FilePropertiesObject propertiesObject) throws AssetNotFoundException, IOException, DirectoryException{
		return false;
		
	}
	
	private boolean checkAssetMappingBehavior(ProductLine sourceLine, ProductLine targetLine,HashSet<String> changedFeatures, FilePropertiesObject in) throws IOException, AssetNotFoundException, DirectoryException {
		boolean sameBehavior = false;
		this.printListofModifiedClasses();
		
		long startedTime = System.currentTimeMillis();

		/* Set the amout of compiled products to 1*/
		SPLOutcomes.getInstance().getMeasures().setQuantidadeProdutosCompilados(1);

		String productPath = Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR + "Product0" + Constants.FILE_SEPARATOR;

		/* Creating folders for source and target products, which will be the same for all modified classes and their dependencies. */
		File testSourceDirectory = FileManager.getInstance().createDir( productPath + "source" + Constants.FILE_SEPARATOR + (in.getLine() == Lines.MOBILE_MEDIA ? ProductBuilder.SRCPREPROCESS : "src"));
		File testTargetDirectory = FileManager.getInstance().createDir( productPath + "target" + Constants.FILE_SEPARATOR + (in.getLine() == Lines.MOBILE_MEDIA ? ProductBuilder.SRCPREPROCESS : "src"));

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

		System.out.println("\n Amount of modified classes: " + modifiedClasses.size()+"\n");
		/* walk through all changed classes. */
		for (String classe : this.modifiedClasses) {
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
				
				if (in.getLine() == Lines.TARGET){
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
			if (product.containsSomeAsset(this.modifiedClasses, sourceLine.getMappingClassesSistemaDeArquivos())) {
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
				if (product.containsSomeAsset(this.modifiedClasses, sourceLine.getMappingClassesSistemaDeArquivos())) {
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
					if (!this.modifiedClasses.contains(aspecto) && !this.listaAspectos.contains(destinationPath)) {
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

					if (!this.modifiedClasses.contains(aspecto) && !this.listaAspectos.contains(aspecto)) {
						File fileDestination = new File(destinationPath);

						FileManager.getInstance().createDir(fileDestination.getParent());
						FileManager.getInstance().copyFile(targetLine.getMappingClassesSistemaDeArquivos().get( FileManager.getInstance().getCorrectName(aspecto)), fileDestination.getAbsolutePath());

						filesToTrash.add(fileDestination);
						filesToTrash.add(new File(fileDestination.getAbsolutePath().replaceFirst(ProductBuilder.SRCPREPROCESS, "src")));
						this.listaAspectos.add(fileDestination.getAbsolutePath());

						this.copyDependencies(fileDestination, testTargetDirectory, targetLine.getMappingClassesSistemaDeArquivos(), filesToTrash, targetLine.getDependencias());
					}
				}

				if (in.getLine() == Lines.TARGET || in.getLine() == Lines.DEFAULT) {
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
						String dir = new File(aspect.replaceFirst(ProductBuilder.SRCPREPROCESS, "src")).getParentFile()
								.getAbsolutePath();
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
		return sameBehavior;
	}

	private void printListofModifiedClasses() {
		Iterator<String> i = this.modifiedClasses.iterator();
		System.out.println("\nList of Modified Classes: ");
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
