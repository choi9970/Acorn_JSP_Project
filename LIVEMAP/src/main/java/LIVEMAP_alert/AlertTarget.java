package LIVEMAP_alert;

public class AlertTarget {
    
    private int memberId;
    private int scheduleId;
    private String memberTel;
    private String scheduleName;
    private String accessToken;
    // private String scheduleImg; // <--- 제거

    public AlertTarget() {}

    // [최종 복구] 생성자에서 scheduleImg 파라미터 제거 (총 5개 파라미터)
    public AlertTarget(int memberId, int scheduleId, String memberTel, String scheduleName, String accessToken) {
        this.memberId = memberId;
        this.scheduleId = scheduleId;
        this.memberTel = memberTel;
        this.scheduleName = scheduleName;
        this.accessToken = accessToken;
    }
    
    // ... (Getter/Setter에서 scheduleImg 관련 코드 제거) ...

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }
    public String getMemberTel() { return memberTel; }
    public void setMemberTel(String memberTel) { this.memberTel = memberTel; }
    public String getScheduleName() { return scheduleName; }
    public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }
    public String getAccessToken() { return accessToken; } 
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    @Override
    public String toString() { 
        return "AlertTarget [memberId=" + memberId + ", scheduleId=" + scheduleId + ", memberTel=" + memberTel
                + ", scheduleName=" + scheduleName + ", accessToken=" + accessToken + "]";
    }
}