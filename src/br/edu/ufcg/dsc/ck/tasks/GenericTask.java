package br.edu.ufcg.dsc.ck.tasks;

import java.util.HashMap;
import java.util.Set;

import br.edu.ufcg.dsc.util.Comparador;



public class GenericTask implements Task {
	
	private Set<String> required;
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



	public GenericTask(Set<String> provided, Set<String> required) {
		this.required = required;
		this.provided = provided;
	}

	public boolean isEmpty() {
		return (this.required.isEmpty() && this.provided.isEmpty());
	}

	@Override
	public boolean addToProvided(String constant, String path) {
		return this.provided.add(constant);
	}

	@Override
	public boolean removeFromProvided(String c) {
		return this.provided.remove(c);
	}

	@Override
	public boolean addToRequired(String c) {
		return this.required.add(c);
	}

	@Override
	public boolean removeFromRequired(String c) {
		return this.provided.remove(c);
	}

	@Override
	public HashMap<String,String> getProvided() {
		HashMap<String, String> result = new HashMap<String, String>();
		
		for(String str : this.provided){
			result.put(str,null);
		}
		
		return result;
	}

	@Override
	public Set<String> getRequired() {
		return this.required;
	}

	@Override
	public void setProvided(HashMap<String,String> interfaces) {
		this.provided = interfaces.keySet();
	}

	@Override
	public void setRequired(Set<String> interfaces) {
		this.required = interfaces;
	}

	@Override
	public String toString() {
		String result = "";
		result += "Provided: " + this.provided + "\n";
		result += "Required: " + this.required + "\n";
		return result;
	}

}
