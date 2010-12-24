# Tapjoy Module for Titanium

This module is a simple interface for connecting to [Tapjoy](http://tapjoy.com)

# Build & Install

## iPhone

Simply run the ./build.py script in 'iphone/' then copy the com.tapjoy-iphone-*.zip file to your Titanium Library  ('/Library/Application Support/Titanium' or '~/Library/Application Support/Titanium')

## Android

Navigate into the 'android/' directory and run 'ant'. Then copy the com.tapjoy-android-*.zip file to your Titanium Library  ('/Library/Application Support/Titanium' or '~/Library/Application Support/Titanium')

# Usage

## iPhone

      var tapjoy = require('com.tapjoy');
      tapjoy.connect('API_KEY');

## Android

      var tapjoy = require('com.tapjoy');
      tapjoy.connectCreate();
      Ti.App.addEventListener('close',function(e) {
        tapjoy.connectDestroy();
      });
    