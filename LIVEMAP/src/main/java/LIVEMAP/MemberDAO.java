package LIVEMAP;

import java.sql.*;

public class MemberDAO {

	private static final String DRIVER = "oracle.jdbc.OracleDriver";
	private static final String URL = "jdbc:oracle:thin:@//localhost:1521/testdb";
	private static final String USER = "SCOTT";
	private static final String PASSWORD = "tiger";

	static {
		try {
			Class.forName(DRIVER);
			DriverManager.setLoginTimeout(3);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Connection getConnection() throws SQLException {
		Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
		con.setAutoCommit(true);
		return con;
	}

	private Member map(ResultSet rs) throws SQLException {
		Member m = new Member();
		m.setMemberId(rs.getInt("member_id"));
		m.setEmail(rs.getString("member_email"));
		m.setNickname(rs.getString("member_nickname"));
		m.setName(rs.getString("member_name"));
		m.setSignup(rs.getString("member_signup"));
		m.setCreatedDate(rs.getDate("member_createddate"));
		m.setUpdatedDate(rs.getDate("member_updatedate"));
		return m;
	}

	public Member findByPk(int memberId) {
		final String sql = "SELECT member_id, member_email, member_nickname, member_name, "
				+ "       member_signup, member_createddate, member_updatedate "
				+ "  FROM SCOTT.members WHERE member_id = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setQueryTimeout(5);
			ps.setInt(1, memberId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		} catch (SQLException e) {
			throw new RuntimeException("findByPk failed: " + memberId, e);
		}
	}

	private static String clampEmail(String email) {
		if (email == null)
			return null;
		return email.length() > 100 ? email.substring(0, 100) : email;
	}

	public Member findByEmail(String email) {
		if (email == null || email.isEmpty())
			return null;
		final String sql = "SELECT member_id, member_email, member_nickname, member_name, "
				+ "       member_signup, member_createddate, member_updatedate "
				+ "  FROM SCOTT.members WHERE member_email = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setQueryTimeout(5);
			ps.setString(1, clampEmail(email));
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? map(rs) : null;
			}
		} catch (SQLException e) {
			System.err.println("[DB][findByEmail] SQLState=" + e.getSQLState() + " Code=" + e.getErrorCode() + " Msg="
					+ e.getMessage());
			return null;
		}
	}

	public Member insertKakaoMember(String email, String nickname) {
		final String insert = "INSERT INTO SCOTT.members (" + "  member_email, member_pw, member_name, member_tel, "
				+ "  member_signup, member_createddate, member_updatedate, "
				+ "  member_sex, member_birth, member_addr1, member_addr2, member_nickname"
				+ ") VALUES (?, NULL, NULL, NULL, 'kakao', SYSDATE, SYSDATE, NULL, NULL, NULL, NULL, ?)";

		try (Connection con = getConnection()) {
			con.setAutoCommit(true);

			try (PreparedStatement ps = con.prepareStatement(insert)) {
				ps.setQueryTimeout(5);
				String em = clampEmail(email);
				if (em == null || em.isEmpty())
					ps.setNull(1, Types.VARCHAR);
				else
					ps.setString(1, em);
				ps.setString(2, (nickname == null || nickname.isEmpty()) ? "kakao_user" : nickname);
				ps.executeUpdate();
			}

			Integer newId = null;
			try (PreparedStatement ps = con.prepareStatement("SELECT SCOTT.SEQ_MEMBERS.CURRVAL FROM dual");
					ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					newId = rs.getInt(1);
			} catch (SQLException e) {
				System.err.println("[DB][insertKakaoMember][CURRVAL] " + e.getMessage());
			}

			if (newId != null) {
				return findByPk(newId);
			} else if (email != null && !email.isEmpty()) {
				Member m = findByEmail(email);
				if (m != null)
					return m;
			}

			try (PreparedStatement ps = con
					.prepareStatement("SELECT member_id, member_email, member_nickname, member_name, "
							+ "       member_signup, member_createddate, member_updatedate "
							+ "  FROM SCOTT.members ORDER BY member_id DESC FETCH FIRST 1 ROWS ONLY");
					ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return map(rs);
			}

			throw new IllegalStateException("insertKakaoMember: cannot resolve new member_id");
		} catch (SQLException e) {
			System.err.println("[DB][insertKakaoMember] SQLState=" + e.getSQLState() + " Code=" + e.getErrorCode()
					+ " Msg=" + e.getMessage());
			throw new RuntimeException("insertKakaoMember failed", e);
		}
	}

	public void updateFromKakaoProfile(int memberId, KakaoProfileData d) {
		if (d == null)
			return;

		final String sql = "UPDATE SCOTT.members SET " + "  member_email      = COALESCE(?, member_email), "
				+ "  member_nickname   = COALESCE(?, member_nickname), "
				+ "  member_name       = COALESCE(?, member_name), " + "  member_tel        = COALESCE(?, member_tel), "
				+ "  member_sex        = COALESCE(?, member_sex), "
				+ "  member_birth      = COALESCE(?, member_birth), " + "  member_updatedate = SYSDATE "
				+ "WHERE member_id = ?";

		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setQueryTimeout(5);
			ps.setString(1, d.email);
			ps.setString(2, d.nickname);
			ps.setString(3, d.name);
			ps.setString(4, d.phone);
			ps.setString(5, d.gender);
			if (d.birth != null)
				ps.setDate(6, d.birth);
			else
				ps.setNull(6, Types.DATE);
			ps.setInt(7, memberId);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("[DB][member_updateFromKakao] SQLState=" + e.getSQLState() + " Code=" + e.getErrorCode()
					+ " Msg=" + e.getMessage());
		}
	}

	public int loginJudge(String email, String pw) {
		if (email == null || pw == null)
			return 0;
		final String sql = "SELECT COUNT(*) FROM SCOTT.members WHERE member_email=? AND member_pw=?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setQueryTimeout(5);
			ps.setString(1, clampEmail(email));
			ps.setString(2, pw); // 운영에서는 BCrypt 등 해시 사용
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException e) {
			System.err.println("[DB][loginJudge] " + e.getMessage());
			return 0;
		}
	}

	public Member findById(String email) {
		return findByEmail(email);
	}
}
