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


try:
    print("Script name:", sys.argv[0])
    print("Arguments:", sys.argv[1:])

    link = str(sys.argv[1])

    if(link is None):
        print("Link Cant Be Empty!")
        sys.exit(0)
except Exception as e:
    print("Link Cant Be Empty!")
    sys.exit(0)

# Gemini function
def get_answer_from_gemini_normal(question, options):
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
            ans this and give answer only in one word only give ANSWER IN OPTIONS LIKE A,B,C,D,E ETC NOT PRINT ANYTHING ELSE
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
            ans this and give answer only in one word only give ANSWER IN OPTIONS LIKE A,B,C,D,E ETC NOT PRINT ANYTHING ELSE
            USE WEB SEARCH AND SEARCH ON GOOGLE FOR ANSWERS AND GENERATE ANSWER CORRECTLY
        ''',
    )

    print(response.text)
    return response.text


def get_answer_from_gemini_flash(question, options):
    prompt = f'''Answer the following question and choose one option:
    Question: {question}
    Options:
    {options}
    Answer (Choose one option):'''

    client = genai.Client(api_key="AIzaSyARkc9tT3MYUZw6lfIVZ1_EZR-sdSYJF1w")

    response = client.models.generate_content(
        model="models/gemini-2.5-flash-preview-04-17",
        contents=prompt+'''
            ans this and give answer only in one word only give ANSWER IN OPTIONS LIKE A,B,C,D,E ETC NOT PRINT ANYTHING ELSE
            USE WEB SEARCH AND SEARCH ON GOOGLE FOR ANSWERS AND GENERATE ANSWER CORRECTLY
        ''',
    )

    print(response.text)
    return response.text


def get_ans(question, options, model):
    if(model=="p"):        
        print("Getting from pro")
        return get_answer_from_gemini_pro(question, options)
    elif(model=="f"):
        print("Getting from flash")
        return get_answer_from_gemini_flash(question, options)
    return get_answer_from_gemini_normal(question,options)


# Setup browser with auto-downloaded Edge driver
options = Options()
options.add_argument("--start-maximized")  # Open in full screen

driver = webdriver.Edge(service=EdgeService(EdgeChromiumDriverManager().install()), options=options)

# Open Moodle and wait for user login
driver.get(link)
WebDriverWait(driver, 3000).until(EC.presence_of_element_located((By.ID, "username")))
n=0
# Quiz loop
while True:
    try:
        WebDriverWait(driver, 3000).until(EC.presence_of_element_located((By.CLASS_NAME, "qtext")))
  # Get all questions on the page
        questions = driver.find_elements(By.CLASS_NAME, "qtext")
        answers= driver.find_elements(By.CLASS_NAME, "ablock")
        # print(questions)
        if not questions:
            print("No more questions found!")
            break

        # # Focus on the question specified by the user
        current_question = questions[n]
        current_answer=answers[n]

        print("TOTAL NUMBE ROF QUESTIONS ",len(questions))

        if(len(questions)>1):
            question_text=current_question.text.strip()
            
            options_div = current_answer
        else:
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
        
        model = "n"

        # Take input from different keys and print the selected model
        if keyboard.is_pressed('q'):
            model = "p"
            print("You chose pro model")
        elif keyboard.is_pressed('z'):
            model = "f"
            print("You chose flash model")
        elif keyboard.is_pressed('s'):                                          
            model = "n"
            print("Falling back to normal model")
        elif keyboard.is_pressed("r"):
            print("Resetting")
            n=0
        

        
        selected_option = get_ans(question_text, options_text, model).strip().lower()
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
        
        keyboard.wait("alt")  # Wait for the next key press
        n+=1
        if(n>len(questions)-1):
            n=0

    except Exception as e:
        break

driver.quit()
