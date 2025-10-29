<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <title>Admin Dashboard</title>
    <style>
        .result { margin-top: 10px; padding: 5px; border: 1px solid #ccc; border-radius: 5px; }
        .loading { color: #888; }
        /* 알림 취소 버튼 결과 표시를 위한 스타일 추가 */
        #processCancelledResult { margin-top: 10px; padding: 5px; border: 1px solid #ccc; border-radius: 5px; }
    </style>
</head>
<body>
<h2>관리자 대시보드</h2>

<button id="categoryBtn">카테고리 Python 실행 및 등록</button>
<div id="categoryResult" class="result"></div>

<hr>

<button id="scheduleBtn">스케줄 Python 실행 및 등록</button>
<div id="scheduleResult" class="result"></div>

<hr>

<button id="processCancelledBtn">중지 방송 알림 일괄 취소 처리</button>
<div id="processCancelledResult" class="result"></div>


<script>
document.addEventListener('DOMContentLoaded', function() {

    const categoryBtn = document.getElementById('categoryBtn');
    const categoryResult = document.getElementById('categoryResult');

    const scheduleBtn = document.getElementById('scheduleBtn');
    const scheduleResult = document.getElementById('scheduleResult');
    
    // [추가] 알림 취소 처리 요소
    const processCancelledBtn = document.getElementById('processCancelledBtn');
    const processCancelledResult = document.getElementById('processCancelledResult');


    // ===== 카테고리 등록 =====
    categoryBtn.addEventListener('click', () => {
        categoryResult.innerHTML = '<span class="loading">실행 중...</span>';

        fetch('${pageContext.request.contextPath}/categoryCreate')
            .then(res => res.json())
            .then(data => {
                if(data.error) {
                    categoryResult.innerHTML = `<span style="color:red">${data.error}</span>`;
                } else {
                    categoryResult.innerHTML = `
                        Python에서 총 ${data.categoryCount}개의 카테고리를 받음<br>
                        신규 등록: ${data.catInsertedCount}개
                    `;
                }
            })
            .catch(err => {
                categoryResult.innerHTML = `<span style="color:red">오류 발생: ${err}</span>`;
            });
    });

    // ===== 스케줄 등록 =====
    scheduleBtn.addEventListener('click', () => {
        scheduleResult.innerHTML = '<span class="loading">실행 중...</span>';

        fetch('${pageContext.request.contextPath}/schedulesCreate')
            .then(res => res.json())
            .then(data => {
                if(data.error) {
                    scheduleResult.innerHTML = `<span style="color:red">${data.error}</span>`;
                } else {
                    scheduleResult.innerHTML = `
                        Python에서 총 ${data.totalCount}개의 스케줄을 받음<br>
                        기존 데이터에서 삭제처리: ${data.deletedCount1 + data.deletedCount2}개<br>
                        신규 등록: ${data.insertedCount}개<br>
                        업데이트: ${data.updatedCount}개<br>
                    `;
                }
            })
            .catch(err => {
                scheduleResult.innerHTML = `<span style="color:red">오류 발생: ${err}</span>`;
            });
    });

    // ===== 중지 방송 알림 일괄 취소 처리 =====
    processCancelledBtn.addEventListener('click', () => {
        if (!confirm('schedule_deleteflg=1인 모든 알림을 취소하고 메시지를 발송하시겠습니까?')) {
            return;
        }

        processCancelledResult.innerHTML = '<span class="loading">처리 중...</span>';
        processCancelledBtn.disabled = true;

        // [핵심] AdminAlertCancelServlet 호출
        fetch('${pageContext.request.contextPath}/admin/processCancelledAlerts', { 
            method: 'POST' 
        })
        .then(response => response.json())
        .then(data => {
            processCancelledBtn.disabled = false;
            
            if (data.success) {
                processCancelledResult.innerHTML = `<span style="color:green; font-weight:bold;">✅ 성공! ${data.message}</span>`;
            } else {
                processCancelledResult.innerHTML = `<span style="color:red">❌ 실패: ${data.message}</span>`;
            }
        })
        .catch(error => {
            processCancelledBtn.disabled = false;
            console.error('Fetch 오류:', error);
            processCancelledResult.innerHTML = `<span style="color:red">통신 오류 발생: ${error}</span>`;
        });
    });

});
</script>

</body>
</html>