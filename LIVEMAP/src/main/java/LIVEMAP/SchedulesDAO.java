package LIVEMAP;

import java.sql.*;
import java.sql.Date; // 사용되지 않으므로 제거해도 됩니다.
import java.text.SimpleDateFormat; // [수정] 추가
import java.time.LocalDateTime; // 사용되지 않으므로 제거해도 됩니다.
import java.time.format.DateTimeFormatter; // 사용되지 않으므로 제거해도 됩니다.
import java.util.*;

public class SchedulesDAO {

    String driver = "oracle.jdbc.driver.OracleDriver";
    String url = "jdbc:oracle:thin:@localhost:1521:testdb";
    String user = "scott";
    String password = "tiger";

    // ✅ DB 연결 (기존과 동일)
    public Connection dbcon() {
        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);
            // System.out.println("DB 연결 ok: " + con.getMetaData().getURL()); // 너무 자주 찍히므로 주석 처리 권장
            return con;
        } catch (Exception e) {
            System.err.println("DB 연결 실패: " + e.getMessage()); // 에러 로그 강화
            e.printStackTrace();
        }
        return null;
    }

    // --- DB 자원 해제용 private 메서드 ---
    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
    private void close(Connection conn, PreparedStatement pstmt) {
        try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
        try { if (conn != null) conn.close(); } catch (Exception e) {}
    }
    // ------------------------------------

    // ✅ 모든 스케줄 조회 (기존과 동일)
    public List<Schedules> getAllSchedules() {
        List<Schedules> list = new ArrayList<>();
        String sql = "SELECT * FROM schedules ORDER BY schedule_start";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null; // [수정] try-with-resources 밖으로 선언
        try {
            conn = dbcon();
            if (conn == null) return list; // 연결 실패 시 빈 리스트 반환
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Schedules s = new Schedules();
                s.setScheduleId(rs.getInt("schedule_id"));
                s.setScheduleName(rs.getString("schedule_name"));
                s.setScheduleStart(rs.getTimestamp("schedule_start"));
                s.setScheduleEnd(rs.getString("schedule_end")); // 타입 확인 필요 (DB가 TIMESTAMP면 getTimestamp)
                s.setScheduleDiscount(rs.getString("schedule_discount"));
                s.setScheduleImg(rs.getString("schedule_img"));
                s.setScheduleUrl(rs.getString("schedule_url"));
                s.setPlatformId(rs.getInt("platform_id"));
                s.setCategoryId(rs.getInt("category_id"));
                s.setScheduleDeleteFlg(rs.getInt("schedule_deleteflg"));

                // Double 처리 (기존과 동일)
                s.setSchedulePrice(rs.getObject("schedule_price") != null ? rs.getDouble("schedule_price") : null);

                list.add(s);
            }
        } catch (Exception e) {
            System.err.println("getAllSchedules 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(conn, ps, rs); // 자원 해제
        }
        return list;
    }

    // ✅ 스케줄 존재 여부 확인 (부분 일치 - 사용되지 않는다면 제거 고려)
    public boolean existsSchedule(String scheduleName) {
        String sql = "SELECT schedule_id FROM schedules WHERE UPPER(TRIM(schedule_name)) LIKE ?"; // * 대신 pk 컬럼 사용
        boolean exists = false;
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = dbcon();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + scheduleName.trim().toUpperCase() + "%");
            rs = ps.executeQuery();
            if (rs.next()) {
                exists = true;
            }
        } catch (Exception e) {
            System.err.println("existsSchedule(name) 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(conn, ps, rs);
        }
        return exists;
    }

    // ✅ 스케줄 존재 여부 확인 (name + start 일치 - SchedulesServlet에서 사용됨)
    public boolean existsSchedule(String scheduleName, Timestamp scheduleStart) {
        if (scheduleName == null || scheduleName.trim().isEmpty() || scheduleStart == null) {
            return false;
        }
        // [수정] TRIM(schedule_start) 제거 (TIMESTAMP 타입에는 TRIM 적용 불가)
        String sql = "SELECT schedule_id FROM schedules WHERE TRIM(schedule_name) = ? AND schedule_start = ?";
        boolean exists = false;
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = dbcon();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);
            ps.setString(1, scheduleName.trim());
            ps.setTimestamp(2, scheduleStart); // setTimestamp 사용
            rs = ps.executeQuery();
            if (rs.next()) {
                exists = true;
            }
        } catch (Exception e) {
            System.err.println("existsSchedule(name, start) 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(conn, ps, rs);
        }
        return exists;
    }

    // ✅ 스케줄 추가 (SchedulesServlet에서 사용됨)
    public boolean insertSchedule(Schedules s) {
        if (s == null || s.getScheduleName() == null || s.getScheduleName().trim().isEmpty())
            return false;

        String sql = "INSERT INTO schedules "
                   + "(platform_id, category_id, schedule_name, schedule_start, schedule_discount, "
                   + "schedule_price, schedule_img, schedule_url, schedule_deleteflg) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = dbcon();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);

            ps.setInt(1, s.getPlatformId());
            ps.setInt(2, s.getCategoryId());
            ps.setString(3, s.getScheduleName());
            ps.setTimestamp(4, s.getScheduleStart()); // setTimestamp
            ps.setString(5, s.getScheduleDiscount());
            // schedulePrice가 null일 수 있으므로 setObject 사용
            ps.setObject(6, s.getSchedulePrice(), java.sql.Types.DOUBLE);
            ps.setString(7, s.getScheduleImg());
            ps.setString(8, s.getScheduleUrl());
            ps.setInt(9, s.getScheduleDeleteFlg());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) { // ORA-00001: Unique constraint violation
                // 중복 로그는 Service에서 이미 찍으므로 여기선 생략 가능
                // System.out.println("[DB 중복] 스케줄 이미 존재: " + s.getScheduleName());
                return false; // 중복 시 false 반환 (정상 처리)
            }
            System.err.println("insertSchedule 오류: " + e.getMessage());
            e.printStackTrace();
            return false; // 그 외 SQL 오류
        } finally {
            close(conn, ps);
        }
    }

    // ✅ 카테고리 이름으로 ID 조회 (SchedulesServlet에서 사용됨)
    public int getCategoryIdByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) return -1; // 빈 이름 처리
        String sql = "SELECT category_id FROM categorys WHERE category_name = ?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        int categoryId = -1; // 못 찾을 경우 -1 반환
        try {
            conn = dbcon();
            if (conn == null) return -1;
             ps = conn.prepareStatement(sql);
            ps.setString(1, categoryName.trim()); // trim 추가
            rs = ps.executeQuery();
            if (rs.next()) {
                categoryId = rs.getInt("category_id");
            }
        } catch (SQLException e) {
            System.err.println("getCategoryIdByName 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(conn, ps, rs);
        }
        return categoryId;
    }

    // ✅ 특정 구간 스케줄 삭제 플래그 처리 (SchedulesServlet에서 사용됨)
    public int updateDeleteFlgByDateRange(int platformId, String minStartStr, String maxStartStr) {
        String sql = "UPDATE schedules SET schedule_deleteflg = 1 "
                   + "WHERE platform_id = ? AND schedule_start BETWEEN ? AND ?";
        Connection conn = null; PreparedStatement ps = null;
        int affected = 0;
        try {
            conn = dbcon();
            if (conn == null) return 0;
            ps = conn.prepareStatement(sql);

            // 문자열을 Timestamp로 변환 (ISO 형식 가정)
            Timestamp minStart = Timestamp.valueOf(LocalDateTime.parse(minStartStr));
            Timestamp maxStart = Timestamp.valueOf(LocalDateTime.parse(maxStartStr));

            ps.setInt(1, platformId);
            ps.setTimestamp(2, minStart);
            ps.setTimestamp(3, maxStart);

            affected = ps.executeUpdate();

        } catch (Exception e) { // DateTimeParseException 등 포함
            System.err.println("updateDeleteFlgByDateRange 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(conn, ps);
        }
        return affected;
    }

    // ✅ 기존 데이터 존재 여부 확인 (SchedulesServlet에서 사용됨)
    // platformId, name, start가 모두 일치하는지 확인
    public boolean existsSchedule(int platformId, String scheduleName, Timestamp scheduleStart) {
        // [수정] 파라미터 유효성 검사 강화
         if (platformId <= 0 || scheduleName == null || scheduleName.trim().isEmpty() || scheduleStart == null) {
            return false;
        }
        // [수정] TRIM(schedule_start) 제거
        String sql = "SELECT schedule_id FROM schedules WHERE platform_id = ? AND schedule_name = ? AND schedule_start = ?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        boolean exists = false;
        try {
            conn = dbcon();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);

            ps.setInt(1, platformId);
            ps.setString(2, scheduleName.trim()); // trim 추가
            ps.setTimestamp(3, scheduleStart);

            rs = ps.executeQuery();
            if (rs.next()) {
                exists = true;
            }
        } catch (Exception e) {
            System.err.println("existsSchedule(pid, name, start) 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(conn, ps, rs);
        }
        return exists;
    }

    // ✅ schedule_deleteflg = 0으로 업데이트 (SchedulesServlet에서 사용됨)
    public boolean updateDeleteFlgToActive(int platformId, String scheduleName, Timestamp scheduleStart) {
        // [수정] 파라미터 유효성 검사 강화
        if (platformId <= 0 || scheduleName == null || scheduleName.trim().isEmpty() || scheduleStart == null) {
           return false;
       }
        // [수정] TRIM(schedule_start) 제거
        String sql = "UPDATE schedules SET schedule_deleteflg = 0 WHERE platform_id = ? AND schedule_name = ? AND schedule_start = ?";
        Connection conn = null; PreparedStatement ps = null;
        boolean success = false;
        try {
            conn = dbcon();
            if (conn == null) return false;
            ps = conn.prepareStatement(sql);

            ps.setInt(1, platformId);
            ps.setString(2, scheduleName.trim()); // trim 추가
            ps.setTimestamp(3, scheduleStart);

            int affected = ps.executeUpdate();
            success = affected > 0; // 1개 이상 업데이트 시 성공
        } catch (Exception e) {
            System.err.println("updateDeleteFlgToActive 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(conn, ps);
        }
        return success;
    }

    // --- 👇 [수정됨] main.jsp 화면용 스케줄 조회 메서드 ---
    /**
     * 특정 날짜 및 카테고리의 스케줄 조회 (로그인 사용자 알림 신청 여부 포함)
     * @param date 조회할 날짜 (Timestamp)
     * @param categoryId 조회할 카테고리 ID (0 또는 음수면 전체 카테고리)
     * @param memberId 현재 로그인한 회원 ID (로그아웃 상태면 null)
     * @return 스케줄 정보 목록 (Map 형태, isSubscribed 포함)
     */
    public List<Map<String, Object>> selectSchedulesByDate(Timestamp date, int categoryId, Integer memberId) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql =
            "SELECT " +
            "    s.schedule_id, s.schedule_name, s.schedule_start, s.schedule_end, " +
            "    s.schedule_discount, s.schedule_price, s.schedule_img, s.schedule_url, " +
            "    s.platform_id, s.category_id, s.schedule_deleteflg, " +
            "    p.platform_name, " +
            // c.member_id가 존재하면(즉, 해당 memberId로 신청된 내역이 있으면) 1, 없으면 0 반환
            "    CASE WHEN c.member_id IS NOT NULL THEN 1 ELSE 0 END AS isSubscribed " +
            "FROM schedules s " +
            "JOIN platforms p ON s.platform_id = p.platform_id " +
            // LEFT JOIN: 로그인 안했거나(memberId=null) 신청 안했어도 방송은 보여야 함
            //            로그인 했다면(memberId != null) 해당 member_id로 조인 시도
            "LEFT JOIN mycart_schedules c ON s.schedule_id = c.schedule_id AND c.member_id = ? AND c.alert_enabled = 'Y' "; // [수정] alert_enabled='Y' 조건 추가

        // WHERE 절 시작
        sql += "WHERE s.schedule_deleteflg = 0 " + // 삭제 안된 것만
               "  AND TRUNC(s.schedule_start) = TRUNC(?) "; // 날짜 비교 (TRUNC 사용)

        // 카테고리 필터링 (categoryId가 0보다 클 때만 추가)
        if (categoryId > 0) {
            sql += "AND s.category_id = ? ";
        }

        sql += "ORDER BY s.schedule_start"; // 시간 순 정렬

        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = dbcon();
            if (conn == null) return list;
            ps = conn.prepareStatement(sql);

            // [수정] 파라미터 바인딩 순서 변경 및 setObject 사용
            int paramIndex = 1;
            // 1번째 ?: memberId (Integer 또는 null)
            ps.setObject(paramIndex++, memberId, java.sql.Types.INTEGER);
            // 2번째 ?: 조회할 날짜 (Timestamp)
            ps.setTimestamp(paramIndex++, date);
            // 3번째 ?: categoryId (categoryId > 0 일 때만)
            if (categoryId > 0) {
                ps.setInt(paramIndex++, categoryId);
            }

            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                row.put("scheduleId", rs.getInt("schedule_id"));
                row.put("scheduleName", rs.getString("schedule_name"));
                row.put("scheduleStart", rs.getTimestamp("schedule_start"));
                row.put("scheduleEnd", rs.getTimestamp("schedule_end")); // 타입 확인 필요
                row.put("scheduleDiscount", rs.getString("schedule_discount"));
                row.put("schedulePrice", rs.getObject("schedule_price") != null ? rs.getDouble("schedule_price") : null);
                row.put("scheduleImg", rs.getString("schedule_img"));
                row.put("scheduleUrl", rs.getString("schedule_url"));
                row.put("platformId", rs.getInt("platform_id"));
                row.put("categoryId", rs.getInt("category_id"));
                row.put("scheduleDeleteFlg", rs.getInt("schedule_deleteflg"));
                row.put("platformName", rs.getString("platform_name"));
                row.put("isSubscribed", rs.getInt("isSubscribed"));

                list.add(row);
            }

        } catch (Exception e) {
            System.err.println("selectSchedulesByDate 오류: " + e.getMessage());
            e.printStackTrace();
        } finally {
            close(conn, ps, rs);
        }
        System.out.println("조회된 스케줄 (" + date + ", cat:" + categoryId + ", mem:" + memberId + "): " + list.size() + "건"); // 로그 추가
        return list;
    }
    // --- 👆 [수정됨] ---
}