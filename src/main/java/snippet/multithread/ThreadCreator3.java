package snippet.multithread;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/*

public class FutureTask<V> implements RunnableFuture<V> 
public interface RunnableFuture<V> extends Runnable, Future<V> 


（1）创建Callable接口的实现类，并实现call()方法，该call()方法将作为线程执行体，并且有返回值。

    public interface Callable
    {
    　　V call() throws Exception;
    }
（2）创建Callable实现类的实例，使用FutureTask类来包装Callable对象，该FutureTask对象封装了该Callable对象的call()方法的返回值。（FutureTask是一个包装器，它通过接受Callable来创建，它同时实现了Future和Runnable接口。）

（3）使用FutureTask对象作为Thread对象的target创建并启动新线程。

（4）调用FutureTask对象的get()方法来获得子线程执行结束后的返回值
*/
public class ThreadCreator3 {
     
    public static void main(String[] args) throws IOException  {
        Callable<Integer> oneCallable = new SomeCallable<Integer>();   
        //由Callable<Integer>创建一个FutureTask<Integer>对象：   
        FutureTask<Integer> oneTask = new FutureTask<>(oneCallable);   
        //注释：FutureTask<Integer>是一个包装器，它通过接受Callable<Integer>来创建，它同时实现了Future和Runnable接口。 
        //由FutureTask<Integer>创建一个Thread对象：   
        Thread oneThread = new Thread(oneTask);   
        oneThread.start();   
        //至此，一个线程就创建完成了。 
    } 
     
    static class SomeCallable<V> extends Object implements Callable<V> {

        @Override
        public V call() throws Exception {
            return null;
        }
    
    }
}
