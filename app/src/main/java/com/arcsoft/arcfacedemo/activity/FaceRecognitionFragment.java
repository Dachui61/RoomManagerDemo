package com.arcsoft.arcfacedemo.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.arcsoft.arcfacedemo.Adapter.UpcomingAppointmentsAdapter;
import com.arcsoft.arcfacedemo.DAO.AppointmentDAO;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.Appointment;
import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.FaceListener;
import com.arcsoft.arcfacedemo.util.face.LivenessType;
import com.arcsoft.arcfacedemo.util.face.RecognizeColor;
import com.arcsoft.arcfacedemo.util.face.RequestFeatureStatus;
import com.arcsoft.arcfacedemo.util.face.RequestLivenessStatus;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.arcfacedemo.widget.FaceSearchResultAdapter;
import com.arcsoft.arcfacedemo.yingjian.controller.LightController;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.lidaxin.data.ComBean;
import com.lidaxin.serial.SerialHelper;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FaceRecognitionFragment extends Fragment implements ViewTreeObserver.OnGlobalLayoutListener {
    private FrameLayout faceRecognitionContainer; // 声明 faceRecognitionContainer 变量
    private static final String TAG = "RegisterAndRecognize";
    private static final int MAX_DETECT_NUM = 10;
    /**
     * 当FR成功，活体未成功时，FR等待活体的时间
     */
    private static final int WAIT_LIVENESS_INTERVAL = 100;
    /**
     * 失败重试间隔时间（ms）
     */
    private static final long FAIL_RETRY_INTERVAL = 1000;
    /**
     * 出错重试最大次数
     */
    private static final int MAX_RETRY_TIME = 3;

    private CameraHelper cameraHelper;
    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    /**
     * 优先打开的摄像头，本界面主要用于单目RGB摄像头设备，因此默认打开前置
     */
    private Integer rgbCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;

    /**
     * VIDEO模式人脸检测引擎，用于预览帧人脸追踪
     */
    private FaceEngine ftEngine;
    /**
     * 用于特征提取的引擎
     */
    private FaceEngine frEngine;
    /**
     * IMAGE模式活体检测引擎，用于预览帧人脸活体检测
     */
    private FaceEngine flEngine;

    private int ftInitCode = -1;
    private int frInitCode = -1;
    private int flInitCode = -1;
    private FaceHelper faceHelper;
    private List<CompareResult> compareResultList;
    private FaceSearchResultAdapter adapter;
    /**
     * 活体检测的开关
     */
    private boolean livenessDetect = true;
    /**
     * 注册人脸状态码，准备注册
     */
    private static final int REGISTER_STATUS_READY = 0;
    /**
     * 注册人脸状态码，注册中
     */
    private static final int REGISTER_STATUS_PROCESSING = 1;
    /**
     * 注册人脸状态码，注册结束（无论成功失败）
     */
    private static final int REGISTER_STATUS_DONE = 2;

    private int registerStatus = REGISTER_STATUS_DONE;
    /**
     * 用于记录人脸识别相关状态
     */
    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    /**
     * 用于记录人脸特征提取出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> extractErrorRetryMap = new ConcurrentHashMap<>();
    /**
     * 用于存储活体值
     */
    private ConcurrentHashMap<Integer, Integer> livenessMap = new ConcurrentHashMap<>();
    /**
     * 用于存储活体检测出错重试次数
     */
    private ConcurrentHashMap<Integer, Integer> livenessErrorRetryMap = new ConcurrentHashMap<>();

    private CompositeDisposable getFeatureDelayedDisposables = new CompositeDisposable();
    private CompositeDisposable delayFaceTaskCompositeDisposable = new CompositeDisposable();
    /**
     * 相机预览显示的控件，可为SurfaceView或TextureView
     */
    private View previewView;
    /**
     * 绘制人脸框的控件
     */
    private FaceRectView faceRectView;

    private Switch switchLivenessDetect;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /**
     * 识别阈值
     */
    private static final float SIMILAR_THRESHOLD = 0.8F;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE

    };

    private MediaPlayer mediaPlayer;

    private DatabaseHelper databaseHelper;

    private static final long Face_TIMEOUT_MILLIS = 10000; // 设置扫描超时时间为5秒
    private Handler FaceTimeoutHandler = new Handler();

    private Handler autoCloseHandler = new Handler();
    private Runnable autoCloseRunnable = new Runnable() {
        @Override
        public void run() {
            playFaceLongSound();
            // 在此处执行关闭人脸识别的操作
            performCloseFragment();
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_register_and_recognize, container, false);
        // 保持亮屏
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // 锁定屏幕方向
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        //本地人脸库初始化
        FaceServer.getInstance(requireContext()).init(requireContext());

        Context appContext = requireActivity().getApplication();

        if((((MyApplication) appContext).isRegisterNum())== true)
        {
            if (registerStatus == REGISTER_STATUS_DONE) {
                registerStatus = REGISTER_STATUS_READY;
            }
        }

        initView(view);
        return view;
    }

    private void initView(View view) {
        previewView = view.findViewById(R.id.single_camera_texture_preview);
        // 在布局结束后才做初始化操作
        previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        faceRectView = view.findViewById(R.id.single_camera_face_rect_view);
        switchLivenessDetect = view.findViewById(R.id.single_camera_switch_liveness_detect);
        switchLivenessDetect.setChecked(livenessDetect);
        switchLivenessDetect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                livenessDetect = isChecked;
            }
        });

        RecyclerView recyclerShowFaceInfo = view.findViewById(R.id.single_camera_recycler_view_person);
        compareResultList = new ArrayList<>();
        adapter = new FaceSearchResultAdapter(compareResultList, requireContext());
        recyclerShowFaceInfo.setAdapter(adapter);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int spanCount = (int) (dm.widthPixels / (getResources().getDisplayMetrics().density * 100 + 0.5f));
        recyclerShowFaceInfo.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));
        recyclerShowFaceInfo.setItemAnimator(new DefaultItemAnimator());


    }

    private void initEngine() {
        ftEngine = new FaceEngine();
        ftInitCode = ftEngine.init(requireContext(), DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(requireContext()),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);

        frEngine = new FaceEngine();
        frInitCode = frEngine.init(requireContext(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);

        flEngine = new FaceEngine();
        flInitCode = flEngine.init(requireContext(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                16, MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);

        Log.i(TAG, "initEngine:  init: " + ftInitCode);

        if (ftInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode);
            Log.i(TAG, "initEngine: " + error);
            showToast(error);
        }
        if (frInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode);
            Log.i(TAG, "initEngine: " + error);
            showToast(error);
        }
        if (flInitCode != ErrorInfo.MOK) {
            String error = getString(R.string.specific_engine_init_failed, "flEngine", flInitCode);
            Log.i(TAG, "initEngine: " + error);
            showToast(error);
        }
    }

    private void unInitEngine() {
        if (ftInitCode == ErrorInfo.MOK && ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frInitCode == ErrorInfo.MOK && frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }
        if (flInitCode == ErrorInfo.MOK && flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.i(TAG, "unInitEngine: " + flUnInitCode);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraHelper != null) {
            cameraHelper.start();
        }
        // 启动计时器，延迟15秒后执行关闭操作
        autoCloseHandler.postDelayed(autoCloseRunnable, 15000);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraHelper != null) {
            cameraHelper.stop();
        }
        // 停止计时器
        autoCloseHandler.removeCallbacks(autoCloseRunnable);
    }

    @Override
    public void onDestroy() {
        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }

        unInitEngine();
        if (getFeatureDelayedDisposables != null) {
            getFeatureDelayedDisposables.clear();
        }
        if (delayFaceTaskCompositeDisposable != null) {
            delayFaceTaskCompositeDisposable.clear();
        }
        if (faceHelper != null) {
            ConfigUtil.setTrackedFaceCount(requireContext(), faceHelper.getTrackedFaceCount());
            faceHelper.release();
            faceHelper = null;
        }

        FaceServer.getInstance(requireContext()).unInit();
        super.onDestroy();
    }
    private void playFaceLongSound() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.face);
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

    private void initCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            //请求FR的回调
            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                //FR成功
                if (faceFeature != null) {
//                    Log.i(TAG, "onPreview: fr end = " + System.currentTimeMillis() + " trackId = " + requestId);
                    Integer liveness = livenessMap.get(requestId);
                    //不做活体检测的情况，直接搜索
                    if (!livenessDetect) {
                        searchFace(faceFeature, requestId);
                    }
                    //活体检测通过，搜索特征
                    else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        searchFace(faceFeature, requestId);
                    }
                    //活体检测未出结果，或者非活体，延迟执行该函数
                    else {
                        if (requestFeatureStatusMap.containsKey(requestId)) {
                            Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS)
                                    .subscribe(new Observer<Long>() {
                                        Disposable disposable;

                                        @Override
                                        public void onSubscribe(Disposable d) {
                                            disposable = d;
                                            getFeatureDelayedDisposables.add(disposable);
                                        }

                                        @Override
                                        public void onNext(Long aLong) {
                                            onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }

                                        @Override
                                        public void onComplete() {
                                            getFeatureDelayedDisposables.remove(disposable);
                                        }
                                    });
                        }
                    }

                }
                //特征提取失败
                else {
                    if (increaseAndGetValue(extractErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        extractErrorRetryMap.put(requestId, 0);

                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = "ExtractCode:" + errorCode;
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        // 在尝试最大次数后，特征提取仍然失败，则认为识别未通过
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                        retryRecognizeDelayed(requestId);
                    } else {
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                    }
                }
            }

            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
                if (livenessInfo != null) {
                    int liveness = livenessInfo.getLiveness();
                    livenessMap.put(requestId, liveness);
                    // 非活体，重试
                    if (liveness == LivenessInfo.NOT_ALIVE) {
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_ALIVE"));
                        // 延迟 FAIL_RETRY_INTERVAL 后，将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        retryLivenessDetectDelayed(requestId);
                    }
                } else {
                    if (increaseAndGetValue(livenessErrorRetryMap, requestId) > MAX_RETRY_TIME) {
                        livenessErrorRetryMap.put(requestId, 0);
                        String msg;
                        // 传入的FaceInfo在指定的图像上无法解析人脸，此处使用的是RGB人脸数据，一般是人脸模糊
                        if (errorCode != null && errorCode == ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL) {
                            msg = getString(R.string.low_confidence_level);
                        } else {
                            msg = "ProcessCode:" + errorCode;
                        }
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, msg));
                        retryLivenessDetectDelayed(requestId);
                    } else {
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                    }
                }
            }


        };


        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size lastPreviewSize = previewSize;
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height, previewView.getWidth(), previewView.getHeight(), displayOrientation
                        , cameraId, isMirror, false, false);
                Log.i(TAG, "onCameraOpened: " + drawHelper.toString());
                // 切换相机的时候可能会导致预览尺寸发生变化
                if (faceHelper == null ||
                        lastPreviewSize == null ||
                        lastPreviewSize.width != previewSize.width || lastPreviewSize.height != previewSize.height) {
                    Integer trackedFaceCount = null;
                    // 记录切换时的人脸序号
                    if (faceHelper != null) {
                        trackedFaceCount = faceHelper.getTrackedFaceCount();
                        faceHelper.release();
                    }
                    faceHelper = new FaceHelper.Builder()
                            .ftEngine(ftEngine)
                            .frEngine(frEngine)
                            .flEngine(flEngine)
                            .frQueueSize(MAX_DETECT_NUM)
                            .flQueueSize(MAX_DETECT_NUM)
                            .previewSize(previewSize)
                            .faceListener(faceListener)
                            .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(FaceRecognitionFragment.this.requireContext().getApplicationContext()) : trackedFaceCount)
                            .build();
                }
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                if (faceRectView != null) {
                    faceRectView.clearFaceInfo();
                }
                List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21);
                if (facePreviewInfoList != null && faceRectView != null && drawHelper != null) {
                    drawPreviewInfo(facePreviewInfoList);
                }
                registerFace(nv21, facePreviewInfoList);
                clearLeftFace(facePreviewInfoList);

                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        Integer status = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());
                        /**
                         * 在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                         */
                        if (livenessDetect && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
                            if (liveness == null
                                    || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                                livenessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                                faceHelper.requestFaceLiveness(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId(), LivenessType.RGB);
                            }
                        }
                        /**
                         * 对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                         * 特征提取回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                         */
                        if (status == null
                                || status == RequestFeatureStatus.TO_RETRY) {
                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                            faceHelper.requestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
//                            Log.i(TAG, "onPreview: fr start = " + System.currentTimeMillis() + " trackId = " + facePreviewInfoList.get(i).getTrackedFaceCount());
                        }
                    }
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(previewView.getMeasuredWidth(), previewView.getMeasuredHeight()))
                .rotation(getActivity().getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(rgbCameraID != null ? rgbCameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(previewView)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        cameraHelper.start();
    }

    private void registerFace(final byte[] nv21, final List<FacePreviewInfo> facePreviewInfoList) {
        if (registerStatus == REGISTER_STATUS_READY && facePreviewInfoList != null && facePreviewInfoList.size() > 0) {
            registerStatus = REGISTER_STATUS_PROCESSING;
            Observable.create(new ObservableOnSubscribe<Boolean>() {
                        @Override
                        public void subscribe(ObservableEmitter<Boolean> emitter) {

                            boolean success = FaceServer.getInstance(requireContext().getApplicationContext()).registerNv21(requireContext(), nv21.clone(), previewSize.width, previewSize.height,
                                    facePreviewInfoList.get(0).getFaceInfo(), "registered " + faceHelper.getTrackedFaceCount());
                            emitter.onNext(success);
                        }
                    })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean success) {
                            String result = success ? "register success!" : "register failed!";
                            showToast(result);
                            registerStatus = REGISTER_STATUS_DONE;
                            if (cameraHelper != null) {
                                cameraHelper.release();
                                cameraHelper = null;
                            }

                            unInitEngine();
                            if (getFeatureDelayedDisposables != null) {
                                getFeatureDelayedDisposables.clear();
                            }
                            if (delayFaceTaskCompositeDisposable != null) {
                                delayFaceTaskCompositeDisposable.clear();
                            }
                            if (faceHelper != null) {
                                ConfigUtil.setTrackedFaceCount(requireContext(), faceHelper.getTrackedFaceCount());
                                faceHelper.release();
                                faceHelper = null;
                            }

                            FaceServer.getInstance(requireContext()).unInit();

                            Context appContext = requireActivity().getApplication();

                            ((MyApplication) appContext).setRegisterNum(false);

                            performCloseFragment();

                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            showToast("register failed!");
                            registerStatus = REGISTER_STATUS_DONE;
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
            Integer liveness = livenessMap.get(facePreviewInfoList.get(i).getTrackId());
            Integer recognizeStatus = requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId());

            // 根据识别结果和活体结果设置颜色
            int color = RecognizeColor.COLOR_UNKNOWN;
            if (recognizeStatus != null) {
                if (recognizeStatus == RequestFeatureStatus.FAILED) {
                    color = RecognizeColor.COLOR_FAILED;
                }
                if (recognizeStatus == RequestFeatureStatus.SUCCEED) {
                    color = RecognizeColor.COLOR_SUCCESS;
                }
            }
            if (liveness != null && liveness == LivenessInfo.NOT_ALIVE) {
                color = RecognizeColor.COLOR_FAILED;
            }

            drawInfoList.add(new DrawInfo(drawHelper.adjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()),
                    GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, liveness == null ? LivenessInfo.UNKNOWN : liveness, color,
                    name == null ? String.valueOf(facePreviewInfoList.get(i).getTrackId()) : name));
        }
        drawHelper.draw(faceRectView, drawInfoList);
    }

    private void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                initEngine();
                initCamera();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (compareResultList != null) {
            for (int i = compareResultList.size() - 1; i >= 0; i--) {
                if (!requestFeatureStatusMap.containsKey(compareResultList.get(i).getTrackId())) {
                    compareResultList.remove(i);
                    adapter.notifyItemRemoved(i);
                }
            }
        }
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            livenessMap.clear();
            livenessErrorRetryMap.clear();
            extractErrorRetryMap.clear();
            if (getFeatureDelayedDisposables != null) {
                getFeatureDelayedDisposables.clear();
            }
            return;
        }
        Enumeration<Integer> keys = requestFeatureStatusMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(key);
                livenessMap.remove(key);
                livenessErrorRetryMap.remove(key);
                extractErrorRetryMap.remove(key);
            }
        }


    }

    private void searchFace(final FaceFeature frFace, final Integer requestId) {
        Observable
                .create(new ObservableOnSubscribe<CompareResult>() {
                    @Override
                    public void subscribe(ObservableEmitter<CompareResult> emitter) {
//                        Log.i(TAG, "subscribe: fr search start = " + System.currentTimeMillis() + " trackId = " + requestId);
                        CompareResult compareResult = FaceServer.getInstance(requireContext().getApplicationContext()).getTopOfFaceLib(frFace);
//                        Log.i(TAG, "subscribe: fr search end = " + System.currentTimeMillis() + " trackId = " + requestId);
                        emitter.onNext(compareResult);

                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CompareResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(CompareResult compareResult) {
                        if (compareResult == null || compareResult.getUserName() == null) {
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                            faceHelper.setName(requestId, "VISITOR " + requestId);
                            return;
                        }

//                        Log.i(TAG, "onNext: fr search get result  = " + System.currentTimeMillis() + " trackId = " + requestId + "  similar = " + compareResult.getSimilar());
                        if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                            boolean isAdded = false;
                            if (compareResultList == null) {
                                requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                                faceHelper.setName(requestId, "VISITOR " + requestId);
                                return;
                            }
                            for (CompareResult compareResult1 : compareResultList) {
                                if (compareResult1.getTrackId() == requestId) {
                                    isAdded = true;
                                    break;
                                }
                            }
                            if (!isAdded) {
                                //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                                if (compareResultList.size() >= MAX_DETECT_NUM) {
                                    compareResultList.remove(0);
                                    adapter.notifyItemRemoved(0);
                                }
                                //添加显示人员时，保存其trackId
                                compareResult.setTrackId(requestId);
                                compareResultList.add(compareResult);
                                adapter.notifyItemInserted(compareResultList.size() - 1);
                            }
                            requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                            faceHelper.setName(requestId, getString(R.string.recognize_success_notice, compareResult.getUserName()));

                            Context appContext = requireActivity().getApplication();

                            if((((MyApplication) appContext).isMeetingMode())==0) {
                                // 人脸识别通过，弹出 Toast并打开灯光
                                LightController lightController = new LightController(requireContext());
                                SerialHelper serialHelper = new SerialHelper("/dev/ttyS3", "9600") {
                                    @Override
                                    protected void onDataReceived(ComBean ComRecData) {

                                    }
                                };
                                serialHelper.open(); // 打开串口通信
                                serialHelper.sendHex("AA0101" + 10 + "55");
                                serialHelper.sendHex("AA02000055"); // 发送十六进制指令
                                serialHelper.close();
                                performCloseFragment();

                                lightController.openLightAll();

                                playAudioOnSucessed();
                            }
//                            ((MyApplication) appContext).setMeetingMode(false);
                            else if((((MyApplication) appContext).isMeetingMode())==1)
                            {
                                ((MyApplication) appContext).setMeetingMode(0);

                                if((((MyApplication) appContext).isMeetingMode())==0)
                                {
                                    Calendar currentTime = Calendar.getInstance();
                                    Appointment currentMeeting = getCurrentMeeting(currentTime);

                                    if (currentMeeting != null) {
                                        // 获取当前的时间
                                        Calendar terminationTime = Calendar.getInstance();

                                        // 更新数据库中的结束时间
                                        updateMeetingEndTime(currentMeeting.getAppointmentID(), terminationTime);

                                        LightController lightController = new LightController(requireContext());
                                        lightController.closeLightAll();

                                        // 播放音频
                                        playAudioOnTermination();

                                    } else {
                                        // 提示用户没有正在进行的会议
                                        showToast("当前没有正在进行的会议");
                                    }
                                }
                            }else if((((MyApplication) appContext).isMeetingMode())==2)
                            {
                                Calendar currentTime = Calendar.getInstance();

                                if(!isTimeSlotBooked(currentTime,"1 小时")) {
                                    handleLocalReservation(currentTime, "1 小时", compareResult.getUserName(), "临时会议");
                                }else{
                                    Toast.makeText(requireContext(), "当前时间段不可用", Toast.LENGTH_SHORT).show();
                                }
                                ((MyApplication) appContext).setMeetingMode(0);
//                                (ChooseFunctionActivity)getActivity().handleLocalReservation(currentTime,"1 小时",compareResult.getUserName(),"临时会议");

                            } else if ((((MyApplication) appContext).isMeetingMode())==3) {
                                Calendar currentTime = Calendar.getInstance();
                                Appointment currentMeeting = getCurrentMeeting(currentTime);
                                if (currentMeeting != null) {
                                    // 获取当前会议的未延长结束时间
                                    Calendar originalEndDateTime = parseTimeString(currentMeeting.getEndTime());
                                    // 检查是否可以延长
                                    if (!isTimeSlotBooked(originalEndDateTime, "15 分钟")) {
                                        // 计算延长后的结束时间
                                        Calendar extendedEndDateTime = (Calendar) originalEndDateTime.clone();
                                        extendedEndDateTime.add(Calendar.MINUTE, 15);
                                        //Log.d("ExtendedEndTime", "Extended End Time: " + formatCalendarToString(extendedEndTime));
                                        String endTime = formatCalendarToString(extendedEndDateTime);
                                        // 根据不同的延长时间播放不同的音频
                                        playExtendSound15();
                                        // 更新数据库中的结束时间
                                        updateMeetingEndTime(currentMeeting.getAppointmentID(),endTime);
                                        ((MyApplication) appContext).setMeetingMode(0);
                                    } else {
                                        // 提示用户无法延长，选定时间段已被预约
                                        showToast("无法延长，选定时间段已被预约");
                                    }
                                }
                            }else if ((((MyApplication) appContext).isMeetingMode())==4) {
                                Calendar currentTime = Calendar.getInstance();
                                Appointment currentMeeting = getCurrentMeeting(currentTime);
                                if (currentMeeting != null) {
                                    // 获取当前会议的未延长结束时间
                                    Calendar originalEndDateTime = parseTimeString(currentMeeting.getEndTime());
                                    // 检查是否可以延长
                                    if (!isTimeSlotBooked(originalEndDateTime, "30 分钟")) {
                                        // 计算延长后的结束时间
                                        Calendar extendedEndDateTime = (Calendar) originalEndDateTime.clone();
                                        extendedEndDateTime.add(Calendar.MINUTE, 30);
                                        //Log.d("ExtendedEndTime", "Extended End Time: " + formatCalendarToString(extendedEndTime));
                                        String endTime = formatCalendarToString(extendedEndDateTime);
                                        // 根据不同的延长时间播放不同的音频
                                        playExtendSound30();
                                        // 更新数据库中的结束时间
                                        updateMeetingEndTime(currentMeeting.getAppointmentID(),endTime);
                                        ((MyApplication) appContext).setMeetingMode(0);
//
                                    } else {
//                        // 提示用户无法延长，选定时间段已被预约
                                        showToast("无法延长，选定时间段已被预约");
                                    }
                                }
                            }else if ((((MyApplication) appContext).isMeetingMode())==5) {
                                Calendar currentTime = Calendar.getInstance();
                                Appointment currentMeeting = getCurrentMeeting(currentTime);
                                if (currentMeeting != null) {
                                    // 获取当前会议的未延长结束时间
                                    Calendar originalEndDateTime = parseTimeString(currentMeeting.getEndTime());
                                    // 检查是否可以延长
                                    if (!isTimeSlotBooked(originalEndDateTime, "1 小时")) {
                                        // 计算延长后的结束时间
                                        Calendar extendedEndDateTime = (Calendar) originalEndDateTime.clone();
                                        extendedEndDateTime.add(Calendar.MINUTE, 60);
                                        //Log.d("ExtendedEndTime", "Extended End Time: " + formatCalendarToString(extendedEndTime));
                                        String endTime = formatCalendarToString(extendedEndDateTime);
                                        // 根据不同的延长时间播放不同的音频
                                        playExtendSound60();
                                        // 更新数据库中的结束时间
                                        updateMeetingEndTime(currentMeeting.getAppointmentID(),endTime);
                                        ((MyApplication) appContext).setMeetingMode(0);

                                    } else {
                                        // 提示用户无法延长，选定时间段已被预约
                                        showToast("无法延长，选定时间段已被预约");
                                    }
                                }
                            }

                            showRecognitionSuccessToast(compareResult.getUserName());

                            if (cameraHelper != null) {
                                cameraHelper.release();
                                cameraHelper = null;
                            }

                            unInitEngine();
                            if (getFeatureDelayedDisposables != null) {
                                getFeatureDelayedDisposables.clear();
                            }
                            if (delayFaceTaskCompositeDisposable != null) {
                                delayFaceTaskCompositeDisposable.clear();
                            }
                            if (faceHelper != null) {
                                ConfigUtil.setTrackedFaceCount(requireContext(), faceHelper.getTrackedFaceCount());
                                faceHelper.release();
                                faceHelper = null;
                            }

                            FaceServer.getInstance(requireContext()).unInit();

                            performCloseFragment();



                        } else {
                            faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                            retryRecognizeDelayed(requestId);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        faceHelper.setName(requestId, getString(R.string.recognize_failed_notice, "NOT_REGISTERED"));
                        retryRecognizeDelayed(requestId);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void updateMeetingEndTime(long appointmentID, String newEndTime) {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("EndTime", newEndTime);

        db.update("Appointment", values, "AppointmentID = ?", new String[]{String.valueOf(appointmentID)});
    }
    private void playExtendSound30() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.yanshi30);
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

    private void playExtendSound60() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.yanshi60);
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
    private void playExtendSound15() {
        // 播放刷卡成功的音频文件，将 R.raw.success_sound 替换成你的音频文件
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.yanshi15);
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

    private String formatCalendarToString1(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    private Calendar parseTimeString(String timeString) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(timeString);
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    private boolean isTimeSlotBooked(Calendar selectedDateTime, String selectedDuration) {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        // 计算预约的结束时间
        Calendar endDateTime = (Calendar) selectedDateTime.clone();
        endDateTime.setTimeInMillis(endDateTime.getTimeInMillis() + getDurationInMillis(selectedDuration));

        // 将时间转换为字符串，使用特定的格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedStartTime = dateFormat.format(selectedDateTime.getTime());
        String formattedEndTime = dateFormat.format(endDateTime.getTime());

        // 查询数据库，检查是否存在与选定时间段有重叠的预约
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM Appointment " +
                        "WHERE (StartTime < ? AND EndTime > ?) OR (StartTime < ? AND EndTime > ?)",
                new String[] {
                        formattedEndTime,
                        formattedStartTime,
                        formattedStartTime,
                        formattedEndTime
                }
        );

        boolean isBooked = cursor != null && cursor.getCount() > 0;

        if (cursor != null) {
            cursor.close();
        }

        return isBooked;
    }
    private long getDurationInMillis(String duration) {
        switch (duration) {
            case "15 分钟":
                return 15 * 60 * 1000;
            case "30 分钟":
                return 30 * 60 * 1000;
            case "45 分钟":
                return 45 * 60 * 1000;
            case "1 小时":
                return 60 * 60 * 1000;
            case "2 小时":
                return 2 * 60 * 60 * 1000;
            case "3 小时":
                return 3 * 60 * 60 * 1000;
            // 添加其他时长的处理逻辑
            default:
                return 0;
        }
    }

    private void handleLocalReservation(Calendar selectedDateTime, String duration, String userName, String appointmentTitle) {
            playAudioOnYuyue();
            // 如果时间可用，插入预约信息到数据库
            insertAppointmentIntoDatabase(selectedDateTime,duration,userName,appointmentTitle);
    }

    private void insertAppointmentIntoDatabase(Calendar selectedDateTime, String duration , String name , String title) {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("UserID","用户ID"/* 将用户ID添加到这里，你需要获取当前用户的ID */);
        values.put("Title", title); // 你可以根据需求修改预约标题
        values.put("StartTime", formatCalendarToString(selectedDateTime));
        values.put("EndTime", formatCalendarToString(getEndDateTime(selectedDateTime, duration)));
        values.put("Location", "预约地点"); // 你可以根据需求修改预约地点
        values.put("Content", "临时预约"); // 你可以根据需求修改预约内容
        values.put("Name", name); // 你可以根据需求修改预约者姓名

        db.insert("Appointment", null, values);
        db.close();
    }
    private String formatCalendarToString(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }
    private Calendar getEndDateTime(Calendar selectedDateTime, String duration) {
        Calendar endDateTime = (Calendar) selectedDateTime.clone();
        switch (duration) {
            case "15 分钟":
                endDateTime.add(Calendar.MINUTE, 15);
                break;
            case "30 分钟":
                endDateTime.add(Calendar.MINUTE, 30);
                break;
            case "45 分钟":
                endDateTime.add(Calendar.MINUTE, 45);
                break;
            case "1 小时":
                endDateTime.add(Calendar.HOUR, 1);
                break;
            case "2 小时":
                endDateTime.add(Calendar.HOUR, 2);
                break;
            case "3 小时":
                endDateTime.add(Calendar.HOUR, 3);
                break;
            // 添加其他时长的处理逻辑
            default:
                break;
        }
        return endDateTime;
    }

    private void playAudioOnYuyue() {
        // 使用 MediaPlayer 播放音频，将 R.raw.termination_sound 替换为你的音频资源
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.yuyue);
        mediaPlayer.setOnCompletionListener(mp -> {
            // 在音频播放完成后执行一些操作，如果需要的话
            // 例如，释放 MediaPlayer 资源
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();
    }

    // 播放音频的方法
    private void playAudioOnTermination() {
        // 使用 MediaPlayer 播放音频，将 R.raw.termination_sound 替换为你的音频资源
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.tiqian);
        mediaPlayer.setOnCompletionListener(mp -> {
            // 在音频播放完成后执行一些操作，如果需要的话
            // 例如，释放 MediaPlayer 资源
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();
    }

    // 更新数据库中会议的结束时间
    private void updateMeetingEndTime(long appointmentID, Calendar terminationTime) {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("EndTime", String.valueOf(terminationTime.getTimeInMillis()));

        db.update("Appointment", values, "AppointmentID = ?", new String[]{String.valueOf(appointmentID)});

        Context appContext = requireActivity().getApplication();

        ((MyApplication) appContext).setMeetingStarted(false);
    }
    private Appointment getCurrentMeeting(Calendar currentTime) {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
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
    // 播放音频的方法，在这里添加你的播放逻辑
    private void playAudioOnSucessed() {
        // 使用 MediaPlayer 播放音频，将 R.raw.termination_sound 替换为你的音频资源
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.faceandmenjin);
        mediaPlayer.setOnCompletionListener(mp -> {
            // 在音频播放完成后执行一些操作，如果需要的话
            // 例如，释放 MediaPlayer 资源
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();
    }


    // 弹出人脸识别通过的 Toast
    private void showRecognitionSuccessToast(String userName) {
        String message = getString(R.string.recognize_success_notice, userName);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void performCloseFragment() {
        closeFragment(); // 调用接口通知宿主 Activity 关闭当前 Fragment
    }
    public interface OnCloseFragmentListener {
        void onCloseFragment();
    }

    private OnCloseFragmentListener onCloseFragmentListener;



    public void setOnCloseFragmentListener(OnCloseFragmentListener listener) {
        this.onCloseFragmentListener = listener;
    }



    private void closeFragment() {
        if (onCloseFragmentListener != null) {
            onCloseFragmentListener.onCloseFragment();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCloseFragmentListener) {
            onCloseFragmentListener = (OnCloseFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnCloseFragmentListener");
        }
    }

    /**
     * 将准备注册的状态置为{@link #REGISTER_STATUS_READY}
     *
     * @param view 注册按钮
     */
    public void register(View view) {
        if (registerStatus == REGISTER_STATUS_DONE) {
            registerStatus = REGISTER_STATUS_READY;
        }
    }

    /**
     * 切换相机。注意：若切换相机发现检测不到人脸，则极有可能是检测角度导致的，需要销毁引擎重新创建或者在设置界面修改配置的检测角度
     *
     * @param view
     */
    public void switchCamera(View view) {
        if (cameraHelper != null) {
            boolean success = cameraHelper.switchCamera();
            if (!success) {
                showToast(getString(R.string.switch_camera_failed));
            } else {
                showLongToast(getString(R.string.notice_change_detect_degree));
            }
        }
    }



    /**
     * 将map中key对应的value增1回传
     *
     * @param countMap map
     * @param key      key
     * @return 增1后的value
     */
    public int increaseAndGetValue(Map<Integer, Integer> countMap, int key) {
        if (countMap == null) {
            return 0;
        }
        Integer value = countMap.get(key);
        if (value == null) {
            value = 0;
        }
        countMap.put(key, ++value);
        return value;
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行活体检测
     *
     * @param requestId 人脸ID
     */
    private void retryLivenessDetectDelayed(final Integer requestId) {
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        if (livenessDetect) {
                            faceHelper.setName(requestId, Integer.toString(requestId));
                        }
                        livenessMap.put(requestId, LivenessInfo.UNKNOWN);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    /**
     * 延迟 FAIL_RETRY_INTERVAL 重新进行人脸识别
     *
     * @param requestId 人脸ID
     */
    private void retryRecognizeDelayed(final Integer requestId) {
        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS)
                .subscribe(new Observer<Long>() {
                    Disposable disposable;

                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                        delayFaceTaskCompositeDisposable.add(disposable);
                    }

                    @Override
                    public void onNext(Long aLong) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        // 将该人脸特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                        faceHelper.setName(requestId, Integer.toString(requestId));
                        requestFeatureStatusMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                        delayFaceTaskCompositeDisposable.remove(disposable);
                    }
                });
    }

    protected boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(requireContext(), neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;
        for (int grantResult : grantResults) {
            isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
        }
        afterRequestPermission(requestCode, isAllGranted);
    }




    protected void showToast(String s) {
        Toast.makeText(requireContext().getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
    protected void showLongToast(String s) {
        Toast.makeText(requireContext().getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGlobalLayout() {
        previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initEngine();
            initCamera();
        }
    }


}


