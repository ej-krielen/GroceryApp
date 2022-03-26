package nl.rekijan.groceryapp.adapter;

import static nl.rekijan.groceryapp.AppConstants.GROCERY_AMOUNT_TAG;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nl.rekijan.groceryapp.AppExtension;
import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.activities.ShoppingListActivity;
import nl.rekijan.groceryapp.helper.DialogHelper;
import nl.rekijan.groceryapp.models.GroceryModel;

/**
 * Adapter for RecyclerView from {@link nl.rekijan.groceryapp.activities.ShoppingListActivity}
 *
 * @author Erik-Jan Krielen ej.krielen@gmail.com
 * @since 17-2-2022
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SHOP = 0;
    private static final int TYPE_GROCERY_MODEL = 1;

    private final ShoppingListAdapter reference;
    private final ShoppingListActivity activityReference;
    private final AppExtension app;
    private final ArrayList<Object> list = new ArrayList<>();

    public ShoppingListAdapter(ShoppingListActivity activityReference, AppExtension app) {
        this.activityReference = activityReference;
        this.app = app;
        this.reference = this;

        ArrayList<GroceryModel> groceryList = app.getGroceryList();
        for (String store : app.getStoreList()) {
            List<GroceryModel> storeList = (groceryList.stream().filter(g -> g.getDataBundle().getDouble(store + GROCERY_AMOUNT_TAG) > 0).collect(Collectors.toList()));
            if (storeList.size() > 0) {
                list.add(store);
                list.addAll(storeList);
            }
        }
    }

    /* ViewHolder region */
    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        final CardView storesCardView;
        final TextView nameTextView;
        final ImageView deleteButton;

        ShopViewHolder(View itemView) {
            super(itemView);
            storesCardView = itemView.findViewById(R.id.shopping_shop_cardView);
            nameTextView = itemView.findViewById(R.id.sls_store_textView);
            deleteButton = itemView.findViewById(R.id.sls_delete_button);
        }
    }

    public static class GroceryModelViewHolder extends RecyclerView.ViewHolder {
        final CardView storesCardView;
        final ConstraintLayout background;
        final TextView nameTextView;
        final TextView priceTextView;
        final TextView unitsTextView;
        final TextView pricePerUnitTextView;
        final ImageView deleteButton;

        GroceryModelViewHolder(View itemView) {
            super(itemView);
            storesCardView = itemView.findViewById(R.id.shopping_grocery_cardView);
            background = itemView.findViewById(R.id.slg_backgroundLayout);
            nameTextView = itemView.findViewById(R.id.slg_store_textView);
            priceTextView = itemView.findViewById(R.id.slg_price_textView);
            unitsTextView = itemView.findViewById(R.id.slg_unit_textView);
            pricePerUnitTextView = itemView.findViewById(R.id.slg_price_per_unit_textView);
            deleteButton = itemView.findViewById(R.id.slg_delete_button);

        }
    }
    /* End of ViewHolder region */

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SHOP)
        {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_shopping_shop, parent, false);
            return new ShopViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_shopping_grocery, parent, false);
            return new GroceryModelViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        if (holder instanceof ShopViewHolder) {
            ShopViewHolder shopViewHolder = (ShopViewHolder) holder;

            if (list.get(position) instanceof String) {
                String store = (String) list.get(position);
                shopViewHolder.nameTextView.setText(store);
                shopViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogHelper.getInstance().removeStoreFromShoppingList(app, store, activityReference);
                    }
                });
            }
        }

        if (holder instanceof GroceryModelViewHolder) {
            GroceryModelViewHolder groceryModelViewHolder = (GroceryModelViewHolder) holder;

            if (list.get(position) instanceof GroceryModel) {

                GroceryModel groceryModel = (GroceryModel) list.get(position);
                String store = getStoreForPosition(position);

                groceryModelViewHolder.nameTextView.setText(activityReference.getString(R.string.sl_name_and_amount, groceryModel.getAmountString(store), groceryModel.getName()));
                groceryModelViewHolder.priceTextView.setText(groceryModel.getFormattedPrice(store, activityReference.getString(R.string.no_data)));
                groceryModelViewHolder.unitsTextView.setText(groceryModel.getFormattedUnit(store, activityReference.getString(R.string.no_data)));
                groceryModelViewHolder.pricePerUnitTextView.setText(groceryModel.getFormattedUnitPrice(store, activityReference.getString(R.string.no_data)));

                groceryModelViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogHelper.getInstance().removeItemFromShoppingList(groceryModel, store, list, holder.getAdapterPosition(), reference, activityReference);
                    }
                });
                
                groceryModelViewHolder.storesCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogHelper.getInstance().compareOnShoppingList(activityReference, groceryModel, store, activityReference.getResources().getString(R.string.no_data));
                    }
                });
            }
        }
    }

    private String getStoreForPosition(int position) {
        String store = "";
        int check = 1;
        while (TextUtils.isEmpty(store)) {
            if (list.get(position-check) instanceof String)
            {
                store = (String) list.get(position-check);
            } else {
                check++;
            }
        }
        return store;
    }

    @Override
    public int getItemViewType(int position) {
        if (isGroceryModel(position))
            return TYPE_GROCERY_MODEL;
        return TYPE_SHOP;
    }

    private boolean isGroceryModel(int position) {
        return list.get(position) instanceof GroceryModel;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}