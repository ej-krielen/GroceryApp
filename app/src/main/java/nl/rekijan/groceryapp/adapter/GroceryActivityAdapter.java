package nl.rekijan.groceryapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.activities.GroceryActivity;
import nl.rekijan.groceryapp.helper.DialogHelper;
import nl.rekijan.groceryapp.models.GroceryModel;

/**
 * Adapter for RecyclerView from {@link GroceryActivity}
 *
 * @author Erik-Jan Krielen ej.krielen@gmail.com
 * @since 17-2-2022
 */
public class GroceryActivityAdapter extends RecyclerView.Adapter<GroceryActivityAdapter.StoresViewHolder> {

    private final GroceryModel groceryModel;
    private final ArrayList<String> storeList;
    private final Context context;
    private final GroceryActivityAdapter reference;

    public GroceryActivityAdapter(Context context, GroceryModel groceryModel, ArrayList<String> storeList) {
        this.context = context;
        this.groceryModel = groceryModel;
        this.storeList = storeList;
        reference = this;
    }

    /* ViewHolder region */
    public static class StoresViewHolder extends RecyclerView.ViewHolder {
        final CardView storesCardView;
        final ConstraintLayout background;
        final TextView nameTextView;
        final TextView priceTextView;
        final TextView unitsTextView;
        final TextView pricePerUnitTextView;

        StoresViewHolder(View itemView) {
            super(itemView);
            storesCardView = itemView.findViewById(R.id.stores_cardView);
            background = itemView.findViewById(R.id.sc_backgroundLayout);
            nameTextView = itemView.findViewById(R.id.sc_store_textView);
            priceTextView = itemView.findViewById(R.id.sc_price_textView);
            unitsTextView = itemView.findViewById(R.id.sc_unit_textView);
            pricePerUnitTextView = itemView.findViewById(R.id.sc_price_per_unit_textView);

        }
    }
    /* End of ViewHolder region */

    @NonNull
    @Override
    public StoresViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_stores, parent, false);
        return new StoresViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StoresViewHolder holder, int position) {
        //Get corresponding store name
        String store = storeList.get(position);
        holder.nameTextView.setText(store);
        holder.priceTextView.setText(groceryModel.getFormattedPrice(store, context.getString(R.string.no_data)));

        holder.priceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().changePrizeOrUnitsDialog(context, true, groceryModel, store, reference, null, null, null);
            }
        });

        holder.unitsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().changePrizeOrUnitsDialog(context, false, groceryModel, store, reference, null, null, null);
            }
        });

        holder.unitsTextView.setText(groceryModel.getFormattedUnit(store, context.getString(R.string.no_data)));
        holder.pricePerUnitTextView.setText(groceryModel.getFormattedUnitPrice(store, context.getString(R.string.no_data)));


        if (groceryModel.getUnitPrice(store) != -1 && groceryModel.getCheapestStore(storeList).equals(store)) {
            holder.storesCardView.setBackgroundColor(context.getResources().getColor(R.color.colorCheapest));
            holder.background.setBackgroundColor(context.getResources().getColor(R.color.colorCheapest));
        } else {
            holder.storesCardView.setBackgroundColor(context.getResources().getColor(R.color.cardBackgroundLight));
            holder.background.setBackgroundColor(context.getResources().getColor(R.color.cardBackgroundLight));
        }

    }
    @Override
    public int getItemCount() {
        return storeList.size();
    }
}