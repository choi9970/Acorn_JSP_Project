package LIVEMAP;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("WEB-INF/views/login.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");	
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		String email = request.getParameter("memberEmail");
		String pwd = request.getParameter("memberPw");
		
		System.out.println( email);
		System.out.println( pwd);
		
		MemberService service  = new MemberService();
		boolean result=service.loginJudgeService(email,pwd);
		
		if(result) {
	     	//회원인경우 session저장소에 저장하기
			//1)  session  저장소 얻어오기   => 사용자에 대한 저장소 얻어오기  ( 세션ID값으로 얻어온다 )
			// 서션id => 서버에 최초에 접속하면 서버가 발급하는 키 
			
			HttpSession session  = request.getSession();
			
			//2) 저장하기 
			session.setAttribute("memberId", email);			
			
			//메인페이지가 요청되게 하기
			response.sendRedirect(request.getContextPath() + "/main.do");		 			
		
		}
		else {		   
			// 로그인 실패 메시지 request에 저장
		    request.setAttribute("msg", "로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.");
			request.getRequestDispatcher("WEB-INF/views/login.jsp").forward(request, response);			
			
		}
	
	}

}
