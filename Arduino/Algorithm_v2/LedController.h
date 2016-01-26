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
    enum colors{
      RED, GREEN=80, ORANGE=30, BLUE=169};
    void setDirection(colors color);
    void shift();
    void off();
  private :
    Adafruit_NeoPixel _led;
    int _nLed;
    int32_t _Wheel(byte WheelPos);
    colors _direction;
};

#endif /* LEDCT */
