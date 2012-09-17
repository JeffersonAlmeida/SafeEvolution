package br.edu.ufcg.dsc.evaluation;

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
	
	public void avalie(Lines line, String sourcePath, String targetPath, 
			int timeout, int qtdTestes, Approach approach, 
			boolean temAspectosSource, boolean temAspectosTarget,
			String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind,
			AMFormat sourceAMFormat, AMFormat targetAMFormat) throws DirectoryException{
		
		ToolCommandLine toolCommandLine = new ToolCommandLine(line);
		
		try {
			ResultadoLPS resultado = this.verifyLine(toolCommandLine, sourcePath, targetPath, timeout, qtdTestes, 
					approach, temAspectosSource, temAspectosTarget, controladoresFachadas, criteria, 
					sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat);
			
			boolean resultadoEstahCerto = true;
			
//			if(!resultado.isAssetMappingsEqual()){
//				if(resultado.isRefinement()){ // Se ferramenta respondeu que eh refinamento, rodar 10x a quantidade de testes.
//					resultado = this.verifyLine(toolCommandLine, sourcePath, targetPath, timeout, 10 * qtdTestes, 
//							approach, temAspectosSource, temAspectosTarget, controladoresFachadas, method, 
//							sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat);
//					
//					if(resultado.isRefinement()){
//						System.out.println("O resultado provavelmente estah OK. Verificar manualmente.");
//					}
//					else{
//						System.out.println("Apï¿½s rodar 10x a quantidade de testes, foram encontradas diferenï¿½as.\n" +
//								"O resultado estï¿½ errado.");
//					}
//				}
//				else{
//					if(resultado.temMesmosMetodosPublicos()){
//						System.out.println("A ferramenta identificou mudanï¿½a de comportamento em algum dos mï¿½todos,\n" +
//								"ï¿½ necessï¿½rio checar se os mï¿½todos com comportamento modificado tem clientes.\n" +
//								"Se existirem clientes e o comportamento deles tambï¿½m tiver sido modificado,\n" +
//								"a resposta estï¿½ OK. Senï¿½o, a resposta estï¿½ errada.");
//					}
//					else{
//						System.out.println("Projetos nï¿½o tem mesmos mï¿½todos pï¿½blicos\n" +
//						"ï¿½ necessï¿½rio a verificaï¿½ï¿½o manual.");
//					}
//				}
//			}
			
			System.out.println(resultadoEstahCerto);
			
			String resultFileName = Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + 
			"Resultados" + Constants.FILE_SEPARATOR + "Execucao" + System.currentTimeMillis();
			
			FileWriter resultFile = new FileWriter(resultFileName + ".txt");
			resultFile.write(resultado.toString());
			resultFile.close();
			
			File fileProperties = new File(resultFileName + ".properties");
			
			resultado.getMeasures().printProperties(fileProperties);
			
		} catch (Err e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AssetNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @author Jefferson Almeida - jra at cin dot ufpe dot br
	 *  
	 * @param Lines line: Escolha de qual linha de produtos será avaliada.  Mobile Media, Default ou Target
	 * @param String sourcePath: O path do source da linha de produtos.  SPL Original  (SPL)
	 * @param String targetPath: O path do source da linha de produto com a evolução/refactoring. (SPL')
	 * @param int timeOut:   ????????
	 * @param int qtdTestes: A quantidade de testes que será gerada para cada método da Linha de Produtos.
	 * @param approach: Qual a abordagem que será utilizada. @see Approach.java
	 * @param boolean temAspectosSource: Valor booleano para indicar se tem aspectos no source da LPS ou não.
	 * @param boolean temAspectosTarget: Valor booleano para indicar se tem aspectos no source Target da LPS ou não.
	 * @param String controladoresFachadas:
	 * @param Criteria criteria: @see Criteria.java
	 * @param CKFormat sourceCKKind: Informa o formato do CK presente no source da LPS Original: Hephaestus ou Formato Simples.  @see CKFormat.java
	 * @param CKFormat targetCKKind: Informa o formato do CK presente no source da LPS Target: Hephaestus ou Formato Simples. @see CKFormat.java
	 * @param AMFormat sourceAMFormat: Informa o formato do AM presente no source da LPS Original: Hephaestus ou Formato Simples @see AMFormat.java
	 * @param AMFormat targetAMFormat: Informa o formato do AM presente no source da LPS Target: Hephaestus ou Formato Simples @see AMFormat.java
	 * @param String... libs: Este parâmetro pode ser uma string ou várias. Uma sequência de bibliotecas.
	 * @return void: Este método não retorna nada.
	 * @throws DirectoryException: Lança uma exceção de diretório dos paths das linhas de produtos.
	 * @exception AssetNotFoundException:  Lança uma exceção de asset não encontrado.
	 * @exception IOException: Lança uma exceção de I/O.
	 * @exception Err: Lança uma exceção de Erro.
	 * */	
	public void avalie(Lines line, String sourcePath, String targetPath, int timeout, int qtdTestes, Approach approach, boolean temAspectosSource, boolean temAspectosTarget, String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind, AMFormat sourceAMFormat, AMFormat targetAMFormat, String... libs) throws DirectoryException{
		
		ToolCommandLine toolCommandLine = new ToolCommandLine(line);
		
		try {
			String libPathSource = null;
			String libPathTarget = null;
			
			if(libs != null){
				if(libs.length > 0){
					libPathSource = libs[0];
					libPathTarget = libs[0];
					
					if(libs.length > 1){
						libPathTarget = libs[1];
					}
				}
			}
			ResultadoLPS resultado = this.verifyLine(toolCommandLine, sourcePath, targetPath, timeout, qtdTestes, 
					approach, temAspectosSource, temAspectosTarget, controladoresFachadas, criteria, 
					sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat, libPathSource, libPathTarget);
			
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AssetNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	
	public ResultadoLPS verifyLine(ToolCommandLine toolCommandLine, String sourcePath, String targetPath, 
			int timeout, int qtdTestes, Approach approach, 
			boolean temAspectosSource, boolean temAspectosTarget,
			String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind,
			AMFormat sourceAMFormat, AMFormat targetAMFormat) throws Err, IOException,
			AssetNotFoundException, DirectoryException {
		
		ResultadoLPS resultado = new ResultadoLPS();
		
		resultado.setSubject(sourcePath, targetPath);
		resultado.getMeasures().setQuantidadeTestesPorProduto(qtdTestes);
		resultado.resetExecution();

		boolean isRefinement = toolCommandLine.verifyLine(sourcePath, targetPath,
				timeout, qtdTestes, approach, temAspectosSource, temAspectosTarget, 
				controladoresFachadas, criteria, 
				sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat, resultado);
		
		resultado.setRefinement(isRefinement);
		
		return resultado;

	}
	
	public ResultadoLPS verifyLine(ToolCommandLine toolCommandLine, String sourcePath, String targetPath, 
			int timeout, int qtdTestes, Approach approach, 
			boolean temAspectosSource, boolean temAspectosTarget,
			String controladoresFachadas, Criteria criteria, CKFormat sourceCKKind, CKFormat targetCKKind,
			AMFormat sourceAMFormat, AMFormat targetAMFormat, String libPathSource, String libPathTarget) throws Err,
			IOException, AssetNotFoundException, DirectoryException {
		
		ResultadoLPS resultado = new ResultadoLPS();
		
		resultado.setSubject(sourcePath, targetPath);
		resultado.getMeasures().setQuantidadeTestesPorProduto(qtdTestes);
		resultado.resetExecution();

		boolean isRefinement = toolCommandLine.verifyLine(sourcePath, targetPath,
				timeout, qtdTestes, approach, temAspectosSource, temAspectosTarget, 
				controladoresFachadas, criteria, 
				sourceCKKind, targetCKKind, sourceAMFormat, targetAMFormat, resultado, libPathSource, libPathTarget);
		
		resultado.setRefinement(isRefinement);
		
		return resultado;

	}

}
