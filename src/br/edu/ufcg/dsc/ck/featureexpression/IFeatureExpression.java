package br.edu.ufcg.dsc.ck.featureexpression;

import java.util.Set;

public interface IFeatureExpression {
	public String getCode();
	public boolean evaluate(Set<String> features);
	public boolean equals(Object o);
	public int hashCode();
}
