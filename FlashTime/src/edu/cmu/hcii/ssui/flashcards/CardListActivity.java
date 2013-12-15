package edu.cmu.hcii.ssui.flashcards;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.d4a.flashtime.R;

import edu.cmu.hcii.ssui.flashcards.Card.CardMutator;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.CardTable;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.Queries;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.Tables;
import edu.cmu.hcii.ssui.flashcards.db.CardDbHelper;
import edu.cmu.hcii.ssui.flashcards.dialogs.DeleteCardDialog;
import edu.cmu.hcii.ssui.flashcards.dialogs.EditCardDialog;
import edu.cmu.hcii.ssui.flashcards.dialogs.NewCardDialog;
import edu.cmu.hcii.ssui.flashcards.util.ArgUtil;

public class CardListActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, CardMutator, ActionMode.Callback {

    private static final int CARDS_LOADER_ID = 1;

    private static final int DECK_LOADER_ID = 2;

    private ActionMode.Callback mActionModeCallback;
    private long mSelectedId;
    private String mSelectedFront, mSelectedBack;

    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;

    private SimpleCursorAdapter mCardsAdapter;
    private SQLiteCursorLoader mCardsLoader;

    private SQLiteCursorLoader mDeckLoader;

    private long mDeckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();

        // Sets the ActionBar title to be that of the Bundle
        CharSequence title = bundle.getCharSequence(ArgUtil.ARG_DECK_NAME);
        CharSequence subtitle = bundle.getCharSequence(ArgUtil.ARG_DECK_DESCRIPTION);
        setActionBarTitle(title, subtitle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar bar = getActionBar();
            bar.setDisplayHomeAsUpEnabled(true);
        }

        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedId = id;
                mSelectedFront = ((TextView) view.findViewById(R.id.label_card_front)).getText()
                        .toString();
                mSelectedBack = ((TextView) view.findViewById(R.id.label_card_back)).getText()
                        .toString();
                startActionMode(mActionModeCallback);
                view.setSelected(true);
                return true;
            }
        });

        mActionModeCallback = this;

        mDeckId = bundle.getLong(ArgUtil.ARG_DECK_ID);

        String[] dataColumns = { CardTable.FRONT, CardTable.BACK };
        int[] viewIds = { R.id.label_card_front, R.id.label_card_back };

        mCardsAdapter = new SimpleCursorAdapter(this, R.layout.card_list_item, null, dataColumns,
                viewIds, 0);
        setListAdapter(mCardsAdapter);
        mCallbacks = this;

        LoaderManager lm = getLoaderManager();
        lm.initLoader(CARDS_LOADER_ID, bundle, mCallbacks);
        lm.initLoader(DECK_LOADER_ID, bundle, mCallbacks);
    }

    private void setActionBarTitle(CharSequence title, CharSequence subtitle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar bar = getActionBar();
            bar.setTitle(title);
            if (subtitle.length() > 0) {
                bar.setSubtitle(subtitle);
            }
        }
    }

    /* --- ACTION BAR --- */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.deck, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_new_card:
            DialogFragment dialog = NewCardDialog.newInstance(mDeckId);
            dialog.show(getFragmentManager(), NewCardDialog.class.getSimpleName());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* --- LOADER CALLBACKS --- */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
        case CARDS_LOADER_ID:
            mCardsLoader = new SQLiteCursorLoader(this, CardDbHelper.getInstance(this),
                    Queries.GET_CARDS_BY_DECK, new String[] { String.valueOf(mDeckId) });
            return mCardsLoader;
        case DECK_LOADER_ID:
            mDeckLoader = new SQLiteCursorLoader(this, CardDbHelper.getInstance(this), Queries.GET_DECK,
                    new String[] { String.valueOf(mDeckId) });
            return mDeckLoader;
        }
        return null; // Should never happen.
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
        case CARDS_LOADER_ID:
            mCardsLoader = (SQLiteCursorLoader) loader;
            mCardsAdapter.swapCursor(cursor);
            break;
        case DECK_LOADER_ID:
            cursor.moveToFirst();
            String name = cursor.getString(1); // Deck Name
            String description = cursor.getString(2); // Deck Description
            setActionBarTitle(name, description);
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
        case CARDS_LOADER_ID:
            mCardsLoader = (SQLiteCursorLoader) loader;
            mCardsAdapter.swapCursor(null);
            break;
        case DECK_LOADER_ID:
            mDeckLoader = (SQLiteCursorLoader) loader;
            break;
        }
    }

    /* --- CARD MUTATOR METHODS --- */

    @Override
    public void insertCard(long deckId, String front, String back) {
        if (!front.isEmpty() && !back.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(CardTable.DECK_ID, deckId);
            values.put(CardTable.FRONT, front);
            values.put(CardTable.BACK, back);

            mCardsLoader.insert(Tables.CARDS, null, values);
        }
    }

    @Override
    public void deleteCard(long id) {
        mCardsLoader.delete(Tables.CARDS, CardTable._ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    @Override
    public void updateCard(long id, String front, String back) {
        ContentValues values = new ContentValues();
        if (!front.isEmpty()) {
            values.put(CardTable.FRONT, front);
        }
        if (!back.isEmpty()) {
            values.put(CardTable.BACK, back);
        }

        if (!front.isEmpty() && !back.isEmpty()) {
        mCardsLoader.update(Tables.CARDS, values, CardTable._ID + " = ?",
                new String[] { String.valueOf(id) });
        }
    }

    /* --- ACTION MODE CALLBACKS --- */

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        DialogFragment dialog;
        switch (item.getItemId()) {
        case R.id.action_edit:
            dialog = EditCardDialog.newInstance(mSelectedId, mSelectedFront, mSelectedBack);
            dialog.show(getFragmentManager(), EditCardDialog.class.getSimpleName());
            mode.finish();
            return true;
        case R.id.action_delete:
            dialog = DeleteCardDialog.newInstance(mSelectedId);
            dialog.show(getFragmentManager(), DeleteCardDialog.class.getSimpleName());
            mode.finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.item_context_menu, menu);
        mode.setTitle(R.string.card_selected);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        // Do nothing.
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

}
