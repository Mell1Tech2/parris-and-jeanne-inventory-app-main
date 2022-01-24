package com.example.inventory

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.inventory.data.Item
import com.example.inventory.data.SessionAddItem
import com.example.inventory.databinding.FragmentAddItemBinding

/**
 * Fragment to add or update an item in the Inventory database.
 */
class AddItemFragment : Fragment() {

    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database
                .itemDao()
        )
    }
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()
    private val sessionAddItem: SessionAddItem by activityViewModels()
    lateinit var item: Item
    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Returns true if the EditTexts are not empty
     */
    private fun isEntryValid(): Boolean {
        return viewModel.isEntryValid(
            binding.itemNameAdd.text.toString(),
            binding.itemPrice.text.toString(),
            binding.itemCount.text.toString(),
        )
    }

    /**
     * Binds views with the passed in [item] information.
     */
    private fun bind(item: Item) {
        val price = "%.2f".format(item.itemPrice)
        binding.apply {
            itemBarcodeAdd.setText(item.itemBarcode, TextView.BufferType.SPANNABLE)
            itemNameAdd.setText(item.itemName, TextView.BufferType.SPANNABLE)
            itemPrice.setText(price, TextView.BufferType.SPANNABLE)
            itemCount.setText(item.quantityInStock.toString(), TextView.BufferType.SPANNABLE)
            itemNoteAdd.setText(item.itemNote, TextView.BufferType.SPANNABLE)
            saveAction.setOnClickListener { updateItem() }
        }
    }

    /**
     * Inserts the new Item into database and navigates up to list fragment.
     */
    private fun addNewItem() {
        if (isEntryValid()) {
            viewModel.addNewItem(
                binding.itemBarcodeAdd.text.toString(),
                binding.itemNameAdd.text.toString(),
                binding.itemPrice.text.toString(),
                binding.itemCount.text.toString(),
                binding.itemNoteAdd.text.toString(),
            )
            sessionAddItem.setBarcode("")
            sessionAddItem.setName("")
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    private fun scanBarcode() {
        sessionAddItem.setState("barcode")
        val action = AddItemFragmentDirections.actionAddItemFragmentToCameraScannerFragment()
        findNavController().navigate(action)
    }

    private fun scanName() {
        sessionAddItem.setState("readText")
        val action = AddItemFragmentDirections.actionAddItemFragmentToCameraScannerFragment()
        findNavController().navigate(action)
    }


    private fun updateItem() {
        if (isEntryValid()) {
            viewModel.updateItem(
                this.navigationArgs.itemId,
                this.binding.itemBarcodeAdd.text.toString(),
                this.binding.itemNameAdd.text.toString(),
                this.binding.itemPrice.text.toString(),
                this.binding.itemCount.text.toString(),
                this.binding.itemNoteAdd.text.toString()
            )
            sessionAddItem.setBarcode("")
            sessionAddItem.setName("")
            val action = AddItemFragmentDirections.actionAddItemFragmentToItemListFragment()
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = navigationArgs.itemId
        binding.apply {
            if (itemCount.text.toString() == "") {
                itemCount.setText("1", TextView.BufferType.SPANNABLE)
            }
        }

        sessionAddItem.getBarcode().observe(viewLifecycleOwner, { barcode ->
            binding.itemBarcodeAdd.setText(barcode.toString())
        })
        sessionAddItem.getName().observe(viewLifecycleOwner, { name ->
            binding.itemNameAdd.setText(name.toString())
        })

        if (id > 0) {
            viewModel.retrieveItem(id).observe(this.viewLifecycleOwner) { selectedItem ->
                item = selectedItem
                bind(item)
            }
        } else {
            binding.scanBarcode.setOnClickListener {
                scanBarcode()
            }
            binding.scanName.setOnClickListener {
                scanName()
            }
            binding.saveAction.setOnClickListener {
                addNewItem()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hide keyboard.
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as
                InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
        _binding = null
    }
}