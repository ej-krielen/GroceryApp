package nl.rekijan.groceryapp.activities;

import static nl.rekijan.groceryapp.AppConstants.GROCERY_MODEL_TAG;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import nl.rekijan.groceryapp.AppExtension;
import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.adapter.CategoryListAdapter;
import nl.rekijan.groceryapp.helper.DialogHelper;
import nl.rekijan.groceryapp.models.GroceryModel;

public class MainActivity extends AppCompatActivity {

    private CategoryListAdapter adapter;
    private ExpandableListView groceryExpandableListView;
    private TextView emptyListLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppExtension app = (AppExtension) this.getApplicationContext();
        MainActivity reference = this;

        adapter = new CategoryListAdapter(this, app.getCategoryList(), app.getGroceryMap());
        groceryExpandableListView = (ExpandableListView) findViewById(R.id.groceries_expandableListView);
        groceryExpandableListView.setAdapter(adapter);
        expandAll();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.groceries_searchView);
        EditText searchEditText = (EditText) searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filterData(query);
                expandAll();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.filterData(query);
                expandAll();
                return true;
            }
        });

        groceryExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final GroceryModel groceryModel = (GroceryModel) adapter.getChild(groupPosition, childPosition);
                if (!TextUtils.isEmpty(groceryModel.getName())) {
                    Intent intent = new Intent(getApplicationContext(), GroceryActivity.class);
                    intent.putExtra(GROCERY_MODEL_TAG, groceryModel);
                    startActivity(intent);
                }

                return false;
            }
        });

        Button addCategoryButton = findViewById(R.id.add_category_button);
        addCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().addStoreOrCategoryDialog(reference, app, null, adapter, null, false);
            }
        });

        Button editCategoryButton = findViewById(R.id.edit_category_order_button);
        editCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(reference, EditCategoryActivity.class);
                startActivity(intent);
            }
        });

        Button addGroceryItem = findViewById(R.id.add_grocery_item_button);
        addGroceryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (app.getCategoryList().isEmpty())
                {
                    Toast.makeText(reference, reference.getString(R.string.error_add_item_no_categories), Toast.LENGTH_LONG).show();
                } else {
                    DialogHelper.getInstance().addGroceryItem(reference, app, adapter);
                }
            }
        });

        emptyListLabel = findViewById(R.id.groceries_empty_list_label);

        checkIfListIsEmpty(app);
    }

    public void checkIfListIsEmpty(AppExtension app) {
        if (app.getCategoryList().isEmpty())
        {
            groceryExpandableListView.setVisibility(View.INVISIBLE);
            emptyListLabel.setVisibility(View.VISIBLE);
            emptyListLabel.setText(R.string.error_no_categories);
        } else if (app.getGroceryList().isEmpty()) {
            groceryExpandableListView.setVisibility(View.INVISIBLE);
            emptyListLabel.setVisibility(View.VISIBLE);
            emptyListLabel.setText(R.string.error_no_items);
        } else {
            groceryExpandableListView.setVisibility(View.VISIBLE);
            emptyListLabel.setVisibility(View.INVISIBLE);
        }
    }

    private void expandAll() {
        int count = adapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            groceryExpandableListView.expandGroup(i);
        }
    }

    public void expandCategoryByName(String categoryName)
    {
        groceryExpandableListView.expandGroup(adapter.getGroupIdByName(categoryName));
    }

    @Override
    protected void onPause() {
        AppExtension app = (AppExtension) this.getApplicationContext();
        app.saveData();
        super.onPause();
    }

    @Override
    protected void onResume() {
        AppExtension app = (AppExtension) this.getApplicationContext();
        adapter = new CategoryListAdapter(this, app.getCategoryList(), app.getGroceryMap());
        groceryExpandableListView.setAdapter(adapter);
        checkIfListIsEmpty(app);
        expandAll();
        super.onResume();
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
            return super.onOptionsItemSelected(item);
        }
    }

    public void updateAdapter() {
        AppExtension app = (AppExtension) this.getApplicationContext();
        adapter.updateCategory(app.getCategoryList());
        adapter.updateGroceryModels(app.getGroceryMap());
    }
}