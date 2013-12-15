package edu.cmu.hcii.ssui.flashcards;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.d4a.flashtime.R;

import edu.cmu.hcii.ssui.flashcards.Deck.DeckMutator;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.DeckTable;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.Queries;
import edu.cmu.hcii.ssui.flashcards.db.CardContract.Tables;
import edu.cmu.hcii.ssui.flashcards.db.CardDbHelper;
import edu.cmu.hcii.ssui.flashcards.dialogs.DeleteDeckDialog;
import edu.cmu.hcii.ssui.flashcards.dialogs.EditDeckDialog;
import edu.cmu.hcii.ssui.flashcards.dialogs.NewDeckDialog;
import edu.cmu.hcii.ssui.flashcards.util.ArgUtil;

public class StudyListActivity extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, DeckMutator, ActionMode.Callback {
    private static final String TAG = StudyListActivity.class.getSimpleName();

    private static final int LOADER_ID = 1;

    private ActionMode.Callback mActionModeCallback;
    private long mSelectedId;
    private String mSelectedName, mSelectedDescription;

    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private SimpleCursorAdapter mAdapter;
    private SQLiteCursorLoader mLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedId = id;
                mSelectedName = ((TextView) view.findViewById(R.id.label_deck_name)).getText().toString();
                mSelectedDescription = ((TextView) view.findViewById(R.id.label_deck_description)).getText()
                        .toString();
                startActionMode(mActionModeCallback);
                view.setSelected(true);
                parent.setSelection(position);
                return true;
            }
        });

        mActionModeCallback = this;

        String[] dataColumns = { DeckTable.NAME, DeckTable.DESCRIPTION };
        int[] viewIds = { R.id.label_deck_name, R.id.label_deck_description };

        mAdapter = new SimpleCursorAdapter(this, R.layout.deck_list_item, null, dataColumns,
                viewIds, 0);
        setListAdapter(mAdapter);
        mCallbacks = this;

        LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_ID, null, mCallbacks);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        v.setEnabled(false);

        String name = ((TextView) v.findViewById(R.id.label_deck_name)).getText().toString();
        String description = ((TextView) v.findViewById(R.id.label_deck_description)).getText()
                .toString();

        Intent intent = new Intent(this, StudyActivity.class);
        intent.putExtra(ArgUtil.ARG_DECK_ID, id);
        intent.putExtra(ArgUtil.ARG_DECK_NAME, name);
        intent.putExtra(ArgUtil.ARG_DECK_DESCRIPTION, description);
        startActivity(intent);
    }

    /* --- ACTION BAR --- */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_new_deck:
            DialogFragment dialog = NewDeckDialog.newInstance();
            dialog.show(getFragmentManager(), NewDeckDialog.class.getSimpleName());
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* --- LOADER CALLBACKS --- */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mLoader = new SQLiteCursorLoader(this, CardDbHelper.getInstance(this),
                Queries.GET_ALL_DECKS, null);
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mLoader = (SQLiteCursorLoader) loader;

        switch (loader.getId()) {
        case LOADER_ID:
            mAdapter.swapCursor(cursor);
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLoader = (SQLiteCursorLoader) loader;
        mAdapter.swapCursor(null);
    }

    /* --- DECK MUTATOR METHODS --- */

    @Override
    public void insertDeck(String name, String description) {
        ContentValues values = new ContentValues();
        values.put(DeckTable.NAME, name);
        values.put(DeckTable.DESCRIPTION, description);

        if (!name.isEmpty()) {
            mLoader.insert(Tables.DECKS, null, values);
        }
    }

    @Override
    public void deleteDeck(long id) {
        mLoader.delete(Tables.DECKS, DeckTable._ID + " = ?", new String[] { String.valueOf(id) });
    }

    @Override
    public void updateDeck(long id, String name, String description) {
        ContentValues values = new ContentValues();
        values.put(DeckTable.NAME, name);
        values.put(DeckTable.DESCRIPTION, description);

        mLoader.update(Tables.DECKS, values, DeckTable._ID + " = ?", new String[] { String.valueOf(id) });
    }

    /* --- ACTION MODE CALLBACKS --- */

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        DialogFragment dialog;
        switch(item.getItemId()) {
        case R.id.action_edit:
            dialog = EditDeckDialog.newInstance(mSelectedId, mSelectedName, mSelectedDescription);
            dialog.show(getFragmentManager(), EditDeckDialog.class.getSimpleName());
            mode.finish();
            return true;
        case R.id.action_delete:
            dialog = DeleteDeckDialog.newInstance(mSelectedId);
            dialog.show(getFragmentManager(), DeleteDeckDialog.class.getSimpleName());
            mode.finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.item_context_menu, menu);
        mode.setTitle(R.string.deck_selected);
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
