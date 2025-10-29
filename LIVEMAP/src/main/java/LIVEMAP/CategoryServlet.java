package LIVEMAP;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet("/categoryCreate")
public class CategoryServlet extends HttpServlet {
    private CategoryService service = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");

        String jsonText = "";

        // 1. WEB-INF/python/category.py 경로
        String pythonPath = "C:\\Users\\Admin\\AppData\\Local\\Programs\\Python\\Python313\\python.exe";
        String realPath = getServletContext().getRealPath("/WEB-INF/python/category.py");

        ProcessBuilder pb = new ProcessBuilder(pythonPath, realPath);
        pb.environment().put("PYTHONIOENCODING", "utf-8");
        pb.redirectErrorStream(true);

        try {
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                jsonText = output.toString();
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("파이썬 실행 실패 (exitCode=" + exitCode + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 에러시 JSON으로 반환
            resp.getWriter().write("{\"categoryCount\":0,\"catInsertedCount\":0,\"error\":\"Python 실행 실패\"}");
            return;
        }

        // 2. JSON 파싱
        JSONArray arr = new JSONArray(jsonText);

        int insertedCount = 0;
        for (int i = 0; i < arr.length(); i++) {
            String name = arr.getString(i);
            if (service.addCategory(name)) {
                insertedCount++;
            }
        }

        // 3. JSON으로 반환
        JSONObject result = new JSONObject();
        result.put("categoryCount", arr.length());
        result.put("catInsertedCount", insertedCount);

        resp.getWriter().write(result.toString());
    }
}
