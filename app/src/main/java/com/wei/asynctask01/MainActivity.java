package com.wei.asynctask01;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    private ImageView imageView;
    private LinearLayout opPanel;

    // 圖片陣列
    private Bitmap[] images;
    // 圖片的編號
    private int imgPosition;

    // 顯示下載進度用的對話框
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        processViews();
    }

    // UI 初始化
    private void processViews() {
        imageView = findViewById(R.id.imageView);
        opPanel = findViewById(R.id.opPanel);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    // <summary>
    //  下載按鈕執行的方法
    //  建立下載進度對話框
    //  啟動背景執行緒下載圖片
    // </summary>
    public void downloadClick(View view) {
        // 隱藏下載按鈕
        view.setVisibility(View.INVISIBLE);

        // 下載圖片的背景執行緒
        final DownloadImageTask downloadImageTask = new DownloadImageTask();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Download");
        progressDialog.setMessage("Please wait for download ......");
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);

        // 加入取消按鈕工作
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消背景執行緒
                        // 參數 true：結束後不會呼叫 doPostExecute 方法，而呼叫 onCancelled 方法
                        downloadImageTask.cancel(true);
                    }
                });
        // 顯示下載進度框
        progressDialog.show();
        // 啟動背景執行緒
        downloadImageTask.execute();
    }

    // <summary> 圖片操作按鈕，上一張、下一張 </summary>
    public void moveBtnClick(View view) {
        int id = view.getId();
        switch (id) {
            // 上一張圖片
            case R.id.btnPrevious:
                imgPosition --;
                break;
            // 下一張圖片
            case R.id.btnNext:
                imgPosition ++;
                break;
        }

        if (imgPosition < 0)
            imgPosition = images.length - 1;
        if (imgPosition >= images.length)
            imgPosition = 0;

        imageView.setImageBitmap(images[imgPosition]);
    }

    // <summary> 下載圖片的背景執行緒 </summary>
    // <param name='Void'> doInBackground 的參數型態 </param>
    // <param name='Integer'> onProgressUpdate 的參數型態 </param>
    // <param name='Void'> onPostExecute 的參數型態 </param>
    private class DownloadImageTask extends AsyncTask<Void, Integer, Void> {

        // 存放下載圖片的 List 物件
        private List<Bitmap> downloads = new ArrayList<>();

        @Override
        protected Void doInBackground(Void... aVoid) {

            // 取得下載圖片檔名
            String[] imageNames = getResources().getStringArray(R.array.icons_array);
            // 下載檔案的網址
            String sourceUrl = "https://i.imgur.com/";
            // 設定進度最大值
            progressDialog.setMax(imageNames.length);

            for (int i = 0; i < imageNames.length; i++) {
                // 完整的下載網址
                String url = sourceUrl + imageNames[i];
                downloads.add(loadBitmap(url));
                publishProgress(i + 1);
            }

            if (downloads.size() > 0) {
                images = new Bitmap[downloads.size()];
                downloads.toArray(images);
            }

            return null;
        }

        // <summary> 更新背景執行緒進度 </summary>
        // <param name='values'> 接收 publishProgress 方法傳過來的參數 </param>
        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新進度條
            progressDialog.setProgress(values[0]);
            // 設定顯示下載圖片
            imageView.setImageBitmap(downloads.get(downloads.size() - 1));
        }

        // <summary> 背景執行緒完成自動呼叫 </summary>
        // <param name='aVoid'> 接收 doInBackground 方法傳過來的參數 </param>
        @Override
        protected void onPostExecute(Void aVoid) {
            // 關閉下載進度對話框
            progressDialog.dismiss();
            // 設定顯示圖片為第一張
            imageView.setImageBitmap(images[0]);
            // 顯示圖片操作按鈕
            opPanel.setVisibility(View.VISIBLE);
        }

        // <summary> 取消背景執行時呼叫的方法 </summary>
        // <param name='result'> 接收 doInBackground 方法傳過來的參數 </param>
        @Override
        protected void onCancelled(Void result) {
            if (images != null) {
                imageView.setImageBitmap(images[0]);
                opPanel.setVisibility(View.VISIBLE);
            }
        }
    }

    private Bitmap loadBitmap(String urlStr) {
        Bitmap bitmap = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(urlStr);
            HttpsURLConnection connection =
                    (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
            }

            bitmap = BitmapFactory.decodeStream(inputStream);

            Log.d(TAG, "loadBitmap: bitmap is null\t" + (bitmap == null));
        } catch (IOException e) {
            Log.d(TAG, "loadBitmap: " + e.toString());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, "loadBitmap: " + e.toString());
                }
            }

        }
        return bitmap;
    }

}
