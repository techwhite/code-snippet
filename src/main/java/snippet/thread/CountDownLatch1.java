package snippet.thread;

import java.util.concurrent.CountDownLatch;

public class CountDownLatch1 {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDown = new CountDownLatch(1);
        CountDownLatch await = new CountDownLatch(5);

        // 依次创建并启动处于等待状态的5个MyRunnable线程
        for (int i = 0; i < 5; ++i) {
            Thread thread = new Thread(new MyRunnable(countDown, await));
            thread.start();
        }

        System.out.println("用于触发处于等待状态的线程开始工作......");
        System.out.println("用于触发处于等待状态的线程工作完成，等待状态线程开始工作......");
        countDown.countDown();
        await.await();
        System.out.println("Bingo!");
    }

    private static class MyRunnable implements Runnable {

        private final CountDownLatch countDown;
        private final CountDownLatch await;

        public MyRunnable(CountDownLatch countDown, CountDownLatch await) {
            this.countDown = countDown;
            this.await = await;
        }

        public void run() {
            try {
                countDown.await();// 等待主线程执行完毕，获得开始执行信号...
                System.out.println("处于等待的线程开始自己预期工作......");
                await.countDown();// 完成预期工作，发出完成信号...
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

/*
 * 运行结果： 用于触发处于等待状态的线程开始工作...... 用于触发处于等待状态的线程工作完成，等待状态线程开始工作......
 * 处于等待的线程开始自己预期工作...... 处于等待的线程开始自己预期工作...... 处于等待的线程开始自己预期工作......
 * 处于等待的线程开始自己预期工作...... 处于等待的线程开始自己预期工作...... Bingo!
 */