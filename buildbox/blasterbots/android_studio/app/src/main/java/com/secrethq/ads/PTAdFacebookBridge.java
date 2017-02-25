package com.secrethq.ads;
import java.lang.ref.WeakReference;

import org.cocos2dx.lib.Cocos2dxActivity;
import com.facebook.ads.*;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class PTAdFacebookBridge  {
	private static native String bannerId();
	private static native String interstitialId();
	private static native void interstitialDidFail();
	private static native void bannerDidFail();
	
	private static final String TAG = "PTAdFacebookBridge";
	private static Cocos2dxActivity activity;
	private static WeakReference<Cocos2dxActivity> s_activity;
	private static RelativeLayout layout;
	private static AdView adView;
	private static InterstitialAd interstitialAd;
	
	private static boolean isBannerScheduledForShow = false;
	private static boolean isInterstitialScheduledForShow = false;
	
	public static void initBridge(Cocos2dxActivity activity) {
		Log.v(TAG, "PTAdFacebookBridge  -- INIT");
		PTAdFacebookBridge.s_activity = new WeakReference<Cocos2dxActivity>(activity);	
		PTAdFacebookBridge.activity = activity;
		PTAdFacebookBridge.isBannerScheduledForShow = false;
		
//		AdSettings.addTestDevice("a69bdc92f615c88e90829d30bf7a331b");
//		AdSettings.addTestDevice("8316127659de8b8430d12ca88a2f8667");
//		AdSettings.addTestDevice("9d48673c77cdcd60c2a95f2aaa538fe0");

		PTAdFacebookBridge.initBanner();
		PTAdFacebookBridge.initInterstitial();
				
	}
	
	public static void initBanner(){
		PTAdFacebookBridge.s_activity.get().runOnUiThread( new Runnable() {
            public void run() {
				

        		FrameLayout frameLayout = (FrameLayout)PTAdFacebookBridge.activity.findViewById(android.R.id.content);
        		PTAdFacebookBridge.layout = new RelativeLayout( PTAdFacebookBridge.activity );
        		frameLayout.addView( PTAdFacebookBridge.layout );
        		
        		RelativeLayout.LayoutParams adViewParams = new RelativeLayout.LayoutParams(
        				AdView.LayoutParams.WRAP_CONTENT,
        				AdView.LayoutParams.WRAP_CONTENT);
        		adViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        		adViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                Log.v(TAG,"__ gonna crash" + PTAdFacebookBridge.bannerId());
        		PTAdFacebookBridge.adView = new AdView(PTAdFacebookBridge.activity, PTAdFacebookBridge.bannerId(), AdSize.BANNER_HEIGHT_50);
        		PTAdFacebookBridge.adView.setLayoutParams(adViewParams);
        		
        		PTAdFacebookBridge.adView.setAdListener(new AdListener() {

        	        @Override
        	        public void onError(Ad ad, AdError error) {
        	        	if ( !PTAdFacebookBridge.isBannerScheduledForShow )
        	        		return;
        	        	
        	        	Log.v(TAG,"ERROR with code: "+ error.getErrorMessage());	
						PTAdFacebookBridge.bannerDidFail();
        	        }

        	        @Override
        	        public void onAdLoaded(Ad ad) {
        	        	Log.v(TAG,"LOADED");
        	        	
        	        	if ( PTAdFacebookBridge.isBannerScheduledForShow )
        	        		PTAdFacebookBridge.layout.addView(PTAdFacebookBridge.adView);
        	        	else
        	        		PTAdFacebookBridge.layout.removeView(PTAdFacebookBridge.adView);
        	        }

        	        @Override
        	        public void onAdClicked(Ad ad) {
        	        	Log.v(TAG,"CLICKED");
        	            // Use this function to detect when an ad was clicked.
        	        }

        	    });

                if (!bannerId().isEmpty())
                    PTAdFacebookBridge.adView.loadAd();
            }
		});
	}
	
	public static void initInterstitial(){
		PTAdFacebookBridge.s_activity.get().runOnUiThread( new Runnable() {
            public void run() {
				

				if(PTAdFacebookBridge.interstitialAd != null){
					PTAdFacebookBridge.interstitialAd.destroy();
					PTAdFacebookBridge.interstitialAd = null;
				}
				
				PTAdFacebookBridge.interstitialAd = new InterstitialAd(PTAdFacebookBridge.activity, interstitialId());
				PTAdFacebookBridge.interstitialAd.setAdListener(new InterstitialAdListener() {
		
			        @Override
			        public void onError(Ad ad, AdError error) {
			        	if ( !isInterstitialScheduledForShow )
			        		return;
			        	
			        	Log.v(TAG,"interstitial ERROR with code: "+ error.getErrorMessage());
			        	PTAdFacebookBridge.interstitialDidFail();
			        }
		
			        @Override
			        public void onAdLoaded(Ad ad) {
			        	if(PTAdFacebookBridge.isInterstitialScheduledForShow == true){
			        		PTAdFacebookBridge.interstitialAd.show();
			        	}
			        	
			        	PTAdFacebookBridge.isInterstitialScheduledForShow = false;
			        }
		
			        @Override
			        public void onAdClicked(Ad ad) {
			        	Log.v(TAG,"interstitial CLICKED");
			            // Use this function to detect when an ad was clicked.
			        }
		
					@Override
					public void onInterstitialDismissed(Ad arg0) {
		        		PTAdFacebookBridge.initInterstitial();			
					}
		
					@Override
					public void onInterstitialDisplayed(Ad arg0) {
						PTAdFacebookBridge.isInterstitialScheduledForShow = false;
					}
		
			    });
	
                if (!interstitialId().isEmpty())
                    interstitialAd.loadAd();
            }
 		});
	}
	
	public static void showFullScreen(){
		Log.v(TAG, "showInterstitials");
		
		isInterstitialScheduledForShow = true;
		
		PTAdFacebookBridge.s_activity.get().runOnUiThread( new Runnable() {
            public void run() {		
				if(PTAdFacebookBridge.interstitialAd.isAdLoaded()){
					PTAdFacebookBridge.interstitialAd.show();
					PTAdFacebookBridge.isInterstitialScheduledForShow = false;
				}
				else{
					PTAdFacebookBridge.isInterstitialScheduledForShow = true;
				}
            }
		});		
	}

	
	public static void showBannerAd(){
		Log.v(TAG, "showBannerAd");	
		
		PTAdFacebookBridge.isBannerScheduledForShow = true;
		
		if(PTAdFacebookBridge.adView != null && isBannerVisisble() == false){
			PTAdFacebookBridge.s_activity.get().runOnUiThread( new Runnable() {
				public void run() {
					PTAdFacebookBridge.layout.addView(PTAdFacebookBridge.adView);
				}
			});			
		}
		
	}

	public static void hideBannerAd(){
		Log.v(TAG, "hideBannerAd");
		
		PTAdFacebookBridge.isBannerScheduledForShow = false;
		
		if(PTAdFacebookBridge.adView != null){
			PTAdFacebookBridge.s_activity.get().runOnUiThread( new Runnable() {
	 			public void run() {
					PTAdFacebookBridge.layout.removeView(PTAdFacebookBridge.adView);
	 			}
	 		});
		}
	}
	
	public static boolean isBannerVisisble(){
		if(PTAdFacebookBridge.adView != null){
			
			if(PTAdFacebookBridge.adView.getParent() != null ){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}


}

