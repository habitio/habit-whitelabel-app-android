package com.muzzley.app.tiles

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.muzzley.Constants
import com.muzzley.model.channels.Address
import com.muzzley.model.realtime.RealtimeMessage
import com.muzzley.model.stores.InterfacesStore
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.retrofit.MuzzleyApiService
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import timber.log.Timber
import java.io.*
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.inject.Inject

class InterfacePresenter2

    @Inject
    constructor(
        val prefererenceRepository: PreferencesRepository,
        val context: Context,
        val muzzleyApiService: MuzzleyApiService,
        val channelService: ChannelService,
        val realtime: Realtime
    ){

//    fun readPropertyAsString(String channelId, String componentId, String propertyId): Observable<String> {
//        Timber.d("RTBridge: requesting = $channelId, $componentId, $propertyId")
//        muzzleyCoreService.readProperty(channelId,componentId,propertyId)
//            .map { it.asJsonObject.get('data').asString}
//    }

    fun readPropertyAsString3(address: Address ): Observable<String> {
        Timber.d("RTBridge: requesting = $address")
        return realtime.send(RealtimeMessage.read(address))
                .flatMap { realtime.listenToRTM() }
                .filter { it.address == address }
                .take(1)
                .map { it.payload?.data as String }
                .timeout(10,TimeUnit.SECONDS)
    }

    fun fetchInterface(uuid: String , etag: String): Observable<Map<String,String>>  {
        return Observable.defer {
            val interfacesStore = prefererenceRepository.interfacesStore ?: InterfacesStore()
            if (interfacesStore.containsInterface(uuid)) {
                if (interfacesStore.isNewEtag(uuid, etag)) {
                    fetchInterfaceUrl(uuid)
                } else {
                    val path = interfacesStore.getPath(uuid) ?: error("no path found")
                    Observable.just(mapOf("uuid" to uuid, "path" to path ))
                }
            } else {
                fetchInterfaceUrl(uuid);
            }
        }
    }


    fun fetchInterfaceUrl(uuid: String ): Observable<Map<String,String>> =
        muzzleyApiService.getInterfaceArchive(uuid)
                .flatMap { saveFile(it) }
                .flatMap { h ->
                    unzip( h["zipPath"] ?: "")
                    .flatMap { metaPath -> updateStore(metaPath, h)}
                }

    fun saveFile(response: Response<ResponseBody>? ): Observable<Map<String,String>> =
        Observable.defer {
            if (response?.body() != null) {
                val uri = Uri.parse(response.raw().request().url().toString());
                val headers = mapOf(
                        "uuid" to  getUuidfromPath(uri.getPath()),
                        "etag" to getValueFromWebResult(response, Constants.INTERFACE_ARCHIVE_HEADER_ETAG),
                        "sha" to getValueFromWebResult(response, Constants.INTERFACE_ARCHIVE_HEADER_SHA256)
                )
                if (headers.all { (_,v) -> !v.isNullOrEmpty()}){
                    saveFile((response.body() as ResponseBody).byteStream(),headers);
//                    saveFile(response.raw().body().byteStream(),headers);
//                        saveFile(response.getBody().in(),headers);
                } else {
                    Observable.error(RuntimeException("Invalid headers from HTTP GET request"));
                }
            } else {
                Observable.error(RuntimeException("Invalid response from HTTP GET request"));
            }
        }

    private fun getUuidfromPath(path: String ): String  =
        path.substring(1).split("/")[1];

    private fun getValueFromWebResult(response: Response<ResponseBody>, headerName: String): String =
        response.headers()?.values(headerName)?.getOrNull(0) ?: ""
//        response?.getHeaders()?.find { it.name == headerName }?.value

    fun saveFile(iinputStream: InputStream, headers: Map<String,String> ): Observable<Map<String,String>> =
        Observable.defer {
            val folderPath = createInterfacesDir().getPath() + File.separator + headers["uuid"] ;

            val folder = File(folderPath)
            if (!folder.exists()) {
                folder.mkdir()
            }

            val file = File(folderPath, Constants.ZIP_NAME);
            val zipPath = file.getAbsolutePath();

            var r: Observable<Map<String,String>>? = null
            iinputStream.use { inputStream ->
                FileOutputStream(file).use { output ->

                    inputStream.copyTo(output,8 * 1024)
                    output.flush()
                    val sha = headers["sha"]
                    if( sha != null && checkSum(zipPath, sha)) {
                         r = Observable.just(headers + mapOf("zipPath" to zipPath))
                    }

                }
            }
            r ?: Observable.error(Exception("Invalid hash file"));

        }

    fun createInterfacesDir(): File {
        val file = File(context.getFilesDir(), Constants.DIR_INTERFACES);
        if (!file.exists()) {
            file.mkdir()
        }
        return file
    }
    //throws NoSuchAlgorithmException, IOException
    fun checkSum(path: String , valueToCompare: String): Boolean =
        FileInputStream(File(path)).use { inputStream ->
            val sha = MessageDigest.getInstance("SHA-256");
            DigestInputStream(inputStream, sha).use { digestInputStream ->
                val buffer = ByteArray(8 * 1024)
                while (digestInputStream.read(buffer) != -1) {
                }
                val hash = sha.digest();
                val hashValue = byteToString(hash);
                valueToCompare == hashValue
            }
        }

    //FIXME: use printf %x ?
    private fun byteToString( input: ByteArray): String  {
        val hexString = StringBuilder();
        for (i in input.indices) {
            if ((0xff and input[i].toInt()) < 0x10) {
                hexString.append("0" + Integer.toHexString((0xFF and input[i].toInt())));
            } else {
                hexString.append(Integer.toHexString(0xFF and input[i].toInt()));
            }
        }
        return hexString.toString();
//        return ""
    }
    fun unzip(zipFilePath: String ) : Observable<String> =
            Observable.defer {

                val zipFile = File(zipFilePath);
                val folder = File(zipFile.getParent());

                var zipInputStream = null;
//                byte[] buffer = new byte[1024];
                ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
                    //get the zipped file list entry
                    var zipEntry = zipInputStream.getNextEntry();
                    while (zipEntry != null) {

                        val fileName = zipEntry.getName();
                        val newFile = File(folder.toString() + File.separator + fileName);
                        Timber.d("File unzip : " + newFile.getAbsoluteFile());

                        //create all non exists folders
                        //else you will hit FileNotFoundException for compressed folder
                        File(newFile.getParent()).mkdirs();

                        FileOutputStream(newFile).use { fileOutputStream ->
                            zipInputStream.copyTo(fileOutputStream)
                        }
                        zipEntry = zipInputStream.getNextEntry();
                    }
                    zipInputStream.closeEntry();

                    //delete zip file
                    zipFile.delete();

                    //check if interface-meta.json exists
                    val metaFile = File(folder.toString() + File.separator + "interface-meta.json");
                    if (metaFile.exists()) {
                        Observable.just(metaFile.getPath());
                    } else {
                        Observable.error(FileNotFoundException ());
                    }
                }
            }


    fun updateStore(path: String, headers: Map<String,String>): Observable<Map<String,String>> =
        Observable.defer {

            val mainFile = getPropertyFromJson(path, Constants.INTERFACE_ARCHIVE_META_PROPERTY_MAIN);
            val uuid = getPropertyFromJson(path, Constants.INTERFACE_ARCHIVE_META_PROPERTY_UUID);
//            if (mainFile != null && uuid != null) {
                val mainFilePath = injectMainFilePath(path, mainFile);
                Timber.d("Path:$mainFilePath");
                val interfacesStore = prefererenceRepository.interfacesStore ?: InterfacesStore()
                interfacesStore.addInterface(uuid, mainFilePath, headers["etag"] ?: error("no etag found") )
                prefererenceRepository.interfacesStore = interfacesStore
                Observable.just(headers + mapOf("path" to mainFilePath))
//            } else {
//                Observable.error(new RuntimeException("Failed to update store"))
//            }
        }

    // throws FileNotFoundException
    fun getPropertyFromJson(jsonPath: String, property: String ): String {
        val jsonFile = File(jsonPath);
        val parser = JsonParser();
        val obj = parser.parse(FileReader(jsonFile)) as? JsonObject
        val propertyJson = obj?.get(property);
        if (propertyJson != null) {
            return propertyJson.getAsString();
        } else {
            throw RuntimeException("Error parsing json")
        }
    }
    private fun injectMainFilePath(path: String , file: String): String =
        path.replace("interface-meta.json", file);


}