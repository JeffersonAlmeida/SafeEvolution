package br.edu.ufcg.dsc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.coverage.All;
import br.edu.ufcg.dsc.coverage.Coverage;
import br.edu.ufcg.dsc.coverage.Package;
import br.edu.ufcg.dsc.coverage.Report;
import br.edu.ufcg.dsc.coverage.Srcfile;

public class RGenerator {
	
	private static final String LINE = "line";
	
	private static final String BLOCK = "block";

	private static String COMPLETO = "completo";
	
	private static String CLASSES_MODIFICADAS = "classes_modificadas";

	private String path;
	
	private String pathCobertura;
	
	private HashMap<String,Properties> todosArquivosExecucoes;
	
	private HashMap<String, String> legendas;

	private FileWriter file;

	private Vector<String> keysOrdendadasDeAcordoComALegenda;

	private HashMap<String,Collection<String>> cacheClassesModificadas;

	public RGenerator(String pathExecucao, String pathCobertura, HashMap<String, String> legendas) throws IOException {
		this.path = pathExecucao;
		this.pathCobertura = pathCobertura;
		
		this.todosArquivosExecucoes = new HashMap<String,Properties>();
		this.cacheClassesModificadas = new HashMap<String, Collection<String>>();
		
		this.legendas = legendas;
		
		this.keysOrdendadasDeAcordoComALegenda = this.orderByValues(legendas);
		
		String nameR = Constants.PLUGIN_PATH + "Resultados" + Constants.FILE_SEPARATOR + 
		"R" + Constants.FILE_SEPARATOR + 
		"Script" + System.currentTimeMillis() + ".txt";

		this.file = new FileWriter(nameR);
	}

	/**
	 * 	Para gerar os gr�ficos de linhas, a quantidade de mudan�as precisa estar entre
	 * 	par�nteses na legenda.
	
	 * @param generateLines
	 * @param generateCoverage
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void generate(boolean generateLines, boolean generateCoverage) throws FileNotFoundException, IOException {
		this.generateTimeGraphics(generateLines);
		
		if(generateCoverage){
			this.generateScriptCobertura(LINE, COMPLETO, "COBERTURA (TODO O CODIGO)", generateLines);
			this.generateScriptCobertura(LINE, CLASSES_MODIFICADAS, "COBERTURA (CLASSES MODIFICADAS)", generateLines);
		}
		
		this.file.close();
	}

	private void generateTimeGraphics(boolean generateLines)
			throws IOException, FileNotFoundException {
		File directory = new File(this.path);
		
		File[] propertiesFiles = directory.listFiles();
		
		for(File file : propertiesFiles){
			if(!file.isHidden()){
				Properties properties = new Properties();
				properties.load(new FileInputStream(file));

				this.todosArquivosExecucoes.put(file.getName(), properties);
			}
		}
		
		this.generateScript(ResultData.TEMPO_TOTAL,"TEMPO TOTAL (MEDIA)","Tempo (ms)", generateLines);
		this.generateScript(ResultData.TEMPO_COMPILACAO_PRODUTOS,"TEMPO COMPILACAO PRODUTOS (MEDIA)","Tempo (ms)", generateLines);
		this.generateScript(ResultData.TEMPO_COMPILACAO_TESTES,"TEMPO COMPILACAO TESTES (MEDIA)","Tempo (ms)", generateLines);
		this.generateScript(ResultData.TEMPO_EXECUCAO_TESTES,"TEMPO EXECUCAO TESTES (MEDIA)","Tempo (ms)", generateLines);
		this.generateScript(ResultData.QUANTIDADE_PRODUTOS_COMPILADOS,"QUANTIDADE DE EXECUCOES DO SAFEREFACTOR (MEDIA)","N", generateLines);
		
		this.generateScript("QUANTIDADE DE TESTES GERADOS (MEDIA)","N", generateLines);
	}
	
	private void generateScriptCobertura(String linhaOuBloco, String completoOuClassesModificadas, String title, boolean generateLines) throws IOException {
		HashMap<String,ArrayList<String>> naivePercentuais = new HashMap<String, ArrayList<String>>();
		HashMap<String,ArrayList<String>> onlyChangedClassesPercentuais = new HashMap<String, ArrayList<String>>();
		HashMap<String,ArrayList<String>> impactedFeaturesPercentuais = new HashMap<String, ArrayList<String>>();
		
		for(String sourceTarget : this.keysOrdendadasDeAcordoComALegenda){
			ArrayList<String> valoresNaive = new ArrayList<String>();
			ArrayList<String> valoresOnlyChangedClassesTimes = new ArrayList<String>();
			ArrayList<String> valoresImpactedFeaturesTimes = new ArrayList<String>();
			
			this.extractCoverageData(completoOuClassesModificadas, sourceTarget, valoresNaive, valoresOnlyChangedClassesTimes, valoresImpactedFeaturesTimes, linhaOuBloco);
			
			naivePercentuais.put(sourceTarget, valoresNaive);
			onlyChangedClassesPercentuais.put(sourceTarget, valoresOnlyChangedClassesTimes);
			impactedFeaturesPercentuais.put(sourceTarget, valoresImpactedFeaturesTimes);
		}
		
		this.generateR(naivePercentuais, onlyChangedClassesPercentuais, impactedFeaturesPercentuais, title, "PERCENTUAL (%)", generateLines);
	}

	private void generateR(HashMap<String, ArrayList<String>> naiveTimes, 
			HashMap<String, ArrayList<String>> onlyChangedClassesTimes, 
			HashMap<String, ArrayList<String>> impactedFeaturesTimes, 
			String title, String ylabel, boolean generateLines) throws IOException {
		
		this.file.write("safe_refactor <- matrix(c(" + this.generateConjuntoMean(naiveTimes) + "," + this.generateConjuntoMean(onlyChangedClassesTimes) + "," + 
				this.generateConjuntoMean(impactedFeaturesTimes) + "),ncol=" + this.legendas.size() + 
				",byrow=TRUE)\n");
		
		this.file.write("colnames(safe_refactor) <- c(" + this.getLegendas() + ")\n" + 
				"rownames(safe_refactor) <- c(\"1\",\"2\",\"3\")\n" + 
				"safe_refactor <- as.table(safe_refactor)\n");

		this.file.write("barplot(as.matrix(safe_refactor), main=\"" + title + "\", ylab= \"" + ylabel + "\", beside=TRUE, col=rainbow(3))\n" +
				"legend(\"topright\", c(\"Naive\",\"Only Changed Classes\",\"Impacted Features\"), cex=0.9, bty=\"n\", fill=rainbow(3))\n\n");
		
		//As legendas devem ser �nicas para que o gragico de linhas saia correto.
		if(generateLines){
			this.generateLinesGraphic(title, naiveTimes,onlyChangedClassesTimes,impactedFeaturesTimes);
		}
		
		this.generateHistogramas(naiveTimes,title + " " + Approach.APP, ylabel);
		this.generateHistogramas(onlyChangedClassesTimes,title + " " + Approach.IC, ylabel);
		this.generateHistogramas(impactedFeaturesTimes,title + " " + Approach.IP, ylabel);
		
		this.file.write("\n\n\n\n");
	}

	private void generateLinesGraphic(String title,
			HashMap<String, ArrayList<String>> naiveTimes,
			HashMap<String, ArrayList<String>> onlyChangedClassesTimes,
			HashMap<String, ArrayList<String>> impactedFeaturesTimes) throws IOException {
		
		//Naive -> 1
		//Only Changed Classes -> 2
		//Impacted Features -> 3
		
		this.file.write("naive <- c(" + this.generateConjuntoMean(naiveTimes) + ")\n");
		this.file.write("only_changed_classes <- c(" + this.generateConjuntoMean(onlyChangedClassesTimes) + ")\n");
		this.file.write("impacted_features <- c(" + this.generateConjuntoMean(impactedFeaturesTimes) + ")\n");
		this.file.write("g_range <- range(0, naive, only_changed_classes, impacted_features)\n");
		
		this.file.write("plot(naive, type=\"o\", col=rainbow(3)[1], ylim=g_range, axes=FALSE, ann=FALSE)\n");
		this.file.write("axis(1, at=1:" + this.legendas.size() + ", lab=c(" + this.getChanges() + "))\n");
		this.file.write("axis(2, las=1, at=round(g_range[2]/10)*0:g_range[2])\n");
		this.file.write("box()\n");
		
		this.file.write("lines(only_changed_classes, type=\"o\", pch=22, lty=2, col=rainbow(3)[2])\n");
		this.file.write("lines(impacted_features, type=\"o\", pch=23, lty=3, col=rainbow(3)[3])\n");
		
		this.file.write("title(main=\"" + title + "\", col.main=\"black\", font.main=4)\n");
		this.file.write("title(xlab=\"Changes\", col.lab=rgb(0,0.5,0))\n");
		this.file.write("title(ylab=\"Time\", col.lab=rgb(0,0.5,0))\n");
		
		this.file.write("legend(1, g_range[2], c(\"Naive\",\"Only Changed Classes\",\"Impacted Features\"), cex=0.8, col=c(rainbow(3)[1],rainbow(3)[2],rainbow(3)[3]), pch=21:23, lty=1:3)\n\n\n");
	}

	private String getChanges() {
		String result = "";
		
		for(String legenda : this.keysOrdendadasDeAcordoComALegenda){
			if(result.equals("")){
				result = getChanges(this.legendas.get(legenda));
			}
			else{
				result = result + "," + getChanges(this.legendas.get(legenda));
			}
			
		}
		
		return result;
	}

	private String getChanges(String legenda) {
		legenda = legenda.split(Pattern.quote("("))[1];
		legenda = legenda.split(Pattern.quote(")"))[0];
		
		return "\"" + legenda + "\"";
	}

	private void generateHistogramas(HashMap<String, ArrayList<String>> times, String title, String ylabel) throws IOException {
		for(String sourceTarget : this.keysOrdendadasDeAcordoComALegenda){
			ArrayList<String> tempos = times.get(sourceTarget);
			String temposString = ""; 

			for(String tempo : tempos){
				temposString = temposString + tempo + ",";
			}

			temposString = "c(" + temposString.substring(0,temposString.length()-1) + ")";
			
			this.file.write("hist(" + temposString + ",main=\"HISTOGRAMA " + title + " \\n" + sourceTarget + "\", xlab= \"" + ylabel + "\")\n");
		}
	}

	private String getLegendas() {
		String result = "";
		
		for(String key : this.keysOrdendadasDeAcordoComALegenda){
			result = result + "\"" + this.legendas.get(key) + "\","; 
		}
		
		return result.substring(0, result.length()-1);
	}

	private String generateConjuntoMean(
			HashMap<String, ArrayList<String>> times) {
		String result = "";
		
		for(String sourceTarget : this.keysOrdendadasDeAcordoComALegenda){
			ArrayList<String> tempos = times.get(sourceTarget);
			String temposString = ""; 
			
			for(String tempo : tempos){
				temposString = temposString + tempo + ",";
			}
			
			temposString = "mean(c(" + temposString.substring(0,temposString.length()-1) + "))";
			
			result = result + temposString + ",";
		}
		
		return result.substring(0,result.length()-1);
	}

	private void generateScript(ResultData resultData, String title, String ylabel, boolean generateLines) throws IOException {
		
		HashMap<String,ArrayList<String>> naiveTimes = new HashMap<String, ArrayList<String>>();
		HashMap<String,ArrayList<String>> onlyChangedClassesTimes = new HashMap<String, ArrayList<String>>();
		HashMap<String,ArrayList<String>> impactedFeaturesTimes = new HashMap<String, ArrayList<String>>();
		
		for(String sourceTarget : this.keysOrdendadasDeAcordoComALegenda){
			ArrayList<String> valoresNaive = new ArrayList<String>();
			ArrayList<String> valoresOnlyChangedClassesTimes = new ArrayList<String>();
			ArrayList<String> valoresImpactedFeaturesTimes = new ArrayList<String>();
			
			this.extractExecutionData(resultData, sourceTarget, valoresNaive, valoresOnlyChangedClassesTimes, valoresImpactedFeaturesTimes);
			
			naiveTimes.put(sourceTarget, valoresNaive);
			onlyChangedClassesTimes.put(sourceTarget, valoresOnlyChangedClassesTimes);
			impactedFeaturesTimes.put(sourceTarget, valoresImpactedFeaturesTimes);
		}
		
		this.generateR(naiveTimes, onlyChangedClassesTimes, impactedFeaturesTimes, title, ylabel, generateLines);
	}
	
	private void extractCoverageData(String tipo,
			String sourceTarget, ArrayList<String> valoresNaive,
			ArrayList<String> valoresOnlyChangedClassesTimes,
			ArrayList<String> valoresImpactedFeaturesTimes, String linhaOuBloco) throws IOException {
		
		//Serah analisada a cobertura apenas na versao original da linha.
		ArrayList<File> coverageSource = this.getCoberturaSourceTarget(sourceTarget);
		
		for(File file : coverageSource){
			String value = null;
			
			if(tipo.equals(COMPLETO)){
				value = this.getCoverageAllClasses(file, linhaOuBloco);
			}
			else{
				value = this.getCoverageChangedClasses(file, linhaOuBloco);
			}
			
			if(file.getName().contains(Approach.APP.toString())){
				valoresNaive.add(value);
			}
			else if(file.getName().contains(Approach.IC.toString())){
				valoresOnlyChangedClassesTimes.add(value);
			}
			else if(file.getName().contains(Approach.IP.toString())){
				valoresImpactedFeaturesTimes.add(value);
			}
		}
	}

	private String getCoverageChangedClasses(File file, String linhaOuBloco) throws IOException {
		String result = null;

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("br.edu.ufcg.dsc.coverage");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Report report = (Report) unmarshaller.unmarshal(file);
			All xmlData = report.getData().getAll();

			Collection<String> classesModificadas = this.getClassesModificadas(file);

			List<Package> packages = xmlData.getPackage();

			for(String classe : classesModificadas){
				String packageString = this.getPackage(classe);
				String classeString = this.getClasse(classe);

				for(Package pack : packages){

					if(pack.getName().equals(packageString)){

						for(Srcfile srcFile : pack.getSrcfile()){
							
							if(srcFile.getName().equals(classeString)){
								List<Coverage> coverages = srcFile.getCoverage();

								for(Coverage coverage : coverages){
									
									if(coverage.getType().contains(linhaOuBloco)){
										if(result == null){
											result = coverage.getValue().split(Pattern.quote("%"))[0];
										}
										else{
											result = result + "," + coverage.getValue().split(Pattern.quote("%"))[0];
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	private String getClasse(String classe) {
		String result = "";

		String[] packs = classe.split(Pattern.quote("."));

		for(int i = packs.length-1; i > packs.length-3; i--){
			if(result.equals("")){
				result = packs[i];
			}
			else{
				result = packs[i] + "." + result;
			}

		}

		return result;
	}

	private String getPackage(String classe) {
		String result = "";
		
		String[] packs = classe.split(Pattern.quote("."));
		
		for(int i = 0; i < packs.length-2; i++){
			if(result.equals("")){
				result = packs[i];
			}
			else{
				result = result + "." + packs[i];
			}
			
		}
		
		return result;
	}

	private Collection<String> getClassesModificadas(File file) throws IOException {
		String source = Constants.PLUGIN_PATH + "/Exemplo/" + file.getName().split(Pattern.quote("+"))[0];
		String target = Constants.PLUGIN_PATH + "/Exemplo/" + file.getName().split(Pattern.quote("+"))[1];
		
		Collection<String> classesModificadas = this.cacheClassesModificadas.get(source+target);
		
		System.out.println("FALTA IMPLEMENTAR COVERAGE PARA CLASSES MODIFICADAS");
		
//		if(classesModificadas == null){
//			ToolCommandLine toolCommandLine = new ToolCommandLine(Lines.DEFAULT);
//			
//			toolCommandLine.walkSourceTarget(source,target);
//			
//			classesModificadas = toolCommandLine.getClassesModificadas();
//			
//			this.cacheClassesModificadas.put(source+target, classesModificadas);
//		}
		
		return classesModificadas;
	}

	private String getCoverageAllClasses(File file, String linhaOuBloco) {
		String result = null;
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance("br.edu.ufcg.dsc.coverage");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Report report = (Report) unmarshaller.unmarshal(file);
			All xmlData = report.getData().getAll();
			
			List<Coverage> coverages = xmlData.getCoverage();
			
			for(Coverage coverage : coverages){
				if(coverage.getType().contains(linhaOuBloco)){
					result = coverage.getValue().split(Pattern.quote("%"))[0];
					
					break;
				}
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	private void extractExecutionData(ResultData resultData,
			String sourceTarget, ArrayList<String> valoresNaive,
			ArrayList<String> valoresOnlyChangedClassesTimes,
			ArrayList<String> valoresImpactedFeaturesTimes) {
		ArrayList<Properties> execucoesSourceTarget = this.getExecucoesSourceTarget(sourceTarget);
		
		for(Properties property : execucoesSourceTarget){
			valoresNaive.add((property.getProperty(Approach.APP.toString()) != null) ? 
					property.getProperty(Approach.APP.toString()).split(",")[resultData.ordinal()] : String.valueOf(0));
			
			valoresOnlyChangedClassesTimes.add((property.getProperty(Approach.IC.toString()) != null) ? 
					property.getProperty(Approach.IC.toString()).split(",")[resultData.ordinal()] : String.valueOf(0));
			
			valoresImpactedFeaturesTimes.add((property.getProperty(Approach.IP.toString()) != null) ? 
					property.getProperty(Approach.IP.toString()).split(",")[resultData.ordinal()] : String.valueOf(0));
		}
	}

	private void generateScript(String title, String ylabel, boolean generateLines) throws IOException {
		HashMap<String,ArrayList<String>> naiveTimes = new HashMap<String, ArrayList<String>>();
		HashMap<String,ArrayList<String>> onlyChangedClassesTimes = new HashMap<String, ArrayList<String>>();
		HashMap<String,ArrayList<String>> impactedFeaturesTimes = new HashMap<String, ArrayList<String>>();
		
		for(String sourceTarget : this.keysOrdendadasDeAcordoComALegenda){
			ArrayList<Properties> execucoesSourceTarget = this.getExecucoesSourceTarget(sourceTarget);
			
			ArrayList<String> valoresNaive = new ArrayList<String>();
			ArrayList<String> valoresOnlyChangedClassesTimes = new ArrayList<String>();
			ArrayList<String> valoresImpactedFeaturesTimes = new ArrayList<String>();
			
			for(Properties property : execucoesSourceTarget){
				String value;
				
				value = (property.getProperty("QUANTIDADE_" + Approach.APP.toString()) != null) ? 
						property.getProperty("QUANTIDADE_" + Approach.APP.toString()) : 
						String.valueOf("0");
						
				valoresNaive.add(value);
				
				value = (property.getProperty("QUANTIDADE_" + Approach.IC.toString()) != null) ? 
						property.getProperty("QUANTIDADE_" + Approach.IC.toString()) : 
						String.valueOf("0");
				valoresOnlyChangedClassesTimes.add(value);

				value = (property.getProperty("QUANTIDADE_" + Approach.IP.toString()) != null) ? 
						property.getProperty("QUANTIDADE_" + Approach.IP.toString()) : 
						String.valueOf("0");
				valoresImpactedFeaturesTimes.add(value);
			}

			naiveTimes.put(sourceTarget, valoresNaive);
			onlyChangedClassesTimes.put(sourceTarget, valoresOnlyChangedClassesTimes);
			impactedFeaturesTimes.put(sourceTarget, valoresImpactedFeaturesTimes);
		}
		
		this.generateR(naiveTimes, onlyChangedClassesTimes, impactedFeaturesTimes, title, ylabel, generateLines);
	}

	private ArrayList<Properties> getExecucoesSourceTarget(String sourceTarget) {
		ArrayList<Properties> result = new ArrayList<Properties>();
		
		for(String fileName : this.todosArquivosExecucoes.keySet()){
			if(fileName.contains(sourceTarget + "+")){
				result.add(this.todosArquivosExecucoes.get(fileName));
			}
		}
		
		return result;
	}

	private ArrayList<File> getCoberturaSourceTarget(String sourceTarget) {
		ArrayList<File> result = new ArrayList<File>();

		for(String fileName : this.todosArquivosExecucoes.keySet()){

			if(fileName.contains(sourceTarget + "+")){
				fileName = fileName.replaceFirst(".properties", "+");
				
				for(Approach approach : Approach.values()){
					String fileNameTemp = fileName + approach + "+source.xml";
					
					File file = new File(this.pathCobertura + Constants.FILE_SEPARATOR + fileNameTemp);
					
					if(file.exists()){
						result.add(file);
					}
				}
			}
		}

		return result;
	}

	private Vector<String> orderByValues(HashMap<String, String> legendas) {
		Vector<String> result = new Vector<String>();
		
		Vector<String> valuesLegendasParaOrdenar = new Vector<String>();
		valuesLegendasParaOrdenar.addAll(legendas.values());
		Collections.sort(valuesLegendasParaOrdenar);
		
		for(String value : valuesLegendasParaOrdenar){
			result.add(this.getKey(value,legendas));
		}
		
		return result;
	}

	private String getKey(String value, HashMap<String, String> legendasMap) {
		for(String key : legendasMap.keySet()){
			if(legendasMap.get(key).equals(value)){
				return key;
			}
		}

		return null;
	}
}
