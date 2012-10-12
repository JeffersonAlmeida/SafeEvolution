package br.edu.ufcg.dsc.ck.tasks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import br.edu.ufcg.dsc.util.Comparador;

public class SelectClass implements Task {
	
	/* This variable will store the required classes. */
	private Set<String> required;
	
	/* This variable will store the provided classes.*/
	private HashMap<String, String> provided;

	/**
	 * Initializes the set of provided and required classes. <br></br>
	 * @param provided provided classes.  <br></br>
	 * @param required required classes. <br></br>
	 */
	public SelectClass(HashMap<String, String> provided, Set<String> required) {
		this.required = required;
		this.provided = provided;
	}

	public  void printRequired() {
		Iterator<String> i = required.iterator();
		String required = "";
		while(i.hasNext()){
			String r = (String) i.next();
			required = required + " [ " + r + " ] ";
		}
		System.out.println("\n Required Classes: " + required);
	}

	public void printProvided() {
		Iterator<String> i = provided.keySet().iterator();
		String provided = "";
		while(i.hasNext()){
			String r = (String) i.next();
			provided = provided + " [ " + r + " ] ";
		}
		System.out.println("\n Provided Classes: "+provided);
	}


	/**
	 * This method adds a class to the set of provided classes.
	 */
	@Override
	public boolean addToProvided(String mappingConstant, String path) {
		String existingValue = this.provided.put(mappingConstant,path);
		return existingValue == null;
	}

	/**
	 * This method removes a class "c" from the set of provided classes.
	 */
	@Override
	public boolean removeFromProvided(String c) {
		String existingValue = this.provided.remove(c);
		return existingValue != null;
	}

	/**
	 * This method adds a class to the set of Required classes.
	 */
	@Override
	public boolean addToRequired(String c) {
		return this.required.add(c);
	}

	/**
	 * This method removes a class "c" from the set of required classes.
	 */
	@Override
	public boolean removeFromRequired(String c) {
		return this.required.remove(c);
	}

	/**
	 * This method returns the set of provided classes.
	 */
	@Override
	public HashMap<String, String> getProvided() {
		return this.provided;
	}

	/**
	 * This method returns the set of required classes.
	 */
	@Override
	public Set<String> getRequired() {
		return this.required;
	}
	
	/**
	 * This method sets the set of provided classes.
	 */
	@Override
	public void setProvided(HashMap<String,String> interfaces) {
		this.provided = interfaces;
	}
	
	/**
	 * This method sets the set of required classes.
	 */
	@Override
	public void setRequired(Set<String> interfaces) {
		this.required = interfaces;
	}

	/**
	 * ToString of the object.
	 */
	@Override
	public String toString() {
		String result = "";
		result+="Provided: "+this.provided+"\n";
		result+="Required: "+this.required+"\n";
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((provided == null) ? 0 : provided.hashCode());
		result = prime * result
				+ ((required == null) ? 0 : required.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SelectClass other = (SelectClass) obj;
		if (provided == null) {
			if (other.provided != null)
				return false;
		} else if (!Comparador.equalSets(provided.keySet(),other.provided.keySet()))
			return false;
		if (required == null) {
			if (other.required != null)
				return false;
		} else if (!Comparador.equalSets(required,other.required))
			return false;
		return true;
	}
	
	/**
	 * is the set of required and provided classes empty ?
	 * @return returns true or false.
	 */
	public boolean isEmpty() {
		return (this.required.isEmpty() && this.provided.isEmpty());
	}
}
