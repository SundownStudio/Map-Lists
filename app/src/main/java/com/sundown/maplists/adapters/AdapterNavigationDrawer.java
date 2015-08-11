package com.sundown.maplists.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sundown.maplists.MapLists;
import com.sundown.maplists.R;
import com.sundown.maplists.pojo.NavigationItem;

import java.util.Collections;
import java.util.List;

/**
 * Created by Sundown on 4/7/2015.
 */
public class AdapterNavigationDrawer extends RecyclerView.Adapter<AdapterNavigationDrawer.ViewHolder>{

    public interface NavigationClickListener{
        public void itemClicked(View view, int position);
    }


    private LayoutInflater inflator;
    private List<NavigationItem> list = Collections.emptyList(); //this just ensures we dont get null pointer exception anywhere
    private NavigationClickListener clickListener;



    public AdapterNavigationDrawer(List<NavigationItem> list, NavigationClickListener clickListener){
        inflator = LayoutInflater.from(MapLists.getContext());
        this.list = list;
        this.clickListener = clickListener;
    }


    /* called when RecyclerView needs a new RecyclerView.ViewHolder of the given type
       to represent an item. The new ViewHolder will be used to display items of the adapter
       using onBindViewHolder.. it will be reused to display different items
    */
    @Override
    public AdapterNavigationDrawer.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflator.inflate(R.layout.row_navigation, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(AdapterNavigationDrawer.ViewHolder holder, int i) {
        NavigationItem item = list.get(i);
        holder.icon.setImageResource(item.iconId);
        holder.text.setText(item.text);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView icon;
        TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            icon = (ImageView) itemView.findViewById(R.id.navigationItemIcon);
            text = (TextView) itemView.findViewById(R.id.navigationItemText);

        }

        @Override
        public void onClick(View v) {
            if (clickListener != null){
                clickListener.itemClicked(v, getPosition()); //always use getPosition(), dont keep a ref to position in onBind, for example, because it wont work
            }

        }
    }
}
