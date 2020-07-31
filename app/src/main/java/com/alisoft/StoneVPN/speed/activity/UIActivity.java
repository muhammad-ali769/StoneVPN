package com.alisoft.StoneVPN.speed.activity;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.multidex.BuildConfig;

import com.airbnb.lottie.LottieAnimationView;
import com.anchorfree.partner.api.response.RemainingTraffic;
import com.anchorfree.sdk.UnifiedSDK;
import com.anchorfree.vpnsdk.callbacks.Callback;
import com.anchorfree.vpnsdk.exceptions.VpnException;
import com.anchorfree.vpnsdk.vpnservice.VPNState;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.alisoft.StoneVPN.speed.Config;
import com.alisoft.StoneVPN.speed.MainApplication;
import com.alisoft.StoneVPN.speed.dialog.RegionChooserDialog;
import com.alisoft.StoneVPN.speed.utils.Converter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.android.material.navigation.NavigationView;
import com.alisoft.StoneVPN.speed.R;
import com.pepperonas.materialdialog.MaterialDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import debugger.Helper;

public abstract class UIActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler, NavigationView.OnNavigationItemSelectedListener {

    protected static final String TAG = MainActivity.class.getSimpleName();
    protected static final String HELPER_TAG = "Helper";

    @BindView(R.id.main_toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.connect_btn)
    ImageView vpn_connect_btn;

    @BindView(R.id.uploading_speed)
    TextView uploading_speed_textview;

    @BindView(R.id.downloading_speed)
    TextView downloading_speed_textview;

    @BindView(R.id.vpn_connection_time)
    TextView vpn_connection_time;

    @BindView(R.id.vpn_connection_time_text)
    TextView vpn_connection_time_text;


   @BindView(R.id.connection_state)
    TextView connectionStateTextView;

    @BindView(R.id.selected_server)
    TextView selectedServerTextView;

    @BindView(R.id.drawer_opener)
    ImageView Drawer_opener_image;

    //    Lottie Variebles Start
    @BindView(R.id.vpn_connecting)
    protected LottieAnimationView vpn_connection_state;
//    Lottie Variebles Ended

    /*google ads*/
    private UnifiedNativeAd nativeAd;
    private InterstitialAd mInterstitialAd;

    /*google IAP*/
    private BillingProcessor bp;
    private boolean isBPavailable = false;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());
    final Runnable mUIUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            checkRemainingTraffic();
            mUIHandler.postDelayed(mUIUpdateRunnable, 10000);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        handleAds();
        initIAP();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        //handleUserLogin();
        setupDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isConnected(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean aBoolean) {
                if (aBoolean) {
                    startUIUpdateTask();
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUIUpdateTask();
    }

    protected abstract void isLoggedIn(Callback<Boolean> callback);

    protected abstract void loginToVpn();

    protected abstract void logOutFromVnp();

    @OnClick(R.id.go_pro)
    public void go_pro_click() {
        showSubsDilog();
    }

    @OnClick(R.id.vpn_select_country)
    public void showRegionDialog() {
        RegionChooserDialog.newInstance().show(getSupportFragmentManager(), RegionChooserDialog.TAG);
    }

   //@OnClick(R.id.share_app_link)
    public void shareAppClick() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "share app");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "I am using that Free Stone VPN App, it's provide all servers for free https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
        }
    }

    @OnClick(R.id.connect_btn)
    public void onConnectBtnClick(View v) {
        vpn_connection_time.setVisibility(View.GONE);
        vpn_connection_state.setVisibility(View.VISIBLE);

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            isConnected(new Callback<Boolean>() {
                @Override
                public void success(@NonNull Boolean aBoolean) {
//                    vpn_connection_state.setVisibility(View.GONE);
//                    vpn_connection_time.setVisibility(View.VISIBLE);
//                    vpn_connection_time.setText("Connected");

                    if (aBoolean) {

                        new MaterialDialog.Builder(UIActivity.this)
                                .title("Confirmation")
                                .message("Are You Sure to Disconnect The Stone VPN")
                                .positiveText("Disconnect")
                                .negativeText("CANCEL")
                                .positiveColor(R.color.pink_700)
                                .negativeColor(R.color.yellow_700)
                                .buttonCallback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        super.onPositive(dialog);
                                        disconnectFromVnp();
//                                        vpn_connect_btn.setImageResource(R.drawable.ic_connect_vpn);
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        super.onNegative(dialog);
//                                                Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .show();

                    } else {
                        connectToVpn();
                    }
                }

                @Override
                public void failure(@NonNull VpnException e) {

                }
            });
        }
    }


    protected abstract void isConnected(Callback<Boolean> callback);

    protected abstract void connectToVpn();

    protected abstract void disconnectFromVnp();

    protected abstract void chooseServer();

    protected abstract void getCurrentServer(Callback<String> callback);

    protected void startUIUpdateTask() {
        stopUIUpdateTask();
        mUIHandler.post(mUIUpdateRunnable);
    }

    protected void stopUIUpdateTask() {
        mUIHandler.removeCallbacks(mUIUpdateRunnable);
        updateUI();
    }

    protected abstract void checkRemainingTraffic();

    protected void updateUI() {
        UnifiedSDK.getVpnState(new Callback<VPNState>() {
            @Override
            public void success(@NonNull VPNState vpnState) {

                switch (vpnState) {
                    case IDLE: {

                        uploading_speed_textview.setText("0 B");
                        downloading_speed_textview.setText("0 B");

                        vpn_connection_state.setVisibility(View.GONE);
                        vpn_connection_time.setVisibility(View.VISIBLE);
                        vpn_connection_time.setText("Not Connected");

                        connectionStateTextView.setText(R.string.disconnected);
                        hideConnectProgress();
//                        vpn_connect_btn.setImageResource(R.drawable.ic_connect_vpn);

                        break;
                    }
                    case CONNECTED: {
                        vpn_connection_state.setVisibility(View.GONE);
                        vpn_connection_time.setVisibility(View.VISIBLE);
                        vpn_connection_time.setText("Connected");

                        connectionStateTextView.setText(R.string.connected);
                        hideConnectProgress();
                        break;
                    }
                    case CONNECTING_VPN:
                        connectionStateTextView.setText(R.string.connecting);

                    case CONNECTING_CREDENTIALS:
                    case CONNECTING_PERMISSIONS: {
                        showConnectProgress();
                        break;
                    }
                    case PAUSED: {
                        connectionStateTextView.setText(R.string.paused);
                        break;
                    }
                }
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });
        UnifiedSDK.getInstance().getBackend().isLoggedIn(new Callback<Boolean>() {
            @Override
            public void success(@NonNull Boolean isLoggedIn) {

                //make connect button enabled
            }

            @Override
            public void failure(@NonNull VpnException e) {

            }
        });

        getCurrentServer(new Callback<String>() {
            @Override
            public void success(@NonNull final String currentServer) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        selectedServerTextView.setText(currentServer != null ? currentServer : "UNKNOWN");
                    }
                });
            }

            @Override
            public void failure(@NonNull VpnException e) {
                selectedServerTextView.setText("UNKNOWN");
            }
        });

    }

    protected void updateTrafficStats(long outBytes, long inBytes) {
        String outString = Converter.humanReadableByteCountOld(outBytes, false);
        String inString = Converter.humanReadableByteCountOld(inBytes, false);

        uploading_speed_textview.setText(inString);
        downloading_speed_textview.setText(outString);
    }

    protected void updateRemainingTraffic(RemainingTraffic remainingTrafficResponse) {
        if (remainingTrafficResponse.isUnlimited()) {
        } else {
            String trafficUsed = Converter.megabyteCount(remainingTrafficResponse.getTrafficUsed()) + "Mb";
            String trafficLimit = Converter.megabyteCount(remainingTrafficResponse.getTrafficLimit()) + "Mb";

        }
    }

    protected void showLoginProgress() {
    }

    protected void hideLoginProgress() {
    }

    protected void showConnectProgress() {
        connectionStateTextView.setVisibility(View.GONE);
    }

    protected void hideConnectProgress() {
        connectionStateTextView.setVisibility(View.VISIBLE);
    }

    protected void showMessage(String msg) {
        Toast.makeText(UIActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void handleUserLogin() {
        ((MainApplication) getApplication()).setNewHostAndCarrier(Config.baseURL, Config.carrierID);
        loginToVpn();
    }

    private void setupDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                UIActivity.this, drawer, null, 0, 0);//R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(UIActivity.this);
    }

    @OnClick(R.id.drawer_opener)
    public void OpenDrawer(View v) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_upgrade) {
//            upgrade application is available...
            RegionChooserDialog.newInstance().show(getSupportFragmentManager(), RegionChooserDialog.TAG);
        } else if (id == R.id.nav_unlock) {
            showSubsDilog();
        } else if (id == R.id.nav_helpus) {
//            find help about the application
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cutvideoedit@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Improve Comments");
            intent.putExtra(Intent.EXTRA_TEXT, "message body");

            try {
                startActivity(Intent.createChooser(intent, "send mail"));
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, "No mail app found!!!", Toast.LENGTH_SHORT);
            } catch (Exception ex) {
                Toast.makeText(this, "Unexpected Error!!!", Toast.LENGTH_SHORT);
            }
        } else if (id == R.id.nav_rate) {
//            rate application...
            //rateUs().show();


        } else if (id == R.id.nav_share) {
//            share the application...
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "share app");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "I am using that Free Stone VPN App, it's provide all servers for free https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            } catch (Exception e) {
            }
        } else if (id == R.id.nav_setting) {
//            Application settings...
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_faq) {
            startActivity(new Intent(this, Faq.class));

        } else if (id == R.id.nav_policy) {
            Uri uri = Uri.parse(getResources().getString(R.string.privacy_policy_link)); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Google Admob Native ad
     **/
    private void handleAds() {

        if (getResources().getBoolean(R.bool.admob_flag) && !Config.ads_subscription) {

            //loading native ad
            refreshAd();
            //interstitial
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.admob_interstitial));
            mInterstitialAd.loadAd(new AdRequest.Builder()
                    .build());

            mInterstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();

                    vpn_connection_time.setVisibility(View.GONE);
                    vpn_connection_state.setVisibility(View.VISIBLE);
                    //same things that was on button click
                    isConnected(new Callback<Boolean>() {
                        @Override
                        public void success(@NonNull Boolean aBoolean) {
                            if (aBoolean) {
                                disconnectFromVnp();
                            } else {

                                connectToVpn();
                            }
                        }

                        @Override
                        public void failure(@NonNull VpnException e) {

                        }
                    });
                }
            });
        }
    }

    //loading native ad
    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView
            adView) {
        // Set the media view. Media content will be automatically populated in the media view once
        MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = nativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.

                    super.onVideoEnd();
                }
            });
        } else {
        }
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     */
    private void refreshAd() {

        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.admob_native_advance));

        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                // You must call destroy on old ads when you are done with them,
                // otherwise you will have a memory leak.
                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                RelativeLayout relativeLayout =
                        findViewById(R.id.fl_adplaceholder);
                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater()
                        .inflate(R.layout.ad_unified, null);
                populateUnifiedNativeAdView(unifiedNativeAd, adView);


                relativeLayout.removeAllViews();
                relativeLayout.addView(adView);
            }

        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                Log.w(HELPER_TAG, "onAdFailedToLoad: " + errorCode);
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder()
                .build());
    }

    // in app subs/billing

    private void showSubsDilog() {
        Dialog dialog = new Dialog(UIActivity.this);
        dialog.setContentView(R.layout.subscription_dialog);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        Button subscribe = dialog.findViewById(R.id.subscribe_button);
        Button cancel = dialog.findViewById(R.id.cancel_button);
        RadioGroup group = dialog.findViewById(R.id.radio_group);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (group.getCheckedRadioButtonId()) {
                    case R.id.rb_one_month:
                        bp.subscribe(UIActivity.this, Config.remove_ads_one_month);
                        break;
                    case R.id.rb_three_month:
                        bp.subscribe(UIActivity.this, Config.remove_ads_three_month);
                        break;
                    case R.id.rb_six_month:
                        bp.subscribe(UIActivity.this, Config.remove_ads_six_month);
                        break;
                    case R.id.rb_one_year:
                        bp.subscribe(UIActivity.this, Config.remove_ads_one_year);
                        break;

                    default:
                        break;
                }
            }
        });

        dialog.show();
    }

    private void initIAP() {
        bp = new BillingProcessor(UIActivity.this, Config.lisence_key, this);
        bp.initialize();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Helper.showToast(UIActivity.this, "Subscribed Successfully");
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        Helper.showToast(UIActivity.this, "Billing Error");
    }

    @Override
    public void onBillingInitialized() {
        if (BillingProcessor.isIabServiceAvailable(UIActivity.this)) {
            isBPavailable = true;
        }

        if (bp.isSubscribed(Config.remove_ads_one_month) || bp.isSubscribed(Config.remove_ads_three_month) || bp.isSubscribed(Config.remove_ads_six_month) || bp.isSubscribed(Config.remove_ads_one_year)) {
            Config.ads_subscription = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
