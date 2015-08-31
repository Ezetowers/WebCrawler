import java.lang.Runnable;
import java.lang.Thread;
import java.util.ArrayList;
import Logger.Logger

public class Workers_Pool<Worker> {
	public Workers_Pool(int amount_workers) {
		amount_workers_ = amount_workers;

		for (int i = 0; i < amount_workers_; ++i) {
			// Initialize Workers Pool
			worker = new Worker;
			workers_.push_back(thread);
		}
	}

	public void start() {
		for (worker : workers_) {
			worker.start();
		}
	}

	public void stop() {
		for (worker : workers_) {
			worker.interrupt();
			worker.join();
		}
	}

	private int amount_workers_;
	private ArrayList<Worker> workers_;
}