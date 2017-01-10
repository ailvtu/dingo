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

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends ActionBarActivity {

    TextView textViewX = null;
    TextView textViewY = null;
    TextView textViewZ = null;
    TextView textViewC = null;
    TextView textViewTime = null;
    Button StartButton = null;
    Button StopButton = null;
    EditText Path = null;
    EditText Fre = null;
    TextView textViewstepFind =  null;

    TextView textViewTH = null;
    TextView textViewVAR = null;

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

    boolean isStart = false;
    String path = null;
    long start,end;
    File magFile,acceFile;
    final pedometer stepDect = new pedometer();
    final writeFile FileHandle = new writeFile();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewX =(TextView) findViewById(R.id.XXX);
        textViewY =(TextView) findViewById(R.id.YYY);
        textViewZ =(TextView) findViewById(R.id.ZZZ);
        textViewC = (TextView)findViewById(R.id.Compound);
        textViewTime = (TextView)findViewById(R.id.Time);
        StartButton = (Button) findViewById(R.id.StartButton);
        StopButton = (Button)findViewById(R.id.StopButton);
        Path = (EditText)findViewById(R.id.Path);
        Fre = (EditText)findViewById(R.id.Frequency);
        textViewstepFind =(TextView) findViewById(R.id.step);
        textViewTH = (TextView) findViewById(R.id.TH);
        textViewVAR = (TextView) findViewById(R.id.VAR);
        FileHandle.CreateFiles();
        mySensorListener = new SensorEventListener (){
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {}
            @Override
            public void onSensorChanged(SensorEvent event) {

                if(isStart==true){
                    if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
                        magneticFieldValues = event.values;
                        textViewX.setText("X :"+magneticFieldValues[0]);
                        textViewY.setText("Y :"+magneticFieldValues[1]);
                        textViewZ.setText("Z :"+magneticFieldValues[2]);
                        //value = Math.sqrt(magneticFieldValues[0] *magneticFieldValues[0] + magneticFieldValues[1] * magneticFieldValues[1] +magneticFieldValues[2] *magneticFieldValues[2]);
                        FileHandle.writeDataTofile(magneticFieldValues,magFile);
                    }
                    else if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
                        gravity[0] = alpha*gravity[0]+(1-alpha)*event.values[0];
                        gravity[1] = alpha*gravity[1]+(1-alpha)*event.values[1];
                        gravity[2] = alpha*gravity[2]+(1-alpha)*event.values[2];

                        accelerometerValues[0] = event.values[0] - gravity[0];
                        accelerometerValues[1] = event.values[1] - gravity[1];
                        accelerometerValues[2] = event.values[2] - gravity[2];
                        float valueAcc  = (float) Math.sqrt(event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2]);
                        //float valueAcc  = (float) Math.sqrt(accelerometerValues[0]*accelerometerValues[0]+accelerometerValues[1]*accelerometerValues[1]+accelerometerValues[2]*accelerometerValues[2]);
                        int stepNum = stepDect.DetectorNewStep(valueAcc);
                        textViewstepFind.setText(" Step : "+stepNum);
                        FileHandle.writeDataTofile(accelerometerValues,acceFile);

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
                        // Log.i("orientValues","roll:"+roll);
                        }
                    }
                }
            }
        };
        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                path = Path.getText().toString();
                magFile = FileHandle.CreateFile(path + ".txt","mag");
                acceFile = FileHandle.CreateFile(path + ".txt","acce");
                Button view = (Button) v;
                if(view.getId() == R.id.StartButton) {
                    //System.out.println(time);
                    isStart = true;
                    textViewstepFind.setText(" Step : "+'0');
                    start =System.currentTimeMillis();
                    Toast.makeText(MainActivity.this,"Start Collecting",Toast.LENGTH_LONG).show();
                }
            }
        });
        StopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button view = (Button) v;
                if(view.getId() == R.id.StopButton)
                    isStart =false;
                end =System.currentTimeMillis();
                textViewTime.setText("Time Cost: "+(end-start)/1000);
                Toast.makeText(MainActivity.this,"Collection Completed",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void transform(float magA[]){

        magneticF[0] = (Math.cos(roll)*Math.cos(azimuth)-Math.sin(pitch)*Math.sin(roll)*Math.sin(azimuth))*magA[0]+(-Math.cos(roll)*Math.sin(azimuth)-Math.sin(roll)*Math.sin(pitch)*Math.cos(azimuth))*magA[1] +(-Math.sin(roll)*Math.cos(pitch))*magA[2];
        magneticF[1] = (Math.cos(pitch)*Math.sin(azimuth))*magA[0]+(Math.cos(pitch)*Math.cos(azimuth))*magA[1]+(-Math.sin(pitch))*magA[2];
        magneticF[2] = (Math.sin(roll)*Math.cos(azimuth)+Math.cos(roll)*Math.sin(pitch)*Math.sin(azimuth))*magA[0]+(-Math.sin(roll)*Math.sin(azimuth)+Math.cos(roll)*Math.sin(pitch)*Math.cos(azimuth))*magA[1]+(Math.cos(roll)*Math.cos(pitch))*magA[2];

    }
    @Override
    protected void onResume() {
        //  SENSOR_DELAY_NOMAL    (200000微秒)
        //  SENSOR_DELAY_UI       (60000微秒)
        // SENSOR_DELAY_GAME     (20000微秒)
        // SENSOR_DELAY_FASTEST  (0微秒)
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


