package snippet.multithread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

// 如果为了可取消性而使用 Future 但又不提供可用的结果，则可以声明 Future<?>或者FutureTask<?> 形式类型、并返回 null 作为底层任务的结果。
public class ThreadCreator4 {
     
    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Task task = new Task();

        // 第一种方式
        FutureTask<Integer> futureTask = new FutureTask<Integer>(task);
        executor.submit(futureTask);
        // 或者
        // 第二种方式
        // Future<Integer> result = executor.submit(task);

        executor.shutdown();
         
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
         
        System.out.println("主线程在执行任务");
         
        try {
            System.out.println("task运行结果"+futureTask.get()); // 阻塞
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
         
        System.out.println("所有任务执行完毕");
    }

    static class Task implements Callable<Integer>{
        @Override
        public Integer call() throws Exception {
            System.out.println("子线程在进行计算");
            Thread.sleep(3000);
            int sum = 0;
            for(int i=0;i<100;i++)
                sum += i;
            return sum;
        }
    }
}
