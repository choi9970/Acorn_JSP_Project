package LIVEMAP;

import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class SchedulesDAO {

    String driver = "oracle.jdbc.driver.OracleDriver";
    String url = "jdbc:oracle:thin:@localhost:1521:testdb";
    String user = "scott";
    String password = "tiger";

    // ✅ DB 연결
    public Connection dbcon() {
        try {
            Class.forName(driver);
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("DB 연결 ok: " + con.getMetaData().getURL());
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

 // ✅ 모든 스케줄 조회 (전체조회)
    public List<Schedules> getAllSchedules() {
        List<Schedules> list = new ArrayList<>();
        String sql = "SELECT * FROM schedules ORDER BY schedule_start";

        try (
            Connection conn = dbcon();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                Schedules s = new Schedules();

                // scheduleId (PK)
                s.setScheduleId(rs.getInt("schedule_id"));

                // 문자열 필드들
                s.setScheduleName(rs.getString("schedule_name"));
                //s.setScheduleStart(rs.getString("schedule_start"));
                s.setScheduleStart(rs.getTimestamp("schedule_start"));
                s.setScheduleEnd(rs.getString("schedule_end"));
                s.setScheduleDiscount(rs.getString("schedule_discount"));
                s.setScheduleImg(rs.getString("schedule_img"));
                s.setScheduleUrl(rs.getString("schedule_url"));

                // 숫자형 필드들
                s.setPlatformId(rs.getInt("platform_id"));
                s.setCategoryId(rs.getInt("category_id"));
                s.setScheduleDeleteFlg(rs.getInt("schedule_deleteflg"));

                // Double 처리 (널 또는 문자열일 수 있음)
                String priceStr = rs.getString("schedule_price");
                if (priceStr == null || priceStr.trim().isEmpty()) {
                    s.setSchedulePrice(null);
                } else {
                    try {
                        s.setSchedulePrice(Double.parseDouble(priceStr));
                    } catch (NumberFormatException e) {
                        s.setSchedulePrice(null);
                    }
                }

                list.add(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // ✅ 스케줄 존재 여부 확인 (부분 일치, 대소문자 무시)// 검색
    public boolean existsSchedule(String scheduleName) {
        // if (scheduleName == null || scheduleName.trim().isEmpty()) return false;
        String sql = "SELECT * FROM schedules WHERE UPPER(TRIM(schedule_name)) LIKE ?";
        boolean exists = false;

        try (
            Connection conn = dbcon();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, "%" + scheduleName.trim().toUpperCase() + "%"); // 부분 일치
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    exists = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exists;
    }

    // ✅ 스케줄 존재 여부 확인 (schedule_name + schedule_start 전체 일치)
    public boolean existsSchedule(String scheduleName, Timestamp scheduleStart) {
    	if (scheduleName == null || scheduleName.trim().isEmpty()
    	        || scheduleStart == null) {
    	    return false;
    	}

        String sql = "SELECT * FROM schedules WHERE TRIM(schedule_name) = ? AND TRIM(schedule_start) = ?";
        boolean exists = false;

        try (
            Connection conn = dbcon();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, scheduleName.trim());
            ps.setTimestamp(2, scheduleStart);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    exists = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exists;
    }

    // ✅ 스케줄 추가
    public boolean insertSchedule(Schedules s) {
    	SchedulesDAO dao=new SchedulesDAO();
        if (s == null || s.getScheduleName() == null || s.getScheduleName().trim().isEmpty())
            return false;

        String sql = "INSERT INTO schedules "
                   + "(platform_id, category_id, schedule_name, schedule_start, schedule_discount, "
                   + "schedule_price, schedule_img, schedule_url, schedule_deleteflg) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
            Connection conn = dbcon();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setInt(1, s.getPlatformId());
            ps.setInt(2, s.getCategoryId());
            ps.setString(3, s.getScheduleName());
            
            
            ps.setTimestamp(4, s.getScheduleStart());
            
            ps.setString(5, s.getScheduleDiscount());
            ps.setObject(6, s.getSchedulePrice(), java.sql.Types.DOUBLE);
            ps.setString(7, s.getScheduleImg());
            ps.setString(8, s.getScheduleUrl());
            ps.setInt(9, s.getScheduleDeleteFlg());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            // UNIQUE 제약 조건 위반 시 중복 처리
            if (e.getErrorCode() == 1) { // ORA-00001
                System.out.println("[중복] 스케줄 이미 존재: " + s.getScheduleName());
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    // ✅ scheduleDeleteFlg 업데이트
//    public boolean updateScheduleDeleteFlg(String scheduleName, String scheduleStart, int deleteFlg) {
//        String sql = "UPDATE schedules "
//                   + "SET schedule_deleteflg = ? "
//                   + "WHERE schedule_name = ? AND schedule_start = ?";
//
//        try (
//            Connection conn = dbcon();
//            PreparedStatement ps = conn.prepareStatement(sql);
//        ) {
//            ps.setInt(1, deleteFlg);
//            ps.setString(2, scheduleName);
//            ps.setString(3, scheduleStart);
//
//            int affected = ps.executeUpdate();
//            return affected > 0;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
    //getCategoryId
    public int getCategoryIdByName(String categoryName) {
        String sql = "SELECT category_id FROM categorys WHERE category_name = ?";
        try (Connection conn = dbcon();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoryName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 없으면 -1 반환 (에러 처리)
    }
 // ✅ 특정 구간(min~max) 안의 스케줄을 삭제 플래그 처리
    public int updateDeleteFlgByDateRange(int platformId, String minStart, String maxStart) {
        String sql = "UPDATE schedules "
                   + "SET schedule_deleteflg = 1 "
                   + "WHERE platform_id = ? "
                   + "AND schedule_start BETWEEN ? AND ?";

        try (
            Connection conn = dbcon();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setInt(1, platformId);
            // ✅ 문자열을 Timestamp로 변환
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(minStart, formatter);
            LocalDateTime end = LocalDateTime.parse(maxStart, formatter);

            ps.setTimestamp(2, Timestamp.valueOf(start));
            ps.setTimestamp(3, Timestamp.valueOf(end));

            int affected = ps.executeUpdate();
            return affected;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    // ✅ 기존 데이터 존재 여부 확인
    public boolean existsSchedule(int platformId, String scheduleName, Timestamp scheduleStart1) {
        String sql = "SELECT COUNT(*) FROM schedules WHERE platform_id = ? AND schedule_name = ? AND schedule_start = ?";
        try (Connection conn = dbcon();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, platformId);
            ps.setString(2, scheduleName);
            ps.setTimestamp(3, scheduleStart1);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
 // ✅ schedule_deleteflg = 0으로 업데이트
    public boolean updateDeleteFlgToActive(int platformId, String scheduleName, Timestamp scheduleStart1) {
        String sql = "UPDATE schedules SET schedule_deleteflg = 0 WHERE platform_id = ? AND schedule_name = ? AND schedule_start = ?";
        try (Connection conn = dbcon();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, platformId);
            ps.setString(2, scheduleName);
            ps.setTimestamp(3, scheduleStart1);

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
 // 특정 날짜 및 카테고리의 스케줄 조회
    public List<Map<String, Object>> selectSchedulesByDate(Timestamp date, int categoryId) {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = 
        	    "SELECT " +
        	    "    s.schedule_id, " +
        	    "    s.schedule_name, " +
        	    "    s.schedule_start, " +
        	    "    s.schedule_end, " +
        	    "    s.schedule_discount, " +
        	    "    s.schedule_price, " +
        	    "    s.schedule_img, " +
        	    "    s.schedule_url, " +
        	    "    s.platform_id, " +
        	    "    s.category_id, " +
        	    "    s.schedule_deleteflg, " +
        	    "    p.platform_name " +
        	    "FROM schedules s " +
        	    "JOIN platforms p ON s.platform_id = p.platform_id " +
        	    "WHERE s.schedule_deleteflg = 0 " +
        	    "  AND TO_CHAR(s.schedule_start, 'YYYY-MM-DD') = ?";


        if (categoryId > 0) {
            sql += "AND s.category_id = ? ";
        }

        sql += "ORDER BY s.schedule_start";

        try (Connection conn = dbcon();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            ps.setString(1, sdf.format(date));

            if (categoryId > 0) {
                ps.setInt(2, categoryId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    row.put("scheduleId", rs.getInt("schedule_id"));
                    row.put("scheduleName", rs.getString("schedule_name"));
                    row.put("scheduleStart", rs.getTimestamp("schedule_start"));
                    row.put("scheduleEnd", rs.getTimestamp("schedule_end"));
                    row.put("scheduleDiscount", rs.getString("schedule_discount"));

                    Object priceObj = rs.getObject("schedule_price");
                    row.put("schedulePrice", priceObj != null ? rs.getDouble("schedule_price") : null);

                    row.put("scheduleImg", rs.getString("schedule_img"));
                    row.put("scheduleUrl", rs.getString("schedule_url"));

                    row.put("platformId", rs.getInt("platform_id"));
                    row.put("categoryId", rs.getInt("category_id"));
                    row.put("scheduleDeleteFlg", rs.getInt("schedule_deleteflg"));

                    row.put("platformName", rs.getString("platform_name"));

                    list.add(row);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
