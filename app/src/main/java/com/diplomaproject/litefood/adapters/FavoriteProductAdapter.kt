package com.diplomaproject.litefood.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.databinding.ItemFavoriteProductBinding
import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class FavoriteProductAdapter(
    private val context: Context,
    private val onItemClickListener: OnItemCLickListener,
) : RecyclerView.Adapter<FavoriteProductAdapter.FavoriteProductViewHolder>() {

    private var favoriteProducts: MutableList<Product> = mutableListOf()
    private val realtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()

    interface OnItemCLickListener {
        fun onPopupMenuClick(favoriteProduct: Product, position: Int)
        fun onButtonClick(favoriteProduct: Product, position: Int)
    }

    inner class FavoriteProductViewHolder(view: View) : ViewHolder(view) {
        val binding = ItemFavoriteProductBinding.bind(view)
        val popupMenu = binding.popupMenu
        val name = binding.name
        val price = binding.price
        val image = binding.productImage
        val button = binding.addToBasket
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): FavoriteProductViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_favorite_product, parent, false)
        return FavoriteProductViewHolder(view)
    }

    override fun getItemCount(): Int {
        return favoriteProducts.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: FavoriteProductViewHolder, position: Int) {
        val favoriteProduct = favoriteProducts[position]
        bindProductData(holder, favoriteProduct)
        updateButtonState(favoriteProduct, holder)
        setupViewsListener(favoriteProduct, holder, position)
    }

    private fun setupViewsListener(
        favoriteProduct: Product,
        holder: FavoriteProductViewHolder,
        position: Int
    ) {
        holder.popupMenu.setOnClickListener {
            shopPopupMenu(favoriteProduct, holder)
        }

        holder.button.setOnClickListener {
            if (!favoriteProduct.isAddedToBasket) {
                addToShoppingBasket(favoriteProduct, position)
            } else {
                removeFromShoppingBasket(favoriteProduct, position)
            }
        }

    }

    private fun addToShoppingBasket(
        favoriteProduct: Product,
        position: Int
    ) {
        realtimeDatabaseManager.writeToCart(CartProduct(favoriteProduct))

        favoriteProduct.isAddedToBasket = true
        notifyItemChanged(position)

        // setAddedProductState(holder)

        //onItemClickListener.onButtonClick(favoriteProduct, holder.position)
    }

    private fun removeFromShoppingBasket(
        favoriteProduct: Product,
        position: Int
    ) {
        realtimeDatabaseManager.deleteProductFromCart(favoriteProduct)

        favoriteProduct.isAddedToBasket = false
        notifyItemChanged(position)


        //onItemClickListener.onButtonClick(favoriteProduct, holder.position)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun bindProductData(
        holder: FavoriteProductViewHolder,
        favoriteProduct: Product
    ) {
        holder.name.text = favoriteProduct.name
        holder.price.text = favoriteProduct.pricePerUnit.toString()
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()
        val fileRef: StorageReference = storageRef.child(favoriteProduct.imageURL)

        fileRef.downloadUrl.addOnSuccessListener { uri ->
            Glide
                .with(holder.itemView.context)
                .load(uri.toString())
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image)
        }
    }

    private fun updateButtonState(
        favoriteProduct: Product,
        holder: FavoriteProductViewHolder
    ) {
        if (favoriteProduct.isAddedToBasket) {
            setAddedProductState(holder)
        } else {
            setNotAddedProductState(holder)
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun setAddedProductState(holder: FavoriteProductViewHolder) {
        holder.button.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.white)
        holder.button.setTextColor(R.color.black)
        holder.button.setText("Добавлен")
    }

    private fun setNotAddedProductState(holder: FavoriteProductViewHolder) {
        holder.button.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.black)
        holder.button.setTextColor(ContextCompat.getColor(context, R.color.white))
        holder.button.setText("Добавить")
    }

    private fun shopPopupMenu(
        favoriteProduct: Product,
        holder: FavoriteProductViewHolder
    ) {
        val popupMenu = PopupMenu(context, holder.popupMenu)
        popupMenu.menuInflater.inflate(R.menu.context_menu_favorite_product, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete_product -> {
                    onItemClickListener.onPopupMenuClick(
                        favoriteProduct,
                        holder.position
                    )
                    true
                }

                else -> {
                    false
                }
            }
        }
        popupMenu.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(products: MutableList<Product>) {
        favoriteProducts = products
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        favoriteProducts.removeAt(position)
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clearList() {
        favoriteProducts.clear()
        notifyDataSetChanged()
    }
}