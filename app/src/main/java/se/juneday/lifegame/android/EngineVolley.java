/*
 *    Copyright (C) 2019 Henrik Sandklef
 *
 *    This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.juneday.lifegame.android;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import se.juneday.lifegame.domain.Situation;
import se.juneday.lifegame.domain.Suggestion;
import se.juneday.lifegame.domain.ThingAction;

public class EngineVolley {

    private static final String LOG_TAG = EngineVolley.class.getSimpleName();
    private final Context context;
 //   private static EngineVolley instance;
    private SituationChangeListener listener;

   private final String baseUrl = "http://10.0.2.2:8080";
//    private String baseUrl = "http://192.168.1.138:8080";
//    private String baseUrl = "http://rameau.sandklef.com:8081";
    private final String gameUrl = "lifegame";
    private final String formatUrl = "format=json";
    private final String URL_SEP = "/";

//    private String gameId;

    public EngineVolley(Context context) {
        this.context = context;
    }

/*    public static synchronized EngineVolley getInstance() {
        if (instance == null) {
            instance = new EngineVolley();
        }
        return instance;
    }
*/

    public String webUrl(String gameId) {
        return baseUrl + URL_SEP + gameUrl + "?gameId=" + gameId + "&format=html";
    }

    public String webAdminUrl(String gameId) {
        return baseUrl + URL_SEP + gameUrl + "?gameId=" + gameId + "&format=html&admin=true";
    }

    public interface SituationChangeListener {
        void onSituationChangeList(Situation situation);

        void onGameExit();

        void onVictory(String message);

        void onError(String message);

        void onGamesChange(List<GameInfo> games);
    }

    public void setSituationChangeListener(SituationChangeListener listener) {
        this.listener = listener;
    }

    private String baseUrl(String gameId) {
        return baseUrl + URL_SEP + gameUrl + "?gameId=" + gameId;
    }

    private String formatUrl(String gameId) {
        return baseUrl(gameId) + "&" + formatUrl;
    }

    private String suggestionUrl(String suggestion, String gameId) {
        return formatUrl(gameId) + "&suggestion=" + suggestion;
    }

    private String currentUrl(String gameId) {
        return formatUrl(gameId) + "&action=current";
    }

    private String newGameUrl(String world) {
        return baseUrl + URL_SEP + gameUrl + "?" + formatUrl + "&world=" + world;
    }

    private String takeThingUrl(String gameId, String thing) {
        return formatUrl(gameId) + "&pickup=" + thing;
    }

    private String dropThingUrl(String gameId, String thing) {
        return formatUrl(gameId) + "&drop=" + thing;
    }

    private String gamesUrl() {
        return baseUrl + URL_SEP + gameUrl + "?worlds=true&format=json";
    }

    private String exitUrl(String gameId) {
        return formatUrl(gameId) + "&exit=true";
    }

    public void exitGame(String gameId) {
        Log.d(LOG_TAG, "exitGame: " + gameId);
        Log.d(LOG_TAG, "exitGame: " + exitUrl(gameId));
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest;
        jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                exitUrl(gameId),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(LOG_TAG, "exitGame: " + response);
                        listener.onGameExit();
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(LOG_TAG, "exitGame() failed: " + error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public void getGames() {
        String url;
        Log.d(LOG_TAG, "getGames()   ");
        url = gamesUrl();
        Log.d(LOG_TAG, "getGames()   url: " + gamesUrl());
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonArrayRequest;
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
                Log.d(LOG_TAG, "getGames() failed: " + error);
                handleError(error, listener);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }


    public void currentSituation(String gameId) {
        Log.d(LOG_TAG, "currentSituation()");
        String url;
        url = currentUrl(gameId);
        getData(url);
    }

    public void getSituation(String gameId, String suggestion) {
        Log.d(LOG_TAG, "getSituation(" + suggestion + ", " + gameId + ")");
        String url;
        url = suggestionUrl(suggestion, gameId);
        getData(url);
    }


    private void getData(String url) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest jsonObjectRequest;
        Log.d(LOG_TAG, "url: " + url);
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

    private List<Suggestion> jsonToSuggestions(JSONArray jarray) {
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

    private List<GameInfo> jsonToGames(JSONArray jarray) {
        Log.d(LOG_TAG, "Adding games ---- " + jarray);
        try {
            List<GameInfo> games = new ArrayList<>();
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jo = jarray.getJSONObject(i);
                String title = jo.getString("title");
                String subTitle = jo.getString("subtitle");
                String url = jo.getString("url");
                games.add(new GameInfo(title, subTitle, url));
                Log.d(LOG_TAG, "Adding games ---- " + title + " (" + subTitle + ")");
            }
            Log.d(LOG_TAG, "Adding games ---- return " + games.size());
            return games;
        } catch (JSONException je) {
            Log.d(LOG_TAG, "je: " + je);
            listener.onError("Failed to contact server");
        }
        return null;
    }

    private List<ThingAction> jsonToThings(JSONObject jo, String tag) {
        JSONArray jarray;
        try {
            jarray = jo.getJSONArray(tag);
        } catch (JSONException je) {
            Log.d(LOG_TAG, "jsonToString, failed getting array from  jo:" + jo + " tag:" + tag);
            return new ArrayList<>();
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


    private Situation jsonToSituation(JSONObject response) throws JSONException {
//     public Situation(String title, String description, String question, List<Suggestion> suggestions, List<ThingAction> actions) {
        String gameTitle = jsonToString(response, "gametitle");
        String gameSubTitle = jsonToString(response, "gamesubtitle");
        String title = jsonToString(response, "title");
        String gameId = jsonToString(response, "gameid");
        String question = jsonToString(response, "question");
        String description = jsonToString(response, "description");
        List<ThingAction> things = jsonToThings(response, "things");
        List<ThingAction> actions = jsonToThings(response, "actions");
        List<Suggestion> suggestions = jsonToSuggestions(response.getJSONArray("suggestions"));
        long millis = response.getLong("millisleft");
        Log.d(LOG_TAG, "expires: " + millis);
        int score = response.getInt("score");
        int situationCount = response.getInt("situationcount");
        Log.d(LOG_TAG, "jsonToSituation()   " + gameId);

        Situation situation = new Situation(gameTitle, gameSubTitle, gameId, title, description,
                question, suggestions, actions, things, response.getString("explanation"),
                millis, score, situationCount);
        Log.d(LOG_TAG, "jsonToSituation()   " + situation.gameId() + " " + situation.score());

        return situation;
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

    private Situation handleSituation(JSONObject response, SituationChangeListener listener) {
        try {
            Log.d(LOG_TAG, "handleSituation()");
            //extractUpdateGameId(response);
            Situation situation = jsonToSituation(response);
            Log.d(LOG_TAG, "handleSituation()  calling onSituationChangeList :: " + situation);
            listener.onSituationChangeList(situation);
            return situation;
        } catch (JSONException e) {
            //  e.printStackTrace();
        }
        return null;
    }

    private String handleEndOfGame(JSONObject response, SituationChangeListener listener) {
        try {
            String message = response.getString("end");
            listener.onVictory(message);
            return message;
        } catch (JSONException e) {
            // e.printStackTrace();
        }
        return null;
    }

    private String handleError(JSONObject response, SituationChangeListener listener) {
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
            Log.d(LOG_TAG, "Found error....");
            // not needed - but feels more in line with the above code
            return;
        }
    }

    private void handleError(VolleyError error, SituationChangeListener listener) {
        if (error.getCause() != null) {
            Log.d(LOG_TAG, " cause: " + error.getCause().getMessage());
        } else {
            Log.d(LOG_TAG, " cause: " + error.getMessage());
        }
        listener.onError("Failed to contact server");
    }

    public void getGame(String world) {
        Log.d(LOG_TAG, "getGame(" + world + ")    world: " + world);
        String url = newGameUrl(world);
        getData(url);
    }

    public void takeThing(String gameId, String thing) {
        Log.d(LOG_TAG, "takeThing(" + thing + ")");
        String url = takeThingUrl(gameId, thing);
        getData(url);
    }

    public void dropThing(String gameId, String thing) {
        Log.d(LOG_TAG, "dropThing(" + thing + ")");
        String url = dropThingUrl(gameId, thing);
        getData(url);
    }


    public static class GameInfo implements Serializable {
        private static final long serialVersionUID = -5098838482768516652L;
        public final String title;
        public final String subTitle;
        public final String url;

        public GameInfo(String title, String subTitle, String url) {
            this.title = title;
            this.subTitle = subTitle;
            this.url = url;
        }

        public String toString() {
            return "<h1>" + title + "</h1>\n" + subTitle;
        }

    }
}
