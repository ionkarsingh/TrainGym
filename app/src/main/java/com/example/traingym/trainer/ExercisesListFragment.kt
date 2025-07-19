package com.example.traingym.trainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.traingym.R
import com.google.android.material.appbar.MaterialToolbar

class ExercisesListFragment : Fragment() {

    private var categoryId: String? = null
    private var categoryName: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getString(ARG_CATEGORY_ID)
            categoryName = it.getString(ARG_CATEGORY_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_exercises_list, container, false)
        val displayTextView = view.findViewById<TextView>(R.id.text_view_category_id_display)

        displayTextView.text = "Received Category ID:\n$categoryId"

        return view
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = categoryName ?: "Exercises"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStop() {
        super.onStop()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = "Exercise List"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }


    companion object {
        private const val ARG_CATEGORY_ID = "category_id"
        private const val ARG_CATEGORY_NAME = "category_name"

        fun newInstance(categoryId: String, categoryName: String): ExercisesListFragment {
            val fragment = ExercisesListFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY_ID, categoryId)
            args.putString(ARG_CATEGORY_NAME, categoryName)

            fragment.arguments = args
            return fragment
        }
    }
}