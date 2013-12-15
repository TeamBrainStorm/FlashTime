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
import edu.cmu.hcii.ssui.flashcards.Card.CardMutator;
import edu.cmu.hcii.ssui.flashcards.util.ArgUtil;
import com.d4a.flashtime.R;

public class NewCardDialog extends DialogFragment {

    private CardMutator mCardMutator;

    public static NewCardDialog newInstance(long deckId) {
        NewCardDialog dialog = new NewCardDialog();
        Bundle args = new Bundle();
        args.putLong(ArgUtil.ARG_DECK_ID, deckId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCardMutator = (CardMutator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement CardMutator");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context ctx = getActivity();
        final long deckId = getArguments().getLong(ArgUtil.ARG_DECK_ID);
        final LayoutInflater inflater = LayoutInflater.from(ctx);
        final View view = inflater.inflate(R.layout.dialog_new_card, null);
        final EditText front = (EditText) view.findViewById(R.id.card_front);
        final EditText back = (EditText) view.findViewById(R.id.card_back);

        return new AlertDialog.Builder(ctx)
                .setTitle(R.string.new_card_title)
                .setView(view)
                .setPositiveButton(R.string.new_card_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCardMutator.insertCard(deckId, front.getText().toString(), back.getText()
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
