package com.example.inventory

 import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.data.Item
import com.example.inventory.data.SessionAddItem
import com.example.inventory.databinding.ItemListFragmentBinding
import com.example.inventory.src.EmailRoomInstance
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * Main fragment displaying details for all items in the database.
 */
class ItemListFragment : Fragment() {
    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }
    private val sessionAddItem: SessionAddItem by activityViewModels()
    private val emailRoomInstance = EmailRoomInstance()

    private lateinit var auth: FirebaseAuth

    private var _binding: ItemListFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ItemListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question2))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.deleteAllItems()
            }
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ItemListAdapter {
            val action =
                ItemListFragmentDirections.actionItemListFragmentToItemDetailFragment(it.id)
            this.findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter

        /* Attach an observer on the allItems list to update the UI automatically when the data
        changes. */
        viewModel.allItems.observe(this.viewLifecycleOwner) { items ->
            items.let {
                adapter.submitList(it)
            }
        }

        // Initialize Firebase Auth
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser != null){
            //attempt a login
        }
        val database = Firebase.database.reference
        //val myRef = database.getReference("message")


        Log.d("Log", "value: " + viewModel.allItems.value.toString())
        //val key = database.child("posts").push().key
        val post = viewModel.allItems.value


        val map = post?.map {  it.id to mutableMapOf(
            "barcode" to it.itemBarcode,
            "name" to it.itemName,
            "price" to it.itemPrice,
            "quantity" to it.quantityInStock,
            "note" to it.itemNote) }
        //val map2 = map?.associate{ it to it.second }
        //val map2 = map?.toMutableSet()
        //val map = post?.groupBy({"Item Name" to it.itemName}, {"Item Barcode" to it.itemBarcode})

        Log.d("Log", "value: $map")
        //Log.d("Log", "value: $map2")

        map?.forEach { database.child(it.first.toString()).updateChildren(it.second as Map<String, Any>) }
        //database.updateChildren()

        binding.floatingActionButton.setOnClickListener {
            sessionAddItem.setBarcode("")
            sessionAddItem.setName("")
            val action = ItemListFragmentDirections.actionItemListFragmentToAddItemFragment(
                getString(R.string.add_fragment_title)
            )
            this.findNavController().navigate(action)
        }
        binding.floatingActionButton2.setOnClickListener {
            emailRoomInstance.backupDatabase(this.requireActivity(), viewModel)
        }
        binding.floatingActionButton3.setOnClickListener {
            showConfirmationDialog()
        }
    }
}