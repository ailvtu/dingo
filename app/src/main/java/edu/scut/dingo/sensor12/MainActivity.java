package edu.scut.dingo.sensor12;
//UPDATE_LOG
//20150811 write file button onclick
//20150812 add Time to file  Toast show
//20150815 add Path an rename
//20150815 modify the layout with Relative and Linear
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class MainActivity extends ActionBarActivity {

    TextView textViewX = null;
    TextView textViewY = null;
    TextView textViewZ = null;
    TextView textViewC = null;
    TextView Time = null;
    Button Start = null;
    Button Stop = null;
    EditText Path = null;
    EditText Fre = null;
    TextView stepFind =  null;

    TextView TH = null;
    TextView VAR = null;
    float[] accelerometerValues=new float[3];
    float[] magneticFieldValues=new float[3];

    float[] MR=new float[9];
    double[] magneticF = new double[3];

    double azimuth = 0;
    double pitch = 0;
    double roll = 0;

    float[] orientValues = new float[3];

    float[] gravity=new float[3];
    final float alpha = (float) 0.8;

    private SensorManager MySensorManager;
    private Sensor gsensor;
    private Sensor msensor;
    private SensorEventListener mySensorListener;
    boolean isSD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    boolean can_write = false;
    boolean isStart = false;
    String vtos =null;
    double value = 0.0;
    int count = 1;
    String path = null;
    File file1,file2,file3;
    long start,end;
    int Frequency = 200000;


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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewX =(TextView) findViewById(R.id.XXX);
        textViewY =(TextView) findViewById(R.id.YYY);
        textViewZ =(TextView) findViewById(R.id.ZZZ);
        textViewC = (TextView)findViewById(R.id.Compound);
        Time = (TextView)findViewById(R.id.Time);
        Start = (Button) findViewById(R.id.StartButton);
        Stop = (Button)findViewById(R.id.StopButton);
        Path = (EditText)findViewById(R.id.Path);
        Fre = (EditText)findViewById(R.id.Frequency);
        stepFind =(TextView) findViewById(R.id.step);
        TH = (TextView) findViewById(R.id.TH);
        VAR = (TextView) findViewById(R.id.VAR);
        //DeleteFile("test.txt");
        CreateFiles();

        mySensorListener = new SensorEventListener (){
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {}
            @Override
            public void onSensorChanged(SensorEvent event) {

                if(isStart==true){
                    if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
                        magneticFieldValues = event.values;
                        writeDataTofile(magneticFieldValues,file1);
                    }
                    else if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                        gravity[0] = alpha*gravity[0]+(1-alpha)*event.values[0];
                        gravity[1] = alpha*gravity[1]+(1-alpha)*event.values[1];
                        gravity[2] = alpha*gravity[2]+(1-alpha)*event.values[2];

                        accelerometerValues[0] = event.values[0] - gravity[0];
                        accelerometerValues[1] = event.values[1] - gravity[1];
                        accelerometerValues[2] = event.values[2] - gravity[2];
                        // accelerometerValues=event.values;
                        float valueAcc  = (float) Math.sqrt(event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2]);
                     // float valueAcc  = (float) Math.sqrt(accelerometerValues[0]*accelerometerValues[0]+accelerometerValues[1]*accelerometerValues[1]+accelerometerValues[2]*accelerometerValues[2]);

                        DetectorNewStep(valueAcc);


                        //writeDataTofile(accelerometerValues,file1);




                    }
                    if(magneticFieldValues !=null && accelerometerValues!=null){
                        boolean success = SensorManager.getRotationMatrix(MR,null,accelerometerValues,magneticFieldValues);

                        if (success){
                            SensorManager.getOrientation(MR,orientValues);
                            azimuth = Math.toDegrees(orientValues[0]);
                            pitch = Math.toDegrees(orientValues[1]);
                            roll = Math.toDegrees(orientValues[2]);
                        //    Log.i("orientValues","azimuth:"+azimuth);
                        //    Log.i("orientValues","pitch:"+pitch);
                         //   Log.i("orientValues","roll:"+roll);
                        }

                    }
                    //transform(magneticFieldValues);
                    //view2(magneticF);
                    //writeDataTofile2(magneticF,file2);
                }
            }
        };



        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String time = GetTime();
                path = Path.getText().toString();
                CreateFile(path + ".txt");
                Button view = (Button) v;
                if(view.getId() == R.id.StartButton) {
                    //System.out.println(time);
                    //   String num= String.valueOf(count);
                    // WriteData("No."+num+"  Time:"+time+"\n");
                    isStart = true;
                    count++;
                    stepFind.setText(" Step : "+'0');
                    start =System.currentTimeMillis();
                    Toast.makeText(MainActivity.this,"Start Collecting",Toast.LENGTH_LONG).show();
                }
            }
        });
        Stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button view = (Button) v;
                if(view.getId() == R.id.StopButton)
                    isStart =false;
                    stepCoutn = 0;
                end =System.currentTimeMillis();
                Time.setText("Time Cost: "+(end-start)/1000);
                Toast.makeText(MainActivity.this,"Collection Completed",Toast.LENGTH_LONG).show();
            }
        });
    }
    //    private void DeleteFile(String filename){
//        file = new File(Environment.getExternalStorageDirectory(),filename);
//        if(file.exists()){
//            System.out.println("delete");
//            file.delete();
//        }
//    }
    private void CreateFile(String filename){
        File sd  = Environment.getExternalStorageDirectory();
        can_write = sd.canWrite();
       // System.out.println(can_write);
        file1 = new File(Environment.getExternalStorageDirectory()+"/"+"magneticData"+"/",filename);
        file2 = new File(Environment.getExternalStorageDirectory()+"/"+"magneticData2"+"/",filename);
        file3 = new File(Environment.getExternalStorageDirectory()+"/"+"acceleroData"+"/",filename);
    }
    private void CreateFiles(){
        String FilePath = Environment.getExternalStorageDirectory()+"/"+"magneticData"+"/";
        File Dir = new File(FilePath);
        String FilePath2 = Environment.getExternalStorageDirectory()+"/"+"magneticData2"+"/";
        File Dir2 = new File(FilePath2);
        String FilePath3 = Environment.getExternalStorageDirectory()+"/"+"acceleroData"+"/";
        File Dir3 = new File(FilePath3);
        if(!Dir.exists())
            Dir.mkdir();
        if(!Dir2.exists())
            Dir2.mkdir();
        if(!Dir3.exists())
            Dir3.mkdir();
    }
    private void WriteData(String msg,File file){
        if(isSD){
            try{
                if(can_write){
                    FileWriter fw = new FileWriter(file,true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(msg+"\n");
                    // bw.write("/n");
                    bw.flush();
                    bw.close();
//                    System.out.println("write");
                }
                else {
                    System.out.println("not write");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
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
/*
    private void register(){
        String FreStr = Fre.getText().toString();
        System.out.println(FreStr);
        if(FreStr!=null) {
            try{
                Frequency = Integer.parseInt(FreStr);
               // Frequency = Frequency*1000;
                }
            catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        MySensorManager.registerListener(mySensorListener,msensor,Frequency);
        MySensorManager.registerListener(mySensorListener,gsensor,Frequency);
        Log.i("SENSOR1.2","FREQUENCY:"+Frequency);
        // SensorManager.SENSOR_DELAY_NORMAL  200ms

              //  SENSOR_DELAY_NOMAL    (200000微秒)
              //  SENSOR_DELAY_UI       (60000微秒)
               // SENSOR_DELAY_GAME     (20000微秒)
               // SENSOR_DELAY_FASTEST  (0微秒)

    }*/
    void writeDataTofile2(double values[],File file){
        value = Math.sqrt(values[0] *values[0] + values[1] * values[1] +values[2] *values[2]);
        String gx = Double.toString(values[0]);
        String gy = Double.toString(values[1]);
        String gz = Double.toString(values[2]);
        WriteData(gx,file);
        WriteData(gy,file);
        WriteData(gz,file);
        vtos = Double.toString(value);
        WriteData(vtos,file);

    }
    void writeDataTofile(float values[],File file){
        textViewX.setText(" X : "+ values[0]);
        textViewY.setText(" Y : "+ values[1]);
        textViewZ.setText(" Z : "+ values[2]);
        value = Math.sqrt(values[0] *values[0] + values[1] * values[1] +values[2] *values[2]);
        textViewC.setText("XYZ: "+value);
        String gx = Float.toString(values[0]);
        String gy = Float.toString(values[1]);
        String gz = Float.toString(values[2]);
        WriteData(gx,file);
        WriteData(gy,file);
        WriteData(gz,file);
        vtos = Double.toString(value);
        WriteData(vtos,file);
    }
    void writeDataTofile3(float values[],File file){
        value = Math.sqrt(values[0] *values[0] + values[1] * values[1] +values[2] *values[2]);
        String gx = Float.toString(values[0]);
        String gy = Float.toString(values[1]);
        String gz = Float.toString(values[2]);
        WriteData(gx,file);
        WriteData(gy,file);
        WriteData(gz,file);
        vtos = Double.toString(value);
        WriteData(vtos,file);
    }

    private void transform(float magA[]){

        magneticF[0] = (Math.cos(roll)*Math.cos(azimuth)-Math.sin(pitch)*Math.sin(roll)*Math.sin(azimuth))*magA[0]+(-Math.cos(roll)*Math.sin(azimuth)-Math.sin(roll)*Math.sin(pitch)*Math.cos(azimuth))*magA[1] +(-Math.sin(roll)*Math.cos(pitch))*magA[2];
        magneticF[1] = (Math.cos(pitch)*Math.sin(azimuth))*magA[0]+(Math.cos(pitch)*Math.cos(azimuth))*magA[1]+(-Math.sin(pitch))*magA[2];
        magneticF[2] = (Math.sin(roll)*Math.cos(azimuth)+Math.cos(roll)*Math.sin(pitch)*Math.sin(azimuth))*magA[0]+(-Math.sin(roll)*Math.sin(azimuth)+Math.cos(roll)*Math.sin(pitch)*Math.cos(azimuth))*magA[1]+(Math.cos(roll)*Math.cos(pitch))*magA[2];

    }
    public void DetectorNewStep(float values) {
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
                    stepFind.setText(" Step : "+stepCoutn);
                    TH.setText(" TH : "+va);
                    VAR.setText(" VAR : "+ThreadValue);
                }
                if (timeOfNow - timeOfLastPeak >= 250
                        && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
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


    @Override
    protected void onResume() {
       // int DelayTime = 200000; //200ms
        MySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        msensor = MySensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gsensor = MySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        MySensorManager.registerListener(mySensorListener,msensor,SensorManager.SENSOR_DELAY_GAME );
        MySensorManager.registerListener(mySensorListener,gsensor,SensorManager.SENSOR_DELAY_GAME );
        super.onResume();
    }
    @Override
    protected void onPause(){
        MySensorManager.unregisterListener(mySensorListener);
        super.onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
