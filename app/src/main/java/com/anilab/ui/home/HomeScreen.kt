package com.anilab.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class HomeAnime(
    val title: String,
    val subtitle: String,
    val image: String
)

private val featured = HomeAnime(
    "Solo Leveling",
    "Season 2 • Action • Fantasy",
    "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx176496-Q2k5K9ZQh2xR.jpg"
)

private val continueWatching = listOf(
    HomeAnime("One Piece", "Episode 1138", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx21-tAq4x5Xk3QzD.jpg"),
    HomeAnime("Solo Leveling", "Episode 13", featured.image),
    HomeAnime("Jujutsu Kaisen", "Episode 48", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx145874-6jL6VqQ9h5rG.jpg")
)

private val trending = listOf(
    HomeAnime("One Piece", "TV • Ongoing", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx21-tAq4x5Xk3QzD.jpg"),
    HomeAnime("Frieren", "TV • Completed", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx154587-9qK8z5s3pX7M.jpg"),
    HomeAnime("Demon Slayer", "TV • Ongoing", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx101922-p3N4w2m8L6sQ.jpg"),
    HomeAnime("Jujutsu Kaisen", "TV • Ongoing", "https://s4.anilist.co/file/anilistcdn/media/anime/cover/large/bx145874-6jL6VqQ9h5rG.jpg")
)

@Composable
fun HomeScreen(
    onSearch: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onAnimeClick: (HomeAnime) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 20.dp)
    ) {
        HomeTopBar(onSearch, onNotifications)
        FeaturedBanner(featured, onAnimeClick)
        Spacer(Modifier.height(22.dp))
        AnimeSection("Continue Watching", continueWatching, onAnimeClick, compact = true)
        Spacer(Modifier.height(24.dp))
        AnimeSection("Trending Anime", trending, onAnimeClick)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun HomeTopBar(onSearch: () -> Unit, onNotifications: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("AniLab", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("What are you watching today?", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = "Search") }
        IconButton(onClick = onNotifications) { Icon(Icons.Filled.NotificationsNone, contentDescription = "Notifications") }
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Text("S", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer) }
    }
}

@Composable
private fun FeaturedBanner(anime: HomeAnime, onClick: (HomeAnime) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(245.dp)
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick(anime) }
    ) {
        AsyncImage(model = anime.image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.42f)
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF0D0D10).copy(alpha = 0.98f)))))
        Row(modifier = Modifier.align(Alignment.BottomStart).padding(18.dp), verticalAlignment = Alignment.Bottom) {
            AsyncImage(model = anime.image, contentDescription = anime.title, modifier = Modifier.width(94.dp).height(136.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.padding(bottom = 2.dp)) {
                Text("FEATURED", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(anime.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(anime.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary) {
                    Row(modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Details", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimeSection(title: String, items: List<HomeAnime>, onClick: (HomeAnime) -> Unit, compact: Boolean = false) {
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("See all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp)) {
            items(items) { anime -> AnimePoster(anime, onClick, compact) }
        }
    }
}

@Composable
private fun AnimePoster(anime: HomeAnime, onClick: (HomeAnime) -> Unit, compact: Boolean) {
    val width = if (compact) 118.dp else 132.dp
    Column(modifier = Modifier.width(width).clickable { onClick(anime) }) {
        AsyncImage(model = anime.image, contentDescription = anime.title, modifier = Modifier.fillMaxWidth().height(if (compact) 166.dp else 188.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(7.dp))
        Text(anime.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(anime.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
