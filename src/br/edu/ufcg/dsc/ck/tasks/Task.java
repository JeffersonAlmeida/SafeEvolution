package br.edu.ufcg.dsc.ck.tasks;

import java.util.HashMap;
import java.util.Set;

/** This interface has all operations you can do with the set of provided and required classes.<br></br>
 * @author Jefferson Almeida - jra at cin dot ufpe dot br  <br></br>
 */
public interface Task {
	
	/**
	 *  This method adds a class to the set of Required classes.
	 * @param c
	 * @return
	 */
	public boolean addToRequired(String c);
	
	/**
	 * This method removes a class "c" from the set of required classes.
	 * @param c
	 * @return
	 */
	public boolean removeFromRequired(String c);
	
	/**
	 * This method compares two objects.
	 * @param o
	 * @return
	 */
	public boolean equals(Object o);
	
	/**
	 *  This method returns the set of required classes.
	 * @return
	 */
	public Set<String> getRequired();
	
	/**
	 * This method sets the set of required classes.
	 * @param interfaces
	 */
	public void setRequired(Set<String> interfaces);
	
	/**
	 * This method adds a class to the set of provided classes.
	 * @param amConstant
	 * @param path
	 * @return
	 */
	public boolean addToProvided(String amConstant,String path);
	
	/**
	 * This method removes a class "c" from the set of provided classes.
	 * @param constant
	 * @return
	 */
	public boolean removeFromProvided(String constant);
	
	/**
	 *  This method returns the set of provided classes.
	 * @return
	 */
	public HashMap<String, String> getProvided();
	
	/**
	 * This method sets the set of provided classes.
	 * @param interfaces
	 */
	public void setProvided(HashMap<String, String> interfaces);
}
