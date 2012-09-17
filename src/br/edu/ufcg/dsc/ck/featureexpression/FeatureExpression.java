package br.edu.ufcg.dsc.ck.featureexpression;

import java.util.Set;


public class FeatureExpression implements IFeatureExpression {
	
	private String exp;
	
	@Override
	public boolean evaluate(Set<String> features) {
		return (features.contains(this.exp));
	}

	@Override
	public String getCode() {
		return this.exp;
	}
	
	public FeatureExpression(String exp) {
		this.exp = exp;
	}

	public String getExp() {
		return this.exp;
	}

	@Override
	public String toString() {
		return this.getCode();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exp == null) ? 0 : exp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof FeatureExpression)
		&& (((FeatureExpression) obj).getCode().equals(this.getCode()));
	}
}
