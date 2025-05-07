package com.diplomaproject.litefood.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.databinding.FragmentEmptyBasketBinding
import com.diplomaproject.litefood.interfaces.BottomNavigationViewSelectedItem


class EmptyShoppingBasketFragment : Fragment() {

    private lateinit var binding: FragmentEmptyBasketBinding
    private lateinit var btnGoToCategories: Button
    private lateinit var bottomNavigationViewSelectedItem: BottomNavigationViewSelectedItem

    @Deprecated("Deprecated in Java")
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        bottomNavigationViewSelectedItem = activity as BottomNavigationViewSelectedItem
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmptyBasketBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setupViewsListeners()
    }

    private fun setupViewsListeners() {
        btnGoToCategories.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FoodCategoryFragment.newInstance())
                .commit()
            bottomNavigationViewSelectedItem.changeItemState(R.id.categories)
        }
    }

    private fun init() {
        btnGoToCategories = binding.goToCategories
    }

    companion object {
        @JvmStatic
        fun newInstance() = EmptyShoppingBasketFragment()
    }
}