package com.example.vcam;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.InputConfiguration;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMain implements IXposedHookLoadPackage {
    private static final String PACKAGE_NAME = "com.ss.android.ugc.aweme@com.polaris.recorder@com.imendon.fomz";
    public static Surface mSurface;
    public static SurfaceTexture mSurfacetexture;
    public static MediaPlayer mMediaPlayer;
    public static SurfaceTexture fake_SurfaceTexture;
    public static Camera origin_preview_camera;

    public static Camera camera_onPreviewFrame;
    public static Camera start_preview_camera;
    public static volatile byte[] data_buffer = {0};
    public static byte[] input;
    public static int mhight;
    public static int mwidth;
    public static boolean is_someone_playing;
    public static boolean is_hooked;
    public static VideoToFrames c1_hw_decode_obj;
    public static VideoToFrames c2_hw_decode_obj;
    public static VideoToFrames c3_hw_decode_obj;
    public static SurfaceTexture c1_fake_texture;
    public static Surface c1_fake_surface;
    public static SurfaceHolder ori_holder;
    public static MediaPlayer mplayer1;
    public static Camera mcamera1;
    public int imageReaderFormat = 0;
    public static boolean is_first_hook_build = true;

    public static int onemhight;
    public static int onemwidth;
    public static Class camera_callback_calss;

    public static String video_path = "/storage/emulated/0/DCIM/Camera1/";
	public static String mp3filepath = video_path + "virtual.wav";

    public static Surface c2_preview_Surfcae;
    public static Surface c2_preview_Surfcae_1;
    public static Surface c2_reader_Surfcae;
    public static Surface c2_reader_Surfcae_1;
    public static MediaPlayer c2_player;
    public static MediaPlayer c2_player_1;
    public static Surface c2_virtual_surface;
    public static SurfaceTexture c2_virtual_surfaceTexture;
    public boolean need_recreate;
    public static CameraDevice.StateCallback c2_state_cb;
    public static CaptureRequest.Builder c2_builder;
    public static SessionConfiguration fake_sessionConfiguration;
    public static SessionConfiguration sessionConfiguration;
    public static OutputConfiguration outputConfiguration;
    public boolean need_to_show_toast = true;

    public int c2_ori_width = 1280;
    public int c2_ori_height = 720;

    public static Class c2_state_callback;
    public Context toast_content;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Exception {
        if (!PACKAGE_NAME.contains(lpparam.packageName)) {
            return;
        }

        if (LogToFileUtils.islog){
            LogToFileUtils.init(toast_content);
            LogToFileUtils.write(String.format("PACKAGE_NAME[%s]", lpparam.packageName));

            XposedHelpers.findAndHookMethod("android.util.Log", lpparam.classLoader, "v", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String tag = (String) param.args[0];
                    String msg = (String) param.args[1];
                    LogToFileUtils.write(String.format("[%s]:%s", tag, msg));
                }
            });

            XposedHelpers.findAndHookMethod("android.util.Log", lpparam.classLoader, "d", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String tag = (String) param.args[0];
                    String msg = (String) param.args[1];
                    LogToFileUtils.write(String.format("[%s]:%s", tag, msg));
                }
            });

            XposedHelpers.findAndHookMethod("android.util.Log", lpparam.classLoader, "i", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String tag = (String) param.args[0];
                    String msg = (String) param.args[1];
                    LogToFileUtils.write(String.format("[%s]:%s", tag, msg));
                }
            });

            XposedHelpers.findAndHookMethod("android.util.Log", lpparam.classLoader, "w", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String tag = (String) param.args[0];
                    String msg = (String) param.args[1];
                    LogToFileUtils.write(String.format("[%s]:%s", tag, msg));
                }
            });

            XposedHelpers.findAndHookMethod("android.util.Log", lpparam.classLoader, "e", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String tag = (String) param.args[0];
                    String msg = (String) param.args[1];
                    LogToFileUtils.write(String.format("[%s]:%s", tag, msg));
                }
            });
        }


        //hook 麦克风
        XposedHelpers.findAndHookConstructor("android.media.AudioRecord", lpparam.classLoader, int.class , int.class ,  int.class , int.class , int.class , new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				/**
				* audioSource：音频输入源，比如有麦克风等，通过MediaRecord.AudioSource获取。
                * sampleRateInHz：音频采样率，常见的采样率为44100即44.1KHZ
                * channelConfig：音频录制时的声道，分为单声道和立体声道，在AudioFormat中定义。
                * audioFormat：音频格式
                * bufferSizeInBytes：音频缓冲区大小，不同手机厂商有不同的实现（比如 我的一加手机该值为3584字节），可以通过下面的方法获取。
				**/
				//1 24000 16 2 3840
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord创建对象 beforeHookedMethod");
                for (int i = 0; i < param.args.length; i++) {
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord创建对象 param[%s]:%s", i, param.args[i]));
                }
				AudioUtils.sampleRate = Float.parseFloat(String.valueOf(param.args[1]));
				AudioUtils.channels = (int)param.args[3];
				AudioUtils.bufferSize = (int)param.args[4];
            }
        });

        XposedHelpers.findAndHookMethod("android.media.AudioRecord", lpparam.classLoader, "startRecording" , new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord startRecording");
                AudioUtils.release();
                if(!AudioUtils.isBegin){
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord创建对象 读取音频文件开始");

                    //AudioInputStream audioInputStream = AudioUtils.getPcmAudioInputStream(mp3filepath);
                    DataInputStream audioInputStream = AudioUtils.getPcmAudioInputStream(mp3filepath);
                    if(audioInputStream == null){
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord创建对象 读取音频文件失败");
                    }else{
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord创建对象 读取音频文件成功");
                        AudioUtils.isBegin = true;
                    }
                }
                //开始hook read
                process_audiorecord_read(lpparam);
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "setPreviewTexture", SurfaceTexture.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                File file = new File(video_path + "virtual.mp4");
                if (file.exists()) {
                    File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                    if (control_file.exists()){
                        return;
                    }
                    if (is_hooked) {
                        is_hooked = false;
                        return;
                    }
                    if (param.args[0] == null) {
                        return;
                    }
                    if (param.args[0].equals(c1_fake_texture)) {
                        return;
                    }
                    if (origin_preview_camera != null && origin_preview_camera.equals(param.thisObject)) {
                        param.args[0] = fake_SurfaceTexture;
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]发现重复" + origin_preview_camera.toString());
                        return;
                    } else {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]创建预览");
                    }

                    origin_preview_camera = (Camera) param.thisObject;
                    mSurfacetexture = (SurfaceTexture) param.args[0];
                    if (fake_SurfaceTexture == null) {
                        fake_SurfaceTexture = new SurfaceTexture(10);
                    } else {
                        fake_SurfaceTexture.release();
                        fake_SurfaceTexture = new SurfaceTexture(10);
                    }
                    param.args[0] = fake_SurfaceTexture;
                } else {
                    File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                    need_to_show_toast = !toast_control.exists();
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "不存在替换视频\n" + lpparam.packageName + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.camera2.CameraManager", lpparam.classLoader, "openCamera", String.class, CameraDevice.StateCallback.class, Handler.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]Environment.getExternalStorageDirectory().getPath():" + Environment.getExternalStorageDirectory().getPath());
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]video_path:" + video_path);
                if (param.args[1] == null) {
                    return;
                }
                if (param.args[1].equals(c2_state_cb)) {
                    return;
                }
                c2_state_cb = (CameraDevice.StateCallback) param.args[1];
                c2_state_callback = param.args[1].getClass();
                File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                if (control_file.exists()) {
                    return;
                }
                File file = new File(video_path + "virtual.mp4");
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                if (!file.exists()) {
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]不存在替换视频:" + lpparam.packageName + " 当前路径：" + video_path);
                            Toast.makeText(toast_content, "不存在替换视频\n" + lpparam.packageName + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    return;
                }
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]1位参数初始化相机，类：" + c2_state_callback.toString());
                is_first_hook_build = true;
                process_camera2_init(c2_state_callback);
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            XposedHelpers.findAndHookMethod("android.hardware.camera2.CameraManager", lpparam.classLoader, "openCamera", String.class, Executor.class, CameraDevice.StateCallback.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args[2] == null) {
                        return;
                    }
                    if (param.args[2].equals(c2_state_cb)) {
                        return;
                    }
                    c2_state_cb = (CameraDevice.StateCallback) param.args[2];
                    File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                    if (control_file.exists()) {
                        return;
                    }
                    File file = new File(video_path + "virtual.mp4");
                    File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                    need_to_show_toast = !toast_control.exists();
                    if (!file.exists()) {
                        if (toast_content != null && need_to_show_toast) {
                            try {
                                Toast.makeText(toast_content, "不存在替换视频\n" + lpparam.packageName + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                            } catch (Exception ee) {
                                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                            }
                        }
                        return;
                    }
                    c2_state_callback = param.args[2].getClass();
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]2位参数初始化相机，类：" + c2_state_callback.toString());
                    is_first_hook_build = true;
                    process_camera2_init(c2_state_callback);
                }
            });
        }


        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "setPreviewCallbackWithBuffer", Camera.PreviewCallback.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] != null) {
                    process_callback(param);
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "addCallbackBuffer", byte[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] != null) {
                    param.args[0] = new byte[((byte[]) param.args[0]).length];
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "setPreviewCallback", Camera.PreviewCallback.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] != null) {
                    process_callback(param);
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "setOneShotPreviewCallback", Camera.PreviewCallback.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] != null) {
                    process_callback(param);
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "takePicture", Camera.ShutterCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, Camera.PictureCallback.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]4参数拍照");
                if (param.args[1] != null) {
                    process_a_shot_YUV(param);
                }

                if (param.args[3] != null) {
                    process_a_shot_jpeg(param, 3);
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.media.MediaRecorder", lpparam.classLoader, "setCamera", Camera.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][record]" + lpparam.packageName);
                if (toast_content != null && need_to_show_toast) {
                    try {
                        Toast.makeText(toast_content, "应用：" + lpparam.appInfo.name + "(" + lpparam.packageName + ")" + "触发了录像，但目前无法拦截", Toast.LENGTH_SHORT).show();
                    }catch (Exception ee){
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.app.Instrumentation", lpparam.classLoader, "callApplicationOnCreate", Application.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (param.args[0] instanceof Application) {
                    try {
                        toast_content = ((Application) param.args[0]).getApplicationContext();
                    } catch (Exception ee) {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(ee));
                    }
                    File force_private = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera1/private_dir.jpg");
                    if (toast_content != null) {//后半段用于强制私有目录
                        int auth_statue = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                auth_statue += (toast_content.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) + 1);
                            } catch (Exception ee) {
                                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][permission-check]" + getStackTraceString(ee));
                            }
                            try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                    auth_statue += (toast_content.checkSelfPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE) + 1);
                                }
                            } catch (Exception ee) {
                                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][permission-check]" + getStackTraceString(ee));
                            }
                        }else {
                            if (toast_content.checkCallingPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ){
                                auth_statue = 2;
                            }
                        }
                        //权限判断完毕
                        if (auth_statue < 1 || force_private.exists()) {
                            File shown_file = new File(toast_content.getExternalFilesDir(null).getAbsolutePath() + "/Camera1/");
                            if ((!shown_file.isDirectory()) && shown_file.exists()) {
                                shown_file.delete();
                            }
                            if (!shown_file.exists()) {
                                shown_file.mkdir();
                            }
                            shown_file = new File(toast_content.getExternalFilesDir(null).getAbsolutePath() + "/Camera1/" + "has_shown");
                            File toast_force_file = new File(Environment.getExternalStorageDirectory().getPath()+ "/DCIM/Camera1/force_show.jpg");
                            if ((!lpparam.packageName.equals(BuildConfig.APPLICATION_ID)) && ((!shown_file.exists()) || toast_force_file.exists())) {
                                try {
                                    Toast.makeText(toast_content, lpparam.packageName+"未授予读取本地目录权限，请检查权限\nCamera1目前重定向为 " + toast_content.getExternalFilesDir(null).getAbsolutePath() + "/Camera1/", Toast.LENGTH_SHORT).show();
                                    FileOutputStream fos = new FileOutputStream(toast_content.getExternalFilesDir(null).getAbsolutePath() + "/Camera1/" + "has_shown");
                                    String info = "shown";
                                    fos.write(info.getBytes());
                                    fos.flush();
                                    fos.close();
                                } catch (Exception ee) {
                                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][switch-dir]" + getStackTraceString(ee));
                                }
                            }
                            video_path = toast_content.getExternalFilesDir(null).getAbsolutePath() + "/Camera1/";
                        }else {
                            video_path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/";
                        }
                    } else {
                        video_path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/";
                        File uni_DCIM_path = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/");
                        if (uni_DCIM_path.canWrite()) {
                            File uni_Camera1_path = new File(video_path);
                            if (!uni_Camera1_path.exists()) {
                                uni_Camera1_path.mkdir();
                            }
                        }
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "startPreview", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                File file = new File(video_path + "virtual.mp4");
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                if (!file.exists()) {
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "不存在替换视频\n" + lpparam.packageName + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    return;
                }
                File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                if (control_file.exists()) {
                    return;
                }
                is_someone_playing = false;
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]开始预览");
                start_preview_camera = (Camera) param.thisObject;
                if (ori_holder != null) {

                    if (mplayer1 == null) {
                        mplayer1 = new MediaPlayer();
                    } else {
                        mplayer1.release();
                        mplayer1 = null;
                        mplayer1 = new MediaPlayer();
                    }
                    if (!ori_holder.getSurface().isValid() || ori_holder == null) {
                        return;
                    }
                    mplayer1.setSurface(ori_holder.getSurface());
                    File sfile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no-silent.jpg");
                    if (!(sfile.exists() && (!is_someone_playing))) {
                        mplayer1.setVolume(0, 0);
                        is_someone_playing = false;
                    } else {
                        is_someone_playing = true;
                    }
                    mplayer1.setLooping(true);

                    mplayer1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]播放声音");
                            mplayer1.start();
                        }
                    });

                    try {
                        mplayer1.setDataSource(video_path + "virtual.mp4");
                        mplayer1.prepare();
                    } catch (IOException ee) {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(ee));
                    }
                }


                if (mSurfacetexture != null) {
                    if (mSurface == null) {
                        mSurface = new Surface(mSurfacetexture);
                    } else {
                        mSurface.release();
                        mSurface = new Surface(mSurfacetexture);
                    }

                    if (mMediaPlayer == null) {
                        mMediaPlayer = new MediaPlayer();
                    } else {
                        mMediaPlayer.release();
                        mMediaPlayer = new MediaPlayer();
                    }

                    mMediaPlayer.setSurface(mSurface);

                    File sfile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no-silent.jpg");
                    if (!(sfile.exists() && (!is_someone_playing))) {
                        mMediaPlayer.setVolume(0, 0);
                        is_someone_playing = false;
                    } else {
                        is_someone_playing = true;
                    }
                    mMediaPlayer.setLooping(true);

                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]播放声音");
                            mMediaPlayer.start();
                        }
                    });

                    try {
                        mMediaPlayer.setDataSource(video_path + "virtual.mp4");
                        mMediaPlayer.prepare();
                    } catch (IOException ee) {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(ee));
                    }
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "setPreviewDisplay", SurfaceHolder.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]添加Surfaceview预览");
                File file = new File(video_path + "virtual.mp4");
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                if (!file.exists()) {
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "不存在替换视频\n" + lpparam.packageName + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    return;
                }
                File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                if (control_file.exists()) {
                    return;
                }
                mcamera1 = (Camera) param.thisObject;
                ori_holder = (SurfaceHolder) param.args[0];
                if (c1_fake_texture == null) {
                    c1_fake_texture = new SurfaceTexture(11);
                } else {
                    c1_fake_texture.release();
                    c1_fake_texture = null;
                    c1_fake_texture = new SurfaceTexture(11);
                }

                if (c1_fake_surface == null) {
                    c1_fake_surface = new Surface(c1_fake_texture);
                } else {
                    c1_fake_surface.release();
                    c1_fake_surface = null;
                    c1_fake_surface = new Surface(c1_fake_texture);
                }
                is_hooked = true;
                mcamera1.setPreviewTexture(c1_fake_texture);
                param.setResult(null);
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.camera2.CaptureRequest.Builder", lpparam.classLoader, "addTarget", Surface.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.args[0] == null) {
                    return;
                }
                if (param.thisObject == null) {
                    return;
                }
                File file = new File(video_path + "virtual.mp4");
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                if (!file.exists()) {
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "不存在替换视频\n" + lpparam.packageName + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    return;
                }
                if (param.args[0].equals(c2_virtual_surface)) {
                    return;
                }
                File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                if (control_file.exists()) {
                    return;
                }
                String surfaceInfo = param.args[0].toString();
                if (surfaceInfo.contains("Surface(name=null)")) {
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]surfaceInfo contains Surface(name=null)");
                    if (c2_reader_Surfcae == null) {
                        c2_reader_Surfcae = (Surface) param.args[0];
                    } else {
                        if ((!c2_reader_Surfcae.equals(param.args[0])) && c2_reader_Surfcae_1 == null) {
                            c2_reader_Surfcae_1 = (Surface) param.args[0];
                        }
                    }
                } else {
                    if (c2_preview_Surfcae == null) {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]c2_preview_Surfcae is null");
                        c2_preview_Surfcae = (Surface) param.args[0];
                    } else {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]c2_preview_Surfcae is not null");
                        if ((!c2_preview_Surfcae.equals(param.args[0])) && c2_preview_Surfcae_1 == null) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]c2_preview_Surfcae not equal current Surfcae, c2_preview_Surfcae_1 is null");
                            c2_preview_Surfcae_1 = (Surface) param.args[0];
                        }
                    }
                }
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]添加目标：" + param.args[0].toString());
                param.args[0] = c2_virtual_surface;

            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.camera2.CaptureRequest.Builder", lpparam.classLoader, "removeTarget", Surface.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {

                if (param.args[0] == null) {
                    return;
                }
                if (param.thisObject == null) {
                    return;
                }
                File file = new File(video_path + "virtual.mp4");
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                if (!file.exists()) {
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "不存在替换视频\n" + lpparam.packageName + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    return;
                }
                File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                if (control_file.exists()) {
                    return;
                }
                Surface rm_surf = (Surface) param.args[0];
                if (rm_surf.equals(c2_preview_Surfcae)) {
                    c2_preview_Surfcae = null;
                }
                if (rm_surf.equals(c2_preview_Surfcae_1)) {
                    c2_preview_Surfcae_1 = null;
                }
                if (rm_surf.equals(c2_reader_Surfcae_1)) {
                    c2_reader_Surfcae_1 = null;
                }
                if (rm_surf.equals(c2_reader_Surfcae)) {
                    c2_reader_Surfcae = null;
                }

                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]移除目标：" + param.args[0].toString());
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.camera2.CaptureRequest.Builder", lpparam.classLoader, "build", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject == null) {
                    return;
                }
                if (param.thisObject.equals(c2_builder)) {
                    return;
                }
                c2_builder = (CaptureRequest.Builder) param.thisObject;
                File file = new File(video_path + "virtual.mp4");
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                if (!file.exists() && need_to_show_toast) {
                    if (toast_content != null) {
                        try {
                            Toast.makeText(toast_content, "不存在替换视频\n" + lpparam.packageName + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    return;
                }

                File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                if (control_file.exists()) {
                    return;
                }
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]开始build请求");
                process_camera2_play();
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.Camera", lpparam.classLoader, "stopPreview", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (param.thisObject.equals(HookMain.origin_preview_camera) || param.thisObject.equals(HookMain.camera_onPreviewFrame) || param.thisObject.equals(HookMain.mcamera1)) {
                    if (c1_hw_decode_obj != null) {
                        c1_hw_decode_obj.stopDecode();
                    }
                    if (mplayer1 != null) {
                        mplayer1.release();
                        mplayer1 = null;
                    }
                    if (mMediaPlayer != null) {
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                    is_someone_playing = false;

                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]停止预览");
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.media.ImageReader", lpparam.classLoader, "newInstance", int.class, int.class, int.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]应用创建了渲染器：宽：" + param.args[0] + " 高：" + param.args[1] + "格式：" + param.args[2]);
                c2_ori_width = (int) param.args[0];
                c2_ori_height = (int) param.args[1];
                imageReaderFormat = (int) param.args[2];
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                if (toast_content != null && need_to_show_toast) {
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]应用创建了渲染器：宽：" + param.args[0] + " 高：" + param.args[1] + " 一般只需要宽高比与视频相同");
                }
            }
        });

        XposedHelpers.findAndHookMethod("android.hardware.camera2.CameraCaptureSession.CaptureCallback", lpparam.classLoader, "onCaptureFailed", CameraCaptureSession.class, CaptureRequest.class, CaptureFailure.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]onCaptureFailed" + "原因：" + ((CaptureFailure) param.args[2]).getReason());
                    }
                });
    }

    private void process_audiorecord_read(final XC_LoadPackage.LoadPackageParam lpparam){
        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord 开始hook read");

        XposedHelpers.findAndHookMethod("android.media.AudioRecord", lpparam.classLoader, "read" , ByteBuffer.class , int.class ,  int.class ,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(AudioUtils.isBegin){
                    ByteBuffer data = (ByteBuffer) param.args[0];
                    data.clear();
                    //new short[(int) param.args[2]];
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord data[%s]", data.capacity()));
                    DataInputStream audioInputStream = AudioUtils.getPcmAudioInputStream(mp3filepath);
                    int i = 0;

                    while (audioInputStream.available() > 0 && i < data.capacity()) {
                        //录音时write Byte 那么读取时就该为readByte要相互对应
                        data.put(audioInputStream.readByte());
                        i++;
                    }
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord data[%s]", data.capacity()));
                }
            }

        });

        /*
        XposedHelpers.findAndHookMethod("android.media.AudioRecord", lpparam.classLoader, "read" , byte[].class , int.class ,  int.class ,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord afterHookedMethod");
                for (int i = 0; i < param.args.length; i++) {
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord param[%s]:%s", i, param.args[i]));
                }
                //在此处重写音频数据
                //音频数据在param.arg
            }

        });*/

        XposedHelpers.findAndHookMethod("android.media.AudioRecord", lpparam.classLoader, "read" , byte[].class , int.class ,  int.class ,  int.class ,  new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(AudioUtils.isBegin){
                    byte[] data = (byte[]) param.args[0];
                    //new short[(int) param.args[2]];
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord data[%s]", data.length));
                    DataInputStream audioInputStream = AudioUtils.getPcmAudioInputStream(mp3filepath);
                    int i = 0;
                    if (audioInputStream.available() <= 0){
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord 啥情况", data.length));
                    }
                    while (audioInputStream.available() > 0 && i < data.length) {
                        //录音时write Byte 那么读取时就该为readByte要相互对应
                        data[i] = audioInputStream.readByte();
                        i++;
                    }
//                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord data[%s]", data.length));
                }else {
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord 啥情况!!!!");
                }
            }

        });

        XposedHelpers.findAndHookMethod("android.media.AudioRecord", lpparam.classLoader, "read" , short[].class , int.class ,  int.class ,  int.class ,  new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //在此处重写音频数据
                //音频数据在param.arg
                //四个参数
                //param.arg[0] short数组
                //param.arg[2] 数组最大长度
				if(AudioUtils.isBegin){
                    short[] data = (short[]) param.args[0];
                    //new short[(int) param.args[2]];
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord data[%s]", data.length));
                    DataInputStream audioInputStream = AudioUtils.getPcmAudioInputStream(mp3filepath);
                    int i = 0;
                    while (audioInputStream.available() > 0 && i < data.length) {
                        //录音时write Byte 那么读取时就该为readByte要相互对应
                        data[i] = audioInputStream.readShort();
                        i++;
                    }
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord data[%s]", data.length));
                    //param.args[0] = data;
				}
            }

        });

        XposedHelpers.findAndHookMethod("android.media.AudioRecord", lpparam.classLoader, "read" , float[].class , int.class ,  int.class ,  int.class ,  new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord beforeHookedMethod");
//                for (int i = 0; i < param.args.length; i++) {
//                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord param[%s]:%s", i, param.args[i]));
//                }
//            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(AudioUtils.isBegin){
                    float[] data = (float[]) param.args[0];
                    //new short[(int) param.args[2]];
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord data[%s]", data.length));
                    DataInputStream audioInputStream = AudioUtils.getPcmAudioInputStream(mp3filepath);
                    int i = 0;
                    while (audioInputStream.available() > 0 && i < data.length) {
                        //录音时write Byte 那么读取时就该为readByte要相互对应
                        data[i] = audioInputStream.readFloat();
                        i++;
                    }
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+String.format("[VCAMLOG]麦克风AudioRecord data[%s]", data.length));
                }
            }

        });
		
		/**
         * 停止录音
         */
        XposedHelpers.findAndHookMethod("android.media.AudioRecord", lpparam.classLoader, "stop" , new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord stop beforeHookedMethod");
                //停止hook麦克风，全局
				//AudioUtils.release();
            }
        });
		

        /**
         * 释放录音
         */
        XposedHelpers.findAndHookMethod("android.media.AudioRecord", lpparam.classLoader, "release" , new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]麦克风AudioRecord release beforeHookedMethod");
                //停止hook麦克风，全局
                //AudioUtils.release();
            }
        });

    }

    private void process_camera2_play() {

        if (c2_reader_Surfcae != null) {
            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]process_camera2_play c2_reader_Surfcae");
            if (c2_hw_decode_obj != null) {
                c2_hw_decode_obj.stopDecode();
                c2_hw_decode_obj = null;
            }

            c2_hw_decode_obj = new VideoToFrames();
            try {
                if (imageReaderFormat == 256) {
                    c2_hw_decode_obj.setSaveFrames("null", OutputImageFormat.JPEG);
                } else {
                    c2_hw_decode_obj.setSaveFrames("null", OutputImageFormat.NV21);
                }
                c2_hw_decode_obj.set_surfcae(c2_reader_Surfcae);
                c2_hw_decode_obj.decode(video_path + "virtual.mp4");
            } catch (Throwable throwable) {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(throwable));
            }
        }

        if (c2_reader_Surfcae_1 != null) {
            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]process_camera2_play c2_reader_Surfcae_1");
            if (c3_hw_decode_obj != null) {
                c3_hw_decode_obj.stopDecode();
                c3_hw_decode_obj = null;
            }

            c3_hw_decode_obj = new VideoToFrames();
            try {
                if (imageReaderFormat == 256) {
                    c3_hw_decode_obj.setSaveFrames("null", OutputImageFormat.JPEG);
                } else {
                    c3_hw_decode_obj.setSaveFrames("null", OutputImageFormat.NV21);
                }
                c3_hw_decode_obj.set_surfcae(c2_reader_Surfcae_1);
                c3_hw_decode_obj.decode(video_path + "virtual.mp4");
            } catch (Throwable throwable) {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(throwable));
            }
        }

        if (c2_preview_Surfcae != null) {
            if (c2_player == null) {
                c2_player = new MediaPlayer();
            } else {
                c2_player.release();
                c2_player = new MediaPlayer();
            }
            c2_player.setSurface(c2_preview_Surfcae);
            File sfile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no-silent.jpg");
            if (!sfile.exists()) {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]设置播放音量为0");
                c2_player.setVolume(0, 0);
            }
            c2_player.setLooping(true);

            try {
                c2_player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]播放声音");
                        c2_player.start();
                    }
                });
                c2_player.setDataSource(video_path + "virtual.mp4");
                c2_player.prepare();
            } catch (Exception ee) {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][c2player][" + c2_preview_Surfcae.toString() + "]" + getStackTraceString(ee));
            }
        }

        if (c2_preview_Surfcae_1 != null) {
            if (c2_player_1 == null) {
                c2_player_1 = new MediaPlayer();
            } else {
                c2_player_1.release();
                c2_player_1 = new MediaPlayer();
            }
            c2_player_1.setSurface(c2_preview_Surfcae_1);
            File sfile = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no-silent.jpg");
            if (!sfile.exists()) {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]设置播放音量为0");
                c2_player_1.setVolume(0, 0);
            }
            c2_player_1.setLooping(true);

            try {
                c2_player_1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer mp) {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]播放声音");
                        c2_player_1.start();
                    }
                });
                c2_player_1.setDataSource(video_path + "virtual.mp4");
                c2_player_1.prepare();
            } catch (Exception ee) {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][c2player1]" + "[ " + c2_preview_Surfcae_1.toString() + "]" + getStackTraceString(ee));
            }
        }
        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]Camera2处理过程完全执行");
    }

    private Surface create_virtual_surface() {
        if (need_recreate) {
            if (c2_virtual_surfaceTexture != null) {
                c2_virtual_surfaceTexture.release();
                c2_virtual_surfaceTexture = null;
            }
            if (c2_virtual_surface != null) {
                c2_virtual_surface.release();
                c2_virtual_surface = null;
            }
            c2_virtual_surfaceTexture = new SurfaceTexture(15);
            c2_virtual_surface = new Surface(c2_virtual_surfaceTexture);
            need_recreate = false;
        } else {
            if (c2_virtual_surface == null) {
                need_recreate = true;
                c2_virtual_surface = create_virtual_surface();
            }
        }
        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]【重建垃圾场】" + c2_virtual_surface.toString());
        return c2_virtual_surface;
    }

    private void process_camera2_init(Class hooked_class) {
        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]process_camera2_init:" + hooked_class.getName());

        XposedHelpers.findAndHookMethod(hooked_class, "onOpened", CameraDevice.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                need_recreate = true;
                create_virtual_surface();
                if (c2_player != null) {
                    c2_player.stop();
                    c2_player.reset();
                    c2_player.release();
                    c2_player = null;
                }
                if (c3_hw_decode_obj != null) {
                    c3_hw_decode_obj.stopDecode();
                    c3_hw_decode_obj = null;
                }
                if (c2_hw_decode_obj != null) {
                    c2_hw_decode_obj.stopDecode();
                    c2_hw_decode_obj = null;
                }
                if (c2_player_1 != null) {
                    c2_player_1.stop();
                    c2_player_1.reset();
                    c2_player_1.release();
                    c2_player_1 = null;
                }
                c2_preview_Surfcae_1 = null;
                c2_reader_Surfcae_1 = null;
                c2_reader_Surfcae = null;
                c2_preview_Surfcae = null;
                is_first_hook_build = true;
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]打开相机C2");

                File file = new File(video_path + "virtual.mp4");
                File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                need_to_show_toast = !toast_control.exists();
                if (!file.exists()) {
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "不存在替换视频\n" + toast_content.getPackageName() + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    return;
                }
                XposedHelpers.findAndHookMethod(param.args[0].getClass(), "createCaptureSession", List.class, CameraCaptureSession.StateCallback.class, Handler.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam paramd) throws Throwable {
                        if (paramd.args[0] != null) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]createCaptureSession创捷捕获，原始:" + paramd.args[0].toString() + "虚拟：" + c2_virtual_surface.toString());
                            paramd.args[0] = Arrays.asList(c2_virtual_surface);
                            if (paramd.args[1] != null) {
                                process_camera2Session_callback((CameraCaptureSession.StateCallback) paramd.args[1]);
                            }
                        }
                    }
                });

                XposedHelpers.findAndHookMethod(param.args[0].getClass(), "close", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam paramd) throws Throwable {
                        LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]C2终止预览");
                        if (c2_hw_decode_obj != null) {
                            c2_hw_decode_obj.stopDecode();
                            c2_hw_decode_obj = null;
                        }
                        if (c3_hw_decode_obj != null) {
                            c3_hw_decode_obj.stopDecode();
                            c3_hw_decode_obj = null;
                        }
                        if (c2_player != null) {
                            c2_player.release();
                            c2_player = null;
                        }
                        if (c2_player_1 != null){
                            c2_player_1.release();
                            c2_player_1 = null;
                        }
                        c2_preview_Surfcae_1 = null;
                        c2_reader_Surfcae_1 = null;
                        c2_reader_Surfcae = null;
                        c2_preview_Surfcae = null;
                        need_recreate = true;
                        is_first_hook_build= true;
                    }
                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    XposedHelpers.findAndHookMethod(param.args[0].getClass(), "createCaptureSessionByOutputConfigurations", List.class, CameraCaptureSession.StateCallback.class, Handler.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (param.args[0] != null) {
                                outputConfiguration = new OutputConfiguration(c2_virtual_surface);
                                param.args[0] = Arrays.asList(outputConfiguration);

                                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]执行了createCaptureSessionByOutputConfigurations-144777");
                                if (param.args[1] != null) {
                                    process_camera2Session_callback((CameraCaptureSession.StateCallback) param.args[1]);
                                }
                            }
                        }
                    });
                }


                XposedHelpers.findAndHookMethod(param.args[0].getClass(), "createConstrainedHighSpeedCaptureSession", List.class, CameraCaptureSession.StateCallback.class, Handler.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        if (param.args[0] != null) {
                            param.args[0] = Arrays.asList(c2_virtual_surface);
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]执行了 createConstrainedHighSpeedCaptureSession -5484987");
                            if (param.args[1] != null) {
                                process_camera2Session_callback((CameraCaptureSession.StateCallback) param.args[1]);
                            }
                        }
                    }
                });


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    XposedHelpers.findAndHookMethod(param.args[0].getClass(), "createReprocessableCaptureSession", InputConfiguration.class, List.class, CameraCaptureSession.StateCallback.class, Handler.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (param.args[1] != null) {
                                param.args[1] = Arrays.asList(c2_virtual_surface);
                                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]执行了 createReprocessableCaptureSession ");
                                if (param.args[2] != null) {
                                    process_camera2Session_callback((CameraCaptureSession.StateCallback) param.args[2]);
                                }
                            }
                        }
                    });
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    XposedHelpers.findAndHookMethod(param.args[0].getClass(), "createReprocessableCaptureSessionByConfigurations", InputConfiguration.class, List.class, CameraCaptureSession.StateCallback.class, Handler.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (param.args[1] != null) {
                                outputConfiguration = new OutputConfiguration(c2_virtual_surface);
                                param.args[0] = Arrays.asList(outputConfiguration);
                                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]执行了 createReprocessableCaptureSessionByConfigurations");
                                if (param.args[2] != null) {
                                    process_camera2Session_callback((CameraCaptureSession.StateCallback) param.args[2]);
                                }
                            }
                        }
                    });
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    XposedHelpers.findAndHookMethod(param.args[0].getClass(), "createCaptureSession", SessionConfiguration.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (param.args[0] != null) {
                                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]执行了 createCaptureSession -5484987");
                                sessionConfiguration = (SessionConfiguration) param.args[0];
                                outputConfiguration = new OutputConfiguration(c2_virtual_surface);
                                fake_sessionConfiguration = new SessionConfiguration(sessionConfiguration.getSessionType(),
                                        Arrays.asList(outputConfiguration),
                                        sessionConfiguration.getExecutor(),
                                        sessionConfiguration.getStateCallback());
                                param.args[0] = fake_sessionConfiguration;
                                process_camera2Session_callback(sessionConfiguration.getStateCallback());
                            }
                        }
                    });
                }
            }
        });


        XposedHelpers.findAndHookMethod(hooked_class, "onError", CameraDevice.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]相机错误onerror：" + (int) param.args[1]);
            }

        });


        XposedHelpers.findAndHookMethod(hooked_class, "onDisconnected", CameraDevice.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]相机断开onDisconnected ：");
            }

        });


    }

    private void process_a_shot_jpeg(XC_MethodHook.MethodHookParam param, int index) {
        try {
            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]第二个jpeg:" + param.args[index].toString());
        } catch (Exception ee) {
            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(ee));

        }
        Class callback = param.args[index].getClass();

        XposedHelpers.findAndHookMethod(callback, "onPictureTaken", byte[].class, android.hardware.Camera.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam paramd) throws Throwable {
                try {
                    Camera loaclcam = (Camera) paramd.args[1];
                    onemwidth = loaclcam.getParameters().getPreviewSize().width;
                    onemhight = loaclcam.getParameters().getPreviewSize().height;
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]JPEG拍照回调初始化：宽：" + onemwidth + "高：" + onemhight + "对应的类：" + loaclcam.toString());
                    File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                    need_to_show_toast = !toast_control.exists();
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "发现拍照\n宽：" + onemwidth + "\n高：" + onemhight + "\n格式：JPEG", Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                    if (control_file.exists()) {
                        return;
                    }

                    Bitmap pict = getBMP(video_path + "1000.bmp");
                    ByteArrayOutputStream temp_array = new ByteArrayOutputStream();
                    pict.compress(Bitmap.CompressFormat.JPEG, 100, temp_array);
                    byte[] jpeg_data = temp_array.toByteArray();
                    paramd.args[0] = jpeg_data;
                } catch (Exception ee) {
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(ee));
                }
            }
        });
    }

    private void process_a_shot_YUV(XC_MethodHook.MethodHookParam param) {
        try {
            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]发现拍照YUV:" + param.args[1].toString());
        } catch (Exception ee) {
            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(ee));
        }
        Class callback = param.args[1].getClass();
        XposedHelpers.findAndHookMethod(callback, "onPictureTaken", byte[].class, android.hardware.Camera.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam paramd) throws Throwable {
                try {
                    Camera loaclcam = (Camera) paramd.args[1];
                    onemwidth = loaclcam.getParameters().getPreviewSize().width;
                    onemhight = loaclcam.getParameters().getPreviewSize().height;
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]YUV拍照回调初始化：宽：" + onemwidth + "高：" + onemhight + "对应的类：" + loaclcam.toString());
                    File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                    need_to_show_toast = !toast_control.exists();
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "发现拍照\n宽：" + onemwidth + "\n高：" + onemhight + "\n格式：YUV_420_888", Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
                    if (control_file.exists()) {
                        return;
                    }
                    input = getYUVByBitmap(getBMP(video_path + "1000.bmp"));
                    paramd.args[0] = input;
                } catch (Exception ee) {
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]" + getStackTraceString(ee));
                }
            }
        });
    }

    private void process_callback(XC_MethodHook.MethodHookParam param) {
        Class preview_cb_class = param.args[0].getClass();
        int need_stop = 0;
        File control_file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "disable.jpg");
        if (control_file.exists()) {
            need_stop = 1;
        }
        File file = new File(video_path + "virtual.mp4");
        File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
        need_to_show_toast = !toast_control.exists();
        if (!file.exists()) {
            if (toast_content != null && need_to_show_toast) {
                try {
                    Toast.makeText(toast_content, "不存在替换视频\n" + toast_content.getPackageName() + "当前路径：" + video_path, Toast.LENGTH_SHORT).show();
                } catch (Exception ee) {
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                }
            }
            need_stop = 1;
        }
        int finalNeed_stop = need_stop;
        XposedHelpers.findAndHookMethod(preview_cb_class, "onPreviewFrame", byte[].class, android.hardware.Camera.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam paramd) throws Throwable {
                Camera localcam = (android.hardware.Camera) paramd.args[1];
                if (localcam.equals(camera_onPreviewFrame)) {
                    while (data_buffer == null) {
                    }
                    System.arraycopy(data_buffer, 0, paramd.args[0], 0, Math.min(data_buffer.length, ((byte[]) paramd.args[0]).length));
                } else {
                    camera_callback_calss = preview_cb_class;
                    camera_onPreviewFrame = (android.hardware.Camera) paramd.args[1];
                    mwidth = camera_onPreviewFrame.getParameters().getPreviewSize().width;
                    mhight = camera_onPreviewFrame.getParameters().getPreviewSize().height;
                    int frame_Rate = camera_onPreviewFrame.getParameters().getPreviewFrameRate();
                    LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]帧预览回调初始化：宽：" + mwidth + " 高：" + mhight + " 帧率：" + frame_Rate);
                    File toast_control = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera1/" + "no_toast.jpg");
                    need_to_show_toast = !toast_control.exists();
                    if (toast_content != null && need_to_show_toast) {
                        try {
                            Toast.makeText(toast_content, "发现预览\n宽：" + mwidth + "\n高：" + mhight + "\n" + "需要视频分辨率与其完全相同", Toast.LENGTH_SHORT).show();
                        } catch (Exception ee) {
                            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG][toast]" + getStackTraceString(ee));
                        }
                    }
                    if (finalNeed_stop == 1) {
                        return;
                    }
                    if (c1_hw_decode_obj != null) {
                        c1_hw_decode_obj.stopDecode();
                    }
                    c1_hw_decode_obj = new VideoToFrames();
                    c1_hw_decode_obj.setSaveFrames("", OutputImageFormat.NV21);
                    c1_hw_decode_obj.decode(video_path + "virtual.mp4");
                    while (data_buffer == null) {
                    }
                    System.arraycopy(data_buffer, 0, paramd.args[0], 0, Math.min(data_buffer.length, ((byte[]) paramd.args[0]).length));
                }

            }
        });

    }

    private void process_camera2Session_callback(CameraCaptureSession.StateCallback callback_calss){
        if (callback_calss == null){
            return;
        }

//        Method[] methods = callback_calss.getClass().getMethods();
//        for(Method method:methods) {
//            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]当前SDK：" + Build.VERSION.SDK_INT);
//            LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]实体类的方法名：" + method.getName());
//        }

        /**
         * 当相机设备完成配置时调用此方法，会话可以开始处理捕获请求。
         * 如果会话中已经有捕获请求排队，则它们将在调用此回调后开始处理，并且会话将在调用此回调后立即调用onActive。
         * 如果没有提交捕获请求，则会话在此回调后立即调用onReady。
         * 如果相机设备配置失败，则会调用onConfigureFailed而不是此回调。
         */
        XposedHelpers.findAndHookMethod(callback_calss.getClass(), "onConfigureFailed", CameraCaptureSession.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]onConfigureFailed ：" + param.args[0].toString());
            }

        });

        /**
         * 如果会话无法按请求进行配置，则调用此方法。
         * 如果请求的输出集包含不受支持的大小，或者一次请求了太多的输出，则可能会发生这种情况。
         * 会话被视为关闭，并且在调用此回调后对其调用的所有方法都将抛出IllegalStateException。在此回调之前提交给会话的任何捕获请求都将被丢弃，并且不会在其侦听器上产生任何回调。
         */
        XposedHelpers.findAndHookMethod(callback_calss.getClass(), "onConfigured", CameraCaptureSession.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]onConfigured ：" + param.args[0].toString());
            }
        });

        /**
         * 每当会话没有更多的捕获请求要处理时，都会调用此方法。
         * 在创建新会话期间，如果在完成配置之前未提交任何捕获请求，则会在 onConfigured 之后调用此回调。
         * 否则，每当会话完成处理其所有活动捕获请求并且未设置重复请求或突发时，都会调用此回调。
         */
//        XposedHelpers.findAndHookMethod( callback_calss.getClass(), "onReady", CameraCaptureSession.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]onReady ："+ param.args[0].toString());
//            }
//        });

        /**
         * 当会话开始主动处理捕获请求时调用此方法。
         * 如果在调用onConfigured之前提交了捕获请求，则会话将立即开始处理这些请求，并在回调后立即调用此方法。
         * 如果会话处理完捕获请求并调用onReady，则一旦提交新的捕获请求，将再次调用此回调。
         */
//        XposedHelpers.findAndHookMethod( callback_calss.getClass(), "onActive", CameraCaptureSession.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]onActive ："+ param.args[0].toString());
//            }
//        });

        /**
         * 当相机设备的输入捕获队列变为空，并准备接受下一个请求时，将调用此方法。
         * 挂起的捕获请求存在于两个队列中的一个：正在处理管道中不同阶段的请求的飞行队列和等待进入飞行队列的输入队列。需要输入队列，因为可能提交的请求比当前相机设备管道深度更多。
         * 当输入队列变为空时，将触发此回调，并且相机设备可能必须回退到重复请求（如果设置），或者完全跳过来自传感器的下一帧。例如，这可能会导致相机预览输出出现故障。此回调仅在由capture（）或captureBurst（）排队的请求之后触发，而不是在重复请求或burst进入飞行队列之后触发。例如，在重复请求和单次JPEG捕获的常见情况下，仅当JPEG请求已进入飞行队列进行捕获时，此回调才会触发。
         * 仅在输入队列为空时发送新的捕获或captureBurst，可以将管道延迟最小化。
         * 创建会话时不会触发此回调。它与onReady不同，后者在两个队列中的所有请求都已处理时触发。
         */
//        XposedHelpers.findAndHookMethod( callback_calss.getClass(), "onCaptureQueueEmpty", CameraCaptureSession.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]onCaptureQueueEmpty ："+ param.args[0].toString());
//            }
//        });

        /**
         * 当会话关闭时调用此方法。if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
         * 当父相机设备创建新会话或关闭父相机设备（用户关闭设备或由于相机设备断开连接或发生致命错误）时，会话关闭。
         * 一旦会话关闭，所有方法都会抛出IllegalStateException，并且任何重复请求或突发请求都将停止（就像调用stopRepeating()一样）。但是，提交给会话的任何正在进行的捕获请求都将像正常完成一样。
         */
//        XposedHelpers.findAndHookMethod( callback_calss.getClass(), "onClosed", CameraCaptureSession.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]onClosed ："+ param.args[0].toString());
//            }
//        });

        /**
         * 当输出Surface的缓冲区预分配完成时，将调用此方法。
         * 输出Surface的缓冲区预分配是通过prepare调用启动的。在分配正在进行时，不能将Surface用作捕获目标。一旦此回调触发，提供的输出Surface可以再次用作捕获请求的目标。
         * 如果在预分配期间发生错误（例如，耗尽适当的内存），则在遇到错误后仍会调用此回调，尽管可能没有成功预分配某些缓冲区。
         */
//        XposedHelpers.findAndHookMethod( callback_calss.getClass(), "onSurfacePrepared", CameraCaptureSession.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                LogToFileUtils.write(Thread.currentThread().getStackTrace()[2].getLineNumber()+"[VCAMLOG]onSurfacePrepared ："+ param.args[0].toString());
//            }
//        });
    }



    //以下代码来源：https://blog.csdn.net/jacke121/article/details/73888732
    private Bitmap getBMP(String file) throws Throwable {
        return BitmapFactory.decodeFile(file);
    }

    private static byte[] rgb2YCbCr420(int[] pixels, int width, int height) {
        int len = width * height;
        // yuv格式数组大小，y亮度占len长度，u,v各占len/4长度。
        byte[] yuv = new byte[len * 3 / 2];
        int y, u, v;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = (pixels[i * width + j]) & 0x00FFFFFF;
                int r = rgb & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb >> 16) & 0xFF;
                // 套用公式
                y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
                u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
                v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;
                y = y < 16 ? 16 : (Math.min(y, 255));
                u = u < 0 ? 0 : (Math.min(u, 255));
                v = v < 0 ? 0 : (Math.min(v, 255));
                // 赋值
                yuv[i * width + j] = (byte) y;
                yuv[len + (i >> 1) * width + (j & ~1)] = (byte) u;
                yuv[len + +(i >> 1) * width + (j & ~1) + 1] = (byte) v;
            }
        }
        return yuv;
    }

    private static byte[] getYUVByBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int[] pixels = new int[size];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        return rgb2YCbCr420(pixels, width, height);
    }

    /**
     * 打印异常堆栈
     */
    public static String getStackTraceString(Throwable ex){
        StackTraceElement[] traceElements = ex.getStackTrace();

        StringBuilder traceBuilder = new StringBuilder();

        if (traceElements != null && traceElements.length > 0) {
            for (StackTraceElement traceElement : traceElements) {
                traceBuilder.append(traceElement.toString());
                traceBuilder.append("\n");
            }
        }

        return traceBuilder.toString();
    }
}

