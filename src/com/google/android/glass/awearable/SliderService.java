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

import com.google.android.glass.awearable.R;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class SliderService extends Service {

    private static final String TAG = "StopwatchService";
    private static final String LIVE_CARD_TAG = "stopwatch";

    private TimelineManager mTimelineManager;
    private LiveCard mLiveCard;
    private int number=0;
    @Override
    public void onCreate() {
        super.onCreate();
        mTimelineManager = TimelineManager.from(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	number++;
    	updateCard("Event"+number,this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            Log.d(TAG, "Unpublishing LiveCard");
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }
    
    public void publishCard(Context context){
   	 Log.d(TAG,"publishCard() called.");
   		 mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);
   		 
   		 RemoteViews remoteviews=new RemoteViews(context.getPackageName(), R.layout.livecard);//left to check
   		 mLiveCard.setViews(remoteviews);
   		 
   		 Intent intent = new Intent(context,MenuActivity.class);
   		 mLiveCard.setAction(PendingIntent.getActivity(context,0, intent,0));
   		 mLiveCard.publish(LiveCard.PublishMode.REVEAL);
   }
    
    public void updateCard(String string, Context context){
    	if(mLiveCard==null){
    		publishCard(context);
    	}
    	else{

    		RemoteViews remoteviews = new RemoteViews(context.getPackageName(),R.layout.livecard); // Defining a view. We need to give some text to the view. And it has to be the address.
    		remoteviews.setCharSequence(R.id.livecard_content, "setText", string);//Changing the text to the updated text from the argument.
    		mLiveCard.setViews(remoteviews); 
    		
    		Intent intent = new Intent(context, MenuActivity.class);
    		mLiveCard.setAction(PendingIntent.getActivity(context, 0, intent, 0));
    		
    		if(! mLiveCard.isPublished()){
    			mLiveCard.publish(LiveCard.PublishMode.REVEAL);
    		}
    		else{
    			Log.d(TAG,"liveCard not published");
    		}
    		
    	}
    }
}
