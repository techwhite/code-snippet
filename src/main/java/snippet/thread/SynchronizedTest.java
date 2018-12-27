package snippet.thread;

public class SynchronizedTest {

    public static void main(String[] args) {
        final SynchronizedTest test = new SynchronizedTest();

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

    public synchronized void get(Thread thread) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start <= 1) {
            System.out.println(thread.getName() + "正在进行读操作");
        }
        System.out.println(thread.getName() + "读操作完毕");
    }
}

/*
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作
 * Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1正在进行读操作 Thread-1读操作完毕
 */