package com.arcsoft.arcfacedemo.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.arcfacedemo.DAO.UserDAO;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.model.User;

import java.util.List;

public class SwipeCardEntryActivity  extends AppCompatActivity {
    private TextView promptTextView;
    private EditText cardNumberEditText;

    private Handler handler = new Handler();
    private static final long DELAY_MILLIS = 500; // 500毫秒延迟

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swipe_card_entry);

        promptTextView = findViewById(R.id.promptTextView);
        cardNumberEditText = findViewById(R.id.cardNumberEditText);

        // 设置提示信息
        showPrompt("请将卡放置右下角");

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
                    boolean compareResult = compareWithDatabase(cardNumber);
                    handleComparisonResult(compareResult);
                }, DELAY_MILLIS);
            }
        });
    }

    private void showPrompt(String message) {
        promptTextView.setText(message);
    }

    private boolean compareWithDatabase(String cardNumber) {

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
            // 匹配成功的逻辑，例如显示对应信息
            Toast.makeText(this, "匹配成功12345", Toast.LENGTH_SHORT).show();
        } else {
            // 匹配失败的逻辑，例如提示用户重新刷卡
            Toast.makeText(this, "匹配失败，请重新刷卡", Toast.LENGTH_SHORT).show();
        }
    }
}
