<%@page import="LIVEMAP.Member"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Live Map</title>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css?123">
</head>
<body>

    <%
        // 세션에서 로그인 아이디 가져오기
        String id = (String) session.getAttribute("memberId");

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
            <a href="#">알림 목록</a> <a href="#">내 정보</a>
            <% if (id != null) { %>
                <a href="/LIVEMAP/logOut">로그아웃</a>
            <% } else { %>
                <a href="/LIVEMAP/login">로그인</a>
            <% } %>
        </div>
    </header>

    <%-- 카테고리 네비게이션 --%>
    <nav id="category-container" class="category-nav">
    	<!-- <a href="${pageContext.request.contextPath}/main.do?selectedDate=<%= currentSelectedDate %>&categoryId=1" class="category-btn" data-category-id="re">추천</a> -->
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
//--- 데이터 수신 ---
        // --- 데이터 수신 ---
        const schedulesData = ${schedulesJson}; // 스케줄 목록 JSON
        const selectedDateFromServer = '${selectedDate}'; // 선택된 날짜 문자열
        const selectedCategoryIdFromServer = ${selectedCategoryId}; // 선택된 카테고리 ID

        document.addEventListener('DOMContentLoaded', function() {

            // --- 날짜 버튼 생성 함수 ---
            function createDateButtons() {
                const container = document.getElementById('date-container');
                if (!container) return; 
                
                const today = new Date();
                const daysOfWeek = ['일', '월', '화', '수', '목', '금', '토'];
                
                for (let i = -2; i < 5; i++) {
                    const targetDate = new Date();
                    targetDate.setDate(today.getDate() + i);
                    
                 	// 날짜 정보 추출
                    const dayOfMonth = targetDate.getDate();
                    const dayOfWeek = daysOfWeek[targetDate.getDay()];
                    
                 	// 'yyyy-MM-dd' 형식의 문자열 생성
                    const yyyy = targetDate.getFullYear();
                    const month = targetDate.getMonth() + 1;
                    const mm = (month < 10 ? '0' : '') + month;
                    const day = targetDate.getDate();
                    const dd = (day < 10 ? '0' : '') + day;
                    const dateString = yyyy + '-' + mm + '-' + dd;
                    
                 	// <a> 태그(버튼) 생성
                    const button = document.createElement('a'); 
                    
                    // 링크에 날짜와 현재 카테고리 ID 포함
                    button.href = '${pageContext.request.contextPath}/main.do?selectedDate=' + dateString + '&categoryId=' + selectedCategoryIdFromServer;
                    button.className = 'date_btn';
                    
                 	// 선택된 날짜 강조
                    if (dateString === selectedDateFromServer) {
                        button.classList.add('today'); 
                    }
                 	
                 	// 버튼 내부에 요일과 날짜 <span> 추가
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


        function createScheduleList(schedules){
            const container = document.getElementById('schedule-list-container');
            if (!container || !schedules) return;

            let listHtml='';
            const platformMap = {
            	    naver_live: "네이버 라이브",
            	    youtube: "유튜브",
            	    kakao_live: "카카오 라이브"
            	};

            	schedules.forEach(item => {
            	    console.log(item);
            	    const scheduleName = item.scheduleName || item.schedule_name;
            	    const scheduleStart = item.scheduleStart || item.schedule_start;
            	    const schedulePrice = item.schedulePrice || item.schedule_price;
            	    const platformName = item.platformName || item.platform_name;
            	    const scheduleImg = item.scheduleImg || item.scheduleImg;
            	    const scheduleUrl = item.scheduleUrl || '#'; // URL 없으면 '#' 처리
            	    
            	    

            	    

            	    let timeString = item.scheduleStart.replace(/\u202F|\u00A0/g, ' ');
            	    const date = new Date(timeString);

            	 // 시, 분 뽑기
					const timeStr = date.toLocaleTimeString('ko-KR', { 
    					hour: '2-digit', 
    					minute: '2-digit', 
    					hour12: false  // 24시간 형식
					});
					console.log(timeStr); // 예: "20:30"

					const priceString = (item.schedulePrice === 0 || item.schedulePrice === null)? '방송에서 공개': item.schedulePrice.toLocaleString('ko-KR') + '원';
            	    //const platformName = platformMap[item.platformName] || item.platformName; // 매핑 또는 원본
					console.log(scheduleName, scheduleStart, schedulePrice, platformName ,priceString,timeStr,schedulePrice);
					listHtml += `
						<div class="schedule-item">
						    <div class="schedule-time">\${timeStr}</div>
						    <div class="schedule-content">
						        <a href="\${scheduleUrl}" target="_blank">
						            <div class="schedule-image-placeholder">
						                <img src="${pageContext.request.contextPath}/images/\${encodeURIComponent(scheduleImg)}" alt="Schedule Image" />
						            </div>
						        </a>
						        <div class="schedule-details">
						        	<span class="platform-name">\${platformName}</span>
						            <a href="\${scheduleUrl}" target="_blank" class="platform-name"><h3 class="schedule-title">\${item.scheduleName}</h3></a>
						            <span class="schedule-price">\${priceString}</span>
						            <button class="notification-btn">알림 받기</button>
						        </div>
						    </div>
						</div>
						`;
					
					//listHtml  =  "<div>" + timeString  + "</div>";
					
            	})

            console.log("HTML 들어가기 전:", listHtml);
            container.innerHTML = listHtml;
        }

        function setActiveCategoryButton(){
            document.querySelectorAll('#category-container .category-btn').forEach(btn=>{
                btn.classList.remove('active');
                if(parseInt(btn.dataset.categoryId)===selectedCategoryIdFromServer) btn.classList.add('active');
            });
        }

        createDateButtons();
        setActiveCategoryButton();

        const scheduleContainer = document.getElementById('schedule-list-container');
        if(schedulesData && schedulesData.length>0){
            createScheduleList(schedulesData);
        } else {
            scheduleContainer.innerHTML = '<p style="text-align:center;margin-top:50px;color:#555;">해당 카테고리의 라이브가 없습니다.</p>';
        }
    });
</script>

</body>
</html>