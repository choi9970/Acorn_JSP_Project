package LIVEMAP;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/main.do")
public class MainServlet extends HttpServlet {

    private SchedulesService service = new SchedulesService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. 파라미터 수신
        
        String categoryParam = request.getParameter("categoryId");
        // 1️⃣ 오늘 날짜 준비
        java.util.Date today = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // 2️⃣ request에서 selectedDate 파라미터 수신
        String dateParam = request.getParameter("selectedDate");

        // 3️⃣ Timestamp 변환
        Timestamp selectedDate;
        try {
            // 파라미터가 null이거나 빈 문자열이면 오늘 날짜 사용
            String selectedDateStr = (dateParam == null || dateParam.isEmpty()) 
                                      ? sdf.format(today) 
                                      : dateParam;

            java.util.Date utilDate = sdf.parse(selectedDateStr);
            selectedDate = new Timestamp(utilDate.getTime());

        } catch (Exception e) {
            // 변환 실패 시도 오늘 날짜
            selectedDate = new Timestamp(today.getTime());
        }

        int selectedCategoryId = 1; // 기본 카테고리
        if (categoryParam != null) {
            try {
                selectedCategoryId = Integer.parseInt(categoryParam);
            } catch (NumberFormatException ignored) {}
        }

        

        // 4. Service 호출
        List<Map<String, Object>> schedules = service.getSchedulesByDateAndCategory(selectedDate, selectedCategoryId);

        
        
     // 5. JSON 변환 (보기 좋게 출력)
        Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
        String schedulesJson = gsonPretty.toJson(schedules);

        // ✅ 콘솔에 출력
        System.out.println("schedulesJson:\n" + schedulesJson);

        // 6. JSP 전달
     // 6. JSP 전달
        request.setAttribute("schedulesJson", schedulesJson);
        request.setAttribute("selectedDate", sdf.format(selectedDate));
        request.setAttribute("selectedCategoryId", selectedCategoryId);

        // 7. JSP 포워딩
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/main.jsp");
        rd.forward(request, response);
    }
}
