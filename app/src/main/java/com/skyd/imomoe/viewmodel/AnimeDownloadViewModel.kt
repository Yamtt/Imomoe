package com.skyd.imomoe.viewmodel

import androidx.lifecycle.ViewModel
import com.skyd.imomoe.R
import com.skyd.imomoe.appContext
import com.skyd.imomoe.bean.AnimeCover7Bean
import com.skyd.imomoe.config.Const
import com.skyd.imomoe.database.entity.AnimeDownloadEntity
import com.skyd.imomoe.database.getAppDataBase
import com.skyd.imomoe.ext.formatSize
import com.skyd.imomoe.ext.request
import com.skyd.imomoe.ext.toMD5
import com.skyd.imomoe.route.Router.buildRouteUri
import com.skyd.imomoe.route.processor.EpisodeDownloadProcessor
import com.skyd.imomoe.route.processor.PlayDownloadM3U8Processor
import com.skyd.imomoe.route.processor.PlayDownloadProcessor
import com.skyd.imomoe.util.compare.EpisodeTitleSort.sortEpisodeTitle
import com.skyd.imomoe.util.download.downloadanime.AnimeDownloadHelper.deleteAnimeFromXml
import com.skyd.imomoe.util.download.downloadanime.AnimeDownloadHelper.getAnimeFromXml
import com.skyd.imomoe.util.download.downloadanime.AnimeDownloadHelper.save2Xml
import com.skyd.imomoe.util.showToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File


class AnimeDownloadViewModel : ViewModel() {
    var mode = 0        //0是默认的，是番剧；1是番剧每一集
    var actionBarTitle = ""
    var directoryName = ""
    var path = 0
    val animeCoverList: MutableStateFlow<AnimeDownloadUiState> =
        MutableStateFlow(AnimeDownloadUiState.None)
    val delete: MutableSharedFlow<DeleteUiState> = MutableSharedFlow(extraBufferCapacity = 1)

    fun getAnimeCover() {
        request(request = {
            val files = arrayOf(File(Const.DownloadAnime.animeFilePath).listFiles(),
                Const.DownloadAnime.run {
                    new = false
                    val f = File(animeFilePath).listFiles()
                    new = true
                    f
                })
            val list: MutableList<Any> = ArrayList()
            for (i: Int in 0..1) {
                files[i]?.let {
                    for (file in it) {
                        if (file.isDirectory) {
                            val episodeCount = file.listFiles { file, s ->
                                //查找文件名不以.temp结尾的文件
                                !s.endsWith(".temp") && !s.endsWith(".xml")
                            }?.size
                            list.add(
                                AnimeCover7Bean(
                                    EpisodeDownloadProcessor.route.buildRouteUri {
                                        appendQueryParameter("directoryName", file.name)
                                        appendQueryParameter(
                                            "type",
                                            (if (i == 0) 0 else 1).toString()
                                        )
                                        appendQueryParameter("animeTitle", file.name)
                                    }.toString(),
                                    title = file.name,
                                    size = file.formatSize(),
                                    episodeCount = episodeCount.toString() + "P",
                                    path = file.path,
                                    pathType = if (i == 0) 0 else 1
                                )
                            )
                        }
                    }
                }
            }
            list
        }, success = {
            animeCoverList.tryEmit(AnimeDownloadUiState.Success(it))
        }, error = {
            animeCoverList.tryEmit(AnimeDownloadUiState.Error(it.message.orEmpty()))
            "${appContext.getString(R.string.get_data_failed)}\n${it.message}".showToast()
        })
    }

    fun getAnimeCoverEpisode() {
        //不支持重命名文件
        request(request = {
            val animeFilePath = if (path == 0) Const.DownloadAnime.animeFilePath
            else {
                Const.DownloadAnime.new = false
                val p = Const.DownloadAnime.animeFilePath
                Const.DownloadAnime.new = true
                p
            }
            val files = File(animeFilePath + directoryName).listFiles()
            if (files != null) {
                val animeList = getAnimeFromXml(directoryName, animeFilePath)

                // xml里的文件名
                val animeFilesName: MutableList<String?> = ArrayList()
                // 文件夹下的文件名
                val filesName: MutableList<String> = ArrayList()
                // 获取文件夹下的文件名
                for (file in files) filesName.add(file.name)
                //数据库中的数据
                val animeMd5InDB = getAppDataBase().animeDownloadDao().getAnimeDownloadMd5List()
                // 先删除xml里被用户删除的视频，再获取xml里的文件名（保证xml里的文件名都是存在的文件）
                val iterator: MutableIterator<AnimeDownloadEntity> = animeList.iterator()
                while (iterator.hasNext()) {
                    val anime = iterator.next()
                    if (anime.fileName !in filesName) {
                        deleteAnimeFromXml(directoryName, anime, animeFilePath)
                        iterator.remove()
                    } else {
                        // 如果不在数据库中，则加入数据库
                        if (anime.md5 !in animeMd5InDB) {
                            getAppDataBase().animeDownloadDao().insertAnimeDownload(anime)
                        }
                        animeFilesName.add(anime.fileName)
                    }
                }
                // 没有在xml里的视频
                for (file in files) {
                    if (file.name !in animeFilesName) {
                        // 试图从数据库中取出不在xml里的视频的数据，如果没找到则是null
                        val unsavedAnime: AnimeDownloadEntity? =
                            getAppDataBase().animeDownloadDao()
                                .getAnimeDownload(file.toMD5() ?: "")
                        if (unsavedAnime != null && unsavedAnime.fileName == null) {
                            unsavedAnime.fileName = file.name
                            getAppDataBase().animeDownloadDao()
                                .updateFileNameByMd5(unsavedAnime.md5, file.name)
                        }
                        if (unsavedAnime != null) {
                            save2Xml(directoryName, unsavedAnime, animeFilePath)
                            animeList.add(unsavedAnime)
                        }
                    }
                }

                val list: MutableList<AnimeCover7Bean> = ArrayList()
                for (anime in animeList) {
                    val fileName = animeFilePath + directoryName + "/" + anime.fileName
                    var route = if (fileName.endsWith(".m3u8", true)) {
                        PlayDownloadM3U8Processor.route
                    } else {
                        PlayDownloadProcessor.route
                    }
                    route = route.buildRouteUri {
                        appendQueryParameter("filePath", fileName)
                        appendQueryParameter("animeTitle", actionBarTitle)
                        appendQueryParameter("episodeTitle", anime.title)
                    }.toString()
                    list.add(
                        AnimeCover7Bean(
                            route,
                            title = anime.title,
                            size = File(animeFilePath + directoryName + "/" + anime.fileName).formatSize(),
                            path = fileName,
                            pathType = path
                        )
                    )
                }
                list.sortEpisodeTitle()
            } else {
                emptyList()
            }
        }, success = {
            animeCoverList.tryEmit(AnimeDownloadUiState.Success(it))
        }, error = {
            animeCoverList.tryEmit(AnimeDownloadUiState.Error(it.message.orEmpty()))
            "${appContext.getString(R.string.get_data_failed)}\n${it.message}".showToast()
        })
    }

    private var deleteJob: Job? = null

    fun cancelDelete() {
        deleteJob?.cancel()
    }

    fun delete(path: String) {
        deleteJob = request(request = {
            val file = File(path)
            file.deleteRecursively()
        }, success = {
            delete.tryEmit(if (it) DeleteUiState.Success(path) else DeleteUiState.Failed(path))
        }, error = {
            delete.tryEmit(DeleteUiState.Failed(path))
            it.message?.showToast()
        }, finish = { deleteJob = null })
    }
}

sealed interface DeleteUiState {
    object None : DeleteUiState
    data class Success(val message: String = "") : DeleteUiState
    data class Failed(val message: String = "") : DeleteUiState

    fun getMessageData(): String {
        return if (this is Success) message else if (this is Failed) message else ""
    }
}

sealed interface AnimeDownloadUiState {
    object None : AnimeDownloadUiState
    data class Success(override val dataList: List<Any>) : WithData(dataList)
    data class Error(val message: String = "") : AnimeDownloadUiState
    data class Refreshing(override val dataList: List<Any>? = null) : WithData(dataList)

    abstract class WithData(open val dataList: List<Any>? = null) : AnimeDownloadUiState
    fun readOrNull(): List<Any>? = (this as? WithData)?.dataList
}