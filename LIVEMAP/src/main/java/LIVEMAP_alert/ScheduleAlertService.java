package LIVEMAP_alert;

import java.util.List; 

public class ScheduleAlertService {
    
    private static ScheduleAlertService instance = new ScheduleAlertService();
    private ScheduleAlertDAO scheduleAlertDAO; 
    private KakaoAlertService kakaoService;

    private ScheduleAlertService() {
        scheduleAlertDAO = new ScheduleAlertDAO(); 
        kakaoService = new KakaoAlertService();
    }
    public static ScheduleAlertService getInstance() { return instance; }

    public String subscribe(int memberId, int scheduleId) {
        return scheduleAlertDAO.toggleSubscription(memberId, scheduleId);
    }

    public void processAlerts() {
        List<AlertTarget> targets5min = scheduleAlertDAO.findTargets("5min");
        sendAlerts(targets5min, "5min");
        List<AlertTarget> targets0min = scheduleAlertDAO.findTargets("0min");
        sendAlerts(targets0min, "0min");
    }
    
    
    /**
     * [수정됨] 실제 카카오 API 발송 및 DB 업데이트 - '나에게 보내기' 사용
     */
    private void sendAlerts(List<AlertTarget> targets, String type) {
        if (targets.isEmpty()) return; 
        System.out.println("[AlertService] " + type + " 알림 " + targets.size() + "건 발견.");

        String TEMPLATE_ID_5MIN = "125357";
        String TEMPLATE_ID_0MIN = "125358";

        for (AlertTarget target : targets) { 
            String templateId = (type.equals("5min")) ? TEMPLATE_ID_5MIN : TEMPLATE_ID_0MIN;
            // --- '나에게 보내기' 호출 로직 ---
            //String accessToken = target.getAccessToken(); 
            String accessToken = "61zwgS3yULGob9M0OYPHFXsG0wVeWp4SAAAAAQoNIpkAAAGaLvR8isTTXs9KIG_V";
            
            if (accessToken == null || accessToken.isEmpty()) {
                System.err.println("  -> 액세스 토큰 없음. 발송 불가: member_id=" + target.getMemberId());
                continue; 
            }

            // kakaoService.send() -> kakaoService.sendToMe() 호출
            boolean isSuccess = kakaoService.sendToMe( 
                accessToken,          
                templateId,
                target.getScheduleName()
            );
            // -----------------------------

            if (isSuccess) {
                scheduleAlertDAO.updateSentStatus(target.getMemberId(), target.getScheduleId(), type);
                System.out.println("  -> '나에게 보내기' 성공 및 DB 업데이트: member_id=" + target.getMemberId());
            } else {
                 System.err.println("  -> '나에게 보내기' 실패: member_id=" + target.getMemberId() + " (토큰 만료 가능성 높음)");
            }
        }
    }
    
    public int processAllDeletedAlertsManually() {
        List<AlertTarget> deletedTargets = scheduleAlertDAO.findDeletedAlertTargets();
        
        if (deletedTargets.isEmpty()) {
            System.out.println("삭제된 알림 대상 없음.");
            return 0;
        }
        
        int sentCount = 0;
        final String TEMPLATE_ID_CANCEL = "125395";

        // 테스트용 액세스 토큰 (직접 입력)
        final String TEST_ACCESS_TOKEN = "61zwgS3yULGob9M0OYPHFXsG0wVeWp4SAAAAAQoNIpkAAAGaLvR8isTTXs9KIG_V";

        for (AlertTarget target : deletedTargets) {
            try {
                if (TEST_ACCESS_TOKEN != null && !TEST_ACCESS_TOKEN.isEmpty()) {
                    boolean sendSuccess = kakaoService.sendToMe( 
                        TEST_ACCESS_TOKEN,          
                        TEMPLATE_ID_CANCEL,
                        target.getScheduleName()
                    );
                    if (sendSuccess) {
                        sentCount++;
                        System.out.println("-> 테스트용 나에게보내기 성공: " + target.getScheduleName());
                    } else {
                        System.err.println("-> 테스트용 발송 실패 (토큰 만료 가능성 있음)");
                    }
                } else {
                    System.err.println("-> 테스트용 액세스 토큰이 비어 있음");
                }
            } catch (Exception e) {
                System.err.println("-> 테스트용 발송 중 예외 발생: " + e.getMessage());
            } 
        }

        System.out.println("테스트 발송 완료. 성공 건수: " + sentCount);
        return sentCount;
    }
}