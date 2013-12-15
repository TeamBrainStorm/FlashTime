package edu.cmu.hcii.ssui.flashcards.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.cmu.hcii.ssui.flashcards.Card;
import edu.cmu.hcii.ssui.flashcards.Deck;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.CardTable;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.DeckTable;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.Tables;

/**
 * Database access object providing methods to interact with the cards and
 * decks stored in persistent storage.
 *
 * @author Shannon Lee
 */
public class CardDatabase {
    private static final String TAG = CardDatabase.class.getSimpleName();

    private CardDbHelper mDbHelper;
    private SQLiteDatabase mDb;

    public CardDatabase(Context context) {
        mDbHelper = CardDbHelper.getInstance(context);
    }

    /**
     * Opens the database for access.
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();
    }

    /**
     * Closes the connection to the database.
     */
    public void close() {
        if (mDb != null) {
            mDb.close();
        }
    }

    /**
     * Inserts a {@link Card} into the database. The information of that card
     * cannot be {@code null}.
     *
     * @param card the {@link Card} to be inserted into the database
     */
    public long insertCard(Card card) {
        return insertCard(card.getDeckId(), card.getFront(), card.getBack());
    }

    public long insertCard(long deckId, String front, String back) {
        Log.i(TAG, "Adding new Card '" + front + "/" + back + "' to the database.");

        ContentValues values = new ContentValues();
        values.put(CardTable.DECK_ID, deckId);
        values.put(CardTable.FRONT, front);
        values.put(CardTable.BACK, back);

        return mDb.insert(Tables.CARDS, null, values);
    }

    /**
     * Inserts a {@link Deck} into the database. The information of that deck
     * cannot be {@code null}.
     *
     * @param deck the {@link Deck} to be inserted into the database
     */
    public long insertDeck(Deck deck) {
        return insertDeck(deck.getName(), deck.getDescription());
    }

    public long insertDeck(String name, String description) {
        Log.i(TAG, "Adding new Deck '" + name + "' to the database.");

        ContentValues values = new ContentValues();
        values.put(DeckTable.NAME, name);
        values.put(DeckTable.DESCRIPTION, description);

        return mDb.insert(Tables.DECKS, null, values);
    }

    public List<Deck> getDecks() {
        // "SELECT * FROM " + Tables.DECKS
        Cursor cursor = mDb.query(true, Tables.DECKS, null, null, null, null, null, null, null);

        List<Deck> decks = new ArrayList<Deck>();

        // Iterate through the decks.
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Deck deck = cursorToDeck(cursor);
            decks.add(deck);
            cursor.moveToNext();
        }
        cursor.close();
        return decks;
    }

    public List<Card> getCardsByDeck(Deck deck) {
        // "SELECT * FROM " + Tables.CARDS + " WHERE " + CardTable.DECK_ID +
        // " = " + deck.getId()
        Cursor cursor = mDb.query(true, Tables.CARDS, null, CardTable.DECK_ID + " = ?",
                new String[] { String.valueOf(deck.getId()) }, null, null, null, null);

        List<Card> cards = new ArrayList<Card>();

        // Iterate through the cards.
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Card card = cursorToCard(cursor);
            cards.add(card);
            cursor.moveToNext();
        }
        cursor.close();

        return cards;
    }

    public void deleteCard(long id) {
        mDb.delete(Tables.CARDS, CardTable._ID + " = ?", new String[] { String.valueOf(id) });
    }

    public void deleteCard(Card card) {
        long id = card.getId();
        deleteCard(id);
    }

    public void deleteDeck(long id) {
        mDb.delete(Tables.DECKS, DeckTable._ID + " = ?", new String[] { String.valueOf(id) });
    }

    public void deleteDeck(Deck deck) {
        long id = deck.getId();
        deleteCard(id);
    }

    private Card cursorToCard(Cursor cursor) {
        long id = cursor.getLong(0);
        long deckId = cursor.getLong(1);
        String front = cursor.getString(2);
        String back = cursor.getString(3);
        return new Card(id, deckId, front, back);
    }

    private Deck cursorToDeck(Cursor cursor) {
        long id = cursor.getLong(0);
        String name = cursor.getString(1);
        String description = cursor.getString(2);
        return new Deck(id, name, description);
    }

}
