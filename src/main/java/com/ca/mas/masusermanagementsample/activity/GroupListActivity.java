/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masusermanagementsample.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.common.MASFilteredRequestBuilder;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.masusermanagementsample.R;
import com.ca.mas.masusermanagementsample.adapter.DividerDecoration;
import com.ca.mas.masusermanagementsample.adapter.GroupRecyclerAdapter;
import com.ca.mas.masusermanagementsample.mas.GroupsManager;
import com.ca.mas.masusermanagementsample.model.MASMenu;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupListActivity extends AppCompatActivity {

    private final String TAG = GroupListActivity.class.getSimpleName();
    private Context mContext;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContext = this;
        mRecyclerView = (RecyclerView) findViewById(R.id.group_list);
        assert mRecyclerView != null;

//        String jsonObject = loadAssetTextAsString(this, "menu.json");
//        MASMenu data =new MASMenu() ;
//        Gson gson = new Gson();
//        data= gson.fromJson(jsonObject,MASMenu.class);

        MAS.start(this, true);

        MASUser.login("username", "password".toCharArray(), getUserCallback());
    }

    private MASCallback<MASUser> getUserCallback() {
        return new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                getAllGroups(user);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG + " getUserCallback()", e.toString());
            }
        };
    }

    private void getAllGroups(MASUser user) {
        String key = "userName";
        List<String> attr = new ArrayList<>();
        attr.add("owner.value");

        MASFilteredRequest frb = new MASFilteredRequest(attr, IdentityConsts.KEY_USER_ATTRIBUTES);
        frb.isEqualTo(key, user.getUserName());
        frb.setPagination(IdentityConsts.INDEX_START, IdentityConsts.PAGE_SIZE);
        frb.setSortOrder(MASFilteredRequestBuilder.SortOrder.descending, key);

        MASGroup.newInstance().getGroupsByFilter(frb, getGroupsCallback());
    }

    private MASCallback<List<MASGroup>> getGroupsCallback() {
        return new MASCallback<List<MASGroup>>() {
            @Override
            public void onSuccess(final List<MASGroup> groups) {
                if (groups != null && groups.size() > 0) {
                    Map<String, MASGroup> groupsMap = new HashMap<>();
                    for (MASGroup group : groups) {
                        String id = group.getGroupName();
                        groupsMap.put(id, group);
                    }

                    GroupsManager.INSTANCE.setGroups(groupsMap);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GroupRecyclerAdapter adapter = new GroupRecyclerAdapter(groups);
                        mRecyclerView.setAdapter(adapter);
                        mRecyclerView.addItemDecoration(new DividerDecoration(mContext));
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG + " getGroupsCallback()", e.toString());
            }
        };
    }

    private String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            JSONObject obj = new JSONObject(buf.toString());
            return buf.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error opening asset " + name);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing asset " + name);
                }
            }
        }

        return null;
    }

}
