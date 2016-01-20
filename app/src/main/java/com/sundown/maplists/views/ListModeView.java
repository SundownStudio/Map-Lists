package com.sundown.maplists.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundown.maplists.R;
import com.sundown.maplists.models.fields.EntryField;
import com.sundown.maplists.models.fields.Field;
import com.sundown.maplists.models.lists.SecondaryList;
import com.sundown.maplists.utils.SecondaryListViewManager;
import com.sundown.maplists.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Sundown on 5/21/2015.
 */
public class ListModeView extends RelativeLayout {

    public interface AllListsListener{
        void SecondaryListSelected(SecondaryList list);
    }

    private RecyclerView recyclerView;
    private TextView emptyListText;
    private AdapterLocationItems adapter;
    private AllListsListener listener;

    public ListModeView(Context context, AttributeSet attrs) { super(context, attrs);}


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        emptyListText = (TextView) findViewById(R.id.emptyText);
        recyclerView = (RecyclerView) findViewById(R.id.listModeRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterLocationItems(getContext());
        recyclerView.setAdapter(adapter);

    }

    public void init(final List<SecondaryList> items, final AllListsListener listener){

        if (items.size() == 0){
            emptyListText.setVisibility(View.VISIBLE);
        } else {
            emptyListText.setVisibility(View.GONE);
        }
        adapter.setList(items);
        this.listener = listener;
    }


    private class AdapterLocationItems extends RecyclerView.Adapter<AdapterLocationItems.ViewHolder> {

        private LayoutInflater inflater;
        private ArrayList<SecondaryList> listItems = new ArrayList<>();
        private SecondaryListViewManager secondaryListViewManager;


        public AdapterLocationItems(Context context) {
            inflater = LayoutInflater.from(context);
            secondaryListViewManager = SecondaryListViewManager.getInstance().reset(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.list_item_container_view, parent, false));
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.reset();

            SecondaryList locationItem = listItems.get(position);
            List<Field> fields = locationItem.getFields();

            for (Field field: fields) {
                int type = field.getType();
                switch (type) {
                    case Field.SUBJECT: { //reserved field only one per item
                        EntryField entryField = (EntryField) field;
                        holder.subjectText.setText(entryField.getEntry(0));
                        holder.subjectText.setBackgroundDrawable(ViewUtils.getTopRoundedCornersDrawable(getResources().getDimension(R.dimen.rounded_corners), locationItem.getColor()));
                        break;
                    }
                    case Field.NAME:
                    case Field.PHONE:
                    case Field.EMAIL:
                    case Field.DATE:
                    case Field.TIME:
                    case Field.DATE_TIME:
                    case Field.URL:
                    case Field.PRICE:{
                        EntryField entryField = (EntryField) field;
                        addTitleView(entryField, holder);

                        int size = entryField.getNumEntries();
                        for (int i = 0; i < size; ++i) {
                            if (size > i + 1) {

                                if (type == Field.DATE_TIME){
                                    holder.container.addView(secondaryListViewManager.drawDoubleView(Field.DATE, Field.TIME, entryField.getEntry(i), entryField.getEntry(++i)));
                                } else if (type == Field.PRICE){
                                    holder.container.addView(secondaryListViewManager.drawDoubleView(type, type, entryField.getEntry(i), entryField.getEntry(++i)));

                                } else {
                                    holder.container.addView(secondaryListViewManager.drawDoubleView(type, type, entryField.getEntry(i), entryField.getEntry(++i)));
                                }

                            } else {
                                ListItemSingleView view = secondaryListViewManager.drawSingleView(entryField.getType(), entryField.getEntry(i), false);
                                holder.container.addView(view);
                            }
                        }
                        break;
                    }
                    case Field.MESSAGE:{
                        EntryField entryField = (EntryField) field;
                        addTitleView(entryField, holder);
                        int size = entryField.getNumEntries();
                        for (int i = 0; i < size; ++i) {
                            ListItemSingleView view = secondaryListViewManager.drawSingleView(entryField.getType(), entryField.getEntry(i), false);
                            holder.container.addView(view);
                        }
                        break;
                    }
                    case Field.ITEM_LIST:{
                        EntryField entryField = (EntryField) field;
                        addTitleView(entryField, holder);
                        addAllDoubleViews(entryField, holder, -1, -1);
                        break;
                    }
                    case Field.PRICE_LIST:{
                        EntryField entryField = (EntryField) field;
                        addTitleView(entryField, holder);
                        addAllDoubleViews(entryField, holder, -1, Field.PRICE);
                        break;
                    }
                }
            }
        }

        private void addTitleView(EntryField entryField, ViewHolder holder){
            if (entryField.isTitleShown()) {
                ListItemSingleView v = secondaryListViewManager.drawSingleView(-1, entryField.getTitle(), true);
                holder.container.addView(v);
            }
        }

        private void addAllDoubleViews(EntryField entryField, ViewHolder holder, int type1, int type2){
            int size = entryField.getNumEntries();
            for (int i = 0; i < size; ++i) {
                if (size > i + 1) {
                    holder.container.addView(secondaryListViewManager.drawDoubleView(type1, type2, entryField.getEntry(i), entryField.getEntry(++i)));
                }
            }
        }


        @Override
        public int getItemCount() {
            return listItems.size();
        }

        public void setList(List<SecondaryList> locationItems) {
            this.listItems.clear(); //*** ALWAYS DO THIS FIRST BECAUSE SOME PHONES WILL THROW OutOfBounds Exception Inconsistency detected. Invalid view holder adapter positionViewHolder
            this.listItems.addAll(locationItems);
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView subjectText;
            //LinearLayout subjectLayout;
            LinearLayout container;


            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                subjectText = (TextView) itemView.findViewById(R.id.listItemSubjectText);
                //subjectLayout = (LinearLayout) itemView.findViewById(R.id.listItemSubjectLayout);
                container = (LinearLayout) itemView.findViewById(R.id.listItemContainer);
            }

            @Override
            public void onClick(View v) {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) //check in case item is deleted avoid crash
                    listener.SecondaryListSelected(listItems.get(pos));
            }


            private void reset(){
                container.removeAllViews();
                //subjectLayout.setBackgroundColor(Color.argb(0, 0, 0, 0));
                subjectText.setText("");
            }

        }
    }

}
