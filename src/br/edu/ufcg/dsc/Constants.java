package br.edu.ufcg.dsc;

public class Constants {

	public static final String FILE_SEPARATOR = System
			.getProperty("file.separator");
	public static final String LINE_SEPARATOR = System
			.getProperty("line.separator");

	
//	GUSTAVO:como o projeto � de plugin agora, o USER_DIR se refere ao workspace criado para testar o plugin 
//	public static final String USER_DIR = System.getProperty("user.dir");
	//	public static final String ALLOY_PATH = USER_DIR + "Alloy" + FILE_SEPARATOR;
	
	//GUSTAVO: caminho para o workspace do projeto do plugin  // C:/Users/Jefferson/Documents/Msc/SE/workspace/ferramentaLPSSM
	public static final String PLUGIN_PATH = "D:/documentos/Msc/SE/workspace/ferramentaLPSSM"; // Activator.getDefault().getPluginFolder();
	public static final String ALLOY_PATH = PLUGIN_PATH + FILE_SEPARATOR + "Alloy" + FILE_SEPARATOR;

	public static final String ALLOY_EXTENSION = ".als";
	public static final String PRODUCTS_DIR = PLUGIN_PATH + "/Products";
	public static final String SOURCE_CK_ALLOY_NAME = "sourceCKAlloy";
	public static final String TARGET_CK_ALLOY_NAME = "targetCKAlloy";
	public static final String SOURCE_FM_ALLOY_NAME = "sourceFMAlloy";
	public static final String TARGET_FM_ALLOY_NAME = "targetFMAlloy";
	public static final String EVOLUTION_FM_ALLOY_NAME = "evolutionFMAlloy";
	
}
