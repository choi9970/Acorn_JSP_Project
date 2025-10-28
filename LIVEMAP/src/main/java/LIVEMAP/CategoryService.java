package LIVEMAP;

import java.util.*;

public class CategoryService {
    private CategoryDAO dao = new CategoryDAO();

    public List<Category> getCategoryList() {
        return dao.getAllCategorys();
    }

    // ✅ 중복 체크 + 삽입 (DB 레벨에서 중복 방지)
    public boolean addCategory(String name) {
        if (name == null || name.trim().isEmpty()) return false;

        // existsCategory는 선택적 확인, DB 제약 조건으로도 안전
        if (dao.existsCategory(name)) {
            System.out.println("[중복] 이미 존재하는 카테고리: " + name);
            return false;
        }

        return dao.insertCategory(name);
    }
}
