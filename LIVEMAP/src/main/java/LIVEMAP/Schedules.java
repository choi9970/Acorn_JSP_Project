package LIVEMAP;

import java.sql.Timestamp;

public class Schedules {
    private int scheduleId;         // DB PK, auto-generated
    private String scheduleName;
    private Timestamp scheduleStart;   // ISO 8601 문자열, TIMESTAMP로 변환 가능
    private String scheduleEnd;     // 필요 시
    private String scheduleDiscount;
    private Double schedulePrice;   // JSON이 빈 문자열일 경우 null 처리
    private String scheduleImg;
    private String scheduleUrl;
    private int platformId;
    private int categoryId;         // category_name → category_id 매핑 필요
    private int scheduleDeleteFlg;

    public Schedules(int scheduleId, String scheduleName, Timestamp scheduleStart, String scheduleEnd,
                     String scheduleDiscount, Double schedulePrice, String scheduleImg, String scheduleUrl,
                     int platformId, int categoryId, int scheduleDeleteFlg) {
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.scheduleDiscount = scheduleDiscount;
        this.schedulePrice = schedulePrice;
        this.scheduleImg = scheduleImg;
        this.scheduleUrl = scheduleUrl;
        this.platformId = platformId;
        this.categoryId = categoryId;
        this.scheduleDeleteFlg = scheduleDeleteFlg;
    }

    public Schedules() {
		// TODO Auto-generated constructor stub
	}

	// Getters & Setters
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }

    public Timestamp getScheduleStart() { return scheduleStart; }
    public void setScheduleStart(Timestamp string) { this.scheduleStart = string; }

    public String getScheduleEnd() { return scheduleEnd; }
    public void setScheduleEnd(String scheduleEnd) { this.scheduleEnd = scheduleEnd; }

    public String getScheduleDiscount() { return scheduleDiscount; }
    public void setScheduleDiscount(String scheduleDiscount) { this.scheduleDiscount = scheduleDiscount; }

    public Double getSchedulePrice() { return schedulePrice; }
    public void setSchedulePrice(Double schedulePrice) { this.schedulePrice = schedulePrice; }

    public String getScheduleImg() { return scheduleImg; }
    public void setScheduleImg(String scheduleImg) { this.scheduleImg = scheduleImg; }

    public String getScheduleUrl() { return scheduleUrl; }
    public void setScheduleUrl(String scheduleUrl) { this.scheduleUrl = scheduleUrl; }

    public int getPlatformId() { return platformId; }
    public void setPlatformId(int platformId) { this.platformId = platformId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getScheduleDeleteFlg() { return scheduleDeleteFlg; }
    public void setScheduleDeleteFlg(int scheduleDeleteFlg) { this.scheduleDeleteFlg = scheduleDeleteFlg; }

    @Override
    public String toString() {
        return "Schedules [scheduleId=" + scheduleId + ", scheduleName=" + scheduleName +
               ", scheduleStart=" + scheduleStart + ", scheduleEnd=" + scheduleEnd +
               ", scheduleDiscount=" + scheduleDiscount + ", schedulePrice=" + schedulePrice +
               ", scheduleImg=" + scheduleImg + ", scheduleUrl=" + scheduleUrl +
               ", platformId=" + platformId + ", categoryId=" + categoryId +
               ", scheduleDeleteFlg=" + scheduleDeleteFlg + "]";
    }
}
