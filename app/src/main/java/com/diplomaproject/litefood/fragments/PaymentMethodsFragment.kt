package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.UserViewModel
import com.diplomaproject.litefood.adapters.PaymentMethodAdapter
import com.diplomaproject.litefood.data.User
import com.diplomaproject.litefood.databinding.FragmentPaymentMethodsBinding
import com.diplomaproject.litefood.dialogs.AddCreditCardBottomSheet


class PaymentMethodsFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentPaymentMethodsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var paymentMethodAdapter: PaymentMethodAdapter
    private val paymentMethodsList: MutableList<HashMap<String, Any>?> = mutableListOf()
    private lateinit var rLayoutAddCreditCard: RelativeLayout
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPaymentMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        init()
        setupListeners()
        loadPaymentMethods()
    }

    private fun init() {
        recyclerView = binding.rvPaymentMethods
        paymentMethodAdapter = PaymentMethodAdapter(paymentMethodsList)
        recyclerView.adapter = paymentMethodAdapter
        rLayoutAddCreditCard = binding.addCreditCard
    }

    private fun PaymentMethodsFragment.setupListeners() {
        rLayoutAddCreditCard.setOnClickListener {
            val addCreditCardBottomSheet =
                AddCreditCardBottomSheet(paymentMethodAdapter, paymentMethodsList)
            addCreditCardBottomSheet.isCancelable = false
            addCreditCardBottomSheet.show(parentFragmentManager, null)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPaymentMethods() {
        userViewModel.user.observe(requireActivity()) { user: User ->
            user.paymentMethod?.let {
                paymentMethodsList.clear()
                paymentMethodsList.addAll(user.paymentMethod!!.values)
                paymentMethodAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        userViewModel.user.observe(this) { user: User ->
            user.paymentMethod?.let {
                menu.removeItem(R.id.edit)
                menuInflater.inflate(R.menu.toolbar_payment_methods_fragment, menu)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.edit) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_fragment_container, EditPaymentMethodFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
        return true
    }

    companion object {
        @JvmStatic
        fun newInstance() = PaymentMethodsFragment()
    }

}