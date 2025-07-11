package app.simple.peri.coil.keyers

import app.simple.peri.coil.models.ContextFolder
import coil3.key.Keyer
import coil3.request.Options

class FolderKeyer : Keyer<ContextFolder> {
    override fun key(data: ContextFolder, options: Options): String? {
        return data.hashCode().toString()
    }
}
