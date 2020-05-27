package com.ca.mas.masusermanagementsample.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ca.mas.masusermanagementsample.R;
import com.ca.mas.masusermanagementsample.model.MASMenu;
import com.ca.mas.masusermanagementsample.model.Submenu;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

public class CustomExpandableJSONListAdapter extends BaseExpandableListAdapter {

    private MASMenu menuData;
    private Context context;

    public CustomExpandableJSONListAdapter(Context context, MASMenu menu) {
        this.context = context;
        menuData = menu;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return menuData.getList().get(listPosition).getSubmenu().get(expandedListPosition).getMenu();
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return super.getChildType(groupPosition, childPosition);
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        TextView expandedListTextView = (TextView) convertView.findViewById(R.id.listitemtextview);
        LinearLayout widgetHolder = (LinearLayout) convertView.findViewById(R.id.widgetholder);
        TextView outputListItemTextView = (TextView) convertView.findViewById(R.id.outputlistitemtextviewtwo);
        expandedListTextView.setText(expandedListText);
        Submenu subMenu = menuData.getList().get(listPosition).getSubmenu().get(expandedListPosition);


        if(subMenu.getShowOutPut()){
            outputListItemTextView.setVisibility(View.VISIBLE);
            outputListItemTextView.setText(subMenu.getMessage());
            widgetHolder.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.green));
        } else {
            outputListItemTextView.setVisibility(View.GONE);
            widgetHolder.setBackgroundColor(convertView.getContext().getResources().getColor(R.color.linearbackground));
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return menuData.getList().get(listPosition).getSubmenu().size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return menuData.getList().get(listPosition).getName();
    }

    @Override
    public int getGroupCount() {
        return menuData.getList().size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = (String) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }

        TextView listTitleTextView = (TextView) convertView.findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

    public void setMenuData(MASMenu menu){
        menuData = menu;
    }
}
