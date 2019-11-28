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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import se.juneday.Session;

public class WorldChoiceActivity extends AppCompatActivity {

    private static final String LOG_TAG = WorldChoiceActivity.class.getSimpleName();
    private List<EngineVolley.GameInfo> games;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_choice);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

     /*   FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.app_name) + " - " + getResources().getString(R.string.world_act_name) );

    }


    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart()  ");
        Log.d(LOG_TAG, "session object: " + Session.getInstance());
        Session session = Session.getInstance();
        if (session==null) {
            Log.d(LOG_TAG, "onStart()  session is null");
            finish();
        }
        Log.d(LOG_TAG, "onStart()  session is: " + session);

        this.games = Objects.requireNonNull(session).games();
        if (session.games() != null) {
            Log.d(LOG_TAG, "onStart()  add items");
            for (EngineVolley.GameInfo gi : session.games()) {
                Log.d(LOG_TAG, "onStart()  add items  -  " + gi.title);
            }
        }
        Log.d(LOG_TAG, "");
        fillListView(session.games());
    }


    private void fillListView(final List<EngineVolley.GameInfo> items) {

        ListView listView = findViewById(R.id.world_list);

        TextView titleView = findViewById(R.id.word_list_title);
        titleView.setText(Html.fromHtml("<b>" + getResources().getString(R.string.world_list_title) + "</b>"));

        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, MainActivity.textSize(this));

        // Create Adapter
/*        adapter = new ArrayAdapter<String>(this,
                R.layout.world_list_item,
                (List)items) {
            public String getItem(int position)
            {
                EngineVolley.GameInfo  gi = items.get(position);
                String s = "<b>BOLDIE</b><br><i>ksjdlajksdlkj</i>";
                return String.valueOf(Html.fromHtml(s));
            }
        };;  // Ugly hack :(
*/
        WorldListAdapter adapter = new WorldListAdapter(this,
                items);

        // Set listView's adapter to the new adapter
        listView.setAdapter(adapter);

        //   listView.addHeaderView(titleView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "onItemClick() " + games.get(position).title);
                Intent intent = new Intent(WorldChoiceActivity.this, MainActivity.class);
                intent.putExtra("url", games.get(position).url);
                intent.putExtra("title", games.get(position).title);
                intent.putExtra("subtitle", games.get(position).subTitle);
                startActivity(intent);
            }
        });

    }

    class WorldListAdapter extends ArrayAdapter<EngineVolley.GameInfo> {

        private final Activity context;
        private final List<EngineVolley.GameInfo> items;

        WorldListAdapter(Activity context, List<EngineVolley.GameInfo> items) {
            super(context, R.layout.world_list_item, items);
            this.context = context;
            this.items = items;
        }

        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater=context.getLayoutInflater();

            // TODO: use ViewHolder pattern
            View rowView=inflater.inflate(R.layout.world_list_item, null,true);
            TextView tv = rowView.findViewById(R.id.world_item);

            EngineVolley.GameInfo gi = items.get(position);
            tv.setText(Html.fromHtml("<b>" +  gi.title + "</b><br><i>" + gi.subTitle + "</i>"));

            return rowView;

        }

    }

}
