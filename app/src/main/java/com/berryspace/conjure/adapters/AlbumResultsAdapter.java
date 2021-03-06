package com.berryspace.conjure.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.berryspace.conjure.R;
import com.berryspace.conjure.connectors.SelectedAlbumsInterface;
import com.berryspace.conjure.models.Album;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


public class AlbumResultsAdapter extends RecyclerView.Adapter<AlbumResultsAdapter.MyViewHolder>{
    private ArrayList<Album> mDataset;
    public Context context;
    private static final String TAG="AlbumSearchResultsAdapter";
    private HashMap<Integer, Boolean> selectedAlbums = new HashMap<>();
    private HashMap<String, String> selectedAlbumImages = new HashMap<>();
    SelectedAlbumsInterface selectedAlbumsInterface;
    private Boolean isSelectedAll = false;

    public void selectAll(Boolean status){
        isSelectedAll=status;
        notifyDataSetChanged();
        if(isSelectedAll) {
            selectedAlbums.replaceAll( (k,v)->v=true );
            mDataset.forEach(album -> {
                selectedAlbumImages.put(album.getId(), album.getImageUrl());
            });
             updateSelectedAlbums();
        } else {
            selectedAlbums.replaceAll( (k,v)->v=false );
            mDataset.forEach(album -> {
                selectedAlbumImages.remove(album.getId());
            });
            updateSelectedAlbums();
        }
     }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public TextView albumName;
        public ImageView imageView;
        public TextView year;
        public CheckBox selectorButton;

        public MyViewHolder(ConstraintLayout view) {
            super(view);
            cardView = (CardView) view.getViewById(R.id.search_album_view);
            imageView = (ImageView) cardView.getChildAt(0);
            albumName = (TextView) view.getViewById(R.id.search_album_name);
            year = (TextView) view.getViewById(R.id.search_album_year);
            selectorButton = (CheckBox) view.getViewById(R.id.album_selector_button);
        }
    }

    public AlbumResultsAdapter(ArrayList<Album> dataset, SelectedAlbumsInterface selectedAlbumCountInterface ) {
        mDataset = dataset;
        for (int i = 0; i < mDataset.size(); i++) {
            selectedAlbums.put(i, false);
        }
        this.selectedAlbumsInterface = selectedAlbumCountInterface;
    }

    @NonNull
    @Override
    public AlbumResultsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout view = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_album, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        context = view.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.albumName.setText(mDataset.get(position).getName());
        holder.year.setText(mDataset.get(position).getYear());
        Picasso.with(context).load(mDataset.get(position).getImageUrl()).into(holder.imageView);
        holder.selectorButton.setChecked(selectedAlbums.get(position));

        holder.itemView.setOnClickListener(v -> {
            Log.i(TAG, mDataset.get(position).getImageUrl());
            if (holder.selectorButton.isChecked()){
                selectedAlbums.put(position, false);
                selectedAlbumImages.remove(mDataset.get(position).getId());
                holder.selectorButton.setChecked(false);
             } else {
                selectedAlbums.put(position, true);
                selectedAlbumImages.put(mDataset.get(position).getId(), mDataset.get(position).getImageUrl());
                holder.selectorButton.setChecked(true);
             }
            updateSelectedAlbums();
         });

        holder.selectorButton.setOnClickListener(v -> {
            Log.i(TAG, mDataset.get(position).getImageUrl());
            if (holder.selectorButton.isChecked()){
                selectedAlbums.put(position, true);
                selectedAlbumImages.put(mDataset.get(position).getId(), mDataset.get(position).getImageUrl());
            } else {
                selectedAlbums.put(position, false);
                selectedAlbumImages.remove(mDataset.get(position).getId());
            }
            updateSelectedAlbums();
        });
    }

    void updateSelectedAlbums(){
        Map<Object, Object> filteredMap = selectedAlbums.entrySet().stream()
                .filter(Entry::getValue)
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        selectedAlbumsInterface.transferSelectedAlbumCount(filteredMap.size());
        selectedAlbumsInterface.transferSelectedAlbumImages(selectedAlbumImages);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}