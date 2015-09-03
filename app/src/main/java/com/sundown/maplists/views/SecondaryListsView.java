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
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.FieldType;
import com.sundown.maplists.models.SecondaryList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        if (items.size() > 0) emptyListText.setVisibility(View.GONE);
        adapter.setList(items);
        this.listener = listener;
    }



    private class AdapterLocationItems extends RecyclerView.Adapter<AdapterLocationItems.ViewHolder> {


        private LayoutInflater inflater;
        private List<SecondaryList> locationItems = Collections.emptyList();



        public AdapterLocationItems(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.list_item_container_view, parent, false));

        }




        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.container.removeAllViews();
            LinkedHashMap<FieldType, LinkedList<EntryField>> map = new LinkedHashMap<>();
            String comment = ""; //if one exists, we will only ever show the first comment, at very bottom of view..


            SecondaryList locationItem = locationItems.get(position);
            List<Field> list = locationItem.getValues();

            for (Field field: list) {
                FieldType type = field.type;
                switch (type) {
                    case SUBJECT: { //reserved field only one per item
                        EntryField entryField = (EntryField) field;
                        holder.subjectText.setText(entryField.entry);
                        holder.subjectText.setBackgroundColor(locationItem.color);
                        break;
                    }
                    case NAME:
                    case PHONE:
                    case EMAIL:
                    case DATE:
                    case TIME:
                    case URL:
                    case PRICE:{
                        EntryField entryField = (EntryField) field;
                        LinkedList<EntryField> entries = map.get(type);
                        if (entries == null)
                            entries = new LinkedList<>();
                        entries.add(entryField);
                        map.put(type, entries);
                        break;
                    }
                    case COMMENT:{
                        if (comment.length() == 0){ //only take first comment for display
                            EntryField entryField = (EntryField) field;
                            comment = entryField.entry;
                        }
                    }
                }
            }

            Set set = map.entrySet();
            Iterator i = set.iterator();
            while(i.hasNext()) {
                Map.Entry me = (Map.Entry)i.next();
                FieldType type = (FieldType) me.getKey();
                LinkedList<EntryField> entryFields = (LinkedList<EntryField>) me.getValue();

                if (type == FieldType.DATE){
                    LinkedList<EntryField> entryFields2 = map.get(FieldType.TIME);
                    drawDoubleViews(entryFields, entryFields2, FieldType.DATE, FieldType.TIME, holder);

                } else if (type == FieldType.TIME){
                    LinkedList<EntryField> entryFields2 = map.get(FieldType.DATE);
                    drawDoubleViews(entryFields2, entryFields, FieldType.DATE, FieldType.TIME, holder);

                } else {
                    drawSingleViews(entryFields, type, holder);
                }
            }

            if (comment.length() > 0) { //lastly draw the comment if one exists
                drawComment(comment, holder);
            }
        }

        private void drawSingleViews(LinkedList<EntryField> entries, FieldType type, ViewHolder holder){
            if (entries != null) {
                boolean addTopMargin = true;
                while (entries.size() > 0) {
                    EntryField entryField = entries.pop();
                    if (entryField.showTitle){
                        addToSingleView(entryField.title, type, holder, true, false);
                        addTopMargin = false;
                    }
                    addToSingleView(entryField.entry, type, holder, false, addTopMargin);
                    addTopMargin = false;
                }
            }
        }

        private void addToSingleView(String text, FieldType type, ViewHolder holder, boolean asTitle, boolean addTopMargin){
            ListItemSingleView view = holder.getSingleView();
            if (asTitle){
                view.initAsTitle(text);
            } else {
                view.initAsEntry(imageResources.get(type), text, addTopMargin);
            }
            holder.container.addView(view);
        }


        private void drawDoubleViews(LinkedList<EntryField> entries1, LinkedList<EntryField> entries2, FieldType type1, FieldType type2, ViewHolder holder){
            if (entries1 != null && entries2 != null) {
                boolean addTopMargin = true;
                while (entries1.size() > 0 && entries2.size() > 0 && holder.doubleViews.size() > 0) {
                    EntryField entry1 = entries1.pop();
                    EntryField entry2 = entries2.pop();
                    String title1 = "", title2 = "";

                    if (entry1.showTitle)
                        title1 = entry1.title;
                    if (entry2.showTitle)
                        title2 = entry2.title;
                    if (title1.length() > 0 || title2.length() > 0) {
                        addToDoubleView(title1, title2, type1, type2, holder, true, false);
                        addTopMargin = false;
                    }
                    addToDoubleView(entry1.entry, entry2.entry, type1, type2, holder, false, addTopMargin);
                    addTopMargin = false;
                }
            }
            drawSingleViews(entries1, type1, holder);
            drawSingleViews(entries2, type2, holder);
        }

        private void addToDoubleView(String text1, String text2, FieldType type1, FieldType type2, ViewHolder holder, boolean asTitle, boolean addTopMargin){
            ListItemDoubleView view = holder.getDoubleView();
            if (asTitle){
                view.initAsTitle(text1, text2);
            } else {
                view.initAsEntry(imageResources.get(type1), imageResources.get(type2), text1, text2, addTopMargin);
            }
            holder.container.addView(view);
        }

        private void drawComment(String comment, ViewHolder holder){
            ListItemSingleView view = holder.getSingleView();
            view.initAsComment(comment);
            holder.container.addView(view);
        }

        @Override
        public int getItemCount() {
            return locationItems.size();
        }


        public void setList(List<SecondaryList> locationItems) {
            this.locationItems = locationItems;
            notifyItemRangeChanged(0, locationItems.size());
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
                Log.m("SecondaryListsView", "generating views: " + viewType);

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
        }
    }

}
