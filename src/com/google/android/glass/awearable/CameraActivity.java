package com.google.android.glass.awearable;

import java.io.File;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

import com.google.android.glass.media.CameraManager;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;

public class CameraActivity extends Activity  {
	private static int TAKE_PICTURE_REQUEST = 1;
	private static int TAKE_VIDEO_REQUEST = 2;
	
	
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            final String picturePath = data.getStringExtra(
                    CameraManager.EXTRA_PICTURE_FILE_PATH);
				processPictureWhenReady(picturePath);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.
        	
         //// Using the foreign library to send the file
        	Gson gson = new Gson();
        	
    		RestAdapter restadapter = new RestAdapter.Builder()
			.setEndpoint("http://160.39.212.162:9000")
			.setConverter(new GsonConverter(gson))
			.build();   
        	
        	
        	GitHubService service = restadapter.create(GitHubService.class);
        	TypedFile typedfile = new TypedFile("application/octet-stream",pictureFile);
        	service.addPhoto(typedfile, new Callback<File>(){
        		
        		@Override
        		public void failure(RetrofitError retrofitError){
        			Log.e("YES",retrofitError.getMessage());
        		}
				@Override
				public void success(File arg0, Response arg1) {
					// TODO Auto-generated method stub
					
				}
        	});//, email, timestamp);
        ///// use of foreign library ends here
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath()) {
                // Protect against additional pending events after CLOSE_WRITE is
                // handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = (event == FileObserver.CLOSE_WRITE
                                && affectedFile.equals(pictureFile));

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }
    public interface GitHubService {
   	 @Multipart 
   	 @POST("/request/new")
   	 //void addPhoto (@Part("file") TypedFile photo, @Part("email") TypedString email, @Part("timestamp") TypedString timestamp);
   	 void addPhoto (@Part("file") TypedFile photo, Callback<File> callback);
   	}
    

}
