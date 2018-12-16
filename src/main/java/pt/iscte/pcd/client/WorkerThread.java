package pt.iscte.pcd.client;



public class WorkerThread extends Thread {

	private BlockThreadPool threadPool;

	public WorkerThread( BlockThreadPool threadPool) {
		this.threadPool = threadPool;
	}

	@Override
	public void run() {
		while (!interrupted()) {
			Runnable task;
			try {
				task = threadPool.getTask();
				task.run();
				threadPool.updateTaskDoneNumber();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

