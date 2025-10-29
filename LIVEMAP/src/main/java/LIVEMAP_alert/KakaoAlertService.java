package LIVEMAP_alert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class KakaoAlertService {

    /**
     * [수정됨] '나에게 보내기' (사용자 정의 템플릿) API 호출 메소드
     */
    public boolean sendToMe(String accessToken, String templateId, String scheduleName) {
        String apiUrl = "https://kapi.kakao.com/v2/api/talk/memo/send"; 
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true); 
            conn.setRequestProperty("Authorization", "Bearer " + accessToken); // Bearer 토큰 사용
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

            String templateArgsJson = String.format("{\"schedule_name\": \"%s\"}", scheduleName); // 실제 템플릿 키 확인!
            String params = String.format(
                "template_id=%s&template_args=%s",
                templateId, 
                URLEncoder.encode(templateArgsJson, StandardCharsets.UTF_8.toString())
            );

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = params.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = conn.getResponseCode();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream(), 
                    StandardCharsets.UTF_8))) {
                String line;
                StringBuilder responseBody = new StringBuilder();
                while ((line = br.readLine()) != null) { responseBody.append(line); }
                System.out.println("카카오 '나에게 보내기' 응답 ("+responseCode+"): " + responseBody.toString());
            }
            return responseCode == 200; 

        } catch (Exception e) {
            System.err.println("카카오 API 호출 오류: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // 기존 send() 메소드는 삭제
}