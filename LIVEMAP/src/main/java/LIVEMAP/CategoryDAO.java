package LIVEMAP;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class CategoryDAO {

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

    // ✅ 카테고리 전체 조회
    public List<Category> getAllCategorys() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categorys ORDER BY category_id";

        try (
            Connection conn = dbcon();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                Category c = new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                );
                list.add(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ✅ 카테고리 존재 여부 확인
    public boolean existsCategory(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String trimmedName = name.trim();
        String sql = "SELECT * FROM categorys WHERE TRIM(category_name) = ?";
        boolean exists = false;

        try (
            Connection conn = dbcon();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, trimmedName);
            try (ResultSet rs = ps.executeQuery()) {
            	if (rs.next()) {
            		exists = true; // 결과가 있으면(로그인 성공) 1을 반환
    	        }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    // ✅ 카테고리 추가 (중복 발생 시 false 반환)
    public boolean insertCategory(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String sql = "INSERT INTO categorys (category_name) VALUES (?)";

        try (
            Connection conn = dbcon();
            PreparedStatement ps = conn.prepareStatement(sql);
        ) {
            ps.setString(1, name.trim());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // UNIQUE 제약 조건 위반 시 중복으로 처리
            if (e.getErrorCode() == 1) { // ORA-00001: unique constraint violated
                System.out.println("[중복] 카테고리 이미 존재: " + name);
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }
}
