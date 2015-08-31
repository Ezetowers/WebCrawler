package concurrent;

import java.lang.Thread;
import java.util.concurrent.BlockingQueue;
import logger.Logger;
import logger.LogLevel;


public abstract class Worker<TASK> extends Thread {
	public Worker(long threadId, String logPrefix, BlockingQueue<TASK> queue) {
		queue_ = queue;
		threadId_ = threadId;
		logPrefix_ = "[" + logPrefix + " Thread ID: " + threadId_ + "] ";
	}

	public void run() {
		Logger.getInstance().log(LogLevel.INFO, logPrefix_ + "Proceed to start Thread");
		while (Thread.interrupted() == false) {
			try {
				this.execute();
			}
			catch (InterruptedException e) {
				break;
			}
        }

        Logger.getInstance().log(LogLevel.INFO, logPrefix_ + "Thread ended.");
	}

	public abstract void execute() throws InterruptedException;

	protected BlockingQueue<TASK> queue_;
	protected long threadId_;
	protected String logPrefix_;
}