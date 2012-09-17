package br.edu.ufcg.dsc.refactoringresults;

public class EvolutionCompositionResult {

	private SafeCompositionResult sourceResult;
	private SafeCompositionResult targetResult;

	public EvolutionCompositionResult(SafeCompositionResult source,
			SafeCompositionResult target) {
		sourceResult = source;
		targetResult = target;
	}

	public SafeCompositionResult getSourceResult() {
		return sourceResult;
	}

	public SafeCompositionResult getTargetResult() {
		return targetResult;
	}

	public boolean existsCompositionProblem() {
		return getSourceResult().getTotalErrors() != 0
				|| getTargetResult().getTotalErrors() != 0;
	}
}
