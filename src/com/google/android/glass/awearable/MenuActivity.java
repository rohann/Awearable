/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.glass.awearable;

import java.io.File;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

import com.google.android.glass.awearable.R;
import com.google.android.glass.media.CameraManager;
import com.google.gson.Gson;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Activity showing the options menu.
 */
public class MenuActivity extends Activity {
	private static int TAKE_PICTURE_REQUEST = 1;
	private static int TAKE_VIDEO_REQUEST = 2;
	private static int SPEECH_REQUEST = 3;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.next, menu);
        inflater.inflate(R.menu.text, menu);
        inflater.inflate(R.menu.picture, menu);
        inflater.inflate(R.menu.video, menu);
        inflater.inflate(R.menu.stop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
        	case R.id.next:
        		startService(new Intent(this,SliderService.class));
        		return true;
        	case R.id.text:
        	    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        	    startActivityForResult(intent, SPEECH_REQUEST);

        		return true;
        	case R.id.picture:
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(pictureIntent, TAKE_PICTURE_REQUEST);
        		return true;
        		
        	case R.id.video:
        		Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        		startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
        		return true;
            case R.id.stop:
                stopService(new Intent(this, SliderService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    		
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            final String picturePath = data.getStringExtra(
                    CameraManager.EXTRA_PICTURE_FILE_PATH);
				processFileWhenReady(picturePath);
        }
        else if(requestCode==TAKE_VIDEO_REQUEST && resultCode == RESULT_OK){
        	final String videoPath = data.getStringExtra(CameraManager.EXTRA_VIDEO_FILE_PATH);
        	processFileWhenReady(videoPath);
        }
        
        else if(requestCode==SPEECH_REQUEST && resultCode == RESULT_OK){
        	Log.d("NOPE","NOPE");
        	List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        	String spokenText = results.get(0);
        	System.out.println(spokenText);
        	Log.d("HERE","HERE");
        	transferText(spokenText); 
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    	 
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the Activity.
        finish();
    }
    private void transferText(String text){
    	Log.d("TEXT", text);
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); // to be checked
    	StrictMode.setThreadPolicy(policy); // to be checked
    }
    private void processFileWhenReady(final String filePath){
    	Log.d("PHOTO", "photo");
    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); // to be checked
    	StrictMode.setThreadPolicy(policy); // to be checked
    	   final File pictureFile = new File(filePath);

           if (pictureFile.exists()) {
               // The picture is ready; process it.
           	
            //// Using the foreign library to send the file
           	Gson gson = new Gson();
           	
       		RestAdapter restadapter = new RestAdapter.Builder()
   			.setEndpoint("http://160.39.212.162:9000") //URL ROHAN SERVER
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
                                       processFileWhenReady(filePath);
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
