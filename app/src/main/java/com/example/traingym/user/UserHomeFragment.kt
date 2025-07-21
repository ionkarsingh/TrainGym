package com.example.traingym.user

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.traingym.R
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class UserHomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userNameTextView: TextView
    private lateinit var gymUserTextView: TextView
    private lateinit var currentDateTextView: TextView
    private lateinit var totalCategoriesTextView: TextView
    private lateinit var totalExercisesTextView: TextView
    private lateinit var lastBmiTextView: TextView
    private lateinit var quotesViewPager: ViewPager2
    private lateinit var quoteAdapter: QuoteAdapter
    private lateinit var batchTimingTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private var quoteAutoScrollRunnable: Runnable? = null
    private val quotes = listOf(
        "Push yourself, because no one else is going to do it for you.",
        "The body achieves what the mind believes.",
        "Success starts with self-discipline.",
        "Don’t limit your challenges. Challenge your limits."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_home, container, false)
        userNameTextView = view.findViewById(R.id.text_view_user_name)
        gymUserTextView = view.findViewById(R.id.text_view_gym_user)
        currentDateTextView = view.findViewById(R.id.text_view_current_date)
        batchTimingTextView = view.findViewById(R.id.text_view_batch_timing)
        lastBmiTextView = view.findViewById(R.id.text_view_last_bmi)
        quotesViewPager = view.findViewById(R.id.view_pager_quotes)
        quoteAdapter = QuoteAdapter(quotes)
        quotesViewPager.adapter = quoteAdapter
        setupQuoteAutoScroll()
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        currentDateTextView.text = dateFormat.format(Date())
        fetchUserNameAndBatchTiming()
        fetchLastBmi()

        return view
    }

    private fun fetchUserName() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("appusers").document(userId).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: "User"
                userNameTextView.text = name
            }
            .addOnFailureListener {
                userNameTextView.text = "User"
            }
    }

    private fun fetchBatchTiming() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("appusers").document(userId).get()
            .addOnSuccessListener { doc ->
                val batchTiming = doc.getString("batch_timing") ?: "--"
                batchTimingTextView.text = batchTiming
            }
            .addOnFailureListener {
                batchTimingTextView.text = "--"
            }
    }

    private fun fetchUserNameAndBatchTiming() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("AppUsers").document(userId).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("username") ?: "User"
                userNameTextView.text = name
                val start = doc.getString("batch_start_time") ?: "--"
                val end = doc.getString("batch_end_time") ?: "--"
                batchTimingTextView.text = if (start != "--" && end != "--") "$start - $end" else "--"
            }
            .addOnFailureListener {
                userNameTextView.text = "User"
                batchTimingTextView.text = "--"
            }
    }

    private fun fetchLastBmi() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("Bmi").document(userId).get()
            .addOnSuccessListener { doc ->
                val bmi = doc.getDouble("bmi")
                lastBmiTextView.text = bmi?.let { String.format("%.2f", it) } ?: "--"
            }
            .addOnFailureListener {
                lastBmiTextView.text = "--"
            }
    }

    private fun setupQuoteAutoScroll() {
        quoteAutoScrollRunnable = object : Runnable {
            override fun run() {
                val next = (quotesViewPager.currentItem + 1) % quotes.size
                quotesViewPager.currentItem = next
                handler.postDelayed(this, 5000)
            }
        }
        handler.postDelayed(quoteAutoScrollRunnable!!, 5000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        quoteAutoScrollRunnable?.let { handler.removeCallbacks(it) }
    }
} 