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

    		String dateParam = request.getParameter("selectedDate");
        String categoryParam = request.getParameter("categoryId");
        java.util.Date today = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Timestamp selectedDate;
        try {
            String selectedDateStr = (dateParam == null || dateParam.isEmpty()) 
                                      ? sdf.format(today) 
                                      : dateParam;

            java.util.Date utilDate = sdf.parse(selectedDateStr);
            selectedDate = new Timestamp(utilDate.getTime());

        } catch (Exception e) {
            selectedDate = new Timestamp(today.getTime());
        }

        int selectedCategoryId = 1;
        if (categoryParam != null) {
            try {
                selectedCategoryId = Integer.parseInt(categoryParam);
            } catch (NumberFormatException ignored) {}
        }

        HttpSession session = request.getSession(false);
        
        Integer memberId = null;
        if (session != null) {
            Object sessionUser = session.getAttribute("memberId");
            if (sessionUser instanceof Integer) {
                memberId = (Integer) sessionUser;
            }
        }

        List<Map<String, Object>> schedules = service.getSchedulesByDateAndCategory(selectedDate, selectedCategoryId, memberId);

        Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
        String schedulesJson = gsonPretty.toJson(schedules);

        System.out.println("schedulesJson:\n" + schedulesJson);

        request.setAttribute("schedulesJson", schedulesJson);
        request.setAttribute("selectedDate", sdf.format(selectedDate));
        request.setAttribute("selectedCategoryId", selectedCategoryId);

        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/main.jsp");
        rd.forward(request, response);
    }
}
