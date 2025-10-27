package LIVEMAP;



/**
 * 카카오 사용자 → members 레코드 확보 → kakao_login upsert → members 빈 칸 채우기 -
 * providerUserId로 기존 연결 우선 확인 - email이 있으면 기존 회원 재사용, 없으면 신규 생성 - DB 세부처리는
 * MemberDAO / KakaoLoginDAO에 위임
 */
public class MemberService {

	private final MemberDAO memberDAO = new MemberDAO();
	private final KakaoLoginDAO kakaoDAO = new KakaoLoginDAO();
	
	//일반 로그인용
	public boolean loginJudge(String email, String pw) {
        return memberDAO.loginJudge(email, pw) > 0;
    }
    public Member getfindById(String email) {
        return memberDAO.findByEmail(email);
    }
	
	
	

	// 호환 유지
	public void ensureKakaoLink(String providerUserId, String nickname, String email, String accessToken,
			String refreshToken) {
		KakaoProfileData d = new KakaoProfileData();
		d.nickname = nickname;
		d.email = email;
		ensureKakaoLinkWithProfile(providerUserId, d, accessToken, refreshToken);
	}

	// 신규: 프로필 동시 채움
	public void ensureKakaoLinkWithProfile(String providerUserId, KakaoProfileData data, String accessToken,
			String refreshToken) {
		try {
			Integer memberId = kakaoDAO.findMemberIdByKakaoId(providerUserId);

			// 1) 이메일로 기존 회원 찾기
			if (memberId == null && data != null && data.email != null && !data.email.isEmpty()) {
				Member existed = memberDAO.findByEmail(data.email);
				if (existed != null)
					memberId = existed.getMemberId();
			}

			// 2) 없으면 신규 members 생성
			if (memberId == null) {
				String nick = (data != null && data.nickname != null) ? data.nickname : "kakao_user";
				String email = (data != null) ? data.email : null;
				Member created = memberDAO.insertKakaoMember(email, nick);
				memberId = created.getMemberId();
			}

			// 3) kakao_login upsert (토큰/연결 갱신)
			kakaoDAO.upsert(providerUserId, memberId, accessToken, refreshToken);

			// 4) members 빈 칸을 카카오 정보로 채움
			memberDAO.updateFromKakaoProfile(memberId, data);

		} catch (Exception e) {
			System.err.println(
					"[SERVICE][ensureKakaoLinkWithProfile] " + e.getClass().getSimpleName() + " : " + e.getMessage());
		}
	}
}
