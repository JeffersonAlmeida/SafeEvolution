package refactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenerateResultsTable {
	private static final String file_separator = System.getProperty("file.separator");
	private static final int LINE_NUMBER_WITH_RESULTS = 2;
	private final static String TEMPLATE_SOURCE_FILE_NAME = "SOURCE-TEST-Example%dTest.txt";
	private final static String TEMPLATE_TARGET_FILE_NAME = "TARGET-TEST-Example%dTest.txt";
	private final static String TESTS_OUTPUT_FOLDER = "tests_output";
	private final static String REGEX_TEST_OUTPUT = "Tests run: ([0-9]+), Failures: ([0-9]+), Errors: ([0-9]+), Time elapsed: (.*) sec";

	// Maps the number of the example (indexes of array) to the refactoring applied to it
	private final static String[] example2refactoring = { "", "Push down method",
			"Push down method", "Push down method", "Rename method",
			"Encapsulate Field", "Rename top level", "Push down method",
			"Rename local variable", "rename member type", "Rename field",
			"Extract method", "Pull up method", "Push down method",
			"Push down method", "Push down method", "Extract class",
			"Push down method", };

	// Maps the number of example (in folder) to the number of subject in the paper
	private final static int[] example2subject = { 0, 8, 9, 11, 5, 6, 1, 10, 4, 2, 3, 7, 12, 13, 14, 15, 16, 17, };
	
	@SuppressWarnings("unused")
	private static String readInformations(String s, int example_number) throws IOException {
		Pattern p = Pattern.compile(REGEX_TEST_OUTPUT);
		Matcher m = p.matcher(s);

		if (m.find()) {
			String number_of_tests = m.group(1);
			String failures = m.group(2);
			String errors = m.group(3);
			String time = m.group(4);

			// Pattern: #subject & #classes & #methods & refactoring & #tests & #failures
			return String.format(
					"%d & %d & METODOS & %s & %s & %s\\\\",
					example2subject[example_number], getNumberOfClasses(example_number), example2refactoring[example_number], number_of_tests, failures);
		} else {
			System.err.println("Error to find values in test ouput.");
			return null;
		}
	}

	private static Object getNumberOfClasses(int example_number) throws IOException {
		File f = new File("refactoringExamples" + file_separator + "examples" + file_separator + "example"+example_number + file_separator + "source" + file_separator + "classes.txt");
		
		return countLines(f);
	}

	private static Object countLines(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		int count = 0;
		
		String line = null;
		while((line = br.readLine()) != null && !line.equals("")) {
			count++;
		}
		
		return count;
	}

	private static String readLine(File file, int line_number)
			throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		for (int i = 1; i < line_number; i++) {
			br.readLine();
		}

		return br.readLine();
	}

	private static String getResult(int example_number, TestType test_type)
			throws IOException {
		String template = test_type.getTemplate();
		String tests_output_file = String.format(template, example_number);
		File f = new File(TESTS_OUTPUT_FOLDER + file_separator
				+ tests_output_file);

		String lineRead = readLine(f, LINE_NUMBER_WITH_RESULTS);
		return readInformations(lineRead, example_number);
	}

	public static void main(String[] args) throws IOException {
		int examples = 17;

		String[] results = new String[examples+1];
		for (int i = 1; i <= examples; i++) {
			results[example2subject[i]] = getResult(i, TestType.TARGET);
		}
		
		for(int i = 1; i <= examples; i++) {
			System.out.println(results[i]);
		}
	}

	// Enum representing the type of the test that will have informations
	// extracted (source or target)
	private enum TestType {
		SOURCE(TEMPLATE_SOURCE_FILE_NAME), TARGET(TEMPLATE_TARGET_FILE_NAME);

		private String template;

		private TestType(String template) {
			this.template = template;
		}

		String getTemplate() {
			return template;
		}
	}

}