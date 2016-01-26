/*
  LedController.cpp - Library to control an LED color shifiting.
  Created by Alaa El Jawad, November 1, 2015.
*/
//#include "Arduino.h"
#include "LedController.h"

Adafruit_NeoPixel _led;
byte wheelpos = 0;

LedController::LedController(Adafruit_NeoPixel* led){
  _led = *led;
}

void LedController::begin(){
  _led.begin();
  _led.setPixelColor(0, 255, 0, 0);
  _led.show();
}

LedController::colors _direction;

void LedController::setDirection(colors color){
  _direction = color;
}

void LedController::shift(){
  if (wheelpos != _direction){
    wheelpos += (_direction-wheelpos)/abs(_direction-wheelpos);
    _led.setPixelColor(0, _Wheel(wheelpos));
    _led.show();
  }
}

int32_t LedController::_Wheel(byte WheelPos) {
  //0=r, 80=g, 125=b
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
