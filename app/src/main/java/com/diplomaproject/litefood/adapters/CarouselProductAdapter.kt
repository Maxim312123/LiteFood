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
import com.diplomaproject.litefood.data.CarouselProduct
import com.diplomaproject.litefood.data.SalesLeaderCarouselProduct
import com.diplomaproject.litefood.data.SpicyCarouselProduct
import com.diplomaproject.litefood.data.VegetarianCarouselProduct
import com.diplomaproject.litefood.databinding.CarouselItemSalesLeaderProductBinding
import com.diplomaproject.litefood.databinding.CarouselItemSpicyProductBinding
import com.diplomaproject.litefood.databinding.CarouselItemVegetarianProductBinding
import com.diplomaproject.litefood.fragments.view_models.MainFragmentViewModel
import com.google.android.material.carousel.MaskableFrameLayout
import com.google.android.material.math.MathUtils.lerp
import com.google.firebase.storage.FirebaseStorage

private const val TYPE_SALES_LEADER_PRODUCT = 0
private const val TYPE_VEGETARIAN_PRODUCT = 1
private const val TYPE_SPICY_PRODUCT = 2

class CarouselProductAdapter(
    private val viewModel: MainFragmentViewModel,
    private val carouselProducts: MutableList<out CarouselProduct>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SalesLeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CarouselItemSalesLeaderProductBinding.bind(itemView)

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
            loadProductImage(product, ivProduct)
        }
    }

    inner class VegetarianProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = CarouselItemVegetarianProductBinding.bind(view)

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
            loadProductImage(product, ivProduct)
        }
    }

    inner class SpicyProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = CarouselItemSpicyProductBinding.bind(view)

        val ivProduct = binding.ivProduct
        val tvProductName = binding.tvProductName
        val tvProductPrice = binding.tvProductPrice
        val cardProduct = binding.cardProduct

        fun bind(product: SpicyCarouselProduct) {
            tvProductName.text = product.name
            tvProductPrice.text = itemView.resources.getString(
                R.string.currency,
                String.format("%.2f", product.pricePerUnit)
            )
            loadProductImage(product, ivProduct)
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_SALES_LEADER_PRODUCT -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.carousel_item_sales_leader_product, viewGroup, false)
                return SalesLeaderViewHolder(view)
            }

            TYPE_VEGETARIAN_PRODUCT -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.carousel_item_vegetarian_product, viewGroup, false)
                return VegetarianProductViewHolder(view)
            }

            TYPE_SPICY_PRODUCT -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.carousel_item_spicy_product, viewGroup, false)
                return SpicyProductViewHolder(view)
            }

            else -> throw IllegalArgumentException("Unexpected ViewHolder type")
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

            is SpicyProductViewHolder -> {
                val product = carouselProducts[position] as SpicyCarouselProduct
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
            is SpicyCarouselProduct -> TYPE_SPICY_PRODUCT
            else -> throw IllegalArgumentException("Unexpected ViewHolder type")
        }
    }

    private fun loadProductImage(product: CarouselProduct, imageView: ImageView) {
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

            is SpicyProductViewHolder -> {
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

            is SpicyProductViewHolder -> {
                viewHolder.cardProduct.setOnClickListener {
                   viewModel.onSpicyProductCLick(position)
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