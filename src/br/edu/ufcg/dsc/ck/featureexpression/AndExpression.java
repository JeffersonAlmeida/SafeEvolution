package br.edu.ufcg.dsc.ck.featureexpression;

import java.util.Set;


public class AndExpression implements IFeatureExpression {
	
	private IFeatureExpression lhs;
	private IFeatureExpression rhs;
	
	@Override
	public boolean evaluate(Set<String> features) {
		return ((this.lhs.evaluate(features)) && (this.rhs.evaluate(features)));
	}

	@Override
	public String getCode() {
		return "(" + this.lhs.getCode() + " and " + this.rhs.getCode() + ")";
	}
	
	public AndExpression(IFeatureExpression lhs, IFeatureExpression rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public IFeatureExpression getLhs() {
		return this.lhs;
	}

	public IFeatureExpression getRhs() {
		return this.rhs;
	}

	@Override
	public String toString() {
		return this.getCode();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lhs == null) ? 0 : lhs.hashCode());
		result = prime * result + ((rhs == null) ? 0 : rhs.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return  (obj instanceof AndExpression)
			&& (((AndExpression) obj).getCode().equals(this.getCode()));
	}
}
