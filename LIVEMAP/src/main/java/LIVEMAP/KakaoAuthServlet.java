package LIVEMAP;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;

@WebServlet("/oauth/kakao")
public class KakaoAuthServlet extends HttpServlet {
	private static final String CLIENT_ID = "9157d3670a9623728bfdbb7559060faa";
	private static final String REDIRECT_URI = "http://localhost:8080/LIVEMAP/oauth/kakao/callback";

	// ★ 필요한 동의항목만 — nickname, email, name, gender, birthday,package LIVEMAP;
	// phone_number
	private static final String SCOPE = String.join(" ", "profile_nickname", "account_email", "name", "gender",
			"birthday", "birthyear", "phone_number");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String state = generateState();
		HttpSession s = req.getSession(true);
		s.setAttribute("oauth_state", state);

		// prompt=consent → 새 스코프 적용 시 재동의 유도
		String authUrl = "https://kauth.kakao.com/oauth/authorize" + "?response_type=code" + "&client_id="
				+ URLEncoder.encode(CLIENT_ID, "UTF-8") + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
				+ "&state=" + URLEncoder.encode(state, "UTF-8") + "&scope=" + URLEncoder.encode(SCOPE, "UTF-8")
				+ "&prompt=consent";

		resp.sendRedirect(authUrl);
	}

	private String generateState() {
		byte[] b = new byte[16];
		new SecureRandom().nextBytes(b);
		StringBuilder sb = new StringBuilder();
		for (byte x : b)
			sb.append(String.format("%02x", x));
		return sb.toString();
	}
}
