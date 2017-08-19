package jp.techacademy.manabe.miyuki.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;


    Timer mTimer;
    Handler mHandler = new Handler();

    Button mNextButton;
    Button mBackButton;
    Button mAutoButton;
    ImageView mImageVIew;
    TextView mTextView;

    // 画像のURI格納用リスト
    List<Uri> mUriList = new ArrayList<Uri>();
    // 画像の表示用インデックス
    int mIndex = 0;
    // 自動送りフラグ
    boolean autoFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNextButton = (Button) findViewById(R.id.next_button);
        mBackButton = (Button) findViewById(R.id.back_button);
        mAutoButton = (Button) findViewById(R.id.auto_button);
        mImageVIew = (ImageView) findViewById(R.id.imageView);
        mTextView = (TextView) findViewById(R.id.textView);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        mAutoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(autoFlag){   //停止ボタンがタップされた場合
                    stop();
                }else{          //再生ボタンがタップされた場合
                    start();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                } else {
                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText("画像にアクセスできませんでした。");
                    textView.setTextColor(Color.RED);
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        // リストmUriListに画像のURIを格納する
        if (cursor.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                mUriList.add(imageUri);
                Log.d("ANDROID", "URI : " + imageUri.toString());
            } while (cursor.moveToNext());
        }
        cursor.close();

        if(mUriList.size() != 0){
            mNextButton.setEnabled(true);
            mBackButton.setEnabled(true);
            mAutoButton.setEnabled(true);
            mImageVIew.setImageURI(mUriList.get(mIndex));
        } else{
            mNextButton.setEnabled(false);
            mBackButton.setEnabled(false);
            mAutoButton.setEnabled(false);
        }
        setText();
    }

    /**
     * 1つ先の画像を表示
     */
    private void next(){
        if (mIndex + 1 != mUriList.size()) {
            mIndex++;
        } else {
            mIndex = 0;
        }
        mImageVIew.setImageURI(mUriList.get(mIndex));
        setText();
    }

    /**
     * 1つ前の画像を表示
     */
    private void back(){
        if (mIndex == 0) {
            mIndex = mUriList.size() - 1;
        } else {
            mIndex--;
        }
        mImageVIew.setImageURI(mUriList.get(mIndex));
        setText();
    }
    /**
     * 自動送り開始
     */
    private void start(){
        mAutoButton.setText("停止");
        mNextButton.setEnabled(false);
        mBackButton.setEnabled(false);
        autoFlag = true;

        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            next();
                        }
                    });
                }
            }, 2000, 2000);
        }
    }

    /**
     * 自動送り停止
     */
    private void stop(){
        mAutoButton.setText("再生");
        mNextButton.setEnabled(true);
        mBackButton.setEnabled(true);
        autoFlag = false;

        //　自動送り停止
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * ページ数を表示
     */
    private void setText(){
        if(mUriList.size() == 0){
            mTextView.setText("0 / 0");
        } else{
            mTextView.setText((mIndex + 1) + " / " + mUriList.size() );
        }
    }
}
