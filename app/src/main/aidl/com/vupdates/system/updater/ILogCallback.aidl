package com.vupdates.system.updater;

interface ILogCallback {
    void onLogEvent(String level, String tag, String message, String throwableStackTrace);
}