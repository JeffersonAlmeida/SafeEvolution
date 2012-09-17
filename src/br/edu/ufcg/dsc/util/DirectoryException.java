package br.edu.ufcg.dsc.util;

public class DirectoryException extends Exception {

	private static final long serialVersionUID = 1L;

	private String directory;
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getDirectory() {
		return directory;
	}
	public DirectoryException(String message, String d){
		super(message);
		directory = d;
	}
}
