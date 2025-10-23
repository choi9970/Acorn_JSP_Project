<%@page import="LIVEMAP.Member"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>


<h1> Main화면 입니다</h1>

<% String id  =(String) session.getAttribute("memberId"); 
   if( id  != null){
%>

<%=id %>님 반갑습니다. ^^<br>
<%=id %>님의 회원정보 입니다.
<%
 Member member = (Member) request.getAttribute("member");
%>
<table border="1">
    <caption>회원 정보</caption>
    <tr>
        <th>회원번호</th>
        <th>아이디</th>
        <th>비밀번호</th>
        <th>이름</th>
    </tr>
    <tr>
        <td><%= member.getMemberId() %></td>
        <td><%= member.getMemberEmail() %></td>
        <td><%= member.getMemberPw() %></td>
        <td><%= member.getMemberName() %></td>
    </tr>
</table>
<a href="/LIVEMAP/logOut">로그아웃</a>

<%
}
else{
%>
<a href="/LIVEMAP/login">로그인</a>
<%} %>

<a href="/LIVEMAP/order">주문하기</a>

</body>
</html>