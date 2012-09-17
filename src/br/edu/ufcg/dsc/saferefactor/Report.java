package br.edu.ufcg.dsc.saferefactor;

public class Report {
	
	private boolean isRefactoring;
	
	private int totalTests;
	
	private int failedTests;
	
	private int totalTime;

	public boolean isRefactoring() {
		return isRefactoring;
	}

	public void setRefactoring(boolean isRefactoring) {
		this.isRefactoring = isRefactoring;
	}

	public int getTotalTests() {
		return totalTests;
	}

	public void setTotalTests(int totalTests) {
		this.totalTests = totalTests;
	}

	public int getFailedTests() {
		return failedTests;
	}

	public void setFailedTests(int failedTests) {
		this.failedTests = failedTests;
	}

	public int getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(int totalTime) {
		this.totalTime = totalTime;
	}

}
