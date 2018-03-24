package com.netspace.library.struct;

import retrofit2.Call;
import retrofit2.Response;

public class CRestDataBase {
    private transient OnRestFailureListener mFailureListener;
    private transient OnRestSuccessListener mSucessListener;

    public interface OnRestFailureListener {
        void OnDataFailure(Call<?> call, Throwable th);
    }

    public interface OnRestSuccessListener {
        void OnDataSuccess(Call<?> call, Response<?> response);
    }

    public OnRestSuccessListener getSuccessListener() {
        return this.mSucessListener;
    }

    public CRestDataBase setSuccessListener(OnRestSuccessListener successListener) {
        this.mSucessListener = successListener;
        return this;
    }

    public OnRestFailureListener getFailureListener() {
        return this.mFailureListener;
    }

    public CRestDataBase setFailureListener(OnRestFailureListener failureListener) {
        this.mFailureListener = failureListener;
        return this;
    }
}
