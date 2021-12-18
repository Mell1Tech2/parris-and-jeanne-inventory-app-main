/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inventory.databinding.ItemListFragmentBinding
import java.io.File
import java.io.IOException

/**
 * Main fragment displaying details for all items in the database.
 */
class ItemListFragment : Fragment() {
    private val viewModel: InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ItemListAdapter {
            val action =
                ItemListFragmentDirections.actionItemListFragmentToItemDetailFragment(it.id)
            this.findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this.context)
        binding.recyclerView.adapter = adapter
        // Attach an observer on the allItems list to update the UI automatically when the data
        // changes.
        viewModel.allItems.observe(this.viewLifecycleOwner) { items ->
            items.let {
                adapter.submitList(it)
            }
        }

        binding.floatingActionButton.setOnClickListener {
            val action = ItemListFragmentDirections.actionItemListFragmentToAddItemFragment(
                getString(R.string.add_fragment_title)
            )
            this.findNavController().navigate(action)
        }
        binding.floatingActionButton2.setOnClickListener {
            backupDatabase(this.requireActivity())
        }
    }

    private fun backupDatabase(activity: FragmentActivity) {

        Log.d("Log", "backup started")

        // Get the database file
        val dbFile = activity.getDatabasePath("item_database")
        //dbFile.forEachLine { Log.d("Log", "dbFileParent: $it") }
        Log.d("Log", "dbFileParent: " + dbFile.exists())

        try {
            // Copy database file to a temp file in (filesDir)
            val parent = File(activity.filesDir, "databases_temp")
            Log.d("Log", "dbFileParent: $parent")
            dbFile.copyTo(parent, true)
            Log.d("Log", "Database copied " + parent.exists())

            // Get Uri of the copied database file from filesDir to be used in email intent
            val uri = getUri(activity.applicationContext, parent)
            Log.d("Log", "Uri complete")

            // Send an email
            sendEmail(activity, uri)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun getUri(context: Context, file: File): Uri {
        Log.d("Log", "Uri started")
        var uri = Uri.fromFile(file)

        Log.d("Log", "uri: $uri")
        // Using FileProvider for API >= 24
        if (Build.VERSION.SDK_INT >= 24) {
            Log.d("Log", "SDK greater than 24")
            uri = FileProvider.getUriForFile(
                context,
                "com.inventory.example.provider", file
            )
        }
        return uri
    }

    private fun sendEmail(activity: FragmentActivity, attachment: Uri) {
        Log.d("Log", "sendEmail start")

        val emailIntent = Intent(Intent.ACTION_SEND)
        Log.d("Log", "intent action complete")
        emailIntent.type = "vnd.android.cursor.dir/email"
        Log.d("Log", "email type complete")
        val toEmail = "KieranAbelen@gmail.com"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, toEmail)
        Log.d("Log", "email EXTRA_EMAIL complete")
        emailIntent.putExtra(Intent.EXTRA_STREAM, attachment)
        Log.d("Log", "email EXTRA_STREAM complete")
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Stock take data")
        Log.d("Log", "email: $emailIntent")
        activity.startActivity(Intent.createChooser(emailIntent, "Send Email"))
        Log.d("Log", "email Sent")
    }
}