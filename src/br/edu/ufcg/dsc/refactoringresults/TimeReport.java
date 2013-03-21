package br.edu.ufcg.dsc.refactoringresults;

import java.util.ArrayList;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.util.FileManager;

public class TimeReport {

	private String text;
	private ArrayList<String> extras;
	private long time;
	private static long total = 0;

	public TimeReport(String t, long ti) {
		text = t;
		time = ti;
		extras = new ArrayList<String>();
		total = total + ti;
	}

	public String toString() {
		String out =  text + " " + time + " (ms)" + Constants.LINE_SEPARATOR;
		for (String s : extras) {
			out += s + Constants.LINE_SEPARATOR;
		}
		return out;
	}

	public void saveReport(int counter) {
		FileManager.getInstance().createFile(
				Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR
						+ "TimeReport" + counter + ".txt", toString());
		System.out.println(toString());
	}

	public static long getTotal() {
		return total;
	}
	
	public void addExtra(String t){
		extras.add(t);
	}
}
