package com.diplomaproject.litefood.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.FavoriteProductMainFragment
import com.diplomaproject.litefood.databinding.ItemFavoriteProductMainFragmentBinding
import com.google.firebase.storage.FirebaseStorage

class FavoriteProductMainFragmentAdapter(
    private val products: MutableList<FavoriteProductMainFragment>
) :
    RecyclerView.Adapter<FavoriteProductMainFragmentAdapter.FavoriteProductViewHolder>() {

    inner class FavoriteProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemFavoriteProductMainFragmentBinding.bind(itemView)

        val productCard = binding.productCard
        val ivProductImage = binding.ivProductImage


        fun bind(product: FavoriteProductMainFragment) {
            loadProductImage(product, ivProductImage)
        }

        private fun loadProductImage(product: FavoriteProductMainFragment, imageView: ImageView) {
            if (product.imageURL == null) {
                val storageRef = FirebaseStorage.getInstance().getReference(product.imagePath)
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    product.imageURL = uri.toString()
                    Glide
                        .with(imageView.context)
                        .load(uri.toString())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .into(imageView)
                }
            } else {
                Glide
                    .with(imageView.context)
                    .load(product.imageURL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(imageView)
            }
        }

    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        position: Int
    ): FavoriteProductViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_favorite_product_main_fragment, viewGroup, false)
        return FavoriteProductViewHolder(view)
    }

    override fun getItemCount() = products.size

    override fun onBindViewHolder(viewHolder: FavoriteProductViewHolder, position: Int) {
        val product = products[position]
        viewHolder.bind(product)

        viewHolder.productCard.setOnClickListener {

        }
    }
}