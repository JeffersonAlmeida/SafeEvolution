package br.edu.ufcg.dsc.evaluation;

import java.util.Collection;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br
This class is responsible to perform some differences between the source product and the target product.
 */
public class Diferenca {
	
	/**The Source Product identification*/
	private int idSourceProduct;
	
	/**The Target Product identification
	 * target product generated from the same configuration of the source product. */
	private int idTargetProduct; 
	
	/**A collection of methods (strings) that have different behavior.*/
	private Collection<String> metodosComComportamentoDiferente;
	
	/**A collection(String) of removed methods.*/
	private Collection<String> metodosRemovidos;
	
	/**A collection(String) of added methods.*/
	private Collection<String> metodosAdicionados;
	
	/**This flag is used to inform whether the target product has same methods when compared with the source product. With the same configuration.*/
	private boolean produtoTemMesmosMetodosQuantoComparadoAoProdutoComMesmaConfiguracao;
	

	/**
	 * <h2><strong>Getters and Setters</strong><br></br></h2>
	 */
	
	/**
	 *  * @return The Id Source Product
	 */
	public int getIdSourceProduct() {
		return idSourceProduct;
	}
	public void setIdSourceProduct(int idSourceProduct) {
		this.idSourceProduct = idSourceProduct;
	}
	/**
	 * 	 * @return
	 */
	public int getIdTargetProduct() {
		return idTargetProduct;
	}
	public void setIdTargetProduct(int idTargetProduct) {
		this.idTargetProduct = idTargetProduct;
	}
	public Collection<String> getMetodosComComportamentoDiferente() {
		return metodosComComportamentoDiferente;
	}
	public void setMetodosComComportamentoDiferente(Collection<String> metodosComComportamentoDiferente) {
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
