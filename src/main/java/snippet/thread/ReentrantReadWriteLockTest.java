package snippet.thread;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockTest {
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public static void main(String[] args) {
        final ReentrantReadWriteLockTest test = new ReentrantReadWriteLockTest();

        new Thread() {
            public void run() {
                test.get(Thread.currentThread());
            };
        }.start();

        new Thread() {
            public void run() {
                test.get(Thread.currentThread());
            };
        }.start();

    }

    public void get(Thread thread) {
        rwl.readLock().lock();
        try {
            long start = System.currentTimeMillis();

            while (System.currentTimeMillis() - start <= 1) {
                System.out.println(thread.getName() + "正在进行读操作");
            }
            System.out.println(thread.getName() + "读操作完毕");
        } finally {
            rwl.readLock().unlock();
        }
    }
}

/*
 * Thread-0正在进行读操作 Thread-0正在进行读操作 Thread-0正在进行读操作 Thread-0正在进行读操作
 * Thread-0正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-0正在进行读操作
 * Thread-0正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-0正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-0正在进行读操作 Thread-1正在进行读操作
 * Thread-0正在进行读操作 Thread-1正在进行读操作 Thread-0正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-0正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-0正在进行读操作 Thread-0正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-0正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-0正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-0正在进行读操作 Thread-1读操作完毕 Thread-0读操作完毕
 */