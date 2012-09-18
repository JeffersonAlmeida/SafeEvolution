package br.edu.ufcg.dsc;

/**
 *  @author Jefferson Almeida - jra at cin dot ufpe dot br
 *  
 *  <p>Classe que armazena as strings constantes que serão usadas para criar arquivos
 *  de acordo com o sistema operacional utilizado. Esta classe também possui: <br></br> 
 *  - Diretório fonte do plugin.  <br></br> 
 *  - Diretório fonte do Alloy.  <br></br> 
 *  - Diretório dos produtos gerados.  <br></br> </p>
 * 
 *  */
public class Constants {
	
	/**Separador de arquivo do sistema que esta sendo utilizado.*/
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	
	/**Separador de linha do sistema que esta sendo utilizado.*/
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/**Caminho para o workspace do projeto do plugin. (Home Path da Ferramenta).
	<br></br>Pegar caminho de forma automática: Activator.getDefault().getPluginFolder();*/  
	public static final String PLUGIN_PATH = "D:/documentos/Msc/SE/workspace/ferramentaLPSSM"; 
	
	/**Home Path para o Alloy*/
	public static final String ALLOY_PATH = PLUGIN_PATH + FILE_SEPARATOR + "Alloy" + FILE_SEPARATOR;
    
	/**String que armazena a extensão do ALLOY.*/
	public static final String ALLOY_EXTENSION = ".als";
	
	/**Diretório que armazenará os produtos gerados da LPS.*/
	public static final String PRODUCTS_DIR = PLUGIN_PATH + "/Products";
	
	/**Arquivo Allory de avaliação do CK da LPS Original.*/
	public static final String SOURCE_CK_ALLOY_NAME = "sourceCKAlloy";
	
	/**Arquivo Allory de avaliação do CK da LPS Target.*/
	public static final String TARGET_CK_ALLOY_NAME = "targetCKAlloy";
	
	/**Arquivo Allory de avaliação do FM da LPS Original.*/
	public static final String SOURCE_FM_ALLOY_NAME = "sourceFMAlloy";
	
	/**Arquivo Allory de avaliação do FM da LPS Target.*/
	public static final String TARGET_FM_ALLOY_NAME = "targetFMAlloy";
	
	/**Arquivo Alloy da evolução do FM.*/
	public static final String EVOLUTION_FM_ALLOY_NAME = "evolutionFMAlloy";
	
}
