package snippet.multithread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// 如果为了可取消性而使用 Future 但又不提供可用的结果，则可以声明 Future<?>或者FutureTask<?> 形式类型、并返回 null 作为底层任务的结果。
public class ThreadCreator5 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {  
   
    System.out.println("----程序开始运行----");  
    Date date1 = new Date();  
  
    int taskSize = 5;  
    // 创建一个线程池  
    ExecutorService pool = Executors.newFixedThreadPool(taskSize);  
    // 创建多个有返回值的任务  
    List<Future<Object>> list = new ArrayList<>();
    for (int i = 0; i < taskSize; i++) {  
        Callable<Object> c = new MyCallable(i + " ");  
        // 执行任务并获取Future对象  
        Future<Object> f = pool.submit(c);  
        // System.out.println(">>>" + f.get().toString());  
        list.add(f);  
    }  
    // 关闭线程池  
    pool.shutdown();  
    
    // 获取所有并发任务的运行结果。所以这个地方完全可以轮询判断是哪个先返回结果，然后做相应的策略，比如取消
    for (Future<Object> f : list) {  
        // 从Future对象上获取任务的返回值，并输出到控制台  
        System.out.println(">>>" + f.get().toString());  
    }  
    
    Date date2 = new Date();  
    System.out.println("----程序结束运行----，程序运行时间【"  
        + (date2.getTime() - date1.getTime()) + "毫秒】");  
    }  
}  
  
class MyCallable implements Callable<Object> {  
    private String taskNum;  
    
    MyCallable(String taskNum) {  
        this.taskNum = taskNum;  
    }  
    
    public Object call() throws Exception {  
        System.out.println(">>>" + taskNum + "任务启动");  
        Date dateTmp1 = new Date();  
        Thread.sleep(1000);  
        Date dateTmp2 = new Date();  
        long time = dateTmp2.getTime() - dateTmp1.getTime();  
        System.out.println(">>>" + taskNum + "任务终止");  
        return taskNum + "任务返回运行结果,当前任务时间【" + time + "毫秒】";  
    }  
}
