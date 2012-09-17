package br.edu.ufcg.dsc.ck.featureexpression;

import java.util.Set;


public class NotExpression implements IFeatureExpression {
	
	private IFeatureExpression exp;
	
	@Override
	public boolean evaluate(Set<String> features) {
		return (!(this.exp.evaluate(features)));
	}

	@Override
	public String getCode() {
		return "not(" + this.exp.getCode() + ")";
	}
	
	public NotExpression(IFeatureExpression exp) {
		this.exp = exp;
	}

	public IFeatureExpression getExp() {
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
		return (obj instanceof NotExpression)
		&& (((NotExpression) obj).getCode().equals(this.getCode()));
	}
	
	
}
