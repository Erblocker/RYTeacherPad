package com.foxit.uiextensions.modules.signature;

import com.foxit.uiextensions.utils.Event;

class SignatureEvent extends Event {

    public interface ISignatureCallBack {
        void result(SignatureEvent signatureEvent, boolean z);
    }

    SignatureEvent() {
    }
}
