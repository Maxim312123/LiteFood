package com.diplomaproject.litefood.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.FoodSection
import com.diplomaproject.litefood.databinding.ItemFoodSectionBinding
import com.diplomaproject.litefood.fragments.view_models.MainFragmentViewModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FoodSectionAdapter(
    private val sections: MutableList<FoodSection>,
    private val viewModel: MainFragmentViewModel
) : RecyclerView.Adapter<FoodSectionAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFoodSectionBinding.bind(itemView)
        val imageFoodSection = binding.imageFoodSection
        val titleFoodSection = binding.titleFoodSection
      //  val skeletonCardView = binding.skeletonCardView

        fun bind(foodSection: FoodSection) {
            titleFoodSection.text = foodSection.title
            bindImage(foodSection)
        }

        private fun bindImage(foodSection: FoodSection) {
            loadImage(foodSection)
        }

        private fun loadImage(foodSection: FoodSection) {
            if (foodSection.imageURL == null) {
                val fileRef: StorageReference =
                    FirebaseStorage.getInstance().getReference(foodSection.imagePath)
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    foodSection.imageURL = uri.toString()
                    Glide
                        .with(itemView.context)
                        .load(uri.toString())
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                p0: GlideException?,
                                p1: Any?,
                                p2: Target<Drawable>?,
                                p3: Boolean
                            ): Boolean {
                                //skeletonCardView.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(
                                p0: Drawable?,
                                p1: Any?,
                                p2: Target<Drawable>?,
                                p3: DataSource?,
                                p4: Boolean
                            ): Boolean {
                                //skeletonCardView.visibility = View.GONE
                                return false
                            }

                        })
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageFoodSection)
                }
            } else {
                Glide
                    .with(itemView.context)
                    .load(foodSection.imageURL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            p0: GlideException?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: Boolean
                        ): Boolean {
                          //  skeletonCardView.visibility = View.INVISIBLE
                            return false
                        }

                        override fun onResourceReady(
                            p0: Drawable?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: DataSource?,
                            p4: Boolean
                        ): Boolean {
                           // skeletonCardView.visibility = View.INVISIBLE
                            return false
                        }

                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(imageFoodSection)
            }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ItemViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_food_section, viewGroup, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = sections.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val section = sections[position]
        holder.bind(section)

        holder.binding.cardFoodSection.setOnClickListener {
            viewModel.onFoodSectionClicked(position)
        }
    }

    fun getListSize() = sections.size
}