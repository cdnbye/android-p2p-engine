package com.cdnbye.sdk;

import java.util.List;

public interface P2pStatisticsListener {

    void onHttpDownloaded(long value);

    void onP2pDownloaded(long value);

    void onP2pUploaded(long value);

    void onPeers(List<String> peers);
}
