package com.netspace.library.interfaces;

import com.netspace.library.database.AnswerSheetStudentAnswer;
import com.netspace.library.database.RESTSynchronizePackage;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AnswerSheetStudentAnswerDataService {
    @DELETE("{field}/{guid}")
    Call<Void> deleteItem(@Path("field") String str, @Path("guid") String str2, @Header("REST-Fields") String str3, @Header("REST-Values") String str4);

    @GET("{field}/{guid}")
    Call<AnswerSheetStudentAnswer> getItem(@Path("field") String str, @Path("guid") String str2, @Header("REST-Fields") String str3, @Header("REST-Values") String str4);

    @GET("{field}/{guids}")
    Call<List<AnswerSheetStudentAnswer>> getItems(@Path("field") String str, @Path("guids") String str2, @Header("REST-GUIDs") List<String> list, @Header("REST-Fields") String str3, @Header("REST-Values") String str4);

    @POST("{field}/{guid}")
    Call<Void> putItem(@Path("field") String str, @Path("guid") String str2, @Body AnswerSheetStudentAnswer answerSheetStudentAnswer, @Header("REST-Fields") String str3, @Header("REST-Values") String str4);

    @POST("{field}/{guid}")
    Call<Void> putItems(@Path("field") String str, @Path("guid") String str2, @Body List<AnswerSheetStudentAnswer> list, @Header("REST-Fields") String str3, @Header("REST-Values") String str4);

    @POST("{field}/")
    Call<RESTSynchronizePackage> synchronizeData(@Path("field") String str, @Body List<RESTBasicData> list, @Header("REST-Fields") String str2, @Header("REST-Values") String str3);
}
