package LIVEMAP;

import java.sql.Date;

/** 카카오에서 가져온 프로필 정보를 담는 DTO */
public class KakaoProfileData {
	public String email; // account_email
	public String nickname; // profile.nickname
	public String name; // name
	public String gender; // 'M' or 'F'
	public Date birth; // birthyear + birthday → YYYY-MM-DD
	public String phone; // 010-1234-5678 형태
	public String profileImage; // profile_image_url (원하면 members에 컬럼 추가하여 저장)
}
