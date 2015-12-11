package com.allo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

//import com.urqa.clientinterface.URQAController;

/**
 * Created by baek_uncheon on 2015. 3. 26..
 */
public class UploadActivity extends Activity {

    ImageView iv_back;
    //    EditText et_file_name;
    EditText et_allo_title;
    EditText et_allo_artist;
    EditText et_desc;

    Allo allo;
    ImageView iv_allo_image;


    RadioGroup rg_allo_open;
    RadioButton rb_allo_open;
    RadioButton rb_allo_close;

    Bitmap bm_allo_image;
    Boolean is_allo_open;

    Button btn_allo_register;

    ProgressDialog pd = null;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private String IMAGE_FILEPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/allo/temp.jpeg";

    Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_allo_contents);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("UploadActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        is_allo_open = true;

        setLayout();
        setListener();
        setAlloInfo();

    }


    private void setLayout() {
        iv_back = (ImageView) findViewById(R.id.iv_back);

        et_allo_title = (EditText) findViewById(R.id.et_title);
        et_allo_artist = (EditText) findViewById(R.id.et_artist);
        et_desc = (EditText) findViewById(R.id.et_desc);

        iv_allo_image = (ImageView) findViewById(R.id.iv_allo_image);

        rg_allo_open = (RadioGroup) findViewById(R.id.rg_allo_open);
        rb_allo_open = (RadioButton) findViewById(R.id.rb_allo_open);
        rb_allo_close = (RadioButton) findViewById(R.id.rb_allo_close);

        btn_allo_register = (Button) findViewById(R.id.btn_allo_register);
    }

    private void setListener() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        iv_allo_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Upload").setAction("allo_image_btn click").build());
                clickAlloImage();
            }
        });
        rg_allo_open.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_allo_open:
                        mTracker.send(new HitBuilders.EventBuilder().setCategory("allo_open").setAction("allo_open true").build());
                        is_allo_open = true;
                        break;
                    case R.id.rb_allo_close:
                        mTracker.send(new HitBuilders.EventBuilder().setCategory("allo_open").setAction("allo_open false").build());
                        is_allo_open = false;

                }

            }
        });

        btn_allo_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Upload").setAction("btn_allo_register click").build());
                clickAlloRegister();
            }
        });

    }

    private void setAlloInfo() {
        allo = (Allo) getIntent().getSerializableExtra("allo_temp");
        if (allo.getTitle() != null) {
            et_allo_title.setText(allo.getTitle());
        }
        if (allo.getArtist() != null) {
            et_allo_artist.setText(allo.getArtist());
        }


        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(allo.getURL());
        byte[] data = mmr.getEmbeddedPicture();
        if (data != null) {
            bm_allo_image = BitmapFactory.decodeByteArray(data, 0, data.length);
            iv_allo_image.setImageBitmap(bm_allo_image); //associated cover art in bitmap
        }
    }


    private void clickAlloImage() {
        final CharSequence[] items = {"앨범", "카메라"};
        new AlertDialog.Builder(this)
                .setTitle("이미지 가져오기")
                .setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                switch (i) {
                                    case 0:
                                        mTracker.send(new HitBuilders.EventBuilder().setCategory("Image").setAction("Album click").build());
                                        getImageFromAlbum();
                                        break;
                                    case 1:
                                        mTracker.send(new HitBuilders.EventBuilder().setCategory("Image").setAction("Camera click").build());
                                        getImageFromCamera();
                                        break;
                                }
                            }
                        })
                .show();
    }

    private void getImageFromAlbum() {
        Intent intent = new Intent();
        // Gallery 호출
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        Display dis = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int mDisWitdh = dis.getWidth();
        if (mDisWitdh > 480) {
            intent.putExtra("outputX", 480);
            intent.putExtra("outputY", 480);
        } else {
            intent.putExtra("outputX", mDisWitdh);
            intent.putExtra("outputY", mDisWitdh);
        }


        Log.i("mdis wit", String.valueOf(mDisWitdh));
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);


        intent.putExtra("crop", "true");
        intent.putExtra("scale", false);
        intent.putExtra("noFaceDetection", true);


        try {
            intent.putExtra("return-data", true);
            startActivityForResult(Intent.createChooser(intent,
                    "Complete action using"), PICK_FROM_GALLERY);
        } catch (ActivityNotFoundException e) {
            // Do nothing for now
        }
    }

    private void getImageFromCamera() {
        // 카메라 호출
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());

        // 이미지 잘라내기 위한 크기
        Display dis = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int mDisWitdh = dis.getWidth();
        if (mDisWitdh > 480) {
            intent.putExtra("outputX", 480);
            intent.putExtra("outputY", 480);
        } else {
            intent.putExtra("outputX", mDisWitdh);
            intent.putExtra("outputY", mDisWitdh);
        }

        Log.i("mdis wit", String.valueOf(mDisWitdh));

//        intent.putExtra("outputX", mDisWitdh);
//        intent.putExtra("outputY", mDisWitdh);

        intent.putExtra("aspectX", 1); //이걸 삭제한다
        intent.putExtra("aspectY", 1); //이걸 삭제한다


        intent.putExtra("crop", "true");
        intent.putExtra("scale", false);
//        intent.putExtra("aspectX", 1); //이걸 삭제한다
//        intent.putExtra("aspectY", 1); //이걸 삭제한다
//        intent.putExtra("scale", true); //이걸 삭제한다
        intent.putExtra("noFaceDetection", true);


        try {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            // Do nothing for now
        }


        /*
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "temp.jpg";
        mImageCaptureUri = Uri.fromFile(new File(IMAGE_TEMP_PATH, url));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        //intent.putExtra("return-data", true);
        startActivityForResult(intent, PICK_FROM_CAMERA);
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try {
            Bundle extras = data.getExtras();
            if (extras != null) {
                bm_allo_image = extras.getParcelable("data");
                iv_allo_image.setImageBitmap(bm_allo_image);

            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void clickAlloRegister() {


        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.setTimeout(3000);

        String url = getApplicationContext().getString(R.string.url_ucc_upload);

        PersistentCookieStore myCookieStore = new PersistentCookieStore(getApplicationContext());
        myClient.setCookieStore(myCookieStore);

        File file = new File(allo.getURL());

        RequestParams params = new RequestParams();

        String st_title = et_allo_title.getText().toString();
        String st_artist = et_allo_artist.getText().toString();
        if (bm_allo_image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm_allo_image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            params.put("image", new ByteArrayInputStream(stream.toByteArray()), "image.jpeg");
        }

        LoginUtils loginUtils = new LoginUtils(UploadActivity.this);
        params.put("id", loginUtils.getId());
        params.put("pw", loginUtils.getPw());
        params.put("title", st_title);
        params.put("artist", st_artist);
        params.put("is_open", is_allo_open);
        params.put("duration", String.valueOf(allo.getDuration()));
        params.put("desc", et_desc.getText().toString());


        try {
            params.put("song", file, "multipart/form-data");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        pd = ProgressDialog.show(UploadActivity.this, "", UploadActivity.this.getString(R.string.wait_upload), true);
        myClient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                pd.dismiss();
                Log.i("HTTP RESPONSE......", new String(responseBody));
                try {
                    JSONObject response_object = new JSONObject(new String(responseBody));
                    String status = response_object.getString("status");
                    if (status.equals("success")) {
                        String msg = "\n나만의 알로가 등록되었습니다.\n";

                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(UploadActivity.this);
                        alert_confirm.setTitle("나만의 알로 만들기").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        UploadActivity.this.finish();
                                    }
                                });
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                    } else {
                        ErrorHandler errorHandler = new ErrorHandler(UploadActivity.this);
                        errorHandler.handleErrorCode(response_object);
                    }

                } catch (JSONException e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                pd.dismiss();

                String msg = "\n"+UploadActivity.this.getText(R.string.on_failure)+"\n";

                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(UploadActivity.this);
                alert_confirm.setTitle("나만의 알로 만들기").setMessage(msg).setCancelable(false).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();

            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onDestroy() {
        // Unregister since the activity is about to be closed.
        // This is somewhat like [[NSNotificationCenter defaultCenter] removeObserver:name:object:]
        super.onDestroy();
    }

    @Override
    protected void onStart(){
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }
}
