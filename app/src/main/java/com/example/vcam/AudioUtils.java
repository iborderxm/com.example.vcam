package com.example.vcam;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

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
	private static AudioInputStream audioInputStream = null;
	
	private AudioUtils() {
	}

	/**
	 * 获取文件的音频流
	 * 
	 * @param mp3filepath
	 * @return
	 */
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
}