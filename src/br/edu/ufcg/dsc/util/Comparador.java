package br.edu.ufcg.dsc.util;

import java.util.Collection;

public class Comparador {

	public static boolean equalSets(Collection<?> source, Collection<?> target) {
		if (source.size() == target.size()) {
			for (Object o : source) {
				if (!target.contains(o)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static boolean containsSome(Collection<?> source, Collection<?> target) {
		for (Object object : target) {
			if(source.contains(object)){
				return true;
			}
		}
		return false;
	}
}
