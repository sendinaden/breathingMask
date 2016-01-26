/*
  LedController.h - Library to control an LED color shifiting.
  Created by Alaa El Jawad, November 1, 2015.
*/
#ifndef LEDCT
#define LEDCT

#include "Arduino.h"
#include <Adafruit_NeoPixel.h>

class LedController{
  public :
    LedController(Adafruit_NeoPixel*, int);
    void begin();
    // Colors on the RGB wheel
    enum colors{
      RED=0, GREEN=85, ORANGE=22, BLUE=150, VIOLET=192, YELLOW=42};
    void setDirection(colors color);
    void shift();
    void off();
    void on();
    void changeBrightness(int);
  private :
    Adafruit_NeoPixel _led;
    int _nLed;
    int32_t _Wheel(byte WheelPos);
    colors _direction;
};

#endif /* LEDCT */
