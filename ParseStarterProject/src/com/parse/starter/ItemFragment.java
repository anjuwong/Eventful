package com.parse.starter;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.parse.starter.ListContent;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ItemFragment extends Fragment /* implements AbsListView.OnItemLongClickListener */{

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String DESCRIPTIONS = "descriptions";
    private static final String IDS = "ids";

    private ArrayList<String> mDescriptions;
    private ArrayList<String> mIDs;
    private ArrayList<String> mLocs;
    private ArrayList<String> mTitles;
    private ArrayList<String> mDates;

    private OnFragmentInteractionListener mListener;
    private OnLongFragmentInteractionListener mLongListener;
    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    //private ListContent mListContent;
    private ArrayList<EventListItem> mListContent;
    private ArrayList<Integer> mTypes;

    public static ItemFragment newInstance(ArrayList<String> descriptions, ArrayList<String> ids, ArrayList<Integer> types) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(DESCRIPTIONS, descriptions);
        args.putStringArrayList(IDS, ids);
        args.putIntegerArrayList("types",types);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            //mDescriptions = getArguments().getStringArrayList(DESCRIPTIONS);
            mIDs = getArguments().getStringArrayList(IDS);
            mLocs = getArguments().getStringArrayList("locs");
            mTitles = getArguments().getStringArrayList("titles");
            mDates = getArguments().getStringArrayList("dates");
            mTypes = getArguments().getIntegerArrayList("types");
        }

        //mListContent = new ListContent();
        mListContent = new ArrayList<EventListItem>();

        for (int i = 0; i < mIDs.size(); i++) {
            //mListContent.addItem(new ListContent.ListItem(mIDs.get(i), mTypes.get(i)+mDescriptions.get(i)));
            mListContent.add(new EventListItem(mIDs.get(i), mTitles.get(i), mLocs.get(i), mDates.get(i), mTypes.get(i)));
        }

        //mAdapter = new ArrayAdapter<ListContent.ListItem>(getActivity(),
        //        R.layout.main_row, R.id.main_row_text, mListContent.ITEMS);
        mAdapter = new EventListAdapter(getActivity(), R.layout.fragment_item_list, mListContent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(mListener != null)
                    mListener.onFragmentInteraction(mListContent.get(position).id);
            }
        });
        // make these data members
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (null != mLongListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mLongListener.onLongFragmentInteraction(mListContent.get(position).id);
                }
                return true;
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mLongListener = (OnLongFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mLongListener = null;
    }

    /*public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mListener.onFragmentInteraction(mListContent.get(position).id);
            //TODO: onLongItemClick
        }
    }
    */
    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

/*    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (null != mLongListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            mLongListener.onLongFragmentInteraction(mListContent.get(position).id);
            //TODO: onLongItemClick
        }
        return true;
    }
*/
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String id);
    }
    public interface OnLongFragmentInteractionListener {
        void onLongFragmentInteraction(String id);
    }

}
