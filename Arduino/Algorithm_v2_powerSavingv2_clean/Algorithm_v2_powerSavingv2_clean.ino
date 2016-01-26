#include <LowPower.h>
#include <SPI.h>
#include <Wire.h>
#include <PinChangeInt.h>

#include <Adafruit_Sensor.h>
#include <Adafruit_BMP280.h>
#include <Adafruit_NeoPixel.h>
#include "LedController.h"

// Defining pins for Sensors (Software SPI):
//Sensor Outside
#define BMPo_SCK 2
#define BMPo_SDO 5
#define BMPo_SDI 3
#define BMPo_CS 4
//Sensor Inside
#define BMPi_SCK 10
#define BMPi_SDO 7
#define BMPi_SDI 9
#define BMPi_CS 8

Adafruit_BMP280 inSensor(BMPi_CS, BMPi_SDI, BMPi_SDO,  BMPi_SCK);
Adafruit_BMP280 outSensor(BMPo_CS, BMPo_SDI, BMPo_SDO,  BMPo_SCK);

Adafruit_NeoPixel led = Adafruit_NeoPixel(2, 11, NEO_GRB + NEO_KHZ800);
LedController ledcontrol(&led, 2);
// BRIGHTNESS LEVELS
#define LOW_BRIGHTNESS 60
#define HIGH_BRIGHTNESS 200

// Variables for the algorithm:
int ex = 0;
int in = 0;
int hold = 0;
int lastValue = 0;
boolean firstBreath = true;   //flag: allow to do some special stuff on the first Breath
long delta[5];
long previoust = 0;

// Variables for the calibration:
float dmax = 0;
float dmin = 1000000;
float first = 0;
long t0 = 0;

// Mode
enum modes {SLEEPING, AWAKE_AUTO, AWAKE_SLAVE};
modes cMode = AWAKE_AUTO;

// Variables for Serial Communication
String receivedString = "";
boolean stringComplete = false;

// The pin that the Arduino receives
// serial data on.
#define SERIAL_RX_PIN 0

// The baud rate for serial communications
#define SERIAL_BAUD 57600

// Time to remain awake to check for serial port activity.
#define SERIAL_SLEEP_TIMEOUT 5000 // [ms]
long elapsed = 0;


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * GOTOSLEEP
 *
 * The function that put the arduino to sleep and its WakeHandler (does nothing)
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void WakeHandler()
{
  // Nothing to do; just wakes the device.
}

void goToSleep() {
  // Make sure all serial messages have been sent.
  Serial.flush();

  // Enable the pin change interrupt on the receive pin
  // so that serial activity will wake the device.
  pinMode(SERIAL_RX_PIN, INPUT_PULLUP);
  PCintPort::attachInterrupt(SERIAL_RX_PIN, &WakeHandler, LOW);

  // Enter power down state. We'll wake periodically.
  LowPower.powerDown(SLEEP_2S, ADC_OFF, BOD_OFF);

  // Detach pinchange interrupts & reconfigure serial port.
  PCintPort::detachInterrupt(SERIAL_RX_PIN);
  Serial.begin(SERIAL_BAUD);
  elapsed = millis();
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * CALIBRATION
 *
 * Calibrate the threshold values for detecting breathes
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void calibrate() {
  ledcontrol.setDirection(ledcontrol.VIOLET);
  for (int i = 0; i < 100; i++) {
    ledcontrol.shift();
    ledcontrol.shift();
    inSensor.readTemperature(); outSensor.readTemperature();
    float d = inSensor.readPressure() - outSensor.readPressure();
    dmax = max(d, dmax);
    dmin = min(d, dmin);
    delay(10);
  }
  t0 = millis();
  ledcontrol.setDirection(ledcontrol.GREEN);
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SET MODE
 *
 * Changes the cMode according to the command received in serial
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void setMode(String command) {
  if (command == "ConnectedX") {
    cMode = AWAKE_SLAVE;
    ledcontrol.on(); 
    ledcontrol.changeBrightness(LOW_BRIGHTNESS);
    calibrate();
  }
  else if (command == "DisconnectedX"){
    cMode = AWAKE_AUTO;
    ledcontrol.changeBrightness(HIGH_BRIGHTNESS);
  }
  else if (command == "SleepX") cMode = SLEEPING;
}
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SERIAL EVENT
 *
 * Is run in each algo to intercept if data is sent.
 * Data sent can notify of a connection / disconnection
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    receivedString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == 'X') {
      //      stringComplete = true;
      setMode(receivedString);
      receivedString = "";
      //      stringComplete = false;
    }
  }
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * ALGORITHM
 *
 * Detect breathes with by seeing if threshold values are cross
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
boolean lookingForNextBreath = true;  //false when we're looking for next exhale

void updateLED() {
  long bpm = 5 * 60000 / (delta[0] + delta[1] + delta[2] + delta[3] + delta[4]);
  if (bpm > 15) {
    ledcontrol.setDirection(ledcontrol.RED);
  } else if (bpm > 12) {
    ledcontrol.setDirection(ledcontrol.ORANGE);
  } else {
    ledcontrol.setDirection(ledcontrol.GREEN);
  }
}

void detectBreaths() {
  if (ex == 3) { //exhale confirmed
    if (!lookingForNextBreath) {
      lookingForNextBreath = true;
    }
  }
  if (in == 3) {
    if (lookingForNextBreath) {
      lookingForNextBreath = false;
      if (firstBreath) {
        previoust = millis();
        delta[0] = previoust - t0; delta[1] = previoust - t0; delta[2] = previoust - t0;
        delta[3] = previoust - t0; delta[4] = previoust - t0;
        firstBreath = false;
      }
      delta[0] = delta[1]; delta[1] = delta[2]; delta[2] = delta[3];
      delta[3] = delta[4]; delta[4] = millis() - previoust;
      previoust = millis();
      updateLED();
    }
  }
}

void algo() {
  // Always read temperature before reading the pressue otherwise, 
  // the effect of temperature is not applied to the calcululation
  // of pressure and we get erroneous data values for the pressure
  float it = inSensor.readTemperature(); outSensor.readTemperature();
  lastValue = inSensor.readPressure() - outSensor.readPressure();
  if (lastValue > dmax) {
    ex ++;
    in = 0;
    hold = 0;
  } else if (lastValue < dmin) {
    ex = 0;
    in ++;
    hold = 0;
  } else {
    ex = 0;
    in = 0;
    hold ++;
  }
  // Printing it to Serial
  if (cMode != SLEEPING) {
    Serial.print("A");
    Serial.print(lastValue);
    Serial.print("B");
    Serial.print(it);
    Serial.print("C");
  }
  detectBreaths();
}



/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * INACTIVITY CHECKER
 *
 * Set the mode to SLEEPING if no activity is detected.
 * No activity is detected if breath is hold for more than 200 values = 10s
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void checkInactivity() {
  if (hold > 200 && cMode == AWAKE_AUTO) {
    cMode = SLEEPING;
    ledcontrol.off();
    t0 = millis();
    firstBreath = true;
  }
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * CHECKING FOR ACTIVITY
 *
 * Set the mode to AWAKE_AUTO if an activity is detected.
 * An activity is detected if by doing the algorithm 10 times
 * we detect something else than holding.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void checkForActivity() {
  for (int i = 0; i < 10; i++) {
    algo();
    delay(10);
  }
  if (hold > 8) {
    hold = 0;
  } else if (ex > 2 || in > 2) {
    cMode = AWAKE_AUTO;
    t0 = millis();
    ledcontrol.on();
  }
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * SETUP
 *
 * Setup function runned only at the beginning
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void setup() {
  // initialize serial
  Serial.begin(SERIAL_BAUD);
  while (!Serial);
  delay(100); // to give time to Serial to initialize

  // reserve 200 bytes for the inputString:
  // 1 char is one byte
  receivedString.reserve(200);

  // initialize Led Controller
  ledcontrol.begin();

  // Initialize sensors.
  if (!inSensor.begin()) {
    Serial.println("Could not find inSensor");
    while (1) {};
  }
  if (!outSensor.begin()) {
    Serial.println("Could not find outSensor");
    while (1) {};
  }
  inSensor.readTemperature(); outSensor.readTemperature();
  first = inSensor.readPressure() - outSensor.readPressure();

  // Start the calibration
  calibrate();
  cMode = AWAKE_AUTO;
}

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * AWAKE MODE
 *
 * Describes the behaviour of the awake mode
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void awakeMode() {
  // Do the algorithm
  algo();
  ledcontrol.shift();
  delay(50);
}


/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * LOOP
 *
 * Main code that is going to run repeatedly
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */
void loop() {
  // put your main code here, to run repeatedly:
  serialEvent();
  if (stringComplete) {
    setMode(receivedString);
    receivedString = "";
  }
  switch (cMode) {
    case AWAKE_AUTO:
      awakeMode();
      // Check if nothing happened for a while
      checkInactivity();  // NOT NECESSARY TO ALWAYS CHECK, FROM TIME TO TIME BETTER.
      break;

    case AWAKE_SLAVE:
      awakeMode();
      // Check if nothing happened for a while
      checkInactivity();  // NOT NECESSARY TO ALWAYS CHECK, FROM TIME TO TIME BETTER.
      break;

    case SLEEPING:
      // Go back to sleep after a while if the mode hasn't been changed
      if (millis() - elapsed > SERIAL_SLEEP_TIMEOUT) {
        goToSleep();  // here the arduino goes to sleep, interrupted ONLY by a serial data
        // here the sleep is over (we can change it to make it 8s instead of FOREVER)
        // if forever then it will never wake and check activity by itself
        checkForActivity();
      } else {
        Serial.println("");
      }
      break;
  }
}
