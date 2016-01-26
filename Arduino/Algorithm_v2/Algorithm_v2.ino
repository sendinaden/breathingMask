#include <SPI.h>
#include <Wire.h>

#include <Adafruit_Sensor.h>
#include <Adafruit_BMP280.h>
#include <Adafruit_NeoPixel.h>
#include "LedController.h"

#define BOARD 1   //0:Breadboard, 1:Nicolas, 2:Alaa
#define USER 0    //0:phone, 1:processing, 2:None(does nothing)

// Defining pins for Sensors (Software SPI):
//Sensor Outside
#define BMPo_SCK 2  // 2
#define BMPo_SDO 5  // 5
#define BMPo_SDI 3  // 3
#define BMPo_CS 4   // 4

#if BOARD==0  //Breadboard

  #define BMPi_SCK 2  //7
  #define BMPi_SDO 5  //6
  #define BMPi_SDI 3  //5
  #define BMPi_CS 4   //4

  Adafruit_BMP280 inSensor(BMPi_CS, BMPi_SDI, BMPi_SDO,  BMPi_SCK);
  Adafruit_BMP280 outSensor(10);

  // No Leds
  
#elif BOARD==1  // Nicolas' board

  #define BMPi_SCK 10
  #define BMPi_SDO 7
  #define BMPi_SDI 9
  #define BMPi_CS 8

  Adafruit_BMP280 inSensor(BMPi_CS, BMPi_SDI, BMPi_SDO,  BMPi_SCK);
  Adafruit_BMP280 outSensor(BMPo_CS, BMPo_SDI, BMPo_SDO,  BMPo_SCK);

  
  #define LED 11
  Adafruit_NeoPixel led = Adafruit_NeoPixel(2, LED, NEO_GRB + NEO_KHZ800);
  LedController ledcontrol(&led, 2);

#elif BOARD==2  // Alaa's Board

  #define BMPi_SCK 10
  #define BMPi_SDO 7
  #define BMPi_SDI 9
  #define BMPi_CS 8
  
  Adafruit_BMP280 inSensor(BMPi_CS, BMPi_SDI, BMPi_SDO,  BMPi_SCK);
  Adafruit_BMP280 outSensor(BMPo_CS, BMPo_SDI, BMPo_SDO,  BMPo_SCK);

  #define LED_R 11
  Adafruit_NeoPixel ledR = Adafruit_NeoPixel(1, LED_R, NEO_GRB + NEO_KHZ800);
  #define LED_L 6
  Adafruit_NeoPixel ledL = Adafruit_NeoPixel(1, LED_L, NEO_GRB + NEO_KHZ800);
  
  LedController ledcontrolR(&ledR, 1);
  LedController ledcontrolL(&ledL, 1);

#endif /*BOARD TYPE*/

int ex = 0;
int in = 0;

// Calibration values:
float dmax = 0;
float dmin = 1000000;
float first = 0;

// Config testing values:
float outNoiseMax = 0;
float outNoiseMin = 1000000;


//====================================================================================
//
void calibrate() {
  for (int i = 0; i < 100; i++) {
    inSensor.readTemperature(); outSensor.readTemperature();
    float d = inSensor.readPressure() - outSensor.readPressure();
    dmax = max(d, dmax);
    dmin = min(d, dmin);
    delay(10);
  }
}

void algo(){
  inSensor.readTemperature(); outSensor.readTemperature();
  float d = inSensor.readPressure() - outSensor.readPressure();
  //  Serial.print(inSensor.readPressure()); Serial.print("-"); Serial.print(outSensor.readPressure());
  //  Serial.println(inSensor.readPressure()-outSensor.readPressure());
  Serial.print(String(first) + "   ");
  Serial.print(dmin); Serial.print(" - "); Serial.print(d); Serial.print(" - "); Serial.print(dmax);
  if (d > dmax) {
    ex ++;
    in = 0;
  } else if (d < dmin) {
    ex = 0;
    in ++;
  } else {
    ex = 0;
    in = 0;
  }
  if (in > 2) {
    Serial.println(" I");
    #if BOARD == 0
      // no led so no feedback
    #elif BOARD == 1
    ledcontrol.setDirection(ledcontrol.GREEN);
    #elif BOARD == 2
    ledcontrolR.setDirection(ledcontrolR.GREEN);
    ledcontrolL.setDirection(ledcontrolL.GREEN);
    #endif  /*BOARD*/
  }
  else if (ex > 2){
    Serial.println(" E");
    #if BOARD == 0
      // no led so no feedback
    #elif BOARD == 1
    ledcontrol.setDirection(ledcontrol.RED);
    #elif BOARD == 2
    ledcontrolR.setDirection(ledcontrolR.RED);
    ledcontrolL.setDirection(ledcontrolL.RED);
    #endif
  }
  else Serial.println();
}

void testConfig() {
  inSensor.readTemperature(); outSensor.readTemperature();
  float d = outSensor.readPressure();
  outNoiseMax = max(d, outNoiseMax);
  outNoiseMin = min(d, outNoiseMin);
  
  Serial.print(outNoiseMax);
  Serial.print("-");
  Serial.print(outNoiseMin);
  Serial.print("=");
  Serial.println(outNoiseMax - outNoiseMin);
  
}
void plotOutSensorForPhone() {
  float ot = outSensor.readTemperature();
  Serial.print("A");
  Serial.print(outSensor.readPressure());
  Serial.print("B");
  Serial.print(ot);
  Serial.print("C");
}

void testConfigForPhone() {
  float it = inSensor.readTemperature(); outSensor.readTemperature();
  float d = outSensor.readPressure();
  outNoiseMax = max(d, outNoiseMax);
  outNoiseMin = min(d, outNoiseMin);
  
  Serial.print("A");
  Serial.print(outNoiseMax - outNoiseMin);
  Serial.print("B");
  Serial.print(it);
  Serial.print("C");
}

void phoneMode(){
  float it = inSensor.readTemperature();
  outSensor.readTemperature();
  float d = inSensor.readPressure() - outSensor.readPressure();
  if (d > dmax) {
    ex ++;
    in = 0;
  } else if (d < dmin) {
    ex = 0;
    in ++;
  } else {
    ex = 0;
    in = 0;
  }
  Serial.print("A");
  Serial.print(d);
  Serial.print("B");
  Serial.print(it);
  Serial.print("C");
}

void processingMode(){
  Serial.print(inSensor.readTemperature());
  Serial.print("|");
  Serial.println(inSensor.readPressure());
  
}
void processingMode2(){
  float it = inSensor.readTemperature();
  outSensor.readTemperature();
  Serial.print(inSensor.readPressure() - outSensor.readPressure());
  Serial.print("-");
  Serial.print(it);
  Serial.println("C");
}
//====================================================================================
//

void setup() {
  #if USER == 2
  #else
  Serial.begin(57600);
  while (!Serial);
  delay(100); // to give time to Serial to initialize
  
  #if BOARD == 0
  #elif BOARD == 1
    ledcontrol.begin();
//    ledcontrol.off();
  #elif BOARD == 2
    ledcontrolR.begin();
//    ledcontrolR.off();
    ledcontrolL.begin();
//    ledcontrolL.off();
  #endif  /*BOARD*/
    
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
  calibrate();
  #endif /*USER*/
}

void loop() {
  // put your main code here, to run repeatedly:
//  algo();
//  ledcontrol.shift();
//  testConfig();
//  testConfigForPhone();
//   plotOutSensorForPhone();
  #if USER == 0
  phoneMode();
  #elif USER == 1
  processingMode2();
  #elif USER == 2
  #endif /*USER*/
  delay(50);
}

