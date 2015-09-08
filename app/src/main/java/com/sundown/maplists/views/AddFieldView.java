package com.sundown.maplists.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sundown.maplists.R;
import com.sundown.maplists.logging.Log;
import com.sundown.maplists.models.Field;
import com.sundown.maplists.models.PhotoField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sundown.maplists.models.FieldType.PHOTO;

/**
 * Created by Sundown on 5/20/2015.
 */
public class AddFieldView extends LinearLayout {

    public interface FieldSelector{
        void addNewField(Field field);
    }

    private AdapterSelectField adapter;
    private RecyclerView recyclerView;
    private ArrayList<Integer> imageResources;

    public AddFieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        recyclerView = (RecyclerView) findViewById(R.id.selectFieldList);
        recyclerView.addItemDecoration(new SpacesItemDecoration(1));

    }


    public void setAdapter(final FieldSelector listener, final List<Field> list, final ArrayList<Integer> imageResources){
        this.imageResources = imageResources;
        adapter = new AdapterSelectField(listener, list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    protected void onDetachedFromWindow() {
        imageResources.clear();
        super.onDetachedFromWindow();
    }

    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if(parent.getChildPosition(view) == 0)
                outRect.top = space;
        }
    }

    private class AdapterSelectField extends RecyclerView.Adapter<AdapterSelectField.ViewHolder>{

        private LayoutInflater inflater;
        private List<Field> fields = Collections.emptyList();
        private FieldSelector listener;


        private AdapterSelectField(final FieldSelector listener, final List<Field> fields){
            inflater = LayoutInflater.from(getContext());
            this.fields = fields;
            this.listener = listener;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.row_select_field, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AdapterSelectField.ViewHolder holder, int position) {
            Field field = fields.get(position);
            holder.title.setText(field.getTitle());
            try {
                holder.image.setImageResource(imageResources.get(position)); //we don't have images for all screensizes yet so will use default if fails
            } catch (Exception e){
                Log.e(e);
            }
        }

        @Override
        public int getItemCount() {
            return fields.size();
        }



        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            TextView title;
            ImageView image;


            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                image = (ImageView) itemView.findViewById(R.id.rowImage);
                title = (TextView) itemView.findViewById(R.id.rowTitle);


            }

            @Override
            public void onClick(View v) {
                if (listener != null){
                    Field field = fields.get(getAdapterPosition ());
                    if (field.getType() == PHOTO){
                        field = new PhotoField(false);
                    }
                    listener.addNewField(field);
                }
            }
        }
    }
}
