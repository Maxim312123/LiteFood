package com.diplomaproject.litefood.utils

import androidx.recyclerview.widget.DiffUtil
import com.diplomaproject.litefood.data.FoodCategory

class FoodCategoryDiffUtils(
    private val oldList: List<FoodCategory>,
    private val newList: List<FoodCategory>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.imageURL == newItem.imageURL
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        val diff = mutableMapOf<String, Any>()

        if (oldItem.title != newItem.title) {
            diff["title"] = newItem.title.toString()
        }

        if (oldItem.imageURL != newItem.imageURL) {
            diff["imageURL"] = newItem.imageURL.toString()
        }

        return if (diff.isNotEmpty()) diff else null
    }

}