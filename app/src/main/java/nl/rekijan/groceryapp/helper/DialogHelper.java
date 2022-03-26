package nl.rekijan.groceryapp.helper;

import static nl.rekijan.groceryapp.AppConstants.GROCERY_MODEL_TAG;
import static nl.rekijan.groceryapp.AppConstants.GROCERY_PRICE_TAG;
import static nl.rekijan.groceryapp.AppConstants.GROCERY_UNITS_TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Currency;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import nl.rekijan.groceryapp.AppExtension;
import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.activities.EditCategoryActivity;
import nl.rekijan.groceryapp.activities.EditStoresActivity;
import nl.rekijan.groceryapp.activities.GroceryActivity;
import nl.rekijan.groceryapp.activities.MainActivity;
import nl.rekijan.groceryapp.activities.ShoppingListActivity;
import nl.rekijan.groceryapp.adapter.EditCategoryAdapter;
import nl.rekijan.groceryapp.adapter.EditStoresAdapter;
import nl.rekijan.groceryapp.adapter.CategoryListAdapter;
import nl.rekijan.groceryapp.adapter.ShoppingListAdapter;
import nl.rekijan.groceryapp.adapter.GroceryActivityAdapter;
import nl.rekijan.groceryapp.models.GroceryModel;

/**
 * Helper class to make simple dialogs
 *
 * @author Erik-Jan Krielen ej.krielen@gmail.com
 * @since 24-3-2021
 */
public class DialogHelper {

    private static DialogHelper sInstance = null;

    public static synchronized DialogHelper getInstance() {
        if (sInstance == null) {
            sInstance = new DialogHelper();
        }
        return sInstance;
    }

    public void simpleDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setMessage(message)
                .setTitle(title);
        builder.setNegativeButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Show dialog explaining more info is available on the website
     */
    public void aboutInfo(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setMessage(context.getString(R.string.dialog_about_info))
                .setTitle(context.getString(R.string.dialog_about_info_title));
        builder.setPositiveButton(context.getString(R.string.dialog_about_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent siteIntent =
                        new Intent("android.intent.action.VIEW",
                                Uri.parse("http://www.rekijan.nl/"));
                context.startActivity(siteIntent);
            }
        });
        builder.setNegativeButton(context.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void changePrizeOrUnitsDialog(Context context, boolean isPrize, GroceryModel groceryModel, String store, GroceryActivityAdapter reference, TextView changeTextView, TextView unitPriceTextView, ConstraintLayout dealBackground) {
        //Build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        //Add custom layout to dialog
        LayoutInflater inflater = LayoutInflater.from(context);
        final View alertDialogView = inflater.inflate(R.layout.dialog_change_with_edit_text, null);

        builder.setTitle(context.getString(isPrize ? R.string.change_prize_dialog_title : R.string.change_units_dialog_title));
        //Set button to close and cancel
        builder.setNegativeButton(context.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        String noDataString = context.getString(R.string.no_data);
        //Get views
        EditText changeEditText = alertDialogView.findViewById(R.id.dialog_change_editText);
        changeEditText.setText(isPrize ? groceryModel.getFormattedPrice(store, noDataString) : groceryModel.getFormattedUnit(store, noDataString));

        builder.setPositiveButton(context.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                double price = getDoubleFromEditText(changeEditText);
                if (isPrize) {
                    groceryModel.getDataBundle().putDouble(store + GROCERY_PRICE_TAG, price);
                } else {
                    groceryModel.getDataBundle().putDouble(store + GROCERY_UNITS_TAG, price);
                }
                reference.notifyDataSetChanged();
                if (changeTextView != null) {
                    if (isPrize) {
                        changeTextView.setText(groceryModel.getFormattedPrice(store, noDataString));
                    } else {
                        changeTextView.setText(groceryModel.getFormattedUnit(store, noDataString));
                    }
                }
                if (unitPriceTextView != null) {
                    unitPriceTextView.setText(groceryModel.getFormattedUnitPrice(store, context.getString(R.string.no_data)));
                }
                if (groceryModel.isUnitsLessThenDivider(store)) {
                    Toast.makeText(context, context.getString(R.string.error_low_unit_input), Toast.LENGTH_LONG).show();
                }
            }
        });

        //Bind view to the dialog builder and create it
        builder.setView(alertDialogView);
        final AlertDialog dialog = builder.create();

        //Show the main dialog
        dialog.show();
    }

    private double getDoubleFromEditText(EditText editText) {
        String input = editText.getText().toString();
        String currencySymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        input = input.replace(currencySymbol, "").replace(",", ".").replaceAll("\\s+", "");
        return !TextUtils.isEmpty(input) ? Double.parseDouble(input) : 0;
    }

    public void addStoreOrCategoryDialog(Activity reference, AppExtension app, EditStoresAdapter storesAdapter, CategoryListAdapter categoryListAdapter, EditCategoryAdapter categoryAdapter, boolean isStore) {
        //Build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(reference, R.style.AlertDialogStyle);
        //Add custom layout to dialog
        LayoutInflater inflater = LayoutInflater.from(reference);
        final View alertDialogView = inflater.inflate(R.layout.dialog_change_with_edit_text, null);

        builder.setTitle(reference.getString(isStore ? R.string.action_add_store : R.string.action_add_category));
        //Set button to close and cancel
        builder.setNegativeButton(reference.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //Get views
        EditText changeEditText = alertDialogView.findViewById(R.id.dialog_change_editText);
        changeEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        builder.setPositiveButton(reference.getString(R.string.dialog_ok), null);

        //Bind view to the dialog builder and create it
        builder.setView(alertDialogView);
        final AlertDialog dialog = builder.create();


        //Show the main dialog
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = changeEditText.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    Toast.makeText(reference, reference.getString(R.string.error_empty), Toast.LENGTH_LONG).show();
                } else if (!isNameUnique(input, isStore ? app.getStoreList() : app.getCategoryList())) {
                    Toast.makeText(reference, reference.getString(R.string.error_not_unique), Toast.LENGTH_LONG).show();
                } else {
                    if (isStore) {
                        app.getStoreList().add(input);
                        if (reference instanceof EditStoresActivity) {
                            ((EditStoresActivity) reference).checkIfListIsEmpty(app);
                        }
                        if (storesAdapter != null) storesAdapter.notifyDataSetChanged();
                    } else {
                        app.getCategoryList().add(input);
                        if (categoryListAdapter != null) {
                            categoryListAdapter.updateCategory(app.getCategoryList());
                            categoryListAdapter.notifyDataSetChanged();
                        }
                        if (reference instanceof MainActivity) {
                            ((MainActivity) reference).checkIfListIsEmpty(app);
                        }
                        if (categoryAdapter != null) {
                            categoryAdapter.notifyDataSetChanged();
                        }
                    }
                    dialog.dismiss();
                }

            }
        });

    }

    public void removeStore(EditStoresActivity activityReference, EditStoresAdapter adapter, AppExtension app, String item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityReference, R.style.AlertDialogStyle);
        builder.setMessage(item)
                .setTitle(activityReference.getResources().getString(R.string.dialog_delete));
        builder.setNegativeButton(activityReference.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(activityReference.getResources().getString(R.string.dialog_confirm_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                app.getStoreList().remove(item);
                activityReference.checkIfListIsEmpty(app);
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void removeCategory(EditCategoryActivity activityReference, EditCategoryAdapter adapter, AppExtension app, String item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityReference, R.style.AlertDialogStyle);
        builder.setMessage(item + activityReference.getResources().getString(R.string.delete_all_sub_categories))
                .setTitle(activityReference.getResources().getString(R.string.dialog_delete));
        builder.setNegativeButton(activityReference.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(activityReference.getResources().getString(R.string.dialog_confirm_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                app.getGroceryList().removeIf(g -> g.getCategory().equals(item));
                app.getCategoryList().remove(item);
                activityReference.checkIfListIsEmpty(app);
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void addGroceryItem(MainActivity activityReference, AppExtension app, CategoryListAdapter adapter) {

        //Build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activityReference, R.style.AlertDialogStyle);
        //Add custom layout to dialog
        LayoutInflater inflater = LayoutInflater.from(activityReference);
        final View alertDialogView = inflater.inflate(R.layout.dialog_add_grocery_item, null);

        builder.setTitle(activityReference.getString(R.string.action_add_grocery));
        //Set button to close and cancel
        builder.setNegativeButton(activityReference.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //Get views
        EditText nameEditText = alertDialogView.findViewById(R.id.agm_name_editText);
        Spinner categorySpinner = alertDialogView.findViewById(R.id.agm_category_spinner);
        EditText unitEditText = alertDialogView.findViewById(R.id.agm_unit_editText);
        EditText unitDividerEditText = alertDialogView.findViewById(R.id.agm_unitDivider_editText);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(activityReference, android.R.layout.simple_spinner_item, app.getCategoryList());
        categorySpinner.setAdapter(categoryAdapter);

        builder.setPositiveButton(activityReference.getString(R.string.dialog_ok), null);

        //Bind view to the dialog builder and create it
        builder.setView(alertDialogView);
        final AlertDialog dialog = builder.create();

        //Show the main dialog
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameInput = nameEditText.getText().toString();
                String categoryInput = categorySpinner.getSelectedItem().toString();
                if (TextUtils.isEmpty(nameInput)) {
                    Toast.makeText(activityReference, activityReference.getString(R.string.error_empty), Toast.LENGTH_LONG).show();
                } else if (!isNameUnique(nameInput, app.getGroceryList().stream().filter(g -> g.getCategory().equals(categoryInput)).map(GroceryModel::getName).collect(Collectors.toCollection(ArrayList<String>::new)))) {
                    Toast.makeText(activityReference, activityReference.getString(R.string.error_not_unique), Toast.LENGTH_LONG).show();
                } else {
                    if (adapter != null) {
                        GroceryModel groceryModel = new GroceryModel(nameInput, categoryInput);
                        String unit = unitEditText.getText().toString();
                        if (TextUtils.isEmpty(unit))
                            unit = activityReference.getString(R.string.label_unit);
                        groceryModel.setUnitName(unit);

                        String unitDivider = unitDividerEditText.getText().toString();
                        if (!TextUtils.isEmpty(unitDivider) && TextUtils.isDigitsOnly(unitDivider)) {
                            double input = Double.parseDouble(unitDivider);
                            if (input > 1) {
                                groceryModel.setUnitDivider(input);
                            }
                        }
                        app.getGroceryList().add(groceryModel);
                        adapter.updateGroceryModels(app.getGroceryMap());
                        adapter.notifyDataSetChanged();
                        activityReference.checkIfListIsEmpty(app);
                        activityReference.expandCategoryByName(categoryInput);
                        dialog.dismiss();
                    }
                }
            }
        });

    }

    public void editGroceryItem(GroceryActivity activityReference, AppExtension app, GroceryModel groceryModel) {
        //Build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activityReference, R.style.AlertDialogStyle);
        //Add custom layout to dialog
        LayoutInflater inflater = LayoutInflater.from(activityReference);
        final View alertDialogView = inflater.inflate(R.layout.dialog_edit_grocery_item, null);

        builder.setTitle(activityReference.getString(R.string.egd_edit_grocery));
        //Set button to close and cancel
        builder.setNegativeButton(activityReference.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //Get views
        EditText nameEditText = alertDialogView.findViewById(R.id.egm_name_editText);
        Spinner categorySpinner = alertDialogView.findViewById(R.id.egm_category_spinner);
        EditText unitEditText = alertDialogView.findViewById(R.id.egm_unit_editText);
        EditText unitDividerEditText = alertDialogView.findViewById(R.id.egm_unitDivider_editText);

        String startingName = groceryModel.getName();
        nameEditText.setText(startingName);
        unitEditText.setText(groceryModel.getUnitName());
        unitDividerEditText.setText(groceryModel.getFormattedUnitDivider());

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(activityReference, android.R.layout.simple_spinner_item, app.getCategoryList());
        categorySpinner.setAdapter(categoryAdapter);

        String categoryName = groceryModel.getCategory();
        for (int i = 0; i < categoryAdapter.getCount(); i++) {
            if (categorySpinner.getItemAtPosition(i).toString().equals(categoryName)) {
                categorySpinner.setSelection(i);
                break;
            }
        }

        builder.setPositiveButton(activityReference.getString(R.string.dialog_ok), null);

        //Bind view to the dialog builder and create it
        builder.setView(alertDialogView);
        final AlertDialog dialog = builder.create();

        //Show the main dialog
        dialog.show();

        ViewFlipper viewFlipper = alertDialogView.findViewById(R.id.egm_viewFlipper);
        Button deleteButton = alertDialogView.findViewById(R.id.egm_delete_button);
        Button confirmButton = alertDialogView.findViewById(R.id.egm_delete_confirm_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewFlipper.showNext();
            }
        });

        viewFlipper.getInAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (GroceryModel g : app.getGroceryList()) {
                            if (g.getName().equals(startingName))
                            {
                                app.getGroceryList().remove(g);
                                break;
                            }
                        }
                        dialog.dismiss();
                        activityReference.finish();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameInput = nameEditText.getText().toString();
                String categoryInput = categorySpinner.getSelectedItem().toString();
                String unitInput = unitEditText.getText().toString();
                String unitDividerInput = unitDividerEditText.getText().toString();

                if (TextUtils.isEmpty(nameInput)) {
                    Toast.makeText(activityReference, activityReference.getString(R.string.error_empty), Toast.LENGTH_LONG).show();
                } else if (!nameInput.equals(startingName) && !isNameUnique(nameInput, app.getGroceryList().stream().filter(g -> g.getCategory().equals(categoryInput)).map(GroceryModel::getName).collect(Collectors.toCollection(ArrayList<String>::new)))) {
                    Toast.makeText(activityReference, activityReference.getString(R.string.error_not_unique), Toast.LENGTH_LONG).show();
                } else {
                    if (!groceryModel.getName().equals(nameInput)) groceryModel.setName(nameInput);
                    if (!groceryModel.getCategory().equals(categoryInput))
                        groceryModel.setCategory(categoryInput);
                    if (!groceryModel.getUnitName().equals(unitInput)) {
                        groceryModel.setUnitName(TextUtils.isEmpty(unitInput) ? activityReference.getString(R.string.label_unit) : unitInput);
                    }
                    if (!TextUtils.isEmpty(unitDividerInput) && TextUtils.isDigitsOnly(unitDividerInput))
                    {
                        double input = Double.parseDouble(unitDividerInput);
                        if (groceryModel.getUnitDivider() != input) groceryModel.setUnitDivider(input);
                    }

                    for (int i = 0; i < app.getGroceryList().size(); i++) {
                        if (app.getGroceryList().get(i).getName().equals(startingName))
                        {
                            app.getGroceryList().set(i, groceryModel);
                            break;
                        }
                    }

                    Intent intent = new Intent(app, GroceryActivity.class);
                    intent.putExtra(GROCERY_MODEL_TAG, groceryModel);
                    activityReference.startActivity(intent);
                    activityReference.finish();
                    dialog.dismiss();
                }
            }
        });

    }

    private boolean isNameUnique(String input, ArrayList<String> storeList) {
        for (String s : storeList) {
            if (s.equals(input)) return false;
        }
        return true;
    }


    public void removeStoreFromShoppingList(AppExtension app, String store, ShoppingListActivity activityReference) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activityReference, R.style.AlertDialogStyle);
        builder.setMessage(store + activityReference.getResources().getString(R.string.delete_all_sub_categories))
                .setTitle(activityReference.getResources().getString(R.string.sl_dialog_delete_shop));
        builder.setNegativeButton(activityReference.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(activityReference.getResources().getString(R.string.dialog_confirm_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (GroceryModel g: app.getGroceryList()) {
                    g.removeFromShoppingList(store);
                    activityReference.recreate();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void removeItemFromShoppingList(GroceryModel groceryModel, String store, ArrayList<Object> list, int adapterPosition, ShoppingListAdapter reference, ShoppingListActivity activityReference) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityReference, R.style.AlertDialogStyle);
        builder.setTitle(activityReference.getResources().getString(R.string.sl_delete_item, groceryModel.getName()));
        builder.setNegativeButton(activityReference.getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton(activityReference.getResources().getString(R.string.dialog_confirm_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                groceryModel.removeFromShoppingList(store);
                list.remove(adapterPosition);
                reference.notifyDataSetChanged();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void selectStoreToAddToList(AppExtension app, GroceryActivity activityReference, GroceryModel groceryModel) {
        //Build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activityReference, R.style.AlertDialogStyle);
        //Add custom layout to dialog
        LayoutInflater inflater = LayoutInflater.from(activityReference);
        final View alertDialogView = inflater.inflate(R.layout.dialog_select_store, null);

        builder.setTitle(activityReference.getString(R.string.sl_dialog_title));
        //Set button to close and cancel
        builder.setNegativeButton(activityReference.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //Get views
        TextView messageTextView = alertDialogView.findViewById(R.id.sl_message_label);
        Spinner storeSpinner = alertDialogView.findViewById(R.id.sl_store_spinner);

        messageTextView.setText(activityReference.getString(R.string.sl_dialog_message, groceryModel.getName()));

        ArrayAdapter<String> storeAdapter = new ArrayAdapter<String>(activityReference, android.R.layout.simple_spinner_item, app.getStoreList());
        storeSpinner.setAdapter(storeAdapter);

        builder.setPositiveButton(activityReference.getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String storeInput = storeSpinner.getSelectedItem().toString();
                groceryModel.addAmount(storeInput);
                activityReference.updateShoppingListTextView(app.getStoreList(), groceryModel);
            }
        });

        //Bind view to the dialog builder and create it
        builder.setView(alertDialogView);
        final AlertDialog dialog = builder.create();

        //Show the main dialog
        dialog.show();
    }


    public void compareOnShoppingList(ShoppingListActivity activityReference, GroceryModel groceryModel, String store, String noData) {
        //Build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(activityReference, R.style.AlertDialogStyle);
        //Add custom layout to dialog
        LayoutInflater inflater = LayoutInflater.from(activityReference);
        final View alertDialogView = inflater.inflate(R.layout.dialog_store_compare, null);

        builder.setTitle(activityReference.getString(R.string.slc_dialog_title));
        //Set button to close and cancel
        builder.setNegativeButton(activityReference.getString(R.string.dialog_done), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //Get views
        TextView nameTextView = alertDialogView.findViewById(R.id.slc_name_textView);
        TextView priceTextView = alertDialogView.findViewById(R.id.slc_price_textView);
        TextView unitTextView = alertDialogView.findViewById(R.id.slc_unit_textView);
        TextView unitPriceTextView = alertDialogView.findViewById(R.id.slc_price_per_unit_textView);

        nameTextView.setText(groceryModel.getName());
        priceTextView.setText(groceryModel.getFormattedPrice(store, noData));
        unitTextView.setText(groceryModel.getFormattedUnit(store, noData));
        unitPriceTextView.setText(groceryModel.getFormattedUnitPrice(store, noData));

        EditText priceEditText = alertDialogView.findViewById(R.id.slcc_price_editText);
        EditText unitEditText = alertDialogView.findViewById(R.id.slcc_unit_editText);
        TextView unitPriceCompareTextView = alertDialogView.findViewById(R.id.slcc_price_per_unit_textView);

        ConstraintLayout background = alertDialogView.findViewById(R.id.slc_backgroundLayout);
        ConstraintLayout backgroundCompare = alertDialogView.findViewById(R.id.slcc_backgroundLayout);

        priceEditText.setText(groceryModel.getFormattedPrice(store, noData));
        unitEditText.setText(groceryModel.getFormattedUnit(store, noData));
        updateUnitPriceCompareTextView(activityReference, unitPriceCompareTextView, priceEditText, unitEditText, groceryModel, noData, store, background, backgroundCompare);

        priceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUnitPriceCompareTextView(activityReference, unitPriceCompareTextView, priceEditText, unitEditText, groceryModel, noData, store, background, backgroundCompare);
            }
        });

        unitEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUnitPriceCompareTextView(activityReference, unitPriceCompareTextView, priceEditText, unitEditText, groceryModel, noData, store, background, backgroundCompare);
            }
        });

        //Bind view to the dialog builder and create it
        builder.setView(alertDialogView);
        final AlertDialog dialog = builder.create();

        //Show the main dialog
        dialog.show();
    }

    private void updateUnitPriceCompareTextView(ShoppingListActivity activityReference, TextView unitPriceCompareTextView, EditText priceEditText, EditText unitEditText, GroceryModel groceryModel, String noData, String store, ConstraintLayout background, ConstraintLayout backgroundCompare) {
        double price = getDoubleFromEditText(priceEditText);
        double unit = getDoubleFromEditText(unitEditText);
        if (price == 0 || unit == 0) {
            unitPriceCompareTextView.setText(noData);
        } else {
            double unitPriceCompare = price/(unit/groceryModel.getUnitDivider());
            unitPriceCompareTextView.setText(NumberFormat.getCurrencyInstance().format(unitPriceCompare));

            int green = activityReference.getResources().getColor(R.color.colorCheapest);
            int white = activityReference.getResources().getColor(R.color.cardBackgroundLight);

            background.setBackgroundColor(unitPriceCompare > groceryModel.getUnitPrice(store) ? green : white);
            backgroundCompare.setBackgroundColor(groceryModel.getUnitPrice(store) > unitPriceCompare ? green : white);
        }
    }
}