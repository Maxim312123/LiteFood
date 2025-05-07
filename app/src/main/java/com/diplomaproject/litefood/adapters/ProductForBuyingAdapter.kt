package com.diplomaproject.litefood.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.databinding.ItemOrderRegistrationProductBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProductForBuyingAdapter(
    private val products: MutableList<CartProduct>
) : RecyclerView.Adapter<ProductForBuyingAdapter.ProductForBuyingViewHolder>() {

    inner class ProductForBuyingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemOrderRegistrationProductBinding.bind(itemView)
        val tvProductPrice = binding.tvProductPrice
        val ivProductImage = binding.ivProductImage
        val tvProductAmount = binding.tvProductAmount

        @SuppressLint("DefaultLocale")
        fun bind(product: CartProduct) {
            tvProductPrice.text = itemView.context.getString(
                R.string.currency,
                String.format("%.2f", product.totalPrice)
            )
            tvProductAmount.text = "x${product.amount}"

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.getReference()
            val fileRef: StorageReference = storageRef.child(product.imageURL)

            fileRef.downloadUrl.addOnSuccessListener { uri ->
                Glide
                    .with(itemView.context)
                    .load(uri.toString())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivProductImage)
            }
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        position: Int
    ): ProductForBuyingViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_order_registration_product, viewGroup, false)
        return ProductForBuyingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductForBuyingViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = products.size

}