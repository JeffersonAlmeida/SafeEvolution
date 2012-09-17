package br.edu.ufcg.dsc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import soot.Main;
import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.am.ReadAM;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.dsc.ck.ConfigurationKnowledge;
import br.edu.ufcg.dsc.ck.HephaestusCKReader;
import br.edu.ufcg.dsc.ck.xml.XMLReader;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.FilesManager;

public class ProductLine {
	private String path;
	private String ckPath;
	private String fmPath;
	private String amPath;
	
	private boolean temAspectos;
	
	private String controladoresFachadas;
	
	private HashSet<String> features;
	private HashMap<String, Collection<String>> dependencias;
	
	private ConfigurationKnowledge ck;
	private HashSet<HashSet<String>> setsOfFeatures;
	
	private ArrayList<Product> products;
	
	//Nomes de classes encontrados na pasta da linha mapeados em paths.
	private HashMap<String, String> mappingClassesSistemaDeArquivos;
	
	//Constantes mapeadas em paths relativos.
	private HashMap<String, String> assetMapping;
	
	private CKFormat ckFormat;
	private AMFormat amFormat;
	
	private Properties preprocessProperties;
	private String libPath;
	
	public ProductLine(String path, String ckPath, String fmPath, String amPath, 
			boolean temAspectos, String controladoresFachadas, CKFormat ckFormat, AMFormat amFormat) throws IOException {
		this.path = path;
		this.ckPath = ckPath;
		this.fmPath = fmPath;
		this.amPath = amPath;
		
		this.temAspectos = temAspectos;
		
		this.controladoresFachadas = controladoresFachadas;
		
		this.ckFormat = ckFormat;
		this.amFormat = amFormat;
	}
	
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
		
		this.walkSrc(this.getPath() + Constants.FILE_SEPARATOR + "src");
		
		this.loadPreprocessProperties();
		
		this.loadAssetMapping();
		
//		this.loadDependencesCache();
	}
	
	private void walkSrc(String dir) {
		System.out.println(dir);

		File f = new File(dir);
		for (File other : f.listFiles()) {
			if (other.isHidden()) {
				continue;
			}
			if (other.isDirectory()) {
				walkSrc(other.getPath());
			} else {
				if (other.getName().endsWith(".java") || other.getName().endsWith(".aj")) {
					String path = other.getPath();
					this.mappingClassesSistemaDeArquivos.put(FilesManager.getInstance().getCorrectName(path), path);
				}
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

	public void setLibPath(String libPath) {
		this.libPath = libPath;
	}

	public String getLibPath() {
		return libPath;
	}
}
