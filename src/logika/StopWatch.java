package logika;

public class StopWatch implements Runnable {

	private long startTime = 0;
	private long elapsedTime = 0;
	
	public void run() {
		start();
	}
	
	
	public void start() {
		this.startTime = System.nanoTime();
	}	

	public void stop() {
		elapsedTime = elapsedTime + (System.nanoTime() - startTime);		
	}
	
	public void reset(){
		startTime = 0;
		elapsedTime = 0;
	}
		
	// elaspsed time in milliseconds
	public long getElapsedTime() {
		long elapsed;

		elapsed = elapsedTime;

		return elapsed;
	}

	
	
	
}