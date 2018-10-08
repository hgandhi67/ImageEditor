package com.app.image.editor.javaclasses.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.image.editor.R;
import com.app.image.editor.javaclasses.Interfaces.OnItemClickListener;
import com.app.image.editor.javaclasses.models.ImageModel;

import java.util.ArrayList;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder> {
    private Context context;
    private ArrayList<ImageModel> arrayList;
    private OnItemClickListener onItemClickListener;
    private LayoutInflater layoutInflater;

    public ImagesAdapter(Context context, ArrayList<ImageModel> arrayList, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.onItemClickListener = onItemClickListener;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = layoutInflater.inflate(R.layout.custom_image, parent, false);
        final ImagesViewHolder imagesViewHolder = new ImagesViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(view, imagesViewHolder.getAdapterPosition());
            }
        });
        return imagesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder holder, int position) {
        holder.imageView.setImageBitmap(arrayList.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ImagesViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ImagesViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
