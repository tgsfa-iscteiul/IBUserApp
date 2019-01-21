package pt.iscte.pcd.iscte_bay.user_app.thread;

import java.util.LinkedList;
import java.util.Queue;

public class TaskBlockingQueue {

	private final Queue<Object> tasks = new LinkedList<Object>();

	public synchronized void add(Object r) {
		tasks.add(r);
		notifyAll();
	}

	public synchronized Object take() throws InterruptedException {
		while (tasks.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("Fui interrompido. TASKBLOCKINGQUEUE");
			}
		}
		Object r = tasks.remove();
		notifyAll();
		return r;
	}
	
	public int getSize() {
		return tasks.size();
	}

}
