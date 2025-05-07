package com.diplomaproject.litefood.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.databinding.ItemShoppingBasketBinding
import com.diplomaproject.litefood.fragments.view_models.CartViewModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.DecimalFormat

class CartAdapter(
    private val cartViewModel: CartViewModel,
    var cart: MutableList<CartProduct>,
    val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<CartAdapter.ItemViewHolder>() {

    init {
        cart.sortByDescending { it.timeStamp }
        setProductForBuyingAmount()
    }

    interface OnItemClickListener {
        fun onCardClick(position: Int)
        fun onOverFlowMenuClick(view: View, position: Int)
        fun onDecreaseAmountProductClick(product: CartProduct, position: Int)
        fun onIncreaseAmountProductClick(product: CartProduct, position: Int)
        fun onCheckboxClick(position: Int, isChecked: Boolean)
        fun onButtonBuyProductClick(position: Int)
    }

    inner class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding = ItemShoppingBasketBinding.bind(itemView)

        val name = binding.name
        val checkBoxIsForBuying = binding.isForBuying
        val price = binding.price
        val image = binding.productImage
        val amount = binding.amount
        val decreaseAmount = binding.decreaseAmount
        val increaseAmount = binding.increaseAmount
        val overflowMenu = binding.overflowMenu
        val productCardView = binding.productCardView
        val btnBuyProduct = binding.btnBuyProduct

        fun onBind(product: CartProduct) {
            name.text = product.name
            checkBoxIsForBuying.isChecked = product.forBuying
            amount.setText(product.amount.toString())

            val priceFormat = DecimalFormat("0.00").format(product.totalPrice)
            price.setText("${priceFormat} руб")

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.getReference()
            val fileRef: StorageReference = storageRef.child(product.imageURL)

            fileRef.downloadUrl?.addOnSuccessListener(object : OnSuccessListener<Uri?> {
                override fun onSuccess(uri: Uri?) {
                    Glide
                        .with(itemView.context)
                        .load(uri.toString())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(image)
                }
            })
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ItemViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_shopping_basket, viewGroup, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        var shoppingBasketProduct = cart[position]
        holder.onBind(shoppingBasketProduct)
        setupViewListeners(holder, shoppingBasketProduct, position)
    }

    override fun onBindViewHolder(
        @NonNull holder: ItemViewHolder,
        position: Int,
        @NonNull payloads: List<Any>
    ) {
        if (!payloads.isEmpty()) {
            val payload = payloads[0]
            if (payload is String && "AMOUNT_CHANGED" == payload) {
                holder.amount.setText(cart.get(position).amount.toString())
                val priceFormat = DecimalFormat("0.00").format(cart[position].totalPrice)
                holder.price.setText("${priceFormat} руб")
            } else if (payload is String && "CHECKBOX_CHANGED" == payload) {
                holder.checkBoxIsForBuying.isChecked = cart.get(position).forBuying
            } else if (payload is String && "PRICE_CHANGED" == payload) {
                val priceFormat = DecimalFormat("0.00").format(cart[position].totalPrice)
                holder.price.setText("${priceFormat} руб")
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int {
        return cart.size
    }

    private fun setupViewListeners(
        holder: ItemViewHolder,
        product: CartProduct,
        position: Int
    ) {
        holder.checkBoxIsForBuying.setOnCheckedChangeListener { _, isChecked ->
            onItemClickListener.onCheckboxClick(position, isChecked)
        }

        holder.overflowMenu.setOnClickListener { view ->
            onItemClickListener.onOverFlowMenuClick(view, position)
        }
        holder.productCardView.setOnClickListener {
            onItemClickListener.onCardClick(position)
        }

        holder.decreaseAmount.setOnClickListener {
            onItemClickListener.onDecreaseAmountProductClick(
                product,
                position
            )
        }

        holder.increaseAmount.setOnClickListener {
            onItemClickListener.onIncreaseAmountProductClick(
                product,
                position
            )
        }

        holder.btnBuyProduct.setOnClickListener {
            onItemClickListener.onButtonBuyProductClick(position)
        }
    }

    private fun setProductForBuyingAmount() {
        cart.forEach { product ->
            if (product.forBuying) {
                cartViewModel.addProductForBuying(product)
            }
        }
    }

    fun updateList(products: MutableList<CartProduct>) {
        cart = products
        notifyDataSetChanged()
    }

    fun getProductByPosition(position: Int): CartProduct {
        return cart.get(position)
    }

    fun isBasketEmpty(): Boolean {
        return cart.isEmpty()
    }

    fun removeProductByPosition(position: Int) {
        cart.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, cart.size);
    }

    fun getBasketSize(): Int {
        return cart.size
    }

    fun changProductAmount(position: Int, newAmount: Int) {
        val changedProduct = cart[position]
        changedProduct.amount = newAmount
        notifyItemChanged(position, "AMOUNT_CHANGED")
    }


}