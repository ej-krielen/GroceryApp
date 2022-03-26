package nl.rekijan.groceryapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import nl.rekijan.groceryapp.AppExtension;
import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.activities.EditStoresActivity;
import nl.rekijan.groceryapp.helper.DialogHelper;

/**
 * Adapter for RecyclerView from {@link nl.rekijan.groceryapp.activities.EditStoresActivity}
 *
 * @author Erik-Jan Krielen ej.krielen@gmail.com
 * @since 17-2-2022
 */
public class EditStoresAdapter extends RecyclerView.Adapter<EditStoresAdapter.StoresViewHolder> {

    private final ArrayList<String> storeList;
    private final EditStoresAdapter reference;
    private final AppExtension app;
    private final EditStoresActivity activityReference;

    public EditStoresAdapter(ArrayList<String> storeList, AppExtension app, EditStoresActivity activityReference) {
        this.storeList = storeList;
        this.reference = this;
        this.app = app;
        this.activityReference = activityReference;
    }

    /* ViewHolder region */
    public static class StoresViewHolder extends RecyclerView.ViewHolder {
        final CardView storesCardView;
        final TextView nameTextView;
        final Button editOrderUpButton;
        final Button editOrderDownButton;
        final ImageView removeButton;

        StoresViewHolder(View itemView) {
            super(itemView);
            storesCardView = itemView.findViewById(R.id.edit_order_stores_cardView);
            nameTextView = itemView.findViewById(R.id.edit_stores_name_textView);
            editOrderUpButton = itemView.findViewById(R.id.edit_stores_order_up_button);
            editOrderDownButton = itemView.findViewById(R.id.edit_stores_order_down_button);
            removeButton = itemView.findViewById(R.id.edit_stores_delete_button);
        }
    }
    /* End of ViewHolder region */

    @NonNull
    @Override
    public StoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_edit_stores, parent, false);
        return new StoresViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StoresViewHolder holder, int position) {
        //Get corresponding store name
        String store = storeList.get(position);
        holder.nameTextView.setText(store);

        holder.editOrderUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int swapPosition = holder.getAdapterPosition()-1;
                if (swapPosition < 0) swapPosition = storeList.size()-1;
                Collections.swap(storeList, holder.getAdapterPosition(), swapPosition);
                notifyDataSetChanged();
            }
        });

        holder.editOrderDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int swapPosition = holder.getAdapterPosition()+1;
                if (swapPosition == storeList.size()) swapPosition = 0;
                Collections.swap(storeList, holder.getAdapterPosition(), swapPosition);
                notifyDataSetChanged();
            }
        });

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().removeStore(activityReference, reference, app, holder.nameTextView.getText().toString());
            }
        });

    }
    @Override
    public int getItemCount() {
        return storeList.size();
    }



}