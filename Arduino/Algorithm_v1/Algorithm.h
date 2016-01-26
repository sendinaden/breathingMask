/*
  Algorithm.h - Library for the algorithm that control the led depending on the breathing
  Created by Alaa El Jawad, November 1, 2015.
*/

#ifndef ALGO
#define ALGO

#include "Arduino.h"

class Algorithm{
  public :
    Algorithm();
    void begin();
    enum feedbacks{
      GOOD, MEDIUM, BAD, ALERT};
    void newPressure(int32_t pressure, int32_t temperature);
    feedbacks mFeedback;
    boolean calibrating;
    
  private :
    int32_t _first;
    boolean _ftime;
    double _cpressure;
    double _mean, _p0max, _p0min;
    double _pmax[3];
    double _pmin[3];
    double _buff[3];
    unsigned long int _time_buff[3];
    char _scan_state;
    double _delta_buff[3];
    unsigned long int _last_inhale_time;
    double _delta_moy;
    unsigned long int _nb_inhales;

    void _initialization();
    void _process();
    void _updateFeedback();
};

#endif /* LEDCT */
