package br.edu.ufcg.dsc.refactoringresults;

import java.util.ArrayList;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.util.FileManager;

public class CompleteReport {

	private ArrayList<TimeReport> reports;

	public CompleteReport() {
		reports = new ArrayList<TimeReport>();
	}

	public String toString() {
		StringBuilder output = new StringBuilder("====Begin of Report===="
				+ Constants.LINE_SEPARATOR);
		for (TimeReport report : reports) {
			output.append(report.toString() + Constants.LINE_SEPARATOR);
		}
		output.append("====End of Report====" + Constants.LINE_SEPARATOR);
		return output.toString();
	}

	public void saveReport(int counter) {
		FileManager.getInstance().createFile(
				Constants.PRODUCTS_DIR + Constants.FILE_SEPARATOR
						+ "RefactoringReport" + counter + ".txt", toString());
	}
}
