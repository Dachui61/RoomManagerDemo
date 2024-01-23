package com.lidaxin.serial;

import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.lidaxin.data.ComBean;
import com.lidaxin.data.MyFunc;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.BaseSerialPort;
import android_serialport_api.SerialPort;

/**
 * @author benjaminwan
 *串口辅助工具类
 */
public abstract class SerialHelper {
    private BaseSerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private SendThread mSendThread;
    private String sPort= null;
    private int iBaudRate=9600;
    private boolean _isOpen=false;
    private byte[] _bLoopData=new byte[]{0x30};
    private int iDelay=500;
    //----------------------------------------------------
    public SerialHelper(String sPort, int iBaudRate){
        this.sPort = sPort;
        this.iBaudRate=iBaudRate;
    }
    public SerialHelper(){
        this("/ttyS0",9600);
    }
    public SerialHelper(String sPort){
        this(sPort,9600);
    }
    public SerialHelper(String sPort, String sBaudRate){
        this(sPort,Integer.parseInt(sBaudRate));
    }
    //----------------------------------------------------
    public void open()  {
//        LogFileUtil.saveLog("open: "+ sPort+"  "+iBaudRate+"  "+ Build.MODEL);
        try {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                mSerialPort =  new SerialPort(new File(sPort), iBaudRate, 0);
            } else{
                mSerialPort =  new com.yishengkj.testtools.utils.SerialPort(new File(sPort), iBaudRate, 0);
            }
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mReadThread = new ReadThread();
            mReadThread.start();
            mSendThread = new SendThread();
            mSendThread.setSuspendFlag();
            mSendThread.start();
            _isOpen=true;
            Log.i("串口通信","成功打开串口");
        }catch (Exception e){
 //           LogFileUtil.saveLog("打开串口失败: "+e.toString());
        }catch (Error error){
  //          LogFileUtil.saveLog("打开串口失败: "+error.toString());
        }

    }
    //----------------------------------------------------
    public void close(){
   //     LogFileUtil.saveLog("close: "+ sPort+"  "+iBaudRate);
        if (mReadThread != null){
            mReadThread.interrupt();
            mReadThread = null;
        }
        if (mSerialPort != null) {
            mSerialPort.close();
            mSerialPort = null;
        }
        _isOpen=false;
    }
    //----------------------------------------------------
    public void send(byte[] bOutArray){
        try
        {
    //        LogFileUtil.saveLog("write1"+ Arrays.toString(bOutArray));
            if(mOutputStream != null){
                mOutputStream.write(bOutArray);
                mOutputStream.flush();
     //           LogFileUtil.saveLog("write2");
            }
        } catch (Exception e)
        {
            e.printStackTrace();
    //        LogFileUtil.saveLog("e: "+e.toString());
        }
    }

    private static final String TAG = "SerialHelper";
    //----------------------------------------------------
    public void sendHex(String sHex){
    //    LogFileUtil.saveLog("sendHex: "+sHex);
        byte[] bOutArray = MyFunc.HexToByteArr(sHex);
        send(bOutArray);
    }
    //----------------------------------------------------
    public void sendTxt(String sTxt){
    //    LogFileUtil.saveLog("sendTxt: "+sTxt);
        byte[] bOutArray =sTxt.getBytes();
        send(bOutArray);
    }
    //----------------------------------------------------
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                try
                {
                    if (mInputStream == null) {
           //             LogFileUtil.saveLog("mInputStream == null");
                        return;
                    }
                    byte[] buffer = new byte[512];
//                    int size = mInputStream.read(buffer);
//                    LogFileUtil.saveLog("ReadThread: ");
                    int size = mInputStream.available();
//                    LogFileUtil.saveLog("ReadThread: "+size);
                    if (size > 0){
                        size = mInputStream.read(buffer);
                        ComBean ComRecData = new ComBean(sPort,buffer,size);
                        onDataReceived(ComRecData);
                    }
                   SystemClock.sleep(50);
                } catch (Exception e)
                {
 //                   LogFileUtil.saveLog("e: "+e.toString());
                    e.printStackTrace();
                    return;
                }catch (Error error){
 //                   LogFileUtil.saveLog("e: "+error.toString());
                    error.printStackTrace();
                    return;
                }
            }
        }
    }
    //----------------------------------------------------
    private class SendThread extends Thread{
        public boolean suspendFlag = false;// 控制线程的执行
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                synchronized (this)
                {
    //                LogFileUtil.saveLog("suspendFlag: "+suspendFlag);
                    while (suspendFlag)
                    {
                        SystemClock.sleep(50);
                    }
                }
                send(getbLoopData());
                try
                {
                    Thread.sleep(iDelay);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        //线程暂停
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }

        //唤醒线程
        public synchronized void setResume() {
            this.suspendFlag = false;
            notify();
        }
    }
    //----------------------------------------------------
    public int getBaudRate()
    {
        return iBaudRate;
    }
    public boolean setBaudRate(int iBaud)
    {
 //       LogFileUtil.saveLog("设置波特率: "+iBaud+"   "+_isOpen);
        if (_isOpen)
        {
            return false;
        } else
        {
            iBaudRate = iBaud;
            return true;
        }
    }
    public boolean setBaudRate(String sBaud)
    {
        int iBaud = Integer.parseInt(sBaud);
        return setBaudRate(iBaud);
    }
    //----------------------------------------------------
    public String getPort()
    {
        return sPort;
    }
    public boolean setPort(String sPort)
    {
        if (_isOpen)
        {
            return false;
        } else
        {
            this.sPort = sPort;
            return true;
        }
    }
    //----------------------------------------------------
    public boolean isOpen()
    {
        return _isOpen;
    }
    //----------------------------------------------------
    public byte[] getbLoopData()
    {
        return _bLoopData;
    }
    //----------------------------------------------------
    public void setbLoopData(byte[] bLoopData)
    {
        this._bLoopData = bLoopData;
    }
    //----------------------------------------------------
    public void setTxtLoopData(String sTxt){
        this._bLoopData = sTxt.getBytes();
    }
    //----------------------------------------------------
    public void setHexLoopData(String sHex){
        this._bLoopData = MyFunc.HexToByteArr(sHex);
    }
    //----------------------------------------------------
    public int getiDelay()
    {
        return iDelay;
    }
    //----------------------------------------------------
    public void setiDelay(int iDelay)
    {
        this.iDelay = iDelay;
    }
    //----------------------------------------------------
    public void startSend()
    {
        if (mSendThread != null)
        {
            mSendThread.setResume();
        }
    }
    //----------------------------------------------------
    public void stopSend()
    {
        if (mSendThread != null)
        {
            mSendThread.setSuspendFlag();
        }
    }

    public void initOutputStream() {
        mOutputStream = mSerialPort.getOutputStream();
    }

    public OutputStream getmOutputStream() {
        return mOutputStream;
    }

    //----------------------------------------------------
    protected abstract void onDataReceived(ComBean ComRecData);
}