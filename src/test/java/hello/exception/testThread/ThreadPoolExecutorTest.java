package hello.exception.testThread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class ThreadPoolExecutorTest {

    public static void main(String[] args){
        //작업 큐 생성 - 크기 제한
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(5);

        RejectedExecutionHandler handler
                = new ThreadPoolExecutor.CallerRunsPolicy();

        ThreadPoolExecutor executor
                = new ThreadPoolExecutor(
                        2,
                4,
                1,
                TimeUnit.MICROSECONDS,
                workQueue,
                Executors.defaultThreadFactory(),
                handler
        );

        printThreadPoolStatus(executor, "ThreadPool initialized");


        for (int i = 1; i <= 10; i++) {
            final int taskId = i;
            executor.execute(() -> {
                log.info("!!!!!!!!!!!!!!!!!!!! Task {} is starting.", taskId);
                try {
                    // 작업 수행 시간 (랜덤)
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.error("???????????????????? Task {} was interrupted.", taskId);
                    Thread.currentThread().interrupt();
                }
                log.info("???????????????????? Task {} is completed.", taskId);
            });
            printThreadPoolStatus(executor, "Submitted Task " + taskId);
        }

        //풀 크기 동적 조정 테스트
        try{
            Thread.sleep(3000);
            //log -> System.out.println("\n-- Increasing core pool size to 3 --");
            executor.setCorePoolSize(3);
            printThreadPoolStatus(executor, "Increased core pool size");

            Thread.sleep(3000);
            //log -> System.out.println("\n-- Decreasing maximum pool size to 3 --");
            executor.setMaximumPoolSize(3);
            printThreadPoolStatus(executor, "Decreased maximum poll size");

            for(int i = 11; i <= 15; i++){
                final int taskId = i;
                executor.execute(() ->{
                    log.info("!!!!!!!!!!!!!!!!!!!! Task {} is starting.", taskId);
                    try{
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        log.error("???????????????????? Task {} was interrupted.", taskId);
                        Thread.currentThread().interrupt();
                    }
                    log.info("???????????????????? Task {} is completed.", taskId);
                });
                printThreadPoolStatus(executor, "Submitted Task " + taskId);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }



        //스레드 풀 종료
        executor.shutdown();
        try{
            if(!executor.awaitTermination(20, TimeUnit.SECONDS)){
                log.warn("Forcing shutdown...");
                executor.shutdownNow();
            }
        }catch (InterruptedException e){
            log.error("Interrupted during shutdown.");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        printThreadPoolStatus(executor, "ThreadPool shutdown");
    }

    private static void printThreadPoolStatus(ThreadPoolExecutor executor,
                                              String message){
        log.info("\n[{}]", message);
        log.info("Pool size: {}", executor.getPoolSize());
        log.info("Active Threads: {}", executor.getActiveCount());
        log.info("Completed Tasks: {}", executor.getCompletedTaskCount());
        log.info("Task Queue Size: {}", executor.getQueue().size());
        // 표준 ThreadPoolExecutor에는 getSubmittedCount()가 없으므로 대체 방법 사용
        long submittedTasks = executor.getTaskCount() - executor.getCompletedTaskCount();
        log.info("Submitted Tasks (Active + Queued): {}", submittedTasks);
        log.info("Largest Pool Size: {}", executor.getLargestPoolSize());
    }
}
