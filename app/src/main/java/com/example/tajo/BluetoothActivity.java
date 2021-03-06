package com.example.tajo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class BluetoothActivity  extends AppCompatActivity {
    Switch mSwBlutooth;
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;
    Handler mBluetoothHandler;
    TextView connectName;
    TextView practiceTV;
    TextView greetMent;
    Button examplebutton;
    Button timerbutton;
    static boolean flag;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");    //아두이노 연결

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //메인 액티비티 최초 생성 시 호출됨
        // 전역으로 선언한 버튼, 텍스트뷰 등을 findviewById 메서드를 통해 참조시킴


        flag = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        examplebutton = (Button) findViewById(R.id.examplebutton);
        timerbutton = (Button) findViewById(R.id.timerbutton);

        greetMent = (TextView) findViewById(R.id.greetMent);
        String userEmail = user.getEmail();
        String id = userEmail.substring(0, userEmail.indexOf("@"));
        greetMent.setText("안녕하세요"+id+"님\nTAJO입니다.");
        connectName = (TextView) findViewById(R.id.connectName);
        practiceTV= (TextView) findViewById(R.id.practiceTV);
        mSwBlutooth = (Switch) findViewById(R.id.bt_switch);
        mSwBlutooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    // switchButton이 체크된 경우
                    listPairedDevices();
                } else {
                    // switchButton이 체크되지 않은 경우
                    bluetoothOff();
                }
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();   //해당 장치가 블루투스기능을 지원하는 지 알아오는 메서드
        bluetoothOn();

        mBluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    practiceTV.setText(readMessage);
                    String donanFlag = practiceTV.getText().toString();
                    String donanFlagg = (donanFlag.substring(0,1));
                    //Log.d("MyTag","아여기까지했다고...");
                    //Log.d("MyTag",donanFlagg);

                    if (donanFlagg.equals("1")) {
                        Log.d("MyTag","1받아옴..");
                        // 블루투스에 1받음 -> 도난 -> 팝업창
                        if (flag == false) {
                            flag = true;
                            CDT.start();    // 타이머 시작
                            //Log.d("MyTag","찐으로cdt시작");

                            Intent intent = new Intent(BluetoothActivity.this, PopupActivity.class);
                            intent.putExtra("data","Test popup");
                            startActivityForResult(intent,1);

                        }
                    }
                    else if (donanFlagg.equals("2")) {
                        // 블루투스에 2받음 -> 타이머 멈춤, flag 원래대로
                        //Log.d("MyTag","찐으로cdt멈춤");
                        CDT.cancel();   // 타이머 멈춤
                        flag = false;
                    }
                    else{
                        Log.d("MyTag","엿먹어라");
                        //Log.d("MyTag",donanFlagg);
                    }
                }
            }
        };

        timerbutton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // timer버튼 누르면 수행 할 명령 : 도난 감지, 타이머 시작
                if (flag == false) {
                    flag = true;
                    CDT.start();
                    Log.d("MyTag","button으로cdt시작");

                    Intent intent = new Intent(BluetoothActivity.this, PopupActivity.class);
                    startActivityForResult(intent,1);
                }
            }
        });


        examplebutton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // example 버튼 누르면 수행 할 명령 : 반납, 타이머 종료, flag 원래대로
                Log.d("MyTag","button으로cdt종료");
                CDT.cancel();
                flag = false;
            }
        });

    }

    CountDownTimer CDT = new CountDownTimer(10 * 1000, 1000) {  //10초동안 1초마다 실행
        public void onTick(long millisUntilFinished) {
            //반복실행할 구문 : 시간 가는 중
            Log.d("MyTag","cdt시간가는중");

        }
        public void onFinish() {
            //마지막에 실행할 구문 : 시간 다 됨
            Log.d("MyTag","db올리기");
            databaseReference.child("DonanId").push().setValue(user.getEmail());
        }
    };

    void bluetoothOn() {
        //ON버튼 누르면 동작하는 메서드
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "블루투스를 활성화 합니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }
    void bluetoothOff() {
        //OFF버튼 누르면 동작하는 메서드
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            connectName.setText(" ");
        }
        else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //ON메서드에서 Intent로 받은 결과를 처리하는 메서드
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            //블루투스 활성화 확인
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                //페어링된 장치 존재 시 새로운 알림창 객체 생성하여 장치선택 타이틀과 각 페어링된 장치명 추가
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void connectSelectedDevice(String selectedDeviceName) {
        //블루투스 연결 메서드
        //listPairedDevices메서드를 통해 전달받은 값은 장치의 이름이고 실제 연결에 필요한 것은 장치의 주소임
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                connectName.setText(tempDevice.getName());
                Log.d("MyTag","connectName해짜나");
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            if(mThreadConnectedBluetooth != null) {
                Log.d("MyTag","요기");
                mThreadConnectedBluetooth.write("2");
                Log.d("MyTag","짜잔");
            }
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
        //mBluetoothDevice를 통해 createRfcommSocketToServiceRecord(UUID)를 호출하여 mBluetoothSocket을 가지고 옴
        //UUID는 시리얼 통신용
        //mBluetoothSocket 초기화, connect()호출 연결
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            //데이터 전송 및 수신하는 길 만드는 작업
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                //데이터 항상 확인을 위해서 while문
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        // 수신 가능
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                        //원래 여기있던게 handler로 옮김
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
                Log.d("MyTag","write갔다진짜");
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }
    }
}