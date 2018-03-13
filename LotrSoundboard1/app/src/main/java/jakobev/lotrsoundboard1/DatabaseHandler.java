package jakobev.lotrsoundboard1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Marv & Jutta on 04.11.2017.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String LOG_TAG = "DATABASEHANDLER";
    private static final String DATABASE_NAME = "soundboard.db";
    private static final int DATABASE_VERSION = 2;

    //Tabelle 1 = Alle Sounds
    private static final String MAIN_TABLE = "main_table";
    private static final String MAIN_ID = "_id";
    private static final String MAIN_NAME = "soundName";
    private static final String MAIN_ITEM_ID = "soundID";

    //Tabelle 2 = Favoriten
    private static final String FAVORITES_TABLE = "favorites_table";
    private static final String FAVORITES_ID = "_id";
    private static final String FAVORITES_NAME = "favoName";
    private static final String FAVORITES_ITEM_ID = "favoID";

    private static final String SQL_CREATE_MAIN_TABLE = "CREATE TABLE IF NOT EXISTS " + MAIN_TABLE + "(" + MAIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MAIN_NAME + " TEXT, " + MAIN_ITEM_ID + " INTEGER unique);";
    private static final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS " + FAVORITES_TABLE + "(" + FAVORITES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FAVORITES_NAME + " TEXT, " + FAVORITES_ITEM_ID + " INTEGER);";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {

            db.execSQL(SQL_CREATE_MAIN_TABLE);
            db.execSQL(SQL_CREATE_FAVORITES_TABLE);

        }catch (Exception e){
            Log.e(LOG_TAG, "Fehler beim erstellen der Datenbank"+ e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + MAIN_TABLE);
        onCreate(db);
    }
    private boolean verification(SQLiteDatabase database, String tableName,String idColumn,Integer soundID){
        int count = -1;
        Cursor cursor = null;

        try {

            String query = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = " + soundID;
            cursor = database.rawQuery(query,null);

            if (cursor.moveToFirst())
                count = cursor.getInt(0);

            return (count > 0);

        }finally {

            if (cursor != null)
                cursor.close();
        }
    }

    public void createSoundCollection(Context context){

        List<String> nameList = Arrays.asList(context.getResources().getStringArray(R.array.soundNames));
        SoundObject[] soundItems = {
                new SoundObject(nameList.get(0), R.raw.dale),
                new SoundObject(nameList.get(1), R.raw.erebor),
                new SoundObject(nameList.get(2), R.raw.gondor),
                new SoundObject(nameList.get(3), R.raw.gungabadangmar),
                new SoundObject(nameList.get(4), R.raw.gungabadattack),
                new SoundObject(nameList.get(5), R.raw.hammerhand),
                new SoundObject(nameList.get(6), R.raw.hammerhandisolated),
                new SoundObject(nameList.get(7), R.raw.haradrim),
                new SoundObject(nameList.get(8), R.raw.isengard),
                new SoundObject(nameList.get(9), R.raw.isengardisolated),
                new SoundObject(nameList.get(10), R.raw.lothrorien),
                new SoundObject(nameList.get(11), R.raw.mordorhorn),
                new SoundObject(nameList.get(12), R.raw.rhuneasterlings),
                new SoundObject(nameList.get(13), R.raw.rohan),
                new SoundObject(nameList.get(14), R.raw.woodlandrealm),
                new SoundObject(nameList.get(15), R.raw.youshallnotpass),
        };
        for (SoundObject i: soundItems)
            putIntoMain(i);
    }
    private void putIntoMain(SoundObject soundObject){

        SQLiteDatabase database = this.getWritableDatabase();

        if (!verification(database,MAIN_TABLE,MAIN_ITEM_ID,soundObject.getItemID())){

            try {

                ContentValues contentValues = new ContentValues();
                contentValues.put(MAIN_NAME,soundObject.getItemName());
                contentValues.put(MAIN_ITEM_ID,soundObject.getItemID());

                database.insert(MAIN_TABLE,null,contentValues);

            }catch (Exception e){
                Log.e(LOG_TAG,"(MAIN) Fehler beim Einfügen des Sounds" + e.getMessage());
            }finally {
                database.close();
            }
        }
    }

    public Cursor getSoundCollection(){

        SQLiteDatabase database = this.getReadableDatabase();

        return database.rawQuery("SELECT * FROM " + MAIN_TABLE + " ORDER BY " + MAIN_NAME, null);
    }

    public void addFavorites (SoundObject soundObject){

        SQLiteDatabase database = this.getWritableDatabase();

        if (!verification(database, FAVORITES_TABLE,FAVORITES_ITEM_ID,soundObject.getItemID())){

            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(FAVORITES_NAME,soundObject.getItemName());
                contentValues.put(FAVORITES_ITEM_ID,soundObject.getItemID());

                database.insert(FAVORITES_TABLE,null,contentValues);

            }catch (Exception e){
                Log.e(LOG_TAG,"(FAVORITES) Fehler beim Einfügen des Sounds" + e.getMessage());
            }finally {
                database.close();

            }
        }

    }

    public void removeFavorite(Context context, SoundObject soundObject){

        SQLiteDatabase database = this.getWritableDatabase();

        if (verification(database,FAVORITES_TABLE,FAVORITES_ITEM_ID,soundObject.getItemID())){

            try {

                database.delete(FAVORITES_TABLE,FAVORITES_ITEM_ID + " = " + soundObject.getItemID(),null);

                Activity activity = (Activity) context;
                Intent delete = activity.getIntent();
                activity.overridePendingTransition(0,0);
                delete.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.finish();
                activity.overridePendingTransition(0,0);
                context.startActivity(delete);

            }catch (Exception e){
                Log.e(LOG_TAG,"(FAVORITES) Fehler beim löschen des Sounds" + e.getMessage());
            }finally {
                database.close();
            }
        }
    }

    public Cursor getFavorites(){

        SQLiteDatabase database = this.getReadableDatabase();

        return database.rawQuery("SELECT * FROM " + FAVORITES_TABLE + " ORDER BY " + FAVORITES_NAME , null);
    }

    public void updateFavorites(){

        SQLiteDatabase database = this.getWritableDatabase();

        try {

            Cursor favorite_content = database.rawQuery("SELECT * FROM " + FAVORITES_TABLE, null);

            if (favorite_content.getCount()== 0){

                Log.d(LOG_TAG,"Cursor ist leer");
                favorite_content.close();
            }
            while (favorite_content.moveToNext()){

                String entryName = favorite_content.getString(favorite_content.getColumnIndex(FAVORITES_NAME));

                Cursor updateEntry = database.rawQuery("SELECT * FROM " + MAIN_TABLE + " WHERE " + MAIN_NAME + " = '" + entryName + "'",null );

                if (updateEntry.getCount()==0){

                    Log.d(LOG_TAG,"Cursor ist leer");
                            updateEntry.close();
                }

                updateEntry.moveToFirst();

                if (favorite_content.getInt(favorite_content.getColumnIndex(FAVORITES_ITEM_ID)) != updateEntry.getInt(updateEntry.getColumnIndex(MAIN_ITEM_ID))){

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FAVORITES_ITEM_ID,updateEntry.getInt(updateEntry.getColumnIndex(MAIN_ITEM_ID)));
                    database.update(FAVORITES_TABLE,contentValues,FAVORITES_NAME + " = '" + entryName + "'",null);
                }
            }

        }catch (Exception e){
            Log.e(LOG_TAG,"Fehler beim Aktualiesieren der Favoriten" + e.getMessage());
        }
    }

    public void update (){

        try {

            SQLiteDatabase database = this.getWritableDatabase();

            database.execSQL("DROP TABLE IF EXISTS " + MAIN_TABLE);

            database.execSQL(SQL_CREATE_MAIN_TABLE);

            database.close();

        }catch (Exception e){
            Log.e(LOG_TAG,"Fehler beim Aktualisieren der Main Tabelle" + e.getMessage());
        }
    }
}
