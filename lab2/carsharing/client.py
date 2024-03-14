import requests
import json

URL = "http://localhost:8056/carsharing/"

initial_client_type = -1
initial_message_id = -1
PARAMS = {'clientId': "", 'clientType': initial_client_type, 'messageId': initial_message_id, 'payload': ""}

print("Enter id: ")
stringClientId = str(input())

print("Enter client type (0 owner, 1 renter): ")
clientType = int(input())

print("Enter command (0 register renter, 1 register owner, 2 post car, 3 request car, 4 start rental, 5 end rental): ")
messageId = int(input())

payload = "Random text"

PARAMS["clientId"] = stringClientId
PARAMS["clientType"] = clientType
PARAMS["messageId"] = messageId
PARAMS["payload"] = payload

headers = {'Content-Type': 'application/json', 'Accept': '*/*'}
r = requests.post(url = URL, data = json.dumps(PARAMS), headers = headers)

print('Status code:', r.status_code)
print('Response body:', r.text)

