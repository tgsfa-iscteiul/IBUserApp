package pt.iscte.pcd.iscte_bay.user_app.thread;



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
				(new Thread(task)).start();
				System.out.println("Ran task");
				threadPool.updateTaskDoneNumber();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

