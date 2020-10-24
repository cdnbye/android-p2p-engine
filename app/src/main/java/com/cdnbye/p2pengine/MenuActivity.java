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
//                .diskCacheLimit(1000*1024*1024)

//                .p2pEnabled(false)

                // 测试环境
                .announce("https://tracker.p2pengine.net:7067/v1")
//                .wsSignalerAddr("wss://signal.p2pengine.net:8089")

                .localPortHls(-1)
                .localPortMp4(-1)

                .pieceLength(2024*1024)

                .build();

        long t1 =  System.currentTimeMillis();

        // 测试环境
        P2pEngine.init(getApplicationContext(), "U8qIyZDZg", config);

        // 正式环境
//       P2pEngine.initEngine(getApplicationContext(), "ZMuO5qHZg", config);

        long t2 =  System.currentTimeMillis();
        System.out.println("P2pEngine init 耗时 " + (t2-t1));

    }

    private List<ListEntry> buildListData() {
        return Arrays.asList(
//                new ListEntry("Streaming P2P", PlayerActivity.class),
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
