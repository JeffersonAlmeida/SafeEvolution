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

				try {
					System.out.println("### ONLY_CHANGED_CLASSES ###");
					avaliador
							.avalie(
									Lines.DEFAULT, // Escolha de qual linha de produtos será avaliada.  Mobile Media, Default ou Target.
									"D:\\101Companies\\FirstCategory\\1011S",  // SPL Original  (SPL)
									"D:\\101Companies\\FirstCategory\\1011T",  // Evolução/refactoring da SPL.   (SPL')
									60,  // timeOut
									4,   // A quantidade de testes que será gerada para cada método.
									Approach.NAIVE_1_APROXIMACAO, // A abordagem que será utilizada
									true, // A SPL Source possui aspectos.
									true,  // A SPL Target possui aspectos.
									null, // String: controladores fachadas.
									Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT,  // Qual o critério.
									CKFormat.HEPHAESTUS, // Qual o formato do CK da LPS Original.
									CKFormat.HEPHAESTUS, //  Qual o formato do CK da LPS Target.
									AMFormat.SIMPLE, //  Qual o formato do AM da LPS Original.
									AMFormat.SIMPLE,  //  Qual o formato do AM da LPS Target.
									"D:\\101Companies\\FirstCategory\\1011S\\lib", "D:\\101Companies\\FirstCategory\\1011T\\lib"); // Uma sequência de bibliotecas (String)
				} catch (DirectoryException e) {
					System.out.println(e.getMessage()+ "\n\n\n\n");
					e.printStackTrace();
				}
				
				/*try {
					System.out.println("### IMPACTED_FEATURES ###");
					avaliador
							.avalie(
									Lines.DEFAULT, // Escolha de qual linha de produtos será avaliada.  Mobile Media, Default ou Target.
									"C:/WorkspaceSPL/MiniSPL",  // SPL Original  (SPL)
									"C:/WorkspaceSPL/MiniSPL_R02",  // Evolução/refactoring da SPL.   (SPL')
									60,  // timeOut
									4,   // A quantidade de testes que será gerada para cada método.
									Approach.IMPACTED_FEATURES, // A abordagem que será utilizada
									true, // A SPL Source possui aspectos.
									true,  // A SPL Target possui aspectos.
									null, // String: controladores fachadas.
									Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT,  // Qual o critério.
									CKFormat.HEPHAESTUS, // Qual o formato do CK da LPS Original.
									CKFormat.HEPHAESTUS, //  Qual o formato do CK da LPS Target.
									AMFormat.SIMPLE, //  Qual o formato do AM da LPS Original.
									AMFormat.SIMPLE,  //  Qual o formato do AM da LPS Target.
									"C:/WorkspaceSPL/MiniSPL/lib", "C:/WorkspaceSPL/MiniSPL_R02/lib"); // Uma sequência de bibliotecas (String)
				} catch (DirectoryException e) {
					System.out.println(e.getMessage()+ "\n\n\n\n");
					e.printStackTrace();
				}
				
				
				try {
					System.out.println("### NAIVE_1_APROXIMACAO ###");
					avaliador
							.avalie(
									Lines.DEFAULT, // Escolha de qual linha de produtos será avaliada.  Mobile Media, Default ou Target.
									"C:/WorkspaceSPL/MiniSPL",  // SPL Original  (SPL)
									"C:/WorkspaceSPL/MiniSPL_R02",  // Evolução/refactoring da SPL.   (SPL')
									60,  // timeOut
									4,   // A quantidade de testes que será gerada para cada método.
									Approach.NAIVE_1_APROXIMACAO, // A abordagem que será utilizada
									true, // A SPL Source possui aspectos.
									true,  // A SPL Target possui aspectos.
									null, // String: controladores fachadas.
									Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT,  // Qual o critério.
									CKFormat.HEPHAESTUS, // Qual o formato do CK da LPS Original.
									CKFormat.HEPHAESTUS, //  Qual o formato do CK da LPS Target.
									AMFormat.SIMPLE, //  Qual o formato do AM da LPS Original.
									AMFormat.SIMPLE,  //  Qual o formato do AM da LPS Target.
									"C:/WorkspaceSPL/MiniSPL/lib", "C:/WorkspaceSPL/MiniSPL_R02/lib"); // Uma sequência de bibliotecas (String)
				} catch (DirectoryException e) {
					System.out.println(e.getMessage()+ "\n\n\n\n");
					e.printStackTrace();
				}
				

				
				try {
					System.out.println("### NAIVE_2_ICTAC ###");
					avaliador
							.avalie(
									Lines.DEFAULT, // Escolha de qual linha de produtos será avaliada.  Mobile Media, Default ou Target.
									"C:/WorkspaceSPL/MiniSPL",  // SPL Original  (SPL)
									"C:/WorkspaceSPL/MiniSPL_R02",  // Evolução/refactoring da SPL.   (SPL')
									60,  // timeOut
									4,   // A quantidade de testes que será gerada para cada método.
									Approach.NAIVE_2_ICTAC, // A abordagem que será utilizada
									true, // A SPL Source possui aspectos.
									true,  // A SPL Target possui aspectos.
									null, // String: controladores fachadas.
									Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT,  // Qual o critério.
									CKFormat.HEPHAESTUS, // Qual o formato do CK da LPS Original.
									CKFormat.HEPHAESTUS, //  Qual o formato do CK da LPS Target.
									AMFormat.SIMPLE, //  Qual o formato do AM da LPS Original.
									AMFormat.SIMPLE,  //  Qual o formato do AM da LPS Target.
									"C:/WorkspaceSPL/MiniSPL/lib", "C:/WorkspaceSPL/MiniSPL_R02/lib"); // Uma sequência de bibliotecas (String)
				} catch (DirectoryException e) {
					System.out.println(e.getMessage()+ "\n\n\n\n");
					e.printStackTrace();
				}*/
				


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
