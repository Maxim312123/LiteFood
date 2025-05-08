package com.diplomaproject.litefood.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.diplomaproject.litefood.ProductType
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.adapters.FoodCategoryAdapter
import com.diplomaproject.litefood.data.FoodCategory
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.databinding.FragmentFoodCategoriesBinding
import com.diplomaproject.litefood.repository.FirestoreDatabaseRepository
import com.google.android.material.appbar.MaterialToolbar


class FoodCategoryFragment : Fragment(), MenuProvider, FoodCategoryAdapter.OnItemClickListener {

    private lateinit var binding: FragmentFoodCategoriesBinding
    private lateinit var recyclerView: RecyclerView
    private var foodCategoryList: MutableList<FoodCategory> = mutableListOf()
    private lateinit var adapter: FoodCategoryAdapter
    private lateinit var toolbar: MaterialToolbar;
    private val firestoreDatabaseManager = FirestoreDatabaseRepository()
    private lateinit var pullToRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFoodCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupToolbar()
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        setupViewListeners()
    }

    private fun setupViewListeners() {
        pullToRefresh.setOnRefreshListener {
            firestoreDatabaseManager.fetchFoodCategories{ foodCategories ->
                adapter.updateList(foodCategories)
                pullToRefresh.isRefreshing = false
            }
        }
    }

    private fun init() {
        pullToRefresh = binding.pullToRefresh

        recyclerView = binding.rvFoodCategories
        adapter = FoodCategoryAdapter(requireActivity(), foodCategoryList, this)
        recyclerView.adapter = adapter
    }

    private fun loadImages() {
        firestoreDatabaseManager.fetchFoodCategories{ foodCategories ->
            adapter.updateList(foodCategories)
        }
    }

    private fun setupToolbar() {
        toolbar = binding.materialToolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.removeItem(R.id.search)
    }

    override fun onMenuItemSelected(p0: MenuItem): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        loadImages()
    }

    override fun onItemClick(position: Int) {
        val categoryName = ProductType.getCategoryNameByNumber(position)

        val foodCategoryToolbarTitles =
            resources.getStringArray(R.array.toolbar_title_food_category_name)
        val toolbarTitle = foodCategoryToolbarTitles[position]

        if (categoryName != null && toolbarTitle != null) {
            loadProducts(categoryName, toolbarTitle)
        }
    }

    private fun loadProducts(categoryName: String, toolbarTitle: String) {
        firestoreDatabaseManager.fetchCategoryProducts(
            categoryName
        ) { products ->
            openFragment(toolbarTitle, products)
        }
    }

    private fun openFragment(
        toolbarTitle: String,
        retrievedCategoryProducts: MutableList<Product>
    ) {
        val transaction: FragmentTransaction =
            parentFragmentManager.beginTransaction()
        transaction.replace(
            R.id.fragment_container,
            ProductFragment.newInstance(
                toolbarTitle,
                retrievedCategoryProducts
            ),
            "FoodCategoriesFragment"
        )
        transaction.addToBackStack("FoodCategoriesFragment")
        transaction.commit()
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            FoodCategoryFragment()
    }

}