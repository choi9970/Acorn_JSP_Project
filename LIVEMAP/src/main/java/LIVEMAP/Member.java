package LIVEMAP;

import java.util.Date;

public class Member {
	private int memberId;       // 회원 번호 (PK)
    private String memberEmail;    // 이메일아이디
    private String memberPw;    // 비밀번호
    private String memberName;  // 이름
	private String memberTel;   // 전화번호
	private String memberSignup;
	private Date memberCreateddate;
	private Date memberUpdatedate;
	private char memberGender;
	private Date memberBirth;
	private String memberAddr1;
	private String memberAddr2;
	private String memberNickname;
	
	
	
	public Member(int memberId, String memberEmail, String memberPw, String memberName, String memberTel,
			String memberSignup, Date memberCreateddate, Date memberUpdatedate, char memberGender, Date memberBirth,
			String memberAddr1, String memberAddr2, String memberNickname) {
		super();
		this.memberId = memberId;
		this.memberEmail = memberEmail;
		this.memberPw = memberPw;
		this.memberName = memberName;
		this.memberTel = memberTel;
		this.memberSignup = memberSignup;
		this.memberCreateddate = memberCreateddate;
		this.memberUpdatedate = memberUpdatedate;
		this.memberGender = memberGender;
		this.memberBirth = memberBirth;
		this.memberAddr1 = memberAddr1;
		this.memberAddr2 = memberAddr2;
		this.memberNickname = memberNickname;
	}


	
	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}

	public String getMemberEmail() {
		return memberEmail;
	}

	public void setMemberEmail(String memberEmail) {
		this.memberEmail = memberEmail;
	}

	public String getMemberPw() {
		return memberPw;
	}

	public void setMemberPw(String memberPw) {
		this.memberPw = memberPw;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getMemberTel() {
		return memberTel;
	}

	public void setMemberTel(String memberTel) {
		this.memberTel = memberTel;
	}

	public String getMemberSignup() {
		return memberSignup;
	}

	public void setMemberSignup(String memberSignup) {
		this.memberSignup = memberSignup;
	}

	public Date getMemberCreateddate() {
		return memberCreateddate;
	}

	public void setMemberCreateddate(Date memberCreateddate) {
		this.memberCreateddate = memberCreateddate;
	}

	public Date getMemberUpdatedate() {
		return memberUpdatedate;
	}

	public void setMemberUpdatedate(Date memberUpdatedate) {
		this.memberUpdatedate = memberUpdatedate;
	}

	public char getMemberGender() {
		return memberGender;
	}

	public void setMemberGender(char memberGender) {
		this.memberGender = memberGender;
	}

	public Date getMemberBirth() {
		return memberBirth;
	}

	public void setMemberBirth(Date memberBirth) {
		this.memberBirth = memberBirth;
	}

	public String getMemberAddr1() {
		return memberAddr1;
	}

	public void setMemberAddr1(String memberAddr1) {
		this.memberAddr1 = memberAddr1;
	}

	public String getMemberAddr2() {
		return memberAddr2;
	}

	public void setMemberAddr2(String memberAddr2) {
		this.memberAddr2 = memberAddr2;
	}

	public String getMemberNickname() {
		return memberNickname;
	}

	public void setMemberNickname(String memberNickname) {
		this.memberNickname = memberNickname;
	}



	@Override
	public String toString() {
		return "Member [memberId=" + memberId + ", memberEmail=" + memberEmail + ", memberPw=" + memberPw
				+ ", memberName=" + memberName + ", memberTel=" + memberTel + ", memberSignup=" + memberSignup
				+ ", memberCreateddate=" + memberCreateddate + ", memberUpdatedate=" + memberUpdatedate
				+ ", memberGender=" + memberGender + ", memberBirth=" + memberBirth + ", memberAddr1=" + memberAddr1
				+ ", memberAddr2=" + memberAddr2 + ", memberNickname=" + memberNickname + "]";
	}
	
	
}
