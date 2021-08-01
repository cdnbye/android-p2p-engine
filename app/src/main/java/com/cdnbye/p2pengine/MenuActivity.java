package com.cdnbye.p2pengine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.fragment.app.FragmentActivity;
import java.util.Arrays;
import java.util.List;

import com.cdnbye.core.p2p.EngineExceptionListener;
import com.cdnbye.core.utils.EngineException;
import com.cdnbye.core.utils.LogLevel;
import com.cdnbye.core.p2p.P2pConfig;
import com.cdnbye.sdk.P2pEngine;

public class MenuActivity extends FragmentActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, buildListData());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                ListEntry item = (ListEntry) listView.getAdapter().getItem(position);
                startActivity(new Intent(MenuActivity.this, item.activityClass));
            }
        });

        P2pConfig config = new P2pConfig.Builder()
                .logEnabled(true)
                .logLevel(LogLevel.DEBUG)
                .p2pEnabled(true)
                .waitForPeer(true)
                .build();

        P2pEngine.init(getApplicationContext(), "ZMuO5qHZg", config);

        P2pEngine.getInstance().registerExceptionListener(new EngineExceptionListener() {
            @Override
            public void onTrackerException(EngineException e) {
                System.out.println("onTrackerException " + e.getMessage());
            }

            @Override
            public void onSignalException(EngineException e) {
                System.out.println("onSignalException " + e.getMessage());
            }

            @Override
            public void onSchedulerException(EngineException e) {
                System.out.println("onSchedulerException " + e.getMessage());
            }

            @Override
            public void onOtherException(EngineException e) {
                System.out.println("onOtherException " + e.getMessage());
            }
        });
    }

    private List<ListEntry> buildListData() {
        return Arrays.asList(
                new ListEntry("Video Stream P2P", PlayerActivity.class),
                new ListEntry("File Download P2P", DownloadActivity.class)
        );
    }

    private static final class ListEntry {

        private final String title;
        private final Class activityClass;

        public ListEntry(String title, Class activityClass) {
            this.title = title;
            this.activityClass = activityClass;
        }

        @Override
        public String toString() {
            return title;
        }
    }

}
