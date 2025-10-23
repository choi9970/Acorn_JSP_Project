package LIVEMAP;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class MemberDAO {
	String driver="oracle.jdbc.driver.OracleDriver";
	String url="jdbc:oracle:thin:@localhost:1521:testdb";
	String user="scott";
	String password="tiger";
	public Connection dbcon() {		 
		Connection con = null;
		try {
			Class.forName(driver);
			con =DriverManager.getConnection(url, user, password);
			if( con != null) System.out.println( "ok");
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;		 	
	}
	
	public int loginJudge(String id, String pw) {
	    // 💡 만약 데이터베이스의 컬럼명이 MEMBER_EMAIL이라면 아래 SQL문을 수정하세요.
	    // String sql = "SELECT * FROM MINI_MEMBER WHERE MEMBER_EMAIL = ? and MEMBER_PW = ?";
		String sql = "SELECT * FROM MEMBERS WHERE MEMBER_EMAIL = ? and MEMBER_PW = ?";
	    int result = 0; // 로그인 실패 시 0을 반환하도록 초기화

	    // try-with-resources 구문을 사용하면 자원이 자동으로 close 됩니다.
	    try (Connection con = dbcon();
	         PreparedStatement pst = con.prepareStatement(sql)) {
	        
	        pst.setString(1, id);
	        pst.setString(2, pw);
	   
	        // 🚨 수정된 부분: executeUpdate() -> executeQuery()로 변경
	        ResultSet rs = pst.executeQuery();
	        
	        // 🚨 추가된 부분: 조회된 결과(rs)가 있는지 확인
	        if (rs.next()) {
	            result = 1; // 결과가 있으면(로그인 성공) 1을 반환
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	public Member findById(String id) {
	    Connection con = dbcon();
	    
	    // 🚨 수정된 부분: 컬럼명을 MEMBER_EMAIL (대문자)로 변경했습니다.
	    String sql = "SELECT * FROM MEMBERS WHERE MEMBER_EMAIL = ?"; 
	    
	    PreparedStatement pst = null;
	    ResultSet rs = null;
	    Member member = null;
	    try {
	        pst = con.prepareStatement(sql);
	        pst.setString(1, id); // 여기서 id는 실제로는 이메일 주소입니다.
	        rs = pst.executeQuery();

	        if (rs.next()) {
	            // ResultSet에서 Member 객체의 모든 필드 값을 가져옵니다.
	            int memberId = rs.getInt("MEMBER_ID");
	            String memberEmail = rs.getString("MEMBER_EMAIL");
	            String memberPw = rs.getString("MEMBER_PW");
	            String memberName = rs.getString("MEMBER_NAME");
	            String memberTel = rs.getString("MEMBER_TEL");
	            String memberSignup = rs.getString("MEMBER_SIGNUP");
	            java.util.Date memberCreateddate = rs.getDate("MEMBER_CREATEDDATE");
	            java.util.Date memberUpdatedate = rs.getDate("MEMBER_UPDATEDATE");
	            // 🚨 주의: 데이터베이스 컬럼명은 'MEMBER_SEX'인데, Java 필드는 'memberGender' 입니다.
	            //    이름이 다르므로 rs.getString("MEMBER_SEX")로 DB 컬럼명에 맞춰야 합니다.
	            char memberGender = rs.getString("MEMBER_SEX").charAt(0);
	            java.util.Date memberBirth = rs.getDate("MEMBER_BIRTH");
	            String memberAddr1 = rs.getString("MEMBER_ADDR1");
	            String memberAddr2 = rs.getString("MEMBER_ADDR2");
	            String memberNickname = rs.getString("MEMBER_NICKNAME");

	            member = new Member(memberId, memberEmail, memberPw, memberName, memberTel,
	                                memberSignup, memberCreateddate, memberUpdatedate, memberGender,
	                                memberBirth, memberAddr1, memberAddr2, memberNickname);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        // 자원 해제
	        try {
	            if (rs != null) rs.close();
	            if (pst != null) pst.close();
	            if (con != null) con.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    return member;
	}
}
