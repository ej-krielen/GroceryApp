package nl.rekijan.groceryapp.models;

import static nl.rekijan.groceryapp.AppConstants.GROCERY_AMOUNT_TAG;
import static nl.rekijan.groceryapp.AppConstants.GROCERY_PRICE_TAG;
import static nl.rekijan.groceryapp.AppConstants.GROCERY_UNITS_TAG;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Logic for Grocery models
 *
 * @author Erik-Jan Krielen ej.krielen@gmail.com
 * @since 17-1-2022
 */
public class GroceryModel implements Parcelable {

    private String name;
    private String category;
    private String unitName;
    private double unitDivider;
    private Bundle dataBundle;
    private transient NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();

    public GroceryModel (String name, String category)
    {
        this.name = name;
        this.category = category;
        this.unitDivider = 1.0;
        this.dataBundle = new Bundle();
    }

    public GroceryModel (String name, String category, String unitName, double unitDivider, Bundle bundle)
    {
        this.name = name;
        this.category = category;
        this.unitName = unitName;
        this.unitDivider = unitDivider;
        this.dataBundle = bundle;
    }

    private NumberFormat getCurrencyFormatter()
    {
        if (currencyFormatter == null)
        {
            currencyFormatter = NumberFormat.getCurrencyInstance();
        }
        return currencyFormatter;
    }

    public String getFormattedPrice(String store, String noDataString)
    {
        if (dataBundle == null) return noDataString;
        return getCurrencyFormatter().format(dataBundle.getDouble(store+GROCERY_PRICE_TAG));
    }

    public String getFormattedUnit(String store, String noDataString) {
        if (dataBundle == null) return noDataString;
        double unit = dataBundle.getDouble(store+GROCERY_UNITS_TAG);
        if (unit == Math.floor(unit)) {
            return String.format(Locale.getDefault(), "%.0f", unit); //Format is: 0 places after decimal point
        } else {
            return Double.toString(unit);
        }
    }

    public String getFormattedUnitDivider() {
        if (unitDivider == Math.floor(unitDivider)) {
            return String.format(Locale.getDefault(), "%.0f", unitDivider);
        } else {
            return Double.toString(unitDivider);
        }
    }

    public boolean isUnitsLessThenDivider(String store) {
        if (dataBundle == null) return false;
        double unit = dataBundle.getDouble(store+GROCERY_UNITS_TAG);
        return unit < unitDivider;
    }

    public String getFormattedUnitPrice(String store, String noDataString) {
        if (dataBundle == null) return noDataString;
        double price = dataBundle.getDouble(store+GROCERY_PRICE_TAG);
        double unit = dataBundle.getDouble(store+GROCERY_UNITS_TAG);
        if (price == 0 || unit == 0) return noDataString;
        return getCurrencyFormatter().format(price/(unit/unitDivider));
    }

    public double getUnitPrice(String store) {
        if (dataBundle == null) return -1;
        double price = dataBundle.getDouble(store+GROCERY_PRICE_TAG);
        double unit = dataBundle.getDouble(store+GROCERY_UNITS_TAG);
        if (price == 0 || unit == 0) return -1;
        return price/(unit/unitDivider);
    }

    public String getCheapestStore(ArrayList<String> storeList)
    {
        String cheapestStore = storeList.get(0);
        double lowestUnitPrice = getUnitPrice(storeList.get(0));

        for (String store : storeList) {
            double unitPrice = getUnitPrice(store);
            if (unitPrice > -1 && unitPrice < lowestUnitPrice)
            {
                lowestUnitPrice = unitPrice;
                cheapestStore = store;
            }
        }
        return cheapestStore;
    }

    public void addAmount(String store) {
        dataBundle.putDouble(store+GROCERY_AMOUNT_TAG, dataBundle.getDouble(store+GROCERY_AMOUNT_TAG)+1);
    }

    public String getAmountString(String store)
    {
        double amount = dataBundle.getDouble(store+GROCERY_AMOUNT_TAG, 0.0);
        if (amount == Math.floor(amount)) {
            return String.format(Locale.getDefault(), "%.0f", amount);
        } else {
            return Double.toString(amount);
        }
    }

    public void removeFromShoppingList(String store) {
        dataBundle.putDouble(store+GROCERY_AMOUNT_TAG, 0);
    }

    public String getShoppingListString(ArrayList<String> storeList, String title, String row) {
        StringBuilder sb = new StringBuilder();
        boolean shouldDisplay = false;
        sb.append(title);
        for (String store : storeList) {
            double amount = dataBundle.getDouble((store+GROCERY_AMOUNT_TAG));
            if (amount > 0) {
                sb.append(String.format(row, getAmountString(store), store));
                shouldDisplay = true;
            }
        }

        return shouldDisplay ? sb.toString() : "";
    }

    protected GroceryModel(Parcel in) {
        name = in.readString();
        category = in.readString();
        unitName = in.readString();
        unitDivider = in.readDouble();
        dataBundle = in.readBundle();
    }

    public static final Creator<GroceryModel> CREATOR = new Creator<GroceryModel>() {
        @Override
        public GroceryModel createFromParcel(Parcel in) {
            return new GroceryModel(in);
        }

        @Override
        public GroceryModel[] newArray(int size) {
            return new GroceryModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(category);
        parcel.writeString(unitName);
        parcel.writeDouble(unitDivider);
        parcel.writeBundle(dataBundle);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public double getUnitDivider() {
        return unitDivider;
    }

    public void setUnitDivider(double unitDivider) {
        this.unitDivider = unitDivider;
    }

    public Bundle getDataBundle() {
        return dataBundle;
    }

    public void setDataBundle(Bundle dataBundle) {
        this.dataBundle = dataBundle;
    }

}
