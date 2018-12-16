package pt.iscte.pcd.client;

import java.util.LinkedList;
import java.util.Queue;

public class TaskBlockingQueue {

	private Queue<Runnable> tasks = new LinkedList<Runnable>();

	public synchronized void add(Runnable r) {
		tasks.add(r);
		notifyAll();
	}

	public synchronized Runnable take() throws InterruptedException {
		while (tasks.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		Runnable r = tasks.remove();
		return r;
	}

}
