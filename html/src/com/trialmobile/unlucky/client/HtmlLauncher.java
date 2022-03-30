package com.trialmobile.unlucky.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.trialmobile.unlucky.AppInterface;
import com.trialmobile.unlucky.UnluckyMain;
import com.trialmobile.unlucky.VideoCallback;

public class HtmlLauncher extends GwtApplication implements AppInterface {

        @Override
        public GwtApplicationConfiguration getConfig () {
                // Resizable application, uses available space in browser
                return new GwtApplicationConfiguration(true);
                // Fixed size application:
                //return new GwtApplicationConfiguration(480, 320);
        }

        @Override
        public ApplicationListener createApplicationListener () {
                return new UnluckyMain(this);
        }

        @Override
        public void openOfferwall(String userId) {
        }

        @Override
        public void showBanner(boolean show) {
        }

        @Override
        public void showVideo(VideoCallback callback) {
        }
}