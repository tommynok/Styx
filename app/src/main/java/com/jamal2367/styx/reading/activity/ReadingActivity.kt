package com.jamal2367.styx.reading.activity

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jamal2367.styx.AppTheme
import com.jamal2367.styx.R
import com.jamal2367.styx.di.MainScheduler
import com.jamal2367.styx.di.NetworkScheduler
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.dialog.BrowserDialog.setDialogSize
import com.jamal2367.styx.extensions.snackbar
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.reading.HtmlFetcher
import com.jamal2367.styx.settings.activity.ThemedSettingsActivity
import com.jamal2367.styx.utils.Utils
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.disposables.Disposable
import java.util.*
import javax.inject.Inject

class ReadingActivity : ThemedSettingsActivity(), TextToSpeech.OnInitListener {

    var mTitle: TextView? = null
    var mBody: TextView? = null

    @JvmField
    @Inject
    var mUserPreferences: UserPreferences? = null

    @JvmField
    @Inject
    @NetworkScheduler
    var mNetworkScheduler: Scheduler? = null

    @JvmField
    @Inject
    @MainScheduler
    var mMainScheduler: Scheduler? = null
    private var tts: TextToSpeech? = null
    private var mInvert = false
    private var reading = false
    private var mUrl: String? = null
    private var mTextSize = 0
    private var mProgressDialog: AlertDialog? = null
    private var mPageLoaderSubscription: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        this.injector.inject(this)
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.fade_out_scale)
        mInvert = mUserPreferences!!.invertColors
        tts = TextToSpeech(this, this)

        super.onCreate(savedInstanceState)

        // Change our theme if inverted
        if (mInvert) {
            if (useDarkTheme) {
                applyTheme(AppTheme.LIGHT)
            } else {
                applyTheme(AppTheme.BLACK)
            }
        }

        setContentView(R.layout.reading_view)
        mTitle = findViewById(R.id.textViewTitle)
        mBody = findViewById(R.id.textViewBody)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        if (supportActionBar != null) supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mTextSize = mUserPreferences!!.readingTextSize
        getTextSize(mTextSize).also { mBody!!.textSize = it }
        mTitle!!.text = getString(R.string.untitled)
        mBody!!.text = getString(R.string.loading)
        mTitle!!.visibility = View.INVISIBLE
        mBody!!.visibility = View.INVISIBLE
        val intent = intent

        if (!loadPage(intent)) {
            setText(getString(R.string.untitled), getString(R.string.loading_failed))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reading, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun loadPage(intent: Intent?): Boolean {
        if (intent == null) {
            return false
        }

        mUrl = intent.getStringExtra(LOAD_READING_URL)
        if (mUrl == null) {
            return false
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = Utils.getDisplayDomainName(mUrl)
        }

        // Build progress dialog
        val progressView = LayoutInflater.from(this).inflate(R.layout.dialog_progress, null)
        val builder = MaterialAlertDialogBuilder(this)
                .setView(progressView)
                .setCancelable(false)
        mProgressDialog = builder.create()
        val tv = progressView.findViewById<TextView>(R.id.text_progress_bar)
        tv.setText(R.string.loading)
        mProgressDialog!!.show()
        setDialogSize(this@ReadingActivity, mProgressDialog!!)
        mPageLoaderSubscription = loadPage(mUrl!!)
                .subscribeOn(mNetworkScheduler)
                .observeOn(mMainScheduler)
                .subscribe({ readerInfo: ReaderInfo ->
                    if (readerInfo.title.isEmpty() || readerInfo.body.isEmpty()) {
                        setText(getString(R.string.untitled), getString(R.string.loading_failed))
                    } else {
                        setText(readerInfo.title, readerInfo.body)
                    }
                    dismissProgressDialog()
                }) {
                    setText(getString(R.string.untitled), getString(R.string.loading_failed))
                    dismissProgressDialog()
                }
        return true
    }

    private fun dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
            mProgressDialog = null
        }
    }

    private class ReaderInfo(val title: String, val body: String)

    private fun setText(title: String, body: String) {
        if (mTitle == null || mBody == null) return
        if (mTitle!!.visibility == View.INVISIBLE) {
            mTitle!!.alpha = 0.0f
            mTitle!!.visibility = View.VISIBLE
            mTitle!!.text = title
            val animator = ObjectAnimator.ofFloat(mTitle, "alpha", 1.0f)
            animator.duration = 300
            animator.start()
        } else {
            mTitle!!.text = title
        }

        if (mBody!!.visibility == View.INVISIBLE) {
            mBody!!.alpha = 0.0f
            mBody!!.visibility = View.VISIBLE
            mBody!!.text = body
            val animator = ObjectAnimator.ofFloat(mBody, "alpha", 1.0f)
            animator.duration = 300
            animator.start()
        } else {
            mBody!!.text = body
        }
    }

    override fun onDestroy() {
        mPageLoaderSubscription!!.dispose()
        if (mProgressDialog != null && mProgressDialog!!.isShowing) {
            mProgressDialog!!.dismiss()
            mProgressDialog = null
        }
        tts?.stop()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.fade_in_scale, R.anim.slide_out_to_right)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.invert_item -> {
                mUserPreferences!!.invertColors = !mInvert
                if (mUrl != null) {
                    launch(this, mUrl!!)
                    finish()
                }
            }
            R.id.text_size_item -> {
                val view = LayoutInflater.from(this).inflate(R.layout.dialog_seek_bar, null)
                val bar = view.findViewById<SeekBar>(R.id.text_size_seekbar)
                bar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(view: SeekBar, size: Int, user: Boolean) {
                        mBody!!.textSize = getTextSize(size)
                    }

                    override fun onStartTrackingTouch(arg0: SeekBar) {}
                    override fun onStopTrackingTouch(arg0: SeekBar) {}
                })
                bar.max = 5
                bar.progress = mTextSize
                val builder = MaterialAlertDialogBuilder(this)
                        .setView(view)
                        .setTitle(R.string.size)
                        .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                            mTextSize = bar.progress
                            getTextSize(mTextSize).also { mBody!!.textSize = it }
                            mUserPreferences!!.readingTextSize = bar.progress
                        }
                val dialog: Dialog = builder.show()
                setDialogSize(this, dialog)
            }
            R.id.tts -> {
                reading = !reading
                val text: String = mBody?.text.toString()
                if (reading) tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                else tts!!.stop()
                invalidateOptionsMenu()
            }
            else -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {

            val result: Int = tts!!.setLanguage(Locale.getDefault())

            //tts!!.setPitch(1F) // set pitch level
            //tts!!.setSpeechRate(1F) // set speech speed rate

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                snackbar(R.string.no_tts)
            } else {
                //btnSpeak.setEnabled(true)
                //speakOut()
            }
        } else {
            snackbar(R.string.tts_initilization_failed)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.tts)
        if (reading) {
            item.title = resources.getString(R.string.stop_tts)
        } else {
            item.title = resources.getString(R.string.tts)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    companion object {
        private const val LOAD_READING_URL = "ReadingUrl"

        /**
         * Launches this activity with the necessary URL argument.
         *
         * @param context The context needed to launch the activity.
         * @param url     The URL that will be loaded into reading mode.
         */
        fun launch(context: Context, url: String) {
            val intent = Intent(context, ReadingActivity::class.java)
            intent.putExtra(LOAD_READING_URL, url)
            context.startActivity(intent)
        }

        private const val TAG = "ReadingActivity"
        private const val XXLARGE = 30.0f
        private const val XLARGE = 26.0f
        private const val LARGE = 22.0f
        private const val MEDIUM = 18.0f
        private const val SMALL = 14.0f
        private const val XSMALL = 10.0f

        private fun getTextSize(size: Int): Float {
            return when (size) {
                0 -> XSMALL
                1 -> SMALL
                2 -> MEDIUM
                3 -> LARGE
                4 -> XLARGE
                5 -> XXLARGE
                else -> MEDIUM
            }
        }

        private fun loadPage(url: String): Single<ReaderInfo> {
            return Single.create { emitter: SingleEmitter<ReaderInfo> ->
                val fetcher = HtmlFetcher()
                try {
                    val result = fetcher.fetchAndExtract(url, 2500, true)
                    emitter.onSuccess(ReaderInfo(result.title, result.text))
                } catch (e: Exception) {
                    emitter.onError(Throwable("Encountered exception"))
                    Log.e(TAG, "Error parsing page", e)
                } catch (e: OutOfMemoryError) {
                    System.gc()
                    emitter.onError(Throwable("Out of memory"))
                    Log.e(TAG, "Out of memory", e)
                }
            }
        }
    }
}