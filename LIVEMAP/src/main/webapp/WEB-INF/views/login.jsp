<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.net.URLDecoder" %>
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
        .login-box {
            background: #fff;
            padding: 40px 50px;
            border-radius: 15px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
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
            color: white;
            border: none;
            padding: 12px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 15px;
            transition: 0.2s;
        }
        .btn-login:hover {
            background-color: #3867d6;
        }
        .footer {
            text-align: center;
            margin-top: 15px;
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

<%-- 
  form 태그의 action 경로는 현재 프로젝트 이름(Context Path)에 맞게 유지했습니다.
  "${pageContext.request.contextPath}"를 사용하면 나중에 프로젝트 이름이 바뀌어도
  자동으로 경로가 설정되어 더 편리합니다.
--%>
<form name="frm" action="${pageContext.request.contextPath}/login" method="post">
    <div class="login-box">
        <h2>로그인</h2>
            
            <div class="input-group">
                <label for="memberEmail">이메일 아이디</label>
                <input type="text" id="memberEmail" name="memberEmail" placeholder="이메일 아이디를 입력하세요" required>
            </div>
            
            <div class="input-group">
                <label for="memberPw">비밀번호</label>
                <input type="password" id="memberPw" name="memberPw" placeholder="비밀번호를 입력하세요" required>
            </div>
            
            <button type="submit" class="btn-login">로그인</button>
            
            <%
			    String msg = (String) request.getAttribute("msg");
			    if(msg != null){
			%>
			    <p style="color:red;"><%= msg %></p>
			<%
			    }
			%>
        <div class="footer">
            [cite_start]아직 회원이 아니신가요? [cite: 20]
            [cite_start]<a href="join.jsp">회원가입</a> [cite: 20]
        </div>
    </div>
</form>
</body>
</html>