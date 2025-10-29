package LIVEMAP;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SchedulesService {

    private SchedulesDAO dao = new SchedulesDAO();

    // ✅ 전체 스케줄 조회
    public List<Schedules> getScheduleList() {
        return dao.getAllSchedules();
    }

    // ✅ 중복 체크 + 삽입 (schedule_name + schedule_start 기준)
    public boolean addSchedule(Schedules schedule) {
    	if (schedule == null
    	        || schedule.getScheduleName() == null || schedule.getScheduleName().trim().isEmpty()
    	        || schedule.getScheduleStart() == null) {
    	    return false;
    	}

        // 중복 체크
        if (dao.existsSchedule(schedule.getScheduleName(), schedule.getScheduleStart())) {
            System.out.println("[중복] 이미 존재하는 스케줄: " + schedule.getScheduleName() 
                + " / " + schedule.getScheduleStart());
            return false;
        }

        // 삽입
        return dao.insertSchedule(schedule);
    }

	public int getCategoryIdByName(String categoryName) {
		int CategoryId = dao.getCategoryIdByName(categoryName);
		return CategoryId;
	}
    // ✅ 구간별 삭제 플래그 처리
    public int deleteSchedulesInRange(int platformId, Map<String, LocalDateTime> range) {
        if (range.get("min") == null || range.get("max") == null) {
            return 0;
        }
        String minStr = range.get("min").toString();
        String maxStr = range.get("max").toString();

        return dao.updateDeleteFlgByDateRange(platformId, minStr, maxStr);
    }
    public boolean existsSchedule(int platformId, String scheduleName, Timestamp scheduleStart1) {
        return dao.existsSchedule(platformId, scheduleName, scheduleStart1);
    }

    public boolean updateDeleteFlgToActive(int platformId, String scheduleName, Timestamp scheduleStart1) {
        return dao.updateDeleteFlgToActive(platformId, scheduleName, scheduleStart1);
    }
 // 비즈니스 로직 처리 가능 (예: 가격 필터, 시간 변환 등)
    public List<Map<String, Object>> getSchedulesByDateAndCategory(Timestamp date, int categoryId, Integer memberId) {
        List<Map<String, Object>> schedules = dao.selectSchedulesByDate(date, categoryId, memberId);

        // 필요시 추가 비즈니스 로직 처리 가능
        // 예: 가격 null 처리, 이미지 URL 기본값 처리 등
        for (Map<String, Object> s : schedules) {
            if (s.get("schedulePrice") == null) {
                s.put("schedulePrice", 0.0);
            }
        }

        return schedules;
    }


    

}