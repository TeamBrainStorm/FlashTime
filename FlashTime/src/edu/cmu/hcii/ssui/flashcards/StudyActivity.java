package edu.cmu.hcii.ssui.flashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.commonsware.cwac.loaderex.SQLiteCursorLoader;
import com.d4a.flashtime.R;

import edu.cmu.hcii.ssui.flashcards.db.CardContract.Queries;
import edu.cmu.hcii.ssui.flashcards.db.CardDbHelper;
import edu.cmu.hcii.ssui.flashcards.util.ArgUtil;

public class StudyActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = StudyActivity.class.getSimpleName();

    private static final int LOADER_ID = 1;

    private static final String ARG_SAVE_FLIPPED = "flipped";

    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    private SQLiteCursorLoader mLoader;

    /**
     * The {@link Card}s contained in this {@link Deck}. Populated by the Loader
     * when necessary.
     */
    private final List<Card> mCards = new ArrayList<Card>();

    /**
     * The ID number of the {@link Deck} we're examining at the moment.
     */
    private long mDeckId;

    private String mDeckName;

    private String mDeckDescription;

    /* --- GUI Elements --- */

    private boolean mShowingBack;

    private FrameLayout mCardFront;

    private FrameLayout mCardBack;

    private TextView mMemorized;

    private TextView mEncouragement;

    /**
     * The <code>String</code> array of encouragement.
     */
    private String[] mEncouragementArray;

    /**
     * The pager widget which handles the animation to the next {@link Card}.
     */
    private ViewPager mPager;

    /**
     * Provides the pages to the view pager.
     */
    private StudyPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        Bundle bundle = getIntent().getExtras();
        mDeckId = bundle.getLong(ArgUtil.ARG_DECK_ID);
        mDeckName = bundle.getString(ArgUtil.ARG_DECK_NAME);
        mDeckDescription = bundle.getString(ArgUtil.ARG_DECK_DESCRIPTION);

        mCallbacks = this;

        LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_ID, bundle, mCallbacks);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new StudyPagerAdapter();
        mPager.setAdapter(mPagerAdapter);

        if (savedInstanceState != null) {
            mShowingBack = savedInstanceState.getBoolean(ARG_SAVE_FLIPPED);
        }

        setActionBarTitle(mDeckName, mDeckDescription);

        Resources res = getResources();
        mEncouragementArray = res.getStringArray(R.array.encouragement_strings);
    }

    /* --- ACTION BAR --- */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.study, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_shuffle:
            shuffle();
            return true;
        case R.id.action_cardlist:
            Intent intent = new Intent(this, CardListActivity.class);
            intent.putExtra(ArgUtil.ARG_DECK_ID, mDeckId);
            intent.putExtra(ArgUtil.ARG_DECK_NAME, mDeckName);
            intent.putExtra(ArgUtil.ARG_DECK_DESCRIPTION, mDeckDescription);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ARG_SAVE_FLIPPED, mShowingBack);
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

    /* --- LOADER CALLBACKS --- */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mLoader = new SQLiteCursorLoader(this, CardDbHelper.getInstance(this),
                Queries.GET_CARDS_BY_DECK, new String[] { String.valueOf(mDeckId) });
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
        case LOADER_ID:
            mLoader = (SQLiteCursorLoader) loader;
            mCards.clear();

            // Iterate through the cards and build a list.
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Card card = cursorToCard(cursor);
                mCards.add(card);
                cursor.moveToNext();
            }

            mPagerAdapter.notifyDataSetChanged();
            break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mLoader = (SQLiteCursorLoader) loader;
        mPagerAdapter.notifyDataSetChanged();
    }

    /* --- PRIVATE HELPER METHODS --- */

    private static Card cursorToCard(Cursor cursor) {
        long id = cursor.getLong(0);
        long deckId = cursor.getLong(1);
        String front = cursor.getString(2);
        String back = cursor.getString(3);
        return new Card(id, deckId, front, back);
    }

    private void shuffle() {
        if (mPagerAdapter.getCount() > 0) {
            Collections.shuffle(mCards);
            mPagerAdapter.notifyDataSetChanged();
            Random random = new Random();
            int next = random.nextInt(mPagerAdapter.getCount());
            while (next == random.nextInt(mPagerAdapter.getCount())) {
                next = random.nextInt(mPagerAdapter.getCount());
            }
            mPager.setCurrentItem(next);
        }
    }

    private void flipCard() {
        if (!mShowingBack) {
            // Loads the animation for flipping the cards.
            AnimatorSet backIn = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                    R.anim.card_flip_right_in);
            backIn.setTarget(mCardBack);
            AnimatorSet frontOut = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                    R.anim.card_flip_right_out);
            frontOut.setTarget(mCardFront);

            // Combines and plays the animation.
            AnimatorSet flip = new AnimatorSet();
            flip.play(backIn).with(frontOut);
            flip.start();

            mShowingBack = true;
            mCardBack.setClickable(true);
            mCardFront.setClickable(false);
        } else {
            AnimatorSet backIn = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                    R.anim.card_flip_left_in);
            backIn.setTarget(mCardFront);
            AnimatorSet frontOut = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                    R.anim.card_flip_left_out);
            frontOut.setTarget(mCardBack);

            AnimatorSet flip = new AnimatorSet();
            flip.play(backIn).with(frontOut);
            flip.start();

            mShowingBack = false;
            mCardBack.setClickable(false);
            mCardFront.setClickable(true);
        }
    }

    /* --- PAGER ADAPTER --- */

    /**
     * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment}
     * objects, in
     * sequence.
     */
    private class StudyPagerAdapter extends PagerAdapter {

        private final View.OnClickListener mFlipListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipCard();

                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(mEncouragement, "alpha", 1.0f);
                fadeIn.start();
            }
        };

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = View.inflate(getApplicationContext(), R.layout.study_page, null);
            if (position < mCards.size()) {
                Card card = mCards.get(position);

                mCardFront = (FrameLayout) v.findViewById(R.id.card_front);
                TextView frontText = (TextView) v.findViewById(R.id.card_front_text);
                frontText.setText(card.getFront());
                mCardFront.setOnClickListener(mFlipListener);

                mCardBack = (FrameLayout) v.findViewById(R.id.card_back);
                TextView backText = (TextView) v.findViewById(R.id.card_back_text);
                backText.setText(card.getBack());
                mCardBack.setOnClickListener(mFlipListener);
                mCardBack.setClickable(false);

                String progress = "Card " + (position + 1) + " of " + getCount();
                mMemorized = (TextView) v.findViewById(R.id.memorized);
                mMemorized.setText(progress);

                Random random = new Random();
                String yay = mEncouragementArray[random.nextInt(mEncouragementArray.length)];
                mEncouragement = (TextView) v.findViewById(R.id.encouragement);
                mEncouragement.setText(yay);

                if (mCardFront.getAlpha() == 0.0) {
                    mShowingBack = true;
                } else {
                    mShowingBack = false;
                }
            }
            container.addView(v, 0);
            return v;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (mCards.size() == 0 && position == 0) {
                View v = View.inflate(getApplicationContext(), R.layout.study_page_empty, null);
                ((ViewGroup) container.getParent()).addView(v, 0);
            }
            View v = (View) object;
            if (position < mCards.size()) {
                View empty = findViewById(R.id.study_page_empty);
                if (empty != null) {
                    ((ViewGroup) empty.getParent()).removeView(empty);
                    ;
                }
                mCardFront = (FrameLayout) v.findViewById(R.id.card_front);
                mCardBack = (FrameLayout) v.findViewById(R.id.card_back);

                mMemorized = (TextView) v.findViewById(R.id.memorized);
                mEncouragement = (TextView) v.findViewById(R.id.encouragement);

                if (mCardFront.getAlpha() == 0.0) {
                    mShowingBack = true;
                } else {
                    mShowingBack = false;
                }
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}
