package com.jamal2367.styx.settings.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.widget.ArrayAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jamal2367.styx.R
import com.jamal2367.styx.database.javascript.JavaScriptDatabase
import com.jamal2367.styx.database.javascript.JavaScriptRepository
import com.jamal2367.styx.di.DatabaseScheduler
import com.jamal2367.styx.di.MainScheduler
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.preference.UserPreferences
import javax.inject.Inject

/**
 * The extension settings of the app.
 */
class ExtensionsSettingsFragment : AbstractSettingsFragment() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject internal lateinit var javascriptRepository: JavaScriptRepository

    @Inject @field:DatabaseScheduler internal lateinit var databaseScheduler: io.reactivex.Scheduler
    @Inject @field:MainScheduler internal lateinit var mainScheduler: io.reactivex.Scheduler

    override fun providePreferencesXmlResource() = R.xml.preference_extensions

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preference_extensions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        injector.inject(this)

        clickablePreference(
                preference = SCRIPT_UNINSTALL,
                onClick = ::uninstallUserScript
        )

    }

    private fun uninstallUserScript(){
        val builderSingle = MaterialAlertDialogBuilder(requireContext())
        builderSingle.setTitle(resources.getString(R.string.action_remove) + ":")
        val arrayAdapter = ArrayAdapter<String>(requireContext(), R.layout.userscript_choise)

        var jsList = emptyList<JavaScriptDatabase.JavaScriptEntry>()
        javascriptRepository.lastHundredVisitedJavaScriptEntries()
                .subscribe { list ->
                    jsList = list
                }

        for(i in jsList){
            arrayAdapter.add(i.name.replace("\\s".toRegex(), "").replace("\\n", ""))
        }


        builderSingle.setAdapter(arrayAdapter) { _: DialogInterface?, which: Int ->
            javascriptRepository.deleteJavaScriptEntry(jsList[which].name)
                    .subscribeOn(databaseScheduler)
                    .observeOn(mainScheduler)
                    .subscribe()
        }

        builderSingle.setPositiveButton(resources.getString(R.string.action_cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builderSingle.show()
    }

    companion object {
        private const val SCRIPT_UNINSTALL = "remove_userscript"
    }
}
