package LIVEMAP;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;

@WebServlet("/oauth/kakao/callback")
public class KakaoCallbackServlet extends HttpServlet {

	private static final String CLIENT_ID = "9157d3670a9623728bfdbb7559060faa";
	private static final String REDIRECT_URI = "http://localhost:8080/LIVEMAP/oauth/kakao/callback";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		HttpSession sess = req.getSession(true);

		try {
			String error = req.getParameter("error");
			if (error != null) {
				resp.sendRedirect(req.getContextPath() + "/login?e=" + URLEncoder.encode(error, "UTF-8"));
				return;
			}

			String code = req.getParameter("code");
			String state = req.getParameter("state");
			String saved = (String) sess.getAttribute("oauth_state");
			if (saved == null || !saved.equals(state)) {
				resp.sendRedirect(req.getContextPath() + "/login?e=state");
				return;
			}
			if (Boolean.TRUE.equals(sess.getAttribute("tokenRequested"))) {
				resp.sendRedirect(req.getContextPath() + "/login?e=duplicate");
				return;
			}
			sess.setAttribute("tokenRequested", true);

			sess.removeAttribute("oauth_state");
			Cookie kill = new Cookie("oauth_state", "");
			kill.setMaxAge(0);
			kill.setPath("/");
			resp.addCookie(kill);

			String lastCode = (String) sess.getAttribute("last_auth_code");
			if (code != null && code.equals(lastCode)) {
				forwardToMain(req, resp, sess);
				return;
			}
			sess.setAttribute("last_auth_code", code);

			Long lastAt = (Long) sess.getAttribute("lastTokenExchangeAt");
			long now = System.currentTimeMillis();
			if (lastAt != null && (now - lastAt) < 2000) {
				resp.sendRedirect(req.getContextPath() + "/login?e=fast");
				return;
			}
			sess.setAttribute("lastTokenExchangeAt", now);

			// 1) 토큰 교환
			String body = "grant_type=authorization_code" + "&client_id=" + URLEncoder.encode(CLIENT_ID, "UTF-8")
					+ "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") + "&code="
					+ URLEncoder.encode(code, "UTF-8");

			String tokenJson = postForm("https://kauth.kakao.com/oauth/token", body);
			JsonObject tokenObj = JsonParser.parseString(tokenJson).getAsJsonObject();
			if (tokenObj.has("error")) {
				resp.sendRedirect(req.getContextPath() + "/login?e=token");
				return;
			}
			String accessToken = tokenObj.get("access_token").getAsString();
			String refreshToken = tokenObj.has("refresh_token") ? tokenObj.get("refresh_token").getAsString() : null;

			// 2) 사용자 정보
			String meJson = getWithAuth("https://kapi.kakao.com/v2/user/me", accessToken);
			JsonObject me = JsonParser.parseString(meJson).getAsJsonObject();
			if (!me.has("id")) {
				forwardToMain(req, resp, sess);
				return;
			}
			long kakaoId = me.get("id").getAsLong();
			if (kakaoId <= 0) {
				forwardToMain(req, resp, sess);
				return;
			}

			// 3) 파싱
			KakaoProfileData data = new KakaoProfileData();
			data.nickname = "kakao_user";
			try {
				JsonObject acc = me.getAsJsonObject("kakao_account");
				if (acc != null) {
					// profile
					JsonObject profile = acc.getAsJsonObject("profile");
					if (profile != null) {
						if (profile.has("nickname"))
							data.nickname = profile.get("nickname").getAsString();
						if (profile.has("profile_image_url"))
							data.profileImage = profile.get("profile_image_url").getAsString();
					}
					// email
					if (acc.has("email"))
						data.email = acc.get("email").getAsString();
					// name/gender/birthday/birthyear/phone
					if (acc.has("name"))
						data.name = acc.get("name").getAsString();
					if (acc.has("gender")) {
						String g = acc.get("gender").getAsString(); // male/female
						data.gender = "male".equalsIgnoreCase(g) ? "M" : ("female".equalsIgnoreCase(g) ? "F" : null);
					}
					String birthday = acc.has("birthday") ? acc.get("birthday").getAsString() : null; // MMDD
					String birthyear = acc.has("birthyear") ? acc.get("birthyear").getAsString() : null; // YYYY
					if (birthday != null && birthday.length() == 4 && birthyear != null && birthyear.length() == 4) {
						int y = Integer.parseInt(birthyear);
						int m = Integer.parseInt(birthday.substring(0, 2));
						int d = Integer.parseInt(birthday.substring(2, 4));
						LocalDate ld = LocalDate.of(y, m, d);
						data.birth = java.sql.Date.valueOf(ld);
					}
					if (acc.has("phone_number")) {
						String raw = acc.get("phone_number").getAsString(); // e.g. +82 10-5471-0569
						// 한국 번호 형식으로 변환
						if (raw.startsWith("+82")) {
							raw = raw.replace("+82", "0").replaceAll("\\s+", "");
						}
						data.phone = raw;
					}
				}
			} catch (Exception ignore) {
			}

			// 4) 세션(뷰용)
			String providerUserId = "kuid_" + kakaoId;
			sess.setAttribute("memberId", "k_" + kakaoId);
			sess.setAttribute("nickname", data.nickname);
			if (data.email != null)
				sess.setAttribute("email", data.email);
			sess.setAttribute("puid", providerUserId);
			sess.setAttribute("kakaoAccessToken", accessToken);
			if (refreshToken != null)
				sess.setAttribute("kakaoRefreshToken", refreshToken);
			sess.setMaxInactiveInterval(1800);

			// 5) 비동기: 링크 + 프로필 업데이트까지 한 번에
			AsyncKakaoLinker.submitEnsureLinkWithProfile(providerUserId, data, accessToken, refreshToken);

			// 6) 즉시 렌더
			forwardToMain(req, resp, sess);

		} catch (Exception e) {
			e.printStackTrace();
			resp.sendRedirect(req.getContextPath() + "/login?e=500");
		} finally {
			if (sess != null)
				sess.removeAttribute("tokenRequested");
		}
	}

	private void forwardToMain(HttpServletRequest req, HttpServletResponse resp, HttpSession sess)
			throws ServletException, IOException {
		req.setAttribute("memberId", sess.getAttribute("memberId"));
		req.setAttribute("nickname", sess.getAttribute("nickname"));
		req.setAttribute("email", sess.getAttribute("email"));
		req.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(req, resp);
	}

	private String postForm(String url, String body) throws IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setConnectTimeout(5000);
		c.setReadTimeout(5000);
		c.setRequestMethod("POST");
		c.setDoOutput(true);
		c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
		try (OutputStream os = c.getOutputStream()) {
			os.write(body.getBytes("UTF-8"));
		}
		return read(c);
	}

	private String getWithAuth(String url, String token) throws IOException {
		HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
		c.setConnectTimeout(5000);
		c.setReadTimeout(5000);
		c.setRequestMethod("GET");
		c.setRequestProperty("Authorization", "Bearer " + token);
		return read(c);
	}

	private String read(HttpURLConnection c) throws IOException {
		InputStream is = (c.getResponseCode() / 100 == 2) ? c.getInputStream() : c.getErrorStream();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null)
				sb.append(line);
			return sb.toString();
		}
	}
}
