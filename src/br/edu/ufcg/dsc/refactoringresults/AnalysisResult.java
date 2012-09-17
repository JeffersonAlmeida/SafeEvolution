package br.edu.ufcg.dsc.refactoringresults;

import java.util.ArrayList;

import br.edu.ufcg.dsc.Constants;

public class AnalysisResult {

	private EvolutionCompositionResult evolutionResult;
	private FeatureModelEvolutionResult featureModelEvolutionResult;
	private ArrayList<SafeRefactorResult> behaviorResult;

	public AnalysisResult() {
		behaviorResult = new ArrayList<SafeRefactorResult>();
	}

	public boolean isRefactoring() {
		if (evolutionResult != null) {
			return evolutionResult.existsCompositionProblem();
		} else if (featureModelEvolutionResult != null) {
			return featureModelEvolutionResult.getAnalysisResult();
		} else if (behaviorResult != null) {
			for (SafeRefactorResult result : behaviorResult) {
				if (result.isRefactoring()) {
					return true;
				}
			}
		}
		return false;
	}

	public String getCompleteResult() {
		StringBuilder out = new StringBuilder("===Complete Analysis Result==="
				+ Constants.LINE_SEPARATOR);
		if (evolutionResult != null) {
			out.append(evolutionResult.getSourceResult().getLog()
					+ Constants.LINE_SEPARATOR);
			out.append(evolutionResult.getTargetResult().getLog()
					+ Constants.LINE_SEPARATOR);
		}else{
			out.append("No safe composition analysis was made" + Constants.LINE_SEPARATOR);
		}
		
		if (featureModelEvolutionResult != null) {
			out.append(featureModelEvolutionResult.getLog()
					+ Constants.LINE_SEPARATOR);
		}else{
			out.append("No feature model refactoring analysis was made" + Constants.LINE_SEPARATOR);
		}
		
		if (behaviorResult != null) {
			int counter = 1;
			for (SafeRefactorResult result : behaviorResult) {
				out.append("Product " + counter + " result" + Constants.LINE_SEPARATOR);
				out.append(result.getLog() + Constants.LINE_SEPARATOR);
				counter++;
			}
		} else{
			out.append("No behavior analysis was made" + Constants.LINE_SEPARATOR);
		}
		
		return out.toString();
	}

	public EvolutionCompositionResult getEvolutionResult() {
		return evolutionResult;
	}

	public void setEvolutionResult(EvolutionCompositionResult evolutionResult) {
		this.evolutionResult = evolutionResult;
	}

	public FeatureModelEvolutionResult getFeatureModelEvolutionResult() {
		return featureModelEvolutionResult;
	}

	public void setFeatureModelEvolutionResult(
			FeatureModelEvolutionResult featureModelEvolutionResult) {
		this.featureModelEvolutionResult = featureModelEvolutionResult;
	}

	public ArrayList<SafeRefactorResult> getBehaviorResult() {
		return behaviorResult;
	}

	public void addBehaviorResult(SafeRefactorResult result) {
		behaviorResult.add(result);
	}
}
