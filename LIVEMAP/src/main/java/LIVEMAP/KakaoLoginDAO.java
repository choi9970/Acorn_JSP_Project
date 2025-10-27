package LIVEMAP;

import java.sql.*;
import java.util.Properties;

public class KakaoLoginDAO {

	private static final String DRIVER = "oracle.jdbc.OracleDriver";
	private static final String URL = "jdbc:oracle:thin:@//localhost:1521/testdb"; // 네 환경 그대로
	private static final String USER = "SCOTT";
	private static final String PASSWORD = "tiger";

	private Connection getConnection() throws SQLException {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			throw new SQLException("Oracle JDBC Driver not found", e);
		}

		Properties props = new Properties();
		props.put("user", USER);
		props.put("password", PASSWORD);
		// 네트워크 접속/응답 타임아웃 (ms)
		props.put("oracle.net.CONNECT_TIMEOUT", "3000");
		props.put("oracle.net.READ_TIMEOUT", "4000");

		Connection con = DriverManager.getConnection(URL, props);

		try (Statement s = con.createStatement();
				ResultSet r = s.executeQuery(
						"SELECT sys_context('userenv','service_name'), sys_context('userenv','current_schema') FROM dual")) {
			if (r.next())
				System.err.println("[DB][WHOAMI] service=" + r.getString(1) + ", schema=" + r.getString(2));
		}

		con.setAutoCommit(true);
		return con;
	}

	public Integer findMemberIdByKakaoId(String providerUserId) {
		if (providerUserId == null || providerUserId.isEmpty())
			return null;
		final String sql = "SELECT MEMBER_ID FROM KAKAO_LOGIN WHERE KAKAO_PROVIDERUSERID = ?";
		try (Connection con = getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setQueryTimeout(3); // 초
			ps.setString(1, providerUserId);
			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() ? rs.getInt(1) : null;
			}
		} catch (SQLTimeoutException te) {
			System.err.println("[KAKAO] findMemberIdByKakaoId TIMEOUT: puid=" + providerUserId);
			return null; // TIMEOUT은 화면 흐름 막지 않음
		} catch (SQLException e) {
			System.err.println("[DB][kakao_findByPuid] SQLState=" + e.getSQLState() + " Code=" + e.getErrorCode()
					+ " Msg=" + e.getMessage());
			return null; // 조회 실패도 흡수
		}
	}

	// UPDATE → 0행이면 INSERT (TIMEOUT/경합 시에도 호출부 흐름은 유지)
	public void upsert(String providerUserId, int memberId, String accessToken, String refreshToken) {
		if (providerUserId == null || providerUserId.isEmpty()) {
			System.err.println("[KAKAO] DAO.upsert skip: providerUserId null, memberId=" + memberId);
			return;
		}
		final String upd = "UPDATE kakao_login SET " + "  provider='kakao', " + "  kakao_provideruserid=?, "
				+ "  kakao_accesstoken=?, " + "  kakao_refreshtoken=?, " + "  kakao_connecteddate=SYSDATE "
				+ "WHERE member_id=?";
		final String ins = "INSERT INTO kakao_login "
				+ " (kakao_id, member_id, provider, kakao_provideruserid, kakao_accesstoken, kakao_refreshtoken, kakao_connecteddate) "
				+ "VALUES (SEQ_KAKAO_ID.NEXTVAL, ?, 'kakao', ?, ?, ?, SYSDATE)";

		try (Connection con = getConnection()) {
			// 1) UPDATE
			try (PreparedStatement ps = con.prepareStatement(upd)) {
				ps.setQueryTimeout(3);
				ps.setString(1, providerUserId);
				ps.setString(2, accessToken);
				ps.setString(3, refreshToken);
				ps.setInt(4, memberId);
				int updated = ps.executeUpdate();
				if (updated > 0)
					return;
			}
			// 2) INSERT
			try (PreparedStatement ps = con.prepareStatement(ins)) {
				ps.setQueryTimeout(3);
				ps.setInt(1, memberId);
				ps.setString(2, providerUserId);
				ps.setString(3, accessToken);
				ps.setString(4, refreshToken);
				ps.executeUpdate();
			} catch (SQLException e) {
				// 경합으로 PK/UK 충돌 시 마지막으로 UPDATE 재시도
				if (e.getErrorCode() == 1 /* ORA-00001 */) {
					try (PreparedStatement ps2 = con.prepareStatement(upd)) {
						ps2.setQueryTimeout(3);
						ps2.setString(1, providerUserId);
						ps2.setString(2, accessToken);
						ps2.setString(3, refreshToken);
						ps2.setInt(4, memberId);
						ps2.executeUpdate();
					}
				} else {
					throw e;
				}
			}
		} catch (SQLTimeoutException te) {
			System.err.println("[KAKAO] upsert TIMEOUT member_id=" + memberId + ", puid=" + providerUserId);
			// TIMEOUT 흡수: 화면 전환/세션 흐름 방해 금지
		} catch (SQLException e) {
			System.err.println("[DB][kakao_upsert] SQLState=" + e.getSQLState() + " Code=" + e.getErrorCode() + " Msg="
					+ e.getMessage());
			// 치명적 오류도 호출부는 계속 진행(로그만 남김)
		}
	}
}
