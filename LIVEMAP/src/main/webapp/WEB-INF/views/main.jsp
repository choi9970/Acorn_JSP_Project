<%@page import="LIVEMAP.Member"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Live Map</title>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <%
        // 세션에서 로그인 아이디 가져오기
        Integer memberId = null; 
        Object sessionMemberId = session.getAttribute("memberId");
        if (sessionMemberId instanceof Integer) {
            memberId = (Integer) sessionMemberId;
        }
        
        String memberNickname = (String) session.getAttribute("memberNickname");

        // 서블릿에서 넘겨준 선택된 날짜와 카테고리 ID 받기
        String currentSelectedDate = (String) request.getAttribute("selectedDate");
        Integer currentSelectedCategoryId = (Integer) request.getAttribute("selectedCategoryId");

        // null일 경우 기본값 처리
        if (currentSelectedDate == null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            currentSelectedDate = sdf.format(new java.util.Date()); // 오늘 날짜
        }
        if (currentSelectedCategoryId == null) {
            currentSelectedCategoryId = 1; // 기본 카테고리 ID (추천)
        }
    %>

    <%-- 헤더 --%>
    <header class="main-header">
        <div class="header-spacer"></div>
        <nav id="date-container" class="date-navigation"></nav>
        <div class="user-actions">
            <% if (memberId != null) { %>
                <a href="${pageContext.request.contextPath}/alertList">알림 목록</a>
                <a href="#">내 정보</a>
                <a href="${pageContext.request.contextPath}/logOut">로그아웃</a>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/login">로그인</a>
            <% } %>
        </div>
    </header>

    <%-- 카테고리 네비게이션 --%>
    <nav id="category-container" class="category-nav">
        <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=1" class="category-btn" data-category-id="1">뷰티</a>
        <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=2" class="category-btn" data-category-id="2">푸드</a>
        <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=3" class="category-btn" data-category-id="3">패션</a>
        <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=4" class="category-btn" data-category-id="4">라이프</a>
        <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=5" class="category-btn" data-category-id="5">키즈</a>
        <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=6" class="category-btn" data-category-id="6">테크</a>
        <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=7" class="category-btn" data-category-id="7">취미레저</a>
        <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=8" class="category-btn" data-category-id="8">여행/체험</a>
    </nav>

    <%-- 스케줄 목록 영역 --%>
    <div id="schedule-list-container" class="schedule-list-container"></div>

<script>
        // --- 서버 데이터 (서블릿에서 주입) ---
        const schedulesData = ${schedulesJson}; // 스케줄 목록 JSON
        const selectedDateFromServer = '${selectedDate}'; 
        const selectedCategoryIdFromServer = ${selectedCategoryId}; 
        
        // JSP에서 memberId를 JavaScript 변수로 가져옴 (로그인 확인용)
        const loggedInMemberId = <%= memberId %>; // 로그인 안 했으면 null이 됨

        document.addEventListener('DOMContentLoaded', function() {

            // --- 날짜 버튼 생성 ---
            function createDateButtons() {
                const container = document.getElementById('date-container');
                if (!container) return; 
                
                const today = new Date();
                const daysOfWeek = ['일', '월', '화', '수', '목', '금', '토'];
                
                for (let i = -2; i < 5; i++) {
                    const targetDate = new Date();
                    targetDate.setDate(today.getDate() + i);
                    
                    const dayOfMonth = targetDate.getDate();
                    const dayOfWeek = daysOfWeek[targetDate.getDay()];
                    
                    const yyyy = targetDate.getFullYear();
                    const month = targetDate.getMonth() + 1;
                    const mm = (month < 10 ? '0' : '') + month;
                    const day = targetDate.getDate();
                    const dd = (day < 10 ? '0' : '') + day;
                    const dateString = yyyy + '-' + mm + '-' + dd;
                    
                    const button = document.createElement('a'); 
                    button.href = '${pageContext.request.contextPath}/main.do?selectedDate=' + dateString + '&categoryId=' + selectedCategoryIdFromServer;
                    button.className = 'date_btn';
                    
                    if (dateString === selectedDateFromServer) {
                        button.classList.add('today'); 
                    }
                    
                    const dayOfWeekSpan = document.createElement('span');
                    dayOfWeekSpan.className = 'day-of-week';
                    dayOfWeekSpan.textContent = dayOfWeek;
                    
                    const dayOfMonthSpan = document.createElement('span');
                    dayOfMonthSpan.className = 'day-of-month';
                    dayOfMonthSpan.textContent = dayOfMonth;
                    
                    button.appendChild(dayOfWeekSpan);
                    button.appendChild(dayOfMonthSpan);
                    container.appendChild(button);
                }
            }

            // --- 스케줄 목록 생성 ---
            function createScheduleList(schedules){
                const container = document.getElementById('schedule-list-container');
                if (!container || !schedules) return;

                let listHtml = '';
                const platformMap = {
                    naver_live: "네이버 라이브",
                    youtube: "유튜브",
                    kakao_live: "카카오 라이브"
                };
                
                const now = new Date();

                schedules.forEach(item => {
                    const scheduleName = item.scheduleName || item.schedule_name;
                    const scheduleStart = item.scheduleStart || item.schedule_start;
                    const schedulePrice = item.schedulePrice || item.schedule_price;
                    const platformName = platformMap[item.platformName] || item.platformName;
                    const scheduleImg = item.scheduleImg || item.schedule_img;
                    const scheduleUrl = item.scheduleUrl || '#'; 
                    const isSubscribed = (item.isSubscribed === 1); 

                    let timeString = item.scheduleStart.replace(/\u202F|\u00A0/g, ' ');
                    const date = new Date(timeString);
                    
                    const timeStr = date.toLocaleTimeString('ko-KR', { 
                        hour: '2-digit', 
                        minute: '2-digit',
                        hour12: false 
                    });
                 	// 방송 시작 시간이 현재 시간보다 미래인지(> 0) 확인
                    const isButtonVisible = (date > now);

                    const priceString = (item.schedulePrice === 0 || item.schedulePrice === null)
                        ? '방송에서 공개'
                        : item.schedulePrice.toLocaleString('ko-KR') + '원';

                    listHtml += `
                        <div class="schedule-item">
                            <div class="schedule-time">\${timeStr}</div>
                            <div class="schedule-content">
                                <a href="\${scheduleUrl}" target="_blank">
                                    <div class="schedule-image-placeholder">
                                    <img src="${pageContext.request.contextPath}/images/\${scheduleImg}" alt="Schedule Image" />
                                    </div>
                                </a>
                                <div class="schedule-details">
                                    <span class="platform-name">\${platformName}</span>
                                    <a href="\${scheduleUrl}" target="_blank" class="platform-name"><h3 class="schedule-title">\${scheduleName}</h3></a>
                                    <span class="schedule-price">\${priceString}</span>
                    `;

                    if (isButtonVisible) {
                        listHtml += `
                            <button 
                                class="notification-btn \${isSubscribed ? 'active' : ''}" 
                                data-schedule-id="\${item.scheduleId}" 
                                onclick="toggleAlert(this)">
                                \${isSubscribed ? '알림 신청됨' : '알림 받기'}
                            </button>
                        `;
                    }
                    listHtml += `
                                </div>
                            </div>
                        </div>
                    `;
                });

                container.innerHTML = listHtml;
            }

            // --- 활성 카테고리 버튼 설정 ---
            function setActiveCategoryButton(){
                document.querySelectorAll('#category-container .category-btn').forEach(btn=>{
                    btn.classList.remove('active');
                    if(parseInt(btn.dataset.categoryId) === selectedCategoryIdFromServer) {
                        btn.classList.add('active');
                    }
                });
            }

            // --- 초기화 실행 ---
            createDateButtons();
            setActiveCategoryButton();

            const scheduleContainer = document.getElementById('schedule-list-container');
            if(schedulesData && schedulesData.length > 0){
                createScheduleList(schedulesData);
            } else {
                scheduleContainer.innerHTML = '<p style="text-align:center;margin-top:50px;color:#555;">해당 카테고리의 라이브가 없습니다.</p>';
            }
        });

     
     /**
      * 알림 토글(Toggle) 함수
      * 버튼 클릭 시 호출되며, 로그인 상태를 확인하고 서버에 토글 요청을 보냅니다.
      */
     function toggleAlert(button) {
         // 로그인 상태 확인 (페이지 상단에서 가져온 JSP 변수 사용)
         if (!loggedInMemberId) {
             alert('로그인이 필요합니다.');
             // 로그인 페이지로 이동시킬 수도 있습니다.
             // location.href = '${pageContext.request.contextPath}/login'; 
             return;
         }
         
         const scheduleId = button.dataset.scheduleId;
         if (!scheduleId) {
             alert('스케줄 정보를 찾을 수 없습니다.');
             return;
         }

         console.log("알림 토글 시도: scheduleId =", scheduleId);
         
         // 중복 클릭 방지를 위해 버튼 임시 비활성화
         button.disabled = true;

         // 서버에 Fetch API로 토글 요청
         fetch('${pageContext.request.contextPath}/alert/subscribe', { 
             method: 'POST', 
             headers: {
                 'Content-Type': 'application/x-www-form-urlencoded',
             },
             body: 'scheduleId=' + encodeURIComponent(scheduleId)
         })
         .then(response => {
             if (!response.ok) { 
                 throw new Error('서버 응답 오류: ' + response.status);
             }
             return response.json(); // 서버가 보낸 JSON 파싱
         })
         .then(data => { // data: {success: true, newAlertState: "Y"} 또는 {success: false, errorMessage: "..."}
             console.log("서버 응답 데이터:", data);
             
             if (data.success) {
                 // 4. 성공 시: newAlertState 값에 따라 버튼 상태 변경
                 if (data.newAlertState === 'Y') {
                     button.textContent = '알림 신청됨';
                     button.classList.add('active');
                 } else if (data.newAlertState === 'N') {
                     button.textContent = '알림 받기';
                     button.className = button.className.replace(/\bactive\b/g, '').trim();
                 }
             } else {
                 // 실패 시: 서버가 보낸 에러 메시지 표시
                 alert('알림 처리 실패: ' + (data.errorMessage || '알 수 없는 오류'));
             }
         })
         .catch(error => {
             console.error('알림 토글 중 오류 발생:', error);
             alert('요청 처리 중 문제가 발생했습니다. 콘솔 로그를 확인해주세요.');
         })
         .finally(() => {
             // 성공/실패 여부와 관계없이 버튼 다시 활성화
             button.disabled = false;
         });
     }
</script>

</body>
</html>