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

package se.juneday;

import android.util.Log;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.juneday.lifegame.android.EngineVolley;

public class Session implements Serializable {

    private static final String LOG_TAG = Session.class.getSimpleName();
    private static final long serialVersionUID = 2475346870388802367L;
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static Session instance;
    private List<EngineVolley.GameInfo> games;

    public static class SessionHolder implements Serializable {
        private static final long serialVersionUID = 2156036609439180314L;
        String gameId;
        Calendar expirationTime;
        SessionHolder(String gameId, long millis) {
            Log.d(LOG_TAG, "SessionHolder() m: " + millis);
            Calendar cal = Calendar.getInstance();
            Log.d(LOG_TAG, "SessionHolder() c: " + cal.getTime());
            cal.add(Calendar.MILLISECOND, (int) millis);
            Log.d(LOG_TAG, "SessionHolder() c: " + cal.getTime());
            expirationTime = cal;
            this.gameId = gameId;
        }
    }

    public static void instance(Session session) {
        instance = session;
    }

    public static Session getInstance() {
        return instance;
    }

    private Map<String, SessionHolder> worldGameId;

    //    public String gameId;
    public String currentWorld;

    public Session() {
        worldGameId = new HashMap<>();
    }

    public Map<String, SessionHolder> worldIds() {
        return Collections.unmodifiableMap(worldGameId);
    }

    public void logCache() {
        Log.d(LOG_TAG, "Looping through worlds");
        for (Map.Entry<String, SessionHolder> entry : worldGameId.entrySet()) {
            String key = entry.getKey();
            Log.d(LOG_TAG, "  *  " + key + "=" + entry.getValue().gameId + " (" + entry.getValue().expirationTime.getTime() + ")");
        }
        Log.d(LOG_TAG, "expires: " + expires());
    }

    public void cleanCache() {
        Log.d(LOG_TAG, "---> cleanCache()");
        logCache();
        worldGameId.clear();
        currentWorld=null;
        logCache();
        Log.d(LOG_TAG, "<--- cleanCache()");
    }

    public void removeCurrentGame() {
        Log.d(LOG_TAG, "removeCurrentGame()   size: " + worldGameId.size());
        worldGameId.remove(currentWorld);
        currentWorld = null;
        Log.d(LOG_TAG, "removeCurrentGame()   size: " + worldGameId.size());
    }

    public String currentId() {
        Log.d(LOG_TAG, "currentId(): " + worldGameId.size());
        Log.d(LOG_TAG, "currentId(): " + currentWorld);
        Log.d(LOG_TAG, "currentId(): " + worldGameId);
        Log.d(LOG_TAG, "currentId(): " + worldGameId.get(currentWorld));
        if (worldGameId==null || currentWorld==null || worldGameId.get(currentWorld)==null) {
            return null;
        }
        return worldGameId.get(currentWorld).gameId;
    }

    public String id(String world) {
        if (world==null || worldGameId == null || worldGameId.get(world)==null) {
            return null;
        }
        return worldGameId.get(world).gameId;
    }

    public String subTitle(String world) {
        for (EngineVolley.GameInfo gi : games) {
            if (gi.title.equals(world)) {
                return gi.subTitle;
            }
        }
        return null;
    }

    public String subTitle() {
        return subTitle(currentWorld);
    }

    public Calendar expires(String world) {
        if (world==null || worldGameId.get(world)==null) {
            return null;
        }
        return worldGameId.get(world).expirationTime;
    }

    public String expiresString() {
        if (currentWorld==null) {
            return null;
        }
        return format.format(worldGameId.get(currentWorld).expirationTime.getTime());
    }

    public String expiresString(String world) {
        if (currentWorld==null) {
            return null;
        }
        return format.format(worldGameId.get(world).expirationTime.getTime());
    }

    public Calendar expires() {
        if (currentWorld==null || worldGameId.get(currentWorld)==null ) {
            return null;
        }
        return worldGameId.get(currentWorld).expirationTime;
    }

    public void saveId(String world, String gameId, long millis) {
        logCache();
        worldGameId.put(world, new SessionHolder(gameId, millis));
        currentWorld = world;
        Log.d(LOG_TAG, "saveId(" + world + "," + gameId+ ") stored: " + worldGameId.get(world).gameId + " double check: " + currentId());
        logCache();
    }

    public void games(List<EngineVolley.GameInfo> games) {
        this.games = games;
    }

    public List<EngineVolley.GameInfo> games() {
        return this.games;
    }

/*    private static Session instance;

    private Session() {;}

    public void reUse(Session session) {

    }

    public static Session getInstance(Session s) {
        if (instance==null) {
            instance = new Session();
        }
        return instance;
    }
*/

}
