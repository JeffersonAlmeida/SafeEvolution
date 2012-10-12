package br.edu.ufcg.dsc.ck.featureexpression;

import java.util.Set;

public interface IFeatureExpression {
	public String getCode();
	/**features: A set of features that compose a product.*/
	public boolean evaluate(Set<String> features);
	public boolean equals(Object o);
	public int hashCode();
}
