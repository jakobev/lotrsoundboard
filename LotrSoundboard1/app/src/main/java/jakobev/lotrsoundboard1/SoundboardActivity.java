package jakobev.lotrsoundboard1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.audiofx.BassBoost;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

public class SoundboardActivity extends AppCompatActivity {

    private static final String LOG_TAG = "SoundboardActivity";
    ArrayList<SoundObject>soundList = new ArrayList<>();
    Toolbar toolbar;
    RecyclerView SoundView;
    SoundboardRecyclerAdapter SoundAdapter = new SoundboardRecyclerAdapter(soundList);
    RecyclerView.LayoutManager SoundLayoutManager;
    private View    mLayout;
    DatabaseHandler databaseHandler = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundboard);

        if (update()){

            databaseHandler.createSoundCollection(this);

            databaseHandler.updateFavorites();
        }

        mLayout = findViewById(R.id.activity_soundboard);

        toolbar = (Toolbar)findViewById(R.id.soundboard_toolbar);
        setSupportActionBar(toolbar);

        appDataToArrayList();



        SoundView = (RecyclerView)findViewById(R.id.soundboardRecyclerView);

        SoundLayoutManager = new GridLayoutManager(this,3);
        SoundView.setLayoutManager(SoundLayoutManager);
        SoundView.setAdapter(SoundAdapter);
        requestPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.toolbar_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId()==R.id.action_info_show){
            Toast.makeText(getApplicationContext(),"Hold item to show more options",Toast.LENGTH_LONG).show();
        }
        if (item.getItemId()==R.id.action_about_show){
            Intent intent = new Intent(this,AboutActivity.class);
            startActivity(intent);
        }


        if (item.getItemId()==R.id.action_favorite_show)
            this.startActivity(new Intent(this,FavoriteActivity.class));

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy (){
        super.onDestroy();
        EventHandlerClass.releaseMediaPlayer();
    }



    private void appDataToArrayList(){
        soundList.clear();

        Cursor cursor = databaseHandler.getSoundCollection();

        if (cursor.getCount() == 0){
            Log.e(LOG_TAG, "Cursor ist leer oder Daten konnten nicht konvertiert werden");
            cursor.close();
        }
        if (cursor.getCount()!= soundList.size()){

            while (cursor.moveToNext()){

                String NAME = cursor.getString(cursor.getColumnIndex("soundName"));
                Integer ID = cursor.getInt(cursor.getColumnIndex("soundID"));

                soundList.add(new SoundObject(NAME,ID));
                SoundAdapter.notifyDataSetChanged();
            }
            cursor.close();
        }
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
            }
            if (!Settings.System.canWrite(this)){

                Snackbar.make(mLayout,"Die App benötigt Zugriff auf die Einstellungen",Snackbar.LENGTH_INDEFINITE).setAction("Ok", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                        intent.setData(Uri.parse("package:" + v.getContext().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).show();
            }
        }


    }

    private boolean update(){

        final String PREFS_NAME = "VersionPref";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        int currentVersionCode = 0;
        try {

            currentVersionCode = getPackageManager().getPackageInfo(getPackageName(),0).versionCode;

        }catch (PackageManager.NameNotFoundException e){
            Log.e(LOG_TAG, e.getMessage());
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY,DOESNT_EXIST);

        SharedPreferences.Editor editor = prefs.edit();

        if (savedVersionCode == DOESNT_EXIST){

            databaseHandler.update();
            editor.putInt(PREF_VERSION_CODE_KEY,currentVersionCode);
            editor.commit();
            return true;
        }
        else if (currentVersionCode > savedVersionCode){

            databaseHandler.update();
            editor.putInt(PREF_VERSION_CODE_KEY,currentVersionCode);
            editor.commit();
            return true;
        }

        return false;
    }
}
