package pt.iscte.pcd.iscte_bay.user_app.thread;

public class BlockThreadPool {

	private int numWorkers;
	TaskBlockingQueue tasks;
	private int numTasksDone;

	public BlockThreadPool(int numWorkers) {
		numTasksDone = 0;
		this.numWorkers = numWorkers;
		tasks = new TaskBlockingQueue();
		for (int i = 0; i < numWorkers; i++) {
			new WorkerThread(this).start();
		}
	}
	
	
	public void submit(Object taskToDo) {
		tasks.add(taskToDo);
	}

	public  Object getTask() throws InterruptedException {
		return tasks.take();
	}

	public  void updateTaskDoneNumber() {
		numTasksDone++;
	}
}
