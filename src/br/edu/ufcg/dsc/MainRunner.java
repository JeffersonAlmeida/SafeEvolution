package br.edu.ufcg.dsc;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br 
 */

import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.testing.ITestHarness;
import org.eclipse.ui.testing.TestableObject;

import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.builders.ProductGenerator;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.dsc.evaluation.Avaliador;
import br.edu.ufcg.dsc.gui.AppWindow;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.saferefactor.core.Criteria;

public class MainRunner implements IPlatformRunnable, ITestHarness {

	private TestableObject testableObject;
	private String[] args;

	public Object run(Object args) throws Exception {
		this.args = (String[]) args;
		testableObject = PlatformUI.getTestableObject();
		testableObject.setTestHarness(this);

		Display display = PlatformUI.createDisplay();

		try {

			PlatformUI.createAndRunWorkbench(display, new NullAdvisor());

			AppWindow refinementChecker = new AppWindow(
					"LPS Refinements Checker");

			refinementChecker.open();

			Shell shell = refinementChecker.getShell();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			return EXIT_OK;

		} finally {
			display.dispose();
		}
	}

	public void runTests() {
		testableObject.testingStarting();
		testableObject.runTest(new Runnable() {
			public void run() {

				ProductGenerator.MAX_TENTATIVAS = 2000;
				
				Avaliador avaliador = new Avaliador();
				String evolution = "Teste";
				String source = "/home/jefferson/Dropbox/101Companies/SecondCategory/ToySPL02";
				String target = "/home/jefferson/Dropbox/101Companies/SecondCategory/ToySPL02Teste";
				try {
					System.out.println("### ONLY_CHANGED_CLASSES ###");
					avaliador
							.avalie(evolution,
									Lines.DEFAULT, // Escolha de qual linha de produtos ser� avaliada.  Mobile Media, Default ou Target.
									source,  // SPL Original  (SPL)
									target,  // Evolu��o/refactoring da SPL.   (SPL')
									120,  // timeOut
									4,   // A quantidade de testes que ser� gerada para cada m�todo.
									Approach.IC, // A abordagem que ser� utilizada
									false, // A SPL Source possui aspectos.
									false,  // A SPL Target possui aspectos.
									null, // String: controladores fachadas.
									Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT,  // Qual o crit�rio. // ONLY_COMMON_METHODS_SUBSET_DEFAULT
									CKFormat.HEPHAESTUS, // Qual o formato do CK da LPS Original.
									CKFormat.HEPHAESTUS, //  Qual o formato do CK da LPS Target.
									AMFormat.HEPHAESTUS, //  Qual o formato do AM da LPS Original.
									AMFormat.HEPHAESTUS,  //  Qual o formato do AM da LPS Target.
									source+"/lib",target+"/lib"); // Uma sequencia de bibliotecas (String)
				} catch (DirectoryException e) {
					System.out.println(e.getMessage()+ "\n\n\n\n");
					e.printStackTrace();
				}
			}
		});
		testableObject.testingFinished();
	}
	
	// }

	private void printUsage() {
		System.out.println("Usage: RefactoringTestPlugin.MainRunner [-list] [-runall] [-[-]h[elp]] TestClass [TestClass ...]");
	}

	public static class NullAdvisor extends WorkbenchAdvisor {
		@Override
		public String getInitialWindowPerspectiveId() {
			return null;
		}
	}
}
