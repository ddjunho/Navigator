package com.example.teampro_test

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [NaviHomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NaviHomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_navi_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnClickListener {
            startMainActivity()
        }
    }
    // Call this method to start MainActivity
    private fun startMainActivity() {
        val intent = Intent()
        intent.setClassName("com.example.festunavigator", "com.example.festunavigator.MainActivity")
        intent.putExtra("key", "value") // 데이터 추가
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            //startActivity(intent)
            showErrorDialog()
        } else {
            // 액티비티가 존재하지 않을 때 처리
            Toast.makeText(requireContext(), "Activity not found", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showErrorDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("오류")
            .setMessage("기술적인 문제로 실내 네비게이션 실행이 불가합니다 ㅠㅠ \n추후 업데이트를 기대해주세요")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NaviHomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NaviHomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}