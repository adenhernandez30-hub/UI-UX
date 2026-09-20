package ani.streamix.home

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import ani.streamix.MediaPageTransformer
import ani.streamix.R
import ani.streamix.connections.anilist.Anilist
import ani.streamix.connections.mal.MAL
import ani.streamix.databinding.ItemAnimePageBinding
import ani.streamix.databinding.LayoutTrendingBinding
import ani.streamix.getAppString
import ani.streamix.getThemeColor
import ani.streamix.loadImage
import ani.streamix.media.Media
import ani.streamix.media.MediaAdaptor
import ani.streamix.media.MediaListViewActivity
import ani.streamix.media.SearchActivity
import ani.streamix.px
import ani.streamix.setSafeOnClickListener
import ani.streamix.setSlideIn
import ani.streamix.setSlideUp
import ani.streamix.settings.saving.PrefManager
import ani.streamix.settings.saving.PrefName
import ani.streamix.statusBarHeight
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout

class AnimePageAdapter : RecyclerView.Adapter<AnimePageAdapter.AnimePageViewHolder>() {
    val ready = MutableLiveData(false)
    lateinit var binding: ItemAnimePageBinding
    private lateinit var trendingBinding: LayoutTrendingBinding
    private var trendHandler: Handler? = null
    private lateinit var trendRun: Runnable
    var trendingViewPager: ViewPager2? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimePageViewHolder {
        val binding =
            ItemAnimePageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnimePageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnimePageViewHolder, position: Int) {
        binding = holder.binding
        trendingBinding = LayoutTrendingBinding.bind(binding.root)
        trendingViewPager = trendingBinding.trendingViewPager

        val textInputLayout = holder.itemView.findViewById<TextInputLayout>(R.id.searchBar)
        val currentColor = textInputLayout.boxBackgroundColor
        val semiTransparentColor = (currentColor and 0x00FFFFFF) or 0xA8000000.toInt()
        textInputLayout.boxBackgroundColor = semiTransparentColor
        val materialCardView =
            holder.itemView.findViewById<MaterialCardView>(R.id.userAvatarContainer)
        materialCardView.setCardBackgroundColor(semiTransparentColor)
        val color = binding.root.context.getThemeColor(android.R.attr.windowBackground)
        textInputLayout.boxBackgroundColor = (color and 0x00FFFFFF) or 0x28000000
        materialCardView.setCardBackgroundColor((color and 0x00FFFFFF) or 0x28000000)

        trendingBinding.titleContainer.updatePadding(top = statusBarHeight)

        if (PrefManager.getVal(PrefName.SmallView)) trendingBinding.trendingContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = (-108f).px
        }

        updateAvatar()

        trendingBinding.searchBar.hint = binding.root.context.getString(R.string.search)
        trendingBinding.searchBarText.setOnClickListener {
            val context = binding.root.context
            if (PrefManager.getVal(PrefName.AniMangaSearchDirect) && Anilist.token != null) {
                ContextCompat.startActivity(
                    context,
                    Intent(context, SearchActivity::class.java).putExtra("type", "ANIME"),
                    null
                )
            } else {
                SearchBottomSheet.newInstance().show(
                    (context as AppCompatActivity).supportFragmentManager,
                    "search"
                )
            }
        }

        trendingBinding.userAvatar.setSafeOnClickListener {
            ContextCompat.startActivity(
                it.context,
                Intent(it.context, SearchActivity::class.java)
                    .putExtra("type", "ANIME")
                    .putExtra("openFilter", true),
                null
            )
        }

        trendingBinding.searchBar.setEndIconOnClickListener {
            trendingBinding.searchBar.performClick()
        }

        trendingBinding.notificationCount.isVisible = false
        listOf(
            binding.animePreviousSeason,
            binding.animeThisSeason,
            binding.animeNextSeason
        ).forEachIndexed { i, it ->
            it.setSafeOnClickListener { onSeasonClick.invoke(i) }
            it.setOnLongClickListener { onSeasonLongClick.invoke(i) }
        }

        val rescueMode = PrefManager.getVal<Boolean>(PrefName.RescueMode)
        binding.animeIncludeList.isVisible = if (rescueMode) MAL.token != null else Anilist.token != null

        binding.animeIncludeList.isChecked = PrefManager.getVal(PrefName.PopularAnimeList)

        binding.animeIncludeList.setOnCheckedChangeListener { _, isChecked ->
            onIncludeListClick.invoke(isChecked)

            PrefManager.setVal(PrefName.PopularAnimeList, isChecked)
        }
        if (ready.value == false)
            ready.postValue(true)
    }

    lateinit var onSeasonClick: ((Int) -> Unit)
    lateinit var onSeasonLongClick: ((Int) -> Boolean)
    lateinit var onIncludeListClick: ((Boolean) -> Unit)

    override fun getItemCount(): Int = 1

    fun updateHeight() {
        trendingViewPager!!.updateLayoutParams { height += statusBarHeight }
    }

    fun updateTrending(adaptor: MediaAdaptor) {
        trendingBinding.trendingProgressBar.visibility = View.GONE
        trendingBinding.trendingViewPager.adapter = adaptor
        trendingBinding.trendingViewPager.offscreenPageLimit = 3
        trendingBinding.trendingViewPager.getChildAt(0).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
        trendingBinding.trendingViewPager.setPageTransformer(MediaPageTransformer())
        trendHandler = Handler(Looper.getMainLooper())
        trendRun = Runnable {
            trendingBinding.trendingViewPager.currentItem += 1
        }
        trendingBinding.trendingViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    trendHandler?.removeCallbacks(trendRun)
                    if (PrefManager.getVal(PrefName.TrendingScroller)) {
                        trendHandler!!.postDelayed(trendRun, 4000)
                    }
                }
            }
        )

        trendingBinding.trendingViewPager.layoutAnimation =
            LayoutAnimationController(setSlideIn(), 0.25f)
        trendingBinding.titleContainer.startAnimation(setSlideUp())
        binding.animeListContainer.layoutAnimation =
            LayoutAnimationController(setSlideIn(), 0.25f)
        binding.animeSeasonsCont.layoutAnimation =
            LayoutAnimationController(setSlideIn(), 0.25f)
    }

    fun updateContinue(adaptor: MediaAdaptor, media: MutableList<Media>) {
        binding.apply {
            init(
                adaptor,
                animeContinueRecyclerView,
                animeContinueProgressBar,
                animeContinue,
                animeContinueMore,
                getAppString(R.string.continue_watching),
                media
            )
            if (adaptor.itemCount == 0) {
                animeContinueContainer.visibility = View.GONE
            }
        }
    }

    fun updateRecent(adaptor: MediaAdaptor, media: MutableList<Media>) {
        binding.apply {
            init(
                adaptor,
                animeUpdatedRecyclerView,
                animeUpdatedProgressBar,
                animeRecently,
                animeRecentlyMore,
                getAppString(R.string.updated),
                media
            )
            animePopular.visibility = View.VISIBLE
            animePopular.startAnimation(setSlideUp())
            if (adaptor.itemCount == 0) {
                animeRecentlyContainer.visibility = View.GONE
            }
        }

    }

    fun updateMovies(adaptor: MediaAdaptor, media: MutableList<Media>) {
        binding.apply {
            init(
                adaptor,
                animeMoviesRecyclerView,
                animeMoviesProgressBar,
                animeMovies,
                animeMoviesMore,
                getAppString(R.string.trending_movies),
                media
            )
        }
    }

    fun updateTopRated(adaptor: MediaAdaptor, media: MutableList<Media>) {
        binding.apply {
            init(
                adaptor,
                animeTopRatedRecyclerView,
                animeTopRatedProgressBar,
                animeTopRated,
                animeTopRatedMore,
                getAppString(R.string.top_rated),
                media
            )
        }
    }

    fun updateMostFav(adaptor: MediaAdaptor, media: MutableList<Media>) {
        binding.apply {
            init(
                adaptor,
                animeMostFavRecyclerView,
                animeMostFavProgressBar,
                animeMostFav,
                animeMostFavMore,
                getAppString(R.string.most_favourite),
                media
            )
        }
    }

    private val sharedMediaPool = RecyclerView.RecycledViewPool().apply {
        setMaxRecycledViews(0, 25)
    }

    fun init(
        adaptor: MediaAdaptor,
        recyclerView: RecyclerView,
        progress: View,
        title: View,
        more: View,
        string: String,
        media: MutableList<Media>
    ) {
        progress.visibility = View.GONE
        recyclerView.setRecycledViewPool(sharedMediaPool)
        recyclerView.setHasFixedSize(true)
        val llm = LinearLayoutManager(
            recyclerView.context,
            LinearLayoutManager.HORIZONTAL,
            false
        ).apply {
            initialPrefetchItemCount = 4
        }
        recyclerView.layoutManager = llm
        recyclerView.adapter = adaptor

        more.setOnClickListener {
            MediaListViewActivity.passedMedia = media.toCollection(ArrayList())
            ContextCompat.startActivity(
                it.context, Intent(it.context, MediaListViewActivity::class.java)
                    .putExtra("title", string),
                null
            )
        }
        recyclerView.visibility = View.VISIBLE
        title.visibility = View.VISIBLE
        more.visibility = View.VISIBLE
        title.startAnimation(setSlideUp())
        more.startAnimation(setSlideUp())
        recyclerView.layoutAnimation =
            LayoutAnimationController(setSlideIn(), 0.25f)
    }

    fun updateAvatar() {
        // The home action is now the Anime filter button; keep the method for
        // existing lifecycle callers without changing their call sites.
    }

    fun updateNotificationCount() {
        // Notification/avatar behavior is intentionally removed from the Anime
        // home action button; it is now the filter button.
    }

    inner class AnimePageViewHolder(val binding: ItemAnimePageBinding) :
        RecyclerView.ViewHolder(binding.root)
}
