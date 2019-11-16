package se.juneday.lifegame.android;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import se.juneday.lifegame.domain.Situation;
import se.juneday.lifegame.domain.Suggestion;
import se.juneday.lifegame.domain.ThingAction;

public class EngineVolley {

    private static final String LOG_TAG = EngineVolley.class.getSimpleName();
    private Context context;
    private static EngineVolley instance;
    private SituationChangeListener listener;

//    private String baseUrl = "http://10.0.2.2:8080";
    private String baseUrl = "http://rameau.sandklef.com:8081";
    private String gameUrl = "lifegame";
    private String formatUrl = "format=json";
    private String URL_SEP = "/";

//    private String gameId;

    private EngineVolley(Context context) {
        this.context = context;
    }

    public static synchronized EngineVolley getInstance(Context context) {
        if (instance==null) {
            instance = new EngineVolley(context);
        }
        return instance;
    }

    public interface SituationChangeListener {
        void onSituationChangeList(Situation situation);
        void onVictory(String message);
        void onError(String message);
        void onGamesChange(List<GameInfo> games);
    }

    public void setSituationChangeListener(SituationChangeListener listener) {
        this.listener = listener;
    }

    private String baseUrl(String gameId) {
        return baseUrl +  URL_SEP + gameUrl + "?gameId=" + gameId;
    }

    private String formatUrl(String gameId) {
        return baseUrl(gameId) + "&" + formatUrl;
    }

    private String suggestionUrl(String suggestion, String gameId) throws UnsupportedEncodingException {
        return formatUrl(gameId) + "&suggestion=" + suggestion;
    }

    private String currentUrl(String gameId) throws UnsupportedEncodingException {
        return formatUrl(gameId) +"&action=current";
    }

    private String newGameUrl(String world) throws UnsupportedEncodingException {
        return baseUrl + URL_SEP + gameUrl  + "?" + formatUrl + "&world=" + world;
    }

    private String takeThingUrl(String gameId, String thing) throws UnsupportedEncodingException {
        return formatUrl(gameId) +"&pickup=" + thing;
    }

    private String dropThingUrl(String gameId, String thing) throws UnsupportedEncodingException {
        return formatUrl(gameId) +"&drop=" + thing;
    }

    private String gamesUrl() throws UnsupportedEncodingException {
        return baseUrl + URL_SEP +gameUrl + "?worlds=true&format=json";
    }

    private String exitUrl(String gameId) {
        return formatUrl(gameId) + "&exit=true";
    }

    public void exitGame(String gameId) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = null;
        jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                exitUrl(gameId),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(LOG_TAG, "exitGame: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "exitGame() failed");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }

    public void getGames() {
        String url = null;
        Log.d(LOG_TAG, "getGames()   ");
        try {
            url = gamesUrl();
            Log.d(LOG_TAG, "getGames()   url: " + gamesUrl());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            listener.onError("Failed to create url");
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest = null;
        jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(LOG_TAG, "gamesUrl (new game list): " + response);
                        listener.onGamesChange(jsonToGames(response));
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "getGames() failed: " + error ) ;
                handleError(error, listener);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }


    public void currentSituation(String gameId) {
        Log.d(LOG_TAG, "currentSituation()");
        String url = null;
        try {
            url = currentUrl(gameId);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            listener.onError("Failed to create url");
            return;
        }
        getData(url);
    }

    public void getSituation(String gameId, String suggestion) {
        Log.d(LOG_TAG, "getSituation(" + suggestion + ", " + gameId + ")");
        String url = null;
        try {
            url = suggestionUrl(suggestion, gameId);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            listener.onError("Failed to create url");
        }
        getData(url);
    }


    public void getData(String url) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest = null;
            jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            handleResponse(response, listener);
//                            listener.onSituationChangeList(jsonToSituation(response));
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    handleError(error, listener);
                }
            });
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public List<Suggestion> jsonToSuggestions(JSONArray jarray) {
        Log.d(LOG_TAG, "Adding suggestion: " + jarray);
        try {
            List<Suggestion> suggestions = new ArrayList<>();
            for (int i = 0; i < jarray.length(); i++) {
                String phrase = jarray.getString(i);
                suggestions.add(new Suggestion(phrase));
                Log.d(LOG_TAG, "Adding suggestion: " + phrase);
            }
            return suggestions;
        } catch (JSONException je) {
            Log.d(LOG_TAG, "je: " + je);
            listener.onError("Failed to contact server");
        }
        return null;
    }

    public List<GameInfo> jsonToGames(JSONArray jarray) {
        Log.d(LOG_TAG, "Adding games ---- " + jarray);
        try {
            List<GameInfo> games = new ArrayList<>();
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jo = jarray.getJSONObject(i);
                String title = jo.getString("title");
                String subTitle = jo.getString("subtitle");
                String url = jo.getString("url");
                games.add(new GameInfo(title, subTitle, url));
                Log.d(LOG_TAG, "Adding games ---- " + title);
            }
            Log.d(LOG_TAG, "Adding games ---- return " + games.size());
            return games;
        } catch (JSONException je) {
            Log.d(LOG_TAG, "je: " + je);
            listener.onError("Failed to contact server");
        }
        return null;
    }

    public List<ThingAction> jsonToThings(JSONObject jo, String tag) {
        JSONArray jarray = null;
        try {
            jarray = jo.getJSONArray(tag);
        } catch (JSONException je) {
            Log.d(LOG_TAG, "jsonToString, failed getting array from  jo:" + jo + " tag:" + tag);
            return new ArrayList<ThingAction>();
        }

        if (jarray==null) {
            return new ArrayList<ThingAction>();
        }

        try {
            List<ThingAction> things = new ArrayList<>();
            for (int i = 0; i < jarray.length(); i++) {
                String thing = jarray.getString(i);
                things.add(new ThingAction(thing));
                Log.d(LOG_TAG, "Adding room thing: " + thing);
            }
            return things;
        } catch (JSONException je) {
            Log.d(LOG_TAG, "je: " + je);
            listener.onError("Failed to contact server");
        }
        return null;
    }


    public Situation jsonToSituation(JSONObject response) throws JSONException {
//     public Situation(String title, String description, String question, List<Suggestion> suggestions, List<ThingAction> actions) {
        String gameTitle = jsonToString(response,"gametitle");
        String gameSubTitle = jsonToString(response, "gamesubtitle");
        String title = jsonToString(response, "title");
        String gameId = jsonToString(response, "gameid");
        String question = jsonToString(response,"question");
        String description = jsonToString(response, "description");
        List<ThingAction> things = jsonToThings(response, "things");
        List<ThingAction> actions = jsonToThings(response,"actions");
        List<Suggestion> suggestions = jsonToSuggestions(response.getJSONArray("suggestions"));
        return new Situation(gameTitle, gameSubTitle, gameId, title, description, question, suggestions, actions, things, response.getString("explanation"));
    }

    private String jsonToString(JSONObject jo, String tag) {
        try {
            return jo.getString(tag);
        } catch (JSONException je) {
            Log.d(LOG_TAG, "jsonToString, failed reading " + tag);
            return "";
        }
    }

/*    private void extractUpdateGameId(JSONObject response) {
        try {
            String id = response.getString("gameid");
            gameId = id;
        } catch (JSONException e) {
            //e.printStackTrace();
        }
    }
*/

    private Situation handleSituation(JSONObject response, SituationChangeListener listener){
        try {
            //extractUpdateGameId(response);
            Situation situation = jsonToSituation(response);
            listener.onSituationChangeList(situation);
            return situation;
        } catch (JSONException e) {
          //  e.printStackTrace();
        }
        return null;
    }

    private String handleEndOfGame(JSONObject response, SituationChangeListener listener){
        try {
            String message = response.getString("end");
            listener.onVictory(message);
            return message;
        } catch (JSONException e) {
            // e.printStackTrace();
        }
        return null;
    }

    private String handleError(JSONObject response, SituationChangeListener listener){
        try {
            String message = response.getString("error");
            listener.onError(message);
            return message;
        } catch (JSONException e) {
            // e.printStackTrace();
        }
        return null;
    }

    private void handleResponse(JSONObject response, SituationChangeListener listener) {
        Log.d(LOG_TAG, " response: " + response);
        if (handleSituation(response, listener) != null) {
            Log.d(LOG_TAG, " response: handled situation");
            return;
        }
        if (handleEndOfGame(response, listener) != null) {
            Log.d(LOG_TAG, " response: end of game");
            return;
        }
        if (handleError(response, listener) != null) {
            Log.d(LOG_TAG, " response: handled error");
            Log.d(LOG_TAG, "Found error...." );
            return;
        }
    }

    private void handleError(VolleyError error, SituationChangeListener listener) {
        if (error.getCause()!=null) {
            Log.d(LOG_TAG, " cause: " + error.getCause().getMessage());
        } else {
            Log.d(LOG_TAG, " cause: " + error.getMessage());
        }
        listener.onError("Failed to contact server");
    }

    public void getGame(String world) {
        Log.d(LOG_TAG, "getGame(" + world+")    world: " + world);
        try {
            String url = newGameUrl(world);
            getData(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void takeThing(String gameId, String thing) {
        Log.d(LOG_TAG, "takeThing(" + thing+")");
        try {
            String url = takeThingUrl(gameId, thing);
            getData(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void dropThing(String gameId, String thing) {
        Log.d(LOG_TAG, "dropThing(" + thing+")");
        try {
            String url = dropThingUrl(gameId, thing);
            getData(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public static class GameInfo {
        public String title;
        public String subTitle;
        public String url;
        public GameInfo(String title, String subTtle, String url) {
            this.title = title;
            this.subTitle = subTitle;
            this.url = url;
        }
    }
}
