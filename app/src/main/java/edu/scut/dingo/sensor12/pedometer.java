package edu.scut.dingo.sensor12;

import android.text.format.Time;
import android.util.Log;

/**
 * Created by Dash on 2016/12/2.
 */

public class pedometer {

    int stepCoutn=0;

    final int valueNum = 4;
    float[] tempValue = new float[valueNum];
    int tempCount = 0;
    //是否上升的标志位
    boolean isDirectionUp = false;
    //持续上升次数
    int continueUpCount = 0;
    int continueUpFormerCount = 0;
    boolean lastStatus = false;
    //波峰值
    float peakOfWave = 0;
    //波谷值
    float valleyOfWave = 0;
    //此次波峰的时间
    long timeOfThisPeak = 0;
    //上次波峰的时间
    long timeOfLastPeak = 0;
    //当前的时间
    long timeOfNow = 0;
    //当前传感器的值
    float gravityNew = 0;
    //上次传感器的值
    float gravityOld = 0;
    final float initialValue = (float) 4.0;
    //初始阈值
    float ThreadValue = (float) 4.0;

    public int DetectorNewStep(float values) {
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
            if (DetectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();
                if (timeOfNow - timeOfLastPeak >= 350
                        && (peakOfWave - valleyOfWave >= ThreadValue)) {
                    float va = peakOfWave - valleyOfWave;
                    Log.i("stepFindV", String.valueOf(va));
                    Log.i("stepFindT", String.valueOf(ThreadValue));
                    Log.i("timeVar", String.valueOf(timeOfNow - timeOfLastPeak));
                    timeOfThisPeak = timeOfNow;
                    stepCoutn++;

                }
                if (timeOfNow - timeOfLastPeak >= 250
                        && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
        return stepCoutn;
    }

    public boolean DetectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;//记录上一次状态是上升还是下降
        if (newValue >= oldValue) {
            //如果新的值大于旧的值，则表示上升状态
            isDirectionUp = true;
            continueUpCount++;//记录连续上升次数
        } else {
            //如果新的值小于旧的值，表示是下降状态
            continueUpFormerCount = continueUpCount;//记录了上一次上升的点数
            continueUpCount = 0;
            isDirectionUp = false;//更改为下降状态
        }

        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 || oldValue >= 20)) {
            peakOfWave = oldValue;//记录波峰值
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
        }
        return false;
    }

    public float Peak_Valley_Thread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;
    }
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / valueNum;
        if (ave >= 8)
            ave = (float) 5.3;
        else if (ave >= 7 && ave < 8)
            ave = (float) 4.3;
        else if (ave >= 4 && ave < 7)
            ave = (float) 3.3;
        else if (ave >= 3 && ave < 4)
            ave = (float) 2.0;
        else{
            ave = (float) 1.3;
        }
        return ave;
    }

    private String  GetTime(){
        Time t = new Time();
        t.setToNow();
        int year = t.year;
        int month = t.month+1;
        int day = t.monthDay;
        int hour = t.hour;
        int minu = t.minute;
        int second = t.second;
        String str = year+"-"+month+"-"+day+"  "+hour+":"+minu+":"+second;
        return  str;
    }

}
