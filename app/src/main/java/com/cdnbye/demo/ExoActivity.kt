package com.cdnbye.demo

import android.net.Uri
import android.os.Bundle
import com.cdnbye.demo.databinding.ActivityExoBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.p2pengine.core.p2p.P2pStatisticsListener
import com.p2pengine.core.p2p.PlayerInteractor
import com.p2pengine.sdk.P2pEngine

class ExoActivity : BaseActivity() {
    private val hls1 = "https://video.cdnbye.com/0cf6732evodtransgzp1257070836/e0d4b12e5285890803440736872/v.f100220.m3u8"
    private val hls2 = "https://wowza.peer5.com/live/smil:bbb_abr.smil/chunklist_b591000.m3u8"

    private lateinit var exoBinding: ActivityExoBinding
    private var player: ExoPlayer? = null
    private var currentUrl = hls2
    private var totalHttpDownloaded = 0.0
    private var totalP2pDownloaded = 0.0
    private var totalP2pUploaded = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setNeedBackGesture(true)
        exoBinding = ActivityExoBinding.inflate(layoutInflater)
        setContentView(exoBinding.root)

        "Version: ${P2pEngine.Version} | ${P2pEngine.protocolVersion}".also { exoBinding.version.text = it }

//        P2pEngine.instance?.setHlsSegmentIdGenerator { streamId, sn, segmentUrl, range ->
//            "$segmentUrl|$range"
//        }

        P2pEngine.instance?.setPlayerInteractor(object : PlayerInteractor() {
            override fun onBufferedDuration(): Long {
                val playerLocal = player ?: return -1
                return playerLocal.bufferedPosition - playerLocal.currentPosition
            }
        })

        P2pEngine.instance?.addP2pStatisticsListener(object : P2pStatisticsListener {
            override fun onHttpDownloaded(value: Long) {
                println("onHttpDownloaded $value")
                totalHttpDownloaded += value.toDouble()
                refreshRatio()
            }

            override fun onP2pDownloaded(value: Long, speed: Int) {
                println("p2p download speed $speed")
                totalP2pDownloaded += value.toDouble()
                exoBinding.offload.text = String.format("Offload: %.2fMB", totalP2pDownloaded / 1024)
                refreshRatio()
            }

            override fun onP2pUploaded(value: Long, speed: Int) {
                totalP2pUploaded += value.toDouble()
                exoBinding.upload.text = String.format("Upload: %.2fMB", totalP2pUploaded / 1024)
            }

            override fun onPeers(peers: List<String>) {
                exoBinding.peers.text = String.format("Peers: %d", peers.size)
            }

            override fun onServerConnected(connected: Boolean) {
                exoBinding.connected.text = String.format("Connected: %s", if (connected) "Yes" else "No")
                exoBinding.peerId.text = java.lang.String.format("Peer ID: %s", P2pEngine.instance?.peerId)
            }
        })

        exoBinding.hls1.setOnClickListener {
            currentUrl = hls1
            clearData()
            startPlay(currentUrl)
        }

        exoBinding.hls2.setOnClickListener {
            currentUrl = hls2
            clearData()
            startPlay(currentUrl)
        }

        exoBinding.btnPlay.setOnClickListener {
            val playUrl = exoBinding.playUrl.text.toString()
            if (playUrl.isNotEmpty()) {
                currentUrl = playUrl
                clearData()
                startPlay(currentUrl)
            }
        }
    }

    @Synchronized
    private fun startPlay(url: String) {
        if (player != null) {
            player?.stop()
        }
        println("startPlay $url")
        val parsedUrl = P2pEngine.instance?.parseStreamUrl(url)

        // Create LoadControl
        val loadControl: LoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(
                VideoPlayerConfig.MIN_BUFFER_DURATION,
                VideoPlayerConfig.MAX_BUFFER_DURATION,
                VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
                VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER
            )
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        // Create a data source factory.
        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSource.Factory()
        // Create a HLS media source pointing to a playlist uri.
        val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(parsedUrl)))
        // Create a player instance.
        player = ExoPlayer.Builder(applicationContext)
            .setLoadControl(loadControl)
            .build()
        // Set the media source to be played.
        player?.setMediaSource(hlsMediaSource)
        // Prepare the player.
        player?.prepare()
        // Attach player to the view.
        exoBinding.playerView.player = player

        val s1 = System.currentTimeMillis()

        // Start play when ready
        player?.playWhenReady = true

//        playerView.
        player?.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                println("ExoPlaybackException $error")
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == 3) {
                    val e1 = System.currentTimeMillis()
                    println("time to start play " + (e1 - s1))
                }
            }
        })
    }

    private fun refreshRatio() {
        var ratio = 0.0
        if (totalHttpDownloaded + totalP2pDownloaded != 0.0) {
            ratio = totalP2pDownloaded / (totalHttpDownloaded + totalP2pDownloaded)
        }
        exoBinding.ratio.text = String.format("P2P Ratio: %.0f%%", ratio * 100)
    }

    private fun clearData() {
        totalHttpDownloaded = 0.0
        totalP2pDownloaded = 0.0
        totalP2pUploaded = 0.0
        refreshRatio()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player?.release()
            player = null
        }
    }

    object VideoPlayerConfig {
        //Minimum Video you want to buffer while Playing
        const val MIN_BUFFER_DURATION = 7000

        //Max Video you want to buffer during PlayBack
        const val MAX_BUFFER_DURATION = 15000

        //Min Video you want to buffer before start Playing it
        const val MIN_PLAYBACK_START_BUFFER = 7000

        //Min video You want to buffer when user resumes video
        const val MIN_PLAYBACK_RESUME_BUFFER = 7000
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        P2pEngine.instance?.stopP2p()
    }

}