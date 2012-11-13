package br.edu.ufcg.dsc.evaluation;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br
 * 
 * <strong><p>Esta classe é responsável por avaliar a evolução na Linha de Produtos de software.</p></strong>
 * 
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.org.apache.regexp.internal.RE;

import br.edu.ufcg.dsc.Approach;
import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.Lines;
import br.edu.ufcg.dsc.ToolCommandLine;
import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.dsc.util.Measures;
import br.edu.ufcg.saferefactor.core.Criteria;
import edu.mit.csail.sdg.alloy4.Err;

public class Avaliador {
	
	/**
	 *   {@link #avalie(Lines, String, String, int, int, Approach, boolean, boolean, String, Criteria, CKFormat, CKFormat, AMFormat, AMFormat)}
	 *  <p>Este método avalia a evolução de uma Linha de Produtos de Software. Neste método não precisa passar lista de bibliotecas por parâmetro.<p>
	 *  
	 * <strong>@param Lines line:</strong> Escolha de qual linha de produtos será avaliada.  Mobile Media, Default ou Target<br></br>
	 * <strong>@param String sourcePath:</strong> O path do source da linha de produtos.  SPL Original  (SPL)<br></br>
	 * <strong>@param String targetPath:</strong> O path do source da linha de produto com a evolução/refactoring. (SPL')<br></br>
	 * <strong>@param int timeOut:</strong>   ????????<br></br>
	 * <strong>@param int qtdTestes:</strong> A quantidade de testes que será gerada para cada método da Linha de Produtos.<br></br>
	 * <strong>@param approach:</strong> Qual a abordagem que será utilizada. <br></br>
	 * <strong>@param boolean temAspectosSource:</strong> Valor booleano para indicar se tem aspectos no source da LPS ou não.<br></br>
	 * <strong>@param boolean temAspectosTarget:</strong> Valor booleano para indicar se tem aspectos no source Target da LPS ou não.<br></br>
	 * <strong>@param String controladoresFachadas:</strong>  ??????????<br></br>
	 * <strong>@param Criteria criteria:</strong> <br></br>
	 * <strong>@param CKFormat sourceCKKind:</strong> Informa o formato do CK presente no source da LPS Original: Hephaestus ou Formato Simples. <br></br>
	 * <strong>@param CKFormat targetCKKind:</strong> Informa o formato do CK presente no source da LPS Target: Hephaestus ou Formato Simples.<br></br>
	 * <strong>@param AMFormat sourceAMFormat:</strong> Informa o formato do AM presente no source da LPS Original: Hephaestus ou Formato Simples<br></br>
	 * <strong>@param AMFormat targetAMFormat:</strong> Informa o formato do AM presente no source da LPS Target: Hephaestus ou Formato Simples <br></br>
	 * <strong>@throws DirectoryException:</strong> Lança uma exceção de diretório dos paths das linhas de produtos.<br></br>
	 * <strong>@exception AssetNotFoundException:</strong>  Lança uma exceção de asset não encontrado.<br></br>
	 * <strong>@exception IOException:</strong> Lança uma exceção de I/O.<br></br>
	 * <strong>@exception Err:</strong> Lança uma exceção de Erro.<br></br>
	 * */
	public void avalie(Lines line, String sourcePath, String targetPath, int timeout, int qtdTestes, Approach approach, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat) throws DirectoryException{
		
	    ToolCommandLine toolCommandLine = new ToolCommandLine(line);
	
		try {
			ResultadoLPS resultado = this.verifyLine(toolCommandLine, sourcePath, targetPath, timeout, qtdTestes, approach, temAspectosSource, temAspectosTarget, controladoresFachadas, criteria, sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat);
			/** Esta variável <resultadoEstahCerto> pode ser removida do codigo, esta sendo usadas apenas aqui.*/
			boolean resultadoEstahCerto = true;
			System.out.println("Resultado da verificação da LPS: " + resultadoEstahCerto + "\n");
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
	 * {@link #avalie(Lines, String, String, int, int, Approach, boolean, boolean, String, Criteria, CKFormat, CKFormat, AMFormat, AMFormat, String...)}
	 * 
	 *  <p>Este método avalia a evolução de uma Linha de Produtos de Software.</p>
	 *  
	 *  
	 * <strong>@param Lines line:</strong> Escolha de qual linha de produtos será avaliada.  <strong>Mobile Media, Default ou Target</strong> <br></br>
	 * <strong>@param String sourcePath:</strong> O path do source da linha de produtos.  SPL Original  (SPL)<br></br>
	 * <strong>@param String targetPath:</strong> O path do source da linha de produto com a evolução/refactoring. (SPL')<br></br>
	 * <strong>@param int timeOut:</strong>   ???????? <br></br>
	 * <strong>@param int qtdTestes:</strong> A quantidade de testes que será gerada para cada método da Linha de Produtos.<br></br>
	 * <strong>@param approach:</strong> Qual a abordagem que será utilizada. {@link <br.edu.ufcg.dsc.Approach><br></br>
	 * <strong>@param boolean temAspectosSource:</strong> Valor booleano para indicar se tem aspectos no source da LPS ou não.<br></br>
	 * <strong>@param boolean temAspectosTarget:</strong> Valor booleano para indicar se tem aspectos no source Target da LPS ou não.<br></br>
	 * <strong>@param String controladoresFachadas:</strong>  ??????????<br></br>
	 * <strong>@param Criteria criteria:</strong><br></br>
	 * <strong>@param CKFormat sourceCKKind:</strong> Informa o formato do CK presente no source da LPS Original: Hephaestus ou Formato Simples.<br></br>
	 * <strong>@param CKFormat targetCKKind:</strong> Informa o formato do CK presente no source da LPS Target: Hephaestus ou Formato Simples.<br></br>
	 * <strong>@param AMFormat sourceAMFormat:</strong> Informa o formato do AM presente no source da LPS Original: Hephaestus ou Formato Simples<br></br>
	 * <strong>@param AMFormat targetAMFormat:</strong> Informa o formato do AM presente no source da LPS Target: Hephaestus ou Formato Simples<br></br>
	 * <strong>@param String... libs:</strong> Este parâmetro pode ser uma string ou várias. Uma sequência de bibliotecas.   Ex: lib01, lib02, lib03, ... , lib0n.<br></br>
	 * <strong>@return void:</strong> Este método não retorna nada.<br></br>
	 * <strong>@throws DirectoryException:</strong> Lança uma exceção de diretório dos paths das linhas de produtos.<br></br>
	 * <strong>@exception AssetNotFoundException:</strong>  Lança uma exceção de asset não encontrado.<br></br>
	 * <strong>@exception IOException:</strong> Lança uma exceção de I/O.<br></br>
	 * <strong>@exception Err:</strong> Lança uma exceção de Erro.<br></br> 
	 * @see Approach
	 * @see CKFormat
	 * */	
	public void avalie(Lines line, String sourcePath, String targetPath, int timeout, int qtdTestes, Approach approach, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat, String... libs) throws DirectoryException{
		
		ToolCommandLine toolCommandLine = new ToolCommandLine(line);
		
		System.out.println("Avaliando A LPS.");
		
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
				
				File sourceFile = new File(sourcePath);File targetFile = new File(targetPath);
				String path = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "Resultados" + Constants.FILE_SEPARATOR + "Report-"+sourceFile.getName() + "-"+targetFile.getName();
				System.out.println("\nnew directory: " + path);
				File f = new File(path);
				if (!f.exists()) {
					f.mkdirs();
				}
				
				// String resultFileName = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "Resultados" + Constants.FILE_SEPARATOR + "Execucao" + System.currentTimeMillis();
				String resultFileName =  Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "Resultados" + Constants.FILE_SEPARATOR +"Report-"+sourceFile.getName() + "-"+targetFile.getName() + Constants.FILE_SEPARATOR + "Report-"+sourceFile.getName() + "-"+targetFile.getName()+"-"+approach;
				 
				FileWriter resultFile = new FileWriter(resultFileName + ".txt");
				resultFile.write(resultado.toString());
				resultFile.close();
				
				System.out.println("\n\t SPL REPORT: \n");
				System.out.println(resultado.toString());
				
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
		
		ResultadoLPS resultado = ResultadoLPS.getInstance();
		
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
	 * @return ResultadoLPS
	 * @throws Err
	 * @throws IOException
	 * @throws AssetNotFoundException
	 * @throws DirectoryException
	 */
	public ResultadoLPS verifyLine(ToolCommandLine toolCommandLine, String sourcePath, String targetPath, int timeout, int qtdTestes, Approach approach, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat, String libPathSource, String libPathTarget) throws Err, IOException, AssetNotFoundException, DirectoryException {
		
		/* SPL evolution results */
		ResultadoLPS resultado = ResultadoLPS.getInstance();
		
		/*Set the Original SPL source path and the SPL Target source path from the ResultadoLPS Class */
		resultado.setSubject(sourcePath, targetPath);
		
		/* Set the amount of junit tests applied for each product*/
		resultado.getMeasures().setQuantidadeTestesPorProduto(qtdTestes);
	
		/* This method reset the execution. Set the measures properties to Default values again.*/
		resultado.resetExecution();

		/* Delega a responsabilidade de verificar se a linha é refinamento para a classe ToolCommandLine */
		boolean isRefinement = toolCommandLine.verifyLine(sourcePath, targetPath, timeout, qtdTestes, approach, temAspectosSource, temAspectosTarget, controladoresFachadas, criteria, sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat, resultado, libPathSource, libPathTarget);
		
		/* Is this Evolution a refinement. Put it down in the Results please. */
		resultado.setRefinement(isRefinement);
		
		/* returns the results of my SPL evolution. Is it safe ? Let's check it out on Results class report. */
		return resultado;
	}

}
