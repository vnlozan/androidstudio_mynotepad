package com.example.vladok.mynotepad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class MainActivity extends ActionBarActivity {

    // OBJECTS+VALUES
    final String LOG_TAG = "myLogs";
    private final static String FILENAME = "hello.txt";
    private EditText mEditText;//TEXTVIEW
    final String[] chooseDevice = { "External", "Internal" };
    //DIALOG BOXES
    AlertDialog.Builder builder;
    AlertDialog.Builder builder1;
    AlertDialog.Builder builder2;
    AlertDialog.Builder builder3;
    //ID
    private final int DEVICE = 1;
    private final int SAVE_INTERNAL=2;
    private final int SAVE_EXTERNAL=3;
    private final int OPEN_FILE=4;
    private final int READ_EXTERNAL=5;
    private final int READ_INTERNAL=6;
    private static String myFileName="";
    String[] arrayOfFiles;
    String[] fileArray;
    final String DIR_SD = "MyFiles";//�the name of the folder
    int i=0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mEditText = (EditText) findViewById(R.id.editText);
    }
    //MENU ITEMS
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //MAIN INTERFACE
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open:
                showDialog(OPEN_FILE);
                return true;
            case R.id.action_save:
                showDialog(DEVICE);
                return true;
            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return true;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
    // читаем размер шрифта из EditTextPreference
        float fSize = Float.parseFloat(prefs.getString(
                getString(R.string.pref_size), "20"));
    // применяем настройки в текстовом поле
        mEditText.setTextSize(fSize);
        // читаем стили текста из ListPreference
        String regular = prefs.getString(getString(R.string.pref_style), "");
        int typeface = Typeface.NORMAL;

        if (regular.contains("Полужирный"))
            typeface += Typeface.BOLD;

        if (regular.contains("Курсив"))
            typeface += Typeface.ITALIC;

    // меняем настройки в EditText
        mEditText.setTypeface(null, typeface);
    }
    //FUNCTIONS READ/WRITE
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DEVICE://1
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.path1).setCancelable(false)
                .setNeutralButton(R.string.cancel1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setItems(chooseDevice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        Toast.makeText(getApplicationContext(), chooseDevice[item], Toast.LENGTH_SHORT).show();
                        if (item == 0)
                            showDialog(SAVE_EXTERNAL); // 0
                        else if (item == 1)
                            showDialog(SAVE_INTERNAL); // 1
                    }
                });
                return builder.create();
            case SAVE_INTERNAL:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle(R.string.title);
                // Set up the input
                final EditText input1 = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                builder1.setView(input1);
                // Set up the buttons
                builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myFileName = input1.getText().toString();
                        try {
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                                    openFileOutput(myFileName+".txt", MODE_WORLD_READABLE )));
                            bw.write(mEditText.getText().toString());
                            bw.close();
                            Log.d(LOG_TAG, "FILE SAVED");
                            Toast.makeText(getApplicationContext(), R.string.fileSaved, Toast.LENGTH_SHORT).show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                return builder1.show();

            case SAVE_EXTERNAL:
                Log.d(LOG_TAG, "SAVE_EXTERNAL");
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("Title");
                // Set up the input
                final EditText input2 = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                builder2.setView(input2);
                // Set up the buttons
                builder2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myFileName = input2.getText().toString();
                        if (!Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            Log.d(LOG_TAG, R.string.noacces + Environment.getExternalStorageState());
                            return;
                        }
                        File sdPath = Environment.getExternalStorageDirectory();
                        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
                        sdPath.mkdirs();
                        File sdFile = new File(sdPath, myFileName);
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
                            bw.write(mEditText.getText().toString());
                            bw.close();
                            Log.d(LOG_TAG, "FILE SAVED IN " + sdFile.getAbsolutePath());
                            Toast.makeText(getApplicationContext(), R.string.fileSaved, Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                Log.d(LOG_TAG, "SAVE_EXTERNAL ENDING");
                builder2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                return builder2.show();
            case OPEN_FILE:
                builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.path2).setCancelable(false)
                        .setNeutralButton(R.string.cancel1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setItems(chooseDevice, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                Toast.makeText(getApplicationContext(), chooseDevice[item], Toast.LENGTH_SHORT).show();
                                if (item == 0)
                                    showDialog(READ_EXTERNAL); // 5
                                else if (item == 1)
                                    showDialog(READ_INTERNAL); // 6
                            }
                        });
                return builder.show();
            case READ_EXTERNAL:
                Log.d(LOG_TAG, "READ EXTERNAL");
                File sdPath = Environment.getExternalStorageDirectory();
                sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
                File[] listOfFiles = sdPath.listFiles();
                arrayOfFiles=new String[listOfFiles.length];
                Log.d(LOG_TAG, "THERE ARE "+listOfFiles.length+"FILES");
                i=0;
                for (File file : listOfFiles)
                    if (file.isFile())
                        arrayOfFiles[i++]=file.getName()+".txt";
                builder1 = new AlertDialog.Builder(this);
                builder1.setTitle(R.string.path3).setCancelable(false)
                        .setNeutralButton(R.string.cancel1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setItems(arrayOfFiles, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                Toast.makeText(getApplicationContext(), arrayOfFiles[item], Toast.LENGTH_SHORT).show();
                                StringBuffer output=new StringBuffer(arrayOfFiles[item].length());
                                output.append(arrayOfFiles[item]);
                                output.delete(output.length()-4,output.length());
                                openFileSD(output.toString());
                            }
                        });
                Log.d(LOG_TAG, "READ_EXTERNAL ENDING");
                return builder1.show();

            case READ_INTERNAL:
                File pathInternfile = new File(String.valueOf(getFilesDir()));
                File[] FileList = pathInternfile.listFiles();
                fileArray=new String[FileList.length];
                Log.d(LOG_TAG, "THERE ARE "+FileList.length+"FILES");
                i=0;
                for (File file : FileList)
                    if (file.isFile())
                        fileArray[i++]=file.getName();
                builder1 = new AlertDialog.Builder(this);
                builder1.setTitle(R.string.path3).setCancelable(false)
                        .setNeutralButton(R.string.cancel1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setItems(fileArray, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                Toast.makeText(getApplicationContext(), fileArray[item], Toast.LENGTH_SHORT).show();
                                openFileIN(fileArray[item]);
                            }
                        });
                Log.d(LOG_TAG, "READ_EXTERNAL ENDING");
                return builder1.show();
            default:
                return null;
        }
    }
    private void openFileIN(String fileName){
        try {
            InputStream inputStream = openFileInput(fileName);

            if (inputStream != null) {
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isr);
                String line;
                StringBuilder builder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    builder.append(line + "\n");
                }

                inputStream.close();
                mEditText.setText(builder.toString());
            }
        } catch (Throwable t) {
            Toast.makeText(getApplicationContext(),
                    "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }
    private void openFileSD(String fileName) {
        // проверяем доступность SD
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d(LOG_TAG, "SD-карта не доступна: " + Environment.getExternalStorageState());
            return;
        }
        // получаем путь к SD
        File sdPath = Environment.getExternalStorageDirectory();
        // добавляем свой каталог к пути
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        // формируем объект File, который содержит путь к файлу
        File sdFile = new File(sdPath, fileName);
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(sdFile));
            String str = "";
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                mEditText.setText(str);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   /* private void saveFile(String fileName) {
        try {
            showDialog(DEVICE);
            OutputStream outputStream = openFileOutput(fileName, 0);
            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
            osw.write(mEditText.getText().toString());
            osw.close();
        } catch (Throwable t) {
            Toast.makeText(getApplicationContext(),
                    "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }
    */
    /*
    private String[] readList(String DIR_SD) {
        File sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + DIR_SD);
        File[] listOfFiles = sdPath.listFiles();
        final String[] arrayOfFiles=new String[listOfFiles.length];
        int i=0;
        for (File file : listOfFiles)
            if (file.isFile()&&file.getName().endsWith(".txt"))
                arrayOfFiles[i++]=file.getName()+".txt";
        return arrayOfFiles;
    }
    */
}