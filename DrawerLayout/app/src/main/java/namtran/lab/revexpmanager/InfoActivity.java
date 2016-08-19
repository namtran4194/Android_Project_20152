package namtran.lab.revexpmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import namtran.lab.entity.UserInfo;

/**
 * Created by namtr on 15/08/2016.
 */
public class InfoActivity extends AppCompatActivity {
    private UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        TextView email = (TextView) findViewById(R.id.email_user);
        TextView uid = (TextView) findViewById(R.id.uid_user);
        Button changePass = (Button) findViewById(R.id.btnChangePass_info);
        getUserInfo();
        email.setText(userInfo.getEmail());
        uid.setText(userInfo.getUid());
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Đợi tí xíu đi...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getUserInfo() {
        SharedPreferences pref = getSharedPreferences(UserInfo.PREF_NAME, MODE_PRIVATE);
        String email = pref.getString(UserInfo.KEY_EMAIL, null);
        String uid = pref.getString(UserInfo.KEY_UID, null);
        userInfo = new UserInfo(email, uid);
    }

}
