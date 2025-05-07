package com.diplomaproject.litefood.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.databinding.EditPaymentMethodBinding

class EditPaymentMethodsAdapter(
    private var paymentMethodList: MutableList<HashMap<String, Any>?>
) : RecyclerView.Adapter<EditPaymentMethodsAdapter.ItemViewHolder>() {
    private lateinit var onClickItemListener: OnClickItemListener
    private lateinit var onContextMenuItemClickListener: OnContextMenuItemClickListener
    var currentCheckedItem: Int? = 0
    var isClickedItem = false

    constructor(
        paymentMethodList: MutableList<HashMap<String, Any>?>,
        onClickItemListener: OnClickItemListener,
        onContextMenuItemClickListener: OnContextMenuItemClickListener
    ) : this(paymentMethodList) {
        this.onContextMenuItemClickListener = onContextMenuItemClickListener
        this.onClickItemListener = onClickItemListener
    }

    interface OnClickItemListener {
        fun onCLickItem(position: Int)
    }

    interface OnContextMenuItemClickListener {
        fun onDeleteItem(position: Int)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = EditPaymentMethodBinding.bind(itemView)
        val tvCardLast4Digits = binding.tvNumberCreditCard
        val ivPaymentSystem = binding.ivPaymentSystem
        val radioButton = binding.radioButton
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ItemViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.edit_payment_method, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val cardInfo = paymentMethodList[position]
        val cardLast4Digits = cardInfo?.get("last4Digits").toString()
        val cardBrand = cardInfo?.get("cardBrand")
        val isMainPaymentMethod = cardInfo?.get("isMainPaymentMethod")

        holder.tvCardLast4Digits.text = "**** $cardLast4Digits"
        if (cardBrand == "Visa") {
            holder.ivPaymentSystem.setImageResource(R.drawable.visa24)
        } else {
            holder.ivPaymentSystem.setImageResource(R.drawable.mastercard24)
        }
        if (isMainPaymentMethod as Boolean) {
            holder.radioButton.isChecked = true
            currentCheckedItem = position
        } else {
            holder.radioButton.isChecked = false
        }
        holder.itemView.setOnClickListener {
            onClickItemListener.onCLickItem(position)
        }
        holder.itemView.setOnLongClickListener {
            it.showContextMenu()
            onContextMenuItemClickListener.onDeleteItem(position)
            true
        }
    }

//    private fun initElements(
//        position: Int,
//        holder: ItemViewHolder
//    ) {
//        if (position == 0) {
//            val mainPaymentMethod =
//                paymentMethodList.find { it?.get("isMainPaymentMethod") == true }
//            if (mainPaymentMethod != null) {
//                val cardLast4Digits = mainPaymentMethod["last4Digits"].toString()
//                val cardBrand = mainPaymentMethod["cardBrand"]
//
//                holder.tvCardLast4Digits.text = "**** $cardLast4Digits"
//                if (cardBrand == "Visa") {
//                    holder.ivPaymentSystem.setImageResource(R.drawable.visa24)
//                } else {
//                    holder.ivPaymentSystem.setImageResource(R.drawable.mastercard24)
//                }
//                holder.radioButton.isChecked = true
//                holder.itemView.setOnClickListener {
//                    onClickItemListener.onCLickItem(position)
//                }
//            }
//        } else {
//            val cardInfo = paymentMethodList.get(position - 1)
//            val cardLast4Digits = cardInfo?.get("last4Digits").toString()
//            val cardBrand = cardInfo?.get("cardBrand")
//            holder.tvCardLast4Digits.text = "**** $cardLast4Digits"
//            if (cardBrand == "Visa") {
//                holder.ivPaymentSystem.setImageResource(R.drawable.visa24)
//            } else {
//                holder.ivPaymentSystem.setImageResource(R.drawable.mastercard24)
//            }
//            holder.itemView.setOnClickListener {
//                onClickItemListener.onCLickItem(position)
//            }
//        }
//    }

    override fun getItemCount()
            : Int {
        return paymentMethodList.size
    }

    fun updateMainPaymentMethod(newPosition: Int) {
        currentCheckedItem?.let { oldPosition ->
            paymentMethodList[oldPosition]?.set("isMainPaymentMethod", false)
            notifyItemChanged(oldPosition)
        }

        paymentMethodList[newPosition]?.set("isMainPaymentMethod", true)
        currentCheckedItem = newPosition
        notifyItemChanged(newPosition)
    }

    fun setList(list: MutableList<HashMap<String, Any>?>) {
        paymentMethodList.clear()
        paymentMethodList = list
        notifyDataSetChanged()
    }

}