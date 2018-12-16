package pt.iscte.pcd.client;

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
	
	
	public synchronized void submit(Runnable taskToDo) {
		tasks.add(taskToDo);
	}

	public synchronized Runnable getTask() throws InterruptedException {
		return tasks.take();
	}

	public synchronized void updateTaskDoneNumber() {
		numTasksDone++;
	}
}
