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

ThreadPoolExecutor线程池的逻辑结构图:
![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E7%BB%93%E6%9E%84%E5%9B%BE.png)

### 线程池执行任务的行为方式

```text
1. 当线程数小于核心线程数时，创建线程。
2. 当线程数大于等于核心线程数，且任务队列未满时，将任务放入任务队列。
3. 当线程数大于等于核心线程数，且任务队列已满
    1. 若线程数小于最大线程数，创建线程
    2. 若线程数等于最大线程数，抛出异常，拒绝任务
```

![avatar](https://github.com/techwhite/code-snippet/blob/master/src/main/resource/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%89%A7%E8%A1%8C%E4%BB%BB%E5%8A%A1%E6%96%B9%E5%BC%8F.png)

使用举例

```java
public class ThreadPool {
    private static ExecutorService pool;
    public static void main( String[] args )
    {
        //优先任务队列
        pool = new ThreadPoolExecutor(1, 2, 1000, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<Runnable>(),Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());

        for(int i=0;i<20;i++) {
            pool.execute(new ThreadTask(i));
        }
    }
}

public class ThreadTask implements Runnable,Comparable<ThreadTask>{

    private int priority;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ThreadTask() {

    }

    public ThreadTask(int priority) {
        this.priority = priority;
    }

    //当前对象和其他对象做比较，当前优先级大就返回-1，优先级小就返回1,值越小优先级越高
    public int compareTo(ThreadTask o) {
         return  this.priority>o.priority?-1:1;
    }

    public void run() {
        try {
            //让线程阻塞，使后续任务进入缓存队列
            Thread.sleep(1000);
            System.out.println("priority:"+this.priority+",ThreadName:"+Thread.currentThread().getName());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
```

执行结果

```text
priority:0,ThreadName:pool-1-thread-1
priority:9,ThreadName:pool-1-thread-1
priority:8,ThreadName:pool-1-thread-1
priority:7,ThreadName:pool-1-thread-1
priority:6,ThreadName:pool-1-thread-1
priority:5,ThreadName:pool-1-thread-1
priority:4,ThreadName:pool-1-thread-1
priority:3,ThreadName:pool-1-thread-1
priority:2,ThreadName:pool-1-thread-1
priority:1,ThreadName:pool-1-thread-1
```

更多详细请见： https://www.cnblogs.com/dafanjoy/p/9729358.html

## Executors

Executors 各个方法的弊端：
1）newFixedThreadPool 和 newSingleThreadExecutor:
主要问题是堆积的请求处理队列可能会耗费非常大的内存，甚至 OOM。
2）newCachedThreadPool 和 newScheduledThreadPool:
主要问题是线程数最大数是 Integer.MAX_VALUE，可能会创建数量非常多的线程，甚至 OOM。

Executors类是一个工厂类，提供了一系列静态工厂方法来创建不同的ExecutorService或 ScheduledExecutorService实例。

```java
public interface ExecutorService extends Executor {
public interface Executor {

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the {@code Executor} implementation.
     *
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be
     * accepted for execution
     * @throws NullPointerException if command is null
     */
    void execute(Runnable command);
}
```

### 创建3种不同的ExecutorService（线程池）实例

1.newSingleThreadExecutor
创建一个单线程的线程池：启动一个线程负责按顺序执行任务，先提交的任务先执行。

其原理是：任务会被提交到一个队列里，启动的那个线程会从队里里取任务，然后执行，执行完，再从队列里取下一个任务，再执行。如果该线程执行一个任务失败，并导致线程结束，系统会创建一个新的线程去执行队列里后续的任务，不会因为前面的任务有异常导致后面无辜的任务无法执行。
源码：

```java
    public static ExecutorService newSingleThreadExecutor() {
        return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                                    0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>()));
    }
```

代码例子

```java
package com.wjg.unit4_2_5;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Run {
    public static void main(String[] args) {
        Run run = new Run();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 5; i++) {
            executorService.execute(run.new MyRunnable(" "+(i+1)));
        }
    }


    public class MyRunnable implements Runnable{
        private String username;

        public MyRunnable(String username) {
            this.username = username;
        }


        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+" username="+username+" begin "+System.currentTimeMillis());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName()+" username="+username+" end   "+System.currentTimeMillis());
        }

    }
}
```

```text
执行结果：
pool-1-thread-1 username= 1 begin 1488269392403
pool-1-thread-1 username= 1 end   1488269395409
pool-1-thread-1 username= 2 begin 1488269395409
pool-1-thread-1 username= 2 end   1488269398412
pool-1-thread-1 username= 3 begin 1488269398413
pool-1-thread-1 username= 3 end   1488269401418
pool-1-thread-1 username= 4 begin 1488269401418
pool-1-thread-1 username= 4 end   1488269404422
pool-1-thread-1 username= 5 begin 1488269404422
pool-1-thread-1 username= 5 end   1488269407423

由执行结果的线程名字可以看出，线程池中只有一个线程。
```

2.newFixedThreadPool
创建一个可重用的固定线程数量的线程池。即corePoolSize=线程池中的线程数= maximumPoolSize。

如果没有任务执行，所有的线程都将等待。
如果线程池中的所有线程都处于活动状态，此时再提交任务就在队列中等待，直到有可用线程。
如果线程池中的某个线程由于异常而结束时，线程池就会再补充一条新线程。
源码：

```java
    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                                      0L, TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>());
    }
```

3.newCachedThreadPool
创建一个不限制线程数量的动态线程池。

因为有多个线程存在，任务不一定会按照顺序执行。
一个线程完成任务后，空闲时间达到60秒则会被结束。
在执行新的任务时，当线程池中有之前创建的空闲线程就使用这个线程，否则就新建一条线程。
源码：

```java
    public static ExecutorService newCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                      60L, TimeUnit.SECONDS,
                                      new SynchronousQueue<Runnable>());
    }
```

可以看到newCachedThreadPool使用的队列是SynchronousQueue，和前两个是不一样的。线程池的线程数可达到Integer.MAX_VALUE，即2147483647。此外由于会有线程的创建和销毁，所以会有一定的系统开销。

代码例子

```java
package com.wjg.unit4_2_3;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class Run {
    public static void main(String[] args) {
        Run run = new Run();
        MyThreadFactory factory = run.new MyThreadFactory();
        ExecutorService executorService = Executors.newCachedThreadPool(factory);
        for (int i = 0; i < 5; i++) {
            executorService.execute(run.new MyRunnable(" "+(i+1)));
        }
        Thread.sleep(3000);
        System.out.println();
        System.out.println();
        for (int i = 0; i < 5; i++) {
            executorService.execute(run.new MyRunnable(" "+(i+1)));
        }
    }


    public class MyThreadFactory implements ThreadFactory{

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("自定义名称："+new Date());
            return thread;
        }

    }

    public class MyRunnable implements Runnable{
        private String username;

        public MyRunnable(String username) {
            this.username = username;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+" username="+username+" begin "+System.currentTimeMillis());
            System.out.println(Thread.currentThread().getName()+" username="+username+" end   "+System.currentTimeMillis());
        }

    }
}
```

```text
执行结果：
pool-1-thread-1 username= 1 begin 1488268086641
pool-1-thread-3 username= 3 begin 1488268086641
pool-1-thread-2 username= 2 begin 1488268086641
pool-1-thread-2 username= 2 end   1488268086641
pool-1-thread-4 username= 4 begin 1488268086642
pool-1-thread-4 username= 4 end   1488268086642
pool-1-thread-3 username= 3 end   1488268086641
pool-1-thread-1 username= 1 end   1488268086641
pool-1-thread-5 username= 5 begin 1488268086642
pool-1-thread-5 username= 5 end   1488268086642

pool-1-thread-5 username= 1 begin 1488268089647
pool-1-thread-3 username= 3 begin 1488268089648
pool-1-thread-4 username= 4 begin 1488268089648
pool-1-thread-1 username= 2 begin 1488268089647
pool-1-thread-1 username= 2 end   1488268089648
pool-1-thread-4 username= 4 end   1488268089648
pool-1-thread-3 username= 3 end   1488268089648
pool-1-thread-2 username= 5 begin 1488268089648
pool-1-thread-2 username= 5 end   1488268089648
pool-1-thread-5 username= 1 end   1488268089648

通过线程的名字，可以看出来线程是从池中取出来的，是可以复用的。
```

4.newSingleThreadExecutor 与 newFixedThreadPool(1) 的区别
JavaDoc上说：

Unlike the otherwise equivalent newFixedThreadPool(1) the returned executor is guaranteed not to be reconfigurable to use additional threads.
举个例子：

((ThreadPoolExecutor)newFixedThreadPool(1)).setCorePoolSize(3);
即newFixedThreadPool(1)可以后期修改线程数，不保证线程只有一个。而newSingleThreadExecutor可以保证。

### ScheduledExecutorService

关于ScheduledExecutorService的内容，在下一篇文章中介绍。

```java
public interface ScheduledExecutorService extends ExecutorService {
public interface ExecutorService extends Executor {

public interface Executor {

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the {@code Executor} implementation.
     *
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be
     * accepted for execution
     * @throws NullPointerException if command is null
     */
    void execute(Runnable command);
}
```

1.newSingleThreadScheduledExecutor
创建一个单线程的ScheduledExecutorService，在指定延时之后执行或者以固定的频率周期性的执行提交的任务。在线程池关闭之前如果有一个任务执行失败，并导致线程结束，系统会创建一个新的线程接着执行队列里的任务。

源码：

```java
    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1));   //corePoolSize为1
    }
```

还有一个重载的方法，多了一个ThreadFactory参数，ThreadFactory是用来确定新线程应该怎么创建的。

```java
    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return new DelegatedScheduledExecutorService
            (new ScheduledThreadPoolExecutor(1, threadFactory));
    }
```

2.newScheduledThreadPool
创建一个固定线程数的ScheduledExecutorService对象，在指定延时之后执行或者以固定的频率周期性的执行提交的任务。

```java
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }
```

同样的，也有一个重载的方法：

```java
    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }
```