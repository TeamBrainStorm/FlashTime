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
import com.d4a.flashtime.R;

public class NewDeckDialog extends DialogFragment {

    private DeckMutator mDeckMutator;

    public static NewDeckDialog newInstance() {
        NewDeckDialog dialog = new NewDeckDialog();

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
        final LayoutInflater inflater = LayoutInflater.from(ctx);
        final View view = inflater.inflate(R.layout.dialog_new_deck, null);
        final EditText name = (EditText) view.findViewById(R.id.deck_name);
        final EditText description = (EditText) view.findViewById(R.id.deck_description);

        return new AlertDialog.Builder(ctx)
                .setTitle(R.string.new_deck_title)
                .setView(view)
                .setPositiveButton(R.string.new_deck_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeckMutator.insertDeck(name.getText().toString(), description.getText()
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
