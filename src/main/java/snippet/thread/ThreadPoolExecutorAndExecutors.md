# 线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式，这样的处理方式让写的同学更加明确线程池的运行规则，规避资源耗尽的风险。

## ThreadPoolExecutor

```java
public class ThreadPoolExecutor extends AbstractExecutorService {

public abstract class AbstractExecutorService implements ExecutorService {

/**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters.
     *
     * @param corePoolSize 核心线程数，核心线程会一直存活，即使没有任务需要处理。但如果设置了allowCoreThreadTimeOut `为 true 则核心线程也会超时退出。
     * @param maximumPoolSize 最大线程数，线程池中可允许创建的最大线程数。
     * @param keepAliveTime 当线程池中的线程数大于核心线程数，那些多余的线程空闲时间达到keepAliveTime后就会退出，直到线程数量等于corePoolSize。如果设置了allowCoreThreadTimeout设置为true，则所有线程均会退出直到线程数量为0。
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue 在任务执行前用来保存任务的 阻塞队列。这个队列只会保存通过execute方法提交到线程池的Runnable任务。在ThreadPoolExecutor线程池的API文档中，一共推荐了三种等待队列，它们是：SynchronousQueue、LinkedBlockingQueue 和 ArrayBlockingQueue。
     * @param threadFactory 线程池创建新线程时使用的factory。默认使用defaultThreadFactory创建线程。
     * @param handler 当线程池的线程数已达到最大，并且任务队列已满时来处理被拒绝任务的策略。默认使用ThreadPoolExecutor.AbortPolicy，任务被拒绝时将抛出RejectExecutorException
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              ThreadFactory threadFactory,
                              RejectedExecutionHandler handler) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
```

### 参数的解释

```text
　　　　为了更好地理解这些参数在使用上的一些关系，可以将它们进行详细化的注释：

　　　　（1）A代表execute（runnable）欲执行的runnable的数量；

　　　　（2）B代表corePoolSize；

　　　　（3）C代表maximumPoolSize；

　　　　（4）D代表A-B(假设A>=B)；

　　　　（5）E代表newLinkedBlockingDeque<Runnable>()队列，无构造函数。

　　　　（6）F代表SynchronousQueue队列；

　　　　（7）G代表keepAliveTime；

　　　　在使用线程池的过程下，会出现以下的集中情况：

　　　　（1）如果A<=B,那么马上创建线程运行这个任务，并不放入扩展队列Queue中，其他参数功能忽略；

　　　　（2）如果A>B&&A<=C&&E,则C和G参数忽略，并把D放入E中等待被执行；

　　　　（3）如果A>B&&A<=C&&F,则C和G参数有效，并且马上创建线程运行这些任务，而不把D放入F中，D执行完任务后在指定时间后发生超时时将D进行清除。

　　　　（4）如果A>B&&A>C&&E,则C和G参数忽略，并把D放入E中等待被执行；

　　　　（5）如果A>B&&A>C&&F,则处理C的任务，其他任务则不再处理抛出异常；

 

　　　　方法getActiveCount()的作用是取得有多少个线程正在执行任务。

　　　　方法getPoolSize()的作用是获得当前线程池里面有多少个线程，这些线程数包括正在执行任务的线程，也包括正在休眠的线程。

　　　　方法getCompletedTaskCount()的作用是取得有多少个线程已经执行完任务了。

　　　　方法getCorePoolSize()的作用是取得构造方法传入的corePoolSize参数值。

　　　　方法getMaximumPoolSize()的作用是取得构造方法传入的maximumPoolSize的值。

　　　　方法getTaskCount()的作用是取得有多少个任务发送给了线程池。

　　　　方法shutdown()的作用是使当前未执行完的线程继续执行，而不再添加新的任务task，该方法不会阻塞，调用之后，主线程main马上结束，而线程池会继续运行直到所有任务执行完才会停止。

　　　　方法shutdownNow()的作用是中断所有的任务task，并且抛出InterruptedException异常，前提是在Runnable中使用if（Thread.currentThread().isInterrupted()==true）语句来判断当前线程的中断状态，而未执行的线程不再执行，也就是从执行队列中清除。如果不手工加if语句及抛出异常，则池中正在运行的线程知道执行完毕，而未执行的线程不再执行，也从执行队列中清除。

　　　　方法isShutDown()的作用是判断线程池是否已经关闭。　　

　　　　方法isTerminating()的作用是判断线程池是否正在关闭中。

　　　　方法isTerminated()的作用是判断线程池是否已经关闭。

　　　　方法awaitTermination(long timeout,TimeUnit unit)的作用是查看在指定的时间之内，线程池是否已经终止工作，也就是最多等待多少时间后去判断线程池是否已经终止工作。

　　　　方法allowsCoreThreadTimeOut(boolean) 的作用是配置核心线程是否有超时的效果。

　　　　方法prestartCoreThread()的作用是每调用一次就创建一个核心线程，返回值为boolean。

　　　　方法prestartAllCoreThreads()的作用是启动全部核心线程，返回值是启动核心线程的数量。
```

### 关注点1 线程池大小

线程池有两个线程数的设置，一个为核心池线程数，一个为最大线程数。
在创建了线程池后，默认情况下，线程池中并没有任何线程，等到有任务来才创建线程去执行任务，除非调用了prestartAllCoreThreads()或者prestartCoreThread()方法
当创建的线程数等于 corePoolSize 时，会加入设置的阻塞队列。当队列满时，会创建线程执行任务直到线程池中的数量等于maximumPoolSize。

### 关注点2 适当的阻塞队列

详见 queue->BlockingQueue.md

### 关注点3 明确拒绝策略

```java
/* Predefined RejectedExecutionHandlers */

    /**
     * A handler for rejected tasks that runs the rejected task
     * directly in the calling thread of the {@code execute} method,
     * unless the executor has been shut down, in which case the task
     * is discarded.
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code CallerRunsPolicy}.
         */
        public CallerRunsPolicy() { }

        /**
         * Executes task r in the caller's thread, unless the executor
         * has been shut down, in which case the task is discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    /**
     * A handler for rejected tasks that throws a
     * {@code RejectedExecutionException}.
     */
    public static class AbortPolicy implements RejectedExecutionHandler {
        /**
         * Creates an {@code AbortPolicy}.
         */
        public AbortPolicy() { }

        /**
         * Always throws RejectedExecutionException.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         * @throws RejectedExecutionException always
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                                                 " rejected from " +
                                                 e.toString());
        }
    }

    /**
     * A handler for rejected tasks that silently discards the
     * rejected task.
     */
    public static class DiscardPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code DiscardPolicy}.
         */
        public DiscardPolicy() { }

        /**
         * Does nothing, which has the effect of discarding task r.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }
    }

    /**
     * A handler for rejected tasks that discards the oldest unhandled
     * request and then retries {@code execute}, unless the executor
     * is shut down, in which case the task is discarded.
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        /**
         * Creates a {@code DiscardOldestPolicy} for the given executor.
         */
        public DiscardOldestPolicy() { }

        /**
         * Obtains and ignores the next task that the executor
         * would otherwise execute, if one is immediately available,
         * and then retries execution of task r, unless the executor
         * is shut down, in which case task r is instead discarded.
         *
         * @param r the runnable task requested to be executed
         * @param e the executor attempting to execute this task
         */
        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
```

## Executors

Executors 各个方法的弊端：
1）newFixedThreadPool 和 newSingleThreadExecutor:
主要问题是堆积的请求处理队列可能会耗费非常大的内存，甚至 OOM。
2）newCachedThreadPool 和 newScheduledThreadPool:
主要问题是线程数最大数是 Integer.MAX_VALUE，可能会创建数量非常多的线程，甚至 OOM。