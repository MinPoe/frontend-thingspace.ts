import requests
import random

base_url = "http://3.14.160.244/api/"

dev_login_url = base_url + "auth/dev-login"

dev_login_body = {
    "email" : f"dev-login{random.randint(0,9999)}@example.com"
}
print(dev_login_body, "/n/n")
response = requests.post(dev_login_url, json=dev_login_body)

print(response.json())


bearer_token = None

'''

get note format from frontend note creation. 

pass it in to chatgpt, generate 100 notes, all different semantics. Store in file

get bearer token for an existing user. Create these 100 notes in a new special workspace. 



'''