package com.codenzi.payday

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codenzi.payday.databinding.FragmentIntroPageBinding

class IntroPageFragment : Fragment() {

    private var _binding: FragmentIntroPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntroPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val position = requireArguments().getInt(ARG_POSITION)

        val titles = resources.getStringArray(R.array.intro_titles)
        val descriptions = resources.getStringArray(R.array.intro_descriptions)
        val images = resources.obtainTypedArray(R.array.intro_images)

        binding.introTitle.text = titles[position]
        binding.introDescription.text = descriptions[position]
        binding.introImage.setImageResource(images.getResourceId(position, 0))

        images.recycle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_POSITION = "position"

        fun newInstance(position: Int): IntroPageFragment {
            val fragment = IntroPageFragment()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }
}