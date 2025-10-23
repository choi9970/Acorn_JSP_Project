package LIVEMAP;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



@WebServlet("/main.do")
public class MainServlet extends HttpServlet{
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");	
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		
		HttpSession session = request.getSession();
		String id  = (String)session.getAttribute("memberId");
		System.out.println(id);
		
		MemberService service= new MemberService();
		Member member= service.getfindById(id);
		System.out.println(member);
		
		request.setAttribute("member", member);
		request.getRequestDispatcher("WEB-INF/views/main.jsp").forward(request, response);
	}
	
}
