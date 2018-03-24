package com.stericson.RootTools.execution;

import java.io.Serializable;

public interface IResult {
    Serializable getData();

    int getError();

    Process getProcess();

    void onComplete(int i);

    void onFailure(Exception exception);

    void process(String str) throws Exception;

    void processError(String str) throws Exception;

    IResult setData(Serializable serializable);

    IResult setError(int i);

    IResult setProcess(Process process);
}
