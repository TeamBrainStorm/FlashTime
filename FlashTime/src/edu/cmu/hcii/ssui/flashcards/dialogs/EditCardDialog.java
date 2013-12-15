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

public class EditCardDialog extends DialogFragment {

    private CardMutator mCardMutator;

    public static EditCardDialog newInstance(long cardId, String cardFront, String cardBack) {
        EditCardDialog dialog = new EditCardDialog();
        Bundle args = new Bundle();
        args.putLong(ArgUtil.ARG_CARD_ID, cardId);
        args.putString(ArgUtil.ARG_CARD_FRONT, cardFront);
        args.putString(ArgUtil.ARG_CARD_BACK, cardBack);
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

        final long cardId = getArguments().getLong(ArgUtil.ARG_CARD_ID);
        final String oldFront = getArguments().getString(ArgUtil.ARG_CARD_FRONT);
        final String oldBack = getArguments().getString(ArgUtil.ARG_CARD_BACK);

        final LayoutInflater inflater = LayoutInflater.from(ctx);
        final View view = inflater.inflate(R.layout.dialog_new_card, null);
        final EditText name = (EditText) view.findViewById(R.id.card_front);
        final EditText description = (EditText) view.findViewById(R.id.card_back);

        name.setText(oldFront);
        description.setText(oldBack);

        return new AlertDialog.Builder(ctx)
                .setTitle(R.string.edit_card_title)
                .setView(view)
                .setPositiveButton(R.string.edit_card_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCardMutator.updateCard(cardId, name.getText().toString(), description.getText()
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
