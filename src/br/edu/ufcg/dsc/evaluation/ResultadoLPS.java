package br.edu.ufcg.dsc.evaluation;

import java.util.ArrayList;
import java.util.Collection;

import br.edu.ufcg.dsc.util.Measures;

public class ResultadoLPS {
	
	private Measures measures;
	
	private boolean isWF;

	private boolean fmAndCKRefinement;

	private boolean isAssetMappingsEqual;

	private Collection<Diferenca> diferencas;
	
	private boolean isRefinement;
	
	public ResultadoLPS() {
		this.measures = new Measures();
		
		this.isWF = false;
		
		this.fmAndCKRefinement = false;
		
		this.isAssetMappingsEqual = false;
		
		this.diferencas = new ArrayList<Diferenca>();
	}
	
	@Override
	public String toString() {
		String result = "Well-formed: " + this.isWF + "\n";
		result += "FM and CK refinement: " + this.fmAndCKRefinement + "\n";
		
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

	public void setSubject(String sourcePath, String targetPath) {
		this.measures.setSubject(sourcePath, targetPath);
	}

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
}
