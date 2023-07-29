package paositra.marchand.service;

import org.json.JSONObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Header;

public interface ApiService {

    //login marchand
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    @POST("auth/loginUserMarchand")
    Call<JSONObject> login(@Body RequestBody requestBody);

    //get solde marchand
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    @GET("monetique/operationMarchand/getSoldeMarchand")
    Call<JSONObject> getSoldeMarchand(@Header("Authorization") String authorization);

    //initialisation paiement
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    @GET("monetique/operationMarchand/initialisationRetraitEspeceWithCodeRetrait")
    Call<JSONObject> initialisationRetraitEspeceWithCodeRetrait(@Header("Authorization") String authorization, @Body RequestBody requestBody);

    //validation paiement
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    @GET("monetique/operationMarchand/validationRetraitEspeceByCodeRetrait")
    Call<JSONObject> validationRetraitEspeceByCodeRetrait(@Header("Authorization") String authorization, @Body RequestBody requestBody);

}
