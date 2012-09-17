package refactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

public class FileUtil {

	/**
	 * @param path
	 *            = diretorio base do projeto
	 * @param result
	 *            = armazenara o nome de todos os arquivos Java do diretorio
	 * @param base
	 *            = eh usado na recursao. Indica o nome base do pacote
	 */
	public static void getClasses(String path, Vector<String> result,
			String base) {
		try {
			File dir = new File(path);

			if (!dir.exists()) {
				throw new RuntimeException("Dir " + dir.getAbsolutePath()
						+ " does not exist.");
			}

			File[] arquivos = dir.listFiles();
			int tam = arquivos.length;
			for (int i = 0; i < tam; i++) {
				if (arquivos[i].isDirectory()) {
					// we add the subdirectories
					String baseTemp = base + arquivos[i].getName() + ".";
					getClasses(arquivos[i].getAbsolutePath(), result, baseTemp);
				} else {
					// only .java files
					// TODO maybe, we need to consider aspectj files
					if ((arquivos[i].getName().endsWith(".java") || arquivos[i].getName().endsWith(".aj"))
							&& !arquivos[i].getName().equals(
									"SVGStorageFormat.java")) {
						// TODO Diego - TODEL
						// if(arquivos[i].getName().contains("LogFactory"))
						// continue;

						String temp = base + arquivos[i].getName();
						temp = trataNome(temp);

						if (!result.contains(temp))
							result.add(temp);
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error in FileUtil.getClasses()");
			e.printStackTrace();
		}
	}

	public static void createTestFolders() {
		String tempDir = System.getProperty("java.io.tmpdir") + "/safeRefactor";

		File build = new File(tempDir + "/tests/bin");
		File testsSource = new File(tempDir + "/tests/source");
		File testsTarget = new File(tempDir + "/tests/target");

		build.mkdirs();
		testsSource.mkdirs();
		testsTarget.mkdirs();
	}

	private static String trataNome(String arquivo) {
		// remove a extensao Java (o \\b significa word boundary -- fim da
		// palavra)
		arquivo = arquivo.replaceAll(".java\\b", "");
		arquivo = arquivo.replaceAll(".aj\\b", "");
		return arquivo;
	}

	public static void print(Vector<String> result) {
		String arquivo;
		for (int i = 0; i < result.size(); i++) {
			arquivo = result.get(i);
			System.out.print(trataNome(arquivo) + ", ");
		}
		System.out.println();
	}

	
	public static Vector<String> leArquivo(String name) {
		Vector<String> result = new Vector<String>();
		try {
			FileReader fr = new FileReader(new File(name));
			BufferedReader buf = new BufferedReader(fr);
			while(buf.ready()) {
				String method = buf.readLine();

				result.add(method);
			}			
		} catch (Exception e) {
			System.err.println("Erro no metodo FileUtil.leArquivo()");
			e.printStackTrace();
		}
		return result;
	}

	public static void gravaArquivo(String name, String texto) {
		try {
			FileWriter fw = new FileWriter(new File(name));
			fw.write(texto);
			fw.close();
		} catch (Exception e) {
			System.err.println("Erro no metodo FileUtil.gravaArquivo()");
			e.printStackTrace();
		}
	}

	// testar o que foi feito
	public static void main(String[] args) {
		Vector<String> result = new Vector<String>();
		String baseDir = "F:\\eclipse\\analyzer\\src";
		getClasses(baseDir, result, "");
		System.out.println("O diretorio tem " + result.size() + " arquivos.");
		print(result);
		gravaArquivo("F:\\rohit.txt", "valor 1 + ");
		System.out.println("O arquivo lido e: " + leArquivo("F:\\rohit.txt"));
	}

	public static String getTempPath() {
		return System.getProperty("java.io.tmpdir");
	}
}
