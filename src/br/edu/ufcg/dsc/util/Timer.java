package br.edu.ufcg.dsc.util;

public class Timer {
	
	private long base;
	private long total;
	
	public Timer() {
		this.reset();
	}
	
	public void startContinue(){
		this.base = System.currentTimeMillis();
	}
	
	public void reset() {
		this.total = 0;
	}

	public void pause(){
		long now = System.currentTimeMillis();
		long diference = now - this.base;
		this.total = this.total + diference;
	}
	
	public long getTotal(){
		return this.total;
	}
}
