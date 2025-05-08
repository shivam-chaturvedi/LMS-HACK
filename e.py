# !pip install together
# !pip install cohere
# !pip install webdriver-manager

import time
import keyboard
from selenium import webdriver
from selenium.webdriver.edge.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.edge.service import Service as EdgeService
from webdriver_manager.microsoft import EdgeChromiumDriverManager
from google import genai
import sys
import concurrent.futures

print("Script name:", sys.argv[0])
print("Arguments:", sys.argv[1:])

link = str(sys.argv[1])

# Gemini function
def get_answer_from_gemini(question, options):
    prompt = f'''Answer the following question and choose one option:
    Question: {question}
    Options:
    {options}
    Answer (Choose one option):'''

    try:
        client = genai.Client(api_key="AIzaSyARkc9tT3MYUZw6lfIVZ1_EZR-sdSYJF1w")
        response = client.models.generate_content(
            model="gemini-2.0-flash",
            contents=prompt + ''' 
            ans this and give answer only in one word only goive ANSWER IN OPTIONS LIKE A,B,C,D,E ETC NOT PRINT ANYTHING ELSE
     USE WEB SEARCH AND SEARCH ON GOOGLE FOR ANSWERS AND GENERATE ANSWER CORRECTLY            

            ''',
        )
        print(response.text)
        return response.text
    except Exception as e:
        print(f"Tried API but encountered error: {e}")
        return None

# Cohere function
def get_answer_from_gemini_pro(question, options):
    prompt = f'''Answer the following question and choose one option:
    Question: {question}
    Options:
    {options}
    Answer (Choose one option):'''

    client = genai.Client(api_key="AIzaSyARkc9tT3MYUZw6lfIVZ1_EZR-sdSYJF1w")

    response = client.models.generate_content(
        model="gemini-2.5-pro-preview-05-06",
        contents=prompt+'''
            ans this and give answer only in one word only goive ANSWER IN OPTIONS LIKE A,B,C,D,E ETC NOT PRINT ANYTHING ELSE
     USE WEB SEARCH AND SEARCH ON GOOGLE FOR ANSWERS AND GENERATE ANSWER CORRECTLY            

        ''',
    )

    print(response.text)
    return response.text


# Smart selector with timeout
def get_ans(question, options):
    print("\nSelecting answer for:\n", question)

    with concurrent.futures.ThreadPoolExecutor() as executor:
        future = executor.submit(get_answer_from_gemini_pro, question, options)
        try:
            answer = future.result(timeout=30)  # wait max 20 seconds
            if answer:
                return answer
        except concurrent.futures.TimeoutError:
            print("Gemini Pro took too long. Falling back to normal Gemini...")

    return get_answer_from_gemini(question, options)

# Setup browser with auto-downloaded Edge driver
options = Options()
options.add_argument("--start-maximized")  # Open in full screen

driver = webdriver.Edge(service=EdgeService(EdgeChromiumDriverManager().install()), options=options)

# Open Moodle and wait for user login
driver.get(link)
WebDriverWait(driver, 3000).until(EC.presence_of_element_located((By.ID, "username")))

print("Please enter username and password manually.")
time.sleep(6)  # Give user time to enter credentials
driver.find_element(By.ID, "loginbtn").click()
print("Logged in successfully.")

# Quiz loop
while True:
    try:
        WebDriverWait(driver, 3000).until(EC.presence_of_element_located((By.CLASS_NAME, "qtext")))
        question_text = driver.find_element(By.CLASS_NAME, "qtext").text.strip()
        options_div = driver.find_element(By.CLASS_NAME, "ablock")
        option_labels = options_div.find_elements(By.TAG_NAME, "label")
        options_dict = {}

        for label in option_labels:
            try:
                option_key = label.find_element(By.CLASS_NAME, "answernumber").text.strip()
                option_value = label.find_element(By.CLASS_NAME, "flex-fill").text.strip()
                options_dict[option_key] = option_value
            except:
                continue

        options_text = "\n".join([f"{k}: {v}" for k, v in options_dict.items()])
        print("\nQuestion:")
        print(question_text)
        print("\nOptions:")
        for k, v in options_dict.items():
            print(f"{k} {v}")

        selected_option = get_ans(question_text, options_text).strip().lower()
        print(f"\nSelected option: {selected_option}")
        selected_option = list(selected_option)[0]

        for label in option_labels:
            try:
                label_text = label.text.strip().lower()
                if label_text.startswith(f"{selected_option}"):
                    print(f"\nMatching label found: {label_text}")
                    label.click()
                    print(f"\nClicked option: {selected_option}")
                    break
            except Exception as e:
                print(f"Error matching label: {e}")
                continue

        print("\nWaiting for Ctrl+Alt+A to continue to next question...")
        keyboard.wait('ctrl+alt+a')
        print("Proceeding to next...\n")

    except Exception as e:
        print("Error:", e)
        break

driver.quit()
