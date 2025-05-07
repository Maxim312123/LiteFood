package com.diplomaproject.litefood.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.FoodCategory
import com.diplomaproject.litefood.databinding.ItemFoodCategoriesBinding
import com.diplomaproject.litefood.utils.FoodCategoryDiffUtils
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FoodCategoryAdapter(
    private val context: Context,
    private var foodCategories: MutableList<FoodCategory>,
    private var onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<FoodCategoryAdapter.ItemViewHolder>() {

    private lateinit var diffUtilsResult: DiffUtil.DiffResult

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFoodCategoriesBinding.bind(itemView)
        val title = binding.titleCategory
        val imageCategory = binding.imageCategory
        val progressBar = binding.progressBar
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ItemViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_food_categories, viewGroup, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val foodCategory = foodCategories[position]
        holder.title.text = foodCategory.title
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()
        val fileRef: StorageReference? =
            foodCategory.imageURL?.let { storageRef.child(it) }
        fileRef?.downloadUrl?.addOnSuccessListener(object : OnSuccessListener<Uri?> {
            override fun onSuccess(uri: Uri?) {
                Glide
                    .with(context)
                    .load(uri.toString())
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            p0: GlideException?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: Boolean
                        ): Boolean {
                            holder.progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            p0: Drawable?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: DataSource?,
                            p4: Boolean
                        ): Boolean {
                            holder.progressBar.visibility = View.GONE
                            return false
                        }

                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.imageCategory)
            }
        })
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return foodCategories.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun updateList(newFoodCategories: MutableList<FoodCategory>) {
        diffUtilsResult = DiffUtil.calculateDiff(FoodCategoryDiffUtils(foodCategories, newFoodCategories))
        diffUtilsResult.dispatchUpdatesTo(this)
        this.foodCategories = newFoodCategories
    }

}