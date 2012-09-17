package br.edu.ufcg.dsc.builders;

public class BuilderTime {

	private long timeCompileSourceProduct;
	private long timeCompileTargetProduct;
	private long generationTime;
	
	public BuilderTime(long sT, long tT, long generation){
		this.timeCompileSourceProduct = sT;
		this.timeCompileTargetProduct = tT;
		this.generationTime = generation;
	}

	public long getGenerationTime() {
		return generationTime;
	}

	public long getTimeCompileSourceProduct() {
		return timeCompileSourceProduct;
	}

	public long getTimeCompileTargetProduct() {
		return timeCompileTargetProduct;
	}
	
	
}
