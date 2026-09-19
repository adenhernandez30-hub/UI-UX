package ani.streamix.media.manga

import ani.streamix.media.MediaDetailsViewModel
import ani.streamix.media.SourceAdapter
import ani.streamix.media.SourceSearchDialogFragment
import ani.streamix.parsers.ShowResponse
import kotlinx.coroutines.CoroutineScope

class MangaSourceAdapter(
    sources: List<ShowResponse>,
    val model: MediaDetailsViewModel,
    val i: Int,
    val id: Int,
    fragment: SourceSearchDialogFragment,
    scope: CoroutineScope
) : SourceAdapter(sources, fragment, scope) {
    override suspend fun onItemClick(source: ShowResponse) {
        model.overrideMangaChapters(i, source, id)
    }
}