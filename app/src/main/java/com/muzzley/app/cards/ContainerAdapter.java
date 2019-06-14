package com.muzzley.app.cards;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by caan on 30-09-2015.
 */
public class ContainerAdapter<T>  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Context context;
    final int layout;
    int rootContainer;
    protected List<T> data = new ArrayList<T>();

    public ContainerAdapter(Context context,int layout){
        this(context, layout, 0);
    }

    public ContainerAdapter(Context context,int layout,int rootContainer){
        this.context = context;
        this.layout = layout;
        this.rootContainer = rootContainer;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContainerHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false),rootContainer);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((ContainerAdapter.ContainerHolder)holder).container.setContainerData(data.get(position));
    }

    public void setData(List<T> data){
        this.data = data;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public static class ContainerHolder<T> extends RecyclerView.ViewHolder {

        private final Container<T> container;

        public ContainerHolder(View itemView, int rootContainer) {
            super(itemView);
            container = (Container<T>) (rootContainer > 0 ? itemView.findViewById(rootContainer) : itemView);
        }
    }


}