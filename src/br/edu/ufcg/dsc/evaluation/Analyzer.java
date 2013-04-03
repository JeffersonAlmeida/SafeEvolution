package br.edu.ufcg.dsc.evaluation;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br
 * 
 * <strong><p>Esta classe � respons�vel por avaliar a evolu��o na Linha de Produtos de software.</p></strong>
 * 
 */

import java.io.File;
import java.io.IOException;

import safeEvolution.fileProperties.FilePropertiesObject;
import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.ToolCommandLine;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
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
		
		public void analize(FilePropertiesObject propertiesObject) throws DirectoryException, Err, IOException, AssetNotFoundException{
			ToolCommandLine toolCommandLine = new ToolCommandLine(propertiesObject.getLine());
			/* SPL evolution results */
			SPLOutcomes resultado = SPLOutcomes.getInstance();
			/*Set the Original SPL source path and the SPL Target source path from the ResultadoLPS Class */
			resultado.setSubject(propertiesObject.getSourceLineDirectory(), propertiesObject.getTargetLineDirectory());
			/* Set the amount of junit tests applied for each product*/
			resultado.getMeasures().setQuantidadeTestesPorProduto(propertiesObject.getInputLimit());
			/* This method reset the execution. Set the measures properties to Default values again.*/
			resultado.resetExecution();
			/* Is SPL refactoring a	 REFINEMENT ?*/
			resultado.setRefinement(toolCommandLine.verifyLine(propertiesObject));
			createOutcomesPropertyFile(propertiesObject, resultado);
		}

		private void createOutcomesPropertyFile(FilePropertiesObject propertiesObject, SPLOutcomes resultado) {
			String resultFileName =  Constants.PLUGIN_PATH + Constants.FILE_SEPARATOR + "resultFiles" + Constants.FILE_SEPARATOR +propertiesObject.getEvolutionDescription();
			System.out.println("\n\t SPL REPORT: \n");
			System.out.println(resultado.toString());
			File fileProperties = new File(resultFileName + ".properties");
			try {
				resultado.getMeasures().printProperties(fileProperties,resultado, propertiesObject.getEvolutionDescription());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
}
