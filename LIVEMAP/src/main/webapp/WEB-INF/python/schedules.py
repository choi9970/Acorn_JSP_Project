import logging
import json
import os
import time
import re
import requests
from datetime import datetime, timedelta, timezone
from urllib.parse import urlparse

from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC

# ====== 설정 ======
CHROME_DRIVER_PATH = r"C:\chromedriver-win64\chromedriver.exe"
BASE_URL_NAVER = "https://shoppinglive.naver.com/calendar"
BASE_URL_KAKAO = "https://shoppinglive.kakao.com/calendar"
HEADLESS = False
PATH_DIR = r"C:\javaCloud3Ws\LIVEMAP\src\main\webapp"

# ====== 로그 파일명 생성 (절대경로 기반) ======
log_dir = os.path.join(PATH_DIR, "logs")   # 현재 py 파일이 있는 폴더 기준
os.makedirs(log_dir, exist_ok=True)

timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
log_filename = f"{timestamp}_schedules.py.log"
log_filepath = os.path.join(log_dir, log_filename)

# ====== 로깅 설정 ======
logging.basicConfig(
    level=logging.INFO,  # 로그 레벨
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S',
    handlers=[
        logging.FileHandler(log_filepath, mode='a', encoding='utf-8'),
        #logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# ====== 크롬드라이버 서비스 생성 ======
def get_chromedriver_service(path: str) -> Service:
    if not os.path.exists(path):
        try:
            import chromedriver_autoinstaller
            chromedriver_autoinstaller.install()
            return Service()
        except Exception as e:
            raise RuntimeError(f"chromedriver가 없고 자동설치 실패: {e}")
    return Service(executable_path=path)


# ====== 공통: 날짜 리스트 생성 (KST 기준) ======
def make_kst_dates(days: int = 7):
    KST = timezone(timedelta(hours=9))
    today_kst = datetime.now(KST).date()
    start_date = today_kst - timedelta(days=1)
    end_date = today_kst + timedelta(days=days)
    total_days = (end_date - start_date).days
    return [(start_date + timedelta(days=i)).strftime("%Y%m%d") for i in range(total_days + 1)]


# ====== 공통: 파일명 안전하게 처리 ======
def make_safe_filename(title: str, ext: str = ".jpg") -> str:
    title = re.sub(r'[\n\r\t]', ' ', title)
    title = re.sub(r'[\\/*?:"<>|]', '', title)
    title = re.sub(r'\s+', ' ', title).strip()
    return f"{title}{ext}"


# ====== 공통: 이미지 다운로드 ======
def download_image(img_url: str, title: str, save_dir: str = None) -> str:
    # 저장 경로 지정: 기본은 PATH_DIR/images
    if save_dir is None:
        save_dir = os.path.join(PATH_DIR, "images")

    # 폴더 생성
    os.makedirs(save_dir, exist_ok=True)
    parsed = urlparse(img_url)
    filename_ext = os.path.splitext(parsed.path)[1] or ".jpg"
    save_name = make_safe_filename(title, filename_ext)
    save_path = os.path.join(save_dir, save_name)

    try:
        response = requests.get(img_url)
        with open(save_path, "wb") as f:
            f.write(response.content)
        logger.info(f"✅ 이미지 저장 완료: {save_path}")
        return os.path.basename(save_path)
    except Exception as e:
        logger.error(f"❌ 이미지 다운로드 실패: {e}")
        return None


# ====== 공통: datetime 처리 (카카오) ======
def kakao_datetime(span):
    for s in span.select(".screen_out"):
        s.decompose()
    date_text = span.get_text(strip=True)
    time_text = span.select_one(".txt_time").get_text(strip=True)
    date_text = date_text.replace(time_text, "").strip()
    year = datetime.now().year
    month = int(date_text.split("월")[0])
    day = int(date_text.split("월")[1].replace("일", ""))
    hour, minute = map(int, time_text.split(":"))
    dt = datetime(year, month, day, hour, minute)
    return dt.strftime("%Y-%m-%dT%H:%M:%S")


# ====== 공통: js 클릭 ======
def js_click(driver, css_selector: str, delay: float = 1.5):
    elem = WebDriverWait(driver, 1).until(
        EC.element_to_be_clickable((By.CSS_SELECTOR, css_selector))
    )
    driver.execute_script("arguments[0].scrollIntoView({block:'center'});", elem)
    driver.execute_script("arguments[0].click();", elem)
    time.sleep(delay)


# ====== 네이버 쇼핑라이브 수집 ======
def naver_shoppinglive(driver):
    json_list = []
    dates = make_kst_dates(days=1)

    for d in dates:
        url = f"{BASE_URL_NAVER}?d={d}"
        driver.get(url)

        try:
            WebDriverWait(driver, 1).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, ".Menulist_inner_i4c0e"))
            )
        except Exception:
            logger.warning("네이버 페이지 로딩 지연됨")

        html = driver.page_source
        soup = BeautifulSoup(html, "html.parser")
        a_tags = soup.select(".Menulist_inner_i4c0e a[role='tab']")
        logger.info(f"🔍 a태그 {len(a_tags)}개 발견")

        for i, a in enumerate(a_tags, 1):
            label = a.get_text(strip=True)
            if i == 1:
                logger.info(f"⏭️ {i}. '{label}' 탭은 기본 선택 상태 → 스킵")
                continue

            xpath_expr = f"//a[contains(normalize-space(text()), '{label}')]"
            elem = WebDriverWait(driver, 1).until(
                EC.element_to_be_clickable((By.XPATH, xpath_expr))
            )
            driver.execute_script("arguments[0].scrollIntoView({block:'center'});", elem)
            driver.execute_script("arguments[0].click();", elem)
            time.sleep(0.8)

            last_height = driver.execute_script("return document.body.scrollHeight")
            while True:
                driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
                time.sleep(1.0)
                new_height = driver.execute_script("return document.body.scrollHeight")
                if new_height == last_height:
                    break
                last_height = new_height
                time.sleep(0.8)

            soup_now = BeautifulSoup(driver.page_source, "html.parser")
            cards = soup_now.select(".VerticalCardList_item_YPN88")
            logger.info(f"📝 {len(cards)}개의 카드 발견 in '{label}'")

            for idx, card in enumerate(cards, 1):
                time_tag = card.select_one("time")
                card_time = time_tag["datetime"] if time_tag and time_tag.has_attr("datetime") else None
                title_tag = card.select_one("span.VideoTitle_wrap_fuxqM")
                card_title = title_tag.get_text(strip=True) if title_tag else None
                card_title = re.sub(r'[\n\r\t]', ' ', card_title)
                card_title = re.sub(r'\s+', ' ', card_title).strip()

                img_tag = card.select_one("img.CardThumbnail_image_d88Hz")
                save_path = download_image(img_tag["src"], card_title) if img_tag and img_tag.get("src") else None

                card_id_tag = card.select_one('a[data-shp-contents-id]')
                card_url = f"https://shoppinglive.naver.com/livebridge/{card_id_tag['data-shp-contents-id']}?fm=shoppinglive&sn=home" \
                    if card_id_tag else None

                price_tag = card.select_one(".ProductBox_price_\\+L7cC strong")
                price_num = re.sub(r"[^0-9]", "", price_tag.get_text(strip=True)) if price_tag else None

                discount_tag = card.select_one(".ProductBox_discount_-M6AK")
                discount = discount_tag.get_text(strip=True) if discount_tag else None

                item = {
                    "platform_id": 1,
                    "category_name": label,
                    "schedule_name": card_title,
                    "schedule_start": card_time,
                    "schedule_discount": discount,
                    "schedule_price": price_num,
                    "schedule_img": save_path,
                    "schedule_url": card_url,
                    "schedule_deleteflg": 0
                }
                json_list.append(item)

    logger.info("✅ 네이버 쇼핑라이브 완료")
    return json_list


# ====== 카카오 쇼핑라이브 수집 ======
def kakao_shoppinglive(driver):
    driver.get(BASE_URL_KAKAO)
    WebDriverWait(driver, 1).until(
        EC.presence_of_all_elements_located((By.CSS_SELECTOR, "ol.list_livecalendar li a.link_day"))
    )
    json_list = []

    def harvest_categories():
        html = driver.page_source
        soup = BeautifulSoup(html, "html.parser")
        for li in soup.select("ul.list_keyword li"):
            for a_tag in li.select("a"):
                text = a_tag.get_text(strip=True)
                if not text or text in ("전체", "카쇼라 PICK", "쇼핑 페스타"):
                    continue
                css_selector = f'a[data-tiara-name="{text}"]'
                try:
                    js_click(driver, css_selector)
                    new_soup = BeautifulSoup(driver.page_source, "html.parser")
                    for li2 in new_soup.select('[data-tiara-action-name="예정_콘텐츠_클릭"]'):
                        if li2.find_parent("div", class_="group_nolive"):
                            continue

                        label = {"식품": "푸드", "리빙": "라이프", "여행": "여행/체험"}.get(text, text)
                        span = li2.select_one("span.info_name")

                        card_title = span.get_text(strip=True) if span else None
                        card_title = re.sub(r'[\n\r\t]', ' ', card_title)
                        card_title = re.sub(r'\s+', ' ', card_title).strip()

                        for hidden in (span.select("span.screen_out") if span else []):
                            hidden.decompose()
                        span_time = li2.select_one("span.txt_date")
                        card_time = kakao_datetime(span_time) if span_time else None

                        discount_tag = li2.select_one("span.txt_sale")
                        discount = discount_tag.get_text(strip=True) if discount_tag else None

                        a_price_tag = li2.select_one('a.link_prd')
                        if a_price_tag:
                            num_tag = a_price_tag.select_one('.num_price')
                            price_num = int(re.sub(r'[^0-9]', '', num_tag.get_text())) if num_tag else None
                        else:
                            price_num = None

                        img_tag = li2.select_one("img.img_g")
                        save_path = download_image(img_tag["src"], card_title) if img_tag and img_tag.get(
                            "src") else None

                        a_url_tag = li2.select_one("a.link_item")
                        card_url = f"https://shoppinglive.kakao.com{a_url_tag['href']}" if a_url_tag and a_url_tag.get(
                            "href") else None

                        item = {
                            "platform_id": 2,
                            "category_name": label,
                            "schedule_name": card_title,
                            "schedule_start": card_time,
                            "schedule_discount": discount,
                            "schedule_price": price_num,
                            "schedule_img": save_path,
                            "schedule_url": card_url,
                            "schedule_deleteflg": 0
                        }
                        json_list.append(item)

                except Exception as e:
                    logger.warning(f"[WARN] 클릭 실패 ({text}): {e}")

    today_css = 'ol.list_livecalendar a.link_day[data-tiara-action-name="날짜_오늘_클릭"]'
    try:
        js_click(driver, today_css)
        harvest_categories()
    except Exception as e:
        logger.warning(f"⚠️ 오늘 클릭 실패: {e}")

#    ordnum = 1
#    while True:
#        tomorrow_css = f'ol.list_livecalendar a.link_day[data-tiara-action-name="날짜_미래_클릭"][data-tiara-ordnum="{ordnum}"]'
#        try:
#            js_click(driver, tomorrow_css)
#            harvest_categories()
#            ordnum += 1
#        except Exception:
#            logger.info(f"✅ 더 이상 미래 날짜 없음 (마지막 D-{ordnum - 1})")
#            break
        max_days = 1  # 제한할 미래 날짜 개수

        for ordnum in range(1, max_days + 1):
            tomorrow_css = f'ol.list_livecalendar a.link_day[data-tiara-action-name="날짜_미래_클릭"][data-tiara-ordnum="{ordnum}"]'
            try:
                js_click(driver, tomorrow_css)
                harvest_categories()
            except Exception:
                logger.info(f"⚠️ D-{ordnum} 클릭 실패 또는 날짜 없음")
                break

    logger.info("✅ 카카오 쇼핑라이브 완료")
    return json_list


# ====== 메인 함수 ======
def main():
    chrome_options = Options()
    if HEADLESS:
        chrome_options.add_argument("--headless=new")
    chrome_options.add_argument("--start-maximized")
    chrome_options.add_argument("--disable-blink-features=AutomationControlled")

    service = get_chromedriver_service(CHROME_DRIVER_PATH)
    driver = webdriver.Chrome(service=service, options=chrome_options)

    naver_list = naver_shoppinglive(driver)
    kakao_list = kakao_shoppinglive(driver)

    combined_list = naver_list + kakao_list

    # ====== 기존 combined_list 생성 후 ======
    today_str = datetime.now().strftime("%Y-%m-%d")
    logger.info(f"===== {today_str} 쇼핑라이브 데이터 =====")
    logger.info(json.dumps(combined_list, ensure_ascii=False, indent=2))

    # 결과 출력 (리스트 형태)
    # print(json.dumps(combined_list, ensure_ascii=False, indent=2))
    print(json.dumps(combined_list, ensure_ascii=False))
    driver.quit()


if __name__ == "__main__":
    main()
