package br.edu.ufcg.dsc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Constants;

/**
 * 
 *   @author Jefferson Almeida - jra at cin dot ufpe dot br
 * 
 * <strong><p>Esta classe é responsável por calcular o tempo necessário para realizar determinadas tarefas
 *  ao avaliar a evolução da LPS. Como por exemplo:<br></br>
 *  - tempo para compilar os produtos.<br></br>
 *  - tempo para compilar os teste.<br></br>
 *  - tempo para executar a abordagem.<br></br></p></strong>
 *
 */
public class Measures {
	
	/**Tempo total gasto na avaliação da LPS>*/
	private Timer tempoTotal;
	
	/**Tempo gasto para comilar os produtos da LPS>*/
	private Timer tempoCompilacaoProdutos;
	
	/**Tempo necessário para compilar os testes aplicados aos produtos.*/
	private Timer tempoCompilacaoTestes;
	
	/**Tempo gasto na execução dos testes*/
	private Timer tempoExecucaoTestes;
	
	/**Tempo para executar a abordagem selecionada: <strong><NAIVE_2_ICTAC>, <NAIVE_1_APROXIMACAO>, <ONLY_CHANGED_CLASSES>, <IMPACTED_FEATURES></strong>*/
	private Timer tempoExecucaoAbordagem;

	/**Usada para armazenar o path da LPS Original*/
	private String sourcePath;
	
	/**Usada para armazenar o path da LPS Target*/
	private String targetPath;
	
	/**Propriedade para armazenar qual abordagem foi selecionada. */
	private Approach approach;

	/**Quantidade total dos produtos que foram compilados.*/
	private int quantidadeProdutosCompilados;
	
	/**Quantidade total dos testes aplicados.*/
	private int quatidadeTotalTestes;
	
	/**Quantidade de testes aplicados por produto.*/
	private int quantidadeTestesPorProduto;

	/**Nome resultante do arquivo para gerar o relatório.*/
	private String fileResultName;
	
	/**Variável usada para armazenar o tempo corrente em milisegundos.*/
	private String execucao;
	
	/**Arquivo de propriedades*/
	private String filePropertiesName;
	
	/**Variável boolena para determinar se houve refinamento ou não na Evolução da LPS.*/
	private boolean isRefinement;
	
	/**
	 * Creates all measures instances.
	 */
	public Measures(){
		this.tempoTotal = new Timer();
		this.tempoCompilacaoProdutos = new Timer();
		this.tempoCompilacaoTestes = new Timer();
		this.tempoExecucaoTestes = new Timer();
		this.tempoExecucaoAbordagem = new Timer();
		this.fileResultName = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "Resultados" + Constants.FILE_SEPARATOR + "Execution" + System.currentTimeMillis() + ".txt";
	}
	
	/**
	 * This method reset the execution.
	 */
	public void resetExecution(){
		/*Get the System time in millisenconds*/
		this.execucao = String.valueOf(System.currentTimeMillis());
		File fileSource = new File(this.sourcePath);
		File targetPath = new File(this.targetPath);
		this.filePropertiesName = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "Resultados" + Constants.FILE_SEPARATOR + "properties" + Constants.FILE_SEPARATOR + fileSource.getName() + "+" + targetPath.getName() + "+" + "Execution" + this.execucao + ".properties";
	}

	/**
	 * Try to print measures properties in a file.
	 */
	public void print(){
		try {
			this.printProperties();
		} catch (IOException e) {
			System.out.println("An error occurred when trying to print measures properties in the file.\n\n"+ e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * print measures properties in a file.
	 * @throws IOException
	 */
	private void printProperties() throws IOException {
		File file = new File(this.filePropertiesName);
		File fileSource = new File(this.sourcePath);
		File targetPath = new File(this.targetPath);
		/*Se o arquivo não existir, cria um novo arquivo.*/
		if(!file.exists()){
			file.createNewFile();
		}
		
		Properties properties = new Properties();
		properties.load(new FileInputStream(file));
		properties.setProperty("source", fileSource.getName());
		properties.setProperty("target", targetPath.getName());
		
		//Formato da String Value => Abordagem = tempoTotal, tempoCompilacaoProdutos, tempoCompilacaoTestes, tempoExecucaoTestes, qtdTestes, quantidadeProdutosCompilados, isRefinement, tempoExecucaoAbordagem
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
		
		/*Writes this property list (key and element pairs) in this Properties table to the output stream in a format suitable for loading into a Properties table using the load(InputStream) method.*/
		properties.store(new FileOutputStream(this.filePropertiesName),"Abordagem = tempoTotal, tempoCompilacaoProdutos, tempoCompilacaoTestes, tempoExecucaoTestes, qtdTestes, quantidadeProdutosCompilados, isRefinement, tempoExecucaoAbordagem");
	
	}
	
	/**
	 * print measures properties in a file.
	 * @param file
	 * @throws IOException
	 */
	public void printProperties(File file)throws IOException {		
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

	/**
	<h3>This method is probably not used. Study the possibility to remove it.</h3>
	 * */
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

	/**
	 * Set the Original SPL source path and the SPL Target source path
	 * @param sourcePath: String to the Original SPL source path
	 * @param targetPath: String to the SPL Target source path
	 */
	public void setSubject(String sourcePath, String targetPath) {
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
	}

	/**
	 * Set the approach of the measure.
	 * @param approach
	 * @see Approach
	 */
	public void setApproach(Approach approach) {
		this.approach = approach;
	}

	/**
	 * Reset the time for all measures:
	 * Total Time <br></br>
	 * Time to the products compile<br></br>
	 * Time to run the tests<br></br>
	 * Time to run the approach<br></br>
	 * Set the amount of compiled product to zero<br></br>
	 */
	public void reset() {
		this.tempoTotal.reset();
		this.tempoCompilacaoProdutos.reset();
		this.tempoCompilacaoTestes.reset();
		this.tempoExecucaoTestes.reset();
		this.tempoExecucaoAbordagem.reset();
		this.quantidadeProdutosCompilados = 0;
	}
	
	/**
	 * <h2>Getters and Setters to the Measures Class</h2>
	 */
	
	/**
	 * Set whether the SPL evolution is a refinement. 
	 * @param isRefinement
	 */
	public void setResult(boolean isRefinement){
		this.isRefinement = isRefinement;
	}
	/**
	 * Set the amout of compiled products.
	 * @param quantidadeProdutosCompilados
	 */
	public void setQuantidadeProdutosCompilados(int quantidadeProdutosCompilados) {
		this.quantidadeProdutosCompilados = quantidadeProdutosCompilados;
	}
	/**
	 * @return the amout of compiled products.
	 */
	public int getQuantidadeProdutosCompilados() {
		return quantidadeProdutosCompilados;
	}
	/**
	 * @return the amount of tests
	 */
	public int getQuatidadeTotalTestes() {
		return quatidadeTotalTestes;
	}
	/**
	 * Set the total amount of tests.
	 * @param quatidadeTotalTestes
	 */
	public void setQuatidadeTotalTestes(int quatidadeTotalTestes) {
		this.quatidadeTotalTestes = quatidadeTotalTestes;
	}
	/**
	 * @return the amount of tests for each product
	 */
	public int getQuantidadeTestesPorProduto() {
		return quantidadeTestesPorProduto;
	}
	
	/**
	 * Set the amount of tests for each product
	 * @param quantidadeTestesPorProduto
	 */
	public void setQuantidadeTestesPorProduto(int quantidadeTestesPorProduto) {
		this.quantidadeTestesPorProduto = quantidadeTestesPorProduto;
	}
	/**
	 * @return The total Time.
	 */
	public Timer getTempoTotal() {
		return tempoTotal;
	}
	/**
	 * @return The amout of time to compile the SPL products
	 */
	public Timer getTempoCompilacaoProdutos() {
		return tempoCompilacaoProdutos;
	}
	/**
	 * @return The amout of time to compile the tests.
	 */
	public Timer getTempoCompilacaoTestes() {
		return tempoCompilacaoTestes;
	}
	/**
	 * @return The amount of time to run the tests.
	 */
	public Timer getTempoExecucaoTestes() {
		return tempoExecucaoTestes;
	}
	/**
	 * @return The amount of time spent to run the approach
	 * @see Approach
	 */
	public Timer getTempoExecucaoAbordagem() {
		return tempoExecucaoAbordagem;
	}
	/**
	 * @return The Properties File name.
	 */
	public String getFilePropertiesName() {
		return this.filePropertiesName;
	}
}
