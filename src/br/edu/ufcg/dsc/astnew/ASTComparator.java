package br.edu.ufcg.dsc.astnew;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class ASTComparator {

	private IJavaProject javaProject;

	private IPackageFragmentRoot root;

	private IPackageFragment packageP;

	private ICompilationUnit compUnitA;

	private Set<String> asupertypes = new HashSet<String>();
	private Set<String> aSecondsupertypes = new HashSet<String>();

	private ICompilationUnit compUnitASecond;

	private String compareMessage;

	public IJavaProject setUpProject() throws ConfigurationException {
		try {
			javaProject = JavaProjectHelper.createJavaProject("JavaProject"
					+ System.currentTimeMillis(), "bin");
			JavaProjectHelper.addRTJar(javaProject);
			root = JavaProjectHelper.addSourceContainer(javaProject, "src");
			packageP = root.createPackageFragment("p", true, null);

		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		return javaProject;
	}

	public void tearDownProject() throws CoreException {
		if (javaProject.exists())
			JavaProjectHelper.delete(javaProject);
	}

	/**
	 * Assumes that the input files are text files containing Java source code.
	 * 
	 * @param fileA
	 * @param fileASecond
	 * @throws IOException
	 * @throws JavaModelException
	 */
	public void setInputs(File fileA, File fileASecond) throws IOException,
			JavaModelException {
		asupertypes = new HashSet<String>();
		aSecondsupertypes = new HashSet<String>();

		if (fileA.exists() && fileASecond.exists())
			setInputs(fileA.getName(), getContents(fileA),
					fileASecond.getName(), getContents(fileASecond));
		else
			throw new IOException("one of the input files does not exist");
	}

	public String getContents(File aFile) {
		StringBuffer contents = new StringBuffer();

		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(aFile));
			String line = null;
			while ((line = input.readLine()) != null) {
				contents.append(line);
				contents.append(System.getProperty("line.separator"));
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return contents.toString();
	}

	/**
	 * This assumes that the contents parameter contains java code for a
	 * compilation unit. Assumes also that the java text starts with declaration
	 * of a package p.
	 * 
	 * @param textA
	 * @param textB
	 * @throws JavaModelException
	 */
	public void setInputs(String nameCUA, String contentsA, String nameCUB,
			String contentsB) throws JavaModelException {

		try {
			String formattedCUA = formatCompilationUnit(contentsA);
			String formattedCUB = formatCompilationUnit(contentsB);

			compUnitA = parse(nameCUA, formattedCUA);
			// we need to get a working copy because the next parsing is going
			// to overwrite this compilation
			// unit in case that the names of the two CompUnits are the same
			compUnitA = compUnitA.getWorkingCopy(null);
			// try {
			// Thread.sleep(50);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			compUnitASecond = parse(nameCUB, formattedCUB);
			compUnitASecond = compUnitASecond.getWorkingCopy(null);
			compareMessage = "";
		} catch (JavaModelException e) {
			e.printStackTrace();
			throw e;
		}

	}

	public ASTComparator() {
		// TODO Auto-generated constructor stub
	}

	private ICompilationUnit parse(String nameCU, String contents)
			throws JavaModelException {
		ICompilationUnit cu = packageP.createCompilationUnit(nameCU, contents,
				true, new NullProgressMonitor());
		// cu.save(null, false);
		return cu;
	}
	
	// Kely July 09 commented the method and change it for this 
		public boolean compareIsomorphic() throws JavaModelException{		
			IType[] listTypesA = compUnitA.getAllTypes();
			IType[] listTypesB = compUnitASecond.getAllTypes();
			boolean differences = true;
			if (listTypesA.length != listTypesB.length){
				compareMessage += "One class has " + listTypesA.length + " subtypes and the other class has " + listTypesB.length + " subtypes";
				return false;
			}	
			else {
				for (int i = 0; i < listTypesA.length; i++) {
					IType typeA = listTypesA[i];
					IType typeB = null;				
					for (int j = 0; j < listTypesB.length; j++) {
						//System.out.println("typeA: "+ typeA.getElementName() + " typeB: "+ listTypesB[j].getElementName());
						if (typeA.getElementName().equals(listTypesB[j].getElementName())) {
							typeB = listTypesB[j];						
							break;
						}
					}
					if (typeB == null){
						compareMessage += "class " + typeA.getElementName() + " was not found"; 
						differences = false;
					} else if (!(compareFieldsOf(typeA, typeB) && compareMethodsOf(typeA, typeB))){
						System.out.println("second comparefield");
						differences = false;
					}			
					if ((!compareSuperTypes(typeA, typeB, listTypesA,
							listTypesB)) || !(compareType(typeA, typeB))) {
						differences = false;
					}
					
				}
				if (asupertypes.size() != aSecondsupertypes.size())
					differences = false;
			}
			return differences;
		}

	// Gustavo Soares: esse metodo compara as ASTs independente do nome das classes 
	public boolean compareIsomorphicWithSimetry() throws JavaModelException {
		IType[] listTypesA = compUnitA.getAllTypes();
		IType[] listTypesB = compUnitASecond.getAllTypes();
		boolean differences = true;
		if (listTypesA.length != listTypesB.length) {
			compareMessage += "One class has " + listTypesA.length
					+ " subtypes and the other class has " + listTypesB.length
					+ " subtypes";
			return false;
		} else {
			// int[] jaVistos = new int[listTypesB.length];
			List<Integer> jaVistos = new ArrayList<Integer>();
			for (int i = 0; i < listTypesA.length; i++) {
				IType typeA = listTypesA[i];
				IType typeB = null;
				for (int j = 0; j < listTypesB.length; j++) {
					if (jaVistos.contains(new Integer(j)))
						continue;
					// System.out.println("typeA: "+ typeA.getElementName() +
					// " typeB: "+ listTypesB[j].getElementName());
					// if
					// (typeA.getElementName().equals(listTypesB[j].getElementName()))
					// {
					typeB = listTypesB[j];
					// if (!(compareFieldsOf(typeA, typeB)) &&
					// (compareMethodsOf(typeA, typeB))){
					// System.out.println("second comparefield");
					// differences = false;
					// } else
					if ((!compareSuperTypes(typeA, typeB, listTypesA,
							listTypesB)) || !(compareType(typeA, typeB))) {
						differences = false;
					} else {
						differences = true;
						jaVistos.add(new Integer(j));
						break;
					}

					// break;
					// }
				}
				if (differences == false)
					break;
				// for (int j = 0; j < listTypesB.length; j++) {
				// //System.out.println("typeA: "+ typeA.getElementName() +
				// " typeB: "+ listTypesB[j].getElementName());
				// if
				// (typeA.getElementName().equals(listTypesB[j].getElementName()))
				// {
				// typeB = listTypesB[j];
				// break;
				// }
				// }
				// if (typeB == null){
				// compareMessage += "class " + typeA.getElementName() +
				// " was not found";
				// differences = false;
				// } else if (!(compareFieldsOf(typeA, typeB)) &&
				// (compareMethodsOf(typeA, typeB))){
				// System.out.println("second comparefield");
				// differences = false;
				// } else if ((!compareSuperTypes(typeA, typeB)) ||
				// !(compareType(typeA, typeB)))
				// differences = false;
			}
			if (asupertypes.size() != aSecondsupertypes.size())
				differences = false;
		}

		return differences;
	}

	private boolean compareType(IType typeA, IType typeASecond)
			throws JavaModelException {
		boolean result = true;

		if (typeA.isInterface() && !typeASecond.isInterface())
			result = false;
		if (!typeA.isInterface() && typeASecond.isInterface())
			result = false;
		return result;
	}

	private boolean compareSuperTypes(IType typeA, IType typeASecond,
			IType[] listTypesA, IType[] listTypesB) throws JavaModelException {
		boolean result = true;

		// System.out.println(typeA.getSuperclassName());
		// System.out.println(typeASecond.getSuperclassName());
		//
		// System.out.println(typeA.getSuperInterfaceNames());
		// System.out.println(typeASecond.getSuperInterfaceNames());
		// if (typeA.getSuperclassName() != null &&
		// typeASecond.getSuperclassName() != null &&
		// !typeA.getSuperclassName().equals(typeASecond.getSuperclassName()))
		// result = false;
		// if (typeA.getSuperclassName() == null &&
		// typeASecond.getSuperclassName() != null)
		// result = false;
		// if (typeA.getSuperclassName() != null &&
		// typeASecond.getSuperclassName() == null)
		// result = false;

		if (typeA.getSuperclassName() == null
				&& typeASecond.getSuperclassName() != null)
			return false;
		if (typeA.getSuperclassName() != null
				&& typeASecond.getSuperclassName() == null)
			return false;

		if (typeA.getSuperclassName() != null
				&& !typeA.getSuperclassName().equals("null"))
			asupertypes.add(typeA.getSuperclassName());
		if (typeASecond.getSuperclassName() != null
				&& !typeASecond.getSuperclassName().equals("null"))
			aSecondsupertypes.add(typeASecond.getSuperclassName());

		if (typeA.getSuperclassName() != null
				&& !typeA.getSuperclassName().equals("null")
				&& typeASecond.getSuperclassName() != null
				&& !typeASecond.getSuperclassName().equals("null")) {

			IType superTypeA = null;
			IType superTypeAsecond = null;
			for (int i = 0; i < listTypesA.length; i++) {
				if (typeA.getSuperclassName().equals(
						listTypesA[i].getElementName())) {
					superTypeA = listTypesA[i];
					break;
				}
			}

			for (int i = 0; i < listTypesB.length; i++) {
				if (typeASecond.getSuperclassName().equals(
						listTypesB[i].getElementName())) {
					superTypeAsecond = listTypesB[i];
					break;
				}
			}
			if ((!compareSuperTypes(superTypeA, superTypeAsecond, listTypesA,
					listTypesB))
					|| !(compareType(superTypeA, superTypeAsecond)))
				return false;

		}
		// System.out.println(typeA.getSuperclassName());
		// if (typeASecond.getSuperclassName() != null)
		// System.out.println(typeA.getSuperclassName());
		// && typeASecond.getSuperclassName() != null) {
		// asupertypes.add(typeA.getSuperclassName());
		// aSecondsupertypes.add(typeASecond.getSuperclassName());
		//
		// }

		String[] superInterfaceNames = typeA.getSuperInterfaceNames();
		String[] superInterfaceNames2 = typeASecond.getSuperInterfaceNames();
		for (String superType : superInterfaceNames) {
			asupertypes.add(superType);
		}

		for (String superType : superInterfaceNames2) {
			aSecondsupertypes.add(superType);
		}
		if (superInterfaceNames.length != superInterfaceNames2.length)
			return false;

		if (superInterfaceNames.length > 0
				&& superInterfaceNames.length == superInterfaceNames2.length) {
			IType superTypeA = null;
			IType superTypeAsecond = null;
			List<String> naoUtilizeMais = new ArrayList<String>();
			for (String superType : superInterfaceNames) {
				for (int i = 0; i < listTypesA.length; i++) {
					if (superType.equals(listTypesA[i].getElementName())) {
						superTypeA = listTypesA[i];
						break;
					}
				}
				for (String superType2 : superInterfaceNames2) {
					if (naoUtilizeMais.contains(superType2))
						continue;
					for (int i = 0; i < listTypesB.length; i++) {
						if (superType2.equals(listTypesB[i].getElementName())) {
							superTypeAsecond = listTypesB[i];
							break;
						}
					}
					if ((!compareSuperTypes(superTypeA, superTypeAsecond,
							listTypesA, listTypesB))
							|| !(compareType(superTypeA, superTypeAsecond))) {
						result = false;
					} else {
						result = true;
						naoUtilizeMais.add(superType2);
						break;
					}

				}
				if (!result)
					return false;
			}

		}

		// if
		// (!typeA.getSuperInterfaceNames().equals(typeASecond.getSuperInterfaceNames()))
		// result = false;
		return result;
	}

	// public boolean compareIsomorphic() {
	// IType typeA = compUnitA.findPrimaryType();
	// IType typeASecond = compUnitASecond.findPrimaryType();
	//
	// if (!typeA.getElementName().equals(typeASecond.getElementName()))
	// return false;
	// else
	// return compareFieldsOf(typeA, typeASecond)
	// && compareMethodsOf(typeA, typeASecond);
	// }

	private boolean compareMethodsOf(IType typeA, IType typeASecond) {
		try {
			IMethod[] methodsA = typeA.getMethods();
			IMethod[] methodsASecond = typeASecond.getMethods();
			if (methodsA.length != methodsASecond.length) {
				compareMessage += "the two types do not have the same number of methods";
				return false;
			}
			boolean allMethodsAreEqual = true;
			for (int i = 0; i < methodsA.length; i++) {
				IMethod methodA = methodsA[i];

				IMethod[] methodsCorresponding = typeASecond
						.findMethods(methodA);
				if (methodsCorresponding == null) {
					compareMessage += "\n There is no method correspoding to "
							+ methodA.getElementName() + " in type "
							+ typeASecond.getElementName();
					return false;
				} else
					allMethodsAreEqual = allMethodsAreEqual
							&& compareMethods(methodA, methodsCorresponding[0]);
			}
			return allMethodsAreEqual;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean compareMethods(IMethod methodA, IMethod methodASecond) {
		boolean methodsAreEqual = false;
		try {
			methodsAreEqual = methodA.getSource().trim()
					.equals(methodASecond.getSource().trim());
			if (!methodsAreEqual) {
				methodsAreEqual = compareWithoutLineDelimiters(methodA,
						methodASecond);
			}
			if (!methodsAreEqual) {
				compareMessage += "\n methods " + methodA.getSource().trim()
						+ " and " + methodASecond.getSource().trim()
						+ " are not equal";
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} finally {
			return methodsAreEqual;
		}
	}

	private boolean compareWithoutLineDelimiters(IMethod methodA,
			IMethod methodASecond) {

		try {
			String[] expectedLines = Strings.convertIntoLines(methodA
					.getSource());
			String[] actualLines = Strings.convertIntoLines(methodASecond
					.getSource());

			String expected2 = (expectedLines == null ? null : Strings
					.concatenate(expectedLines, ""));
			String actual2 = (actualLines == null ? null : Strings.concatenate(
					actualLines, ""));
			return expected2.equals(actual2);
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean compareFieldsOf(IType typeA, IType typeASecond) {
		try {
			IField[] fieldsA = typeA.getFields();
			IField[] fieldsASecond = typeASecond.getFields();
			if (fieldsA.length != fieldsASecond.length) {
				compareMessage += "\n the two types have a different number of fields";
				return false;
			}

			boolean allFieldsAreEqual = true;
			for (int i = 0; i < fieldsA.length; i++) {
				IField fieldA = fieldsA[i];
				IField fieldASecond = typeASecond.getField(fieldA
						.getElementName());
				if (!fieldASecond.exists()) {
					compareMessage += "\n there is no field corresponding to "
							+ fieldA.getElementName() + " in type "
							+ typeASecond.getElementName();
					return false;
				}
				allFieldsAreEqual = allFieldsAreEqual
						&& compareFields(fieldA, fieldASecond);
			}
			return allFieldsAreEqual;
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean compareFields(IField fieldA, IField fieldASecond) {
		boolean fieldsAreEqual = false;
		try {
			fieldsAreEqual = fieldA.getSource().trim()
					.equals(fieldASecond.getSource().trim());
			if (!fieldsAreEqual) {
				System.out.println("fieldA.getDeclaring: "
						+ fieldA.getDeclaringType().getElementName());
				compareMessage += "\n type "
						+ fieldA.getDeclaringType().getElementName()
						+ ", fields " + fieldA.getSource().trim() + " and "
						+ fieldASecond.getSource().trim() + " are not equal";
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return fieldsAreEqual;
	}

	public boolean compareTextual() {
		return false;
	}

	public static String formatCompilationUnit(String classSource) {
		IDocument document = new Document();
		document.set(classSource);
		CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(null);
		TextEdit textEdit = codeFormatter.format(
				CodeFormatter.K_COMPILATION_UNIT, classSource, 0,
				classSource.length(), 0, null);
		if (textEdit != null)
			try {
				textEdit.apply(document);
			} catch (MalformedTreeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return document.get();
	}

	public String getCompareMessage() {
		return compareMessage;
	}

}
