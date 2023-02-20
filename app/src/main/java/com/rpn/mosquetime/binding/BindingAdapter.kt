package com.rpn.mosquetime.ui.binding

import android.animation.ObjectAnimator
import android.os.Build
import android.support.v4.media.session.PlaybackStateCompat.*
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.*
import android.widget.*
import androidx.databinding.BindingAdapter
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.transition.ViewPropertyTransition
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.rpn.mosquetime.extensions.*
import com.rpn.mosquetime.utils.createGradientDrawable
import java.util.*

/**
 * @param view is the target view.
 * @param albumId is the id that will be used to get the image form the DB.
 * @param recycled, if it is true the placeholder will be the last song cover selected.
 * */
@BindingAdapter("app:pathImage")
fun setImagePath(
    view: ImageView,
    pathImage: String?
) {
//    val drawable = getDrawable(view.context, R.drawable.erfah_icon_with_background)
    val drawable = createGradientDrawable()
    val fadeAnimation: ViewPropertyTransition.Animator =
        object : ViewPropertyTransition.Animator {
            override fun animate(view: View?) {
                val fadeAnim: ObjectAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
                fadeAnim.setDuration(500);
                fadeAnim.start();
            }

        }
    val shimmer =
        Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
            .setDuration(1800) // how long the shimmering animation takes to do one full sweep
            .setBaseAlpha(0.7f) //the alpha of the underlying children
            .setHighlightAlpha(0.8f) // the shimmer alpha amount
            .setDirection(Shimmer.Direction.TOP_TO_BOTTOM)
            .setAutoStart(true)
            .build()

// This is the placeholder for the imageView
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(shimmer)
    }
    try {
        Glide.with(view)
            .load(pathImage)
            .placeholder(shimmerDrawable)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .transition(GenericTransitionOptions.with(fadeAnimation))
            .error(drawable)
            .into(view)

    } catch (e: Exception) {
        Log.d("SetImagePath", "glide ${e.message}")
    }

}


@BindingAdapter("app:autoStartMarquee")
fun setAutoStartMarquee(textView: TextView, autoStartMarquee: Boolean) {
    textView.isSelected = autoStartMarquee
}
@BindingAdapter("app:html")
fun setTextHtml(view: TextView, html: String?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && html != null) {
        view.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
    }else{
        view.text = html
        view.isSelected = true
        view.ellipsize = TextUtils.TruncateAt.MARQUEE
    }
}


