package br.cin.ufpe.br.fileProperties;

import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Lines;
import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.saferefactor.core.Criteria;

public class FilePropertiesObject {
	
			private String evolutionDescription;
			private String sourceLineDirectory;
			private String targetLineDirectory;
			private String sourceLineLibDirectory;
			private String targetLineLibDirectory;
			private Lines line;
			private int timeOut;
			private int inputLimit;
			private Approach approach;
			private boolean aspectsInSourceSPL;
			private boolean aspectsInTargetSPL;
			private Criteria whichMethods;
			private CKFormat ckFormatSourceSPL;
			private CKFormat ckFormatTargetSPL;
			private AMFormat amFormatSourceSPL;
			private AMFormat amFormatTargetSPL;
			private String artifactsSourceDir;
			private String artifactsTargetDir;
			
			public FilePropertiesObject() {
				super();
			}
			
			/*...............................................>Getters and Setters*/
			public String getSourceLineDirectory() {
				return sourceLineDirectory;
			}
			public void setSourceLineDirectory(String sourceLineDirectory) {
				this.sourceLineDirectory = sourceLineDirectory;
			}
			public String getTargetLineDirectory() {
				return targetLineDirectory;
			}
			public void setTargetLineDirectory(String targetLineDirectory) {
				this.targetLineDirectory = targetLineDirectory;
			}
			public String getSourceLineLibDirectory() {
				return sourceLineLibDirectory;
			}
			public void setSourceLineLibDirectory(String sourceLineLibDirectory) {
				this.sourceLineLibDirectory = sourceLineLibDirectory;
			}
			public String getTargetLineLibDirectory() {
				return targetLineLibDirectory;
			}
			public void setTargetLineLibDirectory(String targetLineLibDirectory) {
				this.targetLineLibDirectory = targetLineLibDirectory;
			}
			public Lines getLine() {
				return line;
			}
			public void setLine(Lines line) {
				this.line = line;
			}
			public int getTimeOut() {
				return timeOut;
			}
			public void setTimeOut(int timeOut) {
				this.timeOut = timeOut;
			}
			public int getInputLimit() {
				return inputLimit;
			}
			public void setInputLimit(int inputLimit) {
				this.inputLimit = inputLimit;
			}
			public Approach getApproach() {
				return approach;
			}
			public void setApproach(Approach approach) {
				this.approach = approach;
			}
			public boolean isAspectsInSourceSPL() {
				return aspectsInSourceSPL;
			}
			public void setAspectsInSourceSPL(boolean aspectsInSourceSPL) {
				this.aspectsInSourceSPL = aspectsInSourceSPL;
			}
			public boolean isAspectsInTargetSPL() {
				return aspectsInTargetSPL;
			}
			public void setAspectsInTargetSPL(boolean aspectsInTargetSPL) {
				this.aspectsInTargetSPL = aspectsInTargetSPL;
			}
			public Criteria getWhichMethods() {
				return whichMethods;
			}
			public void setWhichMethods(Criteria whichMethods) {
				this.whichMethods = whichMethods;
			}
			public CKFormat getCkFormatSourceSPL() {
				return ckFormatSourceSPL;
			}
			public void setCkFormatSourceSPL(CKFormat ckFormatSourceSPL) {
				this.ckFormatSourceSPL = ckFormatSourceSPL;
			}
			public CKFormat getCkFormatTargetSPL() {
				return ckFormatTargetSPL;
			}
			public void setCkFormatTargetSPL(CKFormat ckFormatTargetSPL) {
				this.ckFormatTargetSPL = ckFormatTargetSPL;
			}
			public AMFormat getAmFormatSourceSPL() {
				return amFormatSourceSPL;
			}
			public void setAmFormatSourceSPL(AMFormat amFormatSourceSPL) {
				this.amFormatSourceSPL = amFormatSourceSPL;
			}
			public AMFormat getAmFormatTargetSPL() {
				return amFormatTargetSPL;
			}
			public void setAmFormatTargetSPL(AMFormat amFormatTargetSPL) {
				this.amFormatTargetSPL = amFormatTargetSPL;
			}
			public void setEvolutionDescription(String evolutionDescription) {
				this.evolutionDescription = evolutionDescription;
			}
			public String getEvolutionDescription() {
				return evolutionDescription;
			}
			public String getArtifactsSourceDir() {
				return artifactsSourceDir;
			}
			public void setArtifactsSourceDir(String artifactsSourceDir) {
				this.artifactsSourceDir = artifactsSourceDir;
			}

			public void setArtifactsTargetDir(String artifactsTargetDir) {
				this.artifactsTargetDir = artifactsTargetDir;
			}
			public String getArtifactsTargetDir() {
				return artifactsTargetDir;
			}
			
			/*............................................................toString*/
		
			/* (non-Javadoc)
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				
				return "evolutionDescription = " + evolutionDescription + "\n" +
				        "sourceLineDirectory = " + sourceLineDirectory + "\n" + 
				        "targetLineDirectory = " + targetLineDirectory + "\n" +
				   	    "sourceLineLibDirectory = " + sourceLineLibDirectory + "\n" +
						"targetLineLibDirectory = " + targetLineLibDirectory + "\n" +
						"line = " + line + "\n" +
						"timeOut = " + timeOut + "\n" +
						"inputLimit = " + inputLimit + "\n" +
						"approach  = " + approach + "\n" +
						"aspectsInSourceSPL = " + aspectsInSourceSPL + "\n" +
						"aspectsInTargetSPL = " + aspectsInTargetSPL + "\n" +
						"whichMethods = " + whichMethods + "\n" +
						"ckFormatSourceSPL = " + ckFormatSourceSPL + "\n" +
						"ckFormatTargetSPL = " + ckFormatTargetSPL + "\n" +
						"amFormatSourceSPL = " + amFormatSourceSPL + "\n" +
						"amFormatTargetSPL = " + amFormatTargetSPL + "\n" +
						"artifactsSourceDir = " + artifactsSourceDir + "\n" +
						"artifactsTargetDir = " + artifactsTargetDir + "\n";
			}

			
}
