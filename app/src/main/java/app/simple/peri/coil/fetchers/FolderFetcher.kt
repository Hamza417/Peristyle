package app.simple.peri.coil.fetchers

import app.simple.peri.coil.impl.FolderFetcherImpl
import app.simple.peri.coil.models.ContextFolder
import coil3.ImageLoader
import coil3.fetch.Fetcher
import coil3.request.Options

class FolderFetcher : Fetcher.Factory<ContextFolder> {
    override fun create(data: ContextFolder, options: Options, imageLoader: ImageLoader): Fetcher? {
        return FolderFetcherImpl(data)
    }
}