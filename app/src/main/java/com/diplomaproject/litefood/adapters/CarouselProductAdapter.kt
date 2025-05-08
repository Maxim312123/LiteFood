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
import com.diplomaproject.litefood.data.CarouselProduct
import com.diplomaproject.litefood.data.SalesLeaderCarouselProduct
import com.diplomaproject.litefood.data.VegetarianCarouselProduct
import com.diplomaproject.litefood.databinding.ItemSalesLeaderProductBinding
import com.diplomaproject.litefood.databinding.ItemVegetarianProductBinding
import com.diplomaproject.litefood.fragments.view_models.MainFragmentViewModel
import com.google.android.material.carousel.MaskableFrameLayout
import com.google.android.material.math.MathUtils.lerp
import com.google.firebase.storage.FirebaseStorage

private const val TYPE_SALES_LEADER_PRODUCT = 0
private const val TYPE_VEGETARIAN_PRODUCT = 1
private const val TYPE_SPICY_PRODUCT = 2

class CarouselProductAdapter(
    private val context: Context,
    private val viewModel: MainFragmentViewModel,
    private val carouselProducts: MutableList<out CarouselProduct>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SalesLeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemSalesLeaderProductBinding.bind(itemView)

        val ivProduct = binding.ivProduct
        val tvProductName = binding.tvProductName
        val tvProductPrice = binding.tvProductPrice
        val cardProduct = binding.cardProduct

        fun bind(product: SalesLeaderCarouselProduct) {
            tvProductName.text = product.name
            tvProductPrice.text = itemView.resources.getString(
                R.string.currency,
                String.format("%.2f", product.pricePerUnit)
            )
            bindImage(product)
        }

        private fun bindImage(product: SalesLeaderCarouselProduct) {
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

    inner class VegetarianProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemVegetarianProductBinding.bind(view)

        val ivProduct = binding.ivProduct
        val tvProductName = binding.tvProductName
        val tvProductPrice = binding.tvProductPrice
        val cardProduct = binding.cardProduct

        fun bind(product: VegetarianCarouselProduct) {
            tvProductName.text = product.name
            tvProductPrice.text = itemView.resources.getString(
                R.string.currency,
                String.format("%.2f", product.pricePerUnit)
            )
            bindImage(product)
        }

        private fun bindImage(product: VegetarianCarouselProduct) {
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
        viewType: Int
    ): RecyclerView.ViewHolder {
        if (viewType == TYPE_SALES_LEADER_PRODUCT) {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_sales_leader_product, viewGroup, false)
            return SalesLeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_vegetarian_product, viewGroup, false)
            return VegetarianProductViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (holder) {
            is SalesLeaderViewHolder -> {
                val product = carouselProducts[position] as SalesLeaderCarouselProduct
                holder.bind(product)
            }

            is VegetarianProductViewHolder -> {
                val product = carouselProducts[position] as VegetarianCarouselProduct
                holder.bind(product)
            }
        }
        changeCarouselItemState(holder)
        setupViewListeners(holder, position)
    }

    override fun getItemCount(): Int {
        return carouselProducts.size
    }

    override fun getItemViewType(position: Int): Int {
        val product = carouselProducts[position]
        return when (product) {
            is SalesLeaderCarouselProduct -> TYPE_SALES_LEADER_PRODUCT
            is VegetarianCarouselProduct -> TYPE_VEGETARIAN_PRODUCT
            else -> TYPE_SPICY_PRODUCT
        }
    }

    private fun changeCarouselItemState(viewHolder: RecyclerView.ViewHolder) {
        when (viewHolder) {
            is SalesLeaderViewHolder -> {
                (viewHolder.itemView as MaskableFrameLayout).setOnMaskChangedListener { maskRect ->
                    viewHolder.tvProductName.setTranslationX(maskRect.left)
                    viewHolder.tvProductName.setAlpha(lerp(1F, 0F, maskRect.left))
                    viewHolder.tvProductPrice.setTranslationX(maskRect.left)
                    viewHolder.tvProductPrice.setAlpha(lerp(1F, 0F, maskRect.left))
                }
            }

            is VegetarianProductViewHolder -> {
                (viewHolder.itemView as MaskableFrameLayout).setOnMaskChangedListener { maskRect ->
                    viewHolder.tvProductName.setTranslationX(maskRect.left)
                    viewHolder.tvProductName.setAlpha(lerp(1F, 0F, maskRect.left))
                    viewHolder.tvProductPrice.setTranslationX(maskRect.left)
                    viewHolder.tvProductPrice.setAlpha(lerp(1F, 0F, maskRect.left))
                }
            }
            else -> throw IllegalArgumentException("Unexpected ViewHolder type: ${viewHolder::class}")
        }

    }

    private fun setupViewListeners(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is SalesLeaderViewHolder -> {
                viewHolder.cardProduct.setOnClickListener {
                    viewModel.onSalesLeaderProductCLick(position)
                }
            }

            is VegetarianProductViewHolder -> {
                viewHolder.cardProduct.setOnClickListener {
                    viewModel.onVegetarianProductCLick(position)
                }
            }

            else -> throw IllegalArgumentException("Unexpected ViewHolder type: ${viewHolder::class}")
        }
    }

    private fun <T : RecyclerView.ViewHolder> castViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        clazz: Class<T>
    ): T {
        if (clazz.isInstance(viewHolder)) {
            return viewHolder as T
        } else {
            throw IllegalArgumentException("ViewHolder is not of type ${clazz.simpleName}")
        }
    }


    fun getProduct(position: Int) = carouselProducts.get(position)
}