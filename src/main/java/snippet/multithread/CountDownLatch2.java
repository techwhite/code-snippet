package snippet.multithread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
使用场景:
1. 实现最大的并行性：有时我们想同时启动多个线程，实现最大程度的并行性。例如，我们想测试一个单例类。如果我们创建一个初始计数为1的CountDownLatch，并让所有线程都在这个锁上等待，那么我们可以很轻松地完成测试。我们只需调用 一次countDown()方法就可以让所有的等待线程同时恢复执行。
2. 开始执行前等待n个线程完成各自任务：例如应用程序启动类要确保在处理用户请求前，所有N个外部系统已经启动和运行了。
3. 死锁检测：一个非常方便的使用场景是，你可以使用n个线程访问共享资源，在每次测试阶段的线程数目是不同的，并尝试产生死锁。
*/
public class CountDownLatch2 {
    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 10, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5));
        int count = 10;
        final CountDownLatch latch = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            threadPool.execute(new MyRunnable1(latch, i));
        }

        latch.await();
        System.err.println("等待线程被唤醒！");
        threadPool.shutdown();
    }

    static class MyRunnable1 implements Runnable {

        CountDownLatch latch = null;
        int i;
    
        public MyRunnable1(CountDownLatch latch, int i) {
            this.latch = latch;
            this.i = i;
        }
    
        @Override
        public void run() {
            System.err.println("线程" + i +"完成了操作...");
            try {
                Thread.currentThread();
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latch.countDown();
        }
    
    }
}

/*运行结果：
线程0完成了操作...
线程3完成了操作...
线程2完成了操作...
线程1完成了操作...
线程4完成了操作...//暂停4秒
线程5完成了操作...
线程6完成了操作...
线程8完成了操作...
线程7完成了操作...
线程9完成了操作...//暂停4秒
等待线程被唤醒！
*/