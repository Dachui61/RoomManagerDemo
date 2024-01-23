package com.arcsoft.arcfacedemo.faceserver;

import android.util.Base64;
import android.util.Log;

import com.arcsoft.arcfacedemo.model.FaceRegisterInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class FaceDatabaseHelper {

    private static final String DB_URL = "jdbc:mysql://10.17.209.219:3306/banpai";
    private static final String USER = "root";
    private static final String PASS = "160153";

    private Connection connectToDatabase() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

//     根据人脸ID从数据库中获取人脸特征
//    public FaceInfo getFaceFeatureById(String faceId) {
//        Connection connection = connectToDatabase();
//        if (connection != null) {
//            String query = "SELECT featureData ,name FROM face_table WHERE faceId = ?";
//            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
//                preparedStatement.setString(1, faceId);
//                try (ResultSet resultSet = preparedStatement.executeQuery()) {
//                    if (resultSet.next()) {
//                        String base64FeatureData = resultSet.getString("featureData");
//                        String name = resultSet.getString("name");
//                        byte[] featureData = android.util.Base64.decode(base64FeatureData, Base64.DEFAULT);
//                        return new FaceInfo(name, featureData);
//                        //return resultSet.getBytes("featureData");
//                    }
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            } finally {
//                // 关闭数据库连接
//                try {
//                    connection.close();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return null;
//    }

    public List<FaceRegisterInfo> getAllFaceInfoFromDatabase() {
        List<FaceRegisterInfo> faceRegisterInfoList = new ArrayList<>();

        Connection connection = connectToDatabase();
        if (connection != null) {
            String query = "SELECT featuredata,uname FROM userfeature";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String base64FeatureData = resultSet.getString("featuredata");
                    //System.out.println(base64FeatureData);
                    byte[] featureData = android.util.Base64.decode(base64FeatureData, Base64.DEFAULT);
                    String name = resultSet.getString("uname");
                    Log.d("MySQLData", "Base64 Feature Data: " + base64FeatureData);
                    faceRegisterInfoList.add(new FaceRegisterInfo(featureData, name));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // 关闭数据库连接
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return faceRegisterInfoList;
    }
}

