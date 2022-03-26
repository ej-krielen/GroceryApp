package nl.rekijan.groceryapp.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import nl.rekijan.groceryapp.AppExtension;
import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.adapter.EditCategoryAdapter;
import nl.rekijan.groceryapp.helper.DialogHelper;

public class EditCategoryActivity extends AppCompatActivity {

    ViewFlipper viewFlipper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        AppExtension app = (AppExtension) this.getApplicationContext();
        EditCategoryActivity reference = this;

        //Setup RecyclerView by binding the adapter to it.
        RecyclerView categoryRecyclerView = findViewById(R.id.eca_edit_category_recyclerView);
        categoryRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        categoryRecyclerView.setLayoutManager(llm);

        EditCategoryAdapter adapter = new EditCategoryAdapter(app.getCategoryList(), app, reference);
        categoryRecyclerView.setAdapter(adapter);

        Button addStoreButton = findViewById(R.id.eca_add_category_button);
        addStoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogHelper.getInstance().addStoreOrCategoryDialog(reference, app, null, null, adapter, false);

            }
        });

        viewFlipper = findViewById(R.id.eca_viewFlipper);
        checkIfListIsEmpty(app);
    }

    public void checkIfListIsEmpty(AppExtension app) {
        viewFlipper.setDisplayedChild(app.getCategoryList().isEmpty() ? 0 : 1);
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