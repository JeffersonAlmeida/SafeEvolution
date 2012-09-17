package br.edu.ufcg.dsc.ck.tasks;

import java.util.HashMap;
import java.util.Set;

import br.edu.ufcg.dsc.util.Comparador;

public class SelectClass implements Task {
	
	private Set<String> required;
	private HashMap<String, String> provided;

	public SelectClass(HashMap<String, String> provided, Set<String> required) {
		this.required = required;
		this.provided = provided;
	}

	@Override
	public boolean addToProvided(String mappingConstant, String path) {
		String existingValue = this.provided.put(mappingConstant,path);
		
		return existingValue == null;
	}

	@Override
	public boolean removeFromProvided(String c) {
		String existingValue = this.provided.remove(c);
		
		return existingValue != null;
	}

	@Override
	public boolean addToRequired(String c) {
		return this.required.add(c);
	}

	@Override
	public boolean removeFromRequired(String c) {
		return this.required.remove(c);
	}

	@Override
	public HashMap<String, String> getProvided() {
		return this.provided;
	}

	@Override
	public Set<String> getRequired() {
		return this.required;
	}
	
	@Override
	public void setProvided(HashMap<String,String> interfaces) {
		this.provided = interfaces;
	}
	
	@Override
	public void setRequired(Set<String> interfaces) {
		this.required = interfaces;
	}

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
	
	public boolean isEmpty() {
		return (this.required.isEmpty() && this.provided.isEmpty());
	}
}
