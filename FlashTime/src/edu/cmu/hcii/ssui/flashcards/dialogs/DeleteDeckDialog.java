package edu.cmu.hcii.ssui.flashcards.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import edu.cmu.hcii.ssui.flashcards.Deck.DeckMutator;
import edu.cmu.hcii.ssui.flashcards.util.ArgUtil;
import com.d4a.flashtime.R;

public class DeleteDeckDialog extends DialogFragment {

    private DeckMutator mDeckMutator;

    public static DeleteDeckDialog newInstance(long deckId) {
        DeleteDeckDialog dialog = new DeleteDeckDialog();
        Bundle args = new Bundle();
        args.putLong(ArgUtil.ARG_DECK_ID, deckId);
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

        return new AlertDialog.Builder(ctx)
                .setTitle(R.string.delete_deck_title)
                .setMessage(R.string.delete_deck_message)
                .setPositiveButton(R.string.delete_deck_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mDeckMutator.deleteDeck(deckId);
                            }
                        })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: Cancel delete deck.

                    }
                }).create();
    }

}
