package me.liujia95.videoalarmclock.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import me.liujia95.videoalarmclock.R;
import me.liujia95.videoalarmclock.bean.AlarmClockBean;
import me.liujia95.videoalarmclock.utils.LogUtils;

/**
 * Created by Administrator on 2016/2/23 22:07.
 */
public class VideoViewActivity extends Activity implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    @InjectView(R.id.video_videoview)
    VideoView mVideoview;

    private Uri mUri;
    //private MediaController mMediaController;//媒体控制器
    private int mPositionWhenPaused = -1;//记录onPause时视频播放的位置

    private PowerManager.WakeLock mWakelock;
    private PowerManager          mPm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //让Activity显示在锁屏界面上
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        //唤醒屏幕
        mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakelock = mPm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "SimpleTimer");
        mWakelock.acquire();

        setContentView(R.layout.activity_video);
        //自动横屏
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initView();
        initData();
        initListener();
    }

    private void initView() {
        ButterKnife.inject(this);
    }

    private void initData() {
        AlarmClockBean bean = getIntent().getParcelableExtra(ClockSettingActivity.KEY_ALARMCLOCK);
        if (bean.videoPath.equals("默认视频")) {
            mUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test);
            LogUtils.d("===== mUri=" + mUri.toString());
        } else {
            mUri = Uri.parse(bean.videoPath);
            LogUtils.d("===== mUri=" + mUri.toString());
        }

        //媒体控制器的初始化工作
        //mMediaController = new MediaController(this);
        ////videoview绑定媒体控制器
        //mVideoview.setMediaController(mMediaController);
    }

    private void initListener() {
        mVideoview.setOnCompletionListener(this);
        mVideoview.setOnErrorListener(this);
    }

    @Override
    protected void onStart() {
        mVideoview.setVideoURI(mUri);
        mVideoview.start();
        super.onStart();
    }

    @Override
    public void onResume() {
        // 如果有记录位置，则从记录的位置开始播放
        if (mPositionWhenPaused >= 0) {
            mVideoview.seekTo(mPositionWhenPaused);
            mPositionWhenPaused = -1;
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        //        if (mWakelock != null) {
        //            mWakelock.release();
        //        }

        // Stop video when the activity is pause.
        mPositionWhenPaused = mVideoview.getCurrentPosition();
        mVideoview.stopPlayback();
        LogUtils.d("OnStop: mPositionWhenPaused = " + mPositionWhenPaused);
        LogUtils.d("OnStop: getDuration  = " + mVideoview.getDuration());
        super.onPause();
    }

    /**
     * Video播完的时候得到通知
     *
     * @param mp
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        mWakelock.release();
        LogUtils.d("Video播放完毕");

        this.finish();
    }

    /**
     * 监听MediaPlayer上报的错误信息
     *
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

}
