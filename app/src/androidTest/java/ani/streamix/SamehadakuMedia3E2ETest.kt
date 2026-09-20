package ani.streamix

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kakaanime.providerv2.AniLabExtractionContext
import com.kakaanime.providerv2.AniLabProviderFactory
import com.kakaanime.providerv2.AniLabVerifiedPipelineResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SamehadakuMedia3E2ETest {
    private var scenario: ActivityScenario<Media3E2EActivity>? = null

    @Before
    fun reset() {
        Media3E2EActivity.playbackError = null
        Media3E2EActivity.candidate = null
    }

    @After
    fun cleanup() {
        scenario?.close()
        scenario = null
    }

    @Test
    fun samehadaku_provider_to_media3_first_frame() = runBlocking {
        val episodeUrl = "https://v2.samehadaku.how/one-piece-episode-1178/"

        val stack = AniLabProviderFactory.samehadaku()
        val provider = stack.providers.single { it.id == "samehadaku" }

        val episodeCandidates = provider.loadLinks(episodeUrl)
        assertFalse(
            "Samehadaku returned no episode-page candidates",
            episodeCandidates.isEmpty(),
        )

        val verified = stack.verifiedPipeline.resolve(
            providerId = provider.id,
            candidates = episodeCandidates,
            context = AniLabExtractionContext(referer = episodeUrl),
        )

        assertTrue(
            "Samehadaku returned no verified stream candidates: $verified",
            verified is AniLabVerifiedPipelineResult.Candidates &&
                verified.candidates.isNotEmpty(),
        )

        val result = verified as AniLabVerifiedPipelineResult.Candidates
        val selected = result.candidates.firstOrNull()
        assertNotNull("No verified candidate selected", selected)

        Media3E2EActivity.candidate = selected ?: error("No verified candidate selected")
        scenario = ActivityScenario.launch(Media3E2EActivity::class.java)

        val rendered = Media3E2EActivity.firstFrameLatch.await(90, TimeUnit.SECONDS)
        val error = Media3E2EActivity.playbackError

        assertTrue(
            "Media3 did not render first frame within 90s. error=$error",
            rendered,
        )
    }
}
