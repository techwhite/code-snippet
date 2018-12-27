package snippet.thread;

import java.util.concurrent.Semaphore;

/**
 * A counting semaphore. Conceptually, a semaphore maintains a set of permits.
 * Each {@link #acquire} blocks if necessary until a permit is available, and
 * then takes it. Each {@link #release} adds a permit, potentially releasing a
 * blocking acquirer. However, no actual permit objects are used; the
 * {@code Semaphore} just keeps a count of the number available and acts
 * accordingly.
 * 
 * public Semaphore(int permits) { //参数permits表示许可数目，即同时可以允许多少线程进行访问 sync = new
 * NonfairSync(permits); } public Semaphore(int permits, boolean fair) {
 * //这个多了一个参数fair表示是否是公平的，即等待时间越久的越先获取许可 sync = (fair)? new FairSync(permits) :
 * new NonfairSync(permits); }
 * 
 * public void acquire() throws InterruptedException { } //获取一个许可 public void
 * acquire(int permits) throws InterruptedException { } //获取permits个许可 public
 * void release() { } //释放一个许可 public void release(int permits) { }
 * //释放permits个许可
 * 
 * availablePermits()
 **/
public class SemaphoreExamp {
    public static void main(String[] args) {
        int N = 8; // 工人数
        Semaphore semaphore = new Semaphore(5); // 机器数目
        for (int i = 0; i < N; i++)
            new Worker(i, semaphore).start();
    }

    static class Worker extends Thread {
        private int num;
        private Semaphore semaphore;

        public Worker(int num, Semaphore semaphore) {
            this.num = num;
            this.semaphore = semaphore;
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
                System.out.println("工人" + this.num + "占用一个机器在生产...");
                Thread.sleep(2000);
                System.out.println("工人" + this.num + "释放出机器");
                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/*
 * 工人0占用一个机器在生产... 工人1占用一个机器在生产... 工人2占用一个机器在生产... 工人4占用一个机器在生产...
 * 工人5占用一个机器在生产... 工人0释放出机器 工人2释放出机器 工人3占用一个机器在生产... 工人7占用一个机器在生产... 工人4释放出机器
 * 工人5释放出机器 工人1释放出机器 工人6占用一个机器在生产... 工人3释放出机器 工人7释放出机器 工人6释放出机器
 */

/*
 * 下面对上面说的三个辅助类进行一个总结：
 * 
 * 1）CountDownLatch和CyclicBarrier都能够实现线程之间的等待，只不过它们侧重点不同：
 * 
 * CountDownLatch一般用于某个线程A等待若干个其他线程执行完任务之后，它才执行；
 * 
 * 而CyclicBarrier一般用于一组线程互相等待至某个状态，然后这一组线程再同时执行；
 * 
 * 另外，CountDownLatch是不能够重用的，而CyclicBarrier是可以重用的。
 * 
 * 2）Semaphore其实和锁有点类似，它一般用于控制对某组资源的访问权限。
 */