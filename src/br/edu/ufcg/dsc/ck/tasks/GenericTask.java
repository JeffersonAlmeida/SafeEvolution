package br.edu.ufcg.dsc.ck.tasks;

import java.util.HashMap;
import java.util.Set;

import br.edu.ufcg.dsc.util.Comparador;



public class GenericTask implements Task {
	
	/* This variable will store the required classes. */
	private Set<String> required;
	
	/* This variable will store the provided classes.*/
	private Set<String> provided;
	
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
		GenericTask other = (GenericTask) obj;
		if (provided == null) {
			if (other.provided != null)
				return false;
		} else if (!Comparador.equalSets(provided, other.provided))
			return false;
		if (required == null) {
			if (other.required != null)
				return false;
		} else if (!Comparador.equalSets(required, other.required))
			return false;
		return true;
	}


	/**
	 * Initializes the set of provided and required classes. <br></br>
	 * @param provided
	 * @param required
	 */
	public GenericTask(Set<String> provided, Set<String> required) {
		this.required = required;
		this.provided = provided;
	}
	
	/**
	 * is the set of required and provided classes empty ?
	 * @return returns true or false.
	 */
	public boolean isEmpty() {
		return (this.required.isEmpty() && this.provided.isEmpty());
	}

	/**
	 * This method adds a class to the set of provided classes.
	 */
	@Override
	public boolean addToProvided(String constant, String path) {
		return this.provided.add(constant);
	}

	/**
	 * This method removes a class "c" from the set of provided classes.
	 */
	@Override
	public boolean removeFromProvided(String c) {
		return this.provided.remove(c);
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
		return this.provided.remove(c);
	}

	/**
	 * This method returns the set of provided classes.
	 */
	@Override
	public HashMap<String,String> getProvided() {
		HashMap<String, String> result = new HashMap<String, String>();
		for(String str : this.provided){
			result.put(str,null);
		}
		return result;
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
		this.provided = interfaces.keySet();
	}

	/**
	 * This method sets the set of required classes.
	 */
	@Override
	public void setRequired(Set<String> interfaces) {
		this.required = interfaces;
	}

	/**
	 * Object ToString.
	 */
	@Override
	public String toString() {
		String result = "";
		result += "Provided: " + this.provided + "\n";
		result += "Required: " + this.required + "\n";
		return result;
	}

}
