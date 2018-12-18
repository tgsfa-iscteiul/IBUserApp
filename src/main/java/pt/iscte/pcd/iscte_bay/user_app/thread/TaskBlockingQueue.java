package pt.iscte.pcd.iscte_bay.user_app.thread;

import java.util.LinkedList;
import java.util.Queue;

public class TaskBlockingQueue {

	private Queue<Runnable> tasks = new LinkedList<Runnable>();

	public synchronized void add(Runnable r) {
		System.out.println("Vou adcionar task ");
		tasks.add(r);
		notifyAll();
		System.out.println(" notifiquei todos task ");

	}

	public synchronized Runnable take() throws InterruptedException {
		System.out.println("taking from blocking queue");
		while (tasks.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("Fui interrompido. TASKBLOCKINGQUEUE");
			}
		}
		Runnable r = tasks.remove();
		System.out.println("took from blocking queue");

		return r;
	}

}
