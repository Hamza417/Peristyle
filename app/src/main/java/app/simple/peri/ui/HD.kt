package app.simple.peri.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.simple.peri.constants.BundleConstants
import app.simple.peri.databinding.FragmentHdImageBinding
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ParcelUtils.parcelable
import com.davemorrissey.labs.subscaleview.ImageSource

class HD : Fragment() {

    private var binding: FragmentHdImageBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHdImageBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kotlin.runCatching {
            binding?.hdImageView?.setImage(
                    ImageSource.uri(
                            requireArguments()
                                .parcelable<Wallpaper>(BundleConstants.WALLPAPER)?.uri!!))
        }.onFailure {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    companion object {
        fun newInstance(wallpaper: Wallpaper): HD {
            val args = Bundle()
            val fragment = HD()
            args.putParcelable(BundleConstants.WALLPAPER, wallpaper)
            fragment.arguments = args
            return fragment
        }
    }
}