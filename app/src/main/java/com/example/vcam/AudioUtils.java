package com.example.vcam;

import android.media.MediaExtractor;
import android.media.MediaFormat;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * MP3转PCM Java方式实现
 */
public class AudioUtils {
	//采样率
	public static float sampleRate = 0F;
	//声道数
	public static int channels = 0;
	//采样精度
	public static int sampleSizeInBits = 0;
	//缓冲区大小
	public static int bufferSize = 0;
	//是否开始输出
	public static boolean isBegin = false;
	//pcm音频流
	//private static AudioInputStream audioInputStream = null;
	private static DataInputStream audioInputStream = null;
	
	private AudioUtils() {
	}

	/**
	 * 获取文件的音频流
	 * 
	 * @param mp3filepath
	 * @return
	 */
	/*
	public static AudioInputStream getPcmAudioInputStream(String mp3filepath) {
		if(audioInputStream != null){
			LogToFileUtils.write("audioInputStream已创建");
			return audioInputStream;
		}
		File mp3 = new File(mp3filepath);
		//AudioInputStream audioInputStream = null;
		AudioFormat targetFormat = null;
		AudioInputStream in = null;
		try {

			// 读取音频文件的类
			MpegAudioFileReader mp = new MpegAudioFileReader();
			in = mp.getAudioInputStream(mp3);
			AudioFormat baseFormat = in.getFormat();
			
			LogToFileUtils.write(String.format("当前mp3参数sampleRate[%s]channels[%s]", baseFormat.getSampleRate(), baseFormat.getChannels()));
			if (baseFormat.getSampleRate() == sampleRate && baseFormat.getChannels() == channels && bufferSize > 0) {
				// 设定输出格式为pcm格式的音频文件
				targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
						baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

				// 输出到音频
				audioInputStream = AudioSystem.getAudioInputStream(targetFormat, in);
			}else{
				LogToFileUtils.write(String.format("参数不一致sampleRate[%s]channels[%s]", sampleRate, channels));
			}
		} catch (Exception e) {
			LogToFileUtils.write("audio problem:" + HookMain.getStackTraceString(e));
		}
		return audioInputStream;
	}
*/

	/**
	 * 获取文件的音频流
	 *
	 * @param auidofilepath
	 * @return
	 */
	public static DataInputStream getPcmAudioInputStream(String auidofilepath) {
		if(audioInputStream != null){
			LogToFileUtils.write("audioInputStream已创建");
			return audioInputStream;
		}

		LogToFileUtils.write("-----------------audioInputStream开始创建-----------------");
		//从音频文件中读取声音
		MediaExtractor mex = new MediaExtractor();
		MediaFormat mf = null;
		try {
			mex.setDataSource(auidofilepath);// the adresss location of the sound on sdcard.
			mf = mex.getTrackFormat(0);
		} catch (IOException e) {
			LogToFileUtils.write("audio problem:读取音频文件信息失败");
			LogToFileUtils.write("audio problem:" + HookMain.getStackTraceString(e));
		}

		if (mf != null){
			//int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE);
			int currSampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
			int currChannels = mf.getInteger(MediaFormat.KEY_CHANNEL_COUNT);//获取声道数量
			mex.release();

			LogToFileUtils.write(String.format("当前音频文件参数sampleRate[%s]channels[%s]", currSampleRate, currChannels));
			//if (currSampleRate == sampleRate && currChannels == channels && bufferSize > 0) {
			if (currSampleRate == sampleRate && bufferSize > 0) {
				try{
					audioInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(auidofilepath)));
					LogToFileUtils.write(String.format("当前读取音频流容量[%s]", audioInputStream.available()));
					//audioInputStream.mark(0);
				} catch (FileNotFoundException e) {
					LogToFileUtils.write("audio problem:音频文件不存在");
					LogToFileUtils.write("audio problem:" + HookMain.getStackTraceString(e));
				} catch (IOException e) {
					LogToFileUtils.write("audio problem:读取音频流容量失败");
				}
			}
		}

		return audioInputStream;
	}

	public static void release(){
		isBegin = false;
		if(audioInputStream != null){
			try {
				LogToFileUtils.write("-----------------audioInputStream开始关闭-----------------");
				audioInputStream.close();
				audioInputStream = null;
				LogToFileUtils.write("-----------------audioInputStream完成关闭-----------------");
			} catch (IOException e) {
				LogToFileUtils.write("关闭音频流失败:" + HookMain.getStackTraceString(e));
			}
		}
	}
}