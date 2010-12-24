# Tapjoy Module for Titanium

This module is a simple interface for connecting to [Tapjoy](http://tapjoy.com)

# Build & Install

Simply run the ./build.py script then copy the com.tapjoy-iphone-*.zip file to your Titanium Library  ('/Library/Application Support/Titanium' or '~/Library/Application Support/Titanium')

# Usage

    var tapjoy = require('com.tapjoy');
    tapjoy.connect('API_KEY');
