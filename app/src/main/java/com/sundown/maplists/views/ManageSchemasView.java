package com.sundown.maplists.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sundown.maplists.R;
import com.sundown.maplists.models.SchemaList;

import java.util.Collections;
import java.util.List;

/**
 * Created by Sundown on 8/26/2015.
 */
public class ManageSchemasView extends RelativeLayout {

    private RecyclerView recyclerView;
    private TextView emptyListText;
    private AdapterManageSchemas adapter;

    public ManageSchemasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        emptyListText = (TextView) findViewById(R.id.emptyText);
        recyclerView = (RecyclerView) findViewById(R.id.schemaList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdapterManageSchemas(getContext());
        recyclerView.setAdapter(adapter);

    }

    public void setList(List<SchemaList> schemaLists){
        if (schemaLists.size() > 0) emptyListText.setVisibility(View.GONE);
        adapter.setList(schemaLists);
    }

    private class AdapterManageSchemas extends RecyclerView.Adapter<AdapterManageSchemas.ViewHolder> {

        private LayoutInflater inflater;
        private List<SchemaList> schemaLists = Collections.emptyList();
        StringBuffer buffer = new StringBuffer();


        public AdapterManageSchemas(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public AdapterManageSchemas.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(inflater.inflate(R.layout.row_select_schema, parent, false));
        }

        @Override
        public void onBindViewHolder(AdapterManageSchemas.ViewHolder holder, int position) {
            SchemaList schemaList = schemaLists.get(position);

            holder.name.setText(schemaList.getSchemaName());
            buffer.setLength(0);
            holder.titles.setText(schemaList.getTitlesString(buffer));
            buffer.setLength(0);
            holder.types.setText(schemaList.getFieldTypesString(buffer));
        }

        @Override
        public int getItemCount() { return schemaLists.size(); }

        public void setList(List<SchemaList> schemaLists) {
            this.schemaLists = schemaLists;
            notifyItemRangeChanged(0, schemaLists.size());
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView name;
            TextView titles;
            TextView types;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.rowName);
                titles = (TextView) itemView.findViewById(R.id.rowTitles);
                types = (TextView) itemView.findViewById(R.id.rowTypes);
            }

            @Override
            public void onClick(View v) {

            }
        }
    }
}
