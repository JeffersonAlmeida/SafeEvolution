package br.edu.ufcg.dsc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Constants;

public class Measures {
	
	private Timer tempoTotal;
	private Timer tempoCompilacaoProdutos;
	private Timer tempoCompilacaoTestes;
	private Timer tempoExecucaoTestes;
	private Timer tempoExecucaoAbordagem;

	private String sourcePath;
	private String targetPath;

	private Approach approach;

	private int quantidadeProdutosCompilados;
	private int quatidadeTotalTestes;
	private int quantidadeTestesPorProduto;

	private String fileResultName;
	private String execucao;
	private String filePropertiesName;
	
	private boolean isRefinement;
	
//	private static Measures instance;
	
	public Measures(){
		this.tempoTotal = new Timer();
		this.tempoCompilacaoProdutos = new Timer();
		this.tempoCompilacaoTestes = new Timer();
		this.tempoExecucaoTestes = new Timer();
		
		this.tempoExecucaoAbordagem = new Timer();
		
		this.fileResultName = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "Resultados" + Constants.FILE_SEPARATOR + "Execution" + System.currentTimeMillis() + ".txt";
	}
	
//	public static Measures getInstance(){
//		if(instance == null){
//			instance = new Measures();
//		}
//		
//		return instance;
//	}

	public String getFilePropertiesName() {
		return this.filePropertiesName;
	}

	public void resetExecution(){
		this.execucao = String.valueOf(System.currentTimeMillis());
		
		File fileSource = new File(this.sourcePath);
		File targetPath = new File(this.targetPath);
		
		this.filePropertiesName = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + 
			"Resultados" + Constants.FILE_SEPARATOR + 
			"properties" + Constants.FILE_SEPARATOR + 
			fileSource.getName() + "+" + targetPath.getName() + "+" + "Execution" + this.execucao + ".properties";
	}

	public Timer getTempoTotal() {
		return tempoTotal;
	}

	public Timer getTempoCompilacaoProdutos() {
		return tempoCompilacaoProdutos;
	}

	public Timer getTempoCompilacaoTestes() {
		return tempoCompilacaoTestes;
	}

	public Timer getTempoExecucaoTestes() {
		return tempoExecucaoTestes;
	}

	public Timer getTempoExecucaoAbordagem() {
		return tempoExecucaoAbordagem;
	}

	public void print(){
		try {
		//	this.printResult();
			
			this.printProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void printProperties() throws IOException {
		File file = new File(this.filePropertiesName);
		
		File fileSource = new File(this.sourcePath);
		File targetPath = new File(this.targetPath);
		
		if(!file.exists()){
			file.createNewFile();
		}
		
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		
		properties.setProperty("source", fileSource.getName());
		properties.setProperty("target", targetPath.getName());
		
		//Formato 
		//Abordagem = tempoTotal, tempoCompilacaoProdutos, tempoCompilacaoTestes, tempoExecucaoTestes, qtdTestes, quantidadeProdutosCompilados, isRefinement, tempoExecucaoAbordagem
		
		String value = 
			String.valueOf(this.tempoTotal.getTotal()) + "," +  
			String.valueOf(this.tempoCompilacaoProdutos.getTotal()) + "," +
			String.valueOf(this.tempoCompilacaoTestes.getTotal()) + "," +
			String.valueOf(this.tempoExecucaoTestes.getTotal()) + "," +
			String.valueOf(this.quatidadeTotalTestes) + "," +
			String.valueOf(this.quantidadeProdutosCompilados) + "," +
			String.valueOf(this.isRefinement) + "," +
			String.valueOf(this.tempoExecucaoAbordagem.getTotal());
		
		properties.setProperty(this.approach.toString(), value);
		
		properties.store(new FileOutputStream(this.filePropertiesName),"Abordagem = tempoTotal, tempoCompilacaoProdutos, tempoCompilacaoTestes, tempoExecucaoTestes, qtdTestes, quantidadeProdutosCompilados, isRefinement, tempoExecucaoAbordagem");
	}
	
	public void printProperties(File file) throws IOException {		
		File fileSource = new File(this.sourcePath);
		File targetPath = new File(this.targetPath);
		
		if(!file.exists()){
			file.createNewFile();
		}
		
		Properties properties = new Properties();
		
		FileInputStream fileInputStream = new FileInputStream(file);
		properties.load(fileInputStream);
		fileInputStream.close();
		
		properties.setProperty("source", fileSource.getName());
		properties.setProperty("target", targetPath.getName());
		
		//Formato 
		//Abordagem = tempoTotal, tempoCompilacaoProdutos, tempoCompilacaoTestes, tempoExecucaoTestes, qtdTestes, quantidadeProdutosCompilados, isRefinement, tempoExecucaoAbordagem
		
		String value = 
			String.valueOf(this.tempoTotal.getTotal()) + "," +  
			String.valueOf(this.tempoCompilacaoProdutos.getTotal()) + "," +
			String.valueOf(this.tempoCompilacaoTestes.getTotal()) + "," +
			String.valueOf(this.tempoExecucaoTestes.getTotal()) + "," +
			String.valueOf(this.quatidadeTotalTestes) + "," +
			String.valueOf(this.quantidadeProdutosCompilados) + "," +
			String.valueOf(this.isRefinement) + "," +
			String.valueOf(this.tempoExecucaoAbordagem.getTotal());
		
		properties.setProperty(this.approach.toString(), value);
		
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		properties.store(fileOutputStream,"Abordagem = tempoTotal, tempoCompilacaoProdutos, tempoCompilacaoTestes, tempoExecucaoTestes, qtdTestes, quantidadeProdutosCompilados, isRefinement, tempoExecucaoAbordagem");
		fileOutputStream.close();
	}

	private void printResult() throws IOException {
		FileWriter file = new FileWriter(this.fileResultName, true);
		
		file.write("SOURCE: " + this.sourcePath + "\n");
		file.write("TARGET: " + this.targetPath + "\n\n");
		
		file.write("ABORDAGEM: " + this.approach + "\n");
		file.write("QUANTIDADE DE TESTES: " + this.quatidadeTotalTestes + "\n\n");
		
		file.write("QUANTIDADE DE PRODUTOS COMPILADOS: " + this.quantidadeProdutosCompilados + "\n\n");
		
		file.write("TEMPO TOTAL: " + this.tempoTotal.getTotal() + " milisegundos.\n");
		file.write("TEMPO DE COMPILACAO DE PRODUTOS: " + this.tempoCompilacaoProdutos.getTotal() + " milisegundos.\n");
		file.write("TEMPO DE COMPILACAO DE TESTES: " + this.tempoCompilacaoTestes.getTotal() + " milisegundos.\n");
		file.write("TEMPO DE EXECUCAO DE TESTES: " + this.tempoExecucaoTestes.getTotal() + " milisegundos.\n\n");
		
		file.write("RESPOSTA: " + this.isRefinement + "\n\n\n\n\n");
		
		file.close();
	}

	public void setSubject(String sourcePath, String targetPath) {
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
	}

	public void setApproach(Approach approach) {
		this.approach = approach;
	}

	public void reset() {
		this.tempoTotal.reset();
		this.tempoCompilacaoProdutos.reset();
		this.tempoCompilacaoTestes.reset();
		this.tempoExecucaoTestes.reset();
		this.tempoExecucaoAbordagem.reset();
		
		this.quantidadeProdutosCompilados = 0;
	}

	public void setResult(boolean isRefinement) {
		this.isRefinement = isRefinement;
	}

	public void setQuantidadeProdutosCompilados(int quantidadeProdutosCompilados) {
		this.quantidadeProdutosCompilados = quantidadeProdutosCompilados;
	}

	public int getQuantidadeProdutosCompilados() {
		return quantidadeProdutosCompilados;
	}

	public int getQuatidadeTotalTestes() {
		return quatidadeTotalTestes;
	}

	public void setQuatidadeTotalTestes(int quatidadeTotalTestes) {
		this.quatidadeTotalTestes = quatidadeTotalTestes;
	}

	public int getQuantidadeTestesPorProduto() {
		return quantidadeTestesPorProduto;
	}

	public void setQuantidadeTestesPorProduto(int quantidadeTestesPorProduto) {
		this.quantidadeTestesPorProduto = quantidadeTestesPorProduto;
	}
}
