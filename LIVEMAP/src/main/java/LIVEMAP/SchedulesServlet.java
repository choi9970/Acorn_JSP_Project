package LIVEMAP;

import java.io.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/schedulesCreate")
public class SchedulesServlet extends HttpServlet {

    private SchedulesService service = new SchedulesService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jsonText = "";

        // 1️. Python 실행
        String pythonPath = "C:\\Users\\Admin\\AppData\\Local\\Programs\\Python\\Python313\\python.exe";
        String realPath = getServletContext().getRealPath("/WEB-INF/python/schedules.py");
        ProcessBuilder pb = new ProcessBuilder(pythonPath, realPath);
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("Python 실행 실패 (exitCode=" + exitCode + ")");
                }
                jsonText = output.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray arr = new JSONArray(jsonText);

        // 2️⃣ 플랫폼별 min/max 계산 후 삭제 플래그 1로 설정
        Map<String, LocalDateTime> range1 = findMinMaxScheduleStart(jsonText, 1);
        int deletedCount1 = service.deleteSchedulesInRange(1, range1);

        Map<String, LocalDateTime> range2 = findMinMaxScheduleStart(jsonText, 2);
        int deletedCount2 = service.deleteSchedulesInRange(2, range2);

        // 3️⃣ JSON 데이터 순회하며 DB 갱신
        int insertedCount = 0;
        int updatedCount = 0;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Schedules schedule = new Schedules();

            schedule.setPlatformId(obj.optInt("platform_id", 0));
            String categoryName = obj.optString("category_name", null);
            int categoryId = service.getCategoryIdByName(categoryName);
            schedule.setCategoryId(categoryId);

            schedule.setScheduleName(obj.optString("schedule_name", null));

            // schedule_start 변환
            String startStr = obj.optString("schedule_start", null);
            Timestamp scheduleStart = null;
            if (startStr != null && !startStr.isEmpty()) {
                try {
                    startStr = startStr.replace("T", " ");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    scheduleStart = new Timestamp(sdf.parse(startStr).getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            schedule.setScheduleStart(scheduleStart);

            schedule.setScheduleDiscount(obj.optString("schedule_discount", null));
            String priceStr = obj.optString("schedule_price", "");
            if (priceStr.isEmpty()) {
                schedule.setSchedulePrice(null);
            } else {
                try {
                    schedule.setSchedulePrice(Double.parseDouble(priceStr));
                } catch (NumberFormatException e) {
                    schedule.setSchedulePrice(null);
                }
            }

            schedule.setScheduleImg(obj.optString("schedule_img", null));
            schedule.setScheduleUrl(obj.optString("schedule_url", null));
            schedule.setScheduleDeleteFlg(obj.optInt("schedule_deleteflg", 0));

            // 4️⃣ 존재하면 삭제 플래그 0으로, 없으면 INSERT
            if (service.existsSchedule(schedule.getPlatformId(), schedule.getScheduleName(), scheduleStart)) {
                if (service.updateDeleteFlgToActive(schedule.getPlatformId(), schedule.getScheduleName(), scheduleStart)) {
                    updatedCount++;
                }
            } else {
                if (service.addSchedule(schedule)) {
                    insertedCount++;
                }
            }
        }

//        // 5️⃣ JSP로 전달
//        req.setAttribute("totalCount", arr.length());
//        req.setAttribute("insertedCount", insertedCount);
//        req.setAttribute("updatedCount", updatedCount);
//        req.setAttribute("deletedCount1", deletedCount1);
//        req.setAttribute("deletedCount2", deletedCount2);
//
//        RequestDispatcher rd = req.getRequestDispatcher("WEB-INF/views/adminDashBoard.jsp");
//        rd.forward(req, resp);
//        
        
        JSONObject result = new JSONObject();
        result.put("totalCount", arr.length());
        result.put("insertedCount", insertedCount);
        result.put("updatedCount", updatedCount);
        result.put("deletedCount1", deletedCount1);
        result.put("deletedCount2", deletedCount2);
        System.out.println("totalCount"+arr.length());
        System.out.println("insertedCount"+ insertedCount);
        System.out.println("updatedCount"+ updatedCount);
        System.out.println("deletedCount1"+deletedCount1);
        System.out.println("deletedCount2"+deletedCount2);


        resp.getWriter().write(result.toString());
    }

    // 플랫폼별 schedule_start min/max 구하기
    public static Map<String, LocalDateTime> findMinMaxScheduleStart(String jsonText, int targetPlatformId) {
        JSONArray arr = new JSONArray(jsonText);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime minDate = null;
        LocalDateTime maxDate = null;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if (obj.getInt("platform_id") == targetPlatformId) {
                String startStr = obj.getString("schedule_start");
                LocalDateTime startDate = LocalDateTime.parse(startStr, formatter);
                if (minDate == null || startDate.isBefore(minDate)) minDate = startDate;
                if (maxDate == null || startDate.isAfter(maxDate)) maxDate = startDate;
            }
        }
        Map<String, LocalDateTime> result = new HashMap<>();
        result.put("min", minDate);
        result.put("max", maxDate);
        return result;
    }
}
