/*
  Algorithm.cpp - Library for the algorithm that control the led depending on the breathing
  Created by Alaa El Jawad, November 1, 2015.
*/

#include "Arduino.h"
#include "Algorithm.h"

int i = 0;

Algorithm::Algorithm(){
  _ftime = true;
}

void Algorithm::begin(){
//  Serial.println("Started");
  mFeedback = GOOD;
  _mean = 0;
  calibrating = true;
}

void Algorithm::newPressure(int32_t pressure, int32_t temperature){
  Serial.print("A");
  Serial.print(pressure);
  Serial.print("B");
  Serial.print(temperature);
  Serial.print("C");

//  Serial.print(calibrating);Serial.print("-");
//  Serial.print(i);Serial.print("-"); Serial.println(_delta_moy);

//  Serial.print(pressure);
//  Serial.print("-");
//  Serial.println(temperature);
  
  if (_ftime){
    _first = pressure;
    _ftime = false;
    _p0min = pressure;
    _p0max = pressure;
  }
  _cpressure = pressure;
  if (calibrating){
    _mean += _cpressure;
    _p0max = max(_cpressure, _p0max);
    _p0min = min(_cpressure, _p0min);
    i++;
    if (i==100) {
      calibrating = false;
      _mean /= 100;
      _initialization();
    }
  } else{
    _process();
    _updateFeedback();
  }
}

void Algorithm::_initialization(){
  _buff[0] = _mean; _buff[1] = _mean; _buff[2] = _mean;
  unsigned long int time0 = millis();
  _time_buff[0] = time0; _time_buff[1] = time0; _time_buff[2] = time0;
  _scan_state = 'S';
  _delta_buff[0] = _mean; _delta_buff[1] = _mean; _delta_buff[2] = _mean;
  _last_inhale_time = time0;
  _delta_moy = 0;
  _nb_inhales = 0;

  _pmax[0] = _p0max; _pmax[1] = _p0max; _pmax[2] = _p0max;
  _pmin[0] = _p0min; _pmin[1] = _p0min; _pmin[2] = _p0min;
}

void Algorithm::_process(){
  _buff[0] = _buff[1]; _buff[1] = _buff[2]; _buff[2] = _cpressure;

  _time_buff[0] = _time_buff[1]; _time_buff[1] = _time_buff[2]; _time_buff[2] = millis();
  _delta_buff[0] = _delta_buff[1]; _delta_buff[1] = _delta_buff[2]; _delta_buff[2] = millis()-_last_inhale_time;

  if (_scan_state == 'S' & max(max(_buff[0], _buff[1]), _buff[2]) <= _p0min) {
    _last_inhale_time = _time_buff[0];
    _delta_moy = (_delta_buff[0] + _delta_buff[1] + _delta_buff[2]) / 3;
    _nb_inhales += 1;
    _scan_state = 'I';
  }
  
  if (_scan_state == 'I' & min(min(_buff[0], _buff[1]), _buff[2]) >= _p0max) {
    _scan_state = 'S';
  }
}

void Algorithm::_updateFeedback(){
  if (_delta_moy < 1000) {
    mFeedback = BAD;
  } else if (_delta_moy < 2000) {
    mFeedback = MEDIUM;
  } else {
    mFeedback = GOOD;
  }
}

