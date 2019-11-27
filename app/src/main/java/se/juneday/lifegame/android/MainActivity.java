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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
import java.util.function.BiConsumer;

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

    // TODO: remove the var below
   // private List<EngineVolley.GameInfo> games;

    private ObjectCache<Session> cache;
   // private Session session;
    private Situation lastSituation;
    private boolean bundleSupplied;
    private WinInformationHolder winHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EngineVolley.GameInfo gi = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            gi = new EngineVolley.GameInfo(
                    extras.getString("title"),
                    extras.getString("subTitle"),
                    extras.getString("url"));

            Log.d(LOG_TAG, "bundle: " + gi);
            bundleSupplied = true;
        } else {
            bundleSupplied = false;
        }


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


        setupWorld(gi);
    }

    private void setupWorld(EngineVolley.GameInfo giFromBundle) {
//        cache = new ObjectCache<>(Session.class);
        String fileName = null;

        // read Session object from cache
        try {
            fileName = AndroidObjectCacheHelper.objectCacheFileName(this, Session.class);
            Log.d(LOG_TAG, "HESA onStart()  cache file: " + fileName);
            cache = new ObjectCache<>(fileName);
            Log.d(LOG_TAG, "HESA onStart()  cache     : " + cache);
            Session.instance(cache.readObject());
            Log.d(LOG_TAG, "HESA onStart()  session   : " + Session.getInstance());
        } catch (AndroidObjectCacheHelper.AndroidObjectCacheHelperException e) {
            e.printStackTrace();
        }

        if (Session.getInstance() != null) {
            Log.d(LOG_TAG, "HESA onStart()  cache gameId: " + Session.getInstance().currentId());
        } else {
            Log.d(LOG_TAG, "HESA onStart()  creating session object");
            Session.instance(new Session());
        }
        Log.d(LOG_TAG, "session object: " + Session.getInstance());
        registerListener();

        if (giFromBundle==null) {
            getGames();
            if (Session.getInstance().currentId() != null) {
                currentSituation();
            } else {
                showNoGame();
            }
        } else {
            handleUserWorldChoice(giFromBundle);
        }
        Log.d(LOG_TAG, "onStart()");
    }

    private void handleUserWorldChoice(EngineVolley.GameInfo gi) {
        Log.d(LOG_TAG, "onOptionsItemSelected() current game  " + Session.getInstance().currentWorld);
        Log.d(LOG_TAG, "onOptionsItemSelected() wanted game   " + gi.title);
        Log.d(LOG_TAG, "onOptionsItemSelected() start new game from " + gi.url);
        if (Session.getInstance().id(gi.title) != null) {
            Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played");
            // game already being played
            if (Session.getInstance() == null
                    || (Session.getInstance().currentWorld==null)) {
                Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " current bailed out");
                showNoGame();
            }
            Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " continue check");
            if (Session.getInstance().currentWorld!=null && Session.getInstance().currentWorld.equals(gi.title)) {
                Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | same game => restart");
                // current game is the requested, assuming use wants a new start - still, ask a question
                exitGameQuestion(gi.url);
            } else {
                Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game");
                // wanting to play another world
                if (Session.getInstance().id(gi.title) != null) {
                    Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | old id => keep playing");
                    // existing game id, keep playing
                    showExplanation(getResources().getString(R.string.continue_prev_situation) + gi.title + ".");
                    getSituation(Session.getInstance().id(gi.title));

                } else {
                    Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | no id => start new");
                    // no existing id - start a new game
                    showExplanation(getResources().getString(R.string.no_stored_game) + gi.title + "." + getResources().getString(R.string.starting_a_new_game));
                    // start new game
                    startNewGame(gi.url);
                }
            }
        } else {
            Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | Never played game");
            showExplanation("Playing " + gi.title + " from scratch.");
            startNewGame(gi.url);
        }

    }

    public void onStart() {
        super.onStart();
    }

    public void onPause() {
        super.onPause();
        cache.storeObject(Session.getInstance());
    }

    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "HESA onResume()  gameId: " + Session.getInstance().currentId());
        if (! bundleSupplied) {
            currentSituation();
        }
    }

    private void registerListener() {
        Log.d(LOG_TAG, " register listener");
        EngineVolley.getInstance(this).setSituationChangeListener(new EngineVolley.SituationChangeListener() {
            @Override
            public void onSituationChangeList(Situation situation) {
                winHolder = null;
                Log.d(LOG_TAG, "new json: " + situation);
                Log.d(LOG_TAG, "new situation: " + situation.gameTitle() + "  " + situation.title());
                if (situation != null) {
                    setTextFields(situation.title(), situation.description());
                    // Very type safe ;)
                    fillListView(R.id.suggestions_view, R.id.suggestions_title_view, situation.question(), (List<String>) (List) situation.suggestions());
//                fillListView(R.id.my_things_list, R.id.my_things_title_view, "Saker", (List<String>)(List)situation.things());


                    Log.d(LOG_TAG, "HESA resgisterListener()  save:  " + situation.gameTitle() + " | " + situation.gameId());
                    Session.getInstance().saveId(situation.gameTitle(), situation.gameId(), situation.millisLeft());
                    Log.d(LOG_TAG, "HESA resgisterListener()  saved: " + Session.getInstance().currentWorld + " | " + Session.getInstance().currentId());

                    Log.d(LOG_TAG, "HESA resgisterListener()  cache     : " + cache);
                    Log.d(LOG_TAG, "HESA resgisterListener()  session   : " + Session.getInstance());
                    cache.storeObject(Session.getInstance());
                    String explanation = situation.explanation();
                    if (explanation != null && !explanation.equals("")) {
                        showExplanation(explanation);
                    }
                    worldTitle = situation.gameTitle();

                    updateToolbarTitle();
                    fillActionView((List<String>) (List) situation.actions());
                    fillThingView(((List<String>) (List) situation.things()));
                    fillSuggestionView(situation.question(), (List<String>) (List) situation.suggestions());
                    lastSituation = situation;
                    Log.d(LOG_TAG, "HESA resgisterListener() (jsonToSi) save:  " + situation.gameTitle() + " | " + situation.score() + " | " + lastSituation.score());
                } else {
                    showExplanation("Failed to retrieve data from server.");
                }
            }

            @Override
            public void onGameExit() {
                showExplanation("Game removed from server.");
                showNoGame();
            }

            @Override
            public void onVictory(String message) {
                showExplanation(message);
                winHolder = new WinInformationHolder();
                winHolder.gameTitle = lastSituation.gameTitle();
                winHolder.gameSubTitle = lastSituation.gameSubTitle();
                winHolder.message = message;
                showWin(message);
                Session.getInstance().removeCurrentGame();
            }

            @Override
            public void onError(String message) {
                Log.d(LOG_TAG, " *** ERROR *** " + message);
                if (message.equals("Game id no longer valid")) {
                    showExplanation(message + " for some reason. Start a new game via the menu.");
                    exitGame();
                    initViews();
                } else {
                    showExplanation(message);
                }

            }

            @Override
            public void onGamesChange(List<EngineVolley.GameInfo> games) {
                Log.d(LOG_TAG, "new game list: " + games);
                Session.getInstance().games(games);
                Log.d(LOG_TAG, "new game list: " + Session.getInstance().games().size() + " worlds");
            }
        });
    }

    private void showNoGame() {
        Log.d(LOG_TAG, "showNoGame()");
        initViews();
        setTextFields(getResources().getString(R.string.no_game_selected),
                "\n\n" + getResources().getString(R.string.refresh_and_choose_game));
    }

    private void showWin(String message) {
        Log.d(LOG_TAG, "showWin()");
        initViews();
        setTextFields(getResources().getString(R.string.congratulations),
                getResources().getString(R.string.managed_to_win) + "\n\n" + message);
    }

    private void getGames() {
        Log.d(LOG_TAG, "getGames()");
        EngineVolley.getInstance(this).getGames();
    }

    private void currentSituation() {
        Log.d(LOG_TAG, "currentSituation()");
        getSituation(Session.getInstance().currentId());
    }

    private void getSituation(String id) {
        Log.d(LOG_TAG, "currentSituation(" + id + ")");
        EngineVolley.getInstance(this).currentSituation(id);
    }

    private void exitGame() {
        EngineVolley.getInstance(this).exitGame(Session.getInstance().currentId());
        Session.getInstance().removeCurrentGame();
        worldTitle = null;
        updateToolbarTitle();
    }

    private void exitGame(String gameId) {
        EngineVolley.getInstance(this).exitGame(gameId);
        worldTitle = null;
        updateToolbarTitle();
    }

    private void startNewGame(String world) {
        Log.d(LOG_TAG, "startNewGame(" + world + ")");
        if (Session.getInstance().id(world) != null) {
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

        fillListView(R.id.my_things_list, R.id.my_things_title_view, "", new ArrayList<String>());
        fillListView(R.id.room_things_list, R.id.room_things_title_view, "", new ArrayList<String>());
        fillListView(R.id.suggestions_view, R.id.suggestions_title_view, "", new ArrayList<String>());

    }

    private void setText(int id, String text) {
        TextView tv = findViewById(id);
//        tv.setText(text);
        tv.setText(Html.fromHtml(text));
        tv.setClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void fillActionView(final List<String> items) {
        ListView listView = fillListView(R.id.room_things_list, R.id.room_things_title_view, getString(R.string.room_tings_title), items);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG, "item clicked, pos:" + i + " id: " + l);
                Log.d(LOG_TAG, "item clicked: " + items);
                ThingAction action = (ThingAction) (Object) items.get(i);
                Log.d(LOG_TAG, "item clicked: " + action.thing());
                EngineVolley.getInstance(MainActivity.this).takeThing(Session.getInstance().currentId(), action.thing());

            }
        });
    }

    private void fillThingView(final List<String> items) {
        ListView listView = fillListView(R.id.my_things_list, R.id.my_things_title_view, getString(R.string.your_things_title), items);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG, "item clicked, pos:" + i + " id: " + l);
                Log.d(LOG_TAG, "item clicked: " + items);
                ThingAction action = (ThingAction) (Object) items.get(i);
                Log.d(LOG_TAG, "item clicked: " + action.thing());
                EngineVolley.getInstance(MainActivity.this).dropThing(Session.getInstance().currentId(), action.thing());

            }
        });
    }

    private void fillSuggestionView(String question, final List<String> items) {
        ListView listView = fillListView(R.id.suggestions_view, R.id.suggestions_title_view, question, items);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG, "item clicked, pos:" + i + " id: " + l);
                Log.d(LOG_TAG, "item clicked: " + items);
                Suggestion suggestion = (Suggestion) (Object) items.get(i);
                Log.d(LOG_TAG, "item clicked: " + suggestion.phrase());
                EngineVolley.getInstance(MainActivity.this).getSituation(Session.getInstance().currentId(), suggestion.phrase());

            }
        });
    }

    private void showExplanation(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
//        Snackbar mySnackbar = Snackbar.make(this, message, LENGTH_LONG);
    }

    public static int textSize(Context c) {
        int dp = (int) (c.getResources().getDimension(R.dimen.app_text_size) / c.getResources().getDisplayMetrics().density);
        return dp;
    }

    private ListView fillListView(int id, int titleId, String title, List<String> items) {
        Log.d(LOG_TAG, "fillListView " + title + " : " + items);

        ListView listView = findViewById(id);

        TextView titleView = findViewById(titleId);
        titleView.setText(title);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize(this));

        // Create Adapter
        adapter = new ArrayAdapter<String>(this,
                R.layout.list_item/*android.R.layout.simple_list_item_1*/,
                items);

        // Set listView's adapter to the new adapter
        listView.setAdapter(adapter);

        //   listView.addHeaderView(titleView);


        return listView;
    }


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }
    */


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

/*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onPrepareOptionsMenu()");
        getMenuInflater().inflate(R.menu.menu_main, menu);

        Log.d(LOG_TAG, "onPrepareOptionsMenu()   games: " + Session.getInstance().games());
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Menu subMenu = menu.findItem(R.id.action_games).getSubMenu();
        if (Session.getInstance().games() != null) {
            Log.d(LOG_TAG, "onPrepareOptionsMenu()  add items");
            for (EngineVolley.GameInfo gi : Session.getInstance().games()) {
                Log.d(LOG_TAG, "onPrepareOptionsMenu()  add items  -  " + gi.title);
                MenuItem mi = subMenu.add(gi.title);
                //if (session.id(gi.title)!=null) {
                // already played (cached one exists). Show cached symbol
                mi.setIcon(R.mipmap.baseline_cached_black_18dp);
                //}
            }
        }
//        return super.onPrepareOptionsMenu(menu);
        return true;
    }

*/


    private void exitGameQuestion(final String gameUrl) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int choice) {
                switch (choice) {
                    case DialogInterface.BUTTON_POSITIVE:
                        showExplanation(getString(R.string.leaving_game) + Session.getInstance().currentWorld);
                        exitGame();
                        if (gameUrl != null) {
                            startNewGame(gameUrl);
                        }
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        currentSituation();
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.quit_game_question))
                .setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(LOG_TAG, "onOptionsItemSelected()   -------------------------------- ");
        Log.d(LOG_TAG, "onOptionsItemSelected()   item: " + item);
        Session.getInstance().logCache();
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        String menuTitle = item.toString();


        if (id==R.id.action_exitgame) {
            // TODO: ask if user wants to quit game
            exitGameQuestion(null);
            return true;
        } else if (id==R.id.action_log) {
            Session.getInstance().logCache();
            return true;
        } else if (id==R.id.action_status) {
            if (winHolder!=null) {
                showExplanation( "You've won " + lastSituation.gameTitle());
                return true;
            }
            Log.d(LOG_TAG, "score: " + lastSituation.score());
            Log.d(LOG_TAG, "count: " + lastSituation.situationCount());
            showExplanation( "World: " + lastSituation.gameTitle() +
                    "\nId: " + lastSituation.gameId() +
                    "\nSituation: " + lastSituation.title() +
                    "\nScore: " + lastSituation.score() +
                    "\nSituations: " + lastSituation.situationCount() +
                    "\nExpires: " + Session.getInstance().expiresString());
            return true;
        } else if (id == R.id.action_web) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            EngineVolley ev = EngineVolley.getInstance(this);
            intent.setData(Uri.parse((ev.webUrl(Session.getInstance().currentId()))));
            startActivity(intent);
            return true;
        } else if (id == R.id.action_copy) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, (EngineVolley.getInstance(this).webUrl(Session.getInstance().currentId())));
            intent.setType("text/plain");
            startActivity(intent);
            return true;
        } else if (id == R.id.action_web_admin) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            EngineVolley ev = EngineVolley.getInstance(this);
            intent.setData(Uri.parse((ev.webAdminUrl(Session.getInstance().currentId()))));
            startActivity(intent);
            return true;
        } else if (id == R.id.action_email) {
            Log.d(LOG_TAG, "email: world     " + Session.getInstance().currentWorld);
            Log.d(LOG_TAG, "email: id        " + Session.getInstance().currentId());
            Log.d(LOG_TAG, "email: game      " + lastSituation.gameTitle());
            Log.d(LOG_TAG, "email: title     " + lastSituation.title());
            String message;
            String subject;
            if (winHolder!=null) {
                subject = "I won " + winHolder.gameTitle + " (a LifeGame)";
                message = "Hi!\nJust wanted to say that I've been playing a LifeGame called \"" +
                        winHolder.gameTitle + " (" + winHolder.gameSubTitle + ")\"\n" +
                        "... and I won!!\n\n" +
                        "Check out all the games at: http://life.juneday.se";
            } else {
                subject = "A weblink to " + Session.getInstance().currentWorld + " (a LifeGame)";
                message = "Hi!\nJust wanted to say that I am playing a LifeGame called \"" +
                        Session.getInstance().currentWorld + "\" (" + Session.getInstance().subTitle() + ")\n" +
                        "\n\nHere's a link: " + EngineVolley.getInstance(this).webUrl(Session.getInstance().currentId()) +
                        "\n\nCheck out all the games at: http://life.juneday.se";
            }

            Intent intent = new Intent(Intent.ACTION_SENDTO,Uri.fromParts(
                    "mailto","", null));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message );
            startActivity(Intent.createChooser(intent, "Send email..."));
            EngineVolley ev = EngineVolley.getInstance(this);
            startActivity(intent);

            Log.d(LOG_TAG, "email: subject" + subject);
            Log.d(LOG_TAG, "email: body" + message);
            return true;

        } else if (id==R.id.action_clean) {
            Session.getInstance().cleanCache();
            currentSituation();
            return true;
        } else if (id==R.id.action_refresh) {
            showExplanation(getResources().getString(R.string.update_game));
            getGames();
            getSituation(Session.getInstance().currentId());}
        else if (id==R.id.action_games) {
           // showExplanation("Games.");
            Intent intent = new Intent(this, WorldChoiceActivity.class);
            startActivity(intent);
        } else if (Session.getInstance().games() != null) {
            Log.d(LOG_TAG, "onOptionsItemSelected()  loop items, check for: " + item);
            for (EngineVolley.GameInfo gi : Session.getInstance().games()) {
                Log.d(LOG_TAG, "onOptionsItemSelected  item: " + gi.title + " (" + gi.url + ")");
                if (menuTitle.equals(gi.title)) {
                    Log.d(LOG_TAG, "onOptionsItemSelected() start new game from " + gi.url);
                    if (Session.getInstance().id(gi.title) != null) {
                        Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played");
                        // game already being played
                        if (Session.getInstance().currentWorld.equals(gi.title)) {
                            Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | same game => restart");
                            // current game is the requested, assuming use wants a new start - still, ask a question
                            exitGameQuestion(gi.url);
                        } else {
                            Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game");
                            // wanting to play another world
                            if (Session.getInstance().id(gi.title) != null) {
                                Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | old id => keep playing");
                                // existing game id, keep playing
                                showExplanation(getResources().getString(R.string.continue_prev_situation) + gi.title + ".");
                                getSituation(Session.getInstance().id(gi.title));
                            } else {
                                Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | no id => start new");
                                // no existing id - start a new game
                                showExplanation(getResources().getString(R.string.no_stored_game) + gi.title + ". Starting a new game.");
                                // start new game
                                startNewGame(gi.url);
                            }
                        }

                    } else {
                        Log.d(LOG_TAG, "onOptionsItemSelected() game " + gi.title + " already being played | new game | Never played game");
                        showExplanation(getResources().getString(R.string.playing_from_scratch_1) + gi.title + getResources().getString(R.string.playing_from_scratch_2));
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


    private class WinInformationHolder {
        public String gameTitle;
        public String gameSubTitle;
        public String message;
    }

}
