package safeEvolution.wellFormedness;

public class Teste {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = "aaaa";
		path = path.replaceAll("a", "b");
		String t = "jaca,maca";
		System.out.println("=>" + path);
		System.out.println("=>split " + t.split(",")[0]);
	}

}
