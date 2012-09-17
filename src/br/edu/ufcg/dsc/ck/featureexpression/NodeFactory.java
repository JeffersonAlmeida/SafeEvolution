package br.edu.ufcg.dsc.ck.featureexpression;

import java.util.Set;

public class NodeFactory {
	private Set<String> features;

	public NodeFactory(Set<String> features) {
		this.features = features;
	}

	public AndExpression createAndExpression(IFeatureExpression lhs, IFeatureExpression rhs) {
		return new AndExpression(lhs, rhs);
	}

	public OrExpression createOrExpression(IFeatureExpression lhs, IFeatureExpression rhs) {
		return new OrExpression(lhs, rhs);
	}

	public NotExpression createNotExpression(IFeatureExpression exp) {
		return new NotExpression(exp);
	}

	public FeatureExpression createFeature(String name) throws Exception {
		String selectedFeature = null;
		for (String feature : this.features) {
			if (feature.equals(name)) {
				selectedFeature = feature;
				break;
			}
		}
		
		return new FeatureExpression(selectedFeature);
	}
}
