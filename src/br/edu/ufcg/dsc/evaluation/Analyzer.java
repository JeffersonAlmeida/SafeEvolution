package br.edu.ufcg.dsc.evaluation;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br
 * 
 * <strong><p>Esta classe � respons�vel por avaliar a evolu��o na Linha de Produtos de software.</p></strong>
 * 
 */

import java.io.File;
import java.io.IOException;

import br.cin.ufpe.br.fileProperties.FilePropertiesObject;
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

public class Analyzer {
	
	private static Analyzer instance;

	public static synchronized Analyzer getInstance() {
		if (instance == null) {
			instance = new Analyzer();
		}
		return instance;
	}
	
	private Analyzer(){
		super();
	}
	
	/**
	 * {@link #avalie(Lines, String, String, int, int, Approach, boolean, boolean, String, Criteria, CKFormat, CKFormat, AMFormat, AMFormat, String...)}
	 * 
	 *  <p>Este m�todo avalia a evolu��o de uma Linha de Produtos de Software.</p>
	 *  
	 *  
	 * <strong>@param Lines line:</strong> Escolha de qual linha de produtos ser� avaliada.  <strong>Mobile Media, Default ou Target</strong> <br></br>
	 * <strong>@param String sourcePath:</strong> O path do source da linha de produtos.  SPL Original  (SPL)<br></br>
	 * <strong>@param String targetPath:</strong> O path do source da linha de produto com a evolu��o/refactoring. (SPL')<br></br>
	 * <strong>@param int timeOut:</strong>   ???????? <br></br>
	 * <strong>@param int qtdTestes:</strong> A quantidade de testes que ser� gerada para cada m�todo da Linha de Produtos.<br></br>
	 * <strong>@param approach:</strong> Qual a abordagem que ser� utilizada. {@link <br.edu.ufcg.dsc.Approach><br></br>
	 * <strong>@param boolean temAspectosSource:</strong> Valor booleano para indicar se tem aspectos no source da LPS ou n�o.<br></br>
	 * <strong>@param boolean temAspectosTarget:</strong> Valor booleano para indicar se tem aspectos no source Target da LPS ou n�o.<br></br>
	 * <strong>@param String controladoresFachadas:</strong>  ??????????<br></br>
	 * <strong>@param Criteria criteria:</strong><br></br>
	 * <strong>@param CKFormat sourceCKKind:</strong> Informa o formato do CK presente no source da LPS Original: Hephaestus ou Formato Simples.<br></br>
	 * <strong>@param CKFormat targetCKKind:</strong> Informa o formato do CK presente no source da LPS Target: Hephaestus ou Formato Simples.<br></br>
	 * <strong>@param AMFormat sourceAMFormat:</strong> Informa o formato do AM presente no source da LPS Original: Hephaestus ou Formato Simples<br></br>
	 * <strong>@param AMFormat targetAMFormat:</strong> Informa o formato do AM presente no source da LPS Target: Hephaestus ou Formato Simples<br></br>
	 * <strong>@param String... libs:</strong> Este par�metro pode ser uma string ou v�rias. Uma sequ�ncia de bibliotecas.   Ex: lib01, lib02, lib03, ... , lib0n.<br></br>
	 * <strong>@return void:</strong> Este m�todo n�o retorna nada.<br></br>
	 * <strong>@throws DirectoryException:</strong> Lan�a uma exce��o de diret�rio dos paths das linhas de produtos.<br></br>
	 * <strong>@exception AssetNotFoundException:</strong>  Lan�a uma exce��o de asset n�o encontrado.<br></br>
	 * <strong>@exception IOException:</strong> Lan�a uma exce��o de I/O.<br></br>
	 * <strong>@exception Err:</strong> Lan�a uma exce��o de Erro.<br></br> 
	 * @see Approach
	 * @see CKFormat
	 * */	
	public void avalie(FilePropertiesObject propertiesObject) throws DirectoryException{
		
		ToolCommandLine toolCommandLine = new ToolCommandLine(propertiesObject.getLine());
		
		System.out.println("Avaliando A LPS.");
		
		try {
				
				SPLOutcomes resultado = this.verifyLine(toolCommandLine, propertiesObject);
				
				String resultFileName =  Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "resultFiles" + Constants.FILE_SEPARATOR +propertiesObject.getEvolutionDescription();
				 
				System.out.println("\n\t SPL REPORT: \n");
				System.out.println(resultado.toString());
				File fileProperties = new File(resultFileName + ".properties");
				resultado.getMeasures().printProperties(fileProperties,resultado, propertiesObject.getEvolutionDescription());
			
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
	public SPLOutcomes verifyLine(ToolCommandLine toolCommandLine, FilePropertiesObject propertiesObject) throws Err, IOException, AssetNotFoundException, DirectoryException {
		
		/* SPL evolution results */
		SPLOutcomes resultado = SPLOutcomes.getInstance();
		
		/*Set the Original SPL source path and the SPL Target source path from the ResultadoLPS Class */
		resultado.setSubject(propertiesObject.getSourceLineDirectory(), propertiesObject.getTargetLineDirectory());
		
		/* Set the amount of junit tests applied for each product*/
		resultado.getMeasures().setQuantidadeTestesPorProduto(propertiesObject.getInputLimit());
	
		/* This method reset the execution. Set the measures properties to Default values again.*/
		resultado.resetExecution();

		/* Delega a responsabilidade de verificar se a linha � refinamento para a classe ToolCommandLine */
		boolean isRefinement = toolCommandLine.verifyLine(propertiesObject);
		
		/* Is this Evolution a refinement. Put it down in the Results please. */
		resultado.setRefinement(isRefinement);
		
		/* returns the results of my SPL evolution. Is it safe ? Let's check it out on Results class report. */
		return resultado;
	}

}
