package br.edu.ufcg.dsc.evaluation;

import java.util.Collection;

public class Diferenca {
	
	private int idSourceProduct;
	
	private int idTargetProduct; //Target gerado a partir da mesma configuracao
	
	private Collection<String> metodosComComportamentoDiferente;
	private Collection<String> metodosRemovidos;
	private Collection<String> metodosAdicionados;
	
	private boolean produtoTemMesmosMetodosQuantoComparadoAoProdutoComMesmaConfiguracao;

	public int getIdSourceProduct() {
		return idSourceProduct;
	}

	public void setIdSourceProduct(int idSourceProduct) {
		this.idSourceProduct = idSourceProduct;
	}

	public int getIdTargetProduct() {
		return idTargetProduct;
	}

	public void setIdTargetProduct(int idTargetProduct) {
		this.idTargetProduct = idTargetProduct;
	}

	public Collection<String> getMetodosComComportamentoDiferente() {
		return metodosComComportamentoDiferente;
	}

	public void setMetodosComComportamentoDiferente(
			Collection<String> metodosComComportamentoDiferente) {
		this.metodosComComportamentoDiferente = metodosComComportamentoDiferente;
	}

	public boolean isProdutoTemMesmosMetodosQuantoComparadoAoProdutoComMesmaConfiguracao() {
		return produtoTemMesmosMetodosQuantoComparadoAoProdutoComMesmaConfiguracao;
	}

	public void setProdutoTemMesmosMetodosQuandoComparadoAoProdutoComMesmaConfiguracao(
			boolean produtoTemMesmosMetodosQuantoComparadoAoProdutoComMesmaConfiguracao) {
		this.produtoTemMesmosMetodosQuantoComparadoAoProdutoComMesmaConfiguracao = produtoTemMesmosMetodosQuantoComparadoAoProdutoComMesmaConfiguracao;
	}

	public Collection<String> getMetodosRemovidos() {
		return metodosRemovidos;
	}

	public void setMetotodosRemovidos(Collection<String> metotodosRemovidos) {
		this.metodosRemovidos = metotodosRemovidos;
	}

	public Collection<String> getMetodosAdicionados() {
		return metodosAdicionados;
	}

	public void setMetodosAdicionados(Collection<String> metodosAdicionados) {
		this.metodosAdicionados = metodosAdicionados;
	}
}
