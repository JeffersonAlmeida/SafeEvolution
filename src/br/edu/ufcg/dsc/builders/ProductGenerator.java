package br.edu.ufcg.dsc.builders;

import java.util.HashSet;
import java.util.Iterator;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.util.Comparador;
import edu.mit.csail.sdg.alloy4.A4Reporter;
import edu.mit.csail.sdg.alloy4.Err;
import edu.mit.csail.sdg.alloy4.ErrorWarning;
import edu.mit.csail.sdg.alloy4.SafeList;
import edu.mit.csail.sdg.alloy4compiler.ast.Command;
import edu.mit.csail.sdg.alloy4compiler.ast.Module;
import edu.mit.csail.sdg.alloy4compiler.ast.Sig;
import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Options;
import edu.mit.csail.sdg.alloy4compiler.translator.A4Solution;
import edu.mit.csail.sdg.alloy4compiler.translator.TranslateAlloyToKodkod;

public class ProductGenerator {

	/***/
	private static final int TOTAL_UNUSED_SIGS = 10;
	
	/***/
	private static final int LAST_CHECKED_POSITION = 3;
	
	/***/
	public static int MAX_TENTATIVAS = 100000;

	/***/
	private HashSet<HashSet<String>> products;
	
	/***/
	Iterator<HashSet<String>> iterator;

	
	public ProductGenerator() {
		products = new HashSet<HashSet<String>>();
	}

	public void generateProductsInstance(String sourceAlloy) {
		try {
			generateProduct(sourceAlloy + Constants.ALLOY_EXTENSION);
		} catch (Err e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is the method responsible to generate a product given an Alloy File.
	 * @param file Alloy File.
	 * @throws Err
	 */
	private void generateProduct(String file) throws Err {

		/*System.out.println("\nIt is possible to generate: " + this.products.size() + " products."); */
		
		/* Alloy4 sends diagnostic messages and progress reports to the
		 A4Reporter. By default, the A4Reporter ignores all these events (but you can extend the A4Reporter to display the event for the user) */
		A4Reporter rep = new A4Reporter() {
			/* For example, here we choose to display each "warning" by printing it to System.out */
			@Override
			public void warning(ErrorWarning msg) {
				System.out.print("Relevance Warning:" + Constants.LINE_SEPARATOR + (msg.toString().trim()) + Constants.LINE_SEPARATOR + Constants.LINE_SEPARATOR);
				System.out.flush();
			}
		};
		/* Parse + Type Check the Model */
		Module world = CompUtil.parseEverything_fromFile(rep, null, file);

		/* Choose some default options for how you want to execute the commands */
		A4Options options = new A4Options();
		/* SAT4J using native Java */
		options.solver = A4Options.SatSolver.SAT4J;

		for (Command command : world.getAllCommands()) {
			A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
			/* Apenas evitando o loop infinito. Workaound */
			int i = 0;
			
			while (i++ < MAX_TENTATIVAS) {
				if(i%100 == 0){
					/* It is not necessary to print the amount of products. */
					/*System.out.println(i);
					System.out.println(this.products.size());*/
				}
				
				HashSet<String> output = new HashSet<String>();
				SafeList<Sig> allReachableSigs = ans.getAllReachableSigs();
				
				
				int counter = 1;
				for (Sig sig : allReachableSigs) {
					if (counter >= TOTAL_UNUSED_SIGS) {
						String[] values = sig.toString().split("/");
						String sigName = sig.toString().split("/")[values.length - 1];
						String[] evaluation = ans.eval(sig).toString().split("/");
						boolean value = getValue(evaluation[evaluation.length - 1]);
						if (value) {
							output.add(sigName);
						}
					}
					counter++;
				}
				
				if (output.size() != 0) {
						products.add(output);
				}
				A4Solution next = ans.next();
				if (next.equals(ans)) {
					break;
				}
				ans = next;
			} /*While End*/
		} /*For End*/
	} /*Method End*/

	private boolean inside(HashSet<String> output) {
		for (HashSet<String> set : products) {
			if (Comparador.equalSets(set, output)) {
				return true;
			}
		}
		return false;
	}

	private boolean getValue(String string) {
		if (string.length() <= 2) {
			return false;
		}
		String temp = "";
		for (int i = 0; i <= LAST_CHECKED_POSITION; i++) {
			temp += string.charAt(i);
		}
		if (temp.equals("True")) {
			return true;
		}
		return false;
	}

	public HashSet<HashSet<String>> getProducts() {
		return products;
	}

	public int totalOfGeneratedProducts() {
		return products.size();
	}

//	public static void main(String[] args) throws Err {
//		ProductGenerator p = new ProductGenerator();
//		p
//				.generateProductsInstance("/Users/Solon/Documents/Universidade/IC/teste.als");
//	}
}