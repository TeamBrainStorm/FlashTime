package edu.cmu.hcii.ssui.flashcards.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.CardTable;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.DeckTable;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.Tables;

public class CardDbHelper extends SQLiteOpenHelper {
    private static final String TAG = CardDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "flashcards.db";
    private static final int DATABASE_VERSION = 1;

    /**
     * {@code REFERENCES} clauses.
     * */
    //@formatter:off
    private interface References {
        String DECK_ID = "REFERENCES " + Tables.DECKS + "(" + DeckTable._ID + ") ON DELETE CASCADE";
    }
    //@formatter:on

    //@formatter:off
    private static final String CARD_TABLE_CREATE = "CREATE TABLE " + Tables.CARDS + " ("
            + CardTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CardTable.DECK_ID + " INTEGER NOT NULL " + References.DECK_ID + ", "
            + CardTable.FRONT + " TEXT, "
            + CardTable.BACK + " TEXT);";
    //@formatter:on

    //@formatter:off
    private static final String DECK_TABLE_CREATE = "CREATE TABLE " + Tables.DECKS + " ("
            + DeckTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DeckTable.NAME + " TEXT, "
            + DeckTable.DESCRIPTION + " TEXT);";
    //@formatter:on

    private CardDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static CardDbHelper sInstance;

    public static CardDbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CardDbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CARD_TABLE_CREATE);
        db.execSQL(DECK_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from "+ oldVersion + " to " + newVersion + ".");

        // Drop older tables if exists.
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CARDS + ";");
        db.execSQL("DROP TABLE IF EXISTS " + Tables.DECKS + ";");

        // Create tables again.
        onCreate(db);
    }

}
