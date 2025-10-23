package LIVEMAP;


public class MemberService {
	MemberDAO dao = new MemberDAO();
	
	boolean loginJudgeService(String email,String pw) {
		int cnt = dao.loginJudge(email,pw);
		boolean result=cnt>0?true:false;
		return result;
	}
	public Member getfindById(String email) {
		Member member=dao.findById(email);
		return member;
	}


}
