package com.allo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import ca.uol.aig.fftpack.RealDoubleFFT;

/**
 * Created by baek_uncheon on 2015. 3. 2..
 */
public abstract class AsyncFFTHookMuteFalse extends AsyncTask<String, String, String> {
    final String TAG = getClass().getSimpleName();

    public abstract void stopAllo();

    Context context;

    ArrayList<Long> al_tto;
    ArrayList<Long> al_skt;

    int i_start_count = 0;


    AudioManager audioManager = null;

    boolean is_ringring = false;
    long l_27 = 0;
    long l_27_start = 0;
    long l_27_start_temp = 0;

    int i_ring_state = 0;

    boolean is_mute = false;
    long l_mute = 0;

    int i_skt_count = 0;
    long l_skt_time = 0;
    long l_skt_start_time = 0;
    long l_skt_start_time_temp = 0;

    long l_state_2_start_time = 0;

    int i_tto_count = 0;
    long l_tto_time = 0;
    long l_tto_start_time = 0;
    long l_tto_start_time_temp = 0;

    boolean is_ringing = false;

    long l_fft_start_time = 0;

    long l_start_time = 0;

    AudioRecord audioRecord = null;

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer = null;
    int blockSize = 256;

    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_CALL;
    private static final int SAMPLE_RATE = 44100;

    public AsyncFFTHookMuteFalse(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        Log.i(TAG, "doInBackground start hooking");

        if (System.currentTimeMillis() - SingleToneData.getInstance().getOutCallTime() > 2000) {
            Log.i("doInbackground time out : ", "" + (System.currentTimeMillis() - SingleToneData.getInstance().getOutCallTime()));
            SingleToneData.getInstance().setPhoneState("IDLE");
        }

        transformer = new RealDoubleFFT(blockSize);

        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

        audioRecord = new AudioRecord(AUDIO_SOURCE, frequency, channelConfiguration, audioEncoding, bufferSize);
        short[] buffer = new short[blockSize];
        double[] toTransform = new double[blockSize];

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMicrophoneMute(true);

        l_start_time = System.currentTimeMillis();
        audioRecord.startRecording();

        SingleToneData.getInstance().setPhoneState("RINGING");

        al_tto = new ArrayList<>(10);
        al_tto.add((long) 1000);
        al_tto.add((long) 4000);
        al_tto.add((long) 7000);
        al_tto.add((long) 10000);
        al_tto.add((long) 12000);
        al_tto.add((long) 15000);
        al_tto.add((long) 16000);
        al_tto.add((long) 17000);
        al_tto.add((long) 19000);
        al_tto.add((long) 20000);

        al_skt = new ArrayList<>(10);
        al_skt.add((long) 0);
        al_skt.add((long) 0);
        al_skt.add((long) 0);
        al_skt.add((long) 0);
        al_skt.add((long) 0);
        al_skt.add((long) 0);
        al_skt.add((long) 0);
        al_skt.add((long) 0);
        al_skt.add((long) 0);
        al_skt.add((long) 0);

        while (!SingleToneData.getInstance().getPhoneState().equals("IDLE")) {
//            if (audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL) != 0) {
//                Log.i("mute false stop ", "audio volume not 0");
//                stopFFT();
//            }

            int bufferReadResult = audioRecord.read(buffer, 0, blockSize);
            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / Short.MAX_VALUE;
            }
            transformer.ft(toTransform);

            fftUpdate(toTransform);


            if (isCancelled()) {
                Log.i("mute false stop ", "is canceled true");
                break;
            }
        }

        Log.i("STOP fft", " after while");
        stopFFT();
        destroy();
        return null;
    }

    @Override
    protected void onProgressUpdate(String... params) {
        if (params[0].equals("STOP")) {
            Log.i("mute false", "onProgressUpdate : STOP");
            stopAllo();
        }
    }

    @Override
    protected void onPostExecute(String params) {
        Log.i("mute false", "onPostExecute");
        stopAllo();
    }


    protected void fftUpdate(double[]... toTransform) {
        int i_ring = 0;
        boolean is_tto = false;
        boolean is_skt = false;
        int max_downy = 1000;
        int max_x = 1000;
        for (int i = 0; i < toTransform[0].length; i++) {
            int x = i;
            int downy = (int) (100 - (toTransform[0][i] * 10));

            /*
            if (i_ring_state == 0) {
                    if (x == 41 || x == 41 || x == 41 || x == 42 || x == 43) {
                        if (downy > 73 && downy < 88) {
                            is_skt = true;
                        }
                    }
            }
            */


            /*
            if (x == 27 || x == 28) {
                if (downy > 63 && downy < 80) {
                    is_tto = true;
                }
            }
            */
//            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, AudioManager.FLAG_PLAY_SOUND);


            if (x > 26 && x < 33) {
                if (downy < 93) {
                    is_tto = true;
                }
            }


            if (downy < 98) {
                if (max_downy > downy) {
                    max_downy = downy;
                    max_x = x;
                }
            }
        }







        switch (i_ring_state) {
            case 0:
                state_0(is_skt, is_tto);
                break;
            case 1:
                state_1(is_tto);
                break;
            case 2:
                state_2(is_tto);
                break;
            case 3:
                state_3(is_tto);
                break;
            case 4:
                state_4(is_tto);
                break;
            default:
                state_4(is_tto);
                break;
        }


    }


    private void state_0(boolean is_skt, boolean is_tto) {
        if (is_skt) {
            al_skt.add(0, System.currentTimeMillis());
//            Log.i("arraylist", "skt start : " + (al_skt.get(0) - al_skt.get(1)));
            if (al_skt.get(0) - al_skt.get(3) < 300) {
                l_skt_start_time = al_skt.get(7);
                i_ring_state = 1;
                Log.i("ring_state", "0 to 1");
            }
        }

        if (is_tto) {

            al_tto.add(0, System.currentTimeMillis());

//            Log.i("arraylist", "start : " + (al_tto.get(0) - al_tto.get(1)));

            if (al_tto.get(0) - al_tto.get(9) < 350) {
                if (!is_ringring) {
                    is_ringring = true;
                    l_tto_start_time = al_tto.get(9);
                    i_ring_state = 3;
                    Log.i("start", "l_tto_start_time! start");
                    Log.i("ring_state", "0 to 3");
                }
            }
        }

        if (System.currentTimeMillis() - l_start_time > 12000) {

            Log.i("stop fft : ", "curr - l_start > 12000 : " + (System.currentTimeMillis() - l_start_time));
            stopFFT();
        }
    }


    //          skt already start
    private void state_1(boolean is_tto) {
        if (System.currentTimeMillis() - l_skt_start_time > 1700) {
            l_state_2_start_time = System.currentTimeMillis();
            Log.i("state", "1 to 2");
            i_ring_state = 2;
        }
    }

    private void state_2(boolean is_tto) {
        if (System.currentTimeMillis() - l_state_2_start_time > 2000) {
            Log.i("stopFFT stat_2 current-l_state_2_start_time : ", "" + (System.currentTimeMillis() - l_state_2_start_time));
            stopFFT();
        }

        if (is_tto) {

            al_tto.add(0, System.currentTimeMillis());

//            Log.i("arraylist", "start : " + (al_tto.get(0) - al_tto.get(1)));

            if (al_tto.get(0) - al_tto.get(9) < 350) {
                if (!is_ringring) {
                    is_ringring = true;
                    l_tto_start_time = al_tto.get(9);
                    i_ring_state = 4;
                    i_start_count++;
                    Log.i("start", "l_tto_start_time! start");
                    Log.i("ring_state", "2 to 4");
                }
            }
        }


    }


    private void state_3(boolean is_tto) {
        if (is_tto) {

            al_tto.add(0, System.currentTimeMillis());

//            Log.i("arraylist", "start : " + (al_tto.get(0) - al_tto.get(1)));

            if (al_tto.get(0) - al_tto.get(2) < 300) {
                if (!is_ringring) {
                    is_ringring = true;
                    l_tto_start_time = al_tto.get(2);
                    Log.i("start", "l_tto_start_time! start");
                }
            }
        }

//        during tto_time
        if (System.currentTimeMillis() - l_tto_start_time > 800) {
            is_ringing = false;
            i_ring_state = 4;
            Log.i("ring_state", "3 to 4");
        }

        if (System.currentTimeMillis() - al_tto.get(0) > 250){
            is_ringing = false;
            i_ring_state = 4;
            Log.i("ring_state", "3 to 4");
        }
    }


    private void state_4(boolean is_tto) {
        if (is_tto) {

            if ((!is_ringring && System.currentTimeMillis() - l_tto_start_time > 2850) || is_ringring) {
                al_tto.add(0, System.currentTimeMillis());

//                Log.i("arraylist", "start : " + (al_tto.get(0) - al_tto.get(1)));

                if (al_tto.get(0) - al_tto.get(9) < 350) {
                    if (!is_ringring) {

                        is_ringring = true;
                        i_start_count++;
                        Log.i("start", "l_tto_start_time! cur-prev_start : " + (System.currentTimeMillis() - l_tto_start_time));
                        Log.i("start", "l_tto_start_time! start : " + (al_tto.get(7) - l_tto_start_time));
                        l_tto_start_time = al_tto.get(9);

                    }
                }
            }
        }



        if (System.currentTimeMillis() - al_tto.get(2) > 200 && is_ringring) {
            is_ringring = false;
            if (System.currentTimeMillis() - l_tto_start_time < 800) {
                Log.i("stop", "curr - al_tto.get(0) > 100 && - l_tto_start_time < 900 : " + (System.currentTimeMillis() - al_tto.get(3)) + "    " + (System.currentTimeMillis() - l_tto_start_time));
//                if (i_start_count > 1)
//                    stopFFT();
            }
        }

        if (is_ringring && System.currentTimeMillis() - l_tto_start_time > 1000){
            is_ringring = false;
        }


        if (System.currentTimeMillis() - al_tto.get(0) > 2600 && al_tto.get(0) > 0 && i_start_count > 0) {
            Log.i("stop", "curr - al_tto.get(0) > 2600 : " + (System.currentTimeMillis() - al_tto.get(0)));
            stopFFT();
        }


        if (System.currentTimeMillis() - l_tto_start_time > 3900 && l_tto_start_time > 0) {
            if (i_start_count > 0) {
                Log.i("stop", "curr - l_tto_start_time > 3900 : " + (System.currentTimeMillis() - l_tto_start_time));
                stopFFT();
            } else if (System.currentTimeMillis() - l_tto_start_time > 7800) {
                Log.i("stop", "curr - l_tto_start_time > 7800 : " + (System.currentTimeMillis() - l_tto_start_time));
                stopFFT();
            }
        }
    }


    private void l_27(boolean is_true) {
        if (is_true) {
            if (!is_ringring) {
                is_ringring = true;
                l_27_start = System.currentTimeMillis();
            }
            l_27 = System.currentTimeMillis();
        }

        if (is_ringring) {

            if (System.currentTimeMillis() - l_27_start < 700) {
                if (System.currentTimeMillis() - l_27 > 280) {
                    Log.i("stop", "curr - l_27 > 280 : " + (System.currentTimeMillis() - l_27));
                }
            }
        } else {
            if (l_27_start - l_27 < 2000 && l_27 > 0 && l_27_start - l_27 > 0) {
                Log.i("stop", "l_27_start - l_27 < 1800 : " + (l_27_start - l_27));
            }
        }

        if (System.currentTimeMillis() - l_27_start > 1000) {
            is_ringring = false;
        }

        if (System.currentTimeMillis() - l_27 > 2300 && l_27 > 0) {
            Log.i("stop", "curr - l_27 > 2300 : " + (System.currentTimeMillis() - l_27));
        }


//        if (l_27_start - l_27 < 1800 && l_27_start-l_27 > 100){
//            Log.i("stop", "l_27_start - l_27 < 1800 : " + (l_27_start - l_27));
//        }


//        if (is_ringring){
//            if (System.currentTimeMillis() - l_27 > 2300 && l_27 > 0){
//                Log.i("stop", "curr - l_27 < 1800 : " + (System.currentTimeMillis() - l_27));
//            }
//        }
    }


    private void state_4(int f, int y) {
        if (f > 26 && f < 29 && y > 63 && y < 80) {

            if (i_tto_count == 0) {
                l_tto_start_time_temp = System.currentTimeMillis();
            }

            i_tto_count++;

            if (System.currentTimeMillis() - l_tto_start_time > 2800) {

                if (System.currentTimeMillis() - l_tto_start_time_temp < 200) {
//                Log.i("@@@@@@@@@@", "f_count : " + i_tto_count);
                    if (i_tto_count > 3) {
                        l_tto_start_time = l_tto_start_time_temp;
                        l_tto_time = System.currentTimeMillis();
                        is_ringing = true;
                    }
                } else {
                    if (i_tto_count < 4) {
                        i_tto_count = 0;
                    }
                }
            }

            if (i_tto_count > 3) {
                l_tto_time = System.currentTimeMillis();
            }
        }

//        during tto_time
        if (System.currentTimeMillis() - l_tto_start_time < 1000) {
//            Log.i("during tto_time", "" + (System.currentTimeMillis() - l_tto_start_time) + "   " + (System.currentTimeMillis() - l_tto_time));
            if (System.currentTimeMillis() - l_tto_time > 200) {
                Log.i("stopFFT state_4 : ", "curr - l_tto_time > 200 : " + (System.currentTimeMillis() - l_tto_time));
                stopFFT();
            }
        }

        if (l_tto_time - l_tto_start_time > 1300) {
            Log.i("stopFFT state_4 : ", "l_tto_time - l_tto_start > 1400 : " + (l_tto_time - l_tto_start_time));
            stopFFT();
        }


        if (l_tto_start_time > l_tto_time) {
            Log.i("state_4 : ", "tto_start > tto_time: " + (l_tto_start_time - l_tto_time));
//            다시 뚜르르 소리가 들리는데 1850안이거나 2100밖에 이 소리가 났을때
            if (l_tto_start_time - l_tto_time < 1850 || l_tto_start_time - l_tto_time > 2100) {
                Log.i("stopFFT state_4 : ", "tto_start - tto_time < 1850 : " + (l_tto_start_time - l_tto_time));
            }
        }


//        during silent_time

        if (System.currentTimeMillis() - l_tto_time > 200) {
            if (i_tto_count > 4) {
                i_tto_count = 0;
                is_ringing = false;
            }

//            무음이 타임이 된지 2.2초가 지났을때
            if (System.currentTimeMillis() - l_tto_time > 2300) {
                Log.i("stopFFT state_4 : ", "curr - l_tto_time > 2300 : " + (System.currentTimeMillis() - l_tto_time));
                stopFFT();
            }
        }
    }


    private void state_0(int f) {
        if (f == 13 || f == 14 || f == 41 || f == 42) {
            if (i_skt_count == 0) {
                l_skt_start_time_temp = System.currentTimeMillis();
            }
            i_skt_count++;
            if (System.currentTimeMillis() - l_skt_start_time_temp < 300) {
                if (i_skt_count > 3) {
                    Log.i("i_ring_state : ", "0 to 1");
                    l_skt_start_time = l_skt_start_time_temp;
                    l_skt_time = System.currentTimeMillis();
                    i_ring_state = 1;
                }
            } else {
                if (i_skt_count < 4) {
                    i_skt_count = 0;
                }
            }
        }


        if (f > 22 && f < 33) {
            if (i_tto_count == 0) {
                l_tto_start_time_temp = System.currentTimeMillis();
            }

            i_tto_count++;

            if (System.currentTimeMillis() - l_tto_start_time_temp < 200) {

                if (i_tto_count > 3) {
                    l_tto_start_time = l_tto_start_time_temp;
                    l_tto_time = System.currentTimeMillis();
                    Log.i("i_ring_state : ", "0 to 3");
                    i_ring_state = 3;
                }
            } else {
                if (i_tto_count < 4) {
                    i_tto_count = 0;
                }
            }
        }
    }


    //          skt already start
    private void state_1(int f) {

        if ((f > 8 && f < 19) || (f > 22 && f < 50) || (f > 54 && f < 59) || (f > 82 && f < 85)) {
            l_skt_time = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - l_skt_start_time < 1600) {
            if (System.currentTimeMillis() - l_skt_time > 600) {
                Log.i("stopFFT state 1 : ", "curr - l_skt_start_time : " + (System.currentTimeMillis() - l_skt_start_time));
                Log.i("stopFFT state 1 : ", "curr - l_skt_time : " + (System.currentTimeMillis() - l_skt_time));
                publishProgress("-");
                stopFFT();
            }
        } else {
            Log.i("i_ring_state : ", "1 to 2");
            publishProgress("2");
            l_state_2_start_time = System.currentTimeMillis();
            i_tto_count = 0;
            i_ring_state = 2;
//            if (System.currentTimeMillis() - l_skt_time > 200) {
//
//            }
        }
    }

    private void state_2(int f) {
        if (System.currentTimeMillis() - l_state_2_start_time > 1500) {
            Log.i("stopFFT stat_2 current-l_state_2_start_time : ", "" + (System.currentTimeMillis() - l_state_2_start_time));
            stopFFT();
        }

        if (f > 22 && f < 33) {
            if (i_tto_count == 0) {
                l_tto_start_time_temp = System.currentTimeMillis();
            }

            i_tto_count++;

            if (System.currentTimeMillis() - l_tto_start_time_temp < 200) {

                if (i_tto_count > 4) {
                    l_tto_start_time = l_tto_start_time_temp;
                    l_tto_time = System.currentTimeMillis();
                    Log.i("i_ring_state : ", "2 to 4");
                    i_ring_state = 4;
                }
            } else {
                if (i_tto_count < 5) {
                    i_tto_count = 0;
                }
            }
        }
    }

    private void state_3(int f) {
        if (f > 22 && f < 33) {
            if (i_tto_count == 0) {
                l_tto_start_time_temp = System.currentTimeMillis();
            }

            i_tto_count++;

            if (System.currentTimeMillis() - l_tto_start_time_temp < 200) {
                if (i_tto_count > 4) {
                    l_tto_start_time = l_tto_start_time_temp;
                    l_tto_time = System.currentTimeMillis();
                }
            } else {
                if (i_tto_count < 5) {
                    i_tto_count = 0;
                }
            }

            if (i_tto_count > 4) {
                l_tto_time = System.currentTimeMillis();
            }
        }

//        during tto_time
        if (System.currentTimeMillis() - l_tto_start_time < 1000) {
            if (System.currentTimeMillis() - l_tto_time > 600) {
                Log.i("stopFFT state_3 : ", "curr - l_tto_time : " + (System.currentTimeMillis() - l_tto_time));
                stopFFT();
            }
        } else {
            Log.i("i_ring_state : ", "3 to 4");
            l_tto_time = System.currentTimeMillis();
            i_ring_state = 4;
        }

        if (System.currentTimeMillis() - l_tto_time > 150) {
            if (i_tto_count > 10) {
                i_tto_count = 0;
            }
        }
    }


    private void state_4_(int f) {
        if (f > 26 && f < 33) {

            if (i_tto_count == 0) {
                l_tto_start_time_temp = System.currentTimeMillis();
            }

            i_tto_count++;

            if (System.currentTimeMillis() - l_tto_start_time > 2800) {

                if (System.currentTimeMillis() - l_tto_start_time_temp < 200) {
//                Log.i("@@@@@@@@@@", "f_count : " + i_tto_count);
                    if (i_tto_count > 3) {
                        l_tto_start_time = l_tto_start_time_temp;
                        l_tto_time = System.currentTimeMillis();
                        is_ringing = true;
                    }
                } else {
                    if (i_tto_count < 4) {
                        i_tto_count = 0;
                    }
                }
            }

            if (i_tto_count > 3) {
                l_tto_time = System.currentTimeMillis();
            }
        }

//        during tto_time
        if (System.currentTimeMillis() - l_tto_start_time < 1000) {
//            Log.i("during tto_time", "" + (System.currentTimeMillis() - l_tto_start_time) + "   " + (System.currentTimeMillis() - l_tto_time));
            if (System.currentTimeMillis() - l_tto_time > 200) {
                Log.i("stopFFT state_4 : ", "curr - l_tto_time > 200 : " + (System.currentTimeMillis() - l_tto_time));
                stopFFT();
            }
        }

        if (l_tto_time - l_tto_start_time > 1300) {
            Log.i("stopFFT state_4 : ", "l_tto_time - l_tto_start > 1400 : " + (l_tto_time - l_tto_start_time));
            stopFFT();
        }


        if (l_tto_start_time > l_tto_time) {
            Log.i("state_4 : ", "tto_start > tto_time: " + (l_tto_start_time - l_tto_time));
//            다시 뚜르르 소리가 들리는데 1850안이거나 2100밖에 이 소리가 났을때
            if (l_tto_start_time - l_tto_time < 1850 || l_tto_start_time - l_tto_time > 2100) {
                Log.i("stopFFT state_4 : ", "tto_start - tto_time < 1850 : " + (l_tto_start_time - l_tto_time));
            }
        }


//        during silent_time

        if (System.currentTimeMillis() - l_tto_time > 200) {
            if (i_tto_count > 4) {
                i_tto_count = 0;
                is_ringing = false;
            }

//            무음이 타임이 된지 2.2초가 지났을때
            if (System.currentTimeMillis() - l_tto_time > 2300) {
                Log.i("stopFFT state_4 : ", "curr - l_tto_time > 2300 : " + (System.currentTimeMillis() - l_tto_time));
                stopFFT();
            }
        }
    }


    private void stopFFT() {
        Log.i("mute false", "stopFFT  stop, idle, mute, volume");
        PlayAllo.getInstance().backgroundStopAllo();
        SingleToneData.getInstance().setPhoneState("IDLE");
        audioManager.setMicrophoneMute(false);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, SingleToneData.getInstance().getVolume(), AudioManager.FLAG_PLAY_SOUND);
        publishProgress("STOP");
    }

    private void destroy() {
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }


    @Override
    protected void onCancelled() {
        Log.i("mute false", "onCancelled()");
        super.onCancelled();
    }

    @Override
    protected void onCancelled(String params) {
        Log.i("mute false", "onCancelled(String parmas)");
        super.onCancelled(params);
    }

}


