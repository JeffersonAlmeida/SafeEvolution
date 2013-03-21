package br.edu.ufcg.dsc.ast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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

	private ICompilationUnit compUnitASecond;

	private String compareMessage;

	private String classeName;

    /**
     * Este m�todo cria um projeto java com o nome JavaProject + miliseconds
     * 
     * @return IJavaProject: retorna um projeto java 
     * @throws ConfigurationException
     */
	public IJavaProject setUpProject() throws ConfigurationException {
		try {
			javaProject = JavaProjectHelper.createJavaProject("JavaProject" + System.currentTimeMillis(), "bin");
//			JavaProjectHelper.addRTJar(javaProject);
			root = JavaProjectHelper.addSourceContainer(javaProject, "src");
			packageP = root.createPackageFragment("p", true, null);

		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		return javaProject;
	}

	/**
	 * Assumes that the input files are text files containing Java source code.
	 * @param fileA
	 * @param fileASecond
	 * @throws IOException 
	 * @throws JavaModelException 
	 */
	public void setInputs(File fileA, File fileASecond) throws IOException, JavaModelException {
		if (fileA.exists() && fileASecond.exists())setInputs(fileA.getName(), getContents(fileA),fileASecond.getName() , getContents(fileASecond));
		else throw new IOException("one of the input files does not exist");
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
	public void setInputs(String nameCUA, String contentsA, String nameCUB, String contentsB) throws JavaModelException {

		try {
			String formattedCUA = formatCompilationUnit(contentsA);
			String formattedCUB = formatCompilationUnit(contentsB);

			compUnitA = parse(nameCUA, formattedCUA);
			// we need to get a working copy because the next parsing is going
			// to overwrite this compilation
			// unit in case that the names of the two CompUnits are the same
			compUnitA = compUnitA.getWorkingCopy(null);
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
	//	cu.save(null, false);
		return cu;
	}
	
	public Collection<String> getConstructorParameters() throws JavaModelException{
		ArrayList<String> result = new ArrayList<String>();
		
		IType[] listTypesA = this.compUnitA.getAllTypes();
		
		for(IType type : listTypesA){
			IMethod[] methods = type.getMethods();
			
			for(IMethod method : methods){
				
				if(this.classeName.equals(method.getElementName())){
					String[] parameterTypes = method.getParameterTypes();
					
					for(String str : parameterTypes){
						if(str.length() > 1){
							result.add(str.substring(1, str.length()-1));
						}
					}
				}
			}
		}
		
		return result;
	}

	// Kely July 09 commented the method and change it for this 
	public boolean isIsomorphic() throws JavaModelException{		
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
					if (typeA.getElementName().equals(listTypesB[j].getElementName())) {
						typeB = listTypesB[j];						
						break;
					}
				}
				if (typeB == null){
					compareMessage += "class " + typeA.getElementName() + " was not found"; 
					differences = false;
				} else if (!(compareFieldsOf(typeA, typeB) && compareMethodsOf(typeA, typeB))){
					differences = false;
				}				
			}
		}
		return differences;
	}
	
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

				IMethod[] methodsCorresponding = typeASecond.findMethods(methodA);
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
			methodsAreEqual = methodA.getSource().trim().equals(
					methodASecond.getSource().trim());
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
			fieldsAreEqual = fieldA.getSource().trim().equals(
					fieldASecond.getSource().trim());
			if (!fieldsAreEqual) {
				System.out.println("fieldA.getDeclaring: "+ fieldA.getDeclaringType().getElementName());
				compareMessage += "\n type " + fieldA.getDeclaringType().getElementName() + ", fields " + fieldA.getSource().trim()
						+ " and " + fieldASecond.getSource().trim()
						+ " are not equal";
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
				CodeFormatter.K_COMPILATION_UNIT, classSource, 0, classSource
						.length(), 0, null);
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

	public void setInput(File sourceFile) throws IOException, JavaModelException {
		if (sourceFile.exists()){
			this.setInput(sourceFile.getName(), getContents(sourceFile));
			this.classeName = sourceFile.getName().replaceAll(".java","");
		}
		else {
			throw new IOException("The input file does not exist.");
		}
	}

	private void setInput(String name, String contents) throws JavaModelException {
		try {
			String formattedCUA = formatCompilationUnit(contents);

			compUnitA = parse(name, formattedCUA);
			// we need to get a working copy because the next parsing is going
			// to overwrite this compilation
			// unit in case that the names of the two CompUnits are the same
			compUnitA = compUnitA.getWorkingCopy(null);

		} catch (JavaModelException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
