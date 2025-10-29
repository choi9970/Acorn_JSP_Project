package LIVEMAP_alert;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener // 톰캣이 이 파일을 자동으로 인식
public class AlertSchedulerListener implements ServletContextListener {

    private AlertScheduler scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = new AlertScheduler(); 
        scheduler.start(); // 톰캣 서버가 시작되면 자동 실행
        System.out.println("=== [AlertSchedulerListener] 스케줄러가 시작되었습니다. ==="); 
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.stop(); // 서버 종료 시 중지
        }
        System.out.println("=== [AlertSchedulerListener] 스케줄러가 종료되었습니다. ==="); 
    }
}