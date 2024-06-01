package com.example.dishcovery1.ui.notifications;

import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

public interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    Call<JsonObject> logIn(
            @Field("username") String username,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("sign_up.php")
    Call<JsonObject> signUp(
            @Field("username") String username,
            @Field("password") String password,
            @Field("admin") int admin
    );

    @FormUrlEncoded
    @POST("recipe_comment.php")
    Call<JsonObject> submitComment(
            @Field("username") String username,
            @Field("comment") String comment,
            @Field("rating") float rating,
            @Field("recipe_id") int recipeId
    );

    @GET("get_average_ratings.php")
    Call<List<AverageRating>> getAverageRatings();

    @GET("fetch_images.php")
    Call<JsonObject> fetchImageData();

    @GET("get_comments.php")
    Call<List<Comment>> getComments(@Query("recipe_id") int recipeId);
}
