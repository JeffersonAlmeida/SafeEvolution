package br.edu.ufcg.dsc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import soot.Main;
import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.am.ReadAM;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
import br.edu.ufcg.dsc.ck.HephaestusCKReader;
import br.edu.ufcg.dsc.ck.xml.XMLReader;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.FilesManager;


/**
 *  * @author Jefferson Almeida - jra at cin dot ufpe dot br

 *  This class represents a Software Product Line abstraction.
 */
public class ProductLine {
	
	 /** SPL path */
	private String path;
	
	/**SPL Configuration Knowledge Path*/
	private String ckPath;
	
	/** SPL Feature Model Path */
	private String fmPath;
	
	/**Asset Map SPL Path*/
	private String amPath;
	
	/**The flag used to inform whether the SPL has aspects.*/
	private boolean temAspectos;
	
	/**What does it means ? */
	private String controladoresFachadas;
	
	/**A HashSet to maintain the SPL feature names. (Strings)*/
	private HashSet<String> features;

	/***/
	private HashMap<String, Collection<String>> dependencias;
	
	/** Configuration Knowledge of the Product Line*/
	private ConfigurationKnowledge ck;
	
	/** A set of features that compose a product. */
	private HashSet<HashSet<String>> setsOfFeatures;
	
	/**ArrayList of Products*/
	private ArrayList<Product> products;
	
	/** Class names found in the folder mapped paths. */
	private HashMap<String, String> mappingClassesSistemaDeArquivos;
	
	/** Constants mapped to relative paths. */
	private HashMap<String, String> assetMapping;
	
	 /** CK Format of the SPL CK*/
	private CKFormat ckFormat;
	
	 /** AM Format of the SP AM*/
	private AMFormat amFormat;
	
	/***/
	private Properties preprocessProperties;
	
	/** The library path of the SPL.*/
	private String libPath;
	
	/** List of files to preProcess found in CK */
	private Set<String> filesToPreProcess;
	
	/**
	 * 
	 * @param path SPL path
	 * @param ckPath SPL Configuration Knowledge Path
	 * @param fmPath SPL Feature Model Path
	 * @param amPath Asset Map SPL Path
	 * @param temAspectos The flag used to inform whether the SPL has aspects.
	 * @param controladoresFachadas
	 * @param ckFormat
	 * @param amFormat
	 * @throws IOException
	 */
	public ProductLine(String path, String ckPath, String fmPath, String amPath, boolean temAspectos, String controladoresFachadas, CKFormat ckFormat, AMFormat amFormat) throws IOException {
		this.path = path;
		this.ckPath = ckPath;
		this.fmPath = fmPath;
		this.amPath = amPath;
		this.temAspectos = temAspectos;
		this.controladoresFachadas = controladoresFachadas;
		this.ckFormat = ckFormat;
		this.amFormat = amFormat;
		this.filesToPreProcess = new HashSet<String>();
	}
	
	/**
	 * This method loads the Asset Mapping depending on AM Format. <br></br>
	 * @throws IOException throws I/O Exception
	 * @see {@link AMFormat}
	 */
	private void loadAssetMapping() throws IOException {
		if(this.amFormat == AMFormat.HEPHAESTUS){
			this.assetMapping = ReadAM.readAM(this.amPath);
		}
		else if(this.amFormat == AMFormat.SIMPLE){
			this.assetMapping = FilesManager.getInstance().getAssets(this.amPath);
		}
	}

	private void loadPreprocessProperties() throws IOException {
		this.preprocessProperties = new Properties();
		
		File fileProperties = new File(this.path + Constants.FILE_SEPARATOR + "preprocess.properties");
		
		if(fileProperties.exists()){
			FileInputStream inputStream = new FileInputStream(fileProperties);
			
			this.preprocessProperties.load(inputStream);
			
			inputStream.close();
		}
	}

	public void loadDependencesCache() throws IOException, AssetNotFoundException {
		for(String asset : this.mappingClassesSistemaDeArquivos.values()){	
			File classFile = new File(asset);

			Collection<String> dependencias = null;

			if(asset.endsWith(".java")){
				dependencias = Main.v().getDependences(classFile.getName().replaceAll(".java", ""), classFile.getParent());

				dependencias.addAll(FilesManager.getInstance().getDependenciasAspectos(classFile));
			}
			else if(asset.endsWith(".aj")){
				dependencias = FilesManager.getInstance().getDependenciasDeAspectosPeloImport(classFile);
			}

			if(dependencias != null){
				this.dependencias.put(FilesManager.getInstance().getPathAPartirDoSrc(asset),dependencias);
			}
		}
	}
	
	public void setup() throws IOException, AssetNotFoundException {
		this.features = new HashSet<String>();
		this.dependencias = new HashMap<String, Collection<String>>();
		
		this.products = new ArrayList<Product>();
		
		this.mappingClassesSistemaDeArquivos = new HashMap<String, String>();
		
		String dir = this.getPath() + Constants.FILE_SEPARATOR + "src";
		System.out.println("Walk through this directory and get all files: " + dir);
		this.walkSrc(dir);
		
		this.loadPreprocessProperties();
		
		this.loadAssetMapping();
		
	}
	
	/**
	 * Walk through source product line and put the Java Files and AspectJ Files found in a HashMap.  <br></br>
	 * @param dir source product line.
	 */
	private void walkSrc(String dir) {
		File f = new File(dir);
		if(f.isDirectory()){
			File[] files = f.listFiles();
			for (File file : files) {
				/*Call the same method recursively*/
				this.walkSrc(file.getPath());
			}
		}else if (f.isFile()){
			if (f.getName().endsWith(".java") || f.getName().endsWith(".aj")) {
				String path = f.getPath();
				this.mappingClassesSistemaDeArquivos.put(FilesManager.getInstance().getCorrectName(path), path);
			}
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getCkPath() {
		return ckPath;
	}

	public void setCkPath(String ckPath) {
		this.ckPath = ckPath;
	}

	public String getFmPath() {
		return fmPath;
	}

	public void setFmPath(String fmPath) {
		this.fmPath = fmPath;
	}

	public boolean temAspectos() {
		return temAspectos;
	}

	public String getControladoresFachadas() {
		return controladoresFachadas;
	}

	public void setControladoresFachadas(String controladoresFachadas) {
		this.controladoresFachadas = controladoresFachadas;
	}

	public HashSet<String> getFeatures() {
		return features;
	}

	public void setFeatures(HashSet<String> features) {
		this.features = features;
	}

	public HashMap<String, Collection<String>> getDependencias() {
		return dependencias;
	}

	public void setDependencias(HashMap<String, Collection<String>> dependencias) {
		this.dependencias = dependencias;
	}

	/**
	 * Get the Configuration Knowledge of the SPL.
	 * @return returns a ConfigurationKnowledge
	 * @see ConfigurationKnowledge
	 */
	public ConfigurationKnowledge getCk() {
		if(this.ck == null){
			if(this.ckFormat == CKFormat.HEPHAESTUS){
				this.ck = HephaestusCKReader.readCK(this.getCkPath(), this.preprocessProperties, this);
			}
			else if(this.ckFormat == CKFormat.SIMPLE){
				this.ck = XMLReader.getInstance().getCK(this);
			}
		}
		return this.ck;
	}

	public void setCk(ConfigurationKnowledge ck) {
		this.ck = ck;
	}

	public void setSetsOfFeatures(HashSet<HashSet<String>> setsOfFeatures) {
		this.setsOfFeatures = setsOfFeatures;
	}
	
	public HashSet<HashSet<String>> getSetsOfFeatures(){
		return this.setsOfFeatures;
	}

	public ArrayList<Product> getProducts() {
		return products;
	}

	public HashMap<String, String> getMappingClassesSistemaDeArquivos() {
		return mappingClassesSistemaDeArquivos;
	}

	public Properties getPreprocessProperties() {
		return preprocessProperties;
	}

	public HashMap<String, String> getAssetMapping() {
		return assetMapping;
	}

	/**
	 * Set The library path of the SPL.
	 * @param libPath  library path of the SPL.
	 */
	public void setLibPath(String libPath) {
		this.libPath = libPath;
	}

	/**
	 * @return The library path of the SPL.
	 */
	public String getLibPath() {
		return libPath;
	}

	/**
	 *  Imprime as configuracoes validas = 'produtos'*/
	public void printSetOfFeatures() {
		String s = "";
		int c = 1;
		Iterator<HashSet<String>> i = this.setsOfFeatures.iterator();
		while(i.hasNext()){
			 HashSet<String> set = i.next();
			 Iterator<String> iterator = set.iterator();
			 s = "";
			 while(iterator.hasNext()){
				 s = s + "[ " + iterator.next() + " ] ";
			 }
			 System.out.println("\n"+ (c++) + " -> " + s);
		}
	}

	public void setPreprocessProperties(Properties preprocessProperties) {
		this.preprocessProperties = preprocessProperties;
	}
	
	/**
	 * Imprime as features individualmente.
	 */
	public void printFeatures(String plName){
		System.out.println("\n" + plName.toUpperCase() + " Features: ");
		Iterator<String> i = this.features.iterator();
		while(i.hasNext()){
			String s = i.next();
			System.out.println("\nFeature: " + s);
		}
	}
	
	public void printPreprocessProperties(String plName){
		Iterator<Object> i = this.preprocessProperties.keySet().iterator();
		while(i.hasNext()){
			String feature = (String) i.next();
			System.out.println("Feature: " + feature + " - " + "Files: " + this.preprocessProperties.getProperty(feature));
		}
	}
	
	public void printFilesListToPreProcess(){
		Iterator<String> i = this.filesToPreProcess.iterator();
		System.out.println("\nFile To PreProcess:\n");
		while(i.hasNext()){
			System.out.println("->  " + (String)i.next());
		}
	}

	public Set<String> getFilesToPreProcess() {
		return filesToPreProcess;
	}

	public void setFilesToPreProcess(Set<String> filesToPreProcess) {
		this.filesToPreProcess = filesToPreProcess;
	}
	
	
	
}
