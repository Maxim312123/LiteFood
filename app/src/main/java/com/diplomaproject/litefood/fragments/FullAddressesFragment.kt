package com.diplomaproject.litefood.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.adapters.AddressAdapter
import com.diplomaproject.litefood.data.Address
import com.diplomaproject.litefood.data.User
import com.diplomaproject.litefood.databinding.FragmentFullAddressesBinding
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository


private const val ARG_USER = "User"

class FullAddressesFragment : Fragment(), AddressAdapter.OnItemClickListener {

    private lateinit var binding: FragmentFullAddressesBinding

    private lateinit var recyclerView: RecyclerView
    private lateinit var user: User
    private lateinit var addressAdapter: AddressAdapter
    private val firebaseRealtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable(ARG_USER)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFullAddressesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView


        val userBasket = convertUserAddressesToList()

        addressAdapter = AddressAdapter(userBasket, this)

        recyclerView.adapter = addressAdapter
    }

    private fun convertUserAddressesToList(): MutableList<Address> {
        var addresses: MutableList<Address> = mutableListOf()

        val mainUserAddress = getAddressByType("main")

        addresses.add(mainUserAddress)
        return addresses
    }

    private fun getAddressByType(type: String): Address {
        val mainUserAddress = user?.addresses?.get(type)

        val address = Address(
            mainUserAddress?.get("city").toString(),
            mainUserAddress?.get("street").toString(),
            mainUserAddress?.get("houseNumber").toString()
        )
        return address
    }

    companion object {
        @JvmStatic
        fun newInstance(user: User) = FullAddressesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_USER, user)
            }
        }
    }

    override fun onItemClick(position: Int) {
        firebaseRealtimeDatabaseManager.deleteAddress(addressAdapter.getAddress(position))
        addressAdapter.deleteAddress(position)
    }
}