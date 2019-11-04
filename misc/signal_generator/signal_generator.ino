#include <ESP8266WiFi.h>
#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64

#define OLED_RESET -1
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

int loopCount = 0;

// CONFIG HERE
// Milliseconds between beacons.
#define BEACON_FREQUENCY_MS 200

// The source MAC address, as HEX. There is
// totally a way to this with a string constant
// but god knows how this work with Arduino C
// so you have to live with this shitty way.
#define MAC_1 0x88
#define MAC_2 0x96
#define MAC_3 0x4e
#define MAC_4 0x4d
#define MAC_5 0x77
#define MAC_6 0x80

// SSID
#define SSID "ATT78V5wjA"

// Channel
#define CHANNEL 1
// CONFIG END

void setup() {
  Serial.begin(9600);

  if(!display.begin(SSD1306_SWITCHCAPVCC, 0x3D)) {
    Serial.println(F("SSD1306 allocation failed"));
    for(;;);
  }

  Serial.println("==== BOOT");
  Serial.println("Config:");
  Serial.println("MAC: " + hexMacConstantsToString());
  Serial.println("SSID: " + String(SSID));
  Serial.println("CHANNEL:" + String(CHANNEL));
  

  display.clearDisplay();

  // Splash screen.
  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(10,18);
  display.println(F("Initializing nzyme"));
  display.setCursor(15,30);
  display.println(F("beacon generator"));
  display.display();

  // Initialize WiFi.
  delay(2500);
  wifi_set_opmode(STATION_MODE);
  wifi_promiscuous_enable(1); 
}

void loop() {
  display.clearDisplay();
  display.setCursor(0,0);
  display.println(loopCount);
  display.println("\nMAC:" + hexMacConstantsToString());
  display.println("\nSSID:" + String(SSID));
  display.println("Channel: " + String(CHANNEL));
  display.println();
  
  display.display();

  Serial.println("Sending beacon.");
  Serial.println("Sequence:");
  Serial.println(loopCount);
  
  sendBeacon(SSID);
  delay(BEACON_FREQUENCY_MS);

  loopCount++;
}

void sendBeacon(char* ssidName) {
    byte channel = CHANNEL,
    wifi_set_channel(channel);

    // Base frame structure.
    uint8_t frame[128] = { 0x80, 0x00, 0x00, 0x00,
                0xff, 0xff, 0xff, 0xff, 0xff, 0xff,
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
                0xc0, 0x6c, 0x83, 0x51, 0xf7, 0x8f,
                0x0f, 0x00, 0x00, 0x00, 0xFF, 0x00,
                0x01, 0x04,0x00 };

    // Set SSID
    int ssidLength = strlen(ssidName);
    frame[37] = ssidLength;
    for(int i = 0; i < ssidLength; i++) {
      frame[38+i] = ssidName[i];
    }

    // Add current channel and supported rates behind SSID.
    uint8_t ext[13] = { 0x01, 0x08, 0x82, 0x84, 0x8b,
                        0x96, 0x24, 0x30, 0x48, 0x6c,
                        0x03, 0x01, 0x04  };
    for(int i = 0; i < 12; i++) {
      frame[38 + ssidLength + i] = ext[i];
    }

    // Add channel.
    frame[50 + ssidLength] = channel;

    // MAC address.
    frame[10] = frame[16] = MAC_1;
    frame[11] = frame[17] = MAC_2;
    frame[12] = frame[18] = MAC_3;
    frame[13] = frame[19] = MAC_4;
    frame[14] = frame[20] = MAC_5;
    frame[15] = frame[21] = MAC_6;
    int frameLength = 51 + ssidLength;

    // Front toward enemy.
    wifi_send_pkt_freedom(frame, frameLength, 0);
    wifi_send_pkt_freedom(frame, frameLength, 0);
    wifi_send_pkt_freedom(frame, frameLength, 0);
    
    delay(1);
}

String hexMacConstantsToString() {
  return String(MAC_1, HEX) + ":"
       + String(MAC_2, HEX) + ":"
       + String(MAC_3, HEX) + ":"
       + String(MAC_4, HEX) + ":"
       + String(MAC_5, HEX) + ":"
       + String(MAC_6, HEX);
}
