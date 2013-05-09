package br.edu.ufcg.dsc.am;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;


/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br  <br></br>
 * This class is responsible to read the asset map.
 */
public class ReadAM {

	public static HashMap<String, String> readAM(String cmFile/*, String fullpath*/) {
		
		HashMap<String, String> assetMapping = new HashMap<String, String>();
		List<String> lines = null;
		File cmXML = new File(cmFile);
		try {
			lines = FileUtils.readLines(cmXML);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String line : lines) {
//			System.out.println(line);
			if(line.contains("=")){
				String[] parts = line.split("=>");
				String constant = parts[0].trim();
				String path = parts[1].replaceFirst(";", "").trim();
				
				if (!path.startsWith(br.edu.ufcg.dsc.Constants.FILE_SEPARATOR)) {
					path = br.edu.ufcg.dsc.Constants.FILE_SEPARATOR + path;
				}
				
				assetMapping.put(constant, /*fullpath + */path);
			}
		}
		
		// System.out.println(cm);
		return assetMapping;
	}

	public static Set<String> components(Map<String, String> cm) {
		Set<String> components = new HashSet<String>();
		for (Entry<String, String> entry : cm.entrySet()) {
			// System.out.println(entry.getKey() + "/" + entry.getValue());
			String[] valueTmp = entry.getValue().split("/");
			String value = valueTmp[valueTmp.length - 1];
			int pos = value.indexOf(".");
			value = value.substring(0, pos).trim();
			// System.out.println(value);
			components.add(value);
		}
		return components;
	}

	public static String componentsAlloy(Map<String, String> cm) {
		Set<String> components = ReadAM.components(cm);
		String sig = "";
		for (String component : components) {
			sig += component + ",";
		}
		sig = " one sig " + sig.substring(0, sig.length() - 1) + " in Bool{}"
				+ br.edu.ufcg.dsc.Constants.LINE_SEPARATOR;
		return sig;
	}
}
