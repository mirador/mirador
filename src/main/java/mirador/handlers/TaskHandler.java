package mirador.handlers;

import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

public class TaskHandler {
  final static public int NUM_FREE_PROCESSORS = 1;

  protected ThreadPoolExecutor taskPoolHP;
  protected ThreadPoolExecutor taskPoolLP;

  public TaskHandler() {
    // By default, 70% of the available processors are available to the high-priority pool, and the rest to the low
    // priority pool. That's the only difference between the two.
    initTaskPools(0.7f, 0.3f);
  }

  public TaskHandler(float higp, float lowp) {
    initTaskPools(higp, lowp);
  }

  @SuppressWarnings("unchecked")
  public FutureTask<Object> submit(Runnable task, boolean highp) {
    if (highp) {
      return (FutureTask<Object>) taskPoolHP.submit(task);
    } else {
      return (FutureTask<Object>) taskPoolLP.submit(task);
    }
  }

  private void initTaskPools(float higp, float lowp) {
    int proc = Runtime.getRuntime().availableProcessors();
    int tot = proc - NUM_FREE_PROCESSORS;
    taskPoolHP = (ThreadPoolExecutor)Executors.newFixedThreadPool(Math.max(1, (int)(higp * tot)));
    taskPoolLP = (ThreadPoolExecutor)Executors.newFixedThreadPool(Math.max(1, (int)(lowp * tot)));
  }

  public void printDebug() {
    long count1 = taskPoolHP.getTaskCount() - taskPoolHP.getCompletedTaskCount();
    long count2 = taskPoolLP.getTaskCount() - taskPoolLP.getCompletedTaskCount();
    System.out.println("number of pending high-priority tasks: " + count1);
    System.out.println("number of pending low-priority tasks : " + count2);
  }
}
