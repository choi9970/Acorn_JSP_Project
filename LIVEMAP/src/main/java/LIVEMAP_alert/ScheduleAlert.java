package LIVEMAP_alert;

public class ScheduleAlert {
	private int memberId;
    private int scheduleId;
    private String memberTel;
    private String scheduleName;
    
    public ScheduleAlert(){}

	public ScheduleAlert(int memberId, int scheduleId, String memberTel, String scheduleName) {
		super();
		this.memberId = memberId;
		this.scheduleId = scheduleId;
		this.memberTel = memberTel;
		this.scheduleName = scheduleName;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getMemberTel() {
		return memberTel;
	}

	public void setMemberTel(String memberTel) {
		this.memberTel = memberTel;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	@Override
	public String toString() {
		return "ScheduleAlert [memberId=" + memberId + ", scheduleId=" + scheduleId + ", memberTel=" + memberTel
				+ ", scheduleName=" + scheduleName + "]";
	}
}
