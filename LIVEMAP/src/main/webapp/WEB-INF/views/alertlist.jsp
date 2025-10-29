<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %> <%-- List, Map 사용 --%>
<%@ page import="java.text.SimpleDateFormat" %> <%-- 날짜 포맷 --%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>알림 목록</title>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css"> <%-- main.jsp와 동일한 CSS 사용 --%>
<style>
    /* 페이지 제목 스타일 */
    .page-title {
        text-align: center;
        margin-top: 30px;
        margin-bottom: 20px;
        font-size: 24px;
        font-weight: bold;
    }
    /* 알림 취소 버튼 스타일 (예시) */
    .cancel-alert-btn {
        background-color: #f8d7da; /* 연한 빨강 배경 */
        border: 1px solid #f5c6cb;
        color: #721c24; /* 어두운 빨강 글씨 */
        border-radius: 5px;
        padding: 8px 12px;
        font-weight: bold;
        cursor: pointer;
        margin-top: 10px;
        width: fit-content;
    }
</style>
</head>
<body>

    <%-- 헤더 (main.jsp와 동일하게 가져오거나, 별도 파일로 분리 추천) --%>
    <%
        // 세션에서 로그인 정보 가져오기 (main.jsp와 동일)
        Integer memberId = null;
        Object sessionMemberId = session.getAttribute("memberId");
        if (sessionMemberId instanceof Integer) {
            memberId = (Integer) sessionMemberId;
        }
        String memberNickname = (String) session.getAttribute("memberNickname");

        // 서블릿에서 전달받은 알림 목록 데이터
        List<Map<String, Object>> alertList = (List<Map<String, Object>>) request.getAttribute("alertList");
    %>
    <header class="main-header">
        <div class="header-spacer"><a href="${pageContext.request.contextPath}/main.do" style="text-decoration:none; color:inherit; font-weight:bold; font-size: 20px;">Live Map</a></div>
        <nav id="date-container" class="date-navigation"> <%-- 날짜 네비는 필요 없을 수 있음 --%> </nav>
        <div class="user-actions">
            <% if (memberId != null) { %>
                <a href="${pageContext.request.contextPath}/alertList">알림 목록</a> <%-- 현재 페이지 링크 --%>
                <a href="#">내 정보</a>
                <a href="${pageContext.request.contextPath}/logOut">로그아웃</a>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/login">로그인</a>
            <% } %>
        </div>
    </header>

    <h2 class="page-title">신청한 알림 목록</h2>

    <%-- 알림 목록 표시 영역 --%>
    <div id="alert-list-container" class="schedule-list-container"> <%-- main.jsp와 동일한 CSS 클래스 사용 --%>
        <% if (alertList != null && !alertList.isEmpty()) { %>
            <% for (Map<String, Object> item : alertList) { %>
                <%
                    // 데이터 추출 및 포맷팅 (main.jsp JavaScript 로직과 유사하게)
                    int scheduleId = (Integer) item.getOrDefault("scheduleId", 0);
                    String scheduleName = (String) item.getOrDefault("scheduleName", "제목 없음");
                    java.sql.Timestamp scheduleStart = (java.sql.Timestamp) item.get("scheduleStart");
                    Double schedulePrice = (Double) item.get("schedulePrice"); // Service에서 null 처리됨
                    String platformName = (String) item.getOrDefault("platformName", "플랫폼 정보 없음");
                    String scheduleImg = (String) item.get("scheduleImg");
                    String scheduleUrl = (String) item.getOrDefault("scheduleUrl", "#");

                    // 시간 포맷팅 (HH:MM)
                    String timeStr = "시간정보없음";
                    if (scheduleStart != null) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        timeStr = timeFormat.format(scheduleStart);
                    }

                    // 가격 포맷팅
                    String priceString = (schedulePrice == 0 || schedulePrice == null)
                                        ? "방송에서 공개"
                                        : String.format("%,.0f원", schedulePrice); // 소수점 없이 콤마
					
                    String encodedImgUrl = "";
                    if (scheduleImg != null && !scheduleImg.isEmpty()) {
					    encodedImgUrl = java.net.URLEncoder.encode(scheduleImg, "UTF-8");
                        encodedImgUrl = encodedImgUrl.replace("+", "%20");
                    }
                %>
                <div class="schedule-item">
                    <div class="schedule-time"><%= timeStr %></div>
                    <div class="schedule-content">
                        <a href="<%= scheduleUrl %>" target="_blank" rel="noopener noreferrer">
                            <div class="schedule-image-placeholder">
                                 <img src="${pageContext.request.contextPath}/images/<%= encodedImgUrl %>" alt="<%= scheduleName %> 이미지" />
                            </div>
                        </a>
                        <div class="schedule-details">
                            <span class="platform-name"><%= platformName %></span>
                            <a href="<%= scheduleUrl %>" target="_blank" rel="noopener noreferrer" style="text-decoration: none; color: inherit;">
                                <h3 class="schedule-title"><%= scheduleName %></h3>
                            </a>
                            <span class="schedule-price"><%= priceString %></span>
                            <%-- TODO: 알림 취소 기능 구현 필요 --%>
                            <button class="cancel-alert-btn" data-schedule-id="<%= scheduleId %>" onclick="cancelAlert(this)">알림 취소</button>
                        </div>
                    </div>
                </div>
            <% } %> <%-- end of for loop --%>
        <% } else { %>
            <p style="text-align:center; margin-top:50px; color:#555;">신청한 알림이 없습니다.</p>
        <% } %>
    </div>

<script>
    // 알림 취소 버튼 클릭 시 실행될 함수 (실제 취소 로직은 별도 서블릿 필요)
    function cancelAlert(button) {
        const scheduleId = button.dataset.scheduleId; // 버튼에서 scheduleId 가져오기
        if (!scheduleId) {
            alert('스케줄 정보를 찾을 수 없습니다.');
            return;
        }

        // 사용자에게 취소 여부 확인
        if (confirm("해당 방송 알림을 취소하시겠습니까?")) {
            console.log("알림 취소 시도: scheduleId =", scheduleId);

            // --- 서버로 취소 요청 보내기 (POST 방식) ---
            fetch('${pageContext.request.contextPath}/alert/cancel', { // ★★★ 취소 서블릿 경로 ★★★
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'scheduleId=' + encodeURIComponent(scheduleId) // 취소할 scheduleId 전송
            })
            .then(response => {
                console.log("서버 응답 상태:", response.status);
                if (!response.ok) {
                    throw new Error('서버 응답 오류: ' + response.status);
                }
                return response.json(); // 응답을 JSON으로 파싱
            })
            .then(data => { // JSON 파싱 성공 시
                console.log("서버 응답 데이터:", data);
                if (data.success) { // 성공 시
                    alert('알림 신청이 취소되었습니다.');
                    // 화면에서 해당 항목 제거 (가장 가까운 .schedule-item 부모 요소를 찾아서 삭제)
                    button.closest('.schedule-item').remove();

                    // 만약 목록이 비었으면 안내 메시지 표시 (선택 사항)
                    const container = document.getElementById('alert-list-container');
                    if (container && container.children.length === 0) {
                        container.innerHTML = '<p style="text-align:center; margin-top:50px; color:#555;">신청한 알림이 없습니다.</p>';
                    }
                } else { // 실패 시 (로그인 필요 등)
                    alert('알림 취소 실패: ' + (data.message || '알 수 없는 오류'));
                }
            })
            .catch(error => { // 네트워크 오류 등
                console.error('알림 취소 중 오류 발생:', error);
                alert('요청 처리 중 문제가 발생했습니다. 콘솔 로그를 확인해주세요.');
            });
            // --- fetch 요청 끝 ---
        } else {
            // 사용자가 "취소"를 눌렀을 때
            console.log("알림 취소 작업 취소됨.");
        }
    }

</script>

</body>
</html>