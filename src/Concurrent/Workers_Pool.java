package concurrent;

import concurrent.Workers_Factory;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import logger.Logger;

public class Workers_Pool<TASK> {
	public Workers_Pool(int amount_workers, Workers_Factory factory) {
		factory_ = factory;
		workers_ = new ArrayList<Thread>();
		amount_workers_ = amount_workers;

		for (int i = 0; i < amount_workers_; ++i) {
			// Initialize Workers Pool
			Thread worker = factory_.make();
			workers_.add(worker);
		}
	}

	public void start() {
		for (Thread worker : workers_) {
			worker.start();
		}
	}

	public void stop() {
		for (Thread worker : workers_) {
			try {
				worker.interrupt();
				worker.join();
			}
			catch (InterruptedException e) {
			}
		}
	}

	/* public Boolean add_task(TASK task) {
		queue_.add(task);
	}*/


	private int amount_workers_;
	private Workers_Factory factory_;
	private ArrayList<Thread> workers_;
	// private BlockingQueue<TASK> queue_;
}