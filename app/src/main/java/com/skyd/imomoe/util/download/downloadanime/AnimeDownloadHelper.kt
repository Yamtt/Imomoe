package com.skyd.imomoe.util.download.downloadanime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.skyd.imomoe.R
import com.skyd.imomoe.appContext
import com.skyd.imomoe.config.Const
import com.skyd.imomoe.config.Const.DownloadAnime.animeFilePath
import com.skyd.imomoe.database.entity.AnimeDownloadEntity
import com.skyd.imomoe.util.download.downloadanime.AnimeDownloadService.Companion.ANIME_EPISODE
import com.skyd.imomoe.util.download.downloadanime.AnimeDownloadService.Companion.ANIME_TITLE
import com.skyd.imomoe.util.download.downloadanime.AnimeDownloadService.Companion.DOWNLOAD_URL_KEY
import com.skyd.imomoe.util.download.downloadanime.AnimeDownloadService.Companion.STORE_DIRECTORY_PATH_KEY
import com.skyd.imomoe.util.showToast
import com.skyd.imomoe.view.listener.dsl.requestPermissions
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


object AnimeDownloadHelper {

    fun downloadAnime(
        activity: AppCompatActivity,
        url: String,
        animeTitle: String,
        animeEpisode: String
    ) {
        if (activity.isFinishing) {
            appContext.getString(R.string.do_not_finish_the_page_when_parse_download_data)
                .showToast()
            return
        }
        XXPermissions.with(activity)
            .permission(
                Permission.MANAGE_EXTERNAL_STORAGE,
                Permission.NOTIFICATION_SERVICE
            )
            .requestPermissions {
                onGranted { _, all ->
                    if (!all) return@onGranted
                    activity.startService(
                        Intent(activity, AnimeDownloadService::class.java)
                            .putExtra(DOWNLOAD_URL_KEY, url)
                            .putExtra(ANIME_TITLE, animeTitle)
                            .putExtra(ANIME_EPISODE, animeEpisode)
                            .putExtra(STORE_DIRECTORY_PATH_KEY, animeFilePath + animeTitle)
                    )
                }
                onDenied { permissions, _ ->
                    if (permissions?.contains(Permission.MANAGE_EXTERNAL_STORAGE) == false) {
                        activity.getString(R.string.no_storage_can_not_download).showToast()
                    }
                    if (permissions?.contains(Permission.NOTIFICATION_SERVICE) == false) {
                        activity.getString(R.string.no_notification_service).showToast()
                    }
                }
            }
    }

    private fun createXml(folderName: String) {
        val builderFactory = DocumentBuilderFactory.newInstance()
        // 从DOM工厂里获取DOM解析器
        val documentBuilder: DocumentBuilder
        try {
            documentBuilder = builderFactory.newDocumentBuilder()
            val document = documentBuilder.newDocument()
            //创建节点
            val data: Element = document.createElement("data")
            //将节点添加到document中
            document.appendChild(data)
            //保存
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val domSource = DOMSource(document)
            //设置编码类型
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val f = File(animeFilePath + folderName, "data.xml")
            val result = StreamResult(FileOutputStream(f))
            //把DOM树转换为xml文件
            transformer.transform(domSource, result)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun save2Xml(
        folderName: String, entity: AnimeDownloadEntity,
        animeFilePath: String = Const.DownloadAnime.animeFilePath
    ) {
        try {
            val file = File(animeFilePath + folderName, "data.xml")
            if (!file.exists()) {
                createXml(folderName)
            }
            // 1.得到DOM解析器的工厂实例
            val builderFactory = DocumentBuilderFactory.newInstance()
            // 2.从DOM工厂里获取DOM解析器
            val documentBuilder: DocumentBuilder = builderFactory.newDocumentBuilder()
            // 3.解析XML文档，得到DOM树
            val docs = documentBuilder.parse(file)
            val animeList = docs.getElementsByTagName("anime")
            var replaced = false
            for (i in 0 until animeList.length) {
                val anime: Node = animeList.item(i)
                // 遍历anime的所有属性
                val nodeList: NodeList = anime.childNodes
                var fileName: Node? = null
                var title: Node? = null
                var md5: Node? = null
                for (j in 0 until nodeList.length) {
                    val node: Node = nodeList.item(j)
                    if (node.nodeType == Node.ELEMENT_NODE) {
                        when (node.nodeName) {
                            "fileName" -> fileName = node.firstChild
                            "md5" -> md5 = node.firstChild
                            "title" -> title = node.firstChild
                        }
                    }
                }
                if (entity.md5 == md5?.nodeValue) {
                    val newFileName = fileName?.cloneNode(true)
                    newFileName?.textContent = entity.fileName
                    val newTitle = title?.cloneNode(true)
                    newTitle?.textContent = entity.title
                    anime.replaceChild(newFileName, fileName)
                    anime.replaceChild(newTitle, title)
                    replaced = true
                    break
                }
            }
            if (!replaced) {
                //创建节点
                val animeElement = docs.createElement("anime")
                val fileNameElement = docs.createElement("fileName")
                fileNameElement.textContent = entity.fileName
                val md5Element = docs.createElement("md5")
                md5Element.textContent = entity.md5
                val titleElement = docs.createElement("title")
                titleElement.textContent = entity.title

                //添加父子关系
                animeElement.appendChild(fileNameElement)
                animeElement.appendChild(md5Element)
                animeElement.appendChild(titleElement)
                docs.documentElement.appendChild(animeElement)
            }

            //保存xml文件
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val domSource = DOMSource(docs)
            //设置编码类型
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val result = StreamResult(FileOutputStream(file))
            //把DOM树转换为xml文件
            transformer.transform(domSource, result)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getAnimeFromXml(
        folderName: String,
        animeFilePath: String = Const.DownloadAnime.animeFilePath
    ): MutableList<AnimeDownloadEntity> {
        val list: MutableList<AnimeDownloadEntity> = ArrayList()
        try {
            // 1. 创建DocumentBuilderFactory对象
            val dFactory = DocumentBuilderFactory.newInstance()
            // 2. 创建DocumentBuilder对象
            val dBuilder = dFactory.newDocumentBuilder()
            // 3. 通过DocumentBuilder的parse方法解析xml
            val file = File("$animeFilePath$folderName/data.xml")
            if (!file.exists()) {
                createXml(folderName)
            }
            val doc = dBuilder.parse(FileInputStream(file))
            // 4. 根据根节点名称获取所有的people节点
            val animeList = doc.getElementsByTagName("anime")
            // 5. 遍历所有的people节点
            for (i in 0 until animeList.length) {
                val anime: Node = animeList.item(i)
                // 遍历anime的所有属性
                val entity = AnimeDownloadEntity("", "", "")
                val nodeList: NodeList = anime.childNodes
                for (j in 0 until nodeList.length) {
                    val node: Node = nodeList.item(j)
                    if (node.nodeType == Node.ELEMENT_NODE) {
                        when (node.nodeName) {
                            "fileName" -> entity.fileName = node.firstChild.nodeValue
                            "md5" -> entity.md5 = node.firstChild.nodeValue
                            "title" -> entity.title = node.firstChild.nodeValue
                        }
                    }
                }
                list.add(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun deleteAnimeFromXml(
        folderName: String, entity: AnimeDownloadEntity,
        animeFilePath: String = Const.DownloadAnime.animeFilePath
    ) {
        try {
            // 1. 创建DocumentBuilderFactory对象
            val builderFactory = DocumentBuilderFactory.newInstance()
            // 2. 创建DocumentBuilder对象
            val documentBuilder = builderFactory.newDocumentBuilder()
            val file = File("$animeFilePath$folderName/data.xml")
            if (!file.exists()) return
            // 3. 通过DocumentBuilder的parse方法解析xml
            val document = documentBuilder.parse(FileInputStream(file))
            // 4. 根据根节点名称获取所有的people节点
            val animeList = document.getElementsByTagName("anime")
            // 5. 遍历所有的people节点
            for (i in 0 until animeList.length) {
                val anime: Node = animeList.item(i)
                if (!anime.hasChildNodes()) continue
                // 遍历anime的所有属性
                val nodeList: NodeList = anime.childNodes
                var pickTimes = 0   // 匹配次数
                for (j in 0 until nodeList.length) {
                    val node: Node = nodeList.item(j)
                    if (node.nodeType == Node.ELEMENT_NODE) {
                        if ((node.nodeName == "fileName" && node.firstChild.nodeValue == entity.fileName) ||
                            (node.nodeName == "md5" && node.firstChild.nodeValue == entity.md5) ||
                            (node.nodeName == "title" && node.firstChild.nodeValue == entity.title)
                        )
                            pickTimes++
                    }
                }
                if (pickTimes == 3) anime.parentNode.removeChild(anime)
            }
            // 关闭
            //保存xml文件
            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()
            val domSource = DOMSource(document)
            //设置编码类型
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            val result = StreamResult(FileOutputStream(file))
            //把DOM树转换为xml文件
            transformer.transform(domSource, result)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}