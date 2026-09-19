package ani.streamix.settings.saving.internal

import kotlin.reflect.KClass

data class Pref(
    val prefLocation: Location,
    val type: KClass<*>,
    val default: Any
)

enum class Location(val location: String, val exportable: Boolean) {
    General("ani.streamix.general", true),
    UI("ani.streamix.ui", true),
    Player("ani.streamix.player", true),
    Reader("ani.streamix.reader", true),
    NovelReader("ani.streamix.novelReader", true),
    Irrelevant("ani.streamix.irrelevant", false),
    AnimeDownloads("animeDownloads", false),  //different for legacy reasons
    Protected("ani.streamix.protected", true),
    ExtensionSettings("ani.streamix.extensionSettings", true);
}
