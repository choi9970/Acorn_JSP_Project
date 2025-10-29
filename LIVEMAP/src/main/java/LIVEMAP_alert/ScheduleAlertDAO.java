package LIVEMAP_alert;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap; // [추가] HashMap 사용을 위해 import
import java.util.List;
import java.util.Map;    // [추가] Map 사용을 위해 import

public class ScheduleAlertDAO {

    private String driver = "oracle.jdbc.driver.OracleDriver";
    private String url = "jdbc:oracle:thin:@localhost:1521:testdb";
    private String user = "scott";
    private String password = "tiger";

    // 생성자 및 DB 연결/종료 메서드 (기존과 동일)
    public ScheduleAlertDAO() {
        try { Class.forName(driver); } catch (ClassNotFoundException e) { e.printStackTrace(); }
    }
    private Connection dbcon() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
    private void close(Connection conn, PreparedStatement pstmt) {
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
    
    
    /**
     * 알림 구독 상태를 토글(Y/N)합니다.
     * @return String 토글 후의 최종 상태 ("Y", "N") 또는 "ERROR"
     */
    public String toggleSubscription(int memberId, int scheduleId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        // 1. 현재 구독 상태 확인
        String sqlCheck = "SELECT alert_enabled FROM mycart_schedules WHERE member_id = ? AND schedule_id = ?";
        String currentState = null; // null: 없음, "Y": 구독 중, "N": 구독 취소 상태
        String newState = "ERROR";   // 반환할 최종 상태

        try {
            conn = dbcon();
            pstmt = conn.prepareStatement(sqlCheck);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, scheduleId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                currentState = rs.getString("alert_enabled");
            }
            
            close(null, pstmt, rs); // 첫 번째 pstmt, rs 닫기

            // 2. 상태에 따른 분기 처리 (INSERT 또는 UPDATE)
            String sqlToggle = null;
            
            if (currentState == null) {
                // Case 1: 신규 구독 (INSERT) -> 'Y' 상태가 됨
                sqlToggle = "INSERT INTO mycart_schedules (schedule_id, member_id, alert_enabled, schedule_regdata, sent_5min_alert, sent_0min_alert) VALUES (?, ?, 'Y', SYSDATE, 'N', 'N')";
                newState = "Y";
                
                pstmt = conn.prepareStatement(sqlToggle);
                pstmt.setInt(1, scheduleId);
                pstmt.setInt(2, memberId);

            } else if ("N".equals(currentState)) {
                // Case 2: 재구독 (UPDATE 'N' -> 'Y')
                // 알림 플래그 등도 초기화
                sqlToggle = "UPDATE mycart_schedules SET alert_enabled = 'Y', schedule_regdata = SYSDATE, sent_5min_alert = 'N', sent_0min_alert = 'N' WHERE member_id = ? AND schedule_id = ?";
                newState = "Y";
                
                pstmt = conn.prepareStatement(sqlToggle);
                pstmt.setInt(1, memberId);
                pstmt.setInt(2, scheduleId);
                
            } else if ("Y".equals(currentState)) {
                // Case 3: 구독 취소 (UPDATE 'Y' -> 'N')
                sqlToggle = "UPDATE mycart_schedules SET alert_enabled = 'N' WHERE member_id = ? AND schedule_id = ?";
                newState = "N";

                pstmt = conn.prepareStatement(sqlToggle);
                pstmt.setInt(1, memberId);
                pstmt.setInt(2, scheduleId);
            }

            // 3. 쿼리 실행
            int rowsAffected = pstmt.executeUpdate();
            
            // 성공하면 newState ("Y" or "N") 반환, 실패하면 "ERROR" 반환
            return (rowsAffected > 0) ? newState : "ERROR";

        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR";
        } finally {
            close(conn, pstmt); // 마지막에 사용된 conn과 pstmt 닫기
        }
    }
    
    
    public List<AlertTarget> findDeletedAlertTargets() {
        List<AlertTarget> targets = new ArrayList<>();
        
        // [핵심 SQL]
        String sql = "SELECT m.member_id, s.schedule_name, s.schedule_id, kl.kakao_accesstoken " +
                "FROM schedules s " +
                "JOIN mycart_schedules c ON s.schedule_id = c.schedule_id " +
                "JOIN members m ON c.member_id = m.member_id " +
                "JOIN kakao_login kl ON m.member_id = kl.member_id " +
                "WHERE c.alert_enabled = 'Y' " +         
                "  AND s.schedule_deleteflg = 1 " +       
                "  AND kl.provider = 'kakao'";           // 3. 카카오톡 사용자에게만 전송 (액세스 토큰 필요)

        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = dbcon();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                targets.add(new AlertTarget(
                    rs.getInt("member_id"), rs.getInt("schedule_id"),
                    null, 
                    rs.getString("schedule_name"),
                    rs.getString("kakao_accesstoken") // [복구] scheduleImg 파라미터 제거
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); } finally { close(conn, pstmt, rs); }
        return targets;
    }
    
    
    /**
     * 스케줄러가 호출할 알림 대상 조회 (기존과 동일)
     */
    public List<AlertTarget> findTargets(String type) {
        List<AlertTarget> targets = new ArrayList<>();
        String timeCondition = (type.equals("5min"))
            ? "s.schedule_start BETWEEN (SYSDATE + 5/1440) AND (SYSDATE + 6/1440)"
            : "s.schedule_start BETWEEN SYSDATE AND (SYSDATE + 1/1440)";
        String flagCondition = (type.equals("5min"))
            ? "c.sent_5min_alert = 'N'"
            : "c.sent_0min_alert = 'N'";

        String sql = "SELECT m.member_id, m.member_tel, s.schedule_name, s.schedule_id, kl.kakao_accesstoken " +
                     "FROM schedules s " +
                     "JOIN mycart_schedules c ON s.schedule_id = c.schedule_id " +
                     "JOIN members m ON c.member_id = m.member_id " +
                     "JOIN kakao_login kl ON m.member_id = kl.member_id " +
                     "WHERE c.alert_enabled = 'Y' AND s.schedule_deleteflg = 0 " +
                     "  AND kl.provider = 'kakao' " +
                     "  AND " + timeCondition + " AND " + flagCondition;

        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = dbcon();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                targets.add(new AlertTarget(
                    rs.getInt("member_id"), rs.getInt("schedule_id"),
                    rs.getString("member_tel"), rs.getString("schedule_name"),
                    rs.getString("kakao_accesstoken")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); } finally { close(conn, pstmt, rs); }
        return targets;
    }

    /**
     * 발송 완료 상태 업데이트 (기존과 동일)
     */
    public void updateSentStatus(int memberId, int scheduleId, String type) {
    	
    		String sql = "";
    		
    		if (type.equals("5min")) {
                // 5분 전 알림: 발송 플래그만 'Y'로 변경
                sql = "UPDATE mycart_schedules SET sent_5min_alert = 'Y' WHERE member_id = ? AND schedule_id = ?";
                
        } else if (type.equals("0min")) {
                // [핵심] 정각 알림: 발송 플래그('Y')와 구독상태('N')를 동시에 변경
                sql = "UPDATE mycart_schedules SET sent_0min_alert = 'Y', alert_enabled = 'N' WHERE member_id = ? AND schedule_id = ?";
                
        } else {
                System.err.println("updateSentStatus: 알 수 없는 타입 " + type);
                return; // 쿼리 실행 안 함
        }
    		
        Connection conn = null; PreparedStatement pstmt = null;
        try {
            conn = dbcon();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, scheduleId);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); } finally { close(conn, pstmt); }
    }

    /**
     * 특정 사용자가 신청한 알림 목록 조회 (JOIN 사용)
     * @param memberId 회원 ID
     * @return 신청한 스케줄 정보 목록 (Map 형태)
     */
    public List<Map<String, Object>> findSubscribedSchedulesByMemberId(int memberId) {
        List<Map<String, Object>> list = new ArrayList<>();
        // 필요한 스케줄 정보 + 플랫폼 이름 + mycart 정보(여기선 필터링용) JOIN
        String sql = "SELECT " +
                "    s.schedule_id, s.schedule_name, s.schedule_start, s.schedule_price, " +
                "    s.schedule_img, s.schedule_url, p.platform_name, s.schedule_deleteflg " + // [추가] deleteflg를 JSP에서 사용하기 위해 컬럼 추가
                "FROM mycart_schedules c " + 
                "JOIN schedules s ON c.schedule_id = s.schedule_id " + 
                "JOIN platforms p ON s.platform_id = p.platform_id " + 
                "WHERE c.member_id = ? AND c.alert_enabled = 'Y' " + // <-- s.schedule_deleteflg = 0 조건 제거
                "ORDER BY s.schedule_start"; // 방송 시작 시간 순으로 정렬

        Connection conn = null; PreparedStatement pstmt = null; ResultSet rs = null;
        try {
            conn = dbcon();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId); // SQL의 ? 자리에 memberId 값 설정
            rs = pstmt.executeQuery(); // 쿼리 실행

            // 결과(ResultSet)를 순회하며 Map으로 변환 후 List에 추가
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>(); // 각 행을 담을 Map 생성
                row.put("scheduleId", rs.getInt("schedule_id"));
                row.put("scheduleName", rs.getString("schedule_name"));
                row.put("scheduleStart", rs.getTimestamp("schedule_start"));
                row.put("schedulePrice", rs.getObject("schedule_price") != null ? rs.getDouble("schedule_price") : null);
                row.put("scheduleImg", rs.getString("schedule_img"));
                row.put("scheduleUrl", rs.getString("schedule_url"));
                row.put("platformName", rs.getString("platform_name"));
                row.put("scheduleDeleteFlg", rs.getInt("schedule_deleteflg"));
                list.add(row); // 완성된 Map을 List에 추가
            }
        } catch (SQLException e) {
            System.err.println("findSubscribedSchedulesByMemberId 오류: " + e.getMessage()); // 에러 로그 추가
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs);
        }
        System.out.println("조회된 알림 목록 (" + memberId + "): " + list.size() + "건"); // 조회 건수 로그 추가
        return list;
    }
    
    public boolean cancelSubscription(int memberId, int scheduleId) {
        // alert_enabled 플래그만 'N'으로 변경 (데이터 삭제 대신)
        String sql = "UPDATE mycart_schedules SET alert_enabled = 'N' WHERE member_id = ? AND schedule_id = ?";
        Connection conn = null; PreparedStatement pstmt = null;
        try {
            conn = dbcon();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, scheduleId);
            int affectedRows = pstmt.executeUpdate(); // 영향받은 행의 수 반환
            // 실제로 1개 행이 업데이트되었는지 확인하는 것이 더 정확
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("cancelSubscription 오류: " + e.getMessage()); // 에러 로그 추가
            e.printStackTrace();
            return false;
        } finally {
            close(conn, pstmt); // 자원 해제
        }
    }
}