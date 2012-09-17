package br.edu.ufcg.dsc.util;

/**
 * @author Jefferson Almeida - jra at cin dot ufpe dot br 
 * Esta classe é responsável por gerenciar os arquivos, bem como procurar por suas dependencias
 * necessárias para que seja possível compilar o arquivo: Classe, Aspecto.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.builders.ProductBuilder;

public class FilesManager {

	private static FilesManager instance;
	private ArrayList<String> filesToBeProcessed;

	private FilesManager() {
		this.filesToBeProcessed = new ArrayList<String>();
	}

	public static FilesManager getInstance() {
		if (instance == null) {
			instance = new FilesManager();
		}
		return instance;
	}

	/**
	 * Este método obtém as dependencias do aspecto pelos imports.
	 * Assim, é possível saber quais classes o aspecto precisa para ser efetivamente compilado.
	 * 
	 * @param File aspecto:  O arquivo referente ao aspecto.
	 * @return Retorna uma Collection<String> de todos as classes (imports) necessários para o aspecto compilar.
	 */
	public Collection<String> getDependenciasDeAspectosPeloImport(File aspecto) {
		/**
		 * Uma coleção de strings que contém todas as dependencias do aspecto.
		 * Ou seja, todos os imports encontrados são adicionados à coleção dependencias.
		 */
		Collection<String> dependencias = new ArrayList<String>();

		try {
			FileReader reader = new FileReader(aspecto);
			BufferedReader in = new BufferedReader(reader);
			String linha;
			/**
			 * Parser para identificar todos os imports necessários para compilar o aspecto.
			 */
			while ((linha = in.readLine()) != null && !linha.contains("public")) {
				if (linha.contains("import")) {
					linha = linha.replaceFirst("import", "");
					linha = linha.replace(";", "");
					linha = linha.trim();
					dependencias.add(linha);
				}
			}
			in.close();
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage()+ ":\n Não foi encontrado o arquivo" + aspecto.getName() + "\n\n");
			e.printStackTrace();
		} catch (IOException e) {
		    System.out.println(e.getMessage() + ":\nI/O Exceptioin para encontrar as dependencias do aspecto: " + aspecto.getName() + "\n\n");
			e.printStackTrace();
		}
		return dependencias;
	}

	/**
	 * Este método verifica de quais aspectos a classe eh dependente.
	 *  Ou Verifica em quais classes um aspecto interfere/tem impacto.
	 * 
	 * @param File classe: A classe passada por parametro.
	 * @return Retorna uma sequencia de aspectos ( Collection<String> ) que a classe passada por parametro depende.
	 */
	public Collection<String> getDependenciasAspectos(File classe) {
		Collection<String> aspectos = new ArrayList<String>();
		try {
			FileReader reader = new FileReader(classe);
			BufferedReader in = new BufferedReader(reader);
			String linha = in.readLine();
			linha = linha.replaceAll(Pattern.quote("/"), "");
			String[] aspectosFromText = linha.split(",");
			for (String aspect : aspectosFromText) {
				if (!aspect.trim().equals("")) {
					aspectos.add(aspect.trim());
				}
			}
			in.close();
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage() + "\nArquivo não encontrado: " + classe.getName());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage() + "\nIO exception para o arquivo: " + classe.getName());
			e.printStackTrace();
		}
		return aspectos;
	}

	/**
	 * 
	 * @param sourceFeatures
	 * @return
	 * @throws IOException
	 */
	public HashSet<String> getFeatures(String sourceFeatures) throws IOException {
		HashSet<String> out = new HashSet<String>();
		BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(new File(sourceFeatures))));
		while (true) {
			String line = input.readLine();
			if (line == null) {
				break;
			}
			line = line.trim();
			out.add(line);
		}
		return out;
	}

	public void verifyDirectoriesStructure(ArrayList<String> assets, String path) {
		for (String file : assets) {
			String temp = "";
			String separator = "";

			//Aspectos nao sao pre-processados pelo Antenna. 
			//Precisam ir direto para a pasta de arquivo pre-processados.
			if (file.endsWith("aj")) {
				file = file.replaceFirst(ProductBuilder.SRCPREPROCESS, "src");
			}

			if (!file.startsWith(Constants.FILE_SEPARATOR)) {
				separator = Constants.FILE_SEPARATOR;
			}

			String[] values = file.split(Pattern.quote("/"));

			for (int i = 0; i < values.length - 1; i++) {
				String value = values[i];
				temp += separator + value;
				separator = Constants.FILE_SEPARATOR;
				createDir(path + temp);
			}
		}
	}

	public void copyFiles(String sourcePath, ArrayList<String> assets, ArrayList<String> assetsDestination, String destinationPath) {

		for (int i = 0; i < assets.size(); i++) {
			String assetDestino = assetsDestination.get(i);

			//Aspectos nao sao pre-processados pelo Antenna. 
			//Precisam ir direto para a pasta de arquivo pre-processados.
			if (assetDestino.endsWith("aj")) {
				assetDestino = assetDestino.replaceFirst(ProductBuilder.SRCPREPROCESS, "src");
			}

			if (!assetDestino.startsWith(Constants.FILE_SEPARATOR)) {
				assetDestino = Constants.FILE_SEPARATOR + assetDestino;
			}

			try {
				copyFile(sourcePath + assets.get(i), destinationPath + assetDestino);
			} catch (AssetNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DirectoryException e) {
				// TODO Auto-generated catch block
				//		e.printStackTrace();
				System.out.println("Creating dir");

				createDir(e.getDirectory());

				try {
					copyFile(sourcePath + assets.get(i), destinationPath + assetDestino);
				} catch (AssetNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (DirectoryException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	public void copyFiles(File sourceDirectory, File sourceFile, File destFile) throws AssetNotFoundException, DirectoryException {
		if (!sourceFile.getAbsolutePath().contains("svn")) {
			if (sourceFile.isDirectory()) {
				File[] files = sourceFile.listFiles();

				for (File file : files) {
					this.copyFiles(sourceDirectory, file, destFile);
				}
			} else {
				String sourceAbsolutePath = sourceFile.getAbsolutePath();

				File destFileWithDirectory = new File(destFile.getAbsolutePath() + File.separator
						+ sourceAbsolutePath.replace(sourceDirectory.getAbsolutePath(), ""));

				if (!destFileWithDirectory.getParentFile().exists()) {
					destFileWithDirectory.getParentFile().mkdirs();
				}

				this.copyFile(sourceAbsolutePath, destFileWithDirectory.getAbsolutePath());
			}
		}
	}

	public void copyFile(String sourceAbsolutPath, String targetAbsolutPath) throws AssetNotFoundException, DirectoryException {
		File sourceFile = new File(sourceAbsolutPath);
		File destFile = new File(targetAbsolutPath);

		if (!sourceFile.exists()) {
			throw new AssetNotFoundException("Problem finding asset: " + sourceAbsolutPath);
		} else if (!destFile.getParentFile().exists()) {
			throw new DirectoryException("Cannot find dir: " + targetAbsolutPath, destFile.getParent());
		}

		try {
			// Cria channel na origem
			FileChannel oriChannel = new FileInputStream(sourceAbsolutPath).getChannel();
			// Cria channel no destino
			FileChannel destChannel = new FileOutputStream(targetAbsolutPath).getChannel();
			// Copia conteudo da origem no destino
			destChannel.transferFrom(oriChannel, 0, oriChannel.size());

			// Fecha channels
			oriChannel.close();
			destChannel.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//	String command = "scp " + sourceFile + " " + destinationFolder;
		//	executeUnixCommand(command);
	}

	public void executeUnixCommand(String command) {

		System.out.println("COMANDO " + command);

		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isFobiddenDirectory(String name, String path) {
		return ((name.equals(path + Constants.FILE_SEPARATOR + "src") || name.equals(path + Constants.FILE_SEPARATOR + "bin")));
	}

	public String getDirectory(String asset) {
		String out = "";
		String separator = "";

		String[] values = asset.split(Pattern.quote("/"));

		for (int i = 0; i < values.length - 1; i++) {
			out += separator + values[i];
			separator = Constants.FILE_SEPARATOR;
		}

		return out;
	}

	public ArrayList<String> getFileContent(String file) {
		ArrayList<String> output = new ArrayList<String>();
		BufferedReader input = null;

		try {
			input = new BufferedReader(new InputStreamReader(new FileInputStream(new File(file))));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (true) {
			String line = null;
			try {
				line = input.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (line == null) {
				break;
			}
			output.add(line);
		}
		return output;
	}

	public void createFile(String fileName, String content) {
		ArrayList<String> fileContent = new ArrayList<String>();
		fileContent.add(content);
		createFile(fileName, fileContent);
	}

	public void createFile(String fileName, ArrayList<String> content) {
		PrintStream stream = null;

		try {
			stream = new PrintStream(new File(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String string : content) {
			stream.append(string + Constants.LINE_SEPARATOR);
		}
		stream.flush();
		stream.close();
	}

	public ArrayList<String> getAssetNames(File assetMappingFile) throws IOException, AssetNotFoundException {
		ArrayList<String> result = new ArrayList<String>();

		FileInputStream inputStream = new FileInputStream(assetMappingFile);
		InputStreamReader reader = new InputStreamReader(inputStream);
		BufferedReader buffer = new BufferedReader(reader);

		String line = null;

		while ((line = buffer.readLine()) != null) {
			if (line.startsWith("#")) {
				result.add(line.substring(1).trim());
			}
		}

		buffer.close();
		reader.close();
		inputStream.close();

		return result;
	}

	public String getPathAPartirDoSrc(String path) {
		String result = "";

		String[] parts = path.split("src");

		for (int i = 1; i < parts.length; i++) {
			result = result + "src" + parts[i];
		}

		return result;
	}

	//	//ugly baby
	//	public ArrayList<String> getAssets(String string, String targetCM)
	//	throws IOException, AssetNotFoundException {
	//
	//		ArrayList<String> output = new ArrayList<String>();
	//		boolean found = false;
	//
	//		BufferedReader input = new BufferedReader(new InputStreamReader(
	//				new FileInputStream(new File(targetCM))));
	//
	//		while (true) {
	//			String line = input.readLine();
	//			if (line == null) {
	//				break;
	//			}
	//			line = line.trim();
	//			if (line.startsWith("#")) {
	//				String name = line.substring(1).trim();
	//				if (name.equals(string)) {
	//					found = true;
	//					while (true) {
	//						String nextLine = input.readLine();
	//						if (nextLine == null || nextLine.equals("")) {
	//							break;
	//						}
	//						if (!nextLine.startsWith(Constants.FILE_SEPARATOR)) {
	//							nextLine = Constants.FILE_SEPARATOR + nextLine;
	//						}
	//						output.add(nextLine);
	//					}
	//				} else {
	//					continue;
	//				}
	//			} else {
	//				continue;
	//			}
	//		}
	//
	//		input.close();
	//
	//		if (!found) {
	//			throw new AssetNotFoundException(
	//					"Did not find corresponding asset Name: " + string);
	//		}
	//
	//		return output;
	//	}

	public HashMap<String, String> getAssets(String amPath) throws IOException {

		HashMap<String, String> result = new HashMap<String, String>();

		File amFile = new File(amPath);

		FileInputStream inputAM = new FileInputStream(amFile);

		InputStreamReader readerAM = new InputStreamReader(inputAM);

		BufferedReader bufferAM = new BufferedReader(readerAM);

		String line = null;

		while ((line = bufferAM.readLine()) != null) {
			line = line.trim();

			if (line.startsWith("#")) {
				String constant = line.substring(1).trim(); //Pegando constante.

				line = bufferAM.readLine(); //Pegando path.

				if (!line.startsWith(Constants.FILE_SEPARATOR)) {
					line = Constants.FILE_SEPARATOR + line;
				}

				result.put(constant, line);
			}
		}

		bufferAM.close();
		readerAM.close();
		inputAM.close();

		return result;
	}

	public File createDir(String path) {
		File f = new File(path);

		if (!f.exists()) {
			f.mkdirs();
		}

		System.out.println("CRIADO : " + path);

		return f;
	}

	public String getPath(String string) {
		String out = "";
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			if (c != '.') {
				out += c;
			} else {
				if (i >= string.length() - 5) {
					out += c;
				} else {
					out += Constants.FILE_SEPARATOR;
				}
			}
		}
		return out;
	}

	public void cleanDir(String string) {
		File dir = new File(string);

		if (dir.exists() && dir.isDirectory()) {
			File[] subFiles = dir.listFiles();

			for (File file : subFiles) {
				if (file.isDirectory()) {
					this.cleanDir(file.getAbsolutePath());
				}

				file.delete();
			}
		}

		//System.out.println("LIMPAR DIRETORIO");
		//String command = "rm -rf " + string;
		//executeUnixCommand(command);
	}

	public void limparArquivosJava() {
		this.filesToBeProcessed = new ArrayList<String>();
	}

	public ArrayList<String> copyFilesDirectory(String sourcePath, ArrayList<String> assets, ArrayList<String> assetsDestination,
			String destinationPath, boolean copyAspectos) {

		for (int i = 0; i < assets.size(); i++) {
			File file = new File(sourcePath + assets.get(i));

			if (file.isDirectory()) {
				if (!file.isHidden() && !file.getName().equals("lib") && !file.getName().equals("bin")) {
					File[] files = file.listFiles();
					ArrayList<String> assetsOtherFiles = new ArrayList<String>();

					for (File arq : files) {
						if (!arq.isHidden() && !arq.getName().startsWith(".")) {
							assetsOtherFiles.add(this.getPathAdaptado(arq));
						}
					}

					this.copyFilesDirectory(sourcePath, assetsOtherFiles, assetsDestination, destinationPath, false);
				}
			} else {
				//Para a Target, aspectos nao devem ser copiados.
				if (copyAspectos || !assets.get(i).endsWith("aj")) {
					String destAsset = assets.get(i);

					try {
						if (destAsset.endsWith("java") || destAsset.endsWith("aj") || destAsset.endsWith("xml")
								|| destAsset.endsWith("properties")) {
							this.filesToBeProcessed.add(destinationPath + "/" + destAsset);
						}

						//Pre-processando com o Velocy, os arquivos podem ir direto para a pasta source.
						//	destAsset = assets.get(i).replaceFirst("src", TargetBuilder.SRCPREPROCESS);
						copyFile(sourcePath + assets.get(i), destinationPath + "/" + destAsset);
					} catch (AssetNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (DirectoryException e) {
						// TODO Auto-generated catch block
						//	e.printStackTrace();
						System.out.println("Creating dir");

						createDir(e.getDirectory());

						try {
							copyFile(sourcePath + assets.get(i), destinationPath + "/" + destAsset);
						} catch (AssetNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (DirectoryException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}

			}
		}

		return this.filesToBeProcessed;
	}

	public void copyDiretorioInteiro(String sourcePath, String destinationPath) {

		File file = new File(sourcePath);

		if (file.isDirectory()) {
			File[] files = file.listFiles();

			File pastaDestino = new File(destinationPath + Constants.FILE_SEPARATOR + file.getName());

			if (pastaDestino.getName().equals("eclipse ganymede copy")) {
				for (File arq : files) {
					this.copyDiretorioInteiro(arq.getAbsolutePath(), pastaDestino.getParent());
				}
			} else {
				pastaDestino.mkdirs();

				for (File arq : files) {
					this.copyDiretorioInteiro(arq.getAbsolutePath(), pastaDestino.getAbsolutePath());
				}
			}
		} else {
			try {
				File destino = new File(sourcePath);

				copyFile(sourcePath, destinationPath + Constants.FILE_SEPARATOR + destino.getName());
			} catch (AssetNotFoundException e) {
				e.printStackTrace();
			} catch (DirectoryException e) {
				e.printStackTrace();
			}
		}
	}

	private String getPathAdaptado(File arq) {
		String result = "/src";

		String workaround = "XXXXX";
		String tempPath = arq.getAbsolutePath().replaceFirst("src", workaround);

		String[] parts = tempPath.split(workaround);

		result = result + parts[1];

		return result;
	}

	public String getCorrectName(String path) {
		if (!path.startsWith(Constants.FILE_SEPARATOR)) {
			path = Constants.FILE_SEPARATOR + path;
		}
		String[] dirs = path.trim().split(Pattern.quote(Constants.FILE_SEPARATOR));

		String out = "";
		String separator = "";

		int counter = -1;

		for (int i = 0; i < dirs.length; i++) {
			if (counter != -1) {
				out += separator + dirs[i];
				separator = ".";
			}
			if (dirs[i].equals("src")) {
				counter = i;
			}
		}

		return out;
	}

	public void cleanDirectory(String directory) {
		File buildFile = new File(br.edu.ufcg.dsc.Constants.PLUGIN_PATH + "/ant/build.xml");

		Project p = new Project();
		p.setProperty("directory", directory);

		DefaultLogger consoleLogger = new DefaultLogger();
		consoleLogger.setErrorPrintStream(System.err);
		consoleLogger.setOutputPrintStream(System.out);
		consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
		p.addBuildListener(consoleLogger);

		p.init();
		ProjectHelper helper = ProjectHelper.getProjectHelper();
		p.addReference("ant.projectHelper", helper);
		helper.parse(p, buildFile);

		p.executeTarget("clean_directory");

		System.out.println("Limpando " + directory);
	}

	public void copyLibs(String fileSourcePath, String destDirectoryPath) throws AssetNotFoundException, DirectoryException {
		File fileSource = new File(fileSourcePath);
		File fileDest = new File(destDirectoryPath);

		FilesManager.getInstance().copyFiles(fileSource, fileSource, fileDest);
	}
}