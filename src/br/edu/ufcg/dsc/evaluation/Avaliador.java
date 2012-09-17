package br.edu.ufcg.dsc.evaluation;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br
 * 
 * Esta classe � respons�vel por avaliar a evolu��o na Linha de Produtos de software.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.Lines;
import br.edu.ufcg.dsc.ToolCommandLine;
import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.saferefactor.core.Criteria;
import edu.mit.csail.sdg.alloy4.Err;

public class Avaliador {
	
	/**
	 *   
	 *  Este m�todo avalia a evolu��o de uma Linha de Produtos de Software. Neste m�todo n�o precisa passar lista de bibliotecas por par�metro.
	 *  
	 * @param Lines line: Escolha de qual linha de produtos ser� avaliada.  Mobile Media, Default ou Target
	 * @param String sourcePath: O path do source da linha de produtos.  SPL Original  (SPL)
	 * @param String targetPath: O path do source da linha de produto com a evolu��o/refactoring. (SPL')
	 * @param int timeOut:   ????????
	 * @param int qtdTestes: A quantidade de testes que ser� gerada para cada m�todo da Linha de Produtos.
	 * @param approach: Qual a abordagem que ser� utilizada. @see Approach.java
	 * @param boolean temAspectosSource: Valor booleano para indicar se tem aspectos no source da LPS ou n�o.
	 * @param boolean temAspectosTarget: Valor booleano para indicar se tem aspectos no source Target da LPS ou n�o.
	 * @param String controladoresFachadas:  ??????????
	 * @param Criteria criteria: @see Criteria.java
	 * @param CKFormat sourceCKKind: Informa o formato do CK presente no source da LPS Original: Hephaestus ou Formato Simples.  @see CKFormat.java
	 * @param CKFormat targetCKKind: Informa o formato do CK presente no source da LPS Target: Hephaestus ou Formato Simples. @see CKFormat.java
	 * @param AMFormat sourceAMFormat: Informa o formato do AM presente no source da LPS Original: Hephaestus ou Formato Simples @see AMFormat.java
	 * @param AMFormat targetAMFormat: Informa o formato do AM presente no source da LPS Target: Hephaestus ou Formato Simples @see AMFormat.java
	 * @throws DirectoryException: Lan�a uma exce��o de diret�rio dos paths das linhas de produtos.
	 * @exception AssetNotFoundException:  Lan�a uma exce��o de asset n�o encontrado.
	 * @exception IOException: Lan�a uma exce��o de I/O.
	 * @exception Err: Lan�a uma exce��o de Erro.
	 * */
	public void avalie(Lines line, String sourcePath, String targetPath, int timeout, int qtdTestes, Approach approach, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat) throws DirectoryException{
		
	    ToolCommandLine toolCommandLine = new ToolCommandLine(line);
	
		try {
			ResultadoLPS resultado = this.verifyLine(toolCommandLine, sourcePath, targetPath, timeout, qtdTestes, approach, temAspectosSource, temAspectosTarget, controladoresFachadas, criteria, sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat);
			boolean resultadoEstahCerto = true;
			System.out.println("Resultado da verifica��o da LPS: " + resultadoEstahCerto + "\n");
			
			String resultFileName = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR +  "Resultados" + Constants.FILE_SEPARATOR + "Execucao" + System.currentTimeMillis();
			FileWriter resultFile = new FileWriter(resultFileName + ".txt");
			resultFile.write(resultado.toString());
			resultFile.close();
			File fileProperties = new File(resultFileName + ".properties");
			resultado.getMeasures().printProperties(fileProperties);
		} catch (Err e) {
			System.out.println(e.getMessage()+ "\n\n\n");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage()+ "\n\n\n");
			e.printStackTrace();
		} catch (AssetNotFoundException e) {
			System.out.println(e.getMessage()+ "\n\n");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * @author Jefferson Almeida - jra at cin dot ufpe dot br
	 *  
	 *  Este m�todo avalia a evolu��o de uma Linha de Produtos de Software.
	 *  
	 * @param Lines line: Escolha de qual linha de produtos ser� avaliada.  Mobile Media, Default ou Target
	 * @param String sourcePath: O path do source da linha de produtos.  SPL Original  (SPL)
	 * @param String targetPath: O path do source da linha de produto com a evolu��o/refactoring. (SPL')
	 * @param int timeOut:   ????????
	 * @param int qtdTestes: A quantidade de testes que ser� gerada para cada m�todo da Linha de Produtos.
	 * @param approach: Qual a abordagem que ser� utilizada. @see Approach.java
	 * @param boolean temAspectosSource: Valor booleano para indicar se tem aspectos no source da LPS ou n�o.
	 * @param boolean temAspectosTarget: Valor booleano para indicar se tem aspectos no source Target da LPS ou n�o.
	 * @param String controladoresFachadas:  ??????????
	 * @param Criteria criteria: @see Criteria.java
	 * @param CKFormat sourceCKKind: Informa o formato do CK presente no source da LPS Original: Hephaestus ou Formato Simples.  @see CKFormat.java
	 * @param CKFormat targetCKKind: Informa o formato do CK presente no source da LPS Target: Hephaestus ou Formato Simples. @see CKFormat.java
	 * @param AMFormat sourceAMFormat: Informa o formato do AM presente no source da LPS Original: Hephaestus ou Formato Simples @see AMFormat.java
	 * @param AMFormat targetAMFormat: Informa o formato do AM presente no source da LPS Target: Hephaestus ou Formato Simples @see AMFormat.java
	 * @param String... libs: Este par�metro pode ser uma string ou v�rias. Uma sequ�ncia de bibliotecas.   Ex: lib01, lib02, lib03, ... , lib0n.
	 * @return void: Este m�todo n�o retorna nada.
	 * @throws DirectoryException: Lan�a uma exce��o de diret�rio dos paths das linhas de produtos.
	 * @exception AssetNotFoundException:  Lan�a uma exce��o de asset n�o encontrado.
	 * @exception IOException: Lan�a uma exce��o de I/O.
	 * @exception Err: Lan�a uma exce��o de Erro.
	 * */	
	public void avalie(Lines line, String sourcePath, String targetPath, int timeout, int qtdTestes, Approach approach, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat, String... libs) throws DirectoryException{
		
		ToolCommandLine toolCommandLine = new ToolCommandLine(line);
		
		try {
				String libPathSource = null;
				String libPathTarget = null;
				/*Pega as bibliotecas da SPL Original e da SPL Target */
				if(libs != null){
					if(libs.length > 0){
						libPathSource = libs[0];
						libPathTarget = libs[0];
						if(libs.length > 1){
							libPathTarget = libs[1];
						}
					}
				}
				
				ResultadoLPS resultado = this.verifyLine(toolCommandLine, sourcePath, targetPath, timeout, qtdTestes, approach, temAspectosSource, temAspectosTarget, controladoresFachadas, criteria, sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat, libPathSource, libPathTarget);
				
				boolean resultadoEstahCerto = true;
						
				System.out.println(resultadoEstahCerto);
				
				String resultFileName = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + 
				"Resultados" + Constants.FILE_SEPARATOR + "Execucao" + System.currentTimeMillis();
				
				FileWriter resultFile = new FileWriter(resultFileName + ".txt");
				resultFile.write(resultado.toString());
				resultFile.close();
				
				File fileProperties = new File(resultFileName + ".properties");
					
				resultado.getMeasures().printProperties(fileProperties);
			
		} catch (Err e) {
			System.out.println(e.getMessage()+"\n\n\n\n");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage()+"\n\n\n\n");
			e.printStackTrace();
		} catch (AssetNotFoundException e) {
			System.out.println(e.getMessage()+"\n\n\n\n");
			e.printStackTrace();
		}		
	}
	
	
	
	/**  
	 * @param toolCommandLine
	 * @param sourcePath
	 * @param targetPath
	 * @param timeout
	 * @param qtdTestes
	 * @param approach
	 * @param temAspectosSource
	 * @param temAspectosTarget
	 * @param controladoresFachadas
	 * @param criteria
	 * @param sourceCKKind
	 * @param targetCKKind
	 * @param sourceAMFormat
	 * @param targetAMFormat
	 * @return
	 * @throws Err
	 * @throws IOException
	 * @throws AssetNotFoundException
	 * @throws DirectoryException
	 */
	public ResultadoLPS verifyLine(ToolCommandLine toolCommandLine, String sourcePath, String targetPath, int timeout, int qtdTestes, Approach approach, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat) throws Err, IOException, AssetNotFoundException, DirectoryException {
		
		ResultadoLPS resultado = new ResultadoLPS();
		
		resultado.setSubject(sourcePath, targetPath);
		resultado.getMeasures().setQuantidadeTestesPorProduto(qtdTestes);
		resultado.resetExecution();

		boolean isRefinement = toolCommandLine.verifyLine(sourcePath, targetPath, timeout, qtdTestes, approach, temAspectosSource, temAspectosTarget, controladoresFachadas, criteria, sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat, resultado);
		
		resultado.setRefinement(isRefinement);
		
		return resultado;

	}
	
	/**
	 * 
	 * @param toolCommandLine
	 * @param sourcePath
	 * @param targetPath
	 * @param timeout
	 * @param qtdTestes
	 * @param approach
	 * @param temAspectosSource
	 * @param temAspectosTarget
	 * @param controladoresFachadas
	 * @param criteria
	 * @param sourceCKKind
	 * @param targetCKKind
	 * @param sourceAMFormat
	 * @param targetAMFormat
	 * @param libPathSource
	 * @param libPathTarget
	 * @return
	 * @throws Err
	 * @throws IOException
	 * @throws AssetNotFoundException
	 * @throws DirectoryException
	 */
	public ResultadoLPS verifyLine(ToolCommandLine toolCommandLine, String sourcePath, String targetPath, int timeout, int qtdTestes, Approach approach, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat, String libPathSource, String libPathTarget) throws Err, IOException, AssetNotFoundException, DirectoryException {
		
		ResultadoLPS resultado = new ResultadoLPS();
		
		resultado.setSubject(sourcePath, targetPath);
		resultado.getMeasures().setQuantidadeTestesPorProduto(qtdTestes);
		resultado.resetExecution();

		boolean isRefinement = toolCommandLine.verifyLine(sourcePath, targetPath, timeout, qtdTestes, approach, temAspectosSource, temAspectosTarget, controladoresFachadas, criteria, sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat, resultado, libPathSource, libPathTarget);
		
		resultado.setRefinement(isRefinement);
		
		return resultado;

	}

}
