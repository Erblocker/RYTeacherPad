package com.xsj.crasheye;

import java.util.concurrent.ExecutorService;

class BaseExecutor {
    protected static volatile ExecutorService executor = null;

    BaseExecutor() {
    }
}
