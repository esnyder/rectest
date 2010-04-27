package com.examples.rectest;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.media.MediaRecorder;
import android.widget.TextView;

public class rectest extends Activity implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener
{
    static final String SAMPLE_PREFIX = "recording";
    private MediaRecorder recorder = null;
    private File mSampleFile = null;
    private TextView tv = null;

    //private Logger tv;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try {
            // Eventually we will take our config parameters from the UI fields defined
            // in this layout and start/stop recording in response to the button clicks.
            //
            // For now we just use the tv field as our logging text view and ignore the
            // content of the UI elements.
            setContentView(R.layout.main);
            
            tv = (TextView) findViewById(R.id.tv);
            tv.append("rectest::onCreate about to set up recorder\n");
        } catch (Exception e) {
            // If we can't create the ui from the XML description, try to figure out why.
            tv = new TextView(this);
            setContentView(tv);
            tv.append("Exception trying to set up UI via XML defined layout: " + e + "\n");
        }
            

        recorder = new MediaRecorder();
        tv.append("allocated recorder\n");
        
        File sampleDir = Environment.getExternalStorageDirectory();
        if (!sampleDir.canWrite())
            sampleDir = new File("/sdcard/sdcard");
        tv.append("sampleDir: " + sampleDir + "\n");

        try {
            String extension = ".3gpp";
            mSampleFile = File.createTempFile(SAMPLE_PREFIX, extension, sampleDir);
        } catch (IOException e) {
            tv.append("Attempt to create temp file in sampleDir threw IOException: " + e + "\n");
            recorder = null;
            return;
        }
        tv.append("Will record to mSampleFile: " + mSampleFile + "\n");

        // could use setPreviewDisplay() to display a preview to suitable View here

        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setOutputFile(mSampleFile.getAbsolutePath());
            recorder.setMaxDuration(1000); // 1 seconds
            recorder.setMaxFileSize(1024*1024); // 1KB
            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);
            tv.append("set recorder params\n");
            
            try {
                recorder.prepare();
            } catch (java.io.IOException e) {
                tv.append("IOException attempting recorder.prepare()\n");
                recorder = null;
                return;
            }
            tv.append("recorder.prepare() returned\n");
            
            recorder.start();
            tv.append("recorder.start() returned\n");
        } catch (java.lang.Exception e) {
            tv.append("caught exception: " + e + "\n");
            recorder = null;
        }
    }

    public void onDestroy()
    {
        super.onDestroy();
        try {
        //tv.append("rectest::onDestroy");
            if (null != recorder) {
                recorder.stop();
                recorder.release();
            }
        } catch (Exception e) {
            //tv.append("onDestroy got exception: " + e + "\n");
        }
        //tv.append("rectest recorder.release()'ed");
    }

    public void onError (MediaRecorder mr, int what, int extra)
    {
        tv.append("rectest::onError called with what: " + what + ", extra: " + extra + "\n");
        mr.stop();
    }

    // OnInfoListener method
    public void onInfo (MediaRecorder mr, int what, int extra)
    {
        tv.append("rectest::onInfo called with what: " + what + ", extra: " + extra + "\n");

        switch (what) {
        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
            tv.append("rectest::onInfo max duration reached\n");
            mr.stop();
            break;
        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
            tv.append("rectest::onInfo max filesize reached\n");
            mr.stop();
            break;
        default:
            tv.append("rectest::onInfo got unknown event: " + what + "\n");
            break;
        }
    }
}
