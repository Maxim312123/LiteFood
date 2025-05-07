package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.FirebaseService
import com.diplomaproject.litefood.FoodSections
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.adapters.FoodSectionAdapter
import com.diplomaproject.litefood.adapters.HitSalesProductsAdapter
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.databinding.FragmentMainBinding
import com.diplomaproject.litefood.fragments.view_models.MainFragmentViewModel
import com.diplomaproject.litefood.managers.FirestoreDatabaseRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainFragment : Fragment(), MenuProvider {
    private lateinit var binding: FragmentMainBinding

    private var toolbar: MaterialToolbar? = null

    private lateinit var rvFoodSections: RecyclerView
    private lateinit var rvHitSales: RecyclerView
    private lateinit var foodSectionAdapter: FoodSectionAdapter
    private lateinit var hitSalesProductAdapter: HitSalesProductsAdapter

    private val firestoreDatabaseRepository: FirestoreDatabaseRepository by lazy {
        FirebaseService.firestoreDatabaseRepository
    }

    private var completingCoroutines: MutableList<Job> = mutableListOf()

    private val viewModel: MainFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fetchFoodSectionsJob = lifecycleScope.launch {
            viewModel.fetchFoodSections()
        }

        val fetchHitSalesProductsJob = lifecycleScope.launch {
            viewModel.fetchHitSalesProducts()
        }

        completingCoroutines.add(fetchFoodSectionsJob)
        completingCoroutines.add(fetchHitSalesProductsJob)

        fetchFoodSectionsJob.invokeOnCompletion {
            removeCompletingCoroutine(fetchFoodSectionsJob)
        }
        fetchHitSalesProductsJob.invokeOnCompletion {
            removeCompletingCoroutine(fetchHitSalesProductsJob)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        Log.d("MainFragment", "onCreateView()")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        initViews()
        setupViewObserves()
    }

    private fun setupViewObserves() {
        viewModel.foodSections.observe(viewLifecycleOwner) { foodSections ->
            foodSectionAdapter =
                FoodSectionAdapter(foodSections.asReversed(), viewModel)
            rvFoodSections.adapter = foodSectionAdapter
        }

        viewModel.selectedFoodSectionPosition.observe(viewLifecycleOwner) { position ->
            if (position != -1) {
                val foodSectionName = FoodSections.getFoodSectionNameByNumber(position)
                val toolbarTitle =
                    resources.getStringArray(R.array.toolbar_title_food_section_name).get(position)

                loadFoodSectionsProducts(foodSectionName, toolbarTitle)
                viewModel.onFoodSectionClicked(-1)
            }
        }

        viewModel.hitSalesProducts.observe(viewLifecycleOwner) { hitSalesProducts ->
            hitSalesProductAdapter =
                HitSalesProductsAdapter(requireActivity(), viewModel, hitSalesProducts)
            rvHitSales.adapter = hitSalesProductAdapter
        }

        viewModel.selectedHitSalesProductPosition.observe(viewLifecycleOwner) { position ->
            if (position != -1) {
//                val productRef = hitSalesProductAdapter.getProduct(position).productRef
//                val job = viewLifecycleOwner.lifecycleScope.launch {
//                    viewModel.fetchHitSalesProductData(productRef)
//                }
//
//                completingCoroutines.add(job)
//                job.invokeOnCompletion {
//                    removeCompletingCoroutine(job)
//                }

                fetchProductData(position)
                viewModel.onHitSalesProductClicked(-1)
            }
        }

//        viewModel.hitSalesProduct.observe(viewLifecycleOwner) { product ->
//            if (product != null) {
//                parentFragmentManager.beginTransaction()
//                    .replace(
//                        R.id.fragment_container,
//                        ProductDescriptionFragment.newInstance(product)
//                    )
//                    .addToBackStack(null)
//                    .commit()
//                viewModel.onFetchedHitSalesProduct()
//            }
//        }

    }

    private fun fetchProductData(position: Int) {

        val productRef = hitSalesProductAdapter.getProduct(position).productRef


        val job = viewLifecycleOwner.lifecycleScope.launch {
            val product = firestoreDatabaseRepository.fetchHitSalesProductData(productRef)

            if (product != null) {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        ProductDescriptionFragment.newInstance(product)
                    )
                    .addToBackStack(null)
                    .commit()
                viewModel.onFetchedHitSalesProduct()
            }
        }

        completingCoroutines.add(job)
        job.invokeOnCompletion {
            removeCompletingCoroutine(job)
        }

    }

    private fun setupToolbar() {
        toolbar = binding!!.materialToolbar
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
    }

    @SuppressLint("RestrictedApi", "WrongConstant")
    private fun initViews() {
        rvFoodSections = binding.rvFoodSections!!

        rvHitSales = binding.rvHitSales!!
        rvHitSales.setLayoutManager(CarouselLayoutManager())
        val carouselSnapHelper = CarouselSnapHelper()
        carouselSnapHelper.attachToRecyclerView(rvHitSales)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        requireActivity().menuInflater.inflate(R.menu.toolbar_main_fragment, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        val id = menuItem.itemId

        if (id == android.R.id.home) {
            val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawerLayout)
            drawerLayout.openDrawer(GravityCompat.START)
            return true
        } else if (id == R.id.search) {
            return true
        }
        return false
    }

    private fun loadFoodSectionsProducts(
        foodSectionName: String,
        toolbarTitle: String
    ) {
        var job = viewLifecycleOwner.lifecycleScope.launch {
            val products = firestoreDatabaseRepository.fetchFoodSectionProducts(foodSectionName)

            val shuffledProducts = products.shuffled() as MutableList<Product>
            parentFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                ProductFragment.newInstance(toolbarTitle, shuffledProducts)
            ).addToBackStack(null).commit()
        }
        completingCoroutines.add(job)
        job.invokeOnCompletion {
            removeCompletingCoroutine(job)
        }
    }

    override fun onStop() {
        super.onStop()
        completingCoroutines.forEach {
            it.cancel()
        }
        completingCoroutines.clear()
    }

    private fun removeCompletingCoroutine(job: Job) {
        completingCoroutines.remove(job)
    }
}