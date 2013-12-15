package edu.cmu.hcii.ssui.flashcards;

public class Deck {
    private static final String TAG = Deck.class.getSimpleName();

    public interface DeckMutator {
        void insertDeck(String name, String description);
        void deleteDeck(long id);
        void updateDeck(long id, String name, String description);
    }

    private long mId;
    private String mName;
    private String mDescription;

    public Deck(long id, String name, String description) {
        mId = id;
        mName = name;
        mDescription = description;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

}
