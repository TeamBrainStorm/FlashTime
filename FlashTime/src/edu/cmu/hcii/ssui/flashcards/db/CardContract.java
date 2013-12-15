package edu.cmu.hcii.ssui.flashcards.db;

import android.provider.BaseColumns;
import edu.cmu.hcii.ssui.flashcards.Card;
import edu.cmu.hcii.ssui.flashcards.Deck;

public final class CardContract {

    /**
     * SQLite table names.
     */
    public interface Tables {
        String CARDS = "cards";
        String DECKS = "decks";
    }

    /**
     * {@link Card} table columns.
     */
    public interface CardTable extends BaseColumns {
        String DECK_ID = "deck_id";
        String FRONT = "front";
        String BACK = "back";
    }

    /**
     * {@link Deck} table columns.
     */
    public interface DeckTable extends BaseColumns {
        String NAME = "name";
        String DESCRIPTION = "description";
    }

    public interface Queries {
        String GET_DECK = "SELECT * FROM " + Tables.DECKS + " WHERE " + DeckTable._ID + "= ?";
        String GET_ALL_DECKS = "SELECT * FROM " + Tables.DECKS;
        String GET_CARDS_BY_DECK = "SELECT * FROM " + Tables.CARDS + " WHERE " + CardTable.DECK_ID + "= ?";
    }

    private CardContract() {
        // Should never be instantiated.
    }

}
