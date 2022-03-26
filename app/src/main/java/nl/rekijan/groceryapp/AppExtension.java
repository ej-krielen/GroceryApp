package nl.rekijan.groceryapp;

import static nl.rekijan.groceryapp.AppConstants.GSON_CATEGORIES_TAG;
import static nl.rekijan.groceryapp.AppConstants.GSON_GROCERIES_TAG;
import static nl.rekijan.groceryapp.AppConstants.GSON_STORES_TAG;
import static nl.rekijan.groceryapp.AppConstants.SHARED_PREF_TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.rekijan.groceryapp.models.GroceryModel;

/**
 * Class for methods and variables that need to be app-wide
 *
 * @author Erik-Jan Krielen ej.krielen@gmail.com
 * @since 17-1-2022
 */
public class AppExtension extends Application implements PurchasesUpdatedListener {


    private static final String ITEM_SKU_FIVE = "fiveeurodonation";
    private static final String ITEM_SKU_TEN = "teneurodonation";

    private static final String LOG_TAG = "LOG_TAG";

    private BillingClient billingClient;

    // sku details are written during billing client setup and used later for purchase
    private SkuDetails fiveSkuDetails;
    private SkuDetails tenSkuDetails;

    private ArrayList<String> storeList = new ArrayList<>();
    private ArrayList<String> categoryList = new ArrayList<>();
    private ArrayList<GroceryModel> groceryList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        initializeData();
        setUpBillingClient();
    }

    public void initializeData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREF_TAG, Context.MODE_PRIVATE);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();

        Gson storesGson = new Gson();
        String storesJson = sharedPreferences.getString(GSON_STORES_TAG, null);
        if (!TextUtils.isEmpty(storesJson)) storeList = storesGson.fromJson(storesJson, type);

        Gson categoryGson = new Gson();
        String categoryJson = sharedPreferences.getString(GSON_CATEGORIES_TAG, null);
        if (!TextUtils.isEmpty(categoryJson))
            categoryList = categoryGson.fromJson(categoryJson, type);

        Type groceryType = new TypeToken<ArrayList<GroceryModel>>() {
        }.getType();
        Gson groceryGson = new Gson();
        String groceryJson = sharedPreferences.getString(GSON_GROCERIES_TAG, null);
        if (!TextUtils.isEmpty(groceryJson))
            groceryList = groceryGson.fromJson(groceryJson, groceryType);
    }

    public void saveData() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson storesGson = new Gson();
        String storesJson = storesGson.toJson(storeList);
        editor.putString(GSON_STORES_TAG, storesJson);

        Gson categoryGson = new Gson();
        String categoryJson = categoryGson.toJson(categoryList);
        editor.putString(GSON_CATEGORIES_TAG, categoryJson);

        Gson groceryGson = new Gson();
        String groceryJson = groceryGson.toJson(groceryList);
        editor.putString(GSON_GROCERIES_TAG, groceryJson);

        editor.apply();
    }

    public ArrayList<String> getStoreList() {
        return storeList;
    }

    public ArrayList<String> getCategoryList() {
        return categoryList;
    }

    public ArrayList<GroceryModel> getGroceryList() {
        return groceryList;
    }

    public HashMap<String, List<GroceryModel>> getGroceryMap() {
        HashMap<String, List<GroceryModel>> map = new HashMap<>();

        for (String category : categoryList) {
            ArrayList<GroceryModel> toAddList = new ArrayList<>();
            for (GroceryModel g : groceryList) {
                if (g.getCategory().equals(category)) {
                    toAddList.add(g);
                }
            }
            map.put(category, toAddList);
        }
        return map;
    }

    public GroceryModel getMatchingGrocery(GroceryModel groceryModel) {
        for (GroceryModel g : groceryList) {
            if (g.getName().equals(groceryModel.getName())) {
                return g;
            }
        }
        return groceryModel;
    }

    private void setUpBillingClient(){
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.e(LOG_TAG, "BILLING SETUP FINISHED RESPONSE CODE: "+billingResult.getResponseCode() + " : "+billingResult.getDebugMessage());
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // The BillingClient is ready. You can query in app products here.
                    List<String> skuList = new ArrayList<> ();
                    skuList.add(ITEM_SKU_FIVE);
                    skuList.add(ITEM_SKU_TEN);
                    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
                    billingClient.querySkuDetailsAsync(params.build(),
                            new SkuDetailsResponseListener() {
                                @Override
                                public void onSkuDetailsResponse(BillingResult billingResult,
                                                                 List<SkuDetails> skuDetailsList) {
                                    // Process the result.
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                                        for (SkuDetails skuDetails : skuDetailsList) {
                                            String sku = skuDetails.getSku();
                                            // I set the skudetails here when the billing client is setup so that it can be used later in the purchase flow
                                            if (ITEM_SKU_FIVE.equals(sku)) {
                                                fiveSkuDetails = skuDetails;
                                            } else if (ITEM_SKU_TEN.equals(sku)) {
                                                tenSkuDetails = skuDetails;
                                            }
                                        }
                                    }

                                }
                            });

                    // and query what they have already bought
                    queryPurchases();
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    // query the list of purchases in case you want to say thanks or give something
    public void queryPurchases(){
        if (billingClient!=null){
            List<Purchase> listPurchase = billingClient.queryPurchases(BillingClient.SkuType.INAPP).getPurchasesList();
            if (listPurchase!=null){
                for (Purchase purchase: listPurchase){
                    if (purchase.isAcknowledged() && (purchase.getSkus().contains(ITEM_SKU_FIVE) || purchase.getSkus().contains(ITEM_SKU_TEN))){
                        // they have the purchase so make it available
                        consumePurchase(purchase.getPurchaseToken(), purchase.getDeveloperPayload());
                    }
                }
            }

        } else {
            setUpBillingClient();
        }
    }

    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(BillingResult billingResult) {

            try{
                Toast.makeText(AppExtension.this, "Thank you for your tip", Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.

        }
    }

    // put whatever you want in this method - BUT REMEMBER TO ACKNOWLEDGE THE PURCHASE
    private void handlePurchase(Purchase purchase) {

        if ((purchase.getSkus().contains(ITEM_SKU_FIVE) || purchase.getSkus().contains(ITEM_SKU_TEN)) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {

            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
            }
        }
    }

    // call this if you want to consume a purchased item so that it can be repurchased
    private void consumePurchase(String purchaseToken, String developerPayload){
        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                Log.e(LOG_TAG, "CONSUMED PURCHASE");

            }
        };

        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchaseToken)
//                        .setDeveloperPayload(developerPayload)
                        .build();

        billingClient.consumeAsync(consumeParams, listener);
    }

    public void openTipDialog(Activity callingActivity) {
        //Build a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(callingActivity, R.style.AlertDialogStyle);
        //Add custom layout to dialog
        LayoutInflater inflater = LayoutInflater.from(callingActivity);
        final View alertDialogView = inflater.inflate(R.layout.dialog_tip, null);
        builder.setTitle(callingActivity.getString(R.string.tip_dialog_title));
        //Set button to close and cancel
        builder.setNegativeButton(callingActivity.getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        //Get views
        TextView fullFivePriceTextView = alertDialogView.findViewById(R.id.five_full_price_textView);
        TextView fullTenPriceTextView = alertDialogView.findViewById(R.id.ten_full_price_textView);
        Button fiveButton = alertDialogView.findViewById(R.id.five_btn);
        Button tenButton = alertDialogView.findViewById(R.id.ten_btn);

        if (fiveSkuDetails != null && !TextUtils.isEmpty(fiveSkuDetails.getPrice())) fullFivePriceTextView.setText(fiveSkuDetails.getPrice());
        if (tenSkuDetails != null && !TextUtils.isEmpty(tenSkuDetails.getPrice())) fullTenPriceTextView.setText(tenSkuDetails.getPrice());

        //Bind view to the dialog builder and create it
        builder.setView(alertDialogView);
        final AlertDialog dialog = builder.create();

        fiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (billingClient != null) {
                    queryPurchases();
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(fiveSkuDetails)
                            .build();
                    billingClient.launchBillingFlow(callingActivity, flowParams);
                    dialog.dismiss();
                }
            }
        });

        tenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryPurchases();
                if (billingClient != null) {
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(tenSkuDetails)
                            .build();
                    billingClient.launchBillingFlow(callingActivity, flowParams);
                    dialog.dismiss();

                }
            }
        });

        //Show the main dialog
        dialog.show();
    }

}