<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>Main</title>
<style>
body {
	font-family: 'Pretendard', sans-serif;
	background-color: #f9f9f9;
	text-align: center;
	padding-top: 80px
}

.box {
	background: #fff;
	display: inline-block;
	padding: 40px 60px;
	border-radius: 15px;
	box-shadow: 0 4px 15px rgba(0, 0, 0, .1)
}
</style>
</head>
<body>
	<%
	Object mid = request.getAttribute("memberId");
	if (mid == null) {
		javax.servlet.http.HttpSession sess = request.getSession(false);
		if (sess != null)
			mid = sess.getAttribute("memberId");
	}
	if (mid == null) {
		response.sendRedirect(request.getContextPath() + "/login");
		return;
	}

	String memberId = String.valueOf(mid);

	Object nickObj = request.getAttribute("nickname");
	if (nickObj == null) {
		javax.servlet.http.HttpSession sess = request.getSession(false);
		if (sess != null)
			nickObj = sess.getAttribute("nickname");
	}
	String nickname = nickObj != null ? String.valueOf(nickObj) : "";

	Object mailObj = request.getAttribute("email");
	if (mailObj == null) {
		javax.servlet.http.HttpSession sess = request.getSession(false);
		if (sess != null)
			mailObj = sess.getAttribute("email");
	}
	String email = mailObj != null ? String.valueOf(mailObj) : "";
	%>

	<div class="box">
		<h2>메인 페이지</h2>
		<p>
			회원번호:
			<%=memberId%></p>
		<p>
			닉네임:
			<%=nickname%></p>
		<p>
			이메일:
			<%=(email != null && email.length() > 0) ? email : "없음"%></p>
		<a href="<%=request.getContextPath()%>/logOut"
			style="display: inline-block; margin-top: 10px;">로그아웃</a>
	</div>
</body>
</html>
