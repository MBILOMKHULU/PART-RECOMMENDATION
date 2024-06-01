package com.example.dishcovery1.ui.notifications;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.dishcovery1.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Intent;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.ArrayList;
import java.util.List;
import android.widget.SearchView;

public class MainActivity2 extends AppCompatActivity {
    private String loggedInUserName;
    private ImageView[] imageViews = new ImageView[13];
    private TextView[] averageRatingTextViews = new TextView[13];
    private ApiService apiService;
    private List<String> allRecipes = new ArrayList<>();
    private List<String> filteredRecipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        initializeUIElements();

        new com.example.dishcovery1.ui.notifications.MainActivity2.GetImagesTask().execute("https://lamp.ms.wits.ac.za/home/s2538889/fetch_images.php");
        new com.example.dishcovery1.ui.notifications.MainActivity2.GetRecipesTask().execute("https://lamp.ms.wits.ac.za/home/s2538889/get_recipes.php");

        Intent intent = getIntent();
        loggedInUserName = intent.getStringExtra("USER_NAME");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://lamp.ms.wits.ac.za/home/s2538889/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        fetchAverageRatings();
        setupCommentsAndRatings();
        setupSearchFunctionality();

    }

    private void initializeUIElements() {
        for (int i = 0; i < 13; i++) {
            int imageViewId = getResources().getIdentifier("imageView" + (i + 1), "id", getPackageName());
            imageViews[i] = findViewById(imageViewId);

            int textViewId = getResources().getIdentifier("averageRatingTextView" + (i + 1), "id", getPackageName());
            averageRatingTextViews[i] = findViewById(textViewId);
        }
    }

    private void setupSearchFunctionality() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRecipes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecipes(newText);
                return true;
            }
        });
    }

    private void displayImage(int index, Bitmap bitmap) {
        imageViews[index].setImageBitmap(bitmap);
    }

    private class GetImagesTask extends AsyncTask<String, Void, Bitmap[]> {
        @Override
        protected Bitmap[] doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray imagesArray = jsonResponse.getJSONArray("images");
                Bitmap[] bitmaps = new Bitmap[imagesArray.length()];

                for (int i = 0; i < imagesArray.length(); i++) {
                    JSONObject imageObject = imagesArray.getJSONObject(i);
                    String imageDataString = imageObject.getString("image_data");
                    byte[] imageData = Base64.decode(imageDataString, Base64.DEFAULT);
                    bitmaps[i] = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                }

                return bitmaps;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            if (bitmaps != null) {
                for (int i = 0; i < bitmaps.length && i < 13; i++) {
                    displayImage(i, bitmaps[i]);
                }
            }
        }
    }

    private void setupCommentsAndRatings() {
        for (int i = 0; i < 13; i++) {
            int layoutId = getResources().getIdentifier("recipe" + (i + 1) + "Layout", "id", getPackageName());
            int ratingBarId = getResources().getIdentifier("ratingBar_recipe" + (i + 1), "id", getPackageName());
            int editTextId = getResources().getIdentifier("editText" + (i + 1), "id", getPackageName());
            int buttonId = getResources().getIdentifier("button" + (i + 1), "id", getPackageName());
            int textViewId = getResources().getIdentifier("textView" + (i + 1), "id", getPackageName());

            setUpCommentsFunctionality(layoutId, ratingBarId, editTextId, buttonId, textViewId, i);
        }
    }

    private void setUpCommentsFunctionality(int layoutId, int ratingBarId, int editTextId, int buttonId, int textViewId, int index) {
        LinearLayout recipeLayout = findViewById(layoutId);
        RatingBar ratingBar = recipeLayout.findViewById(ratingBarId);
        EditText editText = recipeLayout.findViewById(editTextId);
        Button button = recipeLayout.findViewById(buttonId);
        TextView textView = recipeLayout.findViewById(textViewId);

        fetchAndDisplayComments(index + 1, textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = editText.getText().toString();
                float rating = ratingBar.getRating();

                if (validateInput(commentText, rating)) {
                    sendCommentToServer(loggedInUserName, commentText, rating, index + 1);
                }
            }
        });
    }

    private boolean validateInput(String commentText, float rating) {
        if (commentText.isEmpty()) {
            Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Please enter your comment", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rating == 0) {
            Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void sendCommentToServer(String username, String comment, float rating, int recipeId) {
        Call<JsonObject> call = apiService.submitComment(username, comment, rating, recipeId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject responseObject = response.body();
                    if (responseObject.has("success") && responseObject.get("success").getAsBoolean()) {
                        Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Comment submitted successfully", Toast.LENGTH_SHORT).show();
                        refreshActivity();
                    } else {
                        Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Failed to submit comment", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Unexpected response from server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAndDisplayComments(int recipeId, TextView textView) {
        Call<List<Comment>> call = apiService.getComments(recipeId);
        call.enqueue(new Callback<List<Comment>>() {
            @Override
            public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> comments = response.body();
                    StringBuilder commentsText = new StringBuilder();
                    for (Comment comment : comments) {
                        commentsText.append("User: ").append(comment.getUsername())
                                .append("\nRating: ").append(comment.getRating())
                                .append("\nComment: ").append(comment.getComment()).append("\n\n");
                    }
                    textView.setText(commentsText.toString());
                }
            }

            @Override
            public void onFailure(Call<List<Comment>> call, Throwable t) {
                Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private class GetRecipesTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                rd.close();
                return parseJSON(result.toString());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] recipes) {
            if (recipes != null) {
                allRecipes.clear();
                for (String recipe : recipes) {
                    allRecipes.add(recipe);
                }
                filteredRecipes.addAll(allRecipes);
                updateUI(filteredRecipes.toArray(new String[0]));
            }
        }

        private String[] parseJSON(String json) throws JSONException {
            JSONArray jsonArray = new JSONArray(json);
            String[] recipes = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                recipes[i] = jsonArray.getString(i);
            }
            return recipes;
        }
    }

    private void updateUI(String[] recipes) {
        TextView[] recipeTextViews = new TextView[13];
        for (int i = 0; i < 13; i++) {
            int textViewId = getResources().getIdentifier("recipeTextView" + (i + 1), "id", getPackageName());
            recipeTextViews[i] = findViewById(textViewId);
        }

        for (int i = 0; i < 13 && i < recipes.length; i++) {
            recipeTextViews[i].setText(recipes[i]);
        }
    }

    private void fetchAverageRatings() {
        Call<List<AverageRating>> call = apiService.getAverageRatings();
        call.enqueue(new Callback<List<AverageRating>>() {
            @Override
            public void onResponse(Call<List<AverageRating>> call, Response<List<AverageRating>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<AverageRating> averageRatings = response.body();
                    for (int i = 0; i < averageRatings.size(); i++) {
                        AverageRating rating = averageRatings.get(i);
                        int recipeId = rating.getRecipeId();
                        float average = rating.getAverageRating();
                        averageRatingTextViews[recipeId - 1].setText("Average Rating: " + average);
                    }
                } else {
                    Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Failed to load average ratings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AverageRating>> call, Throwable t) {
                Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRecipes(String query) {
        filteredRecipes.clear();
        for (String recipe : allRecipes) {
            if (recipe.toLowerCase().contains(query.toLowerCase())) {
                filteredRecipes.add(recipe);
            }
        }
        if (filteredRecipes.isEmpty()) {
            Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, "No recipes found for the search query", Toast.LENGTH_SHORT).show();
        } else {
            StringBuilder resultMessage = new StringBuilder("Found Recipes:\n");
            for (String recipe : filteredRecipes) {
                resultMessage.append(recipe).append("\n");
            }
            Toast.makeText(com.example.dishcovery1.ui.notifications.MainActivity2.this, resultMessage.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
