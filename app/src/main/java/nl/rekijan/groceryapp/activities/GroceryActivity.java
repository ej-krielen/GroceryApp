package nl.rekijan.groceryapp.activities;

import static nl.rekijan.groceryapp.AppConstants.GROCERY_MODEL_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import nl.rekijan.groceryapp.AppExtension;
import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.adapter.GroceryActivityAdapter;
import nl.rekijan.groceryapp.helper.DialogHelper;
import nl.rekijan.groceryapp.models.GroceryModel;

public class GroceryActivity extends AppCompatActivity {

    private TextView shoppingListTextView;
    private GroceryModel groceryModel;
    private RecyclerView storesRecyclerView;
    private TextView emptyListLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        AppExtension app = (AppExtension) this.getApplicationContext();
        GroceryActivity reference = this;

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        groceryModel = app.getMatchingGrocery(intent.getParcelableExtra(GROCERY_MODEL_TAG));

        TextView nameTextView = findViewById(R.id.ga_name_textView);
        nameTextView.setText(groceryModel.getName());

        shoppingListTextView = findViewById(R.id.ga_shopping_list_overview_textView);
        updateShoppingListTextView(app.getStoreList(), groceryModel);

        String unitName = groceryModel.getUnitName();
        if (!TextUtils.isEmpty(unitName))
        {
            TextView unitTextView = findViewById(R.id.ga_unit_label);
            unitTextView.setText(groceryModel.getUnitName());
        }

        double unitDivider = groceryModel.getUnitDivider();
        if (unitDivider > 0)
        {
            String unitPriceLabel = String.format(getString(R.string.unit_price), groceryModel.getUnitDivider(), groceryModel.getUnitName());

            TextView unitPriceTextView = findViewById(R.id.ga_price_per_unit_label);
            unitPriceTextView.setText(unitPriceLabel);
        }

        //Setup RecyclerView by binding the adapter to it.
        storesRecyclerView = findViewById(R.id.ga_stores_recyclerView);
        storesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        storesRecyclerView.setLayoutManager(llm);

        GroceryActivityAdapter adapter = new GroceryActivityAdapter(reference, groceryModel, app.getStoreList());
        storesRecyclerView.setAdapter(adapter);

        Button addCheapestButton = findViewById(R.id.ga_add_cheapest_button);
        addCheapestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groceryModel.addAmount(groceryModel.getCheapestStore(app.getStoreList()));
                updateShoppingListTextView(app.getStoreList(), groceryModel);
            }
        });

        Button addSelectStore = findViewById(R.id.ga_pick_to_add_button);
        addSelectStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().selectStoreToAddToList(app, reference, groceryModel);
            }
        });

        ImageView editImageView = findViewById(R.id.ga_edit_grocery_imageView);
        editImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().editGroceryItem(reference, app, groceryModel);
            }
        });

        emptyListLabel = findViewById(R.id.ga_list_empty_label);

        checkIfListIsEmpty(app);

        updateShoppingListTextView(app.getStoreList(), groceryModel);
    }

    private void checkIfListIsEmpty(AppExtension app) {
        if (app.getStoreList().isEmpty())
        {
            storesRecyclerView.setVisibility(View.INVISIBLE);
            emptyListLabel.setVisibility(View.VISIBLE);
        } else
        {
            storesRecyclerView.setVisibility(View.VISIBLE);
            emptyListLabel.setVisibility(View.INVISIBLE);
        }
    }

    public void updateShoppingListTextView(ArrayList<String> storeList, GroceryModel groceryModel) {
        shoppingListTextView.setText(groceryModel.getShoppingListString(storeList,this.getString(R.string.sl_ga_header), this.getString(R.string.sl_ga_row)));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        AppExtension app = (AppExtension) this.getApplicationContext();

        if (groceryModel != null)
            updateShoppingListTextView(app.getStoreList(), groceryModel);
        if (storesRecyclerView != null && emptyListLabel != null)
            checkIfListIsEmpty(app);
        if (storesRecyclerView != null) {
            GroceryActivityAdapter adapter = new GroceryActivityAdapter(this, groceryModel, app.getStoreList());
            storesRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_shopping_list_pref) {
            Intent intent = new Intent(this, ShoppingListActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_stores_pref) {
            Intent intent = new Intent(this, EditStoresActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_settings_about) {
            DialogHelper.getInstance().aboutInfo(this);
            return true;
        } else if (itemId == R.id.action_settings_tip) {
            AppExtension app = (AppExtension) getApplication();
            app.queryPurchases();
            app.openTipDialog(this);
            return true;
        } else {
            finish();
            return true;
        }
    }

    @Override
    protected void onPause() {
        AppExtension app = (AppExtension) this.getApplicationContext();
        app.saveData();
        super.onPause();
    }
}