package com.diplomaproject.litefood.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.HitSalesProduct
import com.diplomaproject.litefood.databinding.ItemHitSalesProductBinding
import com.google.android.material.carousel.MaskableFrameLayout
import com.google.android.material.math.MathUtils.lerp
import com.google.firebase.storage.FirebaseStorage

class HitSalesProductsAdapter(
    private val context: Context,
    private val hitSalesProducts: MutableList<HitSalesProduct>
) :
    RecyclerView.Adapter<HitSalesProductsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemHitSalesProductBinding.bind(view)

        val ivProduct = binding.ivProduct
        val tvProductName = binding.tvProductName
        val tvProductPrice = binding.tvProductPrice

        fun bind(product: HitSalesProduct) {
            tvProductName.text = product.name
            tvProductPrice.text = itemView.resources.getString(
                R.string.currency,
                String.format("%.2f", product.pricePerUnit)
            )
            bindImage(product)
        }

        private fun bindImage(product: HitSalesProduct) {
            if (product.imageURL == null) {
                val storageRef = FirebaseStorage.getInstance().getReference(product.imagePath)
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    product.imageURL = uri.toString()
                    Glide
                        .with(itemView.context)
                        .load(uri.toString())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .into(ivProduct)
                }
            } else {
                Glide
                    .with(itemView.context)
                    .load(product.imageURL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(ivProduct)
            }
        }

    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        position: Int
    ): HitSalesProductsAdapter.ViewHolder {
        val view =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_hit_sales_product, viewGroup, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return hitSalesProducts.size
    }

    override fun onBindViewHolder(holder: HitSalesProductsAdapter.ViewHolder, position: Int) {
        val product = hitSalesProducts[position]
        holder.bind(product)

        (holder.itemView as MaskableFrameLayout).setOnMaskChangedListener { maskRect ->
            holder.tvProductName.setTranslationX(maskRect.left)
            holder.tvProductName.setAlpha(lerp(1F, 0F, maskRect.left))

            holder.tvProductPrice.setTranslationX(maskRect.left)
            holder.tvProductPrice.setAlpha(lerp(1F, 0F, maskRect.left))
        }
    }
}