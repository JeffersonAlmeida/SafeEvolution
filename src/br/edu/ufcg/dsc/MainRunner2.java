package br.edu.ufcg.dsc;

import br.edu.ufcg.dsc.am.AMFormat;
import br.edu.ufcg.dsc.builders.ProductGenerator;
import br.edu.ufcg.dsc.ck.CKFormat;
import br.edu.ufcg.dsc.evaluation.Avaliador;
import br.edu.ufcg.dsc.util.DirectoryException;
import br.edu.ufcg.saferefactor.core.Criteria;

public class MainRunner2 {

	public static void main(String[] args) {
		
		ProductGenerator.MAX_TENTATIVAS = 2000;
		Avaliador avaliador = new Avaliador();

		try {
			System.out.println("### ONLY_CHANGED_CLASSES ###");
			avaliador
					.avalie(
							Lines.DEFAULT, // Escolha de qual linha de produtos será avaliada.  Mobile Media, Default ou Target.
							"D:\\101Companies\\SecondCategory\\littleSPL",  // SPL Original  (SPL)
							"D:\\101Companies\\SecondCategory\\littleSPLT",  // Evolução/refactoring da SPL.   (SPL')
							60,  // timeOut
							4,   // A quantidade de testes que será gerada para cada método.
							Approach.ONLY_CHANGED_CLASSES, // A abordagem que será utilizada
							true, // A SPL Source possui aspectos.
							true,  // A SPL Target possui aspectos.
							null, // String: controladores fachadas.
							Criteria.ONLY_COMMON_METHODS_SUBSET_DEFAULT,  // Qual o critério.
							CKFormat.HEPHAESTUS, // Qual o formato do CK da LPS Original.
							CKFormat.HEPHAESTUS, //  Qual o formato do CK da LPS Target.
							AMFormat.SIMPLE, //  Qual o formato do AM da LPS Original.
							AMFormat.SIMPLE,  //  Qual o formato do AM da LPS Target.
							"D:\\101Companies\\SecondCategory\\littleSPL\\lib", "D:\\101Companies\\SecondCategory\\littleSPLT\\lib"); // Uma sequência de bibliotecas (String)
		} catch (DirectoryException e) {
			System.out.println(e.getMessage()+ "\n\n\n\n");
			e.printStackTrace();
		}
	}
}


	
	
