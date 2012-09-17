package refactoring;

public class Constants {

	public static final String FILE_SEPARATOR = System
	.getProperty("file.separator");
	// Nao alterar as constantes abaixo.
	// Alterar apenas a constante Identifychange.EXAMPLE_NUMBER
	// public static final String SOURCE_JAVA_PROJECT =
	// "refactoringExamples/examples/example"+ IdentifyChange.EXAMPLE_NUMBER
	// +"/source";
	// public static final String TARGET_JAVA_PROJECT =
	// "refactoringExamples/examples/example"+ IdentifyChange.EXAMPLE_NUMBER
	// +"/target";
	public static final String ARQUIVO_CLASSES = FileUtil.getTempPath() + "/" +"classes.txt";
	public static final String ARQUIVO_BASE = FileUtil.getTempPath() + FILE_SEPARATOR + "arquivobase.txt";
	public static final String ARQUIVO_INTERSECAO = FileUtil.getTempPath() + FILE_SEPARATOR + "intersection.txt";
	// public static final String sourcePackageName = "examples.example"+
	// IdentifyChange.EXAMPLE_NUMBER +".source.";
	// public static final String targetPackageName = "examples.example"+
	// IdentifyChange.EXAMPLE_NUMBER +".target.";
	// public static final String JUNIT_FILENAME = "Example" + + "Test";
	public static final String TEMP = System.getProperty("java.io.tmpdir") + FILE_SEPARATOR + "safeRefactor";
	public static final String TEST = TEMP + FILE_SEPARATOR + "tests";
	public static final String TESTBIN = TEMP + FILE_SEPARATOR + "tests" + FILE_SEPARATOR + "bin";
	public static final String TESTSRC = TEMP + FILE_SEPARATOR + "tests" + FILE_SEPARATOR + "source";
	public static final String TESTTGT = TEMP + FILE_SEPARATOR + "tests" + FILE_SEPARATOR + "target";
	
	
}
