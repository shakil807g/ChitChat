package com.shakil.chitchat

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.shakil.chitchat.extension.loadCircularImage

@BindingAdapter(
    "circleImageUrl"
)
fun loadCircleImage(
    view: ImageView,
    image: String?
) {
    view.loadCircularImage(image)
//    view.load(image){
//        crossfade(true)
//        placeholder(R.drawable.circle_place_holder)
//        transformations(CircleCropTransformation())
//    }

}