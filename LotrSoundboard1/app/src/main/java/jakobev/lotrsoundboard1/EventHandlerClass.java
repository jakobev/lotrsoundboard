package jakobev.lotrsoundboard1;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Marv & Jutta on 01.11.2017.
 */

public class EventHandlerClass {

    private static final String LOG_TAG = "EVENTHANDLER";

    private static MediaPlayer mp;

    private static DatabaseHandler databaseHandler ;

    public static void startMediaPlayer(View view, Integer soundID){

        try{

            if (soundID != null) {
                if (mp != null) {
                    mp.reset();
                }
                mp = MediaPlayer.create(view.getContext(), soundID);
                mp.start();
            }

        }catch (Exception e){

            Log.e(LOG_TAG,"Fehler beim Abspielen" + e.getMessage());
        }
    }

    public static void releaseMediaPlayer(){
        if (mp != null){
            mp.release();
            mp = null;
        }
    }

    public static void popupManager(final View view,final SoundObject soundObject){

        databaseHandler = new DatabaseHandler(view.getContext());

        PopupMenu popup = new PopupMenu(view.getContext(),view);

        if (view.getContext() instanceof FavoriteActivity)
            popup.getMenuInflater().inflate(R.menu.favo_longclick,popup.getMenu());
        else
            popup.getMenuInflater().inflate(R.menu.longclick,popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId()==R.id.action_send || item.getItemId()==R.id.action_ringtone){

                    final String fileName = soundObject.getItemName()+ ".mp3";

                    File storage = Environment.getExternalStorageDirectory();
                    File directory = new File(storage.getAbsolutePath()+"/Lotr und Hobbit Soundboard/");
                    directory.mkdirs();
                    final File file = new File(directory,fileName);

                    InputStream input = view.getContext().getResources().openRawResource(soundObject.getItemID());

                    try {

                        OutputStream output = new FileOutputStream(file);
                        byte[]buffer = new byte[1024];
                        int len;
                        while ((len=input.read(buffer,0,buffer.length))!= -1){
                            output.write(buffer,0,len);
                        }
                        input.close();
                        output.close();

                    }catch (IOException e){
                        Log.e(LOG_TAG,"Fehler beim abspeichern von:"+ e.getMessage());
                    }

                    if (item.getItemId()== R.id.action_ringtone){
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), android.app.AlertDialog.THEME_HOLO_LIGHT);
                        builder.setTitle("Speichern als..");
                        builder.setItems(new CharSequence[]{"Klingelton", "Benachrichtigungston", "Alarmton"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int action) {

                                switch (action){

                                    case 0:
                                        changeSystemAudio(view,fileName,file,1);
                                        break;
                                    case 1:
                                        changeSystemAudio(view,fileName,file,2);
                                        break;
                                    case 2:
                                        changeSystemAudio(view,fileName,file,3);
                                        break;
                                }

                            }
                        });
                        builder.create();
                        builder.show();
                    }

                    if (item.getItemId()== R.id.action_send){

                        try {

                            final Intent send = new Intent (Intent.ACTION_SEND);
                            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().toString()+"/Lotr und Hobbit Soundboard/" + fileName);
                            send.putExtra(Intent.EXTRA_STREAM,uri);
                            send.setType("audio/mp3");

                            view.getContext().startActivity(Intent.createChooser(send,"Teilen über.."));

                        }catch (Exception e){
                            Log.e(LOG_TAG,"Fehler beim Teilen von" + e.getMessage());
                        }

                    }

                }
                if (item.getItemId()== R.id.action_favorite){

                    if (view.getContext() instanceof FavoriteActivity)
                        databaseHandler.removeFavorite(view.getContext(), soundObject);
                    else
                        databaseHandler.addFavorites(soundObject);
                }

                return true;
            }
        });

        popup.show();
    }

    public static void changeSystemAudio(View view, String fileName,File file, int action){

        try {

            ContentValues values = new ContentValues();

            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            values.put(MediaStore.MediaColumns.TITLE, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");

            switch (action){
                case 1:
                    values.put(MediaStore.Audio.Media.IS_RINGTONE,true);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION,false);
                    values.put(MediaStore.Audio.Media.IS_ALARM,false);
                    break;
                case 2:
                    values.put(MediaStore.Audio.Media.IS_RINGTONE,false);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION,true);
                    values.put(MediaStore.Audio.Media.IS_ALARM,false);
                    break;
                case 3:
                    values.put(MediaStore.Audio.Media.IS_RINGTONE,false);
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION,false);
                    values.put(MediaStore.Audio.Media.IS_ALARM,true);
                    break;

            }

            values.put(MediaStore.Audio.Media.IS_MUSIC,false);

            Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
            view.getContext().getContentResolver().delete(uri,MediaStore.MediaColumns.DATA + "=\"" +file.getAbsolutePath() +"\"",null);
            Uri finalUri = view.getContext().getContentResolver().insert(uri,values);

            switch (action){

                case 1:
                    RingtoneManager.setActualDefaultRingtoneUri(view.getContext(),RingtoneManager.TYPE_RINGTONE,finalUri);
                    break;
                case 2:
                    RingtoneManager.setActualDefaultRingtoneUri(view.getContext(),RingtoneManager.TYPE_NOTIFICATION,finalUri);
                    break;
                case 3:
                    RingtoneManager.setActualDefaultRingtoneUri(view.getContext(),RingtoneManager.TYPE_ALARM,finalUri);
                    break;
            }

        }catch (Exception e){
            Log.e(LOG_TAG,"Fehler beim Speichern als Systemton"+ e.getMessage());
        }

    }

}
