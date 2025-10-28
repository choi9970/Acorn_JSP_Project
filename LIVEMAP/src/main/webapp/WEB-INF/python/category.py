import json
import os
import time
from datetime import datetime, timedelta, timezone

from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC


# ====== 설정 ======
CHROME_DRIVER_PATH = r"C:\chromedriver-win64\chromedriver.exe"  # 직접 설치한 크롬드라이버 경로
BASE_URL = "https://shoppinglive.naver.com/calendar"
HEADLESS = False  # 창 숨기려면 True


# ====== 크롬드라이버 서비스 생성 (자동설치 fallback 포함) ======
def get_chromedriver_service(path: str) -> Service:
    if not os.path.exists(path):
        try:
            import chromedriver_autoinstaller
            chromedriver_autoinstaller.install()
            # 자동 설치 후, PATH에서 크롬드라이버를 찾아서 Service 생성
            return Service()
        except Exception as e:
            raise RuntimeError(f"chromedriver가 없고 자동설치 실패: {e}")
    return Service(executable_path=path)


# ====== 한국 시간 기준 날짜 리스트 생성 (오늘부터 지정 일수까지) ======
def make_kst_dates(days: int = 7):
    KST = timezone(timedelta(hours=9))
    today_kst = datetime.now(KST).date()
    return [(today_kst + timedelta(days=i)).strftime("%Y%m%d") for i in range(days + 1)]


# ====== 네이버 쇼핑라이브 캘린더 URL 생성 ======
def build_calendar_url(yyyymmdd: str) -> str:
    return f"{BASE_URL}?d={yyyymmdd}"


# ====== 네이버 쇼핑라이브 카테고리 추출 함수 ======
def naver_shoppinglive_category(driver, category_list: list[str]):
    dates = make_kst_dates(days=7)  # 오늘부터 7일 뒤까지 날짜 리스트 생성

    for d in dates:
        url = build_calendar_url(d)
        driver.get(url)

        # 핵심 영역이 렌더될 때까지 최대 10초 대기
        try:
            WebDriverWait(driver, 10).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, ".Menulist_inner_i4c0e"))
            )
        except Exception:
            # 로딩 지연 시 에러 메시지 출력 없이 그냥 계속 진행
            pass

        html = driver.page_source
        soup = BeautifulSoup(html, "html.parser")

        # 해당 영역 내 모든 탭 a 태그 선택
        a_tags = soup.select(".Menulist_inner_i4c0e a[role='tab']")

        for i, a in enumerate(a_tags, start=1):
            label = a.get_text(strip=True)
            if i == 1:
                # 기본 선택된 첫 번째 탭은 건너뜀
                continue
            if label not in category_list:
                category_list.append(label)

    return category_list


# ====== 카카오 쇼핑라이브 카테고리 추출 함수 ======
def kakao_shoppinglive_category(driver, category_list: list[str]):
    driver.get("https://shoppinglive.kakao.com/calendar")

    # 날짜 요소가 모두 로드될 때까지 대기
    WebDriverWait(driver, 10).until(
        EC.presence_of_all_elements_located((By.CSS_SELECTOR, "ol.list_livecalendar li a.link_day"))
    )

    date_elements = driver.find_elements(By.CSS_SELECTOR, "ol.list_livecalendar li a.link_day")

    for i in range(len(date_elements)):
        html = driver.page_source
        soup = BeautifulSoup(html, "html.parser")

        # 날짜별 키워드 리스트 내 a 태그 추출 및 텍스트 정리
        for li in soup.select("ul.list_keyword li"):
            for a_tag in li.select("a"):
                text = a_tag.get_text(strip=True)

                if text and text not in category_list:
                    # 일부 카테고리명 변경 및 제외 조건 처리
                    if text == '식품':
                        text = "푸드"
                    elif text == '리빙':
                        text = "라이프"
                    elif text == '여행':
                        text = "여행/체험"

                    if text not in category_list and text not in ("전체", "카쇼라 PICK", "쇼핑 페스타"):
                        category_list.append(text)

        # 다시 날짜 요소 재조회 (페이지 DOM 변동 대응)
        date_elements = driver.find_elements(By.CSS_SELECTOR, "ol.list_livecalendar li a.link_day")
        element = date_elements[i]

        # 해당 날짜 클릭 (JS 실행으로 클릭)
        driver.execute_script("arguments[0].click();", element)
        time.sleep(2)

    driver.quit()

    return category_list


# ====== 메인 함수 ======
def main():
    category_list = []

    chrome_options = Options()
    if HEADLESS:
        chrome_options.add_argument("--headless=new")
    chrome_options.add_argument("--start-maximized")
    chrome_options.add_argument("--disable-blink-features=AutomationControlled")

    service = get_chromedriver_service(CHROME_DRIVER_PATH)
    driver = webdriver.Chrome(service=service, options=chrome_options)

    naver_shoppinglive_category(driver, category_list)
    kakao_shoppinglive_category(driver, category_list)

    # 결과 JSON 형태로 출력 (UTF-8 한글 깨짐 방지)
    print(json.dumps(category_list, ensure_ascii=False))


if __name__ == "__main__":
    main()
    
