package snippet.thread;

import java.io.IOException;

public class ThreadJoin {
     
    public static void main(String[] args) throws IOException  {
        System.out.println("进入线程"+Thread.currentThread().getName());
        ThreadJoin threadJoin = new ThreadJoin();
        MyThread thread1 = threadJoin.new MyThread();
        thread1.start();
        try {
            System.out.println("线程"+Thread.currentThread().getName()+"等待");
            thread1.join();
            System.out.println("线程"+Thread.currentThread().getName()+"继续执行");
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } 
     
    class MyThread extends Thread{
        @Override
        public void run() {
            System.out.println("进入线程"+Thread.currentThread().getName());
            try {
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException e) {
                // TODO: handle exception
            }
            System.out.println("线程"+Thread.currentThread().getName()+"执行完毕");
        }
    }
}

/*
进入线程main
线程main等待
进入线程Thread-0
线程Thread-0执行完毕
线程main继续执行
*/