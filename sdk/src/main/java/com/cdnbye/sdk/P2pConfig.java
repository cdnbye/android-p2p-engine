package com.cdnbye.sdk;

import java.util.concurrent.TimeUnit;
import org.webrtc.PeerConnection.RTCConfiguration;

public final class P2pConfig {

    public String getAnnounce() {
        return announce;
    }

    public String getWsSignalerAddr() {
        return wsSignalerAddr;
    }

    public String getCustomTag() {
        return mTag;
    }

    public String getAgent() {
        return mAgent;
    }

    public Boolean getP2pEnabled() {
        return p2pEnabled;
    }


    public int getDcDownloadTimeout() {
        return dcDownloadTimeout;
    }

    public int getDownloadTimeout() {
        return downloadTimeout;
    }

    public long getMaxBufferSize() {
        return maxBufferSize;
    }

    public int getLocalPort() {
        return localPort;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public boolean isDebug() {
        return debug;
    }

    public ChannelIdCallback getChannelId() {
        return channelId;
    }

    public RTCConfiguration getWebRTCConfig() {
        return webRTCConfig;
    }

    public int getMaxPeerConns() {
        return maxPeerConnections;
    }

    public int getMemoryCacheLimit() {
        return memoryCacheLimit;
    }

    public boolean isUseHttpRange() {
        return useHttpRange;
    }

    private String announce;
    private String wsSignalerAddr;
    private String mTag;
    private String mAgent;

    private ChannelIdCallback channelId;
    private boolean p2pEnabled;
    private int dcDownloadTimeout;
    private int downloadTimeout;
    private long maxBufferSize;
    private LogLevel logLevel;
    private boolean  debug;
    private int localPort;
    private RTCConfiguration webRTCConfig;
    private int maxPeerConnections;
    private int memoryCacheLimit;
    private boolean useHttpRange;

    private P2pConfig(Builder builder) {
        this.announce = builder.announce;
        this.wsSignalerAddr = builder.wsSignalerAddr;
        this.mTag = builder.mTag;
        this.mAgent = builder.mAgent;
        this.p2pEnabled = builder.p2pEnabled;
        this.dcDownloadTimeout = builder.dcDownloadTimeout;
        this.downloadTimeout = builder.downloadTimeout;
        this.maxBufferSize = builder.maxBufferSize;
        this.localPort = builder.localPort;
        this.debug = builder.debug;
        this.logLevel = builder.logLevel;
        this.channelId = builder.channelId;
        this.webRTCConfig = builder.webRTCConfig;
        this.maxPeerConnections = builder.maxPeerConnections;
        this.memoryCacheLimit = builder.memoryCacheLimit;
        this.useHttpRange = builder.useHttpRange;
    }

    public static class Builder {
        private String announce = "https://tracker.cdnbye.com/v1";
        private String wsSignalerAddr = "wss://signal.cdnbye.com";
        private String mTag = "unknown";
        private String mAgent = "";
        private ChannelIdCallback channelId = null;
        private boolean p2pEnabled = true;
        private int dcDownloadTimeout = 6_000;
        private int downloadTimeout = 10_000;
        private long maxBufferSize = 1024*1024*1024;
        private int localPort = 52019;
        private LogLevel logLevel = LogLevel.WARN;              // ASSERT = 7; DEBUG = 3; ERROR = 6;INFO = 4;VERBOSE = 2;WARN = 5;
        private boolean debug = false;
        private RTCConfiguration webRTCConfig;                   // 需要判空
        private int maxPeerConnections = 10;
        private int memoryCacheLimit = 60 * 1024 *1024;
        private boolean useHttpRange = true;

        public Builder announce(String announce) {
            this.announce = announce;
            return this;
        }

        public Builder wsSignalerAddr(String addr) {
            this.wsSignalerAddr = addr;
            return this;
        }

        public Builder withTag(String tag) {
            this.mTag = tag;
            return this;
        }

        public Builder withAgent(String agent) {
            this.mAgent = agent;
            return this;
        }

        public Builder p2pEnabled(Boolean enabled) {
            this.p2pEnabled = enabled;
            return this;
        }


        public Builder dcDownloadTimeout(int timeout, TimeUnit unit) {
            long millis = unit.toMillis(timeout);
            this.dcDownloadTimeout = (int)millis;
            return this;
        }

        public Builder downloadTimeout(int timeout, TimeUnit unit) {
            long millis = unit.toMillis(timeout);
            this.downloadTimeout = (int)millis;
            return this;
        }

        public Builder diskCacheLimit(long maxBufferSize) {
            this.maxBufferSize = maxBufferSize;
            return this;
        }

        public Builder localPort(int port) {
            this.localPort = port;
            return this;
        }

        public Builder logEnabled(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder logLevel(LogLevel level) {
            this.logLevel = level;
            return this;
        }

        public Builder channelId(ChannelIdCallback channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder webRTCConfig(RTCConfiguration webRTCConfig) {
            this.webRTCConfig = webRTCConfig;
            return this;
        }

        public Builder maxPeerConnections(int conns) {
            this.maxPeerConnections = conns;
            return this;
        }

        public Builder memoryCacheLimit(int size) {
            this.memoryCacheLimit = size;
            return this;
        }

        public Builder useHttpRange(boolean flag) {
            this.useHttpRange = flag;
            return this;
        }

        public P2pConfig build() {
            return new P2pConfig(this);
        }

    }
}
