package safeEvolution.inputFiles.xml;

import java.io.File;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlReader {
	
	public static String getAverageOfCoverage(String html){
	      File input = new File(html);
	      Document doc;
	      double soma = 0.0, average = 0;
	      int qtd = 0;
	      try {
	    	  doc = Jsoup.parse(input, "UTF-8", "http://example.com/");
	    	  Elements tableElements = doc.select("table");
	          Elements tableRowElements = tableElements.select("td");
	          for (int i = 0; i < tableRowElements.size(); i++) {
	            Element row = tableRowElements.get(i);
	            Elements rowItems = row.select("td");
	            for (int j = 0; j < rowItems.size(); j++) {
	               String p = rowItems.get(j).text(); 
	               if(p.contains("%")){
	            	   System.out.println("Coverage: " + p);
	            	   String [] s = p.split("%");
	            	   double v = Double.parseDouble(s[0]);
	            	   soma += v; 
	            	   qtd++;
	               }
	            }
	         }
	         average = soma/qtd;
	         System.out.println("Coverage Average: " + average);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return average+"";
	}

	public static void main(String[] args) {
	      String html = "/media/jefferson/Expansion Drive/ExecutionReport/branch23.0/EIC/evosuite-report/report-generation.html";
	      HtmlReader.getAverageOfCoverage(html);
	 }

}
