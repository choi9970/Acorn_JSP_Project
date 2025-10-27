package  LIVEMAP;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/logOut")
public class LogOutServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute("tokenRequested");
			session.removeAttribute("memberId");
			session.removeAttribute("nickname");
			session.removeAttribute("kakaoAccessToken");
			session.removeAttribute("kakaoRefreshToken");
			session.invalidate();
			System.out.println("[KAKAO] 세션 초기화 및 로그아웃 완료");
		}
		response.sendRedirect(request.getContextPath() + "/main.do");
	}
}
