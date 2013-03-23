package br.cin.ufpe.br.fileProperties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Lines;
import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.saferefactor.core.Criteria;

public class FilePropertiesReader {
	
			private String filePropertiesDirectory;
			private Properties properties;
			private FilePropertiesObject propertiesObject;
			private boolean isLoaded;
			
			public FilePropertiesReader() {
				super();
			}
			
			public FilePropertiesReader(String filePropertiesDirectory) {
				super();
				this.isLoaded = false;
				this.propertiesObject = new FilePropertiesObject();
				this.filePropertiesDirectory = filePropertiesDirectory;
				this.properties = new Properties();
				this.createFileProperty();
				this.loadData();
			}
			
			private FilePropertiesObject loadData() {
				
				String evolutionDescription = this.properties.getProperty("evolutionDescription");
				this.propertiesObject.setEvolutionDescription(evolutionDescription);
				
				String sourceLineDirectory = this.properties.getProperty("sourceLineDirectory");
				this.propertiesObject.setSourceLineDirectory(sourceLineDirectory);
				
				String targetLineDirectory = this.properties.getProperty("targetLineDirectory");
				this.propertiesObject.setTargetLineDirectory(targetLineDirectory);
				
				String sourceLineLibDirectory = this.properties.getProperty("sourceLineLibDirectory");
				this.propertiesObject.setSourceLineLibDirectory(sourceLineLibDirectory);
				
				String targetLineLibDirectory = this.properties.getProperty("targetLineLibDirectory");
				this.propertiesObject.setTargetLineLibDirectory(targetLineLibDirectory);
				
				/* TaRGeT, MobileMeida, Default */
				Lines splType = findOutSplType(this.properties.getProperty("line"));
				this.propertiesObject.setLine(splType);
				
				String timeOut = this.properties.getProperty("timeout");
				this.propertiesObject.setTimeOut(Integer.parseInt(timeOut)); 
				
				String inputLimit = this.properties.getProperty("inputlimit");
				this.propertiesObject.setInputLimit(Integer.parseInt(inputLimit));
				
				/* APP, AP, IC, IP, EIC */
				Approach ap = findOutApproachType(this.properties.getProperty("approach")); 
				this.propertiesObject.setApproach(ap);
				
				String aspectsInSourceSPL = this.properties.getProperty("aspectsInSourceSPL");
				this.propertiesObject.setAspectsInSourceSPL(findOutTrueOrFalse(aspectsInSourceSPL));
				
				String aspectsInTargetSPL = this.properties.getProperty("aspectsInTargetSPL");
				this.propertiesObject.setAspectsInTargetSPL(findOutTrueOrFalse(aspectsInTargetSPL));
				
				String whichMethods = this.properties.getProperty("whichMethods");
				this.propertiesObject.setWhichMethods( findOutCriteria(whichMethods));
				
				String ckFormatSourceSPL = this.properties.getProperty("ckFormatSourceSPL");
				this.propertiesObject.setCkFormatSourceSPL(findOutCKFormat(ckFormatSourceSPL));
				
				String ckFormatTargetSPL = this.properties.getProperty("ckFormatTargetSPL");
				this.propertiesObject.setCkFormatTargetSPL(findOutCKFormat(ckFormatTargetSPL));
				
				String amFormatSourceSPL = this.properties.getProperty("amFormatSourceSPL");
				this.propertiesObject.setAmFormatSourceSPL(findOutAmFormat(amFormatSourceSPL));
				
				String amFormatTargetSPL = this.properties.getProperty("amFormatTargetSPL");
				this.propertiesObject.setAmFormatTargetSPL(findOutAmFormat(amFormatTargetSPL));
				
				String artifactsSourceDir = this.properties.getProperty("artifactsSourceDir");
				this.propertiesObject.setArtifactsSourceDir(artifactsSourceDir);
				
				String artifactsTargetDir = this.properties.getProperty("artifactsTargetDir");
				this.propertiesObject.setArtifactsTargetDir(artifactsTargetDir);
				
				this.isLoaded = true;
				return this.propertiesObject;
			}
		
			private AMFormat findOutAmFormat(String amFormatSourceSPL) {
				AMFormat  amFormat = AMFormat.SIMPLE;
				/* <simple> , <hephaestus>  */
				if (amFormatSourceSPL.trim().toLowerCase().equals("simple")){
					amFormat = AMFormat.SIMPLE;
				}else if (amFormatSourceSPL.trim().toLowerCase().equals("hephaestus")){
					amFormat = AMFormat.HEPHAESTUS;
				}
				return amFormat;
			}
		
			private CKFormat findOutCKFormat(String ckFormat) {
				/* <simple> , <hephaestus>  */
				CKFormat cKFORMAT = CKFormat.SIMPLE;
				if (ckFormat.trim().toLowerCase().equals("simple")){
					cKFORMAT = CKFormat.SIMPLE;
				}else if (ckFormat.trim().toLowerCase().equals("hephaestus")){
					cKFORMAT = CKFormat.HEPHAESTUS;
				}
				return cKFORMAT;
			}
		
			private Criteria findOutCriteria(String whichMethods) {
				/*  <commonMethods>, <allMethods> */
				Criteria criteria = Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT;
				if (whichMethods.trim().toLowerCase().equals("commonMethods")) {
					return Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT;
				}else if (whichMethods.trim().toLowerCase().equals("allMethods")){
					return Criteria.ALL_METHODS_IN_SOURCE_AND_TARGET;
				}
				return criteria;
			}
		
			private boolean findOutTrueOrFalse(String question) {
				boolean answer = false;
				if (question.trim().toLowerCase().equals("true")){
					answer = true;
				}else if (question.trim().toLowerCase().equals("false")){
					answer = false;
				}
				return answer;
			}
		
			private Approach findOutApproachType(String approach) {
				Approach app = Approach.IC;
				if (approach.trim().toLowerCase().equals("app")){
					app = Approach.APP;
				}else if (approach.trim().toLowerCase().equals("ap")){
					app = Approach.AP;
				}else if (approach.trim().toLowerCase().equals("ip")){
					app = Approach.IP;
				}else if (approach.trim().toLowerCase().equals("ic")){
					app = Approach.IC;
				}else if (approach.trim().toLowerCase().equals("eic")){
					app = Approach.EIC;
				}
				return app;
			}
		
			private Lines findOutSplType(String line) {
				Lines splType = Lines.DEFAULT;
				if(line.trim().toLowerCase().equals("target")){
					splType = Lines.TARGET;
				}else if (line.trim().toLowerCase().equals("mobilemedia")){
					splType = Lines.MOBILE_MEDIA;
				}else if (line.trim().toLowerCase().equals("default")){
					splType = Lines.DEFAULT;
				}
				return splType;
			}
		
			private void createFileProperty(){
				try {
					InputStream is = new FileInputStream(this.filePropertiesDirectory);
					this.properties.load(is);
					is.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
			/*.......................................................... GETTERS AND SETTERS */
			public void setFilePropertiesDirectory(String filePropertiesDirectory) {
				this.filePropertiesDirectory = filePropertiesDirectory;
			}
			public String getFilePropertiesDirectory() {
				return filePropertiesDirectory;
			} 
			public static void main(String [] args){
				System.out.println("File Properties");
				FilePropertiesReader propertiesReader = new FilePropertiesReader("/home/jefferson/workspace/ferramentaLPSSM/inputFiles/input.properties");
			    FilePropertiesObject propertiesObject = propertiesReader.getPropertiesObject();
				System.out.println(propertiesObject);
			}
			public void setPropertiesObject(FilePropertiesObject propertiesObject) {
				this.propertiesObject = propertiesObject;
			}
			public FilePropertiesObject getPropertiesObject() {
				if(this.isLoaded){
					return propertiesObject;	
				}else{
					return this.loadData();
				}
			}
			public boolean isLoaded() {
				return isLoaded;
			}
}
