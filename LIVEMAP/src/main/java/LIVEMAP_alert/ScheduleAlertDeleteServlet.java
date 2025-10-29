package LIVEMAP_alert;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/alert/cancel")
public class ScheduleAlertDeleteServlet extends HttpServlet {

    // DAO 객체를 직접 사용합니다.
    private ScheduleAlertDAO alertDAO = new ScheduleAlertDAO(); //

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        boolean isSuccess = false;
        String errorMessage = "";

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
                errorMessage = "취소할 방송 정보가 올바르지 않습니다."; // scheduleId 없음
            } else {

                int scheduleId = Integer.parseInt(scheduleIdParam);

                isSuccess = alertDAO.cancelSubscription(memberId, scheduleId); //

                if (!isSuccess) {
                    errorMessage = "알림 취소 처리 중 오류가 발생했습니다. (이미 취소되었거나 DB 오류)";
                }
            }

        } catch (NumberFormatException e) {
            errorMessage = "잘못된 방송 정보 형식입니다.";
            e.printStackTrace();
        } catch (Exception e) {
            errorMessage = "알 수 없는 오류가 발생했습니다.";
            e.printStackTrace(); 
        }

        // 6. 결과 JSON 응답 생성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8"); 

        PrintWriter out = response.getWriter();
        out.print("{\"success\": " + isSuccess + ",\"message\": \"" + errorMessage + "\"}");
        out.flush();
    }
}