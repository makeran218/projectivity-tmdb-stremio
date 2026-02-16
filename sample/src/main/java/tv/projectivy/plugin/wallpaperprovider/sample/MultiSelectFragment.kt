package tv.projectivy.plugin.wallpaperprovider.sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.Row
import androidx.leanback.widget.RowPresenter
import androidx.leanback.widget.VerticalGridPresenter
import com.butch708.projectivy.tvbgsuite.R

class MultiSelectFragment : VerticalGridSupportFragment(), OnItemViewClickedListener {

    private lateinit var mAdapter: ArrayObjectAdapter
    private var items: ArrayList<String> = ArrayList()
    private var type: String = ""

    companion object {
        private const val COLUMNS = 4
        private const val TAG = "MultiSelectFragment"
        const val TYPE_GENRE = "GENRE"
        const val TYPE_AGE = "AGE"

        fun newInstance(type: String, items: ArrayList<String>): MultiSelectFragment {
            val fragment = MultiSelectFragment()
            val args = Bundle()
            args.putString("TYPE", type)
            args.putStringArrayList("ITEMS", items)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            PreferencesManager.init(requireContext())

            type = arguments?.getString("TYPE") ?: ""
            title = if (type == TYPE_GENRE) "Select Genre(s)" else "Select Age Rating(s)"

            val gridPresenter = VerticalGridPresenter().apply {
                numberOfColumns = COLUMNS
                shadowEnabled = false
            }
            setGridPresenter(gridPresenter)

            mAdapter = ArrayObjectAdapter(ItemPresenter())
            adapter = mAdapter

            arguments?.getStringArrayList("ITEMS")?.let {
                items = it
                updateAdapter()
            }

            onItemViewClickedListener = this
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            Toast.makeText(context, "Error initializing MultiSelect: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateAdapter() {
        mAdapter.clear()
        val defaultOption = if (type == TYPE_GENRE) "All" else "Any"
        mAdapter.add(defaultOption)
        items.forEach { mAdapter.add(it) }
    }

    override fun onItemClicked(
        itemViewHolder: Presenter.ViewHolder?,
        item: Any?,
        rowViewHolder: RowPresenter.ViewHolder?,
        row: Row?
    ) {
        if (item is String) {
            handleItemClick(item)
            mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size())
        }
    }

    private fun handleItemClick(itemStr: String) {
        try {
            val defaultOption = if (type == TYPE_GENRE) "All" else "Any"
            var currentFilter = if (type == TYPE_GENRE) PreferencesManager.genreFilter else PreferencesManager.ageFilter
            
            // If clicking "All"/"Any", clear filter
            if (itemStr == defaultOption) {
                saveFilter("")
                return
            }

            val currentSelection = if (currentFilter.isEmpty()) mutableSetOf() else currentFilter.split(",").toMutableSet()

            if (currentSelection.contains(itemStr)) {
                currentSelection.remove(itemStr)
            } else {
                currentSelection.add(itemStr)
            }

            val newFilter = currentSelection.joinToString(",")
            saveFilter(newFilter)

        } catch (e: Exception) {
            Log.e(TAG, "Error handling click", e)
        }
    }

    private fun saveFilter(filter: String) {
        if (type == TYPE_GENRE) {
            PreferencesManager.genreFilter = filter
        } else {
            PreferencesManager.ageFilter = filter
        }
        notifySettingsChanged()
    }

    private fun notifySettingsChanged() {
        (requireActivity() as? SettingsActivity)?.requestWallpaperUpdate()
    }

    fun isItemChecked(itemStr: String): Boolean {
        val currentFilter = if (type == TYPE_GENRE) PreferencesManager.genreFilter else PreferencesManager.ageFilter
        val defaultOption = if (type == TYPE_GENRE) "All" else "Any"

        if (itemStr == defaultOption) return currentFilter.isEmpty()
        
        if (currentFilter.isEmpty()) return false
        
        val selection = currentFilter.split(",")
        return selection.contains(itemStr)
    }

    inner class ItemPresenter : Presenter() {
        override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_year, parent, false) as TextView
            view.setOnFocusChangeListener { _, hasFocus ->
                val scale = if (hasFocus) 1.1f else 1.0f
                view.animate().scaleX(scale).scaleY(scale).setDuration(150).start()
            }
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
            val str = item as? String ?: return
            val tv = viewHolder.view as TextView
            tv.text = str
            tv.isSelected = isItemChecked(str)
            
            val scale = if (tv.hasFocus()) 1.1f else 1.0f
            tv.scaleX = scale
            tv.scaleY = scale
        }

        override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        }
    }
}