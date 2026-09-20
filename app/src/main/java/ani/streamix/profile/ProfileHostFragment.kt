package ani.streamix.profile

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import ani.streamix.R
import ani.streamix.connections.anilist.Anilist
import ani.streamix.connections.anilist.api.Query
import ani.streamix.home.LoginFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileHostFragment : Fragment() {
    private var container: FrameLayout? = null

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        parent: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).also {
            it.id = View.generateViewId()
            container = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val root = view as FrameLayout
        val progress = ProgressBar(requireContext()).apply {
            id = R.id.mainProgressBar
        }
        root.addView(progress, FrameLayout.LayoutParams(-2, -2, android.view.Gravity.CENTER))

        val userId = Anilist.userid
        if (userId == null) {
            showLogin(root)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val user = withContext(Dispatchers.IO) {
                Anilist.query.getUserProfile(userId)?.data?.user
            }
            if (!isAdded || _bindingGone()) return@launch
            if (user == null) showLogin(root) else showProfile(root, user)
        }
    }

    private fun showLogin(root: FrameLayout) {
        root.removeAllViews()
        childFragmentManager.commit {
            replace(root.id, LoginFragment())
        }
    }

    private fun showProfile(root: FrameLayout, user: Query.UserProfile) {
        root.removeAllViews()
        childFragmentManager.commit {
            replace(root.id, ProfileFragment.newInstance(user))
        }
    }

    private fun _bindingGone(): Boolean = view == null

    override fun onDestroyView() {
        container = null
        super.onDestroyView()
    }
}
