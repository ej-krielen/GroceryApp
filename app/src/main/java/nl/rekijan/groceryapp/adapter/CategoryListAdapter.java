package nl.rekijan.groceryapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.rekijan.groceryapp.R;
import nl.rekijan.groceryapp.activities.MainActivity;
import nl.rekijan.groceryapp.models.GroceryModel;

/**
 * Adapter to handle the ExpandableListView in {@link MainActivity}
 *
 * @author Erik-Jan Krielen ej.krielen@gmail.com
 * @since 16-2-2022
 */
public class CategoryListAdapter extends BaseExpandableListAdapter {

    private final Context mContext;
    private List<String> mCategoryListDataHeader;
    private List<String> mOriginalCategoryListHeader;
    private HashMap<String, List<GroceryModel>> mGroceryListDataChild;
    private HashMap<String, List<GroceryModel>> mOriginalGroceryListChild;

    public CategoryListAdapter(Context context, List<String> listDataHeader,
                               HashMap<String, List<GroceryModel>> listDataChild) {
        mContext = context;
        mCategoryListDataHeader = new ArrayList<>(listDataHeader);
        mOriginalCategoryListHeader = new ArrayList<>(listDataHeader);
        mGroceryListDataChild = new HashMap<>(listDataChild);
        mOriginalGroceryListChild = new HashMap<>(listDataChild);
    }

    public void updateCategory(List<String> listDataHeader) {

        mCategoryListDataHeader = new ArrayList<>(listDataHeader);
        mOriginalCategoryListHeader = new ArrayList<>(listDataHeader);
    }

    public void updateGroceryModels(HashMap<String, List<GroceryModel>> listDataChild)
    {
        mGroceryListDataChild = new HashMap<>(listDataChild);
        mOriginalGroceryListChild = new HashMap<>(listDataChild);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroceryListDataChild.get(mCategoryListDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (mGroceryListDataChild.get(mCategoryListDataHeader.get(groupPosition)) == null) return 0;
        return mGroceryListDataChild.get(mCategoryListDataHeader.get(groupPosition)).size();
    }

    public void removeGroceryModel(String key){
        mGroceryListDataChild.remove(key);
        mOriginalGroceryListChild.remove(key);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mCategoryListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mCategoryListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.ll_list_header, parent, false);
        }

        if (mCategoryListDataHeader.size() != 0) {
            String headerTitle = (String) getGroup(groupPosition);
            TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);
        }
        return convertView;
    }

    public int getGroupIdByName(String categoryName) {
        for (int i = 0; i < mCategoryListDataHeader.size(); i++)
        {
            if (mCategoryListDataHeader.get(i).equals(categoryName))
            {
                return i;
            }
        }
        return 0;
    }

    private class ViewHolder {
        TextView textView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View v, ViewGroup parent) {
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final GroceryModel groceryModel = (GroceryModel) getChild(groupPosition, childPosition);

        if (v == null) {
            v = inflater.inflate(R.layout.list_item_grocery, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) v.findViewById(R.id.grocery_list_item_textView);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        if (groceryModel != null) {
            holder.textView.setText(groceryModel.getName());
        }
        return v;
    }

    public void filterData(String query) {

        query = query.toLowerCase();
        mCategoryListDataHeader.clear();
        mGroceryListDataChild.clear();

        if (!TextUtils.isEmpty(query)) {
            for (String category : mOriginalCategoryListHeader) {

                List<GroceryModel> groceryModels = mOriginalGroceryListChild.get(category);
                List<GroceryModel> newList = new ArrayList<>();
                for (GroceryModel g : groceryModels) {
                    if (g.getName().toLowerCase().contains(query)) {
                        newList.add(g);
                    }
                }
                if (newList.size() > 0) {
                    mCategoryListDataHeader.add(category);
                    mGroceryListDataChild.put(category, newList);
                }
            }
        } else {
            mCategoryListDataHeader = new ArrayList<>(mOriginalCategoryListHeader);
            for (String category : mOriginalCategoryListHeader) {
                List<GroceryModel> navItems = mOriginalGroceryListChild.get(category);
                mGroceryListDataChild.put(category, navItems);
            }
        }
        notifyDataSetChanged();
    }
}
