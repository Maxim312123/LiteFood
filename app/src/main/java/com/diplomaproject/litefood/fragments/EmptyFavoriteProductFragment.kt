package com.diplomaproject.litefood.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.diplomaproject.litefood.R


class EmptyFavoriteProductFragment : Fragment(), MenuProvider {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().addMenuProvider(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_empty_favorite_product, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = EmptyFavoriteProductFragment()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.removeItem(R.id.delete_all)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }
}