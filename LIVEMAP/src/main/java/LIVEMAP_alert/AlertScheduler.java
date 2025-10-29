package LIVEMAP_alert;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AlertScheduler {

    private final ScheduleAlertService alertService = ScheduleAlertService.getInstance();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void start() {
        System.out.println("[AlertScheduler] 방송 알림 감시 중...");

        // 1분마다 실행할 작업 (Runnable)
        Runnable task = () -> {
            try {
                System.out.println("[AlertScheduler] 1분마다 알림 체크... " + java.time.LocalTime.now());
                
                alertService.processAlerts(); 
                
            } catch (Exception e) {
                System.err.println("!!! [AlertScheduler] 작업 중 심각한 오류 발생 !!!");
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 60, TimeUnit.SECONDS); 
    }

    public void stop() {
        scheduler.shutdown();
        System.out.println("[AlertScheduler] 스케줄러 종료");
    }
}