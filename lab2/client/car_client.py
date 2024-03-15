import websocket
import threading
import time

class Car:
    def __init__(self):
        self.fuel_level = 100
        self.is_locked = "true"
        self.rental_started = False
        self.stop_updates = False

    def start_updates(self, ws):
        while not self.stop_updates:
            if self.rental_started:
                if (self.fuel_level > 0 ):
                    self.fuel_level -= 1
                ws.send(f"{self.fuel_level}")
                time.sleep(10)  # send updates every 5 seconds

car = Car()

def on_message(ws, message):
    print(f"Received: {message}")
    if message == "connected":
        # Send an update with the fuel level and lock status
        ws.send(f"{car.fuel_level},{car.is_locked}")
    elif message == "start rental":
        car.rental_started = True
        car.stop_updates = False
        threading.Thread(target=car.start_updates, args=(ws,)).start()
    elif message == "stop rental":
        car.rental_started = False
        car.stop_updates = True
    elif message == "lock":
        car.is_locked = "true"
    elif message == "unlock":
        car.is_locked = "false"

def on_error(ws, error):
    print(f"Error: {error}")

def on_close(ws, status_code, reason):
    print(f"Connection closed with status code {status_code} and reason {reason}")

def on_open(ws):
    print("Connection opened")
    # Send the car id taken as input from the console
    car_id = input("Enter car id: ")
    ws.send(car_id)

if __name__ == "__main__":
    ws = websocket.WebSocketApp("ws://127.0.0.1:8056/car-connection",
                                on_message = on_message,
                                on_error = on_error,
                                on_close = on_close)
    ws.on_open = on_open
    ws.run_forever()