<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.net.URLDecoder"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>로그인</title>
<style>
body {
	font-family: 'Pretendard', sans-serif;
	background-color: #f5f6fa;
	display: flex;
	justify-content: center;
	align-items: center;
	height: 100vh;
	margin: 0;
}

a {
	text-decoration: none;
	color: black;
}

.login-box {
	background: #fff;
	padding: 40px 50px;
	border-radius: 15px;
	box-shadow: 0 4px 15px rgba(0, 0, 0, .1);
	width: 350px;
}

.login-box h2 {
	text-align: center;
	margin-bottom: 30px;
	color: #333;
}

.input-group {
	margin-bottom: 20px;
}

.input-group label {
	display: block;
	margin-bottom: 8px;
	font-weight: 600;
	color: #555;
}

.input-group input {
	width: 100%;
	padding: 10px;
	border-radius: 8px;
	border: 1px solid #ccc;
	font-size: 14px;
}

.btn-login {
	width: 100%;
	background-color: #4b7bec;
	color: #fff;
	border: none;
	padding: 12px;
	border-radius: 8px;
	cursor: pointer;
	font-size: 15px;
	transition: .2s;
}

.btn-login:hover {
	background-color: #3867d6;
}

.kakao-login {
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	border: none;
	width: 100%;
	box-sizing: border-box;
	height: 50px;
	border-radius: 8px;
	background-color: #FEE500;
	color: #000;
	transition: .2s;
	margin-top: 18px;
	font-size: 15px;
	font-weight: 600;
}

.kakao-login:hover {
	background-color: #E6D400;
}

.kakao-login img {
	height: 20px;
	margin-right: 8px;
}

.footer {
	text-align: center;
	margin-top: 20px;
	font-size: 13px;
	color: #666;
}

.footer a {
	color: #4b7bec;
	text-decoration: none;
}
</style>
</head>

<body>
	<form name="frm" action="<%=request.getContextPath()%>/login"
		method="post">
		<div class="login-box">
			<h2>로그인</h2>

			<div class="input-group">
				<label for="memberId">아이디</label> <input type="text" id="memberId"
					name="memberEmail" placeholder="아이디를 입력하세요">
			</div>

			<div class="input-group">
				<label for="memberPw">비밀번호</label> <input type="password"
					id="memberPw" name="memberPw" placeholder="비밀번호를 입력하세요">
			</div>

			<button type="submit" class="btn-login">로그인</button>

			<%
			String msg = (String) request.getAttribute("msg");
			if (msg != null) {
			%>
			<p style="color: red; text-align: center;"><%=msg%></p>
			<%
			}
			%>

			<div style="text-align: center;">
				<a href="<%=request.getContextPath()%>/oauth/kakao"
					class="kakao-login"> <img
					src="https://t1.kakaocdn.net/kakaocorp/kakaocorp/admin/service/6ffc1d92019900001.png"
					alt="kakao"> 카카오로 로그인
				</a>
			</div>

			<div class="footer">
				<p>
					아직 회원이 아니신가요? <a href="<%=request.getContextPath()%>/join.jsp">회원가입</a>
				</p>
			</div>
		</div>
	</form>
</body>
</html>
