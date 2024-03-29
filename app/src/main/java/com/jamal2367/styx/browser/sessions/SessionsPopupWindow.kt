package com.jamal2367.styx.browser.sessions

import android.graphics.drawable.ColorDrawable
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.EditText
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.PopupWindowCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jamal2367.styx.R
import com.jamal2367.styx.browser.activity.BrowserActivity
import com.jamal2367.styx.controller.UIController
import com.jamal2367.styx.databinding.SessionListBinding
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.dialog.BrowserDialog
import com.jamal2367.styx.extensions.toast
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.utils.FileNameInputFilter
import com.jamal2367.styx.utils.ItemDragDropSwipeHelper
import com.jamal2367.styx.utils.Utils
import javax.inject.Inject


class SessionsPopupWindow : PopupWindow {

    var iUiController: UIController
    var iAdapter: SessionsAdapter
    var iBinding: SessionListBinding
    var iAnchor: View? = null
    private var iItemTouchHelper: ItemTouchHelper? = null

    @Inject lateinit var userPreferences: UserPreferences

    constructor(layoutInflater: LayoutInflater,
                aBinding: SessionListBinding = SessionListBinding.inflate(layoutInflater))
            : super(aBinding.root, WRAP_CONTENT, WRAP_CONTENT, true) {

        aBinding.root.context.injector.inject(this)

        // Needed to make sure our bottom sheet shows below our session pop-up
        PopupWindowCompat.setWindowLayoutType(this, WindowManager.LayoutParams.FIRST_SUB_WINDOW + 5);

        // Elevation just need to be high enough not to cut the effect defined in our layout
        elevation = 100F

        iBinding = aBinding
        iUiController = aBinding.root.context as UIController
        iAdapter = SessionsAdapter(iUiController)

        animationStyle = R.style.AnimationMenu
        //animationStyle = android.R.style.Animation_Dialog

        // Needed on Android 5 to make sure our pop-up can be dismissed by tapping outside and back button
        // See: https://stackoverflow.com/questions/46872634/close-popupwindow-upon-tapping-outside-or-back-button
        setBackgroundDrawable(ColorDrawable())


        // Handle click on "add session" button
        aBinding.buttonNewSession.setOnClickListener { view ->
            val dialogView = LayoutInflater.from(aBinding.root.context).inflate(R.layout.dialog_edit_text, null)
            val textView = dialogView.findViewById<EditText>(R.id.dialog_edit_text)
            // Make sure user can only enter valid filename characters
            textView.filters = arrayOf<InputFilter>(FileNameInputFilter())

            BrowserDialog.showCustomDialog(aBinding.root.context as AppCompatActivity) {
                setTitle(R.string.session_name_prompt)
                setView(dialogView)
                setPositiveButton(R.string.action_ok) { _, _ ->
                    val name = textView.text.toString()
                    // Check if session exists already
                    if (iUiController.getTabModel().isValidSessionName(name)) {
                        // That session does not exist yet, add it then
                        iUiController.getTabModel().iSessions.let {
                            it.add(Session(name, 1))
                            // Switch to our newly added session
                            (view.context as BrowserActivity).apply {
                                presenter?.switchToSession(name)
                                // Close session dialog after creating and switching to new session
                                sessionsMenu.dismiss()
                            }
                            // Update our session list
                            //iAdapter.showSessions(it)
                        }
                    } else {
                        // We already have a session with that name, display an error message
                        context.toast(R.string.session_already_exists)
                    }
                }
            }
        }

        // Handle save as button
        // TODO: reuse code between, new, save as and edit dialog
        aBinding.buttonSaveSession.setOnClickListener { view ->
            val dialogView = LayoutInflater.from(aBinding.root.context).inflate(R.layout.dialog_edit_text, null)
            val textView = dialogView.findViewById<EditText>(R.id.dialog_edit_text)
            // Make sure user can only enter valid filename characters
            textView.filters = arrayOf<InputFilter>(FileNameInputFilter())

            iUiController.getTabModel().let { tabs ->
                BrowserDialog.showCustomDialog(aBinding.root.context as AppCompatActivity) {
                    setTitle(R.string.session_name_prompt)
                    setView(dialogView)
                    setPositiveButton(R.string.action_ok) { _, _ ->
                        val name = textView.text.toString()
                        // Check if session exists already
                        if (tabs.isValidSessionName(name)) {
                            // That session does not exist yet, add it then
                            tabs.iSessions.let {
                                // Save current session session first
                                tabs.saveState()
                                // Add new session
                                it.add(Session(name, tabs.currentSession().tabCount))
                                // Set it as current session
                                tabs.iCurrentSessionName = name
                                // Save current tabs that our newly added session
                                tabs.saveState()
                                // Switch to our newly added session
                                (view.context as BrowserActivity).apply {
                                    // Close session dialog after creating and switching to new session
                                    // TODO: not in edit mode?
                                    sessionsMenu.dismiss()
                                }

                                // Show user we did switch session
                                view.context.apply {
                                    toast(getString(R.string.session_switched, name))
                                }

                                // Update our session list
                                //iAdapter.showSessions(it)
                            }
                        } else {
                            // We already have a session with that name, display an error message
                            context.toast(R.string.session_already_exists)
                        }
                    }
                }
            }
        }


        aBinding.buttonEditSessions.setOnClickListener {

            // Toggle edit mode
            iAdapter.iEditModeEnabledObservable.value?.let { editModeEnabled ->
                // Change button icon
                // TODO: change the text too?
                if (!editModeEnabled) {
                    aBinding.buttonEditSessions.setImageResource(R.drawable.ic_secured)
                } else {
                    aBinding.buttonEditSessions.setImageResource(R.drawable.ic_edit)
                }
                // Notify our observers of edit mode change
                iAdapter.iEditModeEnabledObservable.onNext(!editModeEnabled)

                // Just close and reopen our menu as our layout change animation is really ugly
                dismiss()
                iAnchor?.let {
                    (iUiController as BrowserActivity).mainHandler.post { show(it,!editModeEnabled,false) }
                }
                // We still broadcast the change above and do a post to avoid getting some items caught not fully animated, even though animations are disabled.
                // Android layout animation crap, just don't ask, sometimes it's a blessing other times it's a nightmare...
            }
        }

        // Make sure Ctrl + Shift + S closes our menu so that toggle is working
        // TODO: Somehow still not working
        /*
        contentView.isFocusableInTouchMode = true
        contentView.setOnKeyListener { _, keyCode, event ->
            val isCtrlShiftOnly  = KeyEvent.metaStateHasModifiers(event.metaState, KeyEvent.META_CTRL_ON or KeyEvent.META_SHIFT_ON)
            //(isCtrlShiftOnly && keyCode == KeyEvent.KEYCODE_S).also { if (it) dismiss() }
            if (isCtrlShiftOnly && keyCode == KeyEvent.KEYCODE_S) {
                dismiss()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        */

        // Setup our recycler view
        aBinding.recyclerViewSessions.apply {
            //setLayerType(View.LAYER_TYPE_NONE, null)
            //(itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = iAdapter
            setHasFixedSize(false)
        }

        // Enable drag & drop but not swipe
        val callback: ItemTouchHelper.Callback = ItemDragDropSwipeHelper(iAdapter, true, false)
        iItemTouchHelper = ItemTouchHelper(callback)
        iItemTouchHelper?.attachToRecyclerView(iBinding.recyclerViewSessions)
    }


    /**
     *
     */
    fun show(aAnchor: View, aEdit: Boolean = false, aShowCurrent: Boolean = true) {
        // Disable edit mode when showing our menu
        iAdapter.iEditModeEnabledObservable.onNext(aEdit)
        if (aEdit) {
            iBinding.buttonEditSessions.setImageResource(R.drawable.ic_secured);
        } else {
            iBinding.buttonEditSessions.setImageResource(R.drawable.ic_edit);
        }

        iAnchor = aAnchor
        //showAsDropDown(aAnchor, 0, 0)

        // Get our anchor location
        val anchorLoc = IntArray(2)
        aAnchor.getLocationInWindow(anchorLoc)
        //
        val gravity = if (userPreferences.toolbarsBottom) Gravity.BOTTOM or Gravity.RIGHT else Gravity.TOP or Gravity.RIGHT
        val yOffset = if (userPreferences.toolbarsBottom) (contentView.context as BrowserActivity).iBinding.root.height - anchorLoc[1] else anchorLoc[1]+aAnchor.height
        // Show our popup menu from the right side of the screen below our anchor
        showAtLocation(aAnchor, gravity,
                // Offset from the right screen edge
                Utils.dpToPx(10F),
                // Below our anchor
                yOffset)

        //dimBehind()
        // Show our sessions
        updateSessions()
        if (aShowCurrent) {
            // Make sure current session is on the screen
            iBinding.recyclerViewSessions.smoothScrollToPosition(iUiController.getTabModel().currentSessionIndex())
        }
    }


    /**
     *
     */
    fun updateSessions() {
        //See: https://stackoverflow.com/q/43221847/3969362
        // I'm guessing isComputingLayout is not needed anymore since we moved our update after tab manager initialization
        // TODO: remove it and switch quickly between sessions to see if that still works
        if (!iBinding.recyclerViewSessions.isComputingLayout) {
            iAdapter.showSessions(iUiController.getTabModel().iSessions)
        }
    }

}
