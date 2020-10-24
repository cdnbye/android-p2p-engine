package com.cdnbye.p2pengine;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cdnbye.core.download.DownloadInfo;
import com.cdnbye.core.p2p.P2pStatisticsListener;
import com.cdnbye.core.download.FileDownloadListener;
import com.cdnbye.sdk.P2pEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public class DownloadActivity extends BaseActivity {

    //    private final String file1 = "https://webtorrent.io/torrents/Sintel/Sintel.mp4";
    private final String file1 = "http://huya-w20.huya.com/2027/357649831/1300/e0a4cd303b58bab74f809be7f2d09113.mp4";

    //    private final String file2 = "http://gyxz2.243ty.com/com.tencent.mm.apk";
    private final String file2 = "https://dldir1.qq.com/weixin/android/weixin7019android1760_arm64.apk";

    private TextView peersV;
    private TextView uploadV;
    private TextView offloadV;
    private TextView progressV;
    private TextView fileInfoV;
    private String currentUrl = file2;
    private File downloadingFile;

    private double totalHttpDownloaded = 0;
    private double totalP2pDownloaded = 0;
    private double totalP2pUploaded = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedBackGesture(true);

        setContentView(R.layout.activity_download);

        Button file1Btn = findViewById(R.id.file1);
        Button file2Btn = findViewById(R.id.file2);
        Button pauseBtn = findViewById(R.id.pause);
        Button stopBtn = findViewById(R.id.stop);
        Button clearBtn = findViewById(R.id.clear);
        Button restartBtn = findViewById(R.id.restart);
        peersV = findViewById(R.id.peers);
        uploadV = findViewById(R.id.upload);
        offloadV = findViewById(R.id.offload);
        fileInfoV = findViewById(R.id.file_info);
        progressV = findViewById(R.id.progress);
        TextView versionV = findViewById(R.id.version);
        versionV.setText("Version: " + P2pEngine.Version + "|" + P2pEngine.protocolVersion);

        P2pEngine.getInstance().addP2pStatisticsListener(new P2pStatisticsListener() {
            @Override
            public void onHttpDownloaded(long value) {
//                Log.d("TAG", "httpDownloaded: " + value);
                totalHttpDownloaded += (double)value;
                refreshRatio();
                checkIfConnected();
            }

            @Override
            public void onP2pDownloaded(long value) {
                totalP2pDownloaded += (double)value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView offloadV = findViewById(R.id.offload);
                        String text = String.format("Offload: %.2fMB", totalP2pDownloaded/1024);
                        offloadV.setText(text);

                        refreshRatio();
                    }
                });
                checkIfConnected();
            }

            @Override
            public void onP2pUploaded(long value) {
                totalP2pUploaded += (double)value;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView uploadV = findViewById(R.id.upload);
                        String text = String.format("Upload: %.2fMB", totalP2pUploaded/1024);
                        uploadV.setText(text);
                    }
                });
                checkIfConnected();
            }

            @Override
            public void onPeers(List<String> peers) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        TextView peersV = findViewById(R.id.peers);
                        String text = String.format("Peers: %d", peers.size());
                        peersV.setText(text);
                    }
                });
                checkIfConnected();
            }

            @Override
            public void onServerConnected(boolean connected) {
                System.out.println("onServerConnected " + connected);
                checkIfConnected();
            }
        });

        startDownload(currentUrl);

        P2pEngine.getInstance().registerFileDownloadListener(new FileDownloadListener() {
            @Override
            public void onDownloadFailed(Throwable e) {
                Toast toast=Toast.makeText(DownloadActivity.this, e.getMessage(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
//                if (e instanceof InterruptedException) return;
                e.printStackTrace();
                // fallback
                try {
                    P2pEngine.getInstance().stopFileDownload();
                    P2pEngine.getInstance().downloadFileNormally(currentUrl);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onDownloadFinished(File cacheFile, String url) {
                try {
                    String fileHash = md5HashCode(cacheFile);
                    System.out.println("onDownloadFinished " + url + " fileHash " + fileHash);
                    String text = String.format("Download Finished: %s\nMD5: %s", cacheFile, fileHash);
                    progressV.setText(text);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
//                System.out.println("onCacheAvailable cacheFile " + cacheFile + " url " + url + " percentsAvailable " + percentsAvailable);
                String text = String.format("Download Progress: %d%%", percentsAvailable);
                progressV.setText(text);
            }
        });

        file1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 清空数据
                clearData();
                currentUrl = file1;
                startDownload(currentUrl);
            }
        });

        file2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 清空数据
                clearData();
                currentUrl = file2;
                startDownload(currentUrl);
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                P2pEngine.getInstance().pauseFileDownload();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                P2pEngine.getInstance().stopFileDownload();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("currentFile " + downloadingFile);
                if (downloadingFile != null) {
                    P2pEngine.getInstance().stopFileDownload();
                    boolean isDeleted = P2pEngine.getInstance().deleteDownloadedFile(downloadingFile);
                    if (isDeleted) {
                        String text = String.format("Download Progress: 0%%");
                        progressV.setText(text);
                        Toast toast=Toast.makeText(DownloadActivity.this, "Deleted",Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                }
            }
        });

        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearData();
                if (downloadingFile != null) {
                    P2pEngine.getInstance().stopFileDownload();
                    P2pEngine.getInstance().deleteDownloadedFile(downloadingFile);
                    startDownload(currentUrl);
                }
            }
        });
    }

    private void startDownload(String url) {
        DownloadInfo object = null;
        try {
            object = P2pEngine.getInstance().downloadFile(url);
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast=Toast.makeText(DownloadActivity.this,e.getMessage(),Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        if (!object.isInProgress() && !object.isCached()) {
            return;
        }

        String text = String.format("File Info:\nUrl: %s\nFileName: %s\nSize: %d\n",
                object.getUrl(),
                object.getFileName(),
                object.getFileSize());
        fileInfoV.setText(text);

        downloadingFile = object.getCacheFile();

        if (object.isCached()) {
            try {
                String fileHash = md5HashCode(object.getCacheFile());
                text = String.format("Download Finished: %s\nMD5: %s", object.getCacheFile(), fileHash);
                progressV.setText(text);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void refreshRatio() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double ratio = 0;
                if (totalHttpDownloaded+totalP2pDownloaded != 0) {
                    ratio = totalP2pDownloaded/(totalHttpDownloaded+totalP2pDownloaded);
                }
                TextView ratioV = findViewById(R.id.ratio);
                String text = String.format("P2P Ratio: %.0f%%", ratio*100);
                ratioV.setText(text);
            }
        });
    }

    private void checkIfConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView connectedV = findViewById(R.id.connected);
                String text = String.format("Connected: %s", P2pEngine.getInstance().isConnected()?"Yes":"No");
                connectedV.setText(text);

                TextView peerIdV = findViewById(R.id.peerId);
                String text2 = String.format("Peer ID: %s", P2pEngine.getInstance().getPeerId());
                peerIdV.setText(text2);
            }
        });
    }

    private void clearData() {
        totalHttpDownloaded = 0;
        totalP2pDownloaded = 0;
        totalP2pUploaded = 0;
        checkIfConnected();
        refreshRatio();
    }

    public static String md5HashCode(File file) throws FileNotFoundException {
        FileInputStream fis = new FileInputStream(file);
        return md5HashCode(fis);
    }

    /**
     * java获取文件的md5值
     * @param fis 输入流
     * @return
     */
    public static String md5HashCode(InputStream fis) {
        try {
            //拿到一个MD5转换器,如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256
            MessageDigest md = MessageDigest.getInstance("MD5");

            //分多次将一个文件读入，对于大型文件而言，比较推荐这种方式，占用内存比较少。
            byte[] buffer = new byte[512*1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 512*1024)) != -1) {
                md.update(buffer, 0, length);
            }
            fis.close();
            //转换并返回包含16个元素字节数组,返回数值范围为-128到127
            byte[] md5Bytes  = md.digest();
            BigInteger bigInt = new BigInteger(1, md5Bytes);//1代表绝对值
            return bigInt.toString(16);//转换为16进制
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}