package com.sos.JSHelper.concurrent;

/**
 * @author KB
 *
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SOSThreadPoolExecutor {
	//Parallel running Threads(Executor) on System
	public int									corePoolSize	= 5;

	//Maximum Threads allowed in Pool
	public int									maxPoolSize		= 8;

	//Keep alive time for waiting threads for jobs(Runnable)
	public long								keepAliveTime	= 10;

	//This is the one who manages and start the work
	public ExecutorService						objThreadPool	= null;

	//Working queue for jobs (Runnable). We add them finally here
//	private final ArrayBlockingQueue<Runnable>	workQueue		= new ArrayBlockingQueue<Runnable>(maxPoolSize + 1);

	public SOSThreadPoolExecutor() {
		objThreadPool = Executors.newFixedThreadPool(corePoolSize);
	}

	public SOSThreadPoolExecutor(final int pintCorePoolSize) {
		objThreadPool = Executors.newFixedThreadPool(pintCorePoolSize);
		corePoolSize = pintCorePoolSize;
	}

	@SuppressWarnings("unchecked")
	public Future<Runnable> runTask(final Runnable task) {
//				objThreadPool.execute(task);
		Future<Runnable> objFut = (Future<Runnable>) objThreadPool.submit(task);
//				System.out.println("Tasks in workQueue.." + workQueue.size());
		return objFut;
	}

	public void shutDown() {
		objThreadPool.shutdown();
	}

	public static void main(final String args[]) {
		SOSThreadPoolExecutor mtpe = new SOSThreadPoolExecutor(10);
		int cpus = Runtime.getRuntime().availableProcessors();
		System.out.println("max avl cpus = " + cpus) ;
		for (int i = 0; i < 19; i++) {
			Future objF = mtpe.runTask(new WorkerRunnable(i));
		}

		mtpe.shutDown();
		System.out.println("Finished! ");
	}

	private static class WorkerRunnable implements Runnable {

		private final int	jobNr;

		/**
		 *
		 * @param jobNr number for displaying
		 */
		public WorkerRunnable(final int jobNr) {
			this.jobNr = jobNr;
		}

		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {

				try {
					long intNo = Thread.currentThread().getId();
					System.out.println("Task " + jobNr + ", calculated " + i + ", Thread ID = " + intNo);
					Thread.currentThread().sleep(100);
				}
				catch (InterruptedException ie) {
					ie.printStackTrace();
				}
			}
		}
	}
}