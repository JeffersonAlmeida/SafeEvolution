package br.edu.ufcg.dsc.fm;

import br.edu.ufcg.dsc.Constants;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

public class FeatureModelRefactVerifier {

	private static FeatureModelRefactVerifier instance;

	private FeatureModelRefactVerifier() {
	}

	public static FeatureModelRefactVerifier getInstance() {
		if (instance == null) {
			instance = new FeatureModelRefactVerifier();
		}
		return instance;
	}

	public boolean isFMRefactoring(String file) throws Err {

		// Alloy4 sends diagnostic messages and progress reports to the
		// A4Reporter.
		// By default, the A4Reporter ignores all these events (but you can
		// extend the A4Reporter to display the event for the user)
		A4Reporter rep = new A4Reporter() {
			// For example, here we choose to display each "warning" by printing
			// it to System.out
			@Override
			public void warning(ErrorWarning msg) {
				System.out.print("Relevance Warning:"
						+ Constants.LINE_SEPARATOR + (msg.toString().trim())
						+ Constants.LINE_SEPARATOR + Constants.LINE_SEPARATOR);
				System.out.flush();
			}
		};

		// Parse+typecheck the model
		Module world = CompUtil.parseEverything_fromFile(rep, null, file);

		// Choose some default options for how you want to execute the commands
		A4Options options = new A4Options();
		options.solver = A4Options.SatSolver.SAT4J;

		for (Command command : world.getAllCommands()) {
			A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world
					.getAllReachableSigs(), command, options);
			// Print the outcome
			// If satisfiable...
			return !ans.satisfiable();
		}
		return false;
	}
}
