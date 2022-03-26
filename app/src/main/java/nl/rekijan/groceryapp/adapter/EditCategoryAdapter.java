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
import nl.rekijan.groceryapp.activities.EditCategoryActivity;
import nl.rekijan.groceryapp.activities.EditStoresActivity;
import nl.rekijan.groceryapp.helper.DialogHelper;

/**
 * Adapter for RecyclerView from {@link EditStoresActivity}
 *
 * @author Erik-Jan Krielen ej.krielen@gmail.com
 * @since 17-2-2022
 */
public class EditCategoryAdapter extends RecyclerView.Adapter<EditCategoryAdapter.CategoriesViewHolder> {

    private final ArrayList<String> categoryList;
    private final EditCategoryAdapter reference;
    private final AppExtension app;
    private final EditCategoryActivity activityReference;

    public EditCategoryAdapter(ArrayList<String> categoryList, AppExtension app, EditCategoryActivity activityReference) {
        this.categoryList = categoryList;
        this.reference = this;
        this.app = app;
        this.activityReference = activityReference;
    }

    /* ViewHolder region */
    public static class CategoriesViewHolder extends RecyclerView.ViewHolder {
        final CardView categoriesCardView;
        final TextView nameTextView;
        final Button editOrderUpButton;
        final Button editOrderDownButton;
        final ImageView removeButton;

        CategoriesViewHolder(View itemView) {
            super(itemView);
            categoriesCardView = itemView.findViewById(R.id.edit_order_category_cardView);
            nameTextView = itemView.findViewById(R.id.edit_category_name_textView);
            editOrderUpButton = itemView.findViewById(R.id.edit_category_order_up_button);
            editOrderDownButton = itemView.findViewById(R.id.edit_category_order_down_button);
            removeButton = itemView.findViewById(R.id.edit_category_delete_button);
        }
    }
    /* End of ViewHolder region */

    @NonNull
    @Override
    public CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_edit_categories, parent, false);
        return new CategoriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesViewHolder holder, int position) {
        //Get corresponding store name
        String category = categoryList.get(position);
        holder.nameTextView.setText(category);

        holder.editOrderUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int swapPosition = holder.getAdapterPosition()-1;
                if (swapPosition < 0) swapPosition = categoryList.size()-1;
                Collections.swap(categoryList, holder.getAdapterPosition(), swapPosition);
                notifyDataSetChanged();
            }
        });

        holder.editOrderDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int swapPosition = holder.getAdapterPosition()+1;
                if (swapPosition == categoryList.size()) swapPosition = 0;
                Collections.swap(categoryList, holder.getAdapterPosition(), swapPosition);
                notifyDataSetChanged();
            }
        });

        holder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().removeCategory(activityReference, reference, app, holder.nameTextView.getText().toString());
            }
        });

    }
    @Override
    public int getItemCount() {
        return categoryList.size();
    }

}