package br.edu.ufcg.dsc;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.eclipse.core.runtime.IPlatformRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.testing.ITestHarness;
import org.eclipse.ui.testing.TestableObject;
import safeEvolution.fileProperties.FilePropertiesObject;
import safeEvolution.fileProperties.FilePropertiesReader;
import br.edu.ufcg.dsc.builders.ProductGenerator;
import br.edu.ufcg.dsc.ck.xml.Xml;
import br.edu.ufcg.dsc.gui.AppWindow;
import br.edu.ufcg.dsc.util.AssetNotFoundException;
import br.edu.ufcg.dsc.util.DirectoryException;
import edu.mit.csail.sdg.alloy4.Err;

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
				long startTime = System.currentTimeMillis();
				ProductGenerator.MAX_TENTATIVAS = 2000;
				
				/* arguments */
				String source = "/home/jefferson/Desktop/workspace/Bank1.0/";  // /media/jefferson/Expansion Drive/targetWorkspace/TaRGeT/branches/branch3.0/
				String target = "/home/jefferson/Desktop/workspace/Bank1.1/";
				String stringFile = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/inputFiles/severalFiles.xml";
				int pairsAmount = 5;
				
				/* Create a TaRGeT evolution pair with Python Script and Run */
				createPairs(pairsAmount); 
				
				/* Create an evolution pair with directory Source and Target and Run */
				//onePairInput(source, target);
				
				/* Create several evolution pairs with this file and Run */
				//severalPairsInput(stringFile);
				long stopTime = System.currentTimeMillis();
				long elapsedTime = stopTime - startTime;
				System.out.println("\nTotal Time Spent: " + elapsedTime/60000 + " minutes");
				
			}

			private void severalPairsInput(String stringFile) {
				Xml xml = new Xml(stringFile, "evolutionPairs");
				for (Xml pair : xml.children("pair")) {
					String source = pair.child("source").content().toLowerCase();
					String target = pair.child("target").content().toLowerCase();
					System.out.println("Source: " + source);
					System.out.println("Target: " + target );
					onePairInput(source, target);
				}
			}

			private void onePairInput(String source, String target) {
				String stringFile = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/inputFiles/branchTemplate.properties";
				String array[] = source.split("/");
				String evolutionDescription = array[array.length-1];
				FilePropertiesReader propertiesReader = new FilePropertiesReader(stringFile);
				FilePropertiesObject input = propertiesReader.getPropertiesObject();
				input.setSourceLineDirectory(source);
				input.setTargetLineDirectory(target);
				input.setArtifactsSourceDir(source+ "src/TaRGeT Hephaestus/");
				input.setArtifactsTargetDir(target+ "src/TaRGeT Hephaestus/");
				input.setEvolutionDescription(evolutionDescription);
				System.out.println(input);
				ToolCommandLine toolCommandLine = new ToolCommandLine(input.getLine());
				try {
					toolCommandLine.commonInfoBetweenApproaches(input);
				} catch (Err e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (AssetNotFoundException e) {
					e.printStackTrace();
				} catch (DirectoryException e) {
					e.printStackTrace();
				}
				runApproach(input,toolCommandLine);
			}

			private void runApproach(FilePropertiesObject input, ToolCommandLine toolCommandLine) {
				ArrayList<Approach> approaches = new ArrayList<Approach>();
				ArrayList<String> tool = new ArrayList<String>();
				approaches.add(Approach.IC);approaches.add(Approach.EIC);
				tool.add("evosuite");tool.add("randoop");
				for(int i = 0; i < approaches.size(); i++){
					for(int j = 0; j< tool.size(); j++){
						System.out.println("\n Run tool for approach: " +  approaches.get(i) + " and tool: " + tool.get(j) );
						input.setApproach(approaches.get(i));
						input.setGenerateTestsWith(tool.get(j));
						try {
							toolCommandLine.runApproach(input);
							System.out.println("\n\t SPL REPORT: \n");
							// write approach plus tool in property file 
							toolCommandLine.persitResultsInPropertyFile(input);
						} catch (AssetNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (DirectoryException e) {
							e.printStackTrace();
						}
					}
				}
				// write pair results in spreadsheet
				toolCommandLine.writeResultsInSpreadSheet();
			}

			private void manipulateFileProperty(String stringFile, String approach, String tool) {
				Properties propertyFile = new Properties();
				try {
					InputStream is = new FileInputStream(stringFile);
					propertyFile.load(is);
					propertyFile.setProperty("generateTestsWith", tool); 
					propertyFile.setProperty("approach", approach); 
					is.close();
					OutputStream os = new FileOutputStream(stringFile);
					propertyFile.store(os, "changing variables");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
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

			private void createPairs(int pairsAmount){
				System.out.println("\nCall Python in java");
				String s = null; String line = "";
				String strFile = "/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT/lastBranch"; 
				try {
					BufferedReader br = new BufferedReader(new FileReader(strFile));
					line = br.readLine();
					System.out.println("line: " + line);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		        callPython(pairsAmount);
		        String branches = "/media/jefferson/Expansion Drive/targetWorkspace/TaRGeT/branches/";
		        int init = Integer.parseInt(line)+1;
		        for(int i = init; i < (init+pairsAmount) ; i++){
		        	String source = branches + "branch" + i + ".0/";
		        	String target = branches + "branch" + i + ".1/";
		        	onePairInput(source, target);
		        }
		    }

			private void callPython(int pairsAmount) {
				String s = null;
				try {
		        	String [] cmdArray = new String[3]; 
					cmdArray[0] = "python";
					cmdArray[1] = "/media/jefferson/Expansion Drive/workspace/ferramentaLPSSM/src/safeEvolution/python/script/command.py";
					cmdArray[2] = Integer.toString(pairsAmount);
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
