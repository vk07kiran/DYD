package com.projectii.dyd.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.projectii.dyd.*
import com.projectii.dyd.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val userID = activity?.intent?.getStringExtra("userId")


        val goToAllCars: RelativeLayout = binding.ChooseCars
        goToAllCars.setOnClickListener {
            val intent = Intent(requireContext(), AllCars::class.java)
            intent.putExtra("userId", userID)
            startActivity(intent)
        }

        val goToAllbikes: RelativeLayout = binding.ChooseBikes
        goToAllbikes.setOnClickListener {
            val intent = Intent(requireContext(), AllBikes::class.java)
            intent.putExtra("userId", userID)
            startActivity(intent)
        }

        val goToAllBycyles: RelativeLayout = binding.ChooseBycycle
        goToAllBycyles.setOnClickListener {
            val intent = Intent(requireContext(), AllBycycles::class.java)
            intent.putExtra("userId", userID)
            startActivity(intent)
        }

        val goToMyBookins: ImageView = binding.mybookings
        goToMyBookins.setOnClickListener {
            val intent = Intent(requireContext(), MyBookedVehicles::class.java)
            intent.putExtra("userId", userID)
            startActivity(intent)
        }

//        val goToGarages: ImageView = binding.garages
//        goToGarages.setOnClickListener {
//            val intent = Intent(requireContext(), MyBookedVehicles::class.java)
//            intent.putExtra("userId", userID)
//            startActivity(intent)
//        }

//        val goToLoginPage: ImageButton = binding.logout
//        goToLoginPage.setOnClickListener {
//            val intent = Intent(requireContext(), LoginPage::class.java)
//            startActivity(intent)
//        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
