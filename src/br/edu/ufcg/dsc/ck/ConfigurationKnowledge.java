package br.edu.ufcg.dsc.ck;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import br.edu.ufcg.dsc.Constants;
import br.edu.ufcg.dsc.util.Comparador;

/** @author Jefferson Almeida - jra at cin dot ufpe dot br  <br></br>
   This class represents a Configuration Knowledge of the SPL.
 */
public class ConfigurationKnowledge {

	 /**A set of Configuration Knowledge items.*/
	private Set<ConfigurationItem> ckItems;
	
	private String defauLt;
	private String predProvided;
	private String predRequired;
	private String predCK;
	private String sigs;
	private HashSet<String> signatures;

	public ConfigurationKnowledge(Set<ConfigurationItem> ckItems) {
		this.ckItems = ckItems;
	}

	public Set<ConfigurationItem> getCkItems() {
		return ckItems;
	}

	public void setCkItems(Set<ConfigurationItem> ckItems) {
		this.ckItems = ckItems;
	}

	public void addCKitem(ConfigurationItem item) {
		this.ckItems.add(item);
	}

	public void removeCKitem(ConfigurationItem item) {
		this.ckItems.remove(item);
	}

	public Hashtable<String, Set<String>> evalCK(Set<String> features) {
		Hashtable<String, Set<String>> interfaces = new Hashtable<String, Set<String>>();
		Set<String> provided = new HashSet<String>();
		Set<String> required = new HashSet<String>();

		for (ConfigurationItem item : this.ckItems) {
			if (item.getFeatExp().evaluate(features)) {
				provided.addAll(item.getProvidedItem());
				required.addAll(item.getRequiredItem());
			}
		}

		interfaces.put("provided", provided);
		interfaces.put("required", required);
		
		return interfaces;
	}
	
	/**
	 * Returns all provided classes to compiles these features.
	 * @param features A set of features that compose a product.
	 * @return HashMap<String, String> of provided classes
	 */
	public HashMap<String, String> evalCKDestinos(Set<String> features) {
		HashMap<String, String> provided = new HashMap<String, String>();
		for (ConfigurationItem item : this.ckItems) {
			if (item.getFeatExp().evaluate(features)) {
				provided.putAll(item.getProvidedItemDestinos());
			}
		}
		return provided;
	}

	@Override
	public String toString() {
		String result = "";
		for (ConfigurationItem item : this.ckItems) {
			result += item.toString() + Constants.LINE_SEPARATOR;
		}
		return result;
	}

	public String getDefauLt() {
		return defauLt;
	}

	public String getPredProvided() {
		return predProvided;
	}

	public String getPredRequired() {
		return predRequired;
	}

	public String getPredCK() {
		return predCK;
	}

	public String getSigs() {
		return sigs;
	}

	public HashSet<String> getSignatures() {
		return signatures;
	}

	public void buildAlloy() {
		defauLt = "open default" + Constants.LINE_SEPARATOR;
		predProvided = " pred provided[] { " + Constants.LINE_SEPARATOR;
		predRequired = " pred required[] { " + Constants.LINE_SEPARATOR;
		predCK = " pred semanticaCK[] { provided[] => required[] }"
				+ Constants.LINE_SEPARATOR;
		String separador = "";
		signatures = new HashSet<String>();

		for (ConfigurationItem item : this.ckItems) {
			String featExp = item.getFeatExp().toString().toLowerCase();
//			String featExp = item.getFeatExp().toString();

			signatures.addAll(item.getProvidedItem());
			signatures.addAll(item.getRequiredItem());
			signatures.add(featExp);

			Map<String, String> cItem = item.toAlloy();
			String string = cItem.get("provided");
			if (string != null && !string.trim().equals("")) {
				predProvided += "( isTrue[" + featExp + "] => (" + string
						+ ") )" + Constants.LINE_SEPARATOR;
			}
			String string2 = cItem.get("required");
			if (string2 != null && !string2.trim().equals("")) {
				predRequired += "( isTrue[" + featExp + "] => (" + string2
						+ ") )" + Constants.LINE_SEPARATOR;
			}

		}
		for (String string : signatures) {
			if (string != null) {
//				sigs += separador + string.toLowerCase();
				sigs += separador + string;
				separador = ", ";
			}
		}
		predProvided += " }" + Constants.LINE_SEPARATOR
				+ Constants.LINE_SEPARATOR;
		predRequired += " }" + Constants.LINE_SEPARATOR
				+ Constants.LINE_SEPARATOR;
	}

	public String toAlloy() {
		buildAlloy();
		String pred = defauLt + predProvided + predRequired + predCK;
		return pred;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ckItems == null) ? 0 : ckItems.hashCode());
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
		ConfigurationKnowledge other = (ConfigurationKnowledge) obj;
		return Comparador.equalSets(this.ckItems, other.getCkItems());
	}

	public HashMap<String, String> getClassesComMaisDeUmaVersao() {
		HashMap<String, String> result = new HashMap<String, String>();

		for (ConfigurationItem item : this.ckItems) {
			//Mapeamento Constante -> Destino
			HashMap<String, String> mapeamentos = item.getProvidedItemDestinos();
			
			for(String constante : mapeamentos.keySet()){
				if(mapeamentos.get(constante) != null){
					result.put(constante, (mapeamentos.get(constante)));
				}
			}
		}
		return result;
	}

	public void print(String plName) {
		System.out.println("\n" + plName + " Configuration Knowledge Items:\n" + this.toString());
	}
	
	

}
