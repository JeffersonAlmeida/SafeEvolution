package br.edu.ufcg.dsc.evaluation;

import java.util.ArrayList;
import java.util.Collection;

import br.edu.ufcg.dsc.util.Measures;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br 
 * This class is responsible to present the SPL evolution results.
 */
public class ResultadoLPS {
	
	/**SPL Measures. Time to compile products. Time to compile tests and so on.*/
	private Measures measures;

	/** Is It well formed ? */
	private boolean isWF;

	/**FM and CK is a refinement ?*/
	private boolean fmAndCKRefinement;

	private boolean isAssetMappingsEqual;
	private Collection<Diferenca> diferencas;
	
	/**SPL is a refinement ?*/
	private boolean isRefinement;
	
	/**
	 * Initiate the properties of the class.
	 * Default Values.
	 */
	public ResultadoLPS() {
		this.measures = new Measures();
		this.isWF = false;
		this.fmAndCKRefinement = false;
		this.isAssetMappingsEqual = false;
		this.diferencas = new ArrayList<Diferenca>();
	}
	
	@Override
	public String toString() {
		
		
		String result = "My Software Product Line is Well Formed ?: " + this.isWF + "\n\n";
		result += "Are Configuration Knowledge and Feature Model a refinement ?: " + this.fmAndCKRefinement + "\n";
		
		String metodosAdicionadosString = "";
		String metodosRemovidosString = "";
		String metodosComComportamentoDiferenteString = "";
		
		for(Diferenca diferenca : this.diferencas){
			Collection<String> metodosAdicionados = diferenca.getMetodosAdicionados();
			
			if(metodosAdicionados != null){
				for(String metodoAdicionado : metodosAdicionados){
					metodosAdicionadosString += metodoAdicionado + "\n";
				}
			}
			
			Collection<String> metodosRemovidos = diferenca.getMetodosRemovidos();
			
			if(metodosRemovidos != null){
				for(String metodoRemovido : metodosRemovidos){
					metodosRemovidosString += metodoRemovido + "\n";
				}
			}
			
			Collection<String> metodosComComportamentoDiferente = diferenca.getMetodosComComportamentoDiferente();
			
			if(metodosComComportamentoDiferente != null){
				for(String metodoComComportamentoDiferente : metodosComComportamentoDiferente){
					metodosComComportamentoDiferenteString += metodoComComportamentoDiferente + "\n";
				}
			}
		}
		
		if(!metodosAdicionadosString.equals("")){
			result += "MŽtodos adicionados:\n";
			result += metodosAdicionadosString + "\n";
		}
		if(!metodosRemovidosString.equals("")){
			result += "MŽtodos removidos:\n";
			result += metodosRemovidosString + "\n";
		}
		if(!metodosComComportamentoDiferenteString.equals("")){
			result += "MŽtodos com comportamento diferente:\n";
			result += metodosComComportamentoDiferenteString + "\n";
		}

		return result;
	}

	
	/**
	 * 
	 * @return
	 */
	public boolean temMesmosMetodosPublicos(){
		boolean result = true;
		
		for(Diferenca diferenca : this.diferencas){
			if(diferenca.getMetodosAdicionados().size() > 0 || diferenca.getMetodosRemovidos().size() > 0){
				result = false;
				
				break;
			}
		}
		
		return result;
	}
	
	
	/**
	 * <h2><strong>GETTERS and SETTERS</strong><br></br></h2>
	 */
	
	/** Set the Original SPL source path and the SPL Target source path
	 * @param sourcePath
	 * @param targetPath
	 */
	public void setSubject(String sourcePath, String targetPath) {
		this.measures.setSubject(sourcePath, targetPath);
	}

	/**
	 * This method reset the execution.
	 * Set the measures properties to Default values again.
	 */
	public void resetExecution() {
		this.measures.resetExecution();
	}

	public Measures getMeasures() {
		return measures;
	}

	public boolean isWF() {
		return isWF;
	}

	public void setWF(boolean isWF) {
		this.isWF = isWF;
	}

	public void setFMAndCKRefinement(boolean fmAndCKRefinement) {
		this.fmAndCKRefinement = fmAndCKRefinement;
	}

	public boolean isFmAndCKRefinement() {
		return fmAndCKRefinement;
	}

	public void setAssetMappingsEqual(boolean isAssetMappingsEqual) {
		this.isAssetMappingsEqual = isAssetMappingsEqual;
	}

	public void addDiferenca(Diferenca diferenca) {
		this.diferencas.add(diferenca);
	}

	public boolean isAssetMappingsEqual() {
		return isAssetMappingsEqual;
	}

	public Collection<Diferenca> getDiferencas() {
		return diferencas;
	}

	public boolean isRefinement() {
		return isRefinement;
	}

	public void setRefinement(boolean isRefinement) {
		this.isRefinement = isRefinement;
	}
}
