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
			
			public FilePropertiesReader(String filePropertiesDirectory) {
				super();
				this.filePropertiesDirectory = filePropertiesDirectory;
				this.properties = new Properties();
				this.createFileProperty();
			}
			
			public FilePropertiesObject loadData() {
				
				FilePropertiesObject filePropertiesObject = new FilePropertiesObject();
				
				String sourceLineDirectory = this.properties.getProperty("sourceLineDirectory");
				filePropertiesObject.setSourceLineDirectory(sourceLineDirectory);
				
				String targetLineDirectory = this.properties.getProperty("targetLineDirectory");
				filePropertiesObject.setTargetLineDirectory(targetLineDirectory);
				
				String sourceLineLibDirectory = this.properties.getProperty("sourceLineLibDirectory");
				filePropertiesObject.setSourceLineLibDirectory(sourceLineLibDirectory);
				
				String targetLineLibDirectory = this.properties.getProperty("targetLineLibDirectory");
				filePropertiesObject.setTargetLineLibDirectory(targetLineLibDirectory);
				
				/* TaRGeT, MobileMeida, Default */
				Lines splType = findOutSplType(this.properties.getProperty("line"));
				filePropertiesObject.setLine(splType);
				
				String timeOut = this.properties.getProperty("timeout");
				filePropertiesObject.setTimeOut(Integer.parseInt(timeOut)); 
				
				String inputLimit = this.properties.getProperty("inputlimit");
				filePropertiesObject.setInputLimit(Integer.parseInt(inputLimit));
				
				/* APP, AP, IC, IP, EIC */
				Approach ap = findOutApproachType(this.properties.getProperty("approach")); 
				filePropertiesObject.setApproach(ap);
				
				String aspectsInSourceSPL = this.properties.getProperty("aspectsInSourceSPL");
				filePropertiesObject.setAspectsInSourceSPL(findOutTrueOrFalse(aspectsInSourceSPL));
				
				String aspectsInTargetSPL = this.properties.getProperty("aspectsInTargetSPL");
				filePropertiesObject.setAspectsInTargetSPL(findOutTrueOrFalse(aspectsInTargetSPL));
				
				String whichMethods = this.properties.getProperty("whichMethods");
				filePropertiesObject.setWhichMethods( findOutCriteria(whichMethods));
				
				String ckFormatSourceSPL = this.properties.getProperty("ckFormatSourceSPL");
				filePropertiesObject.setCkFormatSourceSPL(findOutCKFormat(ckFormatSourceSPL));
				
				String ckFormatTargetSPL = this.properties.getProperty("ckFormatTargetSPL");
				filePropertiesObject.setCkFormatTargetSPL(findOutCKFormat(ckFormatTargetSPL));
				
				String amFormatSourceSPL = this.properties.getProperty("amFormatSourceSPL");
				filePropertiesObject.setAmFormatSourceSPL(findOutAmFormat(amFormatSourceSPL));
				
				String amFormatTargetSPL = this.properties.getProperty("amFormatTargetSPL");
				filePropertiesObject.setAmFormatTargetSPL(findOutAmFormat(amFormatTargetSPL));
				
				return filePropertiesObject;
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
				if (approach.trim().toLowerCase().equals("APP")){
					app = Approach.APP;
				}else if (approach.trim().toLowerCase().equals("AP")){
					app = Approach.AP;
				}else if (approach.trim().toLowerCase().equals("IP")){
					app = Approach.IP;
				}else if (approach.trim().toLowerCase().equals("IC")){
					app = Approach.IC;
				}else if (approach.trim().toLowerCase().equals("EIC")){
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
				FilePropertiesObject propertiesObject = propertiesReader.loadData();
				System.out.println(propertiesObject);
			}
}
