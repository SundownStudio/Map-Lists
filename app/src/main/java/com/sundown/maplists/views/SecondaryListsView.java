package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.EntryField;
import com.sundown.maplists.models.FieldType;
import com.sundown.maplists.models.PhotoField;
import com.sundown.maplists.models.SecondaryList;
import com.sundown.maplists.storage.DatabaseCommunicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



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


    public SecondaryListsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        emptyListText = (TextView) findViewById(R.id.emptyText);
        recyclerView = (RecyclerView) findViewById(R.id.locationList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterLocationItems(getContext());
        recyclerView.setAdapter(adapter);

    }

    public void setListAndListener(List<SecondaryList> items, AllListsListener listener){
        if (items.size() > 0)
            emptyListText.setVisibility(View.GONE);
        adapter.setList(items);
        this.listener = listener;

    }


    private class AdapterLocationItems extends RecyclerView.Adapter<AdapterLocationItems.ViewHolder> {

        private final static int TYPE_NO_IMAGE = 10;
        private final static int TYPE_ONE_IMAGE = 20;
        private final static int TYPE_TWO_IMAGE = 30;
        private final static int TYPE_THREE_IMAGE = 40;


        private LayoutInflater inflater;
        private Context context;
        private List<SecondaryList> locationItems = Collections.emptyList();

        private DatabaseCommunicator db;

        public AdapterLocationItems(Context context) {
            inflater = LayoutInflater.from(context);
            this.context = context;
            db = DatabaseCommunicator.getInstance();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType){
                case TYPE_ONE_IMAGE:
                    return new ViewHolder(inflater.inflate(R.layout.row_list_one_image, parent, false), viewType);

                case TYPE_TWO_IMAGE:
                    return new ViewHolder(inflater.inflate(R.layout.row_list_two_image, parent, false), viewType);

                case TYPE_THREE_IMAGE:
                    return new ViewHolder(inflater.inflate(R.layout.row_list_three_image, parent, false), viewType);

                default:
                    return new ViewHolder(inflater.inflate(R.layout.row_list_no_image, parent, false), viewType);
            }

        }


        private void fillTextViews(ViewHolder holder, SecondaryList locationItem){
            List<Field> list = locationItem.getValues();
            int counter = 0;
            String title;
            String text;
            boolean ratingSet = false;

            for (Field i: list){
                FieldType type = i.type;
                if (type != FieldType.FIELD_PHOTO && type != FieldType.FIELD_CHECKBOX){
                    EntryField entryField = (EntryField) i;

                    if (type == FieldType.FIELD_RATING && !ratingSet){

                        try {
                            Float f = Float.parseFloat(entryField.entry);
                            holder.field_rating.setRating(f);
                            holder.field_rating.setVisibility(VISIBLE);
                            ratingSet = true;
                        } catch (Exception e){
                            Log.m("could not parse: " + entryField.entry + " as float");
                        }

                    } else {

                        title = entryField.title;
                        if (counter != 0 && title != null && title.length() > 0) {
                            text = entryField.title + ": " + entryField.entry;
                        } else {
                            text = entryField.entry;
                        }
                        holder.textViews.get(counter++).setText(text);
                    }
                }
                if (counter == 4)
                    break;
            }


        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SecondaryList locationItem = locationItems.get(position);

            fillTextViews(holder, locationItem);

            int type = holder.getItemViewType();
            ArrayList<PhotoField> photos = locationItem.getPhotos();
            String documentId = locationItem.documentId;

            if (type != TYPE_NO_IMAGE) {
                getBitmap(documentId, photos.get(0).thumbName, holder.field_image);

                if (type >= TYPE_TWO_IMAGE) {
                    getBitmap(documentId, photos.get(1).thumbName, holder.field_image_two);
                    holder.field_image_two.setAlpha(.5f);
                }
                if (type >= TYPE_THREE_IMAGE) {
                    getBitmap(documentId, photos.get(2).thumbName, holder.field_image_three);
                    holder.field_image_three.setAlpha(.1f);
                    holder.field_image_two.bringToFront();
                }
                holder.field_image.bringToFront();
            }
        }

        @Override
        public int getItemCount() {
            return locationItems.size();
        }


        @Override
        public int getItemViewType(int position) {
            SecondaryList location = locationItems.get(position);
            ArrayList<PhotoField> photos = location.getPhotos(); //todo this may be slow.. maybe we should keep them in a list so dont need to do this each time..

            if (photos != null){
                int size = photos.size();
                if (size >= 3) {
                    return TYPE_THREE_IMAGE;
                } else if (size == 2) {
                    return TYPE_TWO_IMAGE;
                } else if (size == 1){
                    return TYPE_ONE_IMAGE;
                }
            }
            return TYPE_NO_IMAGE;
        }

        public void setList(List<SecondaryList> locationItems) {
            this.locationItems = locationItems;
            notifyItemRangeChanged(0, locationItems.size());
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView field_image;
            ImageView field_image_two;
            ImageView field_image_three;
            ArrayList<TextView> textViews = new ArrayList<>(4);
            RatingBar field_rating;


            public ViewHolder(View itemView, int viewType) {
                super(itemView);
                itemView.setOnClickListener(this);

                textViews.add((TextView) itemView.findViewById(R.id.field_title));
                textViews.add((TextView) itemView.findViewById(R.id.field_one));
                textViews.add((TextView) itemView.findViewById(R.id.field_two));
                textViews.add((TextView) itemView.findViewById(R.id.field_three));
                field_rating = (RatingBar) itemView.findViewById(R.id.field_rating);
                field_rating.setEnabled(false);
                field_image = (ImageView) itemView.findViewById(R.id.field_image);


                if (viewType >= TYPE_TWO_IMAGE) {
                    field_image_two = (ImageView) itemView.findViewById(R.id.field_image_two);
                }
                if (viewType >= TYPE_THREE_IMAGE){
                    field_image_three = (ImageView) itemView.findViewById(R.id.field_image_three);
                }

            }

            @Override
            public void onClick(View v) {
                Log.Toast(context, "getPosition: " + getPosition(), Log.TOAST_SHORT);
                listener.LocationListSelected(locationItems.get(getPosition()));
            }
        }

        private void getBitmap(String documentId, String thumbName, ImageView imageView){
            Bitmap thumb = null;

            if (thumbName != null && thumbName.length() > 0) {
                thumb = db.loadBitmap(documentId, thumbName);
            }

            if (thumb != null){
                imageView.setImageBitmap(thumb);
            }

        }


    }

}
