package com.alisoft.StoneVPN.speed.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.anchorfree.partner.api.auth.AuthMethod;
import com.anchorfree.partner.api.response.User;
import com.anchorfree.sdk.UnifiedSDK;
import com.anchorfree.vpnsdk.callbacks.Callback;
import com.anchorfree.vpnsdk.exceptions.VpnException;
import com.alisoft.StoneVPN.speed.Config;
import com.alisoft.StoneVPN.speed.MainApplication;
import com.google.android.material.snackbar.Snackbar;
import com.alisoft.StoneVPN.speed.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    @BindView(R.id.parent)
    RelativeLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        Snackbar snackbar = Snackbar
                .make(parent, "Logging in, Please wait...", Snackbar.LENGTH_LONG);
        snackbar.show();


        //logging in
        ((MainApplication) getApplication()).setNewHostAndCarrier(Config.baseURL, Config.carrierID);
        AuthMethod authMethod = AuthMethod.anonymous();
        UnifiedSDK.getInstance().getBackend().login(authMethod, new Callback<User>() {
            @Override
            public void success(User user) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void failure(VpnException e) {
                Snackbar snackbar = Snackbar
                        .make(parent, "Please Check Your Internet Connection.", Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            }
        });
    }
}
