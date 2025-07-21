package com.example.traingym.trainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.traingym.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MembersFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var membersAdapter: MembersAdapter
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var noMembersTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_members, container, false)

        membersRecyclerView = view.findViewById(R.id.members_recycler_view)
        lottieAnimationView = view.findViewById(R.id.lottie_loading_animation)
        noMembersTextView = view.findViewById(R.id.text_view_no_members)
        val fabAddMember = view.findViewById<FloatingActionButton>(R.id.fab_add_member)

        setupRecyclerView()
        fabAddMember.setOnClickListener { showAddMemberDialog() }

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchMembers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.members_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_view_suspended -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, SuspendedMembersFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        membersAdapter = MembersAdapter(
            onEditClick = { member ->
                showEditMemberDialog(member)
            },
            onSuspendClick = { member ->
                suspendMember(member)
            }
        )
        membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = membersAdapter
        }
    }

    private fun showLoading(isLoading: Boolean) {
        lottieAnimationView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun fetchMembers() {
        showLoading(true)
        membersRecyclerView.visibility = View.GONE
        noMembersTextView.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("AppUsers")
                    .whereEqualTo("user_type", "normal_user")
                    .whereEqualTo("_suspended", false)
                    .get()
                    .await()

                val fetchedMembers = snapshot.toObjects(Member::class.java)

                withContext(Dispatchers.Main) {
                    if (fetchedMembers.isEmpty()) {
                        noMembersTextView.visibility = View.VISIBLE
                        membersRecyclerView.visibility = View.GONE
                    } else {
                        noMembersTextView.visibility = View.GONE
                        membersRecyclerView.visibility = View.VISIBLE
                        membersAdapter.updateMembers(fetchedMembers)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error fetching members: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                }
            }
        }
    }

    private fun suspendMember(member: Member) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Suspend Member")
            .setMessage("Are you sure you want to suspend '${member.username}'? They will not be able to use our app.")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Suspend") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        firestore.collection("AppUsers").document(member.uid)
                            .update("_suspended", true).await()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Member suspended.", Toast.LENGTH_SHORT).show()
                            fetchMembers()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .show()
    }

    private fun showAddMemberDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_member, null)
        val usernameEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_username)
        val emailEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_email)
        val passwordEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_password)
        val phoneEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_phone)
        val addressEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_address)
        val startTimeEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_start_time)
        val endTimeEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_end_time)
        val saveButton = dialogView.findViewById<Button>(R.id.button_save_member)
        val closeButton = dialogView.findViewById<ImageView>(R.id.image_view_close_dialog)

        startTimeEditText.setOnClickListener {
            showTimePicker { hour, minute ->
                startTimeEditText.setText(formatTimeForDisplay(hour, minute))
            }
        }
        endTimeEditText.setOnClickListener {
            showTimePicker(false) { hour, minute ->
                endTimeEditText.setText(formatTimeForDisplay(hour, minute))
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        saveButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()
            val startTime = startTimeEditText.text.toString()
            val endTime = endTimeEditText.text.toString()

            when {
                username.isEmpty() -> {
                    usernameEditText.error = "Enter username"
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    emailEditText.error = "Enter valid email"
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    passwordEditText.error = "Minimum 6 characters"
                    return@setOnClickListener
                }
                phone.isEmpty() -> {
                    phoneEditText.error = "Enter phone number"
                    return@setOnClickListener
                }
                address.isEmpty() -> {
                    addressEditText.error = "Enter address"
                    return@setOnClickListener
                }
                !phone.matches(Regex("^\\d{10}$")) -> {
                    phoneEditText.error = "Phone number must be exactly 10 digits"
                    return@setOnClickListener
                }
                startTime.isEmpty() -> {
                    startTimeEditText.error = "Select start time"
                    return@setOnClickListener
                }
                endTime.isEmpty() -> {
                    endTimeEditText.error = "Select end time"
                    return@setOnClickListener
                }
            }

            saveButton.isEnabled = false

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                    val uid = authResult.user?.uid ?: throw Exception("Authentication failed")

                    val newUser = Member(
                        uid = uid,
                        username = username,
                        email = email,
                        batch_start_time = startTime,
                        batch_end_time = endTime,
                        is_suspended = false,
                        phone = phone,
                        address = address
                    )

                    firestore.collection("AppUsers").document(uid).set(newUser).await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "User '$username' added!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        fetchMembers()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error adding member: ${e.message}", Toast.LENGTH_LONG).show()
                        saveButton.isEnabled = true
                    }
                }
            }
        }

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showEditMemberDialog(member: Member) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_member, null)

        val usernameEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_edit_username)
        val phoneEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_edit_phone)
        val addressEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_edit_address)
        val startTimeEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_edit_start_time)
        val endTimeEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_edit_end_time)
        val saveButton = dialogView.findViewById<Button>(R.id.button_save_edit_member)
        val closeButton = dialogView.findViewById<ImageView>(R.id.image_view_close_edit_dialog)

        usernameEditText.setText(member.username)
        phoneEditText.setText(member.phone)
        addressEditText.setText(member.address)
        startTimeEditText.setText(member.batch_start_time)
        endTimeEditText.setText(member.batch_end_time)

        startTimeEditText.setOnClickListener {
            showTimePicker { hour, minute ->
                startTimeEditText.setText(formatTimeForDisplay(hour, minute))
            }
        }
        endTimeEditText.setOnClickListener {
            showTimePicker(false) { hour, minute ->
                endTimeEditText.setText(formatTimeForDisplay(hour, minute))
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        saveButton.setOnClickListener {
            val updatedUsername = usernameEditText.text.toString().trim()
            val updatedPhone = phoneEditText.text.toString().trim()
            val updatedAddress = addressEditText.text.toString().trim()
            val updatedStart = startTimeEditText.text.toString()
            val updatedEnd = endTimeEditText.text.toString()

            if (updatedUsername.isEmpty()) {
                usernameEditText.error = "Enter username"
                return@setOnClickListener
            }
            if (updatedPhone.isEmpty()) {
                phoneEditText.error = "Enter phone number"
                return@setOnClickListener
            }
            if (updatedAddress.isEmpty()) {
                addressEditText.error = "Enter address"
                return@setOnClickListener
            }
            if (!updatedPhone.matches(Regex("^\\d{10}$"))) {
                phoneEditText.error = "Phone number must be exactly 10 digits"
                return@setOnClickListener
            }
            if (updatedStart.isEmpty()) {
                startTimeEditText.error = "Select start time"
                return@setOnClickListener
            }
            if (updatedEnd.isEmpty()) {
                endTimeEditText.error = "Select end time"
                return@setOnClickListener
            }

            val updatedMember = member.copy(
                username = updatedUsername,
                phone = updatedPhone,
                address = updatedAddress,
                batch_start_time = updatedStart,
                batch_end_time = updatedEnd
            )

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    firestore.collection("AppUsers")
                        .document(member.uid)
                        .set(updatedMember)
                        .await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Member updated!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        fetchMembers()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error updating: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        closeButton.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
    private fun showTimePicker(isStartTime: Boolean = true, onTimeSelected: (Int, Int) -> Unit) {
        val calendar = Calendar.getInstance()
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText(if (isStartTime) "Select Start Time" else "Select End Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            onTimeSelected(picker.hour, picker.minute)
        }

        picker.show(childFragmentManager, "timePicker")
    }

    private fun formatTimeForDisplay(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val format = SimpleDateFormat("h:mm a", Locale.US)
        return format.format(calendar.time)
    }
}