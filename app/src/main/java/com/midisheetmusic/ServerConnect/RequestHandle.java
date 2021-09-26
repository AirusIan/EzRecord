package com.midisheetmusic.ServerConnect;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.midisheetmusic.ChooseSongActivity;
import com.midisheetmusic.RecordActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;


import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;

import okhttp3.MediaType;

public class RequestHandle {
    private final String baseurl = "http://140.115.87.119:8000";

    private int DEFAULT_TIMEOUT = 240;

    private OkHttpClient httpClient =
            new OkHttpClient.Builder().connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).build();

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseurl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create()).build();

    private Request_Interface requestInterface = retrofit.create(Request_Interface.class);

    private File wavfile;
    private String midfilepath = "";
    private Handler mainThreadHandler;

    private boolean uploadFile() {

        RequestBody requestFile = RequestBody.create(MediaType.parse("*/*"), wavfile);
        MultipartBody.Part bodyfile = MultipartBody.Part.createFormData("RecordFile", wavfile.getName(), requestFile);
        RequestBody name = RequestBody.create(okhttp3.MultipartBody.FORM, wavfile.getName());

        Call<ResponseBody> call = requestInterface.upload(name, bodyfile);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response_raw) {
                String wavpath_server = "";
                try {
                    wavpath_server = response_raw.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.v("Upload Success", "response path =" + wavpath_server);
                wav2midi(wavpath_server);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error", t.getMessage());
                mainThreadHandler.sendEmptyMessage(3);
            }
        });
        return true;
    }

    private void wav2midi(String wavpath_server){
        Call<ResponseBody> call = requestInterface.wav2midiRequest(wavpath_server);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response_raw) {
                if(response_raw.isSuccessful()){
                    String midpath_server = "";
                    try {
                        midpath_server = response_raw.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.v("Wav2midi success", "response midpath=" + midpath_server);
                    download(midpath_server);
                }else{
                    Log.e("Wav2midi error",response_raw.raw().message());
                    mainThreadHandler.sendEmptyMessage(2);
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Wav2midi error", t.getMessage());
                mainThreadHandler.sendEmptyMessage(3);
            }
        });
    }

    private void download(String midpath_server){
        new AsyncTask<Void,Long,Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                Call<ResponseBody> call = requestInterface.downloadRequest(midpath_server);
                call.enqueue(new Callback<ResponseBody>()

                {
                    @Override
                    public void onResponse
                            (Call < ResponseBody > call, Response < ResponseBody > response){
                        boolean finishSaving = writeResponseBodyToDisk(response.body());
                        if (finishSaving == true) {
                            Log.v("Download success", "response = " + response.raw());
                            mainThreadHandler.sendEmptyMessage(1);
                        }else{
                            Log.e("writeToDisk Error","");
                            mainThreadHandler.sendEmptyMessage(4);
                        }
                    }

                    @Override
                    public void onFailure (Call < ResponseBody > call, Throwable t){
                        Log.e("Downlaod error", t.getMessage());
                        mainThreadHandler.sendEmptyMessage(3);
                    }
                });
                return null;
            }
        }.execute();
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            // todo change the file location/name according to your needs
            File file = new File(midfilepath);
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("file download",  fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void run_wav2midi(File file, String path, Handler handler){
        wavfile = file;
        midfilepath = path;
        mainThreadHandler = handler;
        uploadFile();
    }





}
