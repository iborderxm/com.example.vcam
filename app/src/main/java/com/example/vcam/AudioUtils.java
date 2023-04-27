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
 * MP3תPCM Java��ʽʵ��
 */
public class AudioUtils {

	private AudioUtils() {
	}

	/**
	 * MP3ת��PCM
	 * 
	 * @param mp3filepath ԭʼ�ļ�·��
	 * @param pcmfilepath ת���ļ��ı���·��
	 * @return
	 * @throws Exception
	 */
	public static boolean convertMP3ToPcm(String mp3filepath, String pcmfilepath) {
		try {
			// ��ȡ�ļ�����Ƶ����pcm�ĸ�ʽ
			AudioInputStream audioInputStream = getPcmAudioInputStream(mp3filepath);
			// ����Ƶת��Ϊ pcm�ĸ�ʽ��������
			AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(pcmfilepath));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * ����MP3
	 * 
	 * @param mp3filepath
	 * @throws Exception
	 */
	public static void playMP3(String mp3filepath) throws Exception {
		// ��ȡ��ƵΪpcm�ĸ�ʽ
		AudioInputStream audioInputStream = getPcmAudioInputStream(mp3filepath);

		// ����
		if (audioInputStream == null) {
			System.out.println("null audiostream");
			return;
		}
		// ��ȡ��Ƶ�ĸ�ʽ
		AudioFormat targetFormat = audioInputStream.getFormat();
		DataLine.Info dinfo = new DataLine.Info(SourceDataLine.class, targetFormat, AudioSystem.NOT_SPECIFIED);
		// ����豸
		SourceDataLine line = null;
		try {
			line = (SourceDataLine) AudioSystem.getLine(dinfo);
			line.open(targetFormat);
			line.start();

			int len = -1;
			byte[] buffer = new byte[1024];
			// ��ȡ��Ƶ�ļ�
			while ((len = audioInputStream.read(buffer)) > 0) {
				// �����Ƶ�ļ�
				line.write(buffer, 0, len);
			}

			// Block�ȴ���ʱ���ݱ����Ϊ��
			line.drain();

			// �رն�ȡ��
			audioInputStream.close();

			// ֹͣ����
			line.stop();

		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("audio problem " + ex);
		}finally{
			if(line!=null){
				line.close();
			}
		}
	}

	/**
	 * ��ȡ�ļ�����Ƶ��
	 * 
	 * @param mp3filepath
	 * @return
	 */
	private static AudioInputStream getPcmAudioInputStream(String mp3filepath) {
		File mp3 = new File(mp3filepath);
		AudioInputStream audioInputStream = null;
		AudioFormat targetFormat = null;
		AudioInputStream in = null;
		try {

			// ��ȡ��Ƶ�ļ�����
			MpegAudioFileReader mp = new MpegAudioFileReader();
			in = mp.getAudioInputStream(mp3);
			AudioFormat baseFormat = in.getFormat();

			// �趨�����ʽΪpcm��ʽ����Ƶ�ļ�
			targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
					baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);

			// �������Ƶ
			audioInputStream = AudioSystem.getAudioInputStream(targetFormat, in);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return audioInputStream;
	}
}