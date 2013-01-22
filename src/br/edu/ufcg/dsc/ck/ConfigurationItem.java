package br.edu.ufcg.dsc.ck;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.ck.featureexpression.IFeatureExpression;
import br.edu.ufcg.dsc.ck.tasks.Task;
import br.edu.ufcg.dsc.util.Comparador;

public class ConfigurationItem {

	private IFeatureExpression featExp;
	private Set<Task> tasks;

	/**
	 * Constructs a configuration Item with a feature expression and a set of tasks.
	 * @param featExp
	 * @param tasks
	 */
	public ConfigurationItem(IFeatureExpression featExp, Set<Task> tasks) {
		this.featExp = featExp;
		this.tasks = tasks;
	}

	public IFeatureExpression getFeatExp() {
		return featExp;
	}

	public void setFeatExp(IFeatureExpression featExp) {
		this.featExp = featExp;
	}

	public Set<Task> getTasks() {
		return tasks;
	}

	public void setTasks(Set<Task> tasks) {
		this.tasks = tasks;
	}

	public boolean addTask(Task task) {
		return this.tasks.add(task);
	}

	public boolean removeTask(Task task) {
		return this.tasks.remove(task);
	}

	public Set<String> getRequiredItem() {
		Set<String> required = new HashSet<String>();
		for (Task task : this.tasks) {
			required.addAll(task.getRequired());
		}
		return required;
	}

	public Set<String> getProvidedItem() {
		Set<String> provided = new HashSet<String>();
		for (Task task : this.tasks) {
			provided.addAll(task.getProvided().keySet());
		}
		return provided;
	}

	@Override
	public String toString() {
		String result = "";
		result += "Feature Expression: " + this.featExp
				+ Constants.LINE_SEPARATOR;
		for (Task t : this.tasks) {
			result += t.toString();
		}
		return result;
	}

	public Map<String, String> toAlloy() {
		Map<String, String> associations = new HashMap<String, String>();
		String provided = "", required = "";
		for (Task t : this.tasks) {
			for (String p : (t.getProvided().keySet())) {
	//			p = p.toLowerCase().trim();
				p = p.trim();
				if (!p.equals("")) {
					provided += "isTrue[" + p + "] and ";
				}
			}
			for (String p : (t.getRequired())) {
	//			p = p.toLowerCase().trim();
				p = p.trim();
				if (!p.equals("")) {
					required += "isTrue[" + p + "] and ";
				}
			}
		}
		if (!provided.equals("")) {
			provided = provided.substring(0, provided.length() - 5);
			associations.put("provided", provided);
		} else {
			provided = "";
		}
		if (!required.equals("")) {
			required = required.substring(0, required.length() - 5);
			associations.put("required", required);
		} else {
			required = "";
		}
		return associations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((featExp == null) ? 0 : featExp.hashCode());
		result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
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
		ConfigurationItem other = (ConfigurationItem) obj;
		if (featExp == null) {
			if (other.featExp != null)
				return false;
		} else if (!featExp.equals(other.featExp))
			return false;
		if (tasks == null) {
			if (other.tasks != null)
				return false;
		} else if (!Comparador.equalSets(tasks, other.tasks))
			return false;
		return true;
	}

	public HashMap<String,String> getProvidedItemDestinos() {
		/* Provided Items to compile a class.*/
		HashMap<String,String> provided = new HashMap<String,String>();
		for (Task task : this.tasks) {
			/*Add all provided classes here in the 	HashMap<String,String>*/
			provided.putAll(task.getProvided());
		}
		
		Iterator<String> i = provided.keySet().iterator();
		String all = "";
		while(i.hasNext()){
			String  p = (String) i.next();
			all = all + "\n [ " + p + " ] - " + provided.get(p) + "";
		}
		System.out.println(all + "");
		return provided;
	}

}
