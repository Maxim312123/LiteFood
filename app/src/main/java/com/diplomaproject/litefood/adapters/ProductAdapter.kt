package com.diplomaproject.litefood.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.databinding.ItemProductBinding
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.DecimalFormat

private const val PAYLOAD_COUNT_AND_PRICE = "payload_count_and_price"
private const val PAYLOAD_PRODUCT_IS_ADDED = "Product is added"
private const val PAYLOAD_PRODUCT_IS_DELETED_FROM_BASKET = "Product is deleted from basket"
private const val PAYLOAD_PRODUCT_IS_ADDED_TO_FAVORITE = "Product is added to favorite"
private const val PAYLOAD_PRODUCT_IS_DELETED_FROM_FAVORITE = "Product is deleted from favorite"

class ProductAdapter(
    private val context: Context,
    private val onProductCardViewClickListener: OnProductCardViewClickListener
) : RecyclerView.Adapter<ProductAdapter.ItemViewHolder>() {

    private var products: MutableList<Product> = mutableListOf()
    private val realtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()

    inner class ItemViewHolder(view: View) : ViewHolder(view) {
        val binding = ItemProductBinding.bind(view)

        val tvName = binding.name
        val tvPrice = binding.price
        val ivDrinkImage = binding.drinkImage
        val etCounter = binding.counter
        val ibDecreaseCount = binding.decreaseCount
        val ibIncreaseCount = binding.increaseCount
        val btnAddToBasket = binding.addToBasket
        val ivFavorite = binding.favorite
        val productCardView = binding.productCardView
        val progressBar = binding.progressBar

        @SuppressLint("SetTextI18n", "StringFormatInvalid")
        fun bind(product: Product) {
            tvName.text = product.name
            tvPrice.text = getFormattedTotalPrice(product)
            etCounter.setText(product.amount.toString())

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.getReference()
            val fileRef: StorageReference = storageRef.child(product.imageURL)
            fileRef.downloadUrl.addOnSuccessListener { uri ->
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
                            progressBar.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            p0: Drawable?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: DataSource?,
                            p4: Boolean
                        ): Boolean {
                            progressBar.visibility = View.GONE
                            return false
                        }

                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivDrinkImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)
        updateProductButtonState(product, holder)
        updateDrinkFavoriteImageState(product, holder)
        setupViewListeners(holder, product, position)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isNotEmpty()) {
            val payload = payloads[0]
            val product = products[position]

            if (payload.equals(PAYLOAD_COUNT_AND_PRICE)) {
                holder.etCounter.setText(product.amount.toString())
                holder.tvPrice.setText(getFormattedTotalPrice(product))
            } else if (payload.equals(PAYLOAD_PRODUCT_IS_ADDED)) {
                holder.etCounter.setText(product.amount.toString())
                setAddedProductButtonState(holder)
            } else if (payload.equals(PAYLOAD_PRODUCT_IS_DELETED_FROM_BASKET)) {
                setNotAddedProductButtonState(holder)
            } else if (payload.equals(PAYLOAD_PRODUCT_IS_ADDED_TO_FAVORITE)) {
                holder.ivFavorite.setBackgroundResource(R.drawable.ic_added_favorite)
            } else if (payload.equals(PAYLOAD_PRODUCT_IS_DELETED_FROM_FAVORITE)) {
                holder.ivFavorite.setBackgroundResource(R.drawable.ic_not_added_favorite)
            }

        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    private fun setupViewListeners(
        holder: ItemViewHolder,
        product: Product,
        position: Int
    ) {
        holder.ibIncreaseCount.setOnClickListener {
            var count = holder.etCounter.text.toString().toInt()
            if (count in 1..99) {
                count += 1
                product.amount = count
                notifyItemChanged(position, PAYLOAD_COUNT_AND_PRICE)
            }
        }

        holder.ibDecreaseCount.setOnClickListener {
            var count = holder.etCounter.text.toString().toInt()
            if (count in 2..99) {
                count -= 1
                product.amount = count
                notifyItemChanged(position, PAYLOAD_COUNT_AND_PRICE)
            }
        }

        holder.btnAddToBasket.setOnClickListener {
            if (!product.isAddedToBasket) {
                addProductToCart(product, position)
            } else {
                removeProductFromBasket(product, position)
            }
        }

        holder.ivFavorite.setOnClickListener {
            if (!product.isFavoriteProduct) {
                addProductToFavorites(product, position)
            } else {
                removeProductFromFavorites(product, position)
            }
        }

        holder.productCardView.setOnClickListener {
            onProductCardViewClickListener.onProductCardViewClick(position)
        }
    }

    private fun addProductToCart(
        product: Product,
        position: Int
    ) {
        realtimeDatabaseManager.writeToCart(CartProduct(product))
        product.isAddedToBasket = true
        product.resetCount()

        notifyItemChanged(position, PAYLOAD_PRODUCT_IS_ADDED)
    }

    @SuppressLint("ResourceAsColor")
    private fun removeProductFromBasket(
        product: Product,
        position: Int
    ) {
        realtimeDatabaseManager.deleteProductFromCart(product)

        product.isAddedToBasket = false
        notifyItemChanged(position, PAYLOAD_PRODUCT_IS_DELETED_FROM_BASKET)
    }

    @SuppressLint("ResourceAsColor")
    private fun setAddedProductButtonState(holder: ItemViewHolder) {
        holder.btnAddToBasket.setBackgroundResource(R.drawable.btn_add_product_disable)
        holder.btnAddToBasket.setTextColor(R.color.black)
        holder.btnAddToBasket.setText(R.string.button_state_added)

        holder.ibDecreaseCount.isEnabled = false
        holder.ibIncreaseCount.isEnabled = false
    }

    private fun setNotAddedProductButtonState(holder: ItemViewHolder) {
        holder.btnAddToBasket.setBackgroundResource(R.drawable.background_normal_button)
        holder.btnAddToBasket.setTextColor(
            ContextCompat.getColor(
                holder.itemView.context,
                R.color.white
            )
        )
        holder.btnAddToBasket.setText(R.string.add_button)

        holder.ibDecreaseCount.isEnabled = true
        holder.ibIncreaseCount.isEnabled = true
    }

    @SuppressLint("ResourceAsColor")
    private fun updateProductButtonState(product: Product, holder: ItemViewHolder) {
        if (product.isAddedToBasket) {
            setAddedProductButtonState(holder)
        } else {
            setNotAddedProductButtonState(holder)
        }
    }


    private fun updateDrinkFavoriteImageState(product: Product, holder: ItemViewHolder) {
        if (product.isFavoriteProduct) {
            setAddedDrinkImageState(holder)
        } else {
            setNotAddedDrinkImageState(holder)
        }
    }

    private fun setAddedDrinkImageState(holder: ItemViewHolder) {
        holder.ivFavorite.setBackgroundResource(R.drawable.ic_added_favorite)
    }

    private fun setNotAddedDrinkImageState(holder: ItemViewHolder) {
        holder.ivFavorite.setBackgroundResource(R.drawable.ic_not_added_favorite)
    }

    private fun addProductToFavorites(product: Product, position: Int) {
        product.isFavoriteProduct = true
        realtimeDatabaseManager.writeToFavoriteProducts(product = product)

        notifyItemChanged(position, PAYLOAD_PRODUCT_IS_ADDED_TO_FAVORITE)
        showToast(context.resources.getString(R.string.toast_product_is_added_to_favorite))
    }

    private fun removeProductFromFavorites(product: Product, position: Int) {
        product.isFavoriteProduct = false
        realtimeDatabaseManager.deleteFromFavoriteProducts(product)

        notifyItemChanged(position, PAYLOAD_PRODUCT_IS_DELETED_FROM_FAVORITE)
        showToast(context.resources.getString(R.string.toast_product_is_deleted_from_favorite))
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT)
            .show()
    }

    private fun getFormattedTotalPrice(product: Product): String {
        val totalPrice = product.totalPrice
        val priceFormat = DecimalFormat("0.00").format(totalPrice)
        val productPrice = context.resources.getString(R.string.currency, priceFormat)
        return productPrice
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDrinks(drinks: MutableList<Product>) {
        this.products = drinks
        notifyDataSetChanged()
    }

    fun getProductByPosition(position: Int): Product {
        return products[position]
    }

    interface OnProductCardViewClickListener {
        fun onProductCardViewClick(position: Int)
    }

}