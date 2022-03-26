package nl.rekijan.groceryapp.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import nl.rekijan.groceryapp.AppExtension;
import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.adapter.ShoppingListAdapter;
import nl.rekijan.groceryapp.helper.DialogHelper;

public class ShoppingListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        AppExtension app = (AppExtension) this.getApplicationContext();
        ShoppingListActivity reference = this;

        //Setup RecyclerView by binding the adapter to it.
        RecyclerView shoppingListRecyclerView = findViewById(R.id.asl_shopping_list_recyclerView);
        shoppingListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        shoppingListRecyclerView.setLayoutManager(llm);

        ShoppingListAdapter adapter = new ShoppingListAdapter(reference, app);
        shoppingListRecyclerView.setAdapter(adapter);

        TextView emptyListLabel = findViewById(R.id.asl_empty_list_textView);

        if (adapter.getItemCount() == 0)
        {
            shoppingListRecyclerView.setVisibility(View.INVISIBLE);
            emptyListLabel.setVisibility(View.VISIBLE);
        }
        else
        {
            shoppingListRecyclerView.setVisibility(View.VISIBLE);
            emptyListLabel.setVisibility(View.INVISIBLE);
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