package snippet.thread;

import java.io.IOException;

/*
If you have extended some other classes, then you should implement runnable interface instead.
当传入一个Runnable target参数给Thread后，Thread的run()方法就会调用target.run()，参考JDK源代码：
    @Override
    public void run() {
        if (target != null) {
            target.run();
        }
    }

执行顺序：
（1）定义runnable接口的实现类，并重写该接口的run()方法，该run()方法的方法体同样是该线程的线程执行体。

（2）创建 Runnable实现类的实例，并以此实例作为Thread的target来创建Thread对象，该Thread对象才是真正的线程对象。

（3）调用线程对象的start()方法来启动该线程。
*/
public class ThreadCreator2 {

    public static void main(String[] args) throws IOException {
        MyThread myThread = new MyThread();
        Thread thread = new Thread(myThread); // 为了启动MyThread，需要首先实例化一个Thread，并传入自己的MyThread实例
        thread.start();
    }

    static class MyThread extends Object implements Runnable {
        @Override
        public void run() {
            System.out.println("MyThread.run()");
        }
    }
}
