package com.jamal2367.styx.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import com.jamal2367.styx.R
import com.jamal2367.styx.database.Bookmark
import com.jamal2367.styx.database.HistoryEntry
import com.jamal2367.styx.database.SearchSuggestion
import com.jamal2367.styx.database.WebPage
import com.jamal2367.styx.database.bookmark.BookmarkRepository
import com.jamal2367.styx.database.history.HistoryRepository
import com.jamal2367.styx.di.DatabaseScheduler
import com.jamal2367.styx.di.MainScheduler
import com.jamal2367.styx.di.NetworkScheduler
import com.jamal2367.styx.di.injector
import com.jamal2367.styx.extensions.drawable
import com.jamal2367.styx.preference.UserPreferences
import com.jamal2367.styx.search.suggestions.NoOpSuggestionsRepository
import com.jamal2367.styx.search.suggestions.SuggestionsRepository
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject

class SuggestionsAdapter(
    context: Context,
    private val isIncognito: Boolean
) : BaseAdapter(), Filterable {

    private var filteredList: List<WebPage> = emptyList()

    @Inject internal lateinit var bookmarkRepository: BookmarkRepository
    @Inject internal lateinit var userPreferences: UserPreferences
    @Inject internal lateinit var historyRepository: HistoryRepository
    @Inject @field:DatabaseScheduler internal lateinit var databaseScheduler: Scheduler
    @Inject @field:NetworkScheduler internal lateinit var networkScheduler: Scheduler
    @Inject @field:MainScheduler internal lateinit var mainScheduler: Scheduler
    @Inject internal lateinit var searchEngineProvider: SearchEngineProvider

    private var allBookmarks: List<Bookmark.Entry> = emptyList()
    private val searchFilter = SearchFilter(this)

    private val searchIcon = context.drawable(R.drawable.ic_search)
    private val webPageIcon = context.drawable(R.drawable.ic_history)
    private val bookmarkIcon = context.drawable(R.drawable.ic_bookmark_border)
    private var suggestionsRepository: SuggestionsRepository

    /**
     * The listener that is fired when the insert button on a [SearchSuggestion] is clicked.
     */
    var onSuggestionInsertClick: ((WebPage) -> Unit)? = null

    private val onClick = View.OnClickListener {
        onSuggestionInsertClick?.invoke(it.tag as WebPage)
    }

    private val layoutInflater = LayoutInflater.from(context)

    init {
        context.injector.inject(this)

        suggestionsRepository = if (isIncognito) {
            NoOpSuggestionsRepository()
        } else {
            searchEngineProvider.provideSearchSuggestions()
        }

        refreshBookmarks()

        searchFilter.input().results()
            .subscribeOn(databaseScheduler)
            .observeOn(mainScheduler)
            .subscribe(::publishResults)
    }

    fun refreshPreferences() {
        suggestionsRepository = if (isIncognito) {
            NoOpSuggestionsRepository()
        } else {
            searchEngineProvider.provideSearchSuggestions()
        }
    }

    fun refreshBookmarks() {
        bookmarkRepository.getAllBookmarksSorted()
            .subscribeOn(databaseScheduler)
            .subscribe { list ->
                allBookmarks = list
            }
    }

    override fun getCount(): Int = filteredList.size

    override fun getItem(position: Int): Any? {
        if (position > filteredList.size || position < 0) {
            return null
        }
        return filteredList[position]
    }

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: SuggestionViewHolder
        val finalView: View

        if (convertView == null) {
            finalView = layoutInflater.inflate(R.layout.two_line_autocomplete, parent, false)
            holder = SuggestionViewHolder(finalView)
            finalView.tag = holder
        } else {
            finalView = convertView
            holder = convertView.tag as SuggestionViewHolder
        }
        val webPage: WebPage = filteredList[position]

        holder.titleView.text = webPage.title
        holder.urlView.text = webPage.url

        val image = when (webPage) {
            is Bookmark -> bookmarkIcon
            is SearchSuggestion -> searchIcon
            is HistoryEntry -> webPageIcon
        }

        holder.imageView.setImageDrawable(image)

        holder.insertSuggestion.tag = webPage
        holder.insertSuggestion.setOnClickListener(onClick)

        return finalView
    }

    override fun getFilter(): Filter = searchFilter

    private fun publishResults(list: List<WebPage>?) {
        if (list == null) {
            notifyDataSetChanged()
            return
        }
        if (list != filteredList) {
            filteredList = list
            notifyDataSetChanged()
        }
    }

    private fun getBookmarksForQuery(query: String): Single<List<Bookmark.Entry>> =
            Single.fromCallable {
                var choice = 5
                choice = userPreferences.suggestionChoice.value + 3

            (allBookmarks.filter {
                it.title.toLowerCase(Locale.getDefault()).startsWith(query)
            } + allBookmarks.filter {
                it.url.contains(query)
            }).distinct().take(choice)
        }

    private fun Observable<CharSequence>.results(): Flowable<List<WebPage>> = this
        .toFlowable(BackpressureStrategy.LATEST)
        .map { it.toString().toLowerCase(Locale.getDefault()).trim() }
        .filter(String::isNotEmpty)
        .share()
        .compose { upstream ->
            val searchEntries = upstream
                .flatMapSingle(suggestionsRepository::resultsForSearch)
                .subscribeOn(networkScheduler)
                .startWith(emptyList<List<SearchSuggestion>>())
                .share()

            val bookmarksEntries = upstream
                .flatMapSingle(::getBookmarksForQuery)
                .subscribeOn(databaseScheduler)
                .startWith(emptyList<List<Bookmark.Entry>>())
                .share()

            val historyEntries = upstream
                .flatMapSingle(historyRepository::findHistoryEntriesContaining)
                .subscribeOn(databaseScheduler)
                .startWith(emptyList<HistoryEntry>())
                .share()

            // Entries priority and ideal count:
            // Bookmarks - 2
            // History - 2
            // Search - 1

            bookmarksEntries
                .join(
                    historyEntries,
                    { bookmarksEntries },
                    { historyEntries }
                ) { t1, t2 -> Pair(t1, t2) }
                .compose { bookmarksAndHistory ->
                    bookmarksAndHistory.join(
                        searchEntries,
                        { bookmarksAndHistory },
                        { searchEntries }
                    ) { (bookmarks, history), t2 ->
                        Triple(bookmarks, history, t2)
                    }
                }
        }
            .map { (bookmarks, history, searches) ->
                var choice = 5
                choice = userPreferences.suggestionChoice.value + 3
                val bookmarkCount = choice - 2.coerceAtMost(history.size) - 1.coerceAtMost(searches.size)
                val historyCount = choice - bookmarkCount.coerceAtMost(bookmarks.size) - 1.coerceAtMost(searches.size)
                val searchCount = choice - bookmarkCount.coerceAtMost(bookmarks.size) - historyCount.coerceAtMost(history.size)

                val results = bookmarks.take(bookmarkCount) + history.take(historyCount) + searches.take(searchCount)
                // Reverse results if needed
                if (userPreferences.toolbarsBottom) results.reversed() else results
        }

    private class SearchFilter(
        private val suggestionsAdapter: SuggestionsAdapter
    ) : Filter() {

        private val publishSubject = PublishSubject.create<CharSequence>()

        fun input(): Observable<CharSequence> = publishSubject.hide()

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint?.isBlank() != false) {
                return FilterResults()
            }
            publishSubject.onNext(constraint.trim())

            return FilterResults().apply { count = 1 }
        }

        override fun convertResultToString(resultValue: Any) = (resultValue as WebPage).url

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) =
            suggestionsAdapter.publishResults(null)
    }

}
