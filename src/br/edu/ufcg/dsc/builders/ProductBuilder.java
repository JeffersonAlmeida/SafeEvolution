package br.edu.ufcg.dsc.builders;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.Product;
import br.edu.ufcg.dsc.ProductLine;
import br.edu.ufcg.dsc.evaluation.ResultadoLPS;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.Comparador;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.FilesManager;

public abstract class ProductBuilder {
	public static final String FILES_DIR = Constants.PRODUCTS_DIR;
	public static final String LABUILD = "labBuild.properties";

	protected String defaultTargetPath;
	protected String defaultSourcePath;
	protected ArrayList<Product> prods;
	protected FilesManager filesManager;

	public static final String SRCPREPROCESS = "srcpreprocess";

	protected HashMap<String, String> preprocessFeaturesToConstants;

	protected HashMap<String, String> preprocessConstantsToFeatures;

	public ProductBuilder() {
		this.filesManager = FilesManager.getInstance();
	}

	/**
	 * This method is implemented by the special classes. Each SPL type generates product in a different way.
	 * 
	 * @param product
	 * @param pathSPL
	 * @param resultado
	 * @throws AssetNotFoundException
	 * @throws IOException
	 * @throws DirectoryException
	 */
	public abstract void generateProduct(Product product, String pathSPL, ResultadoLPS resultado) throws AssetNotFoundException, IOException, DirectoryException;

	public void preprocessVelocity(HashSet<String> features, File file, ProductLine productLine, String productPath, ResultadoLPS resultado) throws IOException {
		ArrayList<String> filesToPreprocess = new ArrayList<String>();
		this.getFilesInDirectory(file, filesToPreprocess);
		this.preprocessVelocity(features, filesToPreprocess, productLine, productPath, resultado);
	}

	private void getFilesInDirectory(File file, ArrayList<String> filesToPreprocess) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();

			for (File subFile : files) {
				this.getFilesInDirectory(subFile, filesToPreprocess);
			}
		} else {
			if (file.getAbsolutePath().endsWith("xml") || file.getAbsolutePath().endsWith("java") || file.getAbsolutePath().endsWith("aj")) {
				filesToPreprocess.add(file.getAbsolutePath());
			}
		}
	}

	public void preprocessVelocity(HashSet<String> features, ArrayList<String> filesToPreprocess, ProductLine productLine,
			String productPath, ResultadoLPS resultado) throws IOException {
		String fileConstants = this.createConstansFile(features);
		String filesToPreprocessArgument = this.createFilesToPreprocessArgument(filesToPreprocess, productLine, productPath);

		File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.xml");
		Project p = new Project();

		p.setProperty("contantsfile", fileConstants);

		p.setProperty("fileList", filesToPreprocessArgument);

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);

		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		helper.parse(p, buildFile);

		//O pre-processamento contarah como tempo de compilacao tambem.
		resultado.getMeasures().getTempoCompilacaoProdutos().startContinue();
		p.executeTarget("velocity");
		resultado.getMeasures().getTempoCompilacaoProdutos().pause();
	}

	private String createFilesToPreprocessArgument(ArrayList<String> filesToPreprocess, ProductLine productLine, String productPath)
			throws IOException {
		File arq = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "ant" + Constants.FILE_SEPARATOR
				+ "files.properties");

		if (arq.exists()) {
			arq.delete();
		}

		FileWriter fileWriter = new FileWriter(arq);

		Properties fileProperties = productLine.getPreprocessProperties();

		if (!fileProperties.keySet().isEmpty()) {
			String classesToPreprocessProperty = fileProperties.getProperty("preprocess");

			if (classesToPreprocessProperty != null) {
				this.writeClassesToPrecess(productPath, fileWriter, classesToPreprocessProperty, filesToPreprocess);

				int n = 2;

				classesToPreprocessProperty = fileProperties.getProperty("preprocess" + n++);

				while (classesToPreprocessProperty != null) {
					this.writeClassesToPrecess(productPath, fileWriter, classesToPreprocessProperty, filesToPreprocess);

					classesToPreprocessProperty = fileProperties.getProperty("preprocess" + n++);
				}
			}
		} else {
			//Descomentar esse c�digo se quiser que volte a pr�-processar todas as classes na aus�ncia do arquivo preprocess.properties.
//			for (String file : filesToPreprocess) {
//				if (file.charAt(2) == ':') {
//					file = file.substring(1);
//
//					String[] parts = file.split(Pattern.quote("/"));
//
//					file = "";
//
//					for (String part : parts) {
//						file = file + "\\" + part;
//					}
//
//					file = file.substring(1);
//				}
//
//				fileWriter.write(file + "\n");
//			}
		}

		fileWriter.close();

		return arq.getAbsolutePath();
	}

	private void writeClassesToPrecess(String productPath, FileWriter fileWriter, String classesToPreprocessProperty,
			ArrayList<String> filesToPreprocess) throws IOException {
		String[] classes = classesToPreprocessProperty.split(Pattern.quote(","));

		for (String classe : classes) {
			if (this.productContainsClass(filesToPreprocess, classe)) {
				String file = productPath + "/src/" + classe;

				if (file.charAt(2) == ':') {
					file = file.substring(1);

					String[] parts = file.split(Pattern.quote("/"));

					file = "";

					for (String part : parts) {
						file = file + "\\" + part;
					}

					file = file.substring(1);
				}

				fileWriter.write(file + "\n");
			}
		}
	}

	private boolean productContainsClass(ArrayList<String> filesToPreprocess, String classe) {
		boolean result = false;

		for (String file : filesToPreprocess) {
			if (file.contains(classe)) {
				result = true;

				break;
			}
		}

		return result;
	}

	private String createConstansFile(HashSet<String> features) {
		File file = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "ant" + Constants.FILE_SEPARATOR
				+ "constants.properties");

		if (file.exists()) {
			file.delete();
		}

		try {
			FileWriter fileWriter = new FileWriter(file);

			for (String feature : features) {
				fileWriter.write(feature + "\n");
			}

			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return file.getAbsolutePath();
	}

	public HashMap<String, String> getPreprocessConstantsToFeatures() {
		return preprocessConstantsToFeatures;
	}

	public HashMap<String, String> getPreprocessFeaturesToConstants() {
		return preprocessFeaturesToConstants;
	}

	public String getSymbols(HashSet<String> features) {
		String result = "";

		for (String feature : features) {
			if (!result.equals("")) {
				result = result + "," + this.preprocessFeaturesToConstants.get(feature);
			} else {
				result = result + this.preprocessFeaturesToConstants.get(feature);
			}
		}

		return result;
	}

	public String generateStringPreProcessTags(HashSet<String> preProcessTags) {
		String result = "";

		for (String preProcessTag : preProcessTags) {
			if (!result.equals("")) {
				result = result + "," + preProcessTag;
			} else {
				result = result + preProcessTag;
			}
		}

		return result;
	}

	public HashSet<String> getPreProcessTags(HashSet<String> features) {
		HashSet<String> result = new HashSet<String>();

		for (String feature : features) {
			String preProcessTag = this.preprocessFeaturesToConstants.get(feature);

			if (preProcessTag != null) {
				result.add(preProcessTag);
			}
		}

		return result;
	}

	public void preprocess(String symbols, String srcDir, ResultadoLPS resultado) {
		File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.xml");
		Project p = new Project();

		p.setProperty("srcpreprocess", srcDir + Constants.FILE_SEPARATOR + SRCPREPROCESS);

		p.setProperty("src", srcDir + Constants.FILE_SEPARATOR + "src");

		p.setProperty("symbols", symbols);

		p.setProperty("pluginpath", br.edu.ufcg.dsc.Constants.PLUGIN_PATH);

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);

		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		helper.parse(p, buildFile);

		//O pre-processamento contarah como tempo de compilacao tambem.
		resultado.getMeasures().getTempoCompilacaoProdutos().startContinue();
		p.executeTarget("preprocess");
		resultado.getMeasures().getTempoCompilacaoProdutos().pause();
	}

	public HashSet<HashSet<String>> filter(HashSet<HashSet<String>> products, HashSet<String> changedFeatures) {
		HashSet<HashSet<String>> output = new HashSet<HashSet<String>>();

		for (HashSet<String> hashSet : products) {
			if (Comparador.containsSome(hashSet, changedFeatures)) {
				output.add(hashSet);
			}
		}

		return output;
	}

	public HashSet<HashSet<String>> getProductsFromAlloy(String pathToSourceAlloyFM) {

		ProductGenerator generator = new ProductGenerator();
		generator.generateProductsInstance(pathToSourceAlloyFM);

		HashSet<HashSet<String>> products = generator.getProducts();

		return products;

	}

	protected void createDirs(Product product, ArrayList<String> assets, String splPath) throws AssetNotFoundException {

		String temp = "Product" + product.getId();
		String productPath = FILES_DIR + Constants.FILE_SEPARATOR + temp;

		this.filesManager.createDir(FILES_DIR);
		this.filesManager.createDir(productPath);
		this.filesManager.createDir(productPath + Constants.FILE_SEPARATOR + "bin");

		this.filesManager.verifyDirectoriesStructure(assets, productPath);

		product.setPath(productPath);
	}


	public ArrayList<Product> getProds() {
		return prods;
	}

	public FilesManager getFilesManager() {
		return filesManager;
	}
}
