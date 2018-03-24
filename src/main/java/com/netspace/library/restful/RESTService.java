package com.netspace.library.restful;

import android.content.Intent;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.restful.provider.camera.CameraRESTServiceProvider;
import com.netspace.library.restful.provider.device.DeviceOperationRESTServiceProvider;
import com.netspace.library.restful.provider.httpaccess.HttpFileAccessRESTServiceProvider;
import com.netspace.library.restful.provider.httpaccess.TemporaryFileAccessRESTServiceProvider;
import com.netspace.library.restful.provider.imagecrop.CropRESTServiceProvider;
import com.netspace.library.restful.provider.screencapture.DeviceRootScreenCaptureServiceProvider;
import com.netspace.library.restful.provider.screencapture.ScreenCaptureRESTServiceProvider;
import com.netspace.library.utilities.Utilities;
import java.util.ArrayList;
import net.sqlcipher.database.SQLiteDatabase;

public class RESTService {
    private static RESTService mDefaultInstance = null;
    private ArrayList<RESTServiceProvider> marrRestServiceProvider = new ArrayList();

    public static RESTService getDefault() {
        if (mDefaultInstance == null) {
            mDefaultInstance = new RESTService();
        }
        return mDefaultInstance;
    }

    public RESTService() {
        registerService(new CameraRESTServiceProvider());
        registerService(new CropRESTServiceProvider());
        registerService(new ScreenCaptureRESTServiceProvider());
        registerService(new DeviceRootScreenCaptureServiceProvider());
        registerService(new DeviceOperationRESTServiceProvider());
        registerService(new TemporaryFileAccessRESTServiceProvider());
        registerService(new HttpFileAccessRESTServiceProvider());
    }

    public boolean registerService(RESTServiceProvider service) {
        RESTServiceProvider result = service.initialize(this);
        if (result == null) {
            return false;
        }
        this.marrRestServiceProvider.add(result);
        return true;
    }

    public RESTRequest execute(String szURI) {
        return new RESTRequest(this, szURI);
    }

    public RESTRequest execute(Class<? extends RESTServiceProvider> providerClass) {
        return new RESTRequest(this, (Class) providerClass);
    }

    public RESTRequest executeRemote(String szHost, String szURI) {
        RESTRequest request = new RESTRequest(this, szURI);
        request.setHost(szHost);
        return request;
    }

    public RESTServiceProvider findProvider(Class<? extends RESTServiceProvider> providerClass) {
        for (int i = 0; i < this.marrRestServiceProvider.size(); i++) {
            RESTServiceProvider oneProvider = (RESTServiceProvider) this.marrRestServiceProvider.get(i);
            if (oneProvider.getClass().getName().equalsIgnoreCase(providerClass.getName())) {
                return oneProvider;
            }
        }
        return null;
    }

    public RESTServiceProvider findProvider(String szURI) {
        if (!szURI.startsWith("/")) {
            szURI = "/" + szURI;
        }
        int nMaxMatch = -1;
        int nMaxMatchIndex = -1;
        int nMaxPriority = -1;
        for (int i = 0; i < this.marrRestServiceProvider.size(); i++) {
            RESTServiceProvider oneProvider = (RESTServiceProvider) this.marrRestServiceProvider.get(i);
            String szTestURI = oneProvider.mszURI;
            if (!(szTestURI == null || szTestURI.isEmpty())) {
                if (!szTestURI.startsWith("/")) {
                    szTestURI = "/" + szTestURI;
                }
                if (szURI.length() >= szTestURI.length() && szURI.substring(0, szTestURI.length()).equalsIgnoreCase(szTestURI) && szTestURI.length() >= nMaxMatch && oneProvider.mPriority > nMaxPriority) {
                    nMaxMatch = szTestURI.length();
                    nMaxMatchIndex = i;
                    nMaxPriority = oneProvider.mPriority;
                }
            }
        }
        if (nMaxMatchIndex != -1) {
            return (RESTServiceProvider) this.marrRestServiceProvider.get(nMaxMatchIndex);
        }
        return null;
    }

    public RESTServiceProvider findNextProvider(RESTServiceProvider previousProvider) {
        if (previousProvider == null) {
            return null;
        }
        String szURI = previousProvider.mszURI;
        if (!szURI.startsWith("/")) {
            szURI = "/" + szURI;
        }
        int nMaxMatch = szURI.length();
        int nMaxMatchIndex = -1;
        int nMaxPriority = -1;
        for (int i = 0; i < this.marrRestServiceProvider.size(); i++) {
            RESTServiceProvider oneProvider = (RESTServiceProvider) this.marrRestServiceProvider.get(i);
            String szTestURI = oneProvider.mszURI;
            if (!szTestURI.startsWith("/")) {
                szTestURI = "/" + szTestURI;
            }
            if (szURI.length() >= szTestURI.length() && szURI.substring(0, szTestURI.length()).equalsIgnoreCase(szTestURI) && szTestURI.length() >= nMaxMatch && oneProvider.mPriority > nMaxPriority && oneProvider.mPriority < previousProvider.mPriority) {
                nMaxMatch = szTestURI.length();
                nMaxMatchIndex = i;
                nMaxPriority = oneProvider.mPriority;
            }
        }
        if (nMaxMatchIndex != -1) {
            return (RESTServiceProvider) this.marrRestServiceProvider.get(nMaxMatchIndex);
        }
        return null;
    }

    public void launchIntent(Intent intent, Object event) {
        Utilities.writeObjectToIntent(event, intent);
        intent.addFlags(SQLiteDatabase.CREATE_IF_NECESSARY);
        MyiBaseApplication.getBaseAppContext().getApplicationContext().startActivity(intent);
    }
}
