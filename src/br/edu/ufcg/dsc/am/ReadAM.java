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

import br.edu.ufcg.dsc.Constants;

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
				
				if (!path.startsWith(Constants.FILE_SEPARATOR)) {
					path = Constants.FILE_SEPARATOR + path;
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
				+ Constants.LINE_SEPARATOR;
		return sig;
	}

//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		String fullpath = "/Users/leopoldoteixeira/Documents/CIn/workspaces/msc/ck/samples/MobileMedia_02AO/src/";
//		String cm = "/Users/leopoldoteixeira/Documents/CIn/workspaces/msc/ck/samples/MobileMedia_02AO/componentModel_02AO.txt";
//		ReadCM.readCM(cm, fullpath);
//	}

}
