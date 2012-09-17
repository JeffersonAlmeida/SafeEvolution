package br.edu.ufcg.dsc;

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

				try {
					//   C:/ProductsLines/motivating_source
					avaliador
							.avalie(
									Lines.DEFAULT,
									"C:/WorkspaceSPL/MiniSPL", //  -> /home/felype/Desktop/Exemplos/motivating_source
									"C:/WorkspaceSPL/MiniSPL_R02", //  -> /home/felype/Desktop/Exemplos/motivating_target_1
									60,
									4,
									Approach.NAIVE_1_APROXIMACAO,
									true,
									true,
									null,
									Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT,
									CKFormat.SIMPLE, CKFormat.SIMPLE,
									AMFormat.SIMPLE, AMFormat.SIMPLE,
									"C:/WorkspaceSPL/MiniSPL/lib"); //   ->   /home/felype/Desktop/Exemplos/motivating_source/lib

				} catch (DirectoryException e) {
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
