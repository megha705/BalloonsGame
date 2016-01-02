package com.plattysoft.sage.sound;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.preference.PreferenceManager;

public final class SoundManager {

	private static final int MAX_STREAMS = 10;
	private static final float DEFAULT_MUSIC_VOLUME = 0.6f;

	private static final String SOUNDS_PREF_KEY = "com.plattysoft.balloons.sounds.boolean";
	private static final String MUSIC_PREF_KEY = "com.plattysoft.balloons.music.boolean";

	private HashMap<GameEvent, Integer> mSoundsMap;
	
	private Context mContext;
	private SoundPool mSoundPool;

	private boolean mSoundEnabled;
	private boolean mMusicEnabled;

	private MediaPlayer mBgPlayer;

	public SoundManager(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		mSoundEnabled = prefs.getBoolean(SOUNDS_PREF_KEY, true);
		mMusicEnabled = prefs.getBoolean(MUSIC_PREF_KEY, true);
		// Use SoundPool.Builder on API 21 http://developer.android.com/reference/android/media/SoundPool.Builder.html
		mContext = context;
		loadIfNeeded();
	}

	private void loadEventSound(Context context, GameEvent event, String... filename) {
//		mSoundsMap.put(event,new SoundInfo(context, mSoundPool, filename));
		try {
			AssetFileDescriptor descriptor = context.getAssets().openFd("sfx/" + filename[0]);
			int soundId = mSoundPool.load(descriptor, 1);
			mSoundsMap.put(event, soundId);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void playSoundForGameEvent(GameEvent event) {
		if (!mSoundEnabled) {
			return;
		}
		Integer soundId = mSoundsMap.get(event);
		if (soundId != null) {
			// Left Volume, Right Volume, priority (0 == lowest), loop (0 == no) and rate (1.0 normal playback rate)
			mSoundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
		}
	}

	private void loadIfNeeded () {
		if (mSoundEnabled) {
			loadSounds();
		}
		if (mMusicEnabled) {			
			loadMusic();
		}
	}

	private void loadSounds() {
		createSoundPool();
		mSoundsMap = new HashMap<GameEvent, Integer>();
		loadEventSound(mContext, GameEvent.BalloonHit, "balloon_pop.wav");
	}

	private void createSoundPool() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		}
		else {
			AudioAttributes audioAttributes = new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_GAME)
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
					.build();
			mSoundPool = new SoundPool.Builder()
					.setAudioAttributes(audioAttributes)
					.setMaxStreams(MAX_STREAMS)
					.build();
		}
	}

	private void loadMusic() {
		try {
			// Important to not reuse it. It can be on a strange state
			mBgPlayer = new MediaPlayer();
			AssetFileDescriptor afd = mContext.getAssets().openFd("sfx/Dean_Caedab_-_Everyday_Success.mp3");
			mBgPlayer.setDataSource(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength());
			mBgPlayer.setLooping(true);
			mBgPlayer.setVolume(DEFAULT_MUSIC_VOLUME, DEFAULT_MUSIC_VOLUME);
			mBgPlayer.prepare();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pauseBgMusic() {
		if (mMusicEnabled) {
			mBgPlayer.pause();
		}
	}
	
	public void resumeBgMusic() {
		if (mMusicEnabled) {
			mBgPlayer.start();
		}
	}

	private void unloadMusic() {
		mBgPlayer.stop();
		mBgPlayer.release();
	}

	private void unloadSounds() {
		mSoundPool.release();
		mSoundPool = null;
		mSoundsMap.clear();		
	}

    public void toggleSoundStatus() {
		mSoundEnabled = !mSoundEnabled;
        if (mSoundEnabled) {
        	loadSounds();
        }
        else {
        	unloadSounds();
        }
		// Save it to preferences
		PreferenceManager.getDefaultSharedPreferences(mContext).edit()
				.putBoolean(SOUNDS_PREF_KEY, mSoundEnabled)
				.commit();
    }
    
    public void toggleMusicStatus() {
        mMusicEnabled = !mMusicEnabled;
        if (mMusicEnabled) {
        	loadMusic();
			resumeBgMusic();
        }
        else {
        	unloadMusic();
        }
		// Save it to preferences
		PreferenceManager.getDefaultSharedPreferences(mContext).edit()
				.putBoolean(MUSIC_PREF_KEY, mMusicEnabled)
				.commit();
    }

	public boolean getMusicStatus() {
		return mMusicEnabled;
	}

	public boolean getSoundStatus() {
		return mSoundEnabled;
	}
}
