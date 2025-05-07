package com.diplomaproject.litefood.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.UserViewModel
import com.diplomaproject.litefood.adapters.EditPaymentMethodsAdapter
import com.diplomaproject.litefood.data.User
import com.diplomaproject.litefood.databinding.FragmentEditPaymentMethodsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class EditPaymentMethodFragment : Fragment(), EditPaymentMethodsAdapter.OnClickItemListener,
    MenuProvider, EditPaymentMethodsAdapter.OnContextMenuItemClickListener {

    private lateinit var binding: FragmentEditPaymentMethodsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var editPaymentMethodsAdapter: EditPaymentMethodsAdapter
    private val editPaymentMethodList: MutableList<HashMap<String, Any>?> = mutableListOf()
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var currentUserUid: String
    private lateinit var databaseRef: DatabaseReference
    private var selectedPaymentMethod = -1
    private var deletedPaymentMethod = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditPaymentMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(this, viewLifecycleOwner)
        init()
        loadPaymentMethods()
    }

    private fun init() {
        recyclerView = binding.rvPaymentMethods
        editPaymentMethodsAdapter = EditPaymentMethodsAdapter(editPaymentMethodList, this, this)
        recyclerView.adapter = editPaymentMethodsAdapter
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        databaseRef = FirebaseDatabase.getInstance().getReference("Users/" + currentUserUid)
        registerForContextMenu(recyclerView)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadPaymentMethods() {
        userViewModel.user.observe(activity as LifecycleOwner) { user: User ->
            user.paymentMethod?.let {
                editPaymentMethodList.addAll(user.paymentMethod!!.values)
                editPaymentMethodsAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.toolbar_edit_payment_methods_fragment, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.save) {
            Toast.makeText(requireActivity(), "Изменено", Toast.LENGTH_SHORT).show()
            saveChanges()
        }
        return true
    }

    private fun saveChanges() {
        userViewModel.user.observe(requireActivity()) { user: User ->
            val paymentMethods = user.paymentMethod
            if (paymentMethods != null) {
                if (selectedPaymentMethod != -1) {
                    val selectedItem = editPaymentMethodList[selectedPaymentMethod]
                    var keySelectedItem: String? = null
                    for ((key, value) in paymentMethods) {
                        if (value == selectedItem) {
                            keySelectedItem = key
                            break
                        }
                    }

                    if (keySelectedItem != null) {
                        val tempValue = paymentMethods["main"]

                        if (tempValue != null) {
                            paymentMethods["main"] = paymentMethods[keySelectedItem]!!
                            paymentMethods[keySelectedItem] = tempValue
                            databaseRef.child("/paymentMethod").setValue(paymentMethods)
                        }
                    }
                }
            }
        }
        parentFragmentManager.popBackStack()
    }

    override fun onCLickItem(position: Int) {
        selectedPaymentMethod = position
        if (position != editPaymentMethodsAdapter.currentCheckedItem) {
            editPaymentMethodsAdapter.updateMainPaymentMethod(position)
            editPaymentMethodsAdapter.isClickedItem = true
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        requireActivity().menuInflater.inflate(R.menu.edit_payment_method_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete -> {
                if (deletedPaymentMethod != -1) {
                    deletePaymentMethod()
                    Toast.makeText(requireActivity(), "Удалено", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return true
    }

    override fun onDeleteItem(position: Int) {
        deletedPaymentMethod = position
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deletePaymentMethod() {
        var deletedItem = editPaymentMethodList.get(deletedPaymentMethod)
        var keyDeletedItem: String? = null
        userViewModel.user.observe(requireActivity()) { user: User ->
            val userPaymentMethods = user.paymentMethod
            for ((key, value) in userPaymentMethods!!.entries) {
                if (value == deletedItem) {
                    keyDeletedItem = key
                }
            }
        }
        if (keyDeletedItem != null) {
            databaseRef.child("paymentMethod/").child(keyDeletedItem!!).removeValue()
            editPaymentMethodList.removeAt(deletedPaymentMethod)
            editPaymentMethodsAdapter.setList(editPaymentMethodList)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = EditPaymentMethodFragment()
    }
}