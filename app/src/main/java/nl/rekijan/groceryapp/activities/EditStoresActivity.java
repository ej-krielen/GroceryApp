package nl.rekijan.groceryapp.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import nl.rekijan.groceryapp.AppExtension;
import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.adapter.EditStoresAdapter;
import nl.rekijan.groceryapp.helper.DialogHelper;

public class EditStoresActivity extends AppCompatActivity {

    private RecyclerView storesRecyclerView;
    private TextView emptyListLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stores);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        AppExtension app = (AppExtension) this.getApplicationContext();
        EditStoresActivity reference = this;

        //Setup RecyclerView by binding the adapter to it.
        storesRecyclerView = findViewById(R.id.esa_edit_stores_recyclerView);
        storesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        storesRecyclerView.setLayoutManager(llm);

        EditStoresAdapter adapter = new EditStoresAdapter(app.getStoreList(), app, reference);
        storesRecyclerView.setAdapter(adapter);

        Button addStoreButton = findViewById(R.id.esa_add_store_button);
        addStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().addStoreOrCategoryDialog(reference, app, adapter, null, null, true);
            }
        });

        emptyListLabel = findViewById(R.id.esa_empty_list_label);
        checkIfListIsEmpty(app);
    }

    public void checkIfListIsEmpty(AppExtension app) {
        if (storesRecyclerView != null && emptyListLabel != null) {
            if (app.getStoreList().isEmpty())
            {
                storesRecyclerView.setVisibility(View.INVISIBLE);
                emptyListLabel.setVisibility(View.VISIBLE);
            } else {
                storesRecyclerView.setVisibility(View.VISIBLE);
                emptyListLabel.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_small, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_settings_about) {
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