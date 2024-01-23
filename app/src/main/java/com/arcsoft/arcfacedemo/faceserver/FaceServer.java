package com.arcsoft.arcfacedemo.faceserver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Base64;
import android.util.Log;

import com.arcsoft.arcfacedemo.DAO.UserDAO;
import com.arcsoft.arcfacedemo.activity.DatabaseHelper;
import com.arcsoft.arcfacedemo.model.FaceRegisterInfo;
import com.arcsoft.arcfacedemo.model.User;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.arcsoft.imageutil.ArcSoftRotateDegree;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 人脸库操作类，包含注册和搜索
 */
public class FaceServer {
    private static final String TAG = "FaceServer";
    public static final String IMG_SUFFIX = ".jpg";
    private static FaceEngine faceEngine = null;
    private static FaceServer faceServer = null;
    private static List<FaceRegisterInfo> faceRegisterInfoList;

    private List<User> userList; // 用户列表

    private DatabaseHelper databaseHelper;

    private Context context;
    public static String ROOT_PATH;
    /**
     * 存放注册图的目录
     */
    public static final String SAVE_IMG_DIR = "register" + File.separator + "imgs";
    /**
     * 存放特征的目录
     */
    private static final String SAVE_FEATURE_DIR = "register" + File.separator + "features";

    /**
     * 是否正在搜索人脸，保证搜索操作单线程进行
     */
    private boolean isProcessing = false;

    public static FaceServer getInstance(Context context) {
        if (faceServer == null) {
            synchronized (FaceServer.class) {
                if (faceServer == null) {
                    faceServer = new FaceServer(context);
                }
            }
        }
        return faceServer;
    }

    private FaceServer(Context context) {
        this.context = context;
        this.databaseHelper = new DatabaseHelper(context);
        User user = new User(1,"胡威","13939721407","3097559345","AID6RAAAoEGwyp09bksxPSsj3bznfzq9J6IePKaK670PMoE9q2cqPVENJD2+7IO9ynvMPCDcbjuAHYO9ToTdPdnM+LzofjO8f8pZPemrDj2gADA9Y+QTPTrYXr2uvf69Pur5vV6iIbwWSbA9NSmovQdd+Lsmy5A9WVYFPV23B76yo9g8Emp5vQ1ncD2JtB+883yNvet6cT2RR4s97WL1PPPvCL3J0xw9qKmTO/r8JL2LyK27gf2pO1bjAL0q8TU9KdHeO6rskb09Nj+9qJiBPdlYKj3r8Yc7KbzTvZ4Wrjw/67U6rhijPbJXED3tRZ27JsBzvNWwsjy7Sc29SQNOvdOSkb2FdJg8f8EQvZ21NDxTGUw9g72SPSfXBj5nO5c9kUKUPX3gprv+amW+ZHIOvc9CLTx9A7W9vVEcvVJCUTx7kX26SnJrPU913bz2OCQ94KHHPasPm717V4E8ONaLPZ8lq7w19/k8zkJBvZcSzjv91D09EKxiPVuZHD0fMCK9jluYvY/FDTsSEBS8a9MgPT5stjxpPW29/Z6dvV4DAL6vgv29Q3NgvC8qGj15WiG+RRnWPBZK37wS3sq9OtmLPboMk7z/da29Ww/EvIlnM73OoPA8LavCvY53y7x+nLE97akaPFWnWzxdPWm9VUGIvckDzz1GOAC9wZXbvTXpGDlSpQI+ZsvLvX3V6T0e/Y08ukg6PAChkb3NkdU8y2ZHvRjo1jvuDRu8eXMNPh3uwD3tWk69UReBvT7l1DuvQNA9lZWyPHN0DL33JmA8jrKgPevG6rz3Zla87S03vAKk2jyOLDC9I619PU0Emjw2n7c9YJsoPOZMj7xP9Ta94sDSO6+yDL7/Mig9QiGJvSKwsD3BtZe8nSnoO0+sCj77kPy8UF5+vesaYr2Og969ah+YPUhYJj1YnFe9SmqDPKlYCD2tf4M8noz/vLfSsjxOB/49FbzcvbJooL3K6gK8EnlmPFHFxL1wrRm+s9KAvFgR7j251zU8myUtPDh8sr25gy++mogXvZga4z3T+UO9Kzz6PRur1715A/29hxRbPRhs8bzAU7O9uyCZO5uZmzy1LDu8w4qgvSLAQT2o6v49aSivPUnHvL06GLO9zlpCPcq2WL0vdOG8otCTvc3iHT2R2eA7lAmRvTftJD6VuVY9/zmevBeRJL2pKs88GYnYuxq/Oj1Xhoo9exoqvT2Xg713beC9/XzIvdG5G70lV2Q83kdhvDUl0Ly7E9s8bjhRPTU9Lz3p2ki929STPfhnzz1/H6i9r85VvZhIRT13kxG9Jrv5vBLCXz0LGCW8ENKCvOarBD4fYRA9mBs/vchftLyOyYm9B2GcPb8Bb73a17Y9zkKevBfR0j0LbM08");
        databaseHelper.insertUser(user);
        // 初始化用户列表，可以从数据库或其他地方获取
        this.userList = getUserListFromDatabase();
    }

    // 获取数据库中的用户列表（示例方法，实际情况可能会不同）
    private List<User> getUserListFromDatabase() {
        if (databaseHelper == null) {
            // 初始化数据库帮助类
            databaseHelper = new DatabaseHelper(context);
        }

//        return databaseHelper.getAllUsers();
        return new ArrayList<>();
    }

    /**
     * 初始化
     *
     * @param context 上下文对象
     * @return 是否初始化成功
     */
    public boolean init(Context context) {
        synchronized (this) {
            if (faceEngine == null && context != null) {
                faceEngine = new FaceEngine();
                int engineCode = faceEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 16, 1, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
                if (engineCode == ErrorInfo.MOK) {
                    initFaceList(context);
                    return true;
                } else {
                    faceEngine = null;
                    Log.e(TAG, "init: failed! code = " + engineCode);
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * 销毁
     */
    public void unInit() {
        synchronized (this) {
            if (faceRegisterInfoList != null) {
                faceRegisterInfoList.clear();
                faceRegisterInfoList = null;
            }
            if (faceEngine != null) {
                faceEngine.unInit();
                faceEngine = null;
            }
        }
    }

    /**
     * 初始化人脸特征数据以及人脸特征数据对应的注册图
     *
     * @param context 上下文对象
     */
    private void initFaceList(Context context) {
        synchronized (this) {
            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }
            File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() || !featureDir.isDirectory()) {
                return;
            }
            File[] featureFiles = featureDir.listFiles();
            if (featureFiles == null || featureFiles.length == 0) {
                return;
            }
            faceRegisterInfoList = new ArrayList<>();
            for (File featureFile : featureFiles) {
                try {
                    FileInputStream fis = new FileInputStream(featureFile);
                    byte[] feature = new byte[FaceFeature.FEATURE_SIZE];
                    fis.read(feature);
                    fis.close();
                    faceRegisterInfoList.add(new FaceRegisterInfo(feature, featureFile.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }





    /**
     * 用于预览时注册人脸
     *
     * @param context  上下文对象
     * @param nv21     NV21数据
     * @param width    NV21宽度
     * @param height   NV21高度
     * @param faceInfo {@link FaceEngine#detectFaces(byte[], int, int, int, List)}获取的人脸信息
     * @param name     保存的名字，若为空则使用时间戳
     * @return 是否注册成功
     */
    public boolean registerNv21(Context context, byte[] nv21, int width, int height, FaceInfo faceInfo, String name) {
        synchronized (this) {
            if (faceEngine == null || context == null || nv21 == null || width % 4 != 0 || nv21.length != width * height * 3 / 2) {
                Log.e(TAG, "registerNv21: invalid params");
                return false;
            }

            if (ROOT_PATH == null) {
                ROOT_PATH = context.getFilesDir().getAbsolutePath();
            }
            //特征存储的文件夹
            File featureDir = new File(ROOT_PATH + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() && !featureDir.mkdirs()) {
                Log.e(TAG, "registerNv21: can not create feature directory");
                return false;
            }
            //图片存储的文件夹
            File imgDir = new File(ROOT_PATH + File.separator + SAVE_IMG_DIR);
            if (!imgDir.exists() && !imgDir.mkdirs()) {
                Log.e(TAG, "registerNv21: can not create image directory");
                return false;
            }
            FaceFeature faceFeature = new FaceFeature();
            //特征提取
            int code = faceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfo, faceFeature);
            if (code != ErrorInfo.MOK) {
                Log.e(TAG, "registerNv21: extractFaceFeature failed , code is " + code);
                return false;
            } else {

                String userName = name == null ? String.valueOf(System.currentTimeMillis()) : name;
                try {
                    // 保存注册结果（注册图、特征数据）
                    // 为了美观，扩大rect截取注册图
                    Rect cropRect = getBestRect(width, height, faceInfo.getRect());
                    if (cropRect == null) {
                        Log.e(TAG, "registerNv21: cropRect is null!");
                        return false;
                    }

                    cropRect.left &= ~3;
                    cropRect.top &= ~3;
                    cropRect.right &= ~3;
                    cropRect.bottom &= ~3;

                    File file = new File(imgDir + File.separator + userName + IMG_SUFFIX);


                    // 创建一个头像的Bitmap，存放旋转结果图
                    Bitmap headBmp = getHeadImage(nv21, width, height, faceInfo.getOrient(), cropRect, ArcSoftImageFormat.NV21);

                    FileOutputStream fosImage = new FileOutputStream(file);
                    headBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);
                    fosImage.close();


                    FileOutputStream fosFeature = new FileOutputStream(featureDir + File.separator + userName);
                    fosFeature.write(faceFeature.getFeatureData());
                    fosFeature.close();

                    //内存中的数据同步
//                    if (faceRegisterInfoList == null) {
//                        faceRegisterInfoList = new ArrayList<>();
//                    }
                    String feature = Base64.encodeToString(faceFeature.getFeatureData(), Base64.DEFAULT);
                    User user = new User();
                    user.setCardNumber("111111");
                    user.setFaceFeature(feature);
                    user.setUserName(userName);
                    user.setStudentID("121121");
                    UserDAO userDAO = new UserDAO(context);
                    userDAO.insertUser(user);
                    //faceRegisterInfoList.add(new FaceRegisterInfo(faceFeature.getFeatureData(), userName));
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

    }


    /**
     * 截取合适的头像并旋转，保存为注册头像
     *
     * @param originImageData 原始的BGR24数据
     * @param width           BGR24图像宽度
     * @param height          BGR24图像高度
     * @param orient          人脸角度
     * @param cropRect        裁剪的位置
     * @param imageFormat     图像格式
     * @return 头像的图像数据
     */
    private Bitmap getHeadImage(byte[] originImageData, int width, int height, int orient, Rect cropRect, ArcSoftImageFormat imageFormat) {
        byte[] headImageData = ArcSoftImageUtil.createImageData(cropRect.width(), cropRect.height(), imageFormat);
        int cropCode = ArcSoftImageUtil.cropImage(originImageData, headImageData, width, height, cropRect, imageFormat);
        if (cropCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("crop image failed, code is " + cropCode);
        }

        //判断人脸旋转角度，若不为0度则旋转注册图
        byte[] rotateHeadImageData = null;
        int rotateCode;
        int cropImageWidth;
        int cropImageHeight;
        // 90度或270度的情况，需要宽高互换
        if (orient == FaceEngine.ASF_OC_90 || orient == FaceEngine.ASF_OC_270) {
            cropImageWidth = cropRect.height();
            cropImageHeight = cropRect.width();
        } else {
            cropImageWidth = cropRect.width();
            cropImageHeight = cropRect.height();
        }
        ArcSoftRotateDegree rotateDegree = null;
        switch (orient) {
            case FaceEngine.ASF_OC_90:
                rotateDegree = ArcSoftRotateDegree.DEGREE_270;
                break;
            case FaceEngine.ASF_OC_180:
                rotateDegree = ArcSoftRotateDegree.DEGREE_180;
                break;
            case FaceEngine.ASF_OC_270:
                rotateDegree = ArcSoftRotateDegree.DEGREE_90;
                break;
            case FaceEngine.ASF_OC_0:
            default:
                rotateHeadImageData = headImageData;
                break;
        }
        // 非0度的情况，旋转图像
        if (rotateDegree != null){
            rotateHeadImageData = new byte[headImageData.length];
            rotateCode = ArcSoftImageUtil.rotateImage(headImageData, rotateHeadImageData, cropRect.width(), cropRect.height(), rotateDegree, imageFormat);
            if (rotateCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                throw new RuntimeException("rotate image failed, code is " + rotateCode);
            }
        }
        // 将创建一个Bitmap，并将图像数据存放到Bitmap中
        Bitmap headBmp = Bitmap.createBitmap(cropImageWidth, cropImageHeight, Bitmap.Config.RGB_565);
        if (ArcSoftImageUtil.imageDataToBitmap(rotateHeadImageData, headBmp, imageFormat) != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("failed to transform image data to bitmap");
        }
        return headBmp;
    }

    /**
     * 在特征库中搜索
     *
     * @param faceFeature 传入特征数据
     * @return 比对结果
     */
    public CompareResult getTopOfFaceLib(FaceFeature faceFeature) {
        if (faceEngine == null || isProcessing || faceFeature == null || userList.size() == 0) {
            return null;
        }
        FaceFeature tempFaceFeature = new FaceFeature();
        FaceSimilar faceSimilar = new FaceSimilar();
        float maxSimilar = 0;
        long maxSimilarUserId = -1;
        isProcessing = true;
        for (User user : userList) {
            tempFaceFeature.setFeatureData(Base64.decode(user.getFaceFeature(), Base64.DEFAULT));

            faceEngine.compareFaceFeature(faceFeature, tempFaceFeature, faceSimilar);

            if (faceSimilar.getScore() > maxSimilar) {
                maxSimilar = faceSimilar.getScore();
                maxSimilarUserId = user.getUserID();
            }
        }
        isProcessing = false;
        if (maxSimilarUserId != -1) {
            // 从用户列表中找到匹配的用户，返回姓名和相似度
            User matchedUser = findUserById(maxSimilarUserId);

            if (matchedUser != null) {
                return new CompareResult(matchedUser.getUserName(), maxSimilar);
            }
        }
        return null;
    }

    // 根据用户ID在用户列表中查找用户
    private User findUserById(long userId) {
        for (User user : userList) {
            if (user.getUserID() == userId) {
                return user;
            }
        }
        return null;
    }

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @param width   图像宽度
     * @param height  图像高度
     * @param srcRect 原Rect
     * @return 调整后的Rect
     */
    private static Rect getBestRect(int width, int height, Rect srcRect) {
        if (srcRect == null) {
            return null;
        }
        Rect rect = new Rect(srcRect);

        // 原rect边界已溢出宽高的情况
        int maxOverFlow = Math.max(-rect.left, Math.max(-rect.top, Math.max(rect.right - width, rect.bottom - height)));
        if (maxOverFlow >= 0) {
            rect.inset(maxOverFlow, maxOverFlow);
            return rect;
        }

        // 原rect边界未溢出宽高的情况
        int padding = rect.height() / 2;

        // 若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
        if (!(rect.left - padding > 0 && rect.right + padding < width && rect.top - padding > 0 && rect.bottom + padding < height)) {
            padding = Math.min(Math.min(Math.min(rect.left, width - rect.right), height - rect.bottom), rect.top);
        }
        rect.inset(-padding, -padding);
        return rect;
    }
}
