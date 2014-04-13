package com.google.android.glass.utils;

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
import android.os.FileObserver;
import android.util.Log;

import android.app.*;
import com.google.gson.Gson;

public class processMediaFile extends Activity {
	
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
