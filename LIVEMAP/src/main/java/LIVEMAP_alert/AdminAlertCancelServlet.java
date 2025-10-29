package LIVEMAP_alert; // 패키지명 확인

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import LIVEMAP_alert.ScheduleAlertService; 

/**
 * [최종] 중지된 방송 알림 일괄 취소 처리를 전담하는 서블릿
 */
@WebServlet("/admin/processCancelledAlerts") 
public class AdminAlertCancelServlet extends HttpServlet { 

    private ScheduleAlertService alertService = ScheduleAlertService.getInstance(); 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // [주의: 관리자 권한 체크 로직은 여기에 추가되어야 합니다.]
        
        int processedCount = 0;
        String errorMessage = null;

        try {
            processedCount = alertService.processAllDeletedAlertsManually();
            
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "처리 중 알 수 없는 오류가 발생했습니다: " + e.getMessage();
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse;
        if (errorMessage == null) {
            jsonResponse = String.format("{\"success\": true, \"message\": \"%d건의 중지된 방송 알림을 처리했습니다.\", \"count\": %d}", 
                                         processedCount, processedCount);
        } else {
            String safeErrorMessage = (errorMessage != null) ? errorMessage.replace("\"", "\\\"") : "알 수 없는 오류";
            jsonResponse = String.format("{\"success\": false, \"message\": \"%s\"}", safeErrorMessage);
        }

        response.getWriter().write(jsonResponse);
    }
}