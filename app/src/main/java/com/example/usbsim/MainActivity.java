package com.example.usbsim;


import com.jjoe64.graphview.*;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import android.view.View;

import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    GraphView graph = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graph = (GraphView) findViewById(R.id.view);
        graph.setTitle("USB Временная диаграмма");
    }
    private String nrzi(String message)
    {
        char currentState = '1';
        String code = "";
        for(int i = 0; i < message.length();i++)
        {
            if((message.charAt(i) != '0') && (message.charAt(i) != '1')) code += message.charAt(i);
            else if(message.charAt(i) == '0') { if(currentState == '0') currentState = '1'; else currentState = '0'; code += currentState;}
            else code += currentState;

        }
        return code;
    }
    private String invertString(String str)
    {
        String res = "";
        for(int i = 0; i < str.length(); i++)
        {
            if(str.charAt(i) == '0') res += 1;
            else if(str.charAt(i) == '1') res += 0;
            else res += str.charAt(i);
        }
        return res;
    }
    private String getPositiveSignal(String str)
    {
        String res = "";
        for(int i = 0; i < (str.length()-2); i++)
        {
            if(str.substring(i,i+3).equals("EOP"))
            {
                res += "00";
                i += 3;
            }
            else res += str.charAt(i);
        }
        return res;
    }
    private String getNegativeSignal(String str)
    {
        String res = "";
        String iStr = invertString(str);
        for(int i = 0; i < (iStr.length()-2); i++)
        {
            if(str.substring(i,i+3).equals("EOP"))
            {
                res += "00";
                i += 3;
            }
            else res += iStr.charAt(i);
        }
        return res;
    }
    private ArrayList<Integer> stringToSequence(String str)
    {
        ArrayList<Integer> arr = new ArrayList<Integer>();
        for(int i = 0; i < str.length(); i++)
        {
            if(str.charAt(i) == '0'){ arr.add(0); }
            else arr.add(1);
        }
        return arr;
    }
    private LineGraphSeries<DataPoint> sequenceToSeries(ArrayList<Integer> data)
    {
        //func(seq -> series)
        // input: ArrayList<Integer> data
        // output: LineGraphSeries<DataPoint> series1
        ArrayList<DataPoint> graphPoints = new ArrayList<DataPoint>();
        double yVar = 0.0;
        double xVar = 0.0;
        graphPoints.add(new DataPoint(xVar,yVar));
        xVar = xVar + 0.1;
        for(int i = 0; i < data.size(); i++)
        {
            if( data.get(i).equals(1) ) { yVar = 1.0; }
            else { yVar = 0.0; }
            graphPoints.add(new DataPoint(xVar,yVar));
            xVar = xVar + 0.8;
            graphPoints.add(new DataPoint(xVar,yVar));
            xVar = xVar + 0.2;
        }
        //ArrayList -> DataPoint[]
        DataPoint[] xx = new DataPoint[graphPoints.size()];
        for(int i = 0; i < graphPoints.size(); i++)
        {
            xx[i] = graphPoints.get(i);
        }
        LineGraphSeries<DataPoint> series1 = new LineGraphSeries<DataPoint>(xx);
        series1.setTitle("data");
        series1.setColor(Color.GREEN);
        return series1;
    }
    public void newTransmission(View view) {
        graph.removeAllSeries();
        final Random random = new Random();

        //1. set constants
        String FS = "FullSpeed";
        String HS = "HighSpeed";

        String directionIn = "IN";
        String directionOut = "OUT";
        String directionSetup = "Setup";

        String PID_Ack = "Данные получены без ошибок";
        String PID_Nak = "Устройство не готово";
        String PID_Stall = "Требуется устранение ошибки";

        String SyncFS = "00000001";
        String SyncHS = "00000000000000000000000000000001";

        String t_dirIn = "0001";
        String t_dirOut = "1001";
        String t_dirSetup = "1101";

        String d_pidHS = "0111";
        String d_pidFS = "0011";

        String h_pid_ack = "0010";
        String h_pid_nak = "1010";
        String h_pid_stall = "1110";

        //Входные данные
        String transactionType = "Передача массива данных";
        String direction;
        int rand = random.nextInt(3);
        if(rand == 0)
            direction = directionIn;
        else if(rand == 1)
            direction = directionOut;
        else
            direction = directionSetup;
        rand = random.nextInt(128);
        int DID = rand;
        rand = random.nextInt(16);
        int EPID = rand;
        String transactionResult;
        if(rand == 0)
            transactionResult = PID_Ack;
        else if(rand == 1)
            transactionResult = PID_Nak;
        else
            transactionResult = PID_Stall;
        RadioButton rbFS = (RadioButton) findViewById(R.id.radioButton);
        String speed;
        if(rbFS.isChecked())
            speed = FS;
        else
            speed = HS;

        TextView tvTransactionType = findViewById(R.id.textView);
        TextView tvDirection = findViewById(R.id.textView2);
        TextView tvDID = findViewById(R.id.textView3);
        TextView tvEPID = findViewById(R.id.textView4);
        TextView tvTransactionResult = findViewById(R.id.textView5);
        TextView tvSpeed = findViewById(R.id.textView6);
        TextView tvData = findViewById(R.id.textView7);
        TextView tvToketPacket = findViewById(R.id.textView8);
        TextView tvTokenNRZI = findViewById(R.id.textView9);
        TextView tvDataPacket = findViewById(R.id.textView10);
        TextView tvDataNRZI = findViewById(R.id.textView11);
        TextView tvHandshakePacket = findViewById(R.id.textView12);
        TextView tvHandshakeNRZI = findViewById(R.id.textView13);
        TextView tvTokenCRC = findViewById(R.id.textView14);
        TextView tvDataCRC = findViewById(R.id.textView15);
        TextView tvFinalPacket = findViewById(R.id.textView16);
        Button btnGenData = findViewById(R.id.button);
        tvTransactionType.setText("Тип передачи: " + transactionType);
        tvDirection.setText("Направление передачи: " + direction);
        tvDID.setText("Адрес устройства: " + String.valueOf(DID));
        tvEPID.setText("Номер EP: " + String.valueOf(EPID));
        tvTransactionResult.setText("Результат передачи: " + transactionResult);
        tvSpeed.setText("Скорость передачи: " + speed);
        btnGenData.setText("Сгенерировать новые данные");

        //2. Generate Data
        ArrayList<Integer> data = new ArrayList<Integer>();
        String dataStr = "";
        int dataPacketSize = 0;
        int fullSpeedSize = 64;
        int highSpeedSize = 1024;
        if (speed.equals(FS)) { dataPacketSize = random.nextInt(fullSpeedSize)+1; }
        else if (speed.equals(HS))  { dataPacketSize = random.nextInt(highSpeedSize)+1; }

        for(int i = 0; i < dataPacketSize; i++) { int r = random.nextInt(2); data.add(r); dataStr += r;}
        tvData.setText("Передаваемые данные: " + dataStr);

        //3. Create Token Packet + CRC
        String tokenPacket = "", sync, t_pid, t_pid_i, binDID, binEPID;
        if(speed.equals(FS))  sync = SyncFS;
        else if (speed.equals(HS))  sync = SyncHS;
        else sync = SyncFS;

        if(direction.equals(directionIn)) t_pid = t_dirIn;
        else if (direction.equals(directionOut)) t_pid = t_dirOut;
        else t_pid = t_dirSetup;

        t_pid_i = invertString(t_pid);

        binDID = Integer.toBinaryString(DID);
        if(binDID.length()<7) {
            int numOfCharsToAdd = 7-binDID.length();
            String newBinDID = "";
            while(numOfCharsToAdd>0){binDID+='0'; numOfCharsToAdd--;}
            binDID = newBinDID + binDID; }
        binEPID = Integer.toBinaryString(EPID);
        if(binEPID.length()<4) {
            int numOfCharsToAdd = 4-binEPID.length();
            String newBinEPID = "";
            while(numOfCharsToAdd>0){binEPID+='0'; numOfCharsToAdd--;}
            binEPID = newBinEPID + binEPID; }

        tokenPacket = sync + t_pid + t_pid_i + binDID + binEPID;

        BigInteger dividend = new BigInteger(tokenPacket, 2);
        BigInteger divisor = new BigInteger("100101", 2);
        BigInteger t_CRC = dividend.remainder(divisor);
        tvTokenCRC.setText("Token CRC: " + t_CRC.toString(2));

        tvToketPacket.setText("Token: " + tokenPacket + "CRCEOP");
        tvTokenNRZI.setText("NRZI: " + nrzi(tokenPacket));

        //4. Create Data Packet + CRC
        String dataPacket = "", d_pid, d_pid_i;
        if(speed.equals(FS))  d_pid = d_pidFS;
        else if (speed.equals(HS))  d_pid = d_pidHS;
        else d_pid = d_pidFS;
        d_pid_i = invertString(d_pid);
        dataPacket = sync + d_pid + d_pid_i + dataStr;

        dividend = new BigInteger(dataPacket, 2);
        divisor = new BigInteger("11000000000000101", 2);
        BigInteger d_CRC = dividend.remainder(divisor);
        tvDataCRC.setText("Data CRC: " + d_CRC.toString(2));
        tvDataPacket.setText("Data: \t" + dataPacket + "CRCEOP");
        tvDataNRZI.setText("NRZI:\t\t\t\t\t" + nrzi(dataPacket));

        //5. Create Handshake Packet
        String handshakePacket = "", h_pid, h_pid_i;
        if(transactionResult.equals(PID_Ack)) h_pid = h_pid_ack;
        else if (transactionResult.equals(PID_Nak)) h_pid = h_pid_nak;
        else h_pid = h_pid_stall;
        h_pid_i = invertString(h_pid);
        handshakePacket = sync + h_pid + h_pid_i;
        tvHandshakePacket.setText("Handshake: \t" + handshakePacket + "EOP");
        tvHandshakeNRZI.setText("NRZI:\t\t\t\t\t\t\t" + nrzi(handshakePacket));

        //6. Combine all into frame
        String finalPacket = nrzi(tokenPacket+t_CRC.toString(2))+"EOP"+nrzi(dataPacket+d_CRC.toString(2))+"EOP"+nrzi(handshakePacket)+"EOP";
        String D_plus = getPositiveSignal(finalPacket)+"\t\t D+";
        String D_minus = getNegativeSignal(finalPacket)+"\t\t D-";
        tvFinalPacket.setText(D_plus);

        RadioButton rbDPlus = (RadioButton) findViewById(R.id.radioButton3);
        RadioButton rbDMinus = (RadioButton) findViewById(R.id.radioButton4);
        //7. Add Frame Lines into Graph
        if(! rbDPlus.isChecked()) {
            LineGraphSeries<DataPoint> series1 = sequenceToSeries(stringToSequence(D_plus));
            series1.setTitle("D+");
            series1.setColor(Color.RED);
            series1.setThickness(3);
            graph.addSeries(series1);
        }
        if(! rbDMinus.isChecked()) {
            LineGraphSeries<DataPoint> series2 = sequenceToSeries(stringToSequence(D_minus));
            series2.setTitle("D-");
            series2.setColor(Color.BLUE);
            series2.setThickness(3);
            graph.addSeries(series2);
        }
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(1);
        graph.getLegendRenderer().setVisible(true);
    }
}