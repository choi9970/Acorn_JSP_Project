package LIVEMAP_alert; // 또는 LIVEMAP_alert;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import LIVEMAP_alert.ScheduleAlertDAO; // DAO 클래스가 있는 패키지명 확인!

@WebServlet("/alertList") // "/alertList" URL 요청 처리
public class AlertListServlet extends HttpServlet {

    // DAO 객체 생성 (ScheduleAlertDAO 또는 SchedulesDAO 중 메서드를 추가한 곳)
    private ScheduleAlertDAO alertDAO = new ScheduleAlertDAO(); //

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. 로그인 확인
        HttpSession session = request.getSession(false);
        Integer memberId = null;
        if (session != null) {
            Object sessionUser = session.getAttribute("memberId");
            if (sessionUser instanceof Integer) {
                memberId = (Integer) sessionUser;
            }
        }

        // 2. 로그인이 안 되어 있으면 로그인 페이지로 리다이렉트
        if (memberId == null) {
            response.sendRedirect(request.getContextPath() + "/login"); //
            return; // 서블릿 실행 중단
        }

        // 3. DAO를 통해 해당 회원의 알림 목록 조회
        List<Map<String, Object>> alertList = alertDAO.findSubscribedSchedulesByMemberId(memberId);

        // 4. 조회된 목록을 request에 담기
        request.setAttribute("alertList", alertList);

        // 5. alertList.jsp로 포워딩
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/alertlist.jsp");
        rd.forward(request, response);
    }
}