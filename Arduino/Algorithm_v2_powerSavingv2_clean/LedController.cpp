/*
  LedController.cpp - Library to control an LED color shifiting.
  Created by Alaa El Jawad, November 1, 2015.
*/
//#include "Arduino.h"
#include "LedController.h"

byte wheelpos = 0;

LedController::LedController(Adafruit_NeoPixel* led, int nLed){
  _led = *led;
  _nLed = nLed;
}

void LedController::begin(){
  _led.begin();
  for (int i=0; i<_nLed; i++){
    _led.setPixelColor(i, 0, 0, 0);
  }
  _led.show();
}

void LedController::off(){
  _led.setBrightness(0);
  _led.show();
}

void LedController::on(){
  _led.setBrightness(120);
  for (int i=0; i<_nLed; i++){
      _led.setPixelColor(i, _Wheel(wheelpos));
    }
  _led.show();
}

void LedController::setDirection(colors color){
  _direction = color;
}

void LedController::changeBrightness(int br){
  _led.setBrightness(br);
  _led.show();
}

void LedController::shift(){
  if (wheelpos != _direction){
    wheelpos += (_direction-wheelpos)/abs(_direction-wheelpos);
    for (int i=0; i<_nLed; i++){
      _led.setPixelColor(i, _Wheel(wheelpos));
    }
    _led.setBrightness(120);
    _led.show();
  }
}

int32_t LedController::_Wheel(byte WheelPos) {
  //0->0d 255->360d
  WheelPos = 255 - WheelPos;
  if(WheelPos < 85) {
    return _led.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  }
  if(WheelPos < 170) {
    WheelPos -= 85;
    return _led.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  }
  WheelPos -= 170;
  return _led.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
}
