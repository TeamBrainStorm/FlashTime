package edu.cmu.hcii.ssui.flashcards.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import edu.cmu.hcii.ssui.flashcards.Card.CardMutator;
import edu.cmu.hcii.ssui.flashcards.util.ArgUtil;
import com.d4a.flashtime.R;

public class DeleteCardDialog extends DialogFragment {

    private CardMutator mCardMutator;

    public static DeleteCardDialog newInstance(long cardId) {
        DeleteCardDialog dialog = new DeleteCardDialog();
        Bundle args = new Bundle();
        args.putLong(ArgUtil.ARG_CARD_ID, cardId);
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

        return new AlertDialog.Builder(ctx)
                .setTitle(R.string.delete_card_title)
                .setMessage(R.string.delete_card_message)
                .setPositiveButton(R.string.delete_card_button,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mCardMutator.deleteCard(cardId);
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
