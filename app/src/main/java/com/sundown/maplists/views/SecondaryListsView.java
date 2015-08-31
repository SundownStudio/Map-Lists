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
            LinkedHashMap<FieldType, LinkedList<String>> map = new LinkedHashMap<>();


            SecondaryList locationItem = locationItems.get(position);
            List<Field> list = locationItem.getValues();

            for (Field field: list) {
                FieldType type = field.type;
                switch (type) {
                    case SUBJECT: { //reserved field only one per item
                        EntryField entryField = (EntryField) field;
                        holder.subjectText.setText(entryField.entry);
                        break;
                    }
                    case NAME:
                    case PHONE:
                    case EMAIL:
                    case DATE:
                    case TIME:
                    case URL:
                    case PRICE: {
                        EntryField entryField = (EntryField) field;
                        LinkedList<String> entries = map.get(type);
                        if (entries == null)
                            entries = new LinkedList<>();
                        entries.add(entryField.entry);
                        map.put(type, entries);
                        break;
                    }
                }
            }

            Set set = map.entrySet();
            Iterator i = set.iterator();
            while(i.hasNext()) {
                Map.Entry me = (Map.Entry)i.next();
                FieldType type = (FieldType) me.getKey();
                LinkedList<String> entries = (LinkedList<String>) me.getValue();

                if (type == FieldType.DATE){
                    LinkedList<String> entries2 = map.get(FieldType.TIME);
                    drawDoubleViews(entries, entries2, FieldType.DATE, FieldType.TIME, holder);

                } else if (type == FieldType.TIME){
                    LinkedList<String> entries2 = map.get(FieldType.DATE);
                    drawDoubleViews(entries2, entries, FieldType.DATE, FieldType.TIME, holder);

                } else {
                    drawSingleViews(entries, type, holder);
                }

            }
        }

        private void drawSingleViews(LinkedList<String> entries, FieldType type, ViewHolder holder){
            if (entries != null) {
                while (entries.size() > 0) {
                    String entry = entries.pop();
                    if (holder.singleViews.size() == 0) {
                        break;
                    } else {
                        ListItemSingleView view = holder.singleViews.pop();
                        view.init(imageResources.get(type), entry);
                        holder.container.addView(view);
                    }
                }
            }
        }


        private void drawDoubleViews(LinkedList<String> entries1, LinkedList<String> entries2, FieldType type1, FieldType type2, ViewHolder holder){
            if (entries1 != null && entries2 != null) {
                while (entries1.size() > 0 && entries2.size() > 0 && holder.doubleViews.size() > 0) {
                    String entry1 = entries1.pop();
                    String entry2 = entries2.pop();
                    ListItemDoubleView view = holder.doubleViews.pop();
                    view.init(imageResources.get(type1), imageResources.get(type2), entry1, entry2);
                    holder.container.addView(view);
                }
            }
            drawSingleViews(entries1, type1, holder);
            drawSingleViews(entries2, type2, holder);

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

            TextView subjectText;
            LinearLayout container;
            Stack<ListItemSingleView> singleViews = new Stack<>();
            Stack<ListItemDoubleView> doubleViews = new Stack<>();


            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);

                subjectText = (TextView) itemView.findViewById(R.id.listItemSubject);
                container = (LinearLayout) itemView.findViewById(R.id.listItemContainer);

                for (int i = 0; i < 5; ++i){
                    singleViews.push((ListItemSingleView)inflater.inflate(R.layout.list_item_single_view, container, false));
                    doubleViews.push((ListItemDoubleView)inflater.inflate(R.layout.list_item_double_view, container, false));
                }

            }

            @Override
            public void onClick(View v) {
                listener.LocationListSelected(locationItems.get(getAdapterPosition()));
            }
        }
    }

}
