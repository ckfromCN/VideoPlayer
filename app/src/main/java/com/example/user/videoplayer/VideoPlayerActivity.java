package com.example.user.videoplayer;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoPlayerActivity extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener {

    private MediaPlayer player;

    private SurfaceView surfaceView;

    private SurfaceHolder holder;

    private LinearLayout titleLayout, controlerLayout;

    private RelativeLayout scrollLayout;

    private SeekBar seekBar;

    private ImageView backButton, rewindButton, playButton, forwindButton, lockButton, onlockButton, scrollImage;

    private TextView currentTime, maxTime, titleText, scrollText;

    private SimpleDateFormat format = new SimpleDateFormat("mm:ss");

    private int position;
    private int max;
    private int maxVolume;
    private int currentVolume;

    private long currentMsgTime, lastMsgTime;

    private AudioManager audioManager;

    private boolean isSeekbarVisible, isTouchedSurfaceView, isUeserChanged, isActivityRun, isLocked;

    Thread updateVideoProgress;

    private OnScrollLinstener onScrollLinstener;

    private int step = 5;

    private String fileName = Environment.getExternalStorageDirectory() + "/1000.mp4";

    private static final int MSG_HIDDEN_VIDEO = 0x01016489;

    private static final int MSG_SHOW_VIDEO = 0x34546489;

    private static final int MSG_SET_CURRENTTIME = 0x21211235;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_CURRENTTIME:
                    if (currentTime != null) {
                        position = msg.getData().getInt("position");
                        Date date = new Date(position);
                        String t = format.format(date);
                        currentTime.setText(t);
                    }
                    break;
                case MSG_SHOW_VIDEO:
                    setControlVisibility(true);
                    sendHiddenMessageDelayed();
                    break;

                case MSG_HIDDEN_VIDEO:
                    setControlVisibility(false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.content_video_player);

        //initialize 
        backButton = (ImageView) findViewById(R.id.back_button);
        rewindButton = (ImageView) findViewById(R.id.rewind_button);
        playButton = (ImageView) findViewById(R.id.play_button);
        forwindButton = (ImageView) findViewById(R.id.forward_button);
        lockButton = (ImageView) findViewById(R.id.lock_button);
        onlockButton = (ImageView) findViewById(R.id.onlock_button);
        scrollImage = (ImageView) findViewById(R.id.scroll_image);

        titleText = (TextView) findViewById(R.id.title);
        currentTime = (TextView) findViewById(R.id.current_time);
        maxTime = (TextView) findViewById(R.id.max_time);
        scrollText = (TextView) findViewById(R.id.scroll_text);

        seekBar = (SeekBar) findViewById(R.id.seek_bar);

        surfaceView = (SurfaceView) findViewById(R.id.surface_view);

        titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        controlerLayout = (LinearLayout) findViewById(R.id.controler);
        scrollLayout = (RelativeLayout) findViewById(R.id.scroll_layout);

        titleText.setText(fileName);

        holder = surfaceView.getHolder();
        player = new MediaPlayer();


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        backButton.setOnClickListener(this);
        rewindButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        forwindButton.setOnClickListener(this);
        surfaceView.setOnClickListener(this);
        lockButton.setOnClickListener(this);
        onlockButton.setOnClickListener(this);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int pressedPosition;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                pressedPosition = i;
                if (b) {
                    player.seekTo(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUeserChanged = true;
                setControlVisibility(true);

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUeserChanged = false;
                sendHiddenMessageDelayed();
                player.seekTo(pressedPosition);
            }
        });

        holder.setKeepScreenOn(true);
        holder.addCallback(this);

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        play(fileName);
        onScrollLinstener = new OnScrollLinstener(this, surfaceView, new OnScrollLinstener.OnScrolled() {


            @Override
            public void horizontalScroll(float distanceX) {
                if (distanceX >= OnScrollLinstener.dp2px(VideoPlayerActivity.this, step)) {
                    if (position > 200) {
                        position -= 200;
                    } else {
                        position = 0;
                    }
                } else if (distanceX <= -OnScrollLinstener.dp2px(VideoPlayerActivity.this, step)) {// 快进
                    if (position < max - 300) {// 避免超过总时长
                        position += 200;// scroll执行一次快进3秒
                    } else {
                        position = max;
                    }
                }
                if (position < 0) {
                    position = 0;
                }
                player.seekTo(position);
                if (!player.isPlaying()) {
                    seekBar.setProgress(position);
                }
            }

            @Override
            public void rightVerticalScroll(float distanceY) {
                scrollLayout.setVisibility(View.VISIBLE);
                currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
                if (distanceY >= OnScrollLinstener.dp2px(VideoPlayerActivity.this, step)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (currentVolume < maxVolume) {// 为避免调节过快，distanceY应大于一个设定值
                        currentVolume++;
                       
                    }
                } else if (distanceY <= -OnScrollLinstener.dp2px(VideoPlayerActivity.this, step)) {// 音量调小
                    if (currentVolume > 0) {
                        currentVolume--;
                        
                    }
                }
                if (currentVolume >= 100) {
                    currentVolume = 100;
                    scrollImage.setImageResource(R.drawable.volume_high);
                }else if (currentVolume <= 0) {// 静音，设定静音独有的图片
                    currentVolume = 0;
                    scrollImage.setImageResource(R.drawable.volume_mute);
                }else {
                    scrollImage.setImageResource(R.drawable.volume);

                }
                int percentage = (currentVolume * 100) / maxVolume;
                scrollText.setText(percentage + "%");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            }

            @Override
            public void leftVerticalScroll(float distanceY) {
                scrollLayout.setVisibility(View.VISIBLE);
                float mBrightness = getWindow().getAttributes().screenBrightness;
                if (mBrightness == -1)
                    mBrightness = 0.5f;
                scrollImage.setImageResource(R.drawable.brightness);
                if (distanceY >= OnScrollLinstener.dp2px(VideoPlayerActivity.this, step)) {// 音量调大,注意横屏时的坐标体系,尽管左上角是原点，但横向向上滑动时distanceY为正
                    if (mBrightness < 1.0) {// 为避免调节过快，distanceY应大于一个设定值
                        mBrightness += 0.1;
                    }
                } else if (distanceY <= -OnScrollLinstener.dp2px(VideoPlayerActivity.this, step)) {// 音量调小
                    if (mBrightness > 0) {
                        mBrightness -= 0.1;
                    }
                }

                WindowManager.LayoutParams lpa = getWindow().getAttributes();
                if (mBrightness > 1.0f)
                    mBrightness = 1.0f;
                else if (mBrightness < 0.01f)
                    mBrightness = 0.01f;
                lpa.screenBrightness = mBrightness;
                getWindow().setAttributes(lpa);
                scrollText.setText((int) (mBrightness * 100) + "%");

            }

            @Override
            public void onTouch(View v, MotionEvent event) {
                isTouchedSurfaceView = true;
                setControlVisibility(true);
                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    Log.e("onTouch", "MotionEvent.ACTION_UP-----isSeekBarVisible : " + isSeekbarVisible);
                    sendHiddenMessageDelayed();
                    isTouchedSurfaceView = false;
                    scrollLayout.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onClick(View view) {
        handler.sendEmptyMessage(MSG_SHOW_VIDEO);

        switch (view.getId()) {
            case R.id.rewind_button:
                position=player.getCurrentPosition() - 1000;
                player.seekTo(position);
                seekBar.setProgress(position);
                break;
            case R.id.forward_button:
                position=player.getCurrentPosition() + 1000;
                player.seekTo(position);
                seekBar.setProgress(position);
                break;
            case R.id.lock_button:
                Log.e("lock_button","onScroll前");
                onScrollLinstener.cancelScroll();
                Log.e("lock_button","onScroll后");
                setControlVisibility(false);
                isLocked = true;
                onlockButton.setVisibility(View.VISIBLE);
                break;
            case R.id.onlock_button:
                onScrollLinstener.reconverScroll();
                setControlVisibility(true);
                isLocked = false;
                onlockButton.setVisibility(View.GONE);
                break;
            case R.id.play_button:
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
                break;
            case R.id.back_button:
                finish();
                break;
//            case R.id.surface_view:
//                break;
        }
    }


    private void play(String filePath) {
        player.reset();
        try {
            player.setDataSource(filePath);
            player.prepare();
            player.setDisplay(holder);
            max = player.getDuration();
            seekBar.setMax(max);
            maxTime.setText(format.format(new Date(player.getDuration())));
            player.start();
            isSeekbarVisible = true;
            progressDisplay();

        } catch (IOException e) {
            Toast.makeText(VideoPlayerActivity.this, "加载文件失败,请重试", Toast.LENGTH_SHORT).show();
        }
    }


    private void progressDisplay() {

        updateVideoProgress = new Thread() {
            @Override
            public void run() {
                {
                    while (isActivityRun) {
                        if (player.isPlaying() && isSeekbarVisible) {
                            Message msg = Message.obtain();
                            msg.what = MSG_SET_CURRENTTIME;
                            Bundle bundle = new Bundle();
                            bundle.putInt("position", player.getCurrentPosition());
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            if (!isUeserChanged)
                                seekBar.setProgress(player.getCurrentPosition());
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };
        updateVideoProgress.start();

        sendHiddenMessageDelayed();
    }

    private void sendHiddenMessageDelayed() {
        if (isTouchedSurfaceView) return ;
        handler.removeMessages(MSG_HIDDEN_VIDEO);
        handler.sendEmptyMessageDelayed(MSG_HIDDEN_VIDEO, 2000);
    }

    private void setControlVisibility(boolean isVisibel) {
        if ((isSeekbarVisible = isVisibel) && (!isLocked)) {
            titleLayout.setVisibility(View.VISIBLE);
            controlerLayout.setVisibility(View.VISIBLE);
        } else {
            titleLayout.setVisibility(View.INVISIBLE);
            controlerLayout.setVisibility(View.INVISIBLE);
        }
    }
    
    @Override
    protected void onResume() {
//        player.start();
        isActivityRun = true;
        super.onResume();
    }

    @Override
    protected void onPause() {
        player.stop();
        position = player.getCurrentPosition();
        isActivityRun = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        isActivityRun = false;

        if (player.isPlaying()) player.stop();
        player.release();
        super.onDestroy();
    }

    public void fitScreen() {
        // 当prepare完成后，该方法触发，在这里我们播放视频    

        //首先取得video的宽和高    
        int vWidth = player.getVideoWidth();
        int vHeight = player.getVideoHeight();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mSurfaceViewWidth = dm.widthPixels;
        int mSurfaceViewHeight = dm.heightPixels;


        if (vWidth > mSurfaceViewWidth || vHeight > mSurfaceViewHeight) {
            //如果video的宽或者高超出了当前屏幕的大小，则要进行缩放    
            float wRatio = (float) vWidth / (float) mSurfaceViewHeight;
            float hRatio = (float) vHeight / (float) mSurfaceViewHeight;

            //选择大的一个进行缩放    
            float ratio = Math.max(wRatio, hRatio);

            vWidth = (int) Math.ceil((float) vWidth / ratio);
            vHeight = (int) Math.ceil((float) vHeight / ratio);

            //设置surfaceView的布局参数    
            surfaceView.setLayoutParams(new RelativeLayout.LayoutParams(vWidth, vHeight));

            //然后开始播放视频    

            player.start();
        }
    }
}
