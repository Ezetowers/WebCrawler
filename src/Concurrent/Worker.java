import java.lang.Thread;
import Logger.Logger


public abstract class Worker<Pool_Common_Data> extends Thread {
	public Worker(Pool_Common_Data data) {
		data_ = data;
	}

	public void run() {
		while (not Thread.interrupted()) {
			this.svc();
		}
	}

	public abstract svc();

	private Pool_Common_Data data_;
}