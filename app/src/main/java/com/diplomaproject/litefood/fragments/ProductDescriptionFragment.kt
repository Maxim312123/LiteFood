package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.diplomaproject.litefood.ProductType
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.data.BaseProduct
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.databinding.FragmentProductDescriptionBinding
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val PARAM_PRODUCT = "product"

class ProductDescriptionFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentProductDescriptionBinding

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivProductImage: ImageView
    private lateinit var tvProductName: TextView
    private lateinit var tvIngredients: TextView
    private lateinit var tvMacronutrients: TextView
    private lateinit var tvCalorificValue: TextView
    private lateinit var tvProductWeight: TextView
    private lateinit var btnAddButton: Button

    private lateinit var product: BaseProduct
    private lateinit var realtimeDatabaseManager: FirebaseRealtimeDatabaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arguments ->
            product = arguments.getParcelable(PARAM_PRODUCT)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        initViews()
        init()
        setupToolbar()
        bindProductData()
        updateAddProductButtonVisibility()
        setupViewListeners()
    }

    private fun updateAddProductButtonVisibility() {
        if (product is Product) {
            if ((product as Product).isAddedToBasket) {
                return
            }
            btnAddButton.visibility = View.VISIBLE
        }
    }

    private fun initViews() {
        toolbar = binding.toolbar
        ivProductImage = binding.ivProductImage
        tvProductName = binding.tvProductName
        tvIngredients = binding.tvIngredients
        tvMacronutrients = binding.tvPFC
        tvCalorificValue = binding.tvCalorificValue
        tvProductWeight = binding.tvProductWeight
        btnAddButton = binding.btnAddProduct
    }

    private fun init() {
        realtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()
    }

    private fun setupViewListeners() {
        btnAddButton.setOnClickListener {
            if (product is Product) {
                realtimeDatabaseManager.writeToCart(CartProduct(product as Product))
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun bindProductData() {
        bindProductImage()
        bindProductName()
        bindProductIngredients()
        bindMacronutrientsProduct()
        bindProductCalorificValue()
        bindProductWeight()
    }

    @SuppressLint("StringFormatMatches")
    private fun bindProductWeight() {
        val productWeight = product.weight
        val styledText = getStyledProductWeight(productWeight)
        tvProductWeight.text = styledText
    }

    private fun getStyledProductWeight(productWeight: Int): Spanned {
        when (product.type) {
            ProductType.COFFEE.productType, ProductType.DRINKS.productType,
            ProductType.SOUP.productType -> {
                val volume = getString(R.string.productVolume, productWeight)
                return Html.fromHtml(volume, Html.FROM_HTML_MODE_LEGACY)
            }

            else -> {
                val weight = getString(R.string.productWeight, productWeight)
                return Html.fromHtml(weight, Html.FROM_HTML_MODE_LEGACY)
            }
        }
    }

    private fun bindProductImage() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.getReference()
        val fileRef: StorageReference = storageRef.child(product.imageURL)
        fileRef.downloadUrl.addOnSuccessListener { uri ->
            Glide
                .with(requireActivity())
                .load(uri.toString())
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivProductImage)
        }
    }

    private fun bindProductName() {
        tvProductName.text = product.name
    }

    private fun bindProductIngredients() {
        val productIngredients = product.ingredients

        val ingredients = StringBuilder()

        for (ingredientIndex in productIngredients.indices) {
            val ingredient = productIngredients[ingredientIndex]
            if (ingredientIndex == 0) {
                ingredients.append("• ${ingredient}")
            } else {
                ingredients.append("\n• ${ingredient}")
            }
        }

        tvIngredients.text = ingredients
    }

    @SuppressLint("DefaultLocale")
    private fun bindMacronutrientsProduct() {
        val productMacronutrients = product.macronutrients

        val protein = productMacronutrients.get("protein")
        val fats = productMacronutrients.get("fat")
        val carbs = productMacronutrients.get("carbs")

        val formattedProtein = String.format("%.1f", protein)
        val formattedFats = String.format("%.1f", fats)
        val formattedCarbohydrates = String.format("%.1f", carbs)

        tvMacronutrients.text = getString(
            R.string.macronutrients,
            formattedProtein,
            formattedFats,
            formattedCarbohydrates
        )
    }

    private fun bindProductCalorificValue() {
        val productCalorificValue = product.calorificValue
        val calorificValue = getString(R.string.calorific_value, productCalorificValue)
        val styledText = Html.fromHtml(calorificValue, Html.FROM_HTML_MODE_LEGACY)
        tvCalorificValue.setText(styledText)
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateMenu(p0: Menu, p1: MenuInflater) {}

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                parentFragmentManager.popBackStack(
                    "ProductDescriptionFragment",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                return true
            }
        }
        return false
    }

    companion object {
        @JvmStatic
        fun newInstance(clickedProduct: BaseProduct) = ProductDescriptionFragment().apply {
            arguments = Bundle().apply {
                putParcelable(PARAM_PRODUCT, clickedProduct)
            }
        }
    }
}