package com.netspace.library.virtualnetworkobject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.netspace.library.application.MyiBaseApplication;
import com.netspace.library.database.AnswerSheetResult;
import com.netspace.library.database.AnswerSheetResultDao.Properties;
import com.netspace.library.database.AnswerSheetStudentAnswer;
import com.netspace.library.database.AnswerSheetStudentAnswerDao;
import com.netspace.library.database.DaoMaster;
import com.netspace.library.database.DaoMaster.DevOpenHelper;
import com.netspace.library.database.DaoSession;
import com.netspace.library.im.IMService;
import com.netspace.library.interfaces.AnswerSheetDataService;
import com.netspace.library.interfaces.AnswerSheetStudentAnswerDataService;
import com.netspace.library.receiver.WifiReceiver.WifiConnect;
import com.netspace.library.receiver.WifiReceiver.WifiDisconnect;
import com.netspace.library.struct.TableChangeMessage;
import com.netspace.library.virtualnetworkobject.helper.RESTDataEngineHelper;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import okhttp3.Interceptor;
import okhttp3.Interceptor.Chain;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.http.cookie.SM;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.gson.GsonConverterFactory;

public class RESTEngine extends Engine {
    private static final String TAG = "RESTEngine";
    private static RESTEngine mInstance = null;
    private RESTDataEngineHelper<AnswerSheetDataService, AnswerSheetResult> mAnswerSheetHelper;
    private RESTDataEngineHelper<AnswerSheetStudentAnswerDataService, AnswerSheetStudentAnswer> mAnswerSheetStudentAnswerHelper;
    private DaoSession mDaoSession;
    private String mEngineName = TAG;
    private Gson mGson;
    private OkHttpClient mOKHttpClient;
    private Retrofit mRetrofit = null;
    private boolean mbAutoSychronizeOnWifiConnect = true;

    public RESTEngine(Context Context) {
        super(Context);
        mInstance = this;
    }

    public static RESTEngine getDefault() {
        return mInstance;
    }

    public void setAutoSychronizeOnWifiConnect(boolean bEnable) {
        this.mbAutoSychronizeOnWifiConnect = bEnable;
    }

    public String getEngineName() {
        return this.mEngineName;
    }

    public void startEngine() {
        this.mOKHttpClient = new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {
            public Response intercept(Chain chain) throws IOException {
                return chain.proceed(chain.request().newBuilder().header(SM.COOKIE, "szUserName=" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + ";szUserGUID=" + MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID + ";").build());
            }
        }).hostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).build();
        this.mOKHttpClient.dispatcher().setMaxRequestsPerHost(2);
        this.mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").excludeFieldsWithModifiers(128).create();
        this.mRetrofit = new Builder().baseUrl(MyiBaseApplication.getProtocol() + "://" + MyiBaseApplication.getCommonVariables().ServerInfo.szServerAddress + "/restfuldatasource/").callbackExecutor(Executors.newSingleThreadExecutor()).client(this.mOKHttpClient).addConverterFactory(GsonConverterFactory.create(this.mGson)).build();
        String szDBName = "RestDataBase_" + MyiBaseApplication.getCommonVariables().UserInfo.szUserName + ".db";
        if (!MyiBaseApplication.ReleaseBuild) {
            szDBName = new File(Environment.getExternalStorageDirectory(), "/" + szDBName).getAbsolutePath();
        }
        this.mDaoSession = new DaoMaster(new DevOpenHelper(this.mContext, szDBName).getWritableDb()).newSession();
        this.mAnswerSheetHelper = new RESTDataEngineHelper(this.mRetrofit, "answersheetresult", this.mDaoSession, AnswerSheetDataService.class, AnswerSheetResult.class, Properties.Guid);
        this.mAnswerSheetHelper.setTableName("AnswerSheetResult");
        this.mAnswerSheetStudentAnswerHelper = new RESTDataEngineHelper(this.mRetrofit, "answersheetstudentanswer", this.mDaoSession, AnswerSheetStudentAnswerDataService.class, AnswerSheetStudentAnswer.class, AnswerSheetStudentAnswerDao.Properties.Guid);
        this.mAnswerSheetStudentAnswerHelper.setTableName("AnswerSheetStudentAnswer");
        EventBus.getDefault().register(this);
        super.startEngine();
    }

    public void stopEngine() {
        this.mDaoSession.getDatabase().close();
        this.mDaoSession = null;
        EventBus.getDefault().unregister(this);
        this.mAnswerSheetStudentAnswerHelper = null;
        this.mAnswerSheetHelper = null;
        super.stopEngine();
    }

    public Retrofit getRetrofit() {
        return this.mRetrofit;
    }

    public DaoSession getDaoSession() {
        return this.mDaoSession;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onWifiConnect(WifiConnect data) {
        if (this.mbAutoSychronizeOnWifiConnect) {
            this.mAnswerSheetHelper.doSychronize();
            this.mAnswerSheetStudentAnswerHelper.doSychronize();
        }
    }

    @Subscribe
    public void onWifiDisconnect(WifiDisconnect data) {
    }

    @Subscribe
    public void onTableChangeMessage(TableChangeMessage message) {
        Log.d(TAG, "onTableChangeMessage szTableName=" + message.szTableName);
        for (int i = 0; i < message.arrGUIDs.size(); i++) {
            if (message.szTableName.equalsIgnoreCase("answersheetresult")) {
                this.mAnswerSheetHelper.getAndSaveItem((String) message.arrGUIDs.get(i));
            } else if (message.szTableName.equalsIgnoreCase("answersheetstudentanswer")) {
                this.mAnswerSheetStudentAnswerHelper.getAndSaveItem((String) message.arrGUIDs.get(i));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAnswerSheetResult(AnswerSheetResult data) {
        if (data.getClientid() == null) {
            data.setClientid(IMService.getIMUserName());
        }
        if (data.getUsername() == null) {
            data.setUsername(MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        }
        if (data.getUserguid() == null) {
            data.setUserguid(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        }
        if (data.getStudentname() == null) {
            data.setStudentname(MyiBaseApplication.getCommonVariables().UserInfo.szRealName);
        }
        data.setSyn_timestamp(new Date());
        this.mAnswerSheetHelper.saveData(data, data.getGuid());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onAnswerSheetStudentAnswer(AnswerSheetStudentAnswer data) {
        if (data.getClientid() == null) {
            data.setClientid(IMService.getIMUserName());
        }
        if (data.getUsername() == null) {
            data.setUsername(MyiBaseApplication.getCommonVariables().UserInfo.szUserName);
        }
        if (data.getUserguid() == null) {
            data.setUserguid(MyiBaseApplication.getCommonVariables().UserInfo.szUserGUID);
        }
        if (data.getStudentname() == null) {
            data.setStudentname(MyiBaseApplication.getCommonVariables().UserInfo.szRealName);
        }
        data.setSyn_timestamp(new Date());
        this.mAnswerSheetStudentAnswerHelper.saveData(data, data.getGuid());
    }

    public RESTDataEngineHelper<AnswerSheetDataService, AnswerSheetResult> getAnswerSheetHelper() {
        return this.mAnswerSheetHelper;
    }

    public RESTDataEngineHelper<AnswerSheetStudentAnswerDataService, AnswerSheetStudentAnswer> getAnswerSheetStudentAnswerHelper() {
        return this.mAnswerSheetStudentAnswerHelper;
    }

    public boolean handlePackageRead(ItemObject OneObject) {
        return false;
    }

    public boolean handlePackageWrite(ItemObject OneObject) {
        return false;
    }
}
