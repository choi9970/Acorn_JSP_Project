package LIVEMAP_alert;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/alert/subscribe") 
public class ScheduleAlertServlet extends HttpServlet { 

    private ScheduleAlertService alertService = ScheduleAlertService.getInstance(); 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
    	boolean isSuccess = false;
        String newAlertState = null; // 프론트엔드가 버튼 상태를 바꿀 근거
        String errorMessage = null;

        try {
            HttpSession session = request.getSession(false);
            Integer memberId = null;
            if (session != null) {
                Object sessionUser = session.getAttribute("memberId"); 
                if (sessionUser instanceof Integer) {
                    memberId = (Integer) sessionUser;
                }
            }

            String scheduleIdParam = request.getParameter("scheduleId");
            
            if (memberId == null) {
                errorMessage = "로그인이 필요합니다.";
            } else if (scheduleIdParam == null || scheduleIdParam.isEmpty()) {
                errorMessage = "방송 정보가 올바르지 않습니다.";
            } else {
                int scheduleId = Integer.parseInt(scheduleIdParam);
                
                String toggleResult = alertService.subscribe(memberId, scheduleId);
                
                switch (toggleResult) {
                    case "Y":
                        isSuccess = true;
                        newAlertState = "Y";
                        break;
                    case "N":
                        isSuccess = true;
                        newAlertState = "N";
                        break;
                    case "ERROR":
                    default:
                        isSuccess = false;
                        errorMessage = "알림 처리 중 DB 오류가 발생했습니다.";
                        break;
                }
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            errorMessage = "방송 정보가 올바르지 않습니다."; // scheduleId 변환 실패
        } catch (Exception e) {
            e.printStackTrace();
            errorMessage = "알 수 없는 오류가 발생했습니다.";
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = "";
        if (isSuccess) {
            // 성공 시: JSP가 기대하는 "newAlertState" 키로 전송
            jsonResponse = String.format("{\"success\": true, \"newAlertState\": \"%s\"}", newAlertState);
        } else {
            // 실패 시: JSP가 기대하는 "errorMessage" 키로 전송
            String safeErrorMessage = (errorMessage != null) ? errorMessage.replace("\"", "\\\"") : "알 수 없는 오류";
            jsonResponse = String.format("{\"success\": false, \"errorMessage\": \"%s\"}", safeErrorMessage);
        }

        // 응답 전송
        try {
            response.getWriter().write(jsonResponse);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}