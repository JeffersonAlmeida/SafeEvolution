package br.edu.ufcg.dsc.refactoringresults;

import br.edu.ufcg.dsc.Constants;

public class SafeRefactorResult {

	private long compileTime;
	private long executionTime;
	private boolean isRefactoring;

	public SafeRefactorResult(long startedCompile, long finishedCompile,
			long startedTesting, long finishedTesting, boolean result) {
		this.compileTime = finishedCompile - startedCompile;
		this.executionTime = finishedTesting - startedTesting;
		this.isRefactoring = result;
	}

	public long getCompileTime() {
		return compileTime;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public boolean isRefactoring() {
		return isRefactoring;
	}

	public String getLog() {
		String out = "===SafeRefactor Results===" + Constants.LINE_SEPARATOR;
		out += "Total Compile Time: " + getCompileTime()
				+ Constants.LINE_SEPARATOR;
		out += "Total Test Execution Time: " + getExecutionTime()
				+ Constants.LINE_SEPARATOR;
		out += "Total Time Elapsed: " + (getCompileTime() + getExecutionTime())
				+ Constants.LINE_SEPARATOR;
		out += "Did the Transformation Preserve the Behavior? "
				+ isRefactoring() + Constants.LINE_SEPARATOR;
		return out;
	}

	public String toString() {
		return getLog();
	}
}
