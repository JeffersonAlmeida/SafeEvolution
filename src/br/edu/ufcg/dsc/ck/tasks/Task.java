package br.edu.ufcg.dsc.ck.tasks;

import java.util.HashMap;
import java.util.Set;

public interface Task {
	public boolean addToRequired(String c);
	public boolean removeFromRequired(String c);
	public boolean equals(Object o);
	
	public Set<String> getRequired();
	public void setRequired(Set<String> interfaces);
	
	public boolean addToProvided(String amConstant,String path);
	public boolean removeFromProvided(String constant);
	
	public HashMap<String, String> getProvided();
	public void setProvided(HashMap<String, String> interfaces);
}
