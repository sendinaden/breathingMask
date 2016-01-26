#include <Wire.h>
#include <Adafruit_BMP085.h>
#include "LedController.h"
#include <Adafruit_NeoPixel.h>
#include "Algorithm.h"

Adafruit_NeoPixel led = Adafruit_NeoPixel(1, 6, NEO_GRB + NEO_KHZ800);
LedController ledcontrol(&led);
Adafruit_BMP085 sensor;
Algorithm algo = Algorithm();

void setup() {
  // put your setup code here, to run once:
  Serial.begin(57600);
  ledcontrol.begin();
  sensor.begin();
  algo.begin();
}

void loop() {
  // put your main code here, to run repeatedly:
  ledcontrol.shift();
  algo.newPressure(sensor.readPressure(),sensor.readTemperature());
  if (algo.mFeedback == algo.BAD){
    ledcontrol.setDirection(ledcontrol.RED);
  } else if (algo.mFeedback == algo.GOOD){
    ledcontrol.setDirection(ledcontrol.GREEN);
  } else if (algo.mFeedback == algo.MEDIUM) {
    ledcontrol.setDirection(ledcontrol.ORANGE);
  } else {
    ledcontrol.setDirection(ledcontrol.BLUE);
  }
  delay(10);
}
