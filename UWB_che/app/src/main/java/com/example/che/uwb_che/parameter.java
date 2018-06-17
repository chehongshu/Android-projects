package com.example.che.uwb_che;

/**
 * Created by che on 2018/5/27.
 */

public class parameter {
    float accx;
    float accy;
    float accz;
    float gyrox;
    float gyroy;
    float gyroz;
    float anglex;
    float angley;
    float anglez;

    float depth;
    float temperature;
    float ph;

    parameter(float accx,float accy,float accz, float gyrox, float gyroy, float gyroz,float anglex,
              float angley , float anglez, float depth, float temperature, float ph)
    {
        this.accx = accx;
        this.accy = accy;
        this.accz = accz;
        this.gyrox = gyrox;
        this.gyroy = gyroy;
        this.gyroz = gyroz;
        this.anglex = anglex;
        this.angley = angley;
        this.anglez = anglez;

        this.ph = ph;
        this.temperature = temperature;
        this.depth = depth;
    }

}
