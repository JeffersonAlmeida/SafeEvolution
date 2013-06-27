package br.edu.ufcg.dsc;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.testing.ITestHarness;
import org.eclipse.ui.testing.TestableObject;
import safeEvolution.fileProperties.FilePropertiesObject;
import safeEvolution.fileProperties.FilePropertiesReader;
import edu.mit.csail.sdg.alloy4.Err;
import br.edu.ufcg.dsc.builders.ProductGenerator;
import br.edu.ufcg.dsc.evaluation.Analyzer;
import br.edu.ufcg.dsc.gui.AppWindow;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;

public class MainRunner implements IPlatformRunnable, ITestHarness {

	private TestableObject testableObject;
	private String[] arguments;

	public Object run(Object args) throws Exception {
		this.setArguments((String[]) args);
		testableObject = PlatformUI.getTestableObject();
		testableObject.setTestHarness(this);
		Display display = PlatformUI.createDisplay();
		try {
			// delete generated JavaProjects in Eclipse Run Configuration Directory
			String [] cmdArray = new String[3]; 
			cmdArray[0] = "rm";
			cmdArray[1] = "-rf";
			cmdArray[2] = "/media/jefferson/Expansion Drive/runtime-ferramentaLPS.MainRunner/"; 
			Runtime rt = Runtime.getRuntime(); 
			Process proc = rt.exec(cmdArray);
			System.out.println("SPL Refactoring Checker");
			PlatformUI.createAndRunWorkbench(display, new NullAdvisor());
			AppWindow refinementChecker = new AppWindow("Software Product Line Refinement Checker");
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
		System.out.println("... Checking Start");
		
		testableObject.runTest(new Runnable() {
			public void run() {
				
				ProductGenerator.MAX_TENTATIVAS = 2000;
				// Create an evolution pair with Python Script 
				int numberOfEvolutionPairs = 1;
				int branchNumber = 50; 
				//createEvolutionPair(numberOfEvolutionPairs,branchNumber);
				
				// Create Property File for the Evolution Pair created above
				//createInputFile(numberOfEvolutionPairs,branchNumber);
				
				FilePropertiesReader propertiesReader = new FilePropertiesReader("/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/inputFiles/branch" + branchNumber +".properties");
				FilePropertiesObject propertiesObject = propertiesReader.getPropertiesObject();
				System.out.println(propertiesObject);
				try {
					Analyzer.getInstance().analize(propertiesObject);
				} catch (DirectoryException e) {
					e.printStackTrace(); 
				} catch (Err e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (AssetNotFoundException e) {
					e.printStackTrace();
				}
			}

			private void createInputFile(int numberOfEvolutionPairs, int branchNumber) {
				int count = branchNumber;
				int size = branchNumber + numberOfEvolutionPairs;
				String dir = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/inputFiles/branchTemplate.properties";
				while(count < size){
					String newDirSource = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/inputFiles/branch" + count + ".properties";
					Properties properties = new Properties();
					InputStream is;
					try {
						is = new FileInputStream(dir);
						properties.load(is);	
						String branchName = "branch" + count;
						String directorySource = "/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT/branches/branch"+ count + ".0/";
						String directoryTarget = "/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT/branches/branch"+ count + ".1/";
						String artifactsSourceDir = "/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT/branches/branch"+ count + ".0/" + "src/TaRGeT Hephaestus/";
						String artifactsTargetDir = "/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT/branches/branch"+ count + ".1/" + "src/TaRGeT Hephaestus/";
						properties.setProperty("evolutionDescription", branchName); 
						properties.setProperty("sourceLineDirectory", directorySource); 
						properties.setProperty("targetLineDirectory", directoryTarget);
						properties.setProperty("artifactsSourceDir", artifactsSourceDir);
						properties.setProperty("artifactsTargetDir", artifactsTargetDir);
						is.close();
						OutputStream os = new FileOutputStream(newDirSource);
						properties.store(os, "changing variable");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					count++;
				}
			}

			private void createEvolutionPair(int numberOfEvolutionPairs, int branchNumber) {
				System.out.println("\nCall Python in java");
				String s = null;
		        try {
		        	String [] cmdArray = new String[4]; 
					cmdArray[0] = "python";
					cmdArray[1] = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/src/safeEvolution/python/script/command.py";
					cmdArray[2] = Integer.toString(numberOfEvolutionPairs);
					cmdArray[3] = Integer.toString(branchNumber);
					Runtime rt = Runtime.getRuntime(); 
					Process p = rt.exec(cmdArray);
		            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		            // read the output from the command
		            System.out.println("Here is the standard output of the command:\n");
		            while ((s = stdInput.readLine()) != null) {
		                System.out.println(s);
		            }
		            // read any errors from the attempted command
		            System.out.println("Here is the standard error of the command (if any):\n");
		            while ((s = stdError.readLine()) != null) {
		                System.out.println(s);
		            }
		        }
		        catch (IOException e) {
		            System.out.println("exception happened: ");
		            e.printStackTrace();
		        }
		    }
			
		});
		testableObject.testingFinished();
	}
	
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}
	public String[] getArguments() {
		return arguments;
	}
	public static class NullAdvisor extends WorkbenchAdvisor {
		@Override
		public String getInitialWindowPerspectiveId() {
			return null;
		}
	}
	
}
