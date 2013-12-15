package edu.cmu.hcii.ssui.flashcards.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import edu.cmu.hcii.ssui.flashcards.Deck.DeckMutator;
import edu.cmu.hcii.ssui.flashcards.util.ArgUtil;
import com.d4a.flashtime.R;

public class EditDeckDialog extends DialogFragment {

    private DeckMutator mDeckMutator;

    public static EditDeckDialog newInstance(long deckId, String deckName, String deckDescription) {
        EditDeckDialog dialog = new EditDeckDialog();
        Bundle args = new Bundle();
        args.putLong(ArgUtil.ARG_DECK_ID, deckId);
        args.putString(ArgUtil.ARG_DECK_NAME, deckName);
        args.putString(ArgUtil.ARG_DECK_DESCRIPTION, deckDescription);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mDeckMutator = (DeckMutator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeckMutator");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context ctx = getActivity();

        final long deckId = getArguments().getLong(ArgUtil.ARG_DECK_ID);
        final String oldName = getArguments().getString(ArgUtil.ARG_DECK_NAME);
        final String oldDescription = getArguments().getString(ArgUtil.ARG_DECK_DESCRIPTION);

        final LayoutInflater inflater = LayoutInflater.from(ctx);
        final View view = inflater.inflate(R.layout.dialog_new_deck, null);
        final EditText name = (EditText) view.findViewById(R.id.deck_name);
        final EditText description = (EditText) view.findViewById(R.id.deck_description);

        name.setText(oldName);
        description.setText(oldDescription);

        return new AlertDialog.Builder(ctx)
                .setTitle(R.string.edit_deck_title)
                .setView(view)
                .setPositiveButton(R.string.edit_deck_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeckMutator.updateDeck(deckId, name.getText().toString(), description.getText()
                                .toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Cancel new deck.

                    }
                }).create();
    }

}
