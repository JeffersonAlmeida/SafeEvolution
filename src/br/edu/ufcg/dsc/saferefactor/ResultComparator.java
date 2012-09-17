package br.edu.ufcg.dsc.saferefactor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import br.edu.ufcg.dsc.Constants;

public class ResultComparator {

	// private static final String RESULT_FAIL = "FAIL";

	public static boolean hasSameBehavior(String source, String target) {
		boolean result = true;

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		dbf.setNamespaceAware(false);

		DocumentBuilder docBuilder;
		try {
			docBuilder = dbf.newDocumentBuilder();
			Document docSource = docBuilder.parse(new File(source
					+ Constants.FILE_SEPARATOR + "TEST-RandoopTest.xml"));

			Document docTarget = docBuilder.parse(new File(target
					+ Constants.FILE_SEPARATOR + "TEST-RandoopTest.xml"));

			System.out.println(getChanges(docSource, docTarget));

			result = !hasChanges(docSource, docTarget);

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			result = false;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			result = false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			result = false;
		}
		return result;
	}

	private static String getChanges(Document source, Document target) {
		StringBuilder changes = new StringBuilder();

		Map<String, TestCaseState> sourceMap = buildStateMap(source);
		Map<String, TestCaseState> targetMap = buildStateMap(target);

		for (String key : sourceMap.keySet()) {
			TestCaseState sourceState = sourceMap.get(key);

			if (!targetMap.containsKey(key)) {
				changes.append("Target tests does not contains " + key + "\n");
			}

			TestCaseState targetState = targetMap.get(key);

			if (sourceState != targetState) {
				changes.append(key + " in source is " + sourceState
						+ " while in target is " + targetState + "\n");
			}
		}

		return changes.toString();
	}

	private static boolean hasChanges(Document source, Document target) {
		Map<String, TestCaseState> sourceMap = buildStateMap(source);
		Map<String, TestCaseState> targetMap = buildStateMap(target);

		if (sourceMap.size() != targetMap.size()) {
			return true;
		}

		for (String key : sourceMap.keySet()) {
			TestCaseState sourceState = sourceMap.get(key);

			if (!targetMap.containsKey(key)) {
				return true;
			}

			TestCaseState targetState = targetMap.get(key);

			if (sourceState != targetState) {
				return true;
			}
		}

		return false;
	}

	private static Map<String, TestCaseState> buildStateMap(Document source) {
		Map<String, TestCaseState> stateMap = new HashMap<String, TestCaseState>();
		NodeList list = source.getDocumentElement().getElementsByTagName(
				"testcase");

		for (int i = 0; i < list.getLength(); i++) {
			Element testcase = (Element) list.item(i);
			String tcName = testcase.getAttribute("classname") + "."
					+ testcase.getAttribute("name");

			boolean hasProblems = false;
			if (testcase.hasChildNodes()) {
				NodeList subNodes = testcase.getChildNodes();
				for (int j = 0; j < subNodes.getLength(); j++) {
					if (subNodes.item(j) instanceof Element) {
						Element problem = (Element) subNodes.item(j);
						if (problem.getTagName().equals("error")) {
							hasProblems = true;
							stateMap.put(tcName, TestCaseState.ERROR);
						} else if (problem.getTagName().equals("failure")) {
							hasProblems = true;
							stateMap.put(tcName, TestCaseState.FAILURE);
						}
					}
				}
			}

			if (!hasProblems) {
				stateMap.put(tcName, TestCaseState.SUCCESS);
			}
		}

		return stateMap;
	}

	private static enum TestCaseState {
		SUCCESS, FAILURE, ERROR
	}

}
