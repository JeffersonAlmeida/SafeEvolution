package br.edu.ufcg.dsc.refactoringresults;

import java.util.Hashtable;
import java.util.Set;

import br.edu.ufcg.dsc.Constants;

public class SafeCompositionResult extends AbstractResult {

	private Set<Hashtable<String, String>> listProducts;
	private int totalErrors;
	private long timeAlloy;
	private long timeParse;
	private long timeSave;
	private long timeTmp;
	private String output;

	public SafeCompositionResult(boolean result, long startedTime,
			long finishedTime, long Alloytime,
			Set<Hashtable<String, String>> products, long timeParse,
			long timeSave, long timeTmp, String output) {
		listProducts = products;
		totalErrors = listProducts.size();
		analysisResult = result;
		this.startedTime = startedTime;
		this.finishedTime = finishedTime;
		timeAlloy = Alloytime;
		this.timeParse = timeParse;
		this.timeSave = timeSave;
		this.timeTmp = timeTmp;
		this.output = output;
	}

	public Set<Hashtable<String, String>> getListProducts() {
		return listProducts;
	}

	public int getTotalErrors() {
		return totalErrors;
	}

	public long getTimeAlloy() {
		return timeAlloy;
	}

	public long getTimeParse() {
		return timeParse;
	}

	public long getTimeSave() {
		return timeSave;
	}

	public long getTimeTmp() {
		return timeTmp;
	}

	public String getOutput() {
		return output;
	}

	public String getLog() {
		String log = "";
		log += "total time: " + finishedTime + " ms" + Constants.LINE_SEPARATOR;
		log += "alloy time: " + timeAlloy + " ms" + Constants.LINE_SEPARATOR;
		log += "parsing time: " + timeParse + " ms" + Constants.LINE_SEPARATOR;
		log += "saving time: " + timeSave + " ms" + Constants.LINE_SEPARATOR;
		log += "total of products that break: " + listProducts.size()
				+ Constants.LINE_SEPARATOR;
		log += "list of products: " + listProducts + Constants.LINE_SEPARATOR;
		return log;
	}

	public String toString() {
		return getOutput() + Constants.LINE_SEPARATOR + getLog();
	}

}
