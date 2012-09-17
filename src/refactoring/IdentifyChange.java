package refactoring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import randoop.main.Main;

public class IdentifyChange {

	public static final String PROPERTIES_FILE_PATH = "/ant/" + "tests.properties"; // ->  /home/felype/workspaceMestrado/ferramentaLPSSM/ant/
	
//	public static final String PROPERTIES_FILE_PATH = "/Users/felype/Documents/CIn/Mestrado/workspaceMestrado/ferramentaLPSSM/ant/"
//			+ "tests.properties";
	// Numero do exemplo para o qual serao gerados os testes
	// public static int EXAMPLE_NUMBER = 39;
	private static String sourcePath;
	private static String targetPath;
	private static String testsPath;

	public static String timeout = "100000";
	private static Vector<String> saida;
	static Vector<String> classesComProblema = new Vector<String>();
	private static String testNumber;
	private static String fileQuantidadeTestes;
	private static String abordagem;
	private static int quantidadeMetodos;

	// Record the methods of the source program before refactoring
	public static void saveMethodsTarget(String classesString) {
		Vector<String> metodos = getMethods(getTargetPath(), classesString);

		StringBuffer metodosBuffer = new StringBuffer();
		// adiciona ao buffer tudo que tem no vector
		addBuffer(metodosBuffer, metodos);
		// grava todos os metodos no arquivo
		FileUtil.gravaArquivo(Constants.ARQUIVO_BASE, metodosBuffer.toString());
	}

	private static Vector<String> getMetodosDeClasses(Vector<String> classesParaTestar, Vector<String> allClasses) {
		Vector<String> retorno = new Vector<String>();
		HashSet<String> metodos = new HashSet<String>();

		int quantidadeClasses = classesParaTestar.size();

		for (int i = 0; i < classesParaTestar.size(); i++) {
			String classe = classesParaTestar.get(i);

			if (classe.contains("src.")) {
				classe = classe.split("src.")[1];
			}

			if (!classe.toString().equals("org.jhotdraw.contrib.SVGStorageFormat") && !classe.toString().startsWith("org.apache.naming")
					&& !classe.toString().startsWith("org.apache.tomcat.jni") && !classe.toString().contains("Abstract")) {
				try {
					Class<?> c = Class.forName(classe);

					int modifiers = c.getModifiers();

					//Nao considera interfaces, classes abstratas ou nao publicas.
					if (!c.isInterface() && !Modifier.isAbstract(modifiers) && Modifier.isPublic(modifiers)) {
						if (quantidadeClasses > 0 && classesParaTestar != allClasses) {
							Constructor<?>[] construtores = c.getConstructors();

							for (Constructor<?> constructor : construtores) {
								Class<?>[] classParameters = constructor.getParameterTypes();

								for (Class<?> classParameter : classParameters) {
									String nomeDaClasseDoParametro = classParameter.getName();

									//Independente de que classe esteja no construtor, soh serao gerados testes
									//para ela se ela pertencer ao projeto. Nao serao gerados testes para classes
									//de bibliotecas.
									if (!classesParaTestar.contains(nomeDaClasseDoParametro)
											&& allClasses.contains(nomeDaClasseDoParametro)) {
										classesParaTestar.add(classParameter.getName());
									}
								}
							}

							//Somente as classes dos parametros das classes da lista devem ser identificados.
							//As que forem adicionadas dirante esse processo nao devem ter as classes identificadas.
							quantidadeClasses--;
						}

						metodos.addAll(getMethodList(c));
					}

				} catch (ExceptionInInitializerError e) {
					classesComProblema.add(classe);
				} catch (NoClassDefFoundError e) {
					classesComProblema.add(classe);
				} catch (ClassNotFoundException e) {
					classesComProblema.add(classe);
					// e.printStackTrace();
					//System.err
					//		.println("Erro em leitura de classes no metodo saveMethodsTarget "
					//				+ e.getMessage());
					//Do nothing
					//Classes de features opcionais podem nao existir.
				}
			}
		}

		retorno.addAll(metodos);

		return retorno;
	}

	// adiciona uma colecao de metodos publicos a um buffer
	private static void addBuffer(StringBuffer metodos, Vector<String> colMs) {
		if (colMs != null) {
			for (String metodo : colMs) {
				metodos.append(metodo + "\n");
			}
		}
	}

	// ve se um metodo eh publico
	public static boolean isPublic(int mod) {
		return Modifier.isPublic(mod);
	}

	// pega todos os metodos publicos de uma classe e adiciona um buffer
	public static Vector<String> getMethodList(Class<?> classe) {
		Vector<String> result = new Vector<String>();

		// we only consider the public methods
		Method[] methods = classe.getMethods();
		int size = methods.length;

		for (int i = 0; i < size; i++) {
			// Tirei todos os metodos com cifrao, pois eles
			// vem do aspecto
			// TODO [Diego] Tem que ajeitar isso!
			// if (!col[i].toString().contains(cifrao em aspas))

			String methodClass = methods[i].getDeclaringClass().getName();

			if (!methodClass.equals("java.lang.Object") /*&& !methodClass.contains("org.eclipse") || methodClass.contains("org.eclipse.jface") || methodClass.contains("org.eclipse")*/) {
				String methodName = methods[i].getName();
				String methodReturn = methods[i].getReturnType().getName();
				Class<?>[] parameterTypes = methods[i].getParameterTypes();

				String methodSig = methodReturn + " " + methodClass + "." + methodName + "(";
				for (int j = 0; j < parameterTypes.length; j++) {
					methodSig = methodSig + parameterTypes[j].getName();
					if (j < (parameterTypes.length - 1))
						methodSig = methodSig + ", ";
				}
				methodSig = methodSig + ")";

				if (!result.contains(methodSig) && !result.contains(Pattern.quote("_")) && !methodSig.contains("exceptionblocks")) {
					result.add(methodSig);
				}
			}
		}

		Constructor<?>[] cons = classe.getConstructors();
		size = cons.length;

		for (int i = 0; i < size; i++) {
			String constructor = cons[i].getName();
			Class<?>[] parameterTypes = cons[i].getParameterTypes();
			constructor = constructor + ".<init>(";

			for (int j = 0; j < parameterTypes.length; j++) {
				constructor = constructor + parameterTypes[j].getName();
				if (j < (parameterTypes.length - 1))
					constructor = constructor + ", ";
			}

			constructor = "cons : " + constructor + ")";

			if (!result.contains(constructor)) {
				result.add(constructor);
			}
		}

		return result;
	}

	// pega os metodos do projeto refatorado
	public static Vector<String> getMethods(String path, String classesString) {
		Vector<String> result = null;
		Vector<String> classesParaTestar = new Vector<String>();
		Vector<String> allClasses = new Vector<String>();

		FileUtil.getClasses(path, allClasses, "");

		if (classesString != null) {
			// Otimizacao para otimizacoes em apenas uma classe.
			// Adiciona apenas a classe recebida por parametro.
			String[] arrayString = classesString.split(Pattern.quote("|"));

			for (String string : arrayString) {
				classesParaTestar.add(string);
			}
		} else {
			classesParaTestar = allClasses;
		}

		result = getMetodosDeClasses(classesParaTestar, allClasses);

		return result;
	}

	// pega os metodos do projeto inicial que esta grava em um arquivo
	// public static Vector<String> getMethods(String arq) {
	// Vector<String> result = new Vector<String>();
	// String texto = FileUtil.leArquivo(arq);
	// StringTokenizer str = new StringTokenizer(texto, "==");
	// int tam = str.countTokens();
	// for (int i = 0; i < tam; i++)
	// result.add(str.nextToken().trim());
	// return result;
	// }

	public static Vector<String> intersection(String path, String classes, String method) {
		Vector<String> result = new Vector<String>();

		// pega os metodos do projeto antes (source)
		// e depois (target) de refatorado
		Vector<String> source = getMethods(getSourcePath(), classes);
		Vector<String> target = FileUtil.leArquivo(Constants.ARQUIVO_BASE);
		System.out.println("Source=" + source.size() + " Target=" + target.size());

		if (method.equals("ONLY_COMMON_METHODS")) {
			for (String m : target) {
				if (source.contains(m/* .replaceFirst("target","source") */) && !result.contains(m))
					result.add(m/* .replaceFirst("target","source") */);
			}
		} else {
			result = source;
		}

		//remover o retorno do metodo porque o randoop nao utiliza
		for (int i = 0; i < result.size(); i++) {
			if (result.get(i).startsWith("cons"))
				continue;
			String[] split = result.get(i).split(" ", 2);
			String sig = "method : " + split[1];
			result.set(i, sig);
		}

		quantidadeMetodos = result.size();

		StringBuffer metodos = new StringBuffer();
		// adiciona ao buffer tudo que tem no vector
		addBuffer(metodos, result);
		// grava todos os metodos no arquivo
		FileUtil.gravaArquivo(Constants.ARQUIVO_INTERSECAO, metodos.toString());

		return result;
	}

	private static void storeTestNumber() throws FileNotFoundException, IOException {
		Properties properties = loadTestProperties();

		String quantidade = String.valueOf(Integer.parseInt(testNumber) * quantidadeMetodos);

		properties.setProperty("QUANTIDADE_TESTES", quantidade);

		storeProperties(properties);
	}

	public static void generateTestsRandoop(boolean isGoRandoop) throws FileNotFoundException, IOException {
		storeTestNumber();
		// So mudei a classe randoop.util.DefaultReflectionFilter do randoop
		// deixa o conjunto vazio logo no inicio
		// randoop.util.DefaultReflectionFilter.intersecao = saida;
		// diz se vamos fazer o que queremos ou nao
		// randoop.util.DefaultReflectionFilter.PLUG = isGoRandoop;

		// chamando o go-randoop
		// ele gera testes para as classes especificadas no classes.txt e para
		// os metodos
		// que estao gravados na intersecao
		Main main = new Main();

		// File classesTxtFile = new File(Constants.ARQUIVO_CLASSES);
		String quantidadeTestes = "";

		if (!testNumber.equals("0"))
			quantidadeTestes = "--inputlimit=" + (Integer.parseInt(testNumber) * quantidadeMetodos);

		File methodsFile = new File(Constants.ARQUIVO_INTERSECAO);

		//Subsumed tests nao sao mais removidos. Isso deve aumentar a precisao das medicoes,
		//jah que a quantidade de testes compilados e executados serah sempre a esperada.
		String[] temp = { "gentests", "--methodlist=" + methodsFile.getPath(), "--timelimit=" + timeout, /*"--log=filewriter",*/
		"--remove-subsequences=false", "--junit-output-dir=" + Constants.TEST, quantidadeTestes /*,
						"--componentfile-txt=" + "/Users/felype/Documents/CIn/Mestrado/workspaceMestrado/ferramentaLPSSM/ant/EntradasRandoop.txt"*/};
		//Entradas espec�ficas para o exemplo.

		String[] argsRandoop = null;

		if (!quantidadeTestes.equals("")) {
			argsRandoop = temp.clone();
		} else {
			argsRandoop = new String[temp.length - 1];

			for (int i = 0; i < temp.length - 1; i++) {
				argsRandoop[i] = temp[i];
			}
		}

		main.nonStaticMain(argsRandoop);

		System.out.println();
		// deixamos como estava antes
		// apaga tudo o que foi feito
		// randoop.util.DefaultReflectionFilter.intersecao = new
		// java.util.Vector<String>();
		// deixa para o padrao original
		// randoop.util.DefaultReflectionFilter.PLUG = false;
	}

	// Record the common classes before refactoring
	public static void saveClasses(String classes) {

		Vector<String> classesSource = new Vector<String>();
		Vector<String> classesTarget = new Vector<String>();

		if (classes == null) {
			FileUtil.getClasses(getSourcePath(), classesSource, "");
			FileUtil.getClasses(getTargetPath(), classesTarget, "");
		} else {
			String[] arrayString = classes.split(Pattern.quote("|"));

			for (String string : arrayString) {
				classesSource.add(string);
				classesTarget.add(string);
			}
		}

		for (String classeRuim : classesComProblema) {
			classesSource.remove(classeRuim);
			classesTarget.remove(classeRuim);
		}

		StringBuffer classesToFile = new StringBuffer();

		for (String className : classesSource) {
			//Todas classes no source devem ser consideradas nesta versao.
			if (/*classesTarget.contains(className /*
															 * .replaceFirst("source",
															 * "target")
															 *//*)
								&& */!className.equals("org.htmlparser.lexer.Page")) {
				classesToFile.append(className + "\n");
			}
		}
		// grava todos os metodos no arquivo
		FileUtil.gravaArquivo(Constants.ARQUIVO_CLASSES, classesToFile.toString());
	}

	// Saves the classes and methods in source
	public static void prepareBaseFile(String classes) {
		// Saves the methods and record in file
		long before = System.currentTimeMillis();
		saveMethodsTarget(classes);

		// only for debug
		Vector<String> target = FileUtil.leArquivo(Constants.ARQUIVO_BASE);
		System.out.println("Tem " + target.size() + " metodo(s) no projeto target");
		long after = System.currentTimeMillis();
		System.err.println("Tempo total: " + (after - before));
	}

	public static void finishProcess(String classes, String method) throws IOException {
		long before = System.currentTimeMillis();

		Vector<String> target = FileUtil.leArquivo(Constants.ARQUIVO_BASE);

		// only for debug
		Vector<String> source = getMethods(getSourcePath(), classes);
		System.out.println("Tem " + source.size() + " metodo(s) no projeto source");

		saveClasses(classes);

		// 2. Identifica Mudancas
		saida = intersection(getTargetPath(), classes, method);

		System.out.println("Tem " + saida.size() + " metodo(s) iguais nos projetos source e target");

		long after = System.currentTimeMillis();

		System.err.println("Tempo total: " + (after - before));

		Properties properties = loadTestProperties();

		boolean temMetodosDiferentes = source.size() != target.size();

		properties.setProperty("METODOS_REMOVIDOS", String.valueOf(temMetodosDiferentes));

		if (temMetodosDiferentes) {
			HashSet<String> conjuntoMetodosSource = new HashSet<String>();
			HashSet<String> conjuntoMetodosTarget = new HashSet<String>();

			conjuntoMetodosSource.addAll(source);
			conjuntoMetodosTarget.addAll(target);

			//Ficam apenas metodos que foram removidos da implementacao,
			//que existiam na versao source mas nao existem mais na versao target.
			conjuntoMetodosSource.removeAll(target);

			//Ficam apenas metodos que foram adicionados,
			//que nao existiam na versao source e foram adicionados na versao target.
			conjuntoMetodosTarget.removeAll(source);

			String metodosRemovidos = getStringMetodos(conjuntoMetodosSource);
			String metodosAdicionados = getStringMetodos(conjuntoMetodosTarget);

			properties.setProperty("LISTA_METODOS_REMOVIDOS", metodosRemovidos);
			properties.setProperty("LISTA_METODOS_ADICIONADOS", metodosAdicionados);
		}

		storeProperties(properties);

		//Se a quantidade de testes for diferente, metodos foram removidos da aplicacao.
		//Nesta abordagem, essa mudanca nao pode ser um refinamento.
		if (!method.equals("ONLY_COMMON_METHODS") && source.size() != target.size()) {
			System.out.println("Metodos foram removidos dos controladores ou fachadas.");
			System.out.println("O comportamento nao foi preservado.");
			System.out.println("Nao eh refinamento.");
		} else {
			// 3. Gera o testes a partir das modificacoes
			generateTestsRandoop(true);
		}
	}

	private static String getStringMetodos(HashSet<String> conjuntoMetodos) {
		String result = "";

		for (String metodo : conjuntoMetodos) {
			result = result + "," + metodo;
		}

		if (result.length() > 0) {
			result = result.substring(1);
		}

		return result;
	}

	private static void storeProperties(Properties properties) throws FileNotFoundException, IOException {
		properties.store(new FileOutputStream(PROPERTIES_FILE_PATH), "");
	}

	private static Properties loadTestProperties() throws IOException {
		Properties properties = new Properties();

		File fileProperties = new File(PROPERTIES_FILE_PATH);

		if (fileProperties.exists()) {
			FileInputStream inputStream = new FileInputStream(fileProperties);

			properties.load(inputStream);

			inputStream.close();
		}

		return properties;
	}

	public static void main(String[] args) {
		/*
		 * 1- saves target methods on base file 2- creates intersection 3- saves
		 * classes 4- generates tests
		 */
		setSourcePath(args[0]);
		setTargetPath(args[1]);

		// System.out
		// .println("Type '1' if you want to analyze the target; and '2' if you want to finish the process and generate the tests.");
		// Scanner in = new Scanner(System.in);
		// int command = in.nextInt();
		String command = args[2];
		String classes = (args[3].equals("${classes}")) ? null : args[3];

		String t = args[4];
		if (!t.equals("0"))
			timeout = t;

		testNumber = args[5];

		fileQuantidadeTestes = args[6];
		abordagem = args[7];

		// args do main: prepare "source path" "target path" if(args.length >=
		// 2) {
		if (command.equals("prepare")) {
			prepareBaseFile(classes);
		} else if (command.equals("finish")) {
			String method = args[8];

			try {
				finishProcess(classes, method);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("Invalid command " + command + ". Can be \"1\" or \"2\".");
		}
	}

	public static String getSourcePath() {
		return sourcePath;
	}

	public static void setSourcePath(String sourcePath) {
		IdentifyChange.sourcePath = sourcePath;
	}

	public static String getTargetPath() {
		return targetPath;
	}

	public static void setTargetPath(String targetPath) {
		IdentifyChange.targetPath = targetPath;
	}

	public static String getTestsPath() {
		return testsPath;
	}

	public static void setTestsPath(String testsPath) {
		IdentifyChange.testsPath = testsPath;
	}
}