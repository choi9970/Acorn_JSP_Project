[1mdiff --git a/LIVEMAP/src/main/java/LIVEMAP/LogoutServlet.java b/LIVEMAP/src/main/java/LIVEMAP/LogoutServlet.java[m
[1mindex fc4c6b4..94f58bd 100644[m
[1m--- a/LIVEMAP/src/main/java/LIVEMAP/LogoutServlet.java[m
[1m+++ b/LIVEMAP/src/main/java/LIVEMAP/LogoutServlet.java[m
[36m@@ -1,31 +1,25 @@[m
 package LIVEMAP;[m
 [m
 import java.io.IOException;[m
[31m-[m
 import javax.servlet.ServletException;[m
 import javax.servlet.annotation.WebServlet;[m
[31m-import javax.servlet.http.HttpServlet;[m
[31m-import javax.servlet.http.HttpServletRequest;[m
[31m-import javax.servlet.http.HttpServletResponse;[m
[31m-import javax.servlet.http.HttpSession;[m
[31m-[m
[32m+[m[32mimport javax.servlet.http.*;[m
 [m
 @WebServlet("/logOut")[m
 public class LogoutServlet extends HttpServlet {[m
[31m-	[m
 	@Override[m
[31m-	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {[m
[31m-	 [m
[32m+[m	[32mprotected void doGet(HttpServletRequest request, HttpServletResponse response)[m
[32m+[m			[32mthrows ServletException, IOException {[m
 		HttpSession session = request.getSession(false);[m
[31m-		[m
[31m-		//세션종료[m
[31m-		if( session !=null )	{	[m
[31m-			session.invalidate();	[m
[31m-		[m
[31m-			//메인화면으로[m
[31m-			response.sendRedirect("/LIVEMAP/main.do");[m
[32m+[m		[32mif (session != null) {[m
[32m+[m			[32msession.removeAttribute("tokenRequested");[m
[32m+[m			[32msession.removeAttribute("memberId");[m
[32m+[m			[32msession.removeAttribute("nickname");[m
[32m+[m			[32msession.removeAttribute("kakaoAccessToken");[m
[32m+[m			[32msession.removeAttribute("kakaoRefreshToken");[m
[32m+[m			[32msession.invalidate();[m
[32m+[m			[32mSystem.out.println("[KAKAO] 세션 초기화 및 로그아웃 완료");[m
 		}[m
[31m-		[m
[32m+[m		[32mresponse.sendRedirect(request.getContextPath() + "/main.do");[m
 	}[m
[31m-[m
 }[m
[1mdiff --git a/LIVEMAP/src/main/java/LIVEMAP/MainServlet.java b/LIVEMAP/src/main/java/LIVEMAP/MainServlet.java[m
[1mindex 5e283dd..c98a689 100644[m
[1m--- a/LIVEMAP/src/main/java/LIVEMAP/MainServlet.java[m
[1m+++ b/LIVEMAP/src/main/java/LIVEMAP/MainServlet.java[m
[36m@@ -1,37 +1,22 @@[m
 package LIVEMAP;[m
 [m
[31m-import java.io.IOException;[m
[31m-import java.util.ArrayList;[m
[31m-[m
 import javax.servlet.ServletException;[m
 import javax.servlet.annotation.WebServlet;[m
[31m-import javax.servlet.http.HttpServlet;[m
[31m-import javax.servlet.http.HttpServletRequest;[m
[31m-import javax.servlet.http.HttpServletResponse;[m
[31m-import javax.servlet.http.HttpSession;[m
[31m-[m
[31m-[m
[32m+[m[32mimport javax.servlet.http.*;[m
[32m+[m[32mimport java.io.IOException;[m
 [m
 @WebServlet("/main.do")[m
[31m-public class MainServlet extends HttpServlet{[m
[31m-	[m
[31m-	[m
[32m+[m[32mpublic class MainServlet extends HttpServlet {[m
 	@Override[m
[31m-	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {[m
[31m-		request.setCharacterEncoding("utf-8");	[m
[31m-		response.setCharacterEncoding("utf-8");[m
[31m-		response.setContentType("text/html;charset=utf-8");[m
[31m-		[m
[31m-		HttpSession session = request.getSession();[m
[31m-		String id  = (String)session.getAttribute("memberId");[m
[31m-		System.out.println(id);[m
[31m-		[m
[31m-		MemberService service= new MemberService();[m
[31m-		Member member= service.getfindById(id);[m
[31m-		System.out.println(member);[m
[31m-		[m
[31m-		request.setAttribute("member", member);[m
[31m-		request.getRequestDispatcher("WEB-INF/views/main.jsp").forward(request, response);[m
[32m+[m	[32mprotected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {[m
[32m+[m		[32mHttpSession s = req.getSession(false);[m
[32m+[m		[32mif (s == null || s.getAttribute("memberId") == null) {[m
[32m+[m			[32mresp.sendRedirect(req.getContextPath() + "/login");[m
[32m+[m			[32mreturn;[m
[32m+[m		[32m}[m
[32m+[m		[32mreq.setAttribute("memberId", s.getAttribute("memberId"));[m
[32m+[m		[32mreq.setAttribute("nickname", s.getAttribute("nickname"));[m
[32m+[m		[32mreq.setAttribute("email", s.getAttribute("email"));[m
[32m+[m		[32mreq.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(req, resp);[m
 	}[m
[31m-	[m
 }[m
[1mdiff --git a/LIVEMAP/src/main/java/LIVEMAP/Member.java b/LIVEMAP/src/main/java/LIVEMAP/Member.java[m
[1mindex c6f16a3..b34ebbe 100644[m
[1m--- a/LIVEMAP/src/main/java/LIVEMAP/Member.java[m
[1m+++ b/LIVEMAP/src/main/java/LIVEMAP/Member.java[m
[36m@@ -1,45 +1,15 @@[m
 package LIVEMAP;[m
[31m-[m
[31m-import java.util.Date;[m
[32m+[m[32mimport java.sql.Date;[m
 [m
 public class Member {[m
[31m-	private int memberId;       // 회원 번호 (PK)[m
[31m-    private String memberEmail;    // 이메일아이디[m
[31m-    private String memberPw;    // 비밀번호[m
[31m-    private String memberName;  // 이름[m
[31m-	private String memberTel;   // 전화번호[m
[31m-	private String memberSignup;[m
[31m-	private Date memberCreateddate;[m
[31m-	private Date memberUpdatedate;[m
[31m-	private char memberGender;[m
[31m-	private Date memberBirth;[m
[31m-	private String memberAddr1;[m
[31m-	private String memberAddr2;[m
[31m-	private String memberNickname;[m
[31m-	[m
[31m-	[m
[31m-	[m
[31m-	public Member(int memberId, String memberEmail, String memberPw, String memberName, String memberTel,[m
[31m-			String memberSignup, Date memberCreateddate, Date memberUpdatedate, char memberGender, Date memberBirth,[m
[31m-			String memberAddr1, String memberAddr2, String memberNickname) {[m
[31m-		super();[m
[31m-		this.memberId = memberId;[m
[31m-		this.memberEmail = memberEmail;[m
[31m-		this.memberPw = memberPw;[m
[31m-		this.memberName = memberName;[m
[31m-		this.memberTel = memberTel;[m
[31m-		this.memberSignup = memberSignup;[m
[31m-		this.memberCreateddate = memberCreateddate;[m
[31m-		this.memberUpdatedate = memberUpdatedate;[m
[31m-		this.memberGender = memberGender;[m
[31m-		this.memberBirth = memberBirth;[m
[31m-		this.memberAddr1 = memberAddr1;[m
[31m-		this.memberAddr2 = memberAddr2;[m
[31m-		this.memberNickname = memberNickname;[m
[31m-	}[m
[31m-[m
[32m+[m	[32mprivate int memberId;[m
[32m+[m	[32mprivate String email;[m
[32m+[m	[32mprivate String nickname;[m
[32m+[m	[32mprivate String name;[m
[32m+[m	[32mprivate String signup;[m
[32m+[m	[32mprivate Date createdDate;[m
[32m+[m	[32mprivate Date updatedDate;[m
 [m
[31m-	[m
 	public int getMemberId() {[m
 		return memberId;[m
 	}[m
[36m@@ -48,112 +18,51 @@[m [mpublic class Member {[m
 		this.memberId = memberId;[m
 	}[m
 [m
[31m-	public String getMemberEmail() {[m
[31m-		return memberEmail;[m
[31m-	}[m
[31m-[m
[31m-	public void setMemberEmail(String memberEmail) {[m
[31m-		this.memberEmail = memberEmail;[m
[31m-	}[m
[31m-[m
[31m-	public String getMemberPw() {[m
[31m-		return memberPw;[m
[31m-	}[m
[31m-[m
[31m-	public void setMemberPw(String memberPw) {[m
[31m-		this.memberPw = memberPw;[m
[31m-	}[m
[31m-[m
[31m-	public String getMemberName() {[m
[31m-		return memberName;[m
[31m-	}[m
[31m-[m
[31m-	public void setMemberName(String memberName) {[m
[31m-		this.memberName = memberName;[m
[31m-	}[m
[31m-[m
[31m-	public String getMemberTel() {[m
[31m-		return memberTel;[m
[32m+[m	[32mpublic String getEmail() {[m
[32m+[m		[32mreturn email;[m
 	}[m
 [m
[31m-	public void setMemberTel(String memberTel) {[m
[31m-		this.memberTel = memberTel;[m
[32m+[m	[32mpublic void setEmail(String email) {[m
[32m+[m		[32mthis.email = email;[m
 	}[m
 [m
[31m-	public String getMemberSignup() {[m
[31m-		return memberSignup;[m
[32m+[m	[32mpublic String getNickname() {[m
[32m+[m		[32mreturn nickname;[m
 	}[m
 [m
[31m-	public void setMemberSignup(String memberSignup) {[m
[31m-		this.memberSignup = memberSignup;[m
[32m+[m	[32mpublic void setNickname(String nickname) {[m
[32m+[m		[32mthis.nickname = nickname;[m
 	}[m
 [m
[31m-	public Date getMemberCreateddate() {[m
[31m-		return memberCreateddate;[m
[32m+[m	[32mpublic String getName() {[m
[32m+[m		[32mreturn name;[m
 	}[m
 [m
[31m-	public void setMemberCreateddate(Date memberCreateddate) {[m
[31m-		this.memberCreateddate = memberCreateddate;[m
[32m+[m	[32mpublic void setName(String name) {[m
[32m+[m		[32mthis.name = name;[m
 	}[m
 [m
[31m-	public Date getMemberUpdatedate() {[m
[31m-		return memberUpdatedate;[m
[32m+[m	[32mpublic String getSignup() {[m
[32m+[m		[32mreturn signup;[m
 	}[m
 [m
[31m-	public void setMemberUpdatedate(Date memberUpdatedate) {[m
[31m-		this.memberUpdatedate = memberUpdatedate;[m
[32m+[m	[32mpublic void setSignup(String signup) {[m
[32m+[m		[32mthis.signup = signup;[m
 	}[m
 [m
[31m-	public char getMemberGender() {[m
[31m-		return memberGender;[m
[32m+[m	[32mpublic Date getCreatedDate() {[m
[32m+[m		[32mreturn createdDate;[m
 	}[m
 [m
[31m-	public void setMemberGender(char memberGender) {[m
[31m-		this.memberGender = memberGender;[m
[32m+[m	[32mpublic void setCreatedDate(Date createdDate) {[m
[32m+[m		[32mthis.createdDate = createdDate;[m
 	}[m
 [m
[31m-	public Date getMemberBirth() {[m
[31m-		return memberBirth;[m
[32m+[m	[32mpublic Date getUpdatedDate() {[m
[32m+[m		[32mreturn updatedDate;[m
 	}[m
 [m
[31m-	public void setMemberBirth(Date memberBirth) {[m
[31m-		this.memberBirth = memberBirth;[m
[31m-	}[m
[31m-[m
[31m-	public String getMemberAddr1() {[m
[31m-		return memberAddr1;[m
[31m-	}[m
[31m-[m
[31m-	public void setMemberAddr1(String memberAddr1) {[m
[31m-		this.memberAddr1 = memberAddr1;[m
[31m-	}[m
[31m-[m
[31m-	public String getMemberAddr2() {[m
[31m-		return memberAddr2;[m
[31m-	}[m
[31m-[m
[31m-	public void setMemberAddr2(String memberAddr2) {[m
[31m-		this.memberAddr2 = memberAddr2;[m
[31m-	}[m
[31m-[m
[31m-	public String getMemberNickname() {[m
[31m-		return memberNickname;[m
[31m-	}[m
[31m-[m
[31m-	public void setMemberNickname(String memberNickname) {[m
[31m-		this.memberNickname = memberNickname;[m
[31m-	}[m
[31m-[m
[31m-[m
[31m-[m
[31m-	@Override[m
[31m-	public String toString() {[m
[31m-		return "Member [memberId=" + memberId + ", memberEmail=" + memberEmail + ", memberPw=" + memberPw[m
[31m-				+ ", memberName=" + memberName + ", memberTel=" + memberTel + ", memberSignup=" + memberSignup[m
[31m-				+ ", memberCreateddate=" + memberCreateddate + ", memberUpdatedate=" + memberUpdatedate[m
[31m-				+ ", memberGender=" + memberGender + ", memberBirth=" + memberBirth + ", memberAddr1=" + memberAddr1[m
[31m-				+ ", memberAddr2=" + memberAddr2 + ", memberNickname=" + memberNickname + "]";[m
[32m+[m	[32mpublic void setUpdatedDate(Date updatedDate) {[m
[32m+[m		[32mthis.updatedDate = updatedDate;[m
 	}[m
[31m-	[m
[31m-	[m
 }[m
[1mdiff --git a/LIVEMAP/src/main/java/LIVEMAP/MemberDAO.java b/LIVEMAP/src/main/java/LIVEMAP/MemberDAO.java[m
[1mindex 6d812e7..289fa4e 100644[m
[1m--- a/LIVEMAP/src/main/java/LIVEMAP/MemberDAO.java[m
[1m+++ b/LIVEMAP/src/main/java/LIVEMAP/MemberDAO.java[m
[36m@@ -1,109 +1,194 @@[m
 package LIVEMAP;[m
 [m
[31m-import java.sql.Connection;[m
[31m-import java.sql.DriverManager;[m
[31m-import java.sql.PreparedStatement;[m
[31m-import java.sql.ResultSet;[m
[31m-import java.sql.SQLException;[m
[31m-[m
[32m+[m[32mimport java.sql.*;[m
 [m
[32m+[m[32m/**[m
[32m+[m[32m * SCOTT 스키마 기준 MemberDAO 풀버전 - findByPk - findByEmail (스키마 접두사 + 타임아웃 + 상세로그) -[m
[32m+[m[32m * insertKakaoMember (INSERT→CURRVAL, 실패시 이메일 재조회 fallback) -[m
[32m+[m[32m * updateFromKakaoProfile (널만 채움)[m
[32m+[m[32m */[m
 public class MemberDAO {[m
[31m-	String driver="oracle.jdbc.driver.OracleDriver";[m
[31m-	String url="jdbc:oracle:thin:@localhost:1521:testdb";[m
[31m-	String user="scott";[m
[31m-	String password="tiger";[m
[31m-	public Connection dbcon() {		 [m
[31m-		Connection con = null;[m
[32m+[m
[32m+[m	[32mprivate static final String DRIVER = "oracle.jdbc.OracleDriver";[m
[32m+[m	[32mprivate static final String URL = "jdbc:oracle:thin:@//localhost:1521/testdb";[m
[32m+[m	[32mprivate static final String USER = "SCOTT";[m
[32m+[m	[32mprivate static final String PASSWORD = "tiger";[m
[32m+[m
[32m+[m	[32mstatic {[m
 		try {[m
[31m-			Class.forName(driver);[m
[31m-			con =DriverManager.getConnection(url, user, password);[m
[31m-			if( con != null) System.out.println( "ok");[m
[31m-			[m
[32m+[m			[32mClass.forName(DRIVER);[m
[32m+[m			[32mDriverManager.setLoginTimeout(3);[m
 		} catch (ClassNotFoundException e) {[m
[31m-			// TODO Auto-generated catch block[m
[31m-			e.printStackTrace();[m
[32m+[m			[32mthrow new RuntimeException(e);[m
[32m+[m		[32m}[m
[32m+[m	[32m}[m
[32m+[m
[32m+[m	[32mprivate Connection getConnection() throws SQLException {[m
[32m+[m		[32mConnection con = DriverManager.getConnection(URL, USER, PASSWORD);[m
[32m+[m		[32mcon.setAutoCommit(true);[m
[32m+[m		[32mreturn con;[m
[32m+[m	[32m}[m
[32m+[m
[32m+[m	[32m// ------------------------------------------------------------[m
[32m+[m	[32m// 매핑 유틸[m
[32m+[m	[32m// ------------------------------------------------------------[m
[32m+[m	[32mprivate Member map(ResultSet rs) throws SQLException {[m
[32m+[m		[32mMember m = new Member();[m
[32m+[m		[32mm.setMemberId(rs.getInt("member_id"));[m
[32m+[m		[32mm.setEmail(rs.getString("member_email"));[m
[32m+[m		[32mm.setNickname(rs.getString("member_nickname"));[m
[32m+[m		[32mm.setName(rs.getString("member_name"));[m
[32m+[m		[32mm.setSignup(rs.getString("member_signup"));[m
[32m+[m		[32mm.setCreatedDate(rs.getDate("member_createddate"));[m
[32m+[m		[32mm.setUpdatedDate(rs.getDate("member_updatedate"));[m
[32m+[m		[32mreturn m;[m
[32m+[m	[32m}[m
[32m+[m
[32m+[m	[32m// ------------------------------------------------------------[m
[32m+[m	[32m// PK 조회[m
[32m+[m	[32m// ------------------------------------------------------------[m
[32m+[m	[32mpublic Member findByPk(int memberId) {[m
[32m+[m		[32mfinal String sql = "SELECT member_id, member_email, member_nickname, member_name, "[m
[32m+[m				[32m+ "       member_signup, member_createddate, member_updatedate "[m
[32m+[m				[32m+ "  FROM SCOTT.members WHERE member_id = ?";[m
[32m+[m		[32mtry (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {[m
[32m+[m			[32mps.setQueryTimeout(5);[m
[32m+[m			[32mps.setInt(1, memberId);[m
[32m+[m			[32mtry (ResultSet rs = ps.executeQuery()) {[m
[32m+[m				[32mreturn rs.next() ? map(rs) : null;[m
[32m+[m			[32m}[m
[32m+[m		[32m} catch (SQLException e) {[m
[32m+[m			[32mthrow new RuntimeException("findByPk failed: " + memberId, e);[m
[32m+[m		[32m}[m
[32m+[m	[32m}[m
[32m+[m
[32m+[m	[32m// ------------------------------------------------------------[m
[32m+[m	[32m// 이메일 길이 가드 (DDL이 100자라면 초과분 잘라서 저장/조회)[m
[32m+[m	[32m// ------------------------------------------------------------[m
[32m+[m	[32mprivate static String clampEmail(String email) {[m
[32m+[m		[32mif (email == null)[m
[32m+[m			[32mreturn null;[m
[32m+[m		[32mreturn email.length() > 100 ? email.substring(0, 100) : email;[m
[32m+[m	[32m}[m
[32m+[m
[32m+[m	[32m// ------------------------------------------------------------[m
[32m+[m	[32m// 이메일로 회원 조회 (스키마 접두사 + 타임아웃 + 상세 로그)[m
[32m+[m	[32m// ------------------------------------------------------------[m
[32m+[m	[32mpublic Member findByEmail(String email) {[m
[32m+[m		[32mif (email == null || email.isEmpty())[m
[32m+[m			[32mreturn null;[m
[32m+[m