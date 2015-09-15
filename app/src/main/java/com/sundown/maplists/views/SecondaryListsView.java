package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Color;
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
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.FieldType;
import com.sundown.maplists.models.SecondaryList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * Created by Sundown on 5/21/2015.
 */
public class SecondaryListsView extends RelativeLayout {

    public interface AllListsListener{
        void LocationListSelected(SecondaryList list);
    }

    private RecyclerView recyclerView;
    private TextView emptyListText;
    private AdapterLocationItems adapter;
    private AllListsListener listener;
    private static final Map<FieldType, Integer> imageResources;
    static
    {
        imageResources = new HashMap<>();
        imageResources.put(FieldType.NAME, R.drawable.ic_name);
        imageResources.put(FieldType.PHONE, R.drawable.ic_phonenumber);
        imageResources.put(FieldType.EMAIL, R.drawable.ic_email);
        imageResources.put(FieldType.DATE, R.drawable.ic_date);
        imageResources.put(FieldType.TIME, R.drawable.ic_time);
        imageResources.put(FieldType.URL, R.drawable.ic_url);
        imageResources.put(FieldType.PRICE, R.drawable.ic_price);
    }

    public SecondaryListsView(Context context, AttributeSet attrs) { super(context, attrs);}


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        emptyListText = (TextView) findViewById(R.id.emptyText);
        recyclerView = (RecyclerView) findViewById(R.id.locationList);
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
        private ArrayList<SecondaryList> locationItems = new ArrayList<>();


        public AdapterLocationItems(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.list_item_container_view, parent, false));
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.reset();
            String comment = ""; //if one exists, we will only ever show the first comment, at very bottom of view..


            SecondaryList locationItem = locationItems.get(position);
            List<Field> fields = locationItem.getFields();

            for (Field field: fields) {
                FieldType type = field.getType();
                switch (type) {
                    case SUBJECT: { //reserved field only one per item
                        EntryField entryField = (EntryField) field;
                        holder.subjectText.setText(entryField.getEntry(0));
                        holder.subjectText.setBackgroundColor(locationItem.getColor());
                        break;
                    }
                    case NAME:
                    case PHONE:
                    case EMAIL:
                    case DATE:
                    case TIME:
                    case DATE_TIME:
                    case URL:
                    case PRICE:{
                        drawViews((EntryField) field, holder);
                        break;
                    }
                    case COMMENT:{
                        if (comment.length() == 0){ //only take first comment for display
                            EntryField entryField = (EntryField) field;
                            comment = entryField.getEntry(0);
                        }
                    }
                }
            }

            if (comment.length() > 0) { //lastly draw the comment if one exists
                drawComment(comment, holder);
            }
        }

        private void drawViews(EntryField entryField, ViewHolder holder){
            int size = entryField.getNumEntries();
            if (entryField.isTitleShown()){
                drawTitleView(entryField.getTitle(), holder);
            }

            for (int i = 0; i < size; ++i){
                if (size > i+1) {
                    drawDoubleView(entryField.getEntry(i), entryField.getEntry(i + 1), entryField.getType(), holder);
                    i++;
                } else {
                    drawSingleView(entryField.getEntry(i), entryField.getType(), holder);
                }
            }
        }

        private void drawTitleView(String title, ViewHolder holder){
            ListItemSingleView view = holder.getSingleView();
            view.initAsTitle(title);
            holder.container.addView(view);
        }

        private void drawDoubleView(String entry1, String entry2, FieldType type, ViewHolder holder){
            ListItemDoubleView view = holder.getDoubleView();
            if (type == FieldType.DATE_TIME){
                view.initWithIcon(imageResources.get(FieldType.DATE), imageResources.get(FieldType.TIME), entry1, entry2, false);
            } else {
                view.initWithIcon(imageResources.get(type), imageResources.get(type), entry1, entry2, false);
            }
            holder.container.addView(view);
        }


        private void drawSingleView(String entry1, FieldType type, ViewHolder holder){
            ListItemSingleView view = holder.getSingleView();
            view.initWithIcon(imageResources.get(type), entry1, false);
            holder.container.addView(view);
        }

        private void drawComment(String comment, ViewHolder holder){
            ListItemSingleView view = holder.getSingleView();
            view.initWithoutIcon(comment);
            holder.container.addView(view);
        }

        @Override
        public int getItemCount() {
            return locationItems.size();
        }

        public void setList(List<SecondaryList> locationItems) {
            this.locationItems.clear(); //*** ALWAYS DO THIS FIRST BECAUSE SOME PHONES WILL THROW OutOfBounds Exception Inconsistency detected. Invalid view holder adapter positionViewHolder
            this.locationItems.addAll(locationItems);
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final static int VIEW_TYPE_SINGLE = 1;
            private final static int VIEW_TYPE_DOUBLE = 2;
            private final static int VIEW_TYPE_ALL = 3;

            TextView subjectText;
            LinearLayout container;
            private Stack<ListItemSingleView> singleViews = new Stack<>();
            private Stack<ListItemDoubleView> doubleViews = new Stack<>();


            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                subjectText = (TextView) itemView.findViewById(R.id.listItemSubject);
                container = (LinearLayout) itemView.findViewById(R.id.listItemContainer);

                generateViews(VIEW_TYPE_ALL);

            }

            @Override
            public void onClick(View v) {
                listener.LocationListSelected(locationItems.get(getAdapterPosition()));
            }

            public ListItemSingleView getSingleView(){
                if (singleViews.size() == 0){
                    generateViews(VIEW_TYPE_SINGLE);
                }
                return singleViews.pop();
            }

            public ListItemDoubleView getDoubleView(){
                if (doubleViews.size() == 0){
                    generateViews(VIEW_TYPE_DOUBLE);
                }
                return doubleViews.pop();
            }

            private void generateViews(int viewType) {

                switch (viewType){
                    case VIEW_TYPE_ALL:
                        for (int i = 0; i < 10; ++i){
                            singleViews.push((ListItemSingleView)inflater.inflate(R.layout.list_item_single_view, container, false));
                            doubleViews.push((ListItemDoubleView)inflater.inflate(R.layout.list_item_double_view, container, false));
                        }
                        break;
                    case VIEW_TYPE_SINGLE:
                        for (int i = 0; i < 10; ++i){
                            singleViews.push((ListItemSingleView)inflater.inflate(R.layout.list_item_single_view, container, false));
                        }
                        break;
                    case VIEW_TYPE_DOUBLE:
                        for (int i = 0; i < 10; ++i){
                            doubleViews.push((ListItemDoubleView)inflater.inflate(R.layout.list_item_double_view, container, false));
                        }
                        break;
                }

            }

            private void reset(){
                container.removeAllViews();
                subjectText.setBackgroundColor(Color.argb(0, 0, 0, 0));
                subjectText.setText("");
            }

        }
    }

}
