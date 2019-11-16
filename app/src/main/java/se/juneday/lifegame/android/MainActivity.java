package se.juneday.lifegame.android;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.juneday.ObjectCache;
import se.juneday.Session;
import se.juneday.android.AndroidObjectCacheHelper;
import se.juneday.lifegame.domain.Situation;
import se.juneday.lifegame.domain.Suggestion;
import se.juneday.lifegame.domain.ThingAction;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ArrayAdapter<String> adapter;
    private String worldTitle;
    private List<EngineVolley.GameInfo> games;

    private ObjectCache<Session> cache;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_port);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
/*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
 */
        //       Log.d(LOG_TAG, " init views");
//        initViews();

    }

    public void onStart() {
        super.onStart();
//        cache = new ObjectCache<>(Session.class);
        String fileName =
                null;
        try {
            fileName = AndroidObjectCacheHelper.objectCacheFileName(this, Session.class);
            Log.d(LOG_TAG, "HESA onStart()  cache file: " + fileName);
            cache = new ObjectCache<>(fileName);
            Log.d(LOG_TAG, "HESA onStart()  cache     : " + cache);
            session = cache.readObject();
            Log.d(LOG_TAG, "HESA onStart()  session   : " + session);
        } catch (AndroidObjectCacheHelper.AndroidObjectCacheHelperException e) {
            e.printStackTrace();
        }
        if (session != null) {
            Log.d(LOG_TAG, "HESA onStart()  cache gameId: " + session.currentId());
        } else {
            Log.d(LOG_TAG, "HESA onStart()  creating session object");
            session = new Session();
        }
        registerListener();
        // TODO: do this via menu
        getGames();
        if (session.currentId() != null) {
            currentSituation();
        }
        Log.d(LOG_TAG, "onStart()");
    }

    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "HESA onResume()  gameId: " + session.currentId());
    }

    private void registerListener() {
        Log.d(LOG_TAG, " register listener");
        EngineVolley.getInstance(this).setSituationChangeListener(new EngineVolley.SituationChangeListener() {
            @Override
            public void onSituationChangeList(Situation situation) {
                Log.d(LOG_TAG, "new json: " + situation);
                if (situation != null) {
                    setTextFields(situation.title(), situation.description());
                    // Very type safe ;)
                    fillListView(R.id.suggestions_view, R.id.suggestions_title_view, situation.question(), (List<String>) (List) situation.suggestions());
//                fillListView(R.id.my_things_list, R.id.my_things_title_view, "Saker", (List<String>)(List)situation.things());


                    Log.d(LOG_TAG, "HESA resgisterListener()  save:  " + situation.gameTitle() + " | " + situation.gameId());
                    session.saveId(situation.gameTitle(), situation.gameId());
                    Log.d(LOG_TAG, "HESA resgisterListener()  saved: " + session.currentWorld + " | " + session.currentId());

                    Log.d(LOG_TAG, "HESA resgisterListener()  cache     : " + cache);
                    Log.d(LOG_TAG, "HESA resgisterListener()  session   : " + session);
                    cache.storeObject(session);
                    String explanation = situation.explanation();
                    if (explanation != null && !explanation.equals("")) {
                        showExplanation(explanation);
                    }
                    worldTitle = situation.gameTitle();
                    updateToolbarTitle();
                    fillActionView((List<String>) (List) situation.actions());
                    fillThingView(((List<String>) (List) situation.things()));
                    fillSuggestionView(situation.question(), (List<String>) (List) situation.suggestions());
                } else {
                    showExplanation("Failed to retrieve data from server.");
                }
            }

            @Override
            public void onVictory(String message) {
                showExplanation(message);
            }

            @Override
            public void onError(String message) {
                Log.d(LOG_TAG, " *** ERROR *** " + message);

                if (message.equals("Game id no longer valid")) {
                    showExplanation(message + " for some reason. Start a new game via the menu.");
                    initViews();
                }

            }

            @Override
            public void onGamesChange(List<EngineVolley.GameInfo> games) {
                Log.d(LOG_TAG, "new game list: " + games);
                MainActivity.this.games = games;
            }
        });
    }

    private void getGames() {
        Log.d(LOG_TAG, "getGames()");
        EngineVolley.getInstance(this).getGames();
    }

    private void currentSituation() {
        Log.d(LOG_TAG, "currentSituation()");
        getSituation(session.currentId());
    }

    private void getSituation(String id) {
        Log.d(LOG_TAG, "currentSituation(" + id + ")");
        EngineVolley.getInstance(this).currentSituation(id);
    }

    private void exitGame() {
        EngineVolley.getInstance(this).exitGame(session.currentId());
    }

    private void exitGame(String gameId) {
        EngineVolley.getInstance(this).exitGame(gameId);
    }

    private void startNewGame(String world) {
        Log.d(LOG_TAG, "startNewGame(" + world + ")");
        if (session.id(world) != null) {
            Log.d(LOG_TAG, "startNewGame(" + world + ")  currentSituation()");
            currentSituation();
        } else {
            Log.d(LOG_TAG, "startNewGame(" + world + ")  getGame()");
            EngineVolley.getInstance(this).getGame(world);
        }
    }

    private void updateToolbarTitle() {
        if (worldTitle != null) {
            Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(mActionBarToolbar);
            getSupportActionBar().setTitle(getResources().getString(R.string.app_name) + " - " + worldTitle);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(LOG_TAG, "onConfigurationChanged()");
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }


    private void setTextFields(String title, String description) {
        setText(R.id.title_view, title);
        setText(R.id.description_view, description);
    }

    @Override
    public void onBackPressed() {
        /*    if (!shouldAllowBack()) {
            doSomething();
        } else {
            super.onBackPressed();
        }
        */
        Log.d(LOG_TAG, "onBackPressed()");
    }

    private void initViews() {
        // Title
        setText(R.id.world_view, "");
        setText(R.id.title_view, "");
        setText(R.id.description_view, "");

        fillListView(R.id.my_things_list, R.id.my_things_title_view, "", new ArrayList<>());
        fillListView(R.id.room_things_list, R.id.room_things_title_view, "", new ArrayList<>());
        fillListView(R.id.suggestions_view, R.id.suggestions_title_view, "", new ArrayList<>());
    }

    private void setText(int id, String text) {
        TextView tv = findViewById(id);
        tv.setText(text);
    }

    private void fillActionView(List<String> items) {
        ListView listView = fillListView(R.id.room_things_list, R.id.room_things_title_view, getString(R.string.room_tings_title), items);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG, "item clicked, pos:" + i + " id: " + l);
                Log.d(LOG_TAG, "item clicked: " + items);
                ThingAction action = (ThingAction) (Object) items.get(i);
                Log.d(LOG_TAG, "item clicked: " + action.thing());
                EngineVolley.getInstance(MainActivity.this).takeThing(session.currentId(), action.thing());

            }
        });
    }

    private void fillThingView(List<String> items) {
        ListView listView = fillListView(R.id.my_things_list, R.id.my_things_title_view, getString(R.string.your_things_title), items);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG, "item clicked, pos:" + i + " id: " + l);
                Log.d(LOG_TAG, "item clicked: " + items);
                ThingAction action = (ThingAction) (Object) items.get(i);
                Log.d(LOG_TAG, "item clicked: " + action.thing());
                EngineVolley.getInstance(MainActivity.this).dropThing(session.currentId(), action.thing());

            }
        });
    }

    private void fillSuggestionView(String question, List<String> items) {
        ListView listView = fillListView(R.id.suggestions_view, R.id.suggestions_title_view, question, items);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG, "item clicked, pos:" + i + " id: " + l);
                Log.d(LOG_TAG, "item clicked: " + items);
                Suggestion suggestion = (Suggestion) (Object) items.get(i);
                Log.d(LOG_TAG, "item clicked: " + suggestion.phrase());
                EngineVolley.getInstance(MainActivity.this).getSituation(session.currentId(), suggestion.phrase());

            }
        });
    }

    private void showExplanation(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
//        Snackbar mySnackbar = Snackbar.make(this, message, LENGTH_LONG);
    }

    private int textSize() {
        int dp = (int) (getResources().getDimension(R.dimen.app_text_size) / getResources().getDisplayMetrics().density);
        return dp;
    }

    private ListView fillListView(int id, int titleId, String title, List<String> items) {
        Log.d(LOG_TAG, "fillListView " + title + " : " + items);

        ListView listView = findViewById(id);

        TextView titleView = findViewById(titleId);
        titleView.setText(title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize());

        // Create Adapter
        adapter = new ArrayAdapter<String>(this,
                R.layout.list_item/*android.R.layout.simple_list_item_1*/,
                items);

        // Set listView's adapter to the new adapter
        listView.setAdapter(adapter);

        //   listView.addHeaderView(titleView);


        return listView;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onPrepareOptionsMenu()");
        Log.d(LOG_TAG, "onPrepareOptionsMenu()   games: " + games);
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Menu subMenu = menu.findItem(R.id.action_games).getSubMenu();
        if (games != null) {
            Log.d(LOG_TAG, "onPrepareOptionsMenu()  add items");
            for (EngineVolley.GameInfo gi : games) {
                Log.d(LOG_TAG, "onPrepareOptionsMenu()  add items  -  " + gi.title);
                subMenu.add(gi.title);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void logWorldIds(String tag) {
        Log.d(LOG_TAG, tag + " looping through worlds" );
        session.worldIds().forEach((k, v) -> {
            Log.d(LOG_TAG, tag + "  *  " + k + "=" + v);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected()   -------------------------------- ");
        Log.d(LOG_TAG, "onOptionsItemSelected()   item: " + item);
        logWorldIds("onOptionsItemSelected() world id: ");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String menuTitle = item.toString();


        if (menuTitle.equals(getResources().getString(R.string.action_refresh))) {
            getGames();
        } else if (games != null) {
            Log.d(LOG_TAG, "onOptionsItemSelected()  loop items, check for: " + item);
            for (EngineVolley.GameInfo gi : games) {
                Log.d(LOG_TAG, "  item: " + gi.title + " (" + gi.url + ")");
                if (menuTitle.equals(gi.title)) {
                    Log.d(LOG_TAG, "onOptionsItemSelected() start new game from " + gi.url);
                    if (session.id(gi.title)!=null) {
                        Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played");
                        // game already being played
                        if (session.currentWorld.equals(gi.title)) {
                            Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | same game => restart");
                            // current game is the requested, assuming use wants a new start
                            showExplanation("You're already playing " + gi.title + ". Starting it from beginning.");
                            // exit existing game
                            exitGame();
                            // start new game
                            startNewGame(gi.url);
                        } else {
                            Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game");
                            // wanting to play another world
                            if (session.id(gi.title) != null) {
                                Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | old id => keep playing");
                                // existing game id, keep playing
                                showExplanation("Continuing previous instance of " + gi.title + ".");
                                getSituation(session.id(gi.title));
                            } else {
                                Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | no id => start new");
                                // no existing id - start a new game
                                showExplanation("No stored game for " + gi.title + ". Starting a new game.");
                                // start new game
                                startNewGame(gi.url);
                            }
                        }


                    } else {
                        Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | Never played game");
                        showExplanation("Playing " + gi.title + "for the first time.");
                        startNewGame(gi.url);
                    }
                    break;
                }
            }
            Log.d(LOG_TAG, "onOptionsItemSelected()   -------------------------------- ");
        }

        //noinspection SimplifiableIfStatement
/*        if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }
}
