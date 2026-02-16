package tv.projectivy.plugin.wallpaperprovider.sample

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.butch708.projectivy.tvbgsuite.R
import kotlin.math.roundToInt

class StarRatingPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private var useTenStars = true
    private val defaultRating = 7.0f
    
    private var mCurrentRating: Float = defaultRating

    init {
        layoutResource = R.layout.preference_star_rating
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        
        holder.itemView.isFocusable = false
        holder.itemView.isClickable = false
        
        val titleView = holder.findViewById(android.R.id.title) as? TextView
        val summaryView = holder.findViewById(android.R.id.summary) as? TextView
        val ratingBar = holder.findViewById(R.id.rating_bar) as? RatingBar
        
        titleView?.text = title
        updateSummaryText(summaryView, mCurrentRating)
        
        ratingBar?.let { rb ->
            rb.isFocusable = true
            
            if (useTenStars) {
                rb.numStars = 10
                rb.stepSize = 1.0f
                rb.rating = mCurrentRating
            } else {
                rb.numStars = 5
                rb.stepSize = 0.5f
                rb.rating = mCurrentRating / 2.0f
            }
            
            rb.setOnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    val actualValue = if (useTenStars) rating else rating * 2.0f
                    val roundedValue = (actualValue * 10.0f).roundToInt() / 10.0f
                    
                    if (callChangeListener(roundedValue)) {
                        mCurrentRating = roundedValue
                        persistFloat(mCurrentRating)
                        updateSummaryText(summaryView, mCurrentRating)
                    }
                }
            }
            
            rb.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
                val scale = if (hasFocus) 1.1f else 1.0f
                v.animate().scaleX(scale).scaleY(scale).setDuration(150).start()
            }
        }
    }
    
    private fun updateSummaryText(view: TextView?, rating: Float) {
        val text = "Minimum Rating: $rating / 10"
        view?.text = text
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        if (defaultValue == null) {
            mCurrentRating = getPersistedFloat(defaultRating)
        } else {
            mCurrentRating = defaultValue as? Float ?: defaultRating
            persistFloat(mCurrentRating)
        }
    }
}