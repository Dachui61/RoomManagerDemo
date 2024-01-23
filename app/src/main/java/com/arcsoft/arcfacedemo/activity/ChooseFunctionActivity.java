package com.arcsoft.arcfacedemo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.arcsoft.arcfacedemo.API.ApiService;
import com.arcsoft.arcfacedemo.Adapter.UpcomingAppointmentsAdapter;
import com.arcsoft.arcfacedemo.DAO.AppointmentDAO;
import com.arcsoft.arcfacedemo.DAO.UserDAO;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.Constants;
import com.arcsoft.arcfacedemo.model.Appointment;
import com.arcsoft.arcfacedemo.model.User;
import com.arcsoft.arcfacedemo.yingjian.controller.LightController;
import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.RuntimeABI;
import com.google.zxing.ResultPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;  // 使用旧版的 AppCompatActivity
import com.google.zxing.Result;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.lidaxin.data.ComBean;
import com.lidaxin.serial.SerialHelper;


public class ChooseFunctionActivity extends BaseActivity implements FaceRecognitionFragment.OnCloseFragmentListener {
    // 在 ChooseFunctionActivity 类中添加一个全局变量
    private boolean isEngineActivated = false;

    private static final String TAG = "ChooseFunctionAct ivity";
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    // 在线激活所需的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE
    };
    boolean libraryExists = true;
    // Demo 所需的动态库文件
    private static final String[] LIBRARIES = new String[]{
            // 人脸相关
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            // 图像库相关
            "libarcsoft_image_util.so",
    };
    // 修改配置项的对话框
    //音频
    private MediaPlayer mediaPlayer;
    private TextView currentTimeTextView;

    private DatabaseHelper databaseHelper;
    private Handler handler = new Handler(Looper.getMainLooper());

    private TextView CurrentDate;

    private TextView DayOfWeek;
    private EditText cardNumberEditText;
    private static final long DELAY_MILLIS = 500;
    private Handler handler1 = new Handler();
    private static final long UPDATE_INTERVAL = 1000; // 更新间隔，单位毫秒
    private Runnable updateMeetingStatusRunnable = new Runnable() {
        @Override
        public void run() {
            updateMeetingStatus();
            handler1.postDelayed(this, UPDATE_INTERVAL);
        }
    };
    private View meetingRoomImage;
    private View scanLayout;
    private DecoratedBarcodeView barcodeScannerView;

    //扫码关闭的定时器
    private static final long SCAN_TIMEOUT_MILLIS = 10000; // 设置扫描超时时间为5秒
    private Handler scanTimeoutHandler = new Handler();
    //打开串口用的类
    SerialHelper serialHelper = new SerialHelper("/dev/ttyS3", "9600") {
        @Override
        protected void onDataReceived(ComBean ComRecData) {

        }
    };

    //更多设置按钮
    private Button btnMoreSettings;

    //MyManager manager = MyManager.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_function);

        // 隐藏导航栏和状态栏
        hideSystemUI();

        //拿到展示时间的文本编辑框
        currentTimeTextView = findViewById(R.id.currentTime);

        CurrentDate=findViewById(R.id.currentDate);

        DayOfWeek = findViewById(R.id.dayOfWeek);

        // 使用 Handler 定时更新时间(显示在界面中的时间)
        handler.postDelayed(timeUpdater, UPDATE_INTERVAL);
        //开启当前会议室是否在使用中的定时任务
        handler.postDelayed(updateMeetingStatusRunnable, UPDATE_INTERVAL);
        //更新预约信息界面的异步操作
        handler.postDelayed(updateListViewRunnable,UPDATE_INTERVAL);


        //刷卡
        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        cardNumberEditText.setInputType(InputType.TYPE_NULL);
        cardNumberEditText.requestFocus();
        cardNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 移除之前的比对任务
                handler.removeCallbacksAndMessages(null);

                // 延迟触发比对任务
                handler.postDelayed(() -> {
                    String cardNumber = editable.toString();
                    // 检查文本是否为空，如果为空则不进行比对
                    if (!TextUtils.isEmpty(cardNumber)) {
                        boolean compareResult = compareCardWithDatabase(cardNumber);
                        handleComparisonResult(compareResult);
                    }
                }, DELAY_MILLIS);
            }
        });

        //扫码

        meetingRoomImage = findViewById(R.id.meetingRoomImage);
        scanLayout = findViewById(R.id.scanLayout);
        barcodeScannerView = findViewById(R.id.barcodeScannerView);

        Button scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换扫码区域和图片的可见性
                if (scanLayout.getVisibility() == View.VISIBLE) {
                    scanLayout.setVisibility(View.INVISIBLE);
                    stopScanner();
                } else {
                    scanLayout.setVisibility(View.VISIBLE);
                    startScanner();
                }
            }
        });


        //快速预约按钮配置
        Button btnLocalReservation = findViewById(R.id.localReservationButton);
        btnLocalReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示日期、时间和时长选择器
                //showDateTimePicker();
                ((MyApplication) getApplication()).setMeetingMode(2);
                showFaceRecognitionFragment();
            }
        });

        //增加当前会议时长
        Button extendTime = findViewById(R.id.extendButton);
        extendTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyApplication) getApplication()).setMeetingMode(3);
                showFaceRecognitionFragment();
                //showExtendOptions();
            }
        });

        Button extendTime2 = findViewById(R.id.extendButton2);
        extendTime2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyApplication) getApplication()).setMeetingMode(4);
                showFaceRecognitionFragment();
                //showExtendOptions();
            }
        });

        Button extendTime3 = findViewById(R.id.extendButton3);
        extendTime3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyApplication) getApplication()).setMeetingMode(5);
                showFaceRecognitionFragment();
                //showExtendOptions();
            }
        });

        //终止当前会议
        Button endTime = findViewById(R.id.endButton);
        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                terminateCurrentMeeting();
            }
        });

        //更多设置按钮
        btnMoreSettings = findViewById(R.id.btnMoreSettings);

        // 这里是点击事件触发弹出界面
        btnMoreSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });
        //删除数据库用户信息

        databaseHelper = new DatabaseHelper(this);

        UserDAO userDAO = new UserDAO(this);

        userDAO.deleteAllUsers();


        //删掉之前数据库中的预约信息
        AppointmentDAO appointmentDAO = new AppointmentDAO(this);

        appointmentDAO.deleteAllAppointments();

        //拿到服务器数据
        fetchDataFromServer();


        libraryExists = checkSoFile(LIBRARIES);
        ApplicationInfo applicationInfo = getApplicationInfo();
        Log.i(TAG, "onCreate: " + applicationInfo.nativeLibraryDir);
        if (!libraryExists) {
            showToast(getString(R.string.library_not_found));
        }else {
            VersionInfo versionInfo = new VersionInfo();
            int code = FaceEngine.getVersion(versionInfo);
            Log.i(TAG, "onCreate: getVersion, code is: " + code + ", versionInfo is: " + versionInfo);
        }
    }

    private void showPopup(View anchorView) {
        // 加载弹出界面的布局
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_content, null);

        // 创建PopupWindow实例
        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        // 设置PopupWindow的位置
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int offsetX = 150;
        int offsetY = 150;
        popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, location[0] + offsetX, location[1] + anchorView.getHeight() + offsetY);

        // 设置按钮的点击事件，传递popupWindow对象
        setupButtonListeners(popupView, popupWindow);

        // 这里可以为弹出界面的元素设置监听器等
    }

    private void setupButtonListeners(View popupView, final PopupWindow popupWindow) {


        Button btnRegisterFace = popupView.findViewById(R.id.btnRegisterFace);
        btnRegisterFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MyApplication) getApplication()).setRegisterNum(true);
                showFaceRecognitionFragment();
            }
        });

        Button btnShutdown = popupView.findViewById(R.id.btnShutdown);
        btnShutdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 处理系统关机按钮点击事件
            }
        });

        // 获取 AudioManager 实例
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Button btnVolumeControl = popupView.findViewById(R.id.btnVolumeControl);

        // 获取SeekBar实例
        SeekBar volumeSeekBar = popupView.findViewById(R.id.volumeSeekBar);

        btnVolumeControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 显示音量控制的SeekBar
                volumeSeekBar.setVisibility(View.VISIBLE);

                // 设置SeekBar的监听器，用于调整音量
                volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // 根据SeekBar的进度调整音量
                        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int adjustedVolume = (int) (progress / 100.0 * maxVolume);
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, adjustedVolume, 0);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // 用户开始拖动SeekBar时的操作（可选）
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // 用户停止拖动SeekBar时的操作（可选）
                    }
                });
            }
        });

        Button btnBrightnessControl = popupView.findViewById(R.id.btnBrightnessControl);
        btnBrightnessControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 处理控制屏幕亮度按钮点击事件
            }
        });

        Button btnBack = popupView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 关闭弹窗
                popupWindow.dismiss();
            }
        });

        // 添加其他按钮的点击事件
    }



    //更新预约信息
    private Runnable updateListViewRunnable = new Runnable() {
        @Override
        public void run() {
            updateListView();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    private  void updateListView() {
        AppointmentDAO appointmentDAO = new AppointmentDAO(ChooseFunctionActivity.this);
        // 获取最近一次预约信息
        Appointment latestReservation = appointmentDAO.getLatestReservation();
        // 获取其他预约信息
        if(latestReservation!=null) {
            List<Appointment> otherAppointmentsList = appointmentDAO.getOtherAppointments(latestReservation.getAppointmentID());
            //设置最近一次预约信息的 TextView
            setLatestReservationTextView(latestReservation);
            // 设置其他预约信息的适配器
            UpcomingAppointmentsAdapter otherAppointmentsAdapter = new UpcomingAppointmentsAdapter(ChooseFunctionActivity.this, otherAppointmentsList);
            ListView listView = findViewById(R.id.upcomingAppointmentsListView);
            listView.setAdapter(otherAppointmentsAdapter);
        }
    }

    //终止当前会议
    private void terminateCurrentMeeting() {
        ((MyApplication) getApplication()).setMeetingMode(1);
        showFaceRecognitionFragment();

    }
    // 获取当天日期的方法
    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
    private void fetchDataFromServer() {
        // 替换为你的服务器API地址
        String AppoinmentUrl = "http://113.250.189.122:8090/reservationInfo/getInfo?dateTime="+getCurrentDate()+"&serialNo="+getDeviceSerialNumber();

        String UserUrl = "http://113.250.189.122:8090/user/getInfo/2023-12-04/";
        // 创建并执行异步任务
        new FetchAppointmentDataTask().execute(AppoinmentUrl);

        // 创建并执行异步任务
        new FetchUserDataTask().execute(UserUrl);
    }

    // 获取设备序列号
    public static String getDeviceSerialNumber() {
        return Build.SERIAL;
    }
    private class FetchAppointmentDataTask extends AsyncTask<String, Void, List<Appointment>> {
        // doInBackground 方法在后台线程中执行
        @Override
        protected List<Appointment> doInBackground(String... params) {
            String apiUrl = params[0];

            // 初始化 MyApiClient
            MyApiClient apiClient = new MyApiClient();

            // 获取预约信息
            String appointmentData = apiClient.fetchDataFromServer(apiUrl);

            if (appointmentData != null) {
                // 解析预约信息
                return MyJsonParser.parseAppointmentData(appointmentData);
            } else {
                // 处理获取预约信息失败的情况，例如返回空列表或其他默认值
                return new ArrayList<>();
            }
        }

        // onPostExecute 方法在主线程中执行，用于处理解析后的数据
        @Override
        protected void onPostExecute(List<Appointment> appointments) {
            super.onPostExecute(appointments);

            // 在这里处理解析后的预约数据
            if (!appointments.isEmpty()) {
                // 插入预约信息到本地数据库
                // 这里假设有一个名为 appointmentDAO 的 AppointmentDAO 实例
                AppointmentDAO appointmentDAO = new AppointmentDAO(ChooseFunctionActivity.this);
                // 你需要根据你的代码进行调整
                for (Appointment appointment : appointments) {
                    appointmentDAO.insertAppointment(appointment);
                }
                // 获取所有预约信息
                List<Appointment> allAppointments = appointmentDAO.getAllAppointments();
                // 获取最近一次预约信息
                Appointment latestReservation = appointmentDAO.getLatestReservation();
                if(latestReservation != null){
                // 获取其他预约信息
                    List<Appointment> otherAppointmentsList = appointmentDAO.getOtherAppointments(latestReservation.getAppointmentID());
                //设置最近一次预约信息的 TextView
                setLatestReservationTextView(latestReservation);
                // 设置其他预约信息的适配器
                UpcomingAppointmentsAdapter otherAppointmentsAdapter = new UpcomingAppointmentsAdapter(ChooseFunctionActivity.this, otherAppointmentsList);
                ListView listView = findViewById(R.id.upcomingAppointmentsListView);
                listView.setAdapter(otherAppointmentsAdapter);
                otherAppointmentsAdapter.notifyDataSetChanged();
                }
            } else {
            }
        }
    }

    private class FetchUserDataTask extends AsyncTask<String, Void, List<User>> {
        // doInBackground 方法在后台线程中执行
        @Override
        protected List<User> doInBackground(String... params) {
            String apiUrl = params[0];

            // 在适当的地方初始化 MyApiClient
            MyApiClient apiClient = new MyApiClient();

            // 获取预约信息
            String UserData = apiClient.fetchDataFromServer(apiUrl);

            if (UserData != null) {
                // 解析预约信息
                return MyJsonParser.parseUserData(UserData);
            } else {
                // 处理获取预约信息失败的情况，例如返回空列表或其他默认值
                return new ArrayList<>();
            }
        }

        // onPostExecute 方法在主线程中执行，用于处理解析后的数据
        @Override
        protected void onPostExecute(List<User> users) {
            super.onPostExecute(users);

            // 在这里处理解析后的预约数据
            if (!users.isEmpty()) {
                // 插入预约信息到本地数据库
                UserDAO userDAO = new UserDAO(ChooseFunctionActivity.this);

                for (User user : users) {
                    userDAO.insertUser(user);
                }

                int num = userDAO.getUserCount();

                showAppointmentCountDialog(ChooseFunctionActivity.this, num);


            } else {
                // 处理获取预约信息失败的情况
            }
        }
    }


    private boolean compareCardWithDatabase(String cardNumber) {

        UserDAO userDAO = new UserDAO(this); // 假设有一个合适的上下文 context
        List<User> userList = userDAO.getAllUsers();

        for (User user : userList) {
            if (user.getCardNumber().equals(cardNumber)) {
                // 找到匹配的卡号，表示匹配成功
                return true;
            }
        }

        // 未找到匹配的卡号，表示匹配失败
        return false;
    }
    private void handleComparisonResult(boolean compareResult) {
        if (compareResult) {
            playSuccessSound();
            // 匹配成功的逻辑，例如显示对应信息
            Toast.makeText(this, "匹配成功,请进！", Toast.LENGTH_SHORT).show();
        } else {
            playShuakaSoundFalsed();
            // 匹配失败的逻辑，例如提示用户重新刷卡
            Toast.makeText(this, "匹配失败，请重新刷卡", Toast.LENGTH_SHORT).show();
        }

        cardNumberEditText.setText("");

        cardNumberEditText.requestFocus();
    }
    private void playShuakaSoundFalsed() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.shuakashibai);
            mediaPlayer.setOnCompletionListener(mp -> {
                // 在音频播放完成后执行一些操作，如果需要的话
                // 例如，释放 MediaPlayer 资源
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
        }

        // 播放音频
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }
    private void playSuccessSound() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.shuaka);
            mediaPlayer.setOnCompletionListener(mp -> {
                // 在音频播放完成后执行一些操作，如果需要的话
                // 例如，释放 MediaPlayer 资源
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
        }

        // 播放音频
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    //展示当前是否有会议正在进行中
    private void updateMeetingStatus() {
        TextView meetingStatusTextView = findViewById(R.id.meetingStatusTextView);
        TextView meetingStatusTextView1 = findViewById(R.id.meetingStatusTextView1);
        TextView meetingStatusTextView2 = findViewById(R.id.meetingStatusTextView2);

        // 获取当前时间
        Calendar currentTime = Calendar.getInstance();

        // 假设有一个方法获取当前正在进行的会议信息
        Appointment currentMeeting = getCurrentMeeting(currentTime);

        // 获取布局中的相关视图
        FrameLayout meetingStatusLayout = findViewById(R.id.meetingStatusLayout);
        ImageView meetingRoomImage = findViewById(R.id.meetingRoomImage);

        // 获取延长会议和终止会议按钮
        Button extendButton = findViewById(R.id.extendButton);
        Button endButton = findViewById(R.id.endButton);
        Button extendButton2 = findViewById(R.id.extendButton2);
        Button extendButton3 = findViewById(R.id.extendButton3);

        Button btnLocalReservation = findViewById(R.id.localReservationButton);

        Button FaceRecognitionButton = findViewById(R.id.faceRecognitionButton);

        if (currentMeeting != null) {
            // 如果会议尚未开始，则标记为已经开始，并启动取消会议的定时器
            if (!(((MyApplication) getApplication()).isMeetingStarted())) {
                ((MyApplication) getApplication()).setMeetingStarted(true);
                startCancelMeetingTimer();
            }
            // 如果有正在进行的会议
            String statusText = currentMeeting.getTitle();
            meetingStatusTextView.setText(statusText);

            meetingStatusTextView1.setText("会议正在进行中");

            long remainingTimeMillis = calculateRemainingTime(currentTime, currentMeeting.getEndTime());
            String remainingTimeText = formatTime(remainingTimeMillis);
            meetingStatusTextView2.setText("预计" + remainingTimeText+"后结束");

            //打开蓝灯
            serialHelper.open(); // 打开串口通信
            serialHelper.sendHex("AA13010355"); // 发送十六进制指令
            serialHelper.close();

            FaceRecognitionButton.setText("会议签到");

            // 显示会议状态布局，隐藏会议室照片
            meetingStatusLayout.setVisibility(View.VISIBLE);
            meetingRoomImage.setVisibility(View.GONE);

            extendButton.setVisibility(View.VISIBLE);
            extendButton2.setVisibility(View.VISIBLE);
            extendButton3.setVisibility(View.VISIBLE);

            endButton.setVisibility(View.VISIBLE);

            btnLocalReservation.setVisibility(View.INVISIBLE);
        } else {
            ((MyApplication) getApplication()).setMeetingStarted(false);
            // 如果没有正在进行的会议
            meetingStatusTextView.setText("当前会议：未开始");

            FaceRecognitionButton.setText("人脸识别");

            LightController lightController = new LightController(this);
            lightController.closeLightAll();

            //打开蓝灯
            serialHelper.open(); // 打开串口通信
            serialHelper.sendHex("AA13010255"); // 发送十六进制指令
            serialHelper.close();

            // 显示会议室照片，隐藏会议状态布局
            meetingStatusLayout.setVisibility(View.GONE);
            meetingRoomImage.setVisibility(View.VISIBLE);

            // 隐藏延长会议和终止会议按钮
            extendButton.setVisibility(View.GONE);
            endButton.setVisibility(View.GONE);
            extendButton2.setVisibility(View.GONE);
            extendButton3.setVisibility(View.GONE);

            btnLocalReservation.setVisibility(View.VISIBLE);
        }
    }

    // 用来开始计时30分钟
    private void startCancelMeetingTimer() {
        handler.postDelayed(cancelMeetingTask, 1 * 60 * 1000); // 30分钟后执行
    }

    // 用于终止计时30分钟
    private void cancelCancelMeetingTimer() {
        handler.removeCallbacks(cancelMeetingTask);
    }

    private Runnable cancelMeetingTask = new Runnable() {
        @Override
        public void run() {
            // 在这里通过判断灯光状态来检查是否有人员进入会议
            if (isLightOn()) {
                // 如果灯光状态表明有人员进入，取消定时任务
                cancelCancelMeetingTimer();
            } else {
                // 如果灯光状态表明没有人员进入，将正在进行的会议的结束时间设为当前时间

                // 获取当前的时间
                Calendar currentTime = Calendar.getInstance();

                // 假设有一个方法获取当前正在进行的会议信息
                Appointment currentMeeting = getCurrentMeeting(currentTime);

                // 更新数据库中的结束时间
                updateMeetingEndTime(currentMeeting.getAppointmentID(), currentTime);
            }
        }
    };

    // 判断灯光是否开启，用来推断是否有人员进入
    private boolean isLightOn() {

        return false;
    }

    // 更新正在进行的会议的结束时间为当前时间
    private void updateMeetingEndTime(long appointmentID, Calendar terminationTime) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("EndTime", String.valueOf(terminationTime.getTimeInMillis()));

        db.update("Appointment", values, "AppointmentID = ?", new String[]{String.valueOf(appointmentID)});

        // 取消定时任务，因为会议已经结束
        cancelCancelMeetingTimer();

        ((MyApplication) getApplication()).setMeetingStarted(false);
    }

    // 计算距离结束时间的剩余时间（毫秒）
    private long calculateRemainingTime(Calendar currentTime, String endTimeString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Calendar endTime = Calendar.getInstance();
            endTime.setTime(dateFormat.parse(endTimeString));
            return endTime.getTimeInMillis() - currentTime.getTimeInMillis();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 将毫秒转换为可读的时间格式（HH:mm:ss）
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }
    //从数据库中寻找当前时间段是否有正在进行的会议
    private Appointment getCurrentMeeting(Calendar currentTime) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        // 将 Calendar 对象格式化为字符串
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentTimeString = dateFormat.format(currentTime.getTime());

        // 查询当前时间段内正在进行的会议
        String query = "SELECT * FROM Appointment WHERE StartTime <= ? AND EndTime >= ? ORDER BY StartTime ASC LIMIT 1";
        String[] selectionArgs = {currentTimeString, currentTimeString};
        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    long appointmentID = cursor.getLong(cursor.getColumnIndex("AppointmentID"));
                    String userID = cursor.getString(cursor.getColumnIndex("UserID"));
                    String title = cursor.getString(cursor.getColumnIndex("Title"));
                    String startTime = cursor.getString(cursor.getColumnIndex("StartTime"));
                    String endTime = cursor.getString(cursor.getColumnIndex("EndTime"));
                    String location = cursor.getString(cursor.getColumnIndex("Location"));
                    String content = cursor.getString(cursor.getColumnIndex("Content"));
                    String name = cursor.getString(cursor.getColumnIndex("Name"));

                    return new Appointment(appointmentID, userID, title, startTime, endTime, location, content, name);
                }
            } finally {
                cursor.close();
            }
        }

        return null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            // 在窗口获得焦点时，隐藏导航栏和状态栏
            hideSystemUI();
        }
    }

    // 隐藏导航栏和状态栏的方法
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }


    //打开二维码
    private void startScanner() {
        // 开始扫码
        barcodeScannerView.resume();
        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {

                // 获取扫描到的学生ID
                String scannedStudentID = result.getText();

                UserDAO userDAO = new UserDAO(ChooseFunctionActivity.this);
                // 与数据库中的用户数据比较
                List<User> userList = userDAO.getAllUsers();
                boolean isMatched = false;

                for (User user : userList) {
                    if (scannedStudentID.equals(user.getStudentID())) {
                        playSaomaSound();
                        // 学生ID匹配，执行相应的操作
                        showToast("已扫描二维码，学生ID匹配: " + user.getUserName());

                        serialHelper.open(); // 打开串口通信
                        serialHelper.sendHex("AA0101" + 10 + "55");
                        serialHelper.sendHex("AA02000055"); // 发送十六进制指令
                        serialHelper.close();
                        isMatched = true;
                        break;
                    }
                }

                if (!isMatched) {
                    showToast("学生ID不匹配");
                    playSaomaSoundFalsed();
                }
                stopScanner();
                scanLayout.setVisibility(View.INVISIBLE);
                meetingRoomImage.setVisibility(View.VISIBLE);
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {

            }

        });
        // 启动扫描超时计时器
        scanTimeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 在规定时间内没有扫描成功，执行关闭扫描的操作
                stopScanner();
                scanLayout.setVisibility(View.INVISIBLE);
                meetingRoomImage.setVisibility(View.VISIBLE);
                showToast("扫描超时，自动关闭");
                playSaomaLongSound();
            }
        }, SCAN_TIMEOUT_MILLIS);
    }

    private void stopScanner() {
        // 停止扫码
        barcodeScannerView.pause();

        // 移除扫描超时计时器的回调
        scanTimeoutHandler.removeCallbacksAndMessages(null);
    }
    private void playSaomaLongSound() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.erweimalong);
            mediaPlayer.setOnCompletionListener(mp -> {
                // 在音频播放完成后执行一些操作，如果需要的话
                // 例如，释放 MediaPlayer 资源
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
        }

        // 播放音频
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void playSaomaSound() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.erweima);
            mediaPlayer.setOnCompletionListener(mp -> {
                // 在音频播放完成后执行一些操作，如果需要的话
                // 例如，释放 MediaPlayer 资源
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
        }

        // 播放音频
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void playSaomaSoundFalsed() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.wuxiaoerweima);
            mediaPlayer.setOnCompletionListener(mp -> {
                // 在音频播放完成后执行一些操作，如果需要的话
                // 例如，释放 MediaPlayer 资源
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
        }

        // 播放音频
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }


    private void showAppointmentCountDialog(Context context, int appointmentCount) {
        // 构建 AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("预约数量");
        builder.setMessage("当前用户数量为：" + appointmentCount);

        // 设置确定按钮
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击确定按钮后的处理
                dialog.dismiss(); // 关闭弹窗
            }
        });

        // 创建并显示 AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // 设置最近一次预约信息的 TextView
    private void setLatestReservationTextView(Appointment latestReservation) {
        AppointmentDAO appointmentDAO = new AppointmentDAO(this);
        TextView latestReservationTimeView = findViewById(R.id.latestReservationTime);
        TextView latestReservationTitleView = findViewById(R.id.latestReservationTitle);
        TextView latestReservationMemberView = findViewById(R.id.latestReservationMember);
        TextView latestReservationNextView = findViewById(R.id.latestReservationNext);

        if (latestReservation != null) {
            latestReservationTimeView.setText(latestReservation.getStartTime());
            latestReservationTitleView.setText(latestReservation.getTitle());
            latestReservationMemberView.setText(latestReservation.getName());
            if(appointmentDAO.getNextAppointment()!=null){
                latestReservationNextView.setText(appointmentDAO.getNextAppointment().getStartTime());
            }
        }
    }


    // 更新时间的任务
    private Runnable timeUpdater = new Runnable() {
        @Override
        public void run() {
            updateTime();
            // 一秒后再次执行
            handler.postDelayed(this, 1000);
        }
    };

    // 更新时间的方法
    private void updateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日  ", Locale.getDefault());
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        // 获取当前日期、星期几和时间
        String currentDate = dateFormat.format(new Date());
        String dayOfWeek = dayOfWeekFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        // 设置到相应的 TextView
        CurrentDate.setText(currentDate);
        DayOfWeek.setText(dayOfWeek);
        currentTimeTextView.setText(currentTime);
    }


    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     *
     * @param libraries 需要的动态链接库
     * @return 动态库是否存在
     */
    private boolean checkSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }




    /**
     * 打开相机，RGB活体检测，人脸注册，人脸识别
     *
     * @param view
     */
    public void jumpToFaceRecognizeActivity(View view) {

        playFirstFaceSound();

        if (!isEngineActivated) {
            // 引擎未激活，执行激活操作
            activeEngine(view);
        }
        showFaceRecognitionFragment();
    }

    private void playFirstFaceSound() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.xianshibie);
            mediaPlayer.setOnCompletionListener(mp -> {
                // 在音频播放 完成后执行一些操作，如果需要的话
                // 例如，释放 MediaPlayer 资源
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            });
        }

        // 播放音频
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void showFaceRecognitionFragment() {
        if (!libraryExists) {
            showToast(getString(R.string.library_not_found));
            return;
        }
        // 创建人脸识别 Fragment 的实例
        FaceRecognitionFragment faceRecognitionFragment = new FaceRecognitionFragment();

        // 设置关闭监听器
        faceRecognitionFragment.setOnCloseFragmentListener(this);
        // 获取 FragmentManager 并开始一个事务
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 将 FaceRecognitionFragment 替换为 imageAndScanLayout 中的内容
        transaction.replace(R.id.imageAndScanLayout, faceRecognitionFragment);

        // 添加到返回栈，这样用户按返回键时可以回到上一个状态
        transaction.addToBackStack(null);

        // 提交事务
        transaction.commit();
    }

    @Override
    public void onCloseFragment() {
        // 在这里执行关闭操作，例如移除 Fragment 或改变布局的可见性
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentById(R.id.imageAndScanLayout))
                .commit();
    }


    /**
     * 激活引擎
     *
     * @param view
     */
    public void activeEngine(final View view) {
        if (!libraryExists) {
            showToast(getString(R.string.library_not_found));
            return;
        }
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        if (view != null) {
            view.setClickable(false);
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);

                long start = System.currentTimeMillis();
                int activeCode = FaceEngine.activeOnline(ChooseFunctionActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                Log.i(TAG, "subscribe cost: " + (System.currentTimeMillis() - start));
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        if (activeCode == ErrorInfo.MOK) {
                            showToast(getString(R.string.active_success));
                            // 引擎已经激活，更新全局变量
                            isEngineActivated = true;
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            showToast(getString(R.string.already_activated));
                            // 引擎已经激活，更新全局变量
                            isEngineActivated = true;
                        } else {
                            showToast(getString(R.string.active_failed, activeCode));
                            // 引擎激活失败，更新全局变量
                            isEngineActivated = false;
                        }

                        if (view != null) {
                            view.setClickable(true);
                        }
                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(ChooseFunctionActivity.this, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.i(TAG, activeFileInfo.toString());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(e.getMessage());
                        if (view != null) {
                            view.setClickable(true);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                activeEngine(null);
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

}
