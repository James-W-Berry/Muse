package com.berryspace.Connectors;

import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.berryspace.conjure.Artist;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ArtistSearchService {
    private static final String TAG = "ArtistSearchService";
    private static final String ENDPOINT = "https://api.spotify.com/v1/search";
    private String mToken;
    private RequestQueue mQueue;
    private String mQuery;
    private ArrayList<Artist> artists = new ArrayList<>();

    public ArtistSearchService(RequestQueue queue, String token, String query) {
        mQueue = queue;
        mToken = token;
        mQuery = query;
    }

    public ArrayList<Artist> getArtists() {
        return artists;
    }

    public void getSearchResultArtists(final VolleyCallback callBack) throws JSONException {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, ENDPOINT + "?q=" + mQuery + "&type=artist", null, response -> {
                     try {
                        JSONObject result = response.getJSONObject("artists");
                        JSONArray items = (JSONArray) result.get("items");
                         for (int n = 0; n < items.length(); n++) {
                             try {
                                 JSONObject object = items.getJSONObject(n);
                                 JSONArray images = (JSONArray) object.get("images");
                                 object.put("imageUrl", images.get(0));
                                 Artist artist = new Artist();
                                 artist.setName((String) object.get("name"));
                                 JSONObject image = (JSONObject) images.get(0);
                                 artist.setImageUrl(image.get("url").toString());
                                 JSONArray genres = (JSONArray) object.get("genres");
                                 artist.setGenres(
                                         genres.toString().replace("[","")
                                                 .replace("]","")
                                                 .replace("\"", "")
                                                 .replace(",",", "));
                                 JSONObject followers = (JSONObject) object.get("followers");
                                 NumberFormat numberFormat = NumberFormat.getInstance();
                                 numberFormat.setGroupingUsed(true);
                                 artist.setFollowers((String) numberFormat.format(followers.get("total")));
                                 artist.setId((String) object.get("id"));

                                 artists.add(artist);
                             } catch (JSONException e) {
                                 e.printStackTrace();
                             }
                         }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    callBack.onSuccess();
                }, error -> {
                    // TODO: Handle error

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String auth = "Bearer " + mToken;
                headers.put("Authorization", auth);
                return headers;
            }
        };
        mQueue.add(jsonObjectRequest);
    }
}