/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.newventuresoftware.waveformdemo;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.util.Log;

import ai.kitt.snowboy.SnowboyDetect;

public class RecordingThread {
    static { System.loadLibrary("snowboy-detect-android"); }

    private static final String LOG_TAG = RecordingThread.class.getSimpleName();
    private static final int SAMPLE_RATE = 16000;

    public RecordingThread(AudioDataReceivedListener listener) {
        mListener = listener;
    }

    private boolean mShouldContinue;
    private AudioDataReceivedListener mListener;
    private Thread mThread;
    private SnowboyDetect mDetector = new SnowboyDetect("/sdcard/snowboy/common.res",
                                                        "/sdcard/snowboy/snowboy.umdl");
    private MediaPlayer mPlayer = new MediaPlayer();

    public boolean recording() {
        return mThread != null;
    }

    public void startRecording() {
        mDetector.SetSensitivity("0.45");
        mDetector.SetAudioGain(2);
        Log.i("INFO: GUOGUO ", mDetector.GetSensitivity());
        try {
            mPlayer.setDataSource("/sdcard/snowboy/ding.wav");
            mPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mThread != null)
            return;

        mShouldContinue = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        });
        mThread.start();
    }

    public void stopRecording() {
        if (mThread == null)
            return;

        mShouldContinue = false;
        mThread = null;
    }

    private void record() {
        Log.v(LOG_TAG, "Start");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Buffer size in bytes: for 0.1 second of audio
        int bufferSize = (int)(SAMPLE_RATE * 0.1 * 2);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }
        record.startRecording();

        Log.v(LOG_TAG, "Start recording");

        long shortsRead = 0;
        while (mShouldContinue) {
            int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
            shortsRead += numberOfShort;

            // Notify waveform
            mListener.onAudioDataReceived(audioBuffer);

            // Snowboy hotword detection.
            int result = mDetector.RunDetection(audioBuffer, audioBuffer.length);
            if (result > 0) {
                Log.i("Snowboy: ", "Hotword " + Integer.toString(result) + " detected!");
                mPlayer.start();
            }
        }

        record.stop();
        record.release();

        Log.v(LOG_TAG, String.format("Recording stopped. Samples read: %d", shortsRead));
    }
}
