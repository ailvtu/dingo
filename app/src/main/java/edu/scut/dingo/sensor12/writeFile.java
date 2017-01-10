package edu.scut.dingo.sensor12;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Dash on 2017/1/10.
 */

public class writeFile {
    boolean isSD = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    boolean can_write = false;
    File fileName;
    public File CreateFile(String filename,String Type){
        File sd  = Environment.getExternalStorageDirectory();
        can_write = sd.canWrite();
        Log.i("The file write status:", String.valueOf(can_write));
        if (Type == "mag"){
            fileName = new File(Environment.getExternalStorageDirectory()+"/"+"magneticData"+"/",filename);
        }
       else if (Type == "acce"){
            fileName = new File(Environment.getExternalStorageDirectory()+"/"+"accelerData"+"/",filename);
        }
        return fileName;
    }
    public void CreateFiles(){
        String magFilePath = Environment.getExternalStorageDirectory()+"/"+"magneticData"+"/";
        File magDir = new File(magFilePath);
        String accelerFilePath = Environment.getExternalStorageDirectory()+"/"+"accelerData"+"/";
        File accelerDir = new File(accelerFilePath);
        if(!magDir.exists())
            magDir.mkdir();
        if(!accelerDir.exists())
            accelerDir.mkdir();
    }
    public void WriteData(String msg,File file){
        if(isSD){
            try{
                if(can_write){
                    FileWriter fw = new FileWriter(file,true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(msg+"\n");
                    bw.flush();
                    bw.close();
                }
                else {
                    System.out.println("not write");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    void writeDataTofile(float values[],File file){
        String xValue = Float.toString(values[0]);
        String yValue = Float.toString(values[1]);
        String zValue = Float.toString(values[2]);
        WriteData(xValue,file);
        WriteData(yValue,file);
        WriteData(zValue,file);
        float comValue  = (float) Math.sqrt(values[0]*values[0]+values[1]*values[1]+values[2]*values[2]);
        String compound = Double.toString(comValue);
        WriteData(compound,file);
    }
    /*
    void DeleteFile(String filename){
      File file = new File(Environment.getExternalStorageDirectory(),filename);
       if(file.exists()){
        System.out.println("delete");
           file.delete();
       }
    }*/

}
