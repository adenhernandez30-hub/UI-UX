package ani.streamix.media

import androidx.core.content.ContextCompat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import ani.streamix.R
import ani.streamix.Refresh
import ani.streamix.databinding.ActivityListBinding
import ani.streamix.getThemeColor
import ani.streamix.media.user.ListViewPagerAdapter
import ani.streamix.settings.saving.PrefManager
import ani.streamix.settings.saving.PrefName
import ani.streamix.statusBarHeight
import ani.streamix.themes.ThemeManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarFragment : Fragment() {

    private var _binding: ActivityListBinding? = null
    private val binding get() = _binding!!

    private val model: OtherDetailsViewModel by activityViewModels()

    private var selectedTabIdx = 1
    private var showOnlyLibrary = false
    private var showOnlyDubbed = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ThemeManager(requireActivity()).applyTheme()
        _binding = ActivityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val primaryColor = requireContext()
            .getThemeColor(com.google.android.material.R.attr.colorSurface)
        val primaryTextColor = requireContext()
            .getThemeColor(androidx.appcompat.R.attr.colorPrimary)
        val secondaryTextColor = requireContext()
            .getThemeColor(com.google.android.material.R.attr.colorOutline)

        requireActivity().window.statusBarColor = primaryColor
        requireActivity().window.navigationBarColor = primaryColor

        binding.listTabLayout.setBackgroundColor(primaryColor)
        binding.listAppBar.setBackgroundColor(primaryColor)
        binding.listTitle.setTextColor(primaryTextColor)
        binding.listTabLayout.setTabTextColors(
            secondaryTextColor,
            primaryTextColor
        )
        binding.listTabLayout.setSelectedTabIndicatorColor(primaryTextColor)

        if (!PrefManager.getVal<Boolean>(PrefName.ImmersiveMode)) {
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireContext(), R.color.nav_bg_inv)
            binding.root.fitsSystemWindows = true
        } else {
            binding.root.fitsSystemWindows = false
            requireActivity().hideSystemBarsExtendView()
            binding.settingsContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = statusBarHeight
            }
        }

        binding.listTitle.setText(R.string.release_calendar)
        binding.listSort.visibility = View.GONE
        binding.random.visibility = View.GONE
        binding.search.visibility = View.GONE
        binding.filter.visibility = View.GONE
        binding.listDubbed.visibility = View.VISIBLE
        binding.listDubbed.alpha = 0.6f

        binding.listTabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    selectedTabIdx = tab?.position ?: 1
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            }
        )

        binding.listed.setOnClickListener {
            showOnlyLibrary = !showOnlyLibrary
            binding.listed.setImageResource(
                if (showOnlyLibrary) {
                    R.drawable.ic_round_collections_bookmark_24
                } else {
                    R.drawable.ic_round_library_books_24
                }
            )
            viewLifecycleOwner.lifecycleScope.launch {
                model.loadCalendar(showOnlyLibrary, showOnlyDubbed)
            }
        }

        binding.listDubbed.setOnClickListener {
            showOnlyDubbed = !showOnlyDubbed
            binding.listDubbed.alpha = if (showOnlyDubbed) 1f else 0.6f
            viewLifecycleOwner.lifecycleScope.launch {
                model.loadCalendar(showOnlyLibrary, showOnlyDubbed)
            }
        }

        model.getCalendar().observe(viewLifecycleOwner) {
            if (it != null) {
                binding.listProgressBar.visibility = View.GONE
                binding.listViewPager.adapter =
                    ListViewPagerAdapter(it.size, true, requireActivity())

                val keys = it.keys.toList()
                val values = it.values.toList()

                val savedTab = if (it.isNotEmpty()) {
                    selectedTabIdx.coerceIn(0, it.size - 1)
                } else {
                    0
                }

                TabLayoutMediator(
                    binding.listTabLayout,
                    binding.listViewPager
                ) { tab, position ->
                    tab.text = "${keys[position]} (${values[position].size})"
                }.attach()

                if (it.isNotEmpty()) {
                    binding.listViewPager.setCurrentItem(savedTab, false)
                }
            }
        }

        val live = Refresh.activity.getOrPut(
            this.hashCode()
        ) {
            MutableLiveData(true)
        }

        live.observe(viewLifecycleOwner) {
            if (it) {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        model.loadCalendar(
                            showOnlyLibrary,
                            showOnlyDubbed
                        )
                    }
                    live.postValue(false)
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.listViewPager.adapter = null
        Refresh.activity.remove(this.hashCode())
        _binding = null
        super.onDestroyView()
    }
}
