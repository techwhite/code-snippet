package snippet.thread;

import java.io.IOException;

/**
 * （1）定义Thread类的子类，并重写该类的run方法，该run方法的方法体就代表了线程要完成的任务。因此把run()方法称为执行体。
 * 
 * （2）创建Thread子类的实例，即创建了线程对象。
 * 
 * （3）调用线程对象的start()方法来启动该线程。
 * 
 */
public class ThreadCreator1 {

    public static void main(String[] args) throws IOException {
        MyThread myThread1 = new MyThread();
        MyThread myThread2 = new MyThread();
        myThread1.start(); // 只是分配资源，但不一定可以运行
        myThread2.start();
    }

    static class MyThread extends Thread {
        @Override
        public void run() {
            System.out.println("MyThread.run()");
        }
    }
}
