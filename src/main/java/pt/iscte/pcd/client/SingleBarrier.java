package pt.iscte.pcd.client;

public class SingleBarrier {

	private int currentPosters;
	private int totalPosters;
	private int passedWaiters;
	private int totalWaiters;

	public SingleBarrier(int totalWaiters) {
		this.totalWaiters = totalWaiters;
	}

	public synchronized void init(int i) {
		totalPosters = i;
		currentPosters = 0;
	}

	public synchronized void barrierWait() {
		boolean interrupted = false;
		while (currentPosters != totalPosters) {
			try {
				wait();
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		passedWaiters++;
		if (passedWaiters == totalWaiters) {
			currentPosters = 0;
			passedWaiters = 0;
			notifyAll();
		}
		if (interrupted)
			Thread.currentThread().interrupt();
	}

	public synchronized void barrierPost() {
		boolean interrupted = false;
		// In case a poster thread beats barrierWait,
		// keep count of posters.
		while (currentPosters == totalPosters) {
		try {wait();}
		catch (InterruptedException ie)
		{interrupted=true;}
		}
		currentPosters++;
		if (currentPosters == totalPosters) notifyAll();
		if (interrupted)
		Thread.currentThread().interrupt();
		}

}
