package br.edu.ufcg.dsc.refactoringresults;

import br.edu.ufcg.dsc.Constants;

public class FeatureModelEvolutionResult extends AbstractResult {

	public FeatureModelEvolutionResult(long started, long finished,
			boolean result) {
		this.startedTime = started;
		this.finishedTime = finished;
		this.analysisResult = result;
	}

	public String toString() {
		return getLog();
	}

	public String getLog() {
		String out = "===Feature Model Analysis===" + Constants.LINE_SEPARATOR;
		out += "Started Time: " + getStartedTime() + Constants.LINE_SEPARATOR;
		out += "Finished Time: " + getFinishedTime() + Constants.LINE_SEPARATOR;
		out += "Total Time Elapsed: " + getTotalTime()
				+ Constants.LINE_SEPARATOR;
		out += "Is FM Refactoring? " + getAnalysisResult()
				+ Constants.LINE_SEPARATOR;
		return out;
	}

}
