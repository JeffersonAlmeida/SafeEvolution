package br.edu.ufcg.dsc.refactoringresults;

public abstract class AbstractResult {

	protected boolean analysisResult;
	protected long startedTime;
	protected long finishedTime;

	public abstract String getLog();

	public boolean getAnalysisResult() {
		return analysisResult;
	}

	public long getStartedTime() {
		return startedTime;
	}

	public long getFinishedTime() {
		return finishedTime;
	}

	public long getTotalTime() {
		return getFinishedTime() - getStartedTime();
	}
}
