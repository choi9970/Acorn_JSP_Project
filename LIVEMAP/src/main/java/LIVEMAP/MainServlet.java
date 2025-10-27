package LIVEMAP;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/main.do")
public class MainServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		HttpSession s = req.getSession(false);
		if (s == null || s.getAttribute("memberId") == null) {
			resp.sendRedirect(req.getContextPath() + "/login");
			return;
		}
		req.setAttribute("memberId", s.getAttribute("memberId"));
		req.setAttribute("nickname", s.getAttribute("nickname"));
		req.setAttribute("email", s.getAttribute("email"));
		req.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(req, resp);
	}
}
