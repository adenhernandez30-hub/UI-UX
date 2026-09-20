package ani.streamix.media.user

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import ani.streamix.R
import ani.streamix.Refresh
import ani.streamix.databinding.ActivityListBinding
import ani.streamix.getThemeColor
import ani.streamix.hideSystemBarsExtendView
import ani.streamix.settings.saving.PrefManager
import ani.streamix.settings.saving.PrefName
import ani.streamix.statusBarHeight
import ani.streamix.themes.ThemeManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.appcompat.widget.PopupMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryFragment : Fragment() {
    private var _binding: ActivityListBinding? = null
    private val binding get() = _binding!!
    private val model: ListViewModel by activityViewModels()
    private var selectedTabIdx = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ThemeManager(requireActivity()).applyTheme()
        _binding = ActivityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val primaryColor = requireContext().getThemeColor(com.google.android.material.R.attr.colorSurface)
        val primaryTextColor = requireContext().getThemeColor(androidx.appcompat.R.attr.colorPrimary)
        val secondaryTextColor = requireContext().getThemeColor(com.google.android.material.R.attr.colorOutline)

        binding.listed.visibility = View.GONE
        binding.listTabLayout.setBackgroundColor(primaryColor)
        binding.listAppBar.setBackgroundColor(primaryColor)
        binding.listTitle.setTextColor(primaryTextColor)
        binding.listTabLayout.setTabTextColors(secondaryTextColor, primaryTextColor)
        binding.listTabLayout.setSelectedTabIndicatorColor(primaryTextColor)

        if (!PrefManager.getVal<Boolean>(PrefName.ImmersiveMode)) {
            requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bg_inv)
            binding.root.fitsSystemWindows = true
        } else {
            binding.root.fitsSystemWindows = false
            requireActivity().hideSystemBarsExtendView()
            binding.settingsContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = statusBarHeight
            }
        }

        binding.listTitle.setText(R.string.anime_list)
        binding.listTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTabIdx = tab?.position ?: 0
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        model.getLists().observe(viewLifecycleOwner) {
            val defaultKeys = listOf("Reading", "Watching", "Completed", "Paused", "Dropped", "Planning", "Favourites", "Rewatching", "Rereading", "All")
            val userKeys = resources.getStringArray(R.array.keys)
            if (it != null) {
                binding.listProgressBar.visibility = View.GONE
                binding.listViewPager.adapter = ListViewPagerAdapter(it.size, false, requireActivity())
                val keys = it.keys.toList().map { key -> userKeys.getOrNull(defaultKeys.indexOf(key)) ?: key }
                val values = it.values.toList()
                val savedTab = selectedTabIdx.coerceIn(0, (it.size - 1).coerceAtLeast(0))
                TabLayoutMediator(binding.listTabLayout, binding.listViewPager) { tab, position ->
                    tab.text = "${keys[position]} (${values[position].size})"
                }.attach()
                if (it.isNotEmpty()) binding.listViewPager.setCurrentItem(savedTab, false)
            }
        }

        val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(true) }
        live.observe(viewLifecycleOwner) {
            if (it) {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        model.loadLists(true, ani.streamix.connections.anilist.Anilist.userid ?: 0)
                    }
                    live.postValue(false)
                }
            }
        }

        if (PrefManager.getVal<Boolean>(PrefName.RescueMode)) binding.listSort.visibility = View.GONE

        binding.listSort.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.setOnMenuItemClickListener { item ->
                val sort = when (item.itemId) {
                    R.id.score -> "score"
                    R.id.title -> "title"
                    R.id.updated -> "updatedAt"
                    R.id.release -> "release"
                    else -> null
                }
                PrefManager.setVal(PrefName.AnimeListSortOrder, sort ?: "")
                binding.listProgressBar.visibility = View.VISIBLE
                binding.listViewPager.adapter = null
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        model.loadLists(true, ani.streamix.connections.anilist.Anilist.userid ?: 0, sort)
                    }
                }
                true
            }
            popup.inflate(R.menu.list_sort_menu)
            popup.show()
        }

        binding.filter.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            popup.menu.add(Menu.NONE, 0, Menu.NONE, "All")
            val genres = model.getAllGenres()
            if (genres.isNotEmpty()) {
                val sub = popup.menu.addSubMenu("Filter by Genre")
                genres.forEachIndexed { index, genre -> sub.add(1, index + 1, Menu.NONE, genre) }
            }
            val tags = model.getAllTags()
            if (tags.isNotEmpty()) {
                val sub = popup.menu.addSubMenu("Filter by Tag")
                tags.forEachIndexed { index, tag -> sub.add(2, index + 10000, Menu.NONE, tag) }
            }
            popup.setOnMenuItemClickListener { item ->
                when (item.groupId) {
                    0 -> model.unfilterLists()
                    1 -> model.filterLists(item.title.toString())
                    2 -> model.filterListsByTag(item.title.toString())
                }
                true
            }
            popup.show()
        }

        binding.random.setOnClickListener {
            val currentTab = binding.listTabLayout.getTabAt(binding.listTabLayout.selectedTabPosition)
            val fragment = childFragmentManager.findFragmentByTag("f" + currentTab?.position.toString()) as? ListFragment
            fragment?.randomOptionClick()
        }

        binding.search.setOnClickListener {
            toggleSearchView(binding.searchView.isVisible)
            if (!binding.searchView.isVisible) model.unfilterLists()
        }

        binding.searchViewText.addTextChangedListener {
            model.searchLists(binding.searchViewText.text.toString())
        }
    }

    private fun toggleSearchView(isVisible: Boolean) {
        if (isVisible) {
            binding.searchView.visibility = View.GONE
            binding.searchViewText.text.clear()
        } else {
            binding.searchView.visibility = View.VISIBLE
            binding.searchViewText.requestFocus()
            val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchViewText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        Refresh.activity.remove(this.hashCode())
        binding.listViewPager.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
