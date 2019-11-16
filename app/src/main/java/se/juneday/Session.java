package se.juneday;

import android.util.Log;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, String> worldGameId;

//    public String gameId;
    public String currentWorld;

    public Session() {
        worldGameId = new HashMap<>();
    }

    public Map<String,String> worldIds() {
        return Collections.unmodifiableMap(worldGameId);
    }

    public String currentId() {
        return worldGameId.get(currentWorld);
    }

    public String id(String world) {
        return worldGameId.get(world);
    }

    public void saveId(String world, String gameId) {
        worldGameId.put(world, gameId);
        currentWorld = world;
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
