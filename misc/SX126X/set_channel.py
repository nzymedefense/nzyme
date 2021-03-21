import serial
import time

def read_reply(s):
  while True:
    if s.inWaiting() > 0:
      time.sleep(0.1)
      buf = s.read(s.inWaiting())
      if len(buf) == 4:
        print("Current Channel: " + str(buf[3]))
        break

# CONFIG
serial_port =  "/dev/ttyUSB0"
channel = 65
# CONFIG END

print("!!!!! MAKE SURE TO SET THE BOARD TO CONFIGURATION MODE. M0 = short, M1 = open. Timeout? Possibly an incorrect serial baud rate.")

s = serial.Serial(serial_port, 9600)
s.flushInput()
time.sleep(0.1)

print("Connected to serial port.")
print("Reading current channel configuration...")

s.write(b'\xC1\x05\x01')
read_reply(s)

print("Setting channel...")

command = bytearray(b'\xC0\x05\x01')
command.append(channel)
s.write(command)
read_reply(s)

print("Complete.")

s.close()
