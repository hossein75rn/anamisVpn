package com.v2ray.anamin.ui

import android.Manifest
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import com.tbruyelle.rxpermissions.RxPermissions
import com.tencent.mmkv.MMKV
import com.v2ray.anamin.AppConfig
import com.v2ray.anamin.AppConfig.ANG_PACKAGE
import com.v2ray.anamin.R
import com.v2ray.anamin.data.RetrofitInstance
import com.v2ray.anamin.data.api.ApiService
import com.v2ray.anamin.data.model.ConfigFailure
import com.v2ray.anamin.data.model.ConfigSuccess
import com.v2ray.anamin.data.model.ConfigSuccess.Configs
import com.v2ray.anamin.data.utils.storeStringPreference
import com.v2ray.anamin.databinding.ActivityMainBinding
import com.v2ray.anamin.extension.toast
import com.v2ray.anamin.helper.SimpleItemTouchHelperCallback
import com.v2ray.anamin.service.DnsService
import com.v2ray.anamin.service.V2RayServiceManager
import com.v2ray.anamin.util.AngConfigManager
import com.v2ray.anamin.util.MmkvManager
import com.v2ray.anamin.util.Utils
import com.v2ray.anamin.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.drakeet.support.toast.ToastCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class MainActivity : BaseActivity() {
    //private var loading: KProgressHUD? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var loading:KProgressHUD
    private val adapter by lazy { MainRecyclerAdapter(this) }
    private val mainStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_MAIN, MMKV.MULTI_PROCESS_MODE) }
    private val settingsStorage by lazy { MMKV.mmkvWithID(MmkvManager.ID_SETTING, MMKV.MULTI_PROCESS_MODE) }
    private val requestVpnPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            startV2Ray()
        }
    }
    private var storeData: storeStringPreference? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    val mainViewModel: MainViewModel by viewModels()
    private var api: ApiService? = null
    private var tryToGetInfoFromServer = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        //title = getString(R.string.title_server)
        init()
        binding.tvName.text = readSharedP("fullName")
        binding.tvPhone.text = readSharedP("phone")
        binding.tvPayment.text = readSharedP("payment").toString()
        val userStatus = readSharedPeInt("user_status")
        checkUserStatus(userStatus?:1)

        binding.fab.setOnClickListener {
            if (mainViewModel.isRunning.value == true) {
                Utils.stopVService(this)
            } else if ((settingsStorage?.decodeString(AppConfig.PREF_MODE) ?: "VPN") == "VPN") {
                val intent = VpnService.prepare(this)
                if (intent == null) {
                    startV2Ray()
                } else {
                    requestVpnPermission.launch(intent)
                }
            } else {
                startV2Ray()
            }
        }
        binding.btnRequestConfigs.setOnClickListener {
            it.visibility = View.GONE
            getNewConfigs()
        }
        binding.layoutTest.setOnClickListener {
            if (mainViewModel.isRunning.value == true) {
                setTestState(getString(R.string.connection_test_testing))
                mainViewModel.testCurrentServerRealPing()
            } else {
//                tv_test_state.text = getString(R.string.connection_test_fail)
            }
        }

//        binding.btnDns.setOnClickListener {
//            val intent = Intent(this, DnsService::class.java)
//            startService(intent)
//        }
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        val callback = SimpleItemTouchHelperCallback(adapter)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(binding.recyclerView)


        setupViewModel()
        copyAssets()
        //migrateLegacy()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            RxPermissions(this)
                .request(Manifest.permission.POST_NOTIFICATIONS)
                .subscribe {
                    if (!it)
                        toast(R.string.toast_permission_denied)
                }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    //super.onBackPressed()
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        getNewConfigs()
    }

    private fun readSharedP(sp:String): String? {
        try {
            val sh = storeData?.read(sp);
            if (sh != null && sh.length > 0) {
                return sh
            }
        }catch (e:Exception){
            return ""
        }
        return ""
    }
     private fun readSharedPeInt(sp:String): Int? {
         var sh = 1;
         try {
             sh = storeData?.readInt(sp)?:1
         }catch (e:Exception){
             return 1
         }finally {
             return sh
         }

     }

     private fun getTimeDifference(targetTimestamp: Long): Long {
         val currentTimestamp = System.currentTimeMillis()

         return if (targetTimestamp > currentTimestamp) {
             targetTimestamp - currentTimestamp // Future: return difference in milliseconds
         } else {
             0L // Past: return zero
         }
     }

    fun convertBytesToReadableSize(up: Long, down: Long , tot:Long):Double {
        val totalBytes = tot - (up + down)
        if (totalBytes <= 0 )
            return 0.0
        val KB = 1024.0
        val MB = KB * 1024
        val GB = MB * 1024
        val TB = GB * 1024

        return when {
            totalBytes >= TB -> totalBytes / TB
            totalBytes >= GB -> totalBytes / GB
            totalBytes >= MB -> totalBytes / MB
            else -> totalBytes / KB
        }
    }

    fun checkUserStatus(status:Int){
        if (status==0){
            showAlert("unauthorized user","کاربر غبر مجاز")
        }else if (status == 403){
            showAlert("suspended_user","کاربر مسدود شده")
        }else if (status == 409){
            showAlert("duplicated_user", "کاربر جای دیگری فعال است");
        }
    }

    private fun getNewConfigs() {
        api?.config(readSharedP("uuid"),readSharedP("phone"))?.enqueue(object : Callback<ConfigSuccess?> {
            override fun onResponse(call: Call<ConfigSuccess?>, response: Response<ConfigSuccess?>) {
                if (response.isSuccessful) {
                    loading.dismiss()
                    binding.btnRequestConfigs.visibility = View.VISIBLE
                    mainStorage.clearAll()
                    // The API call was successful (2xx HTTP response)
                    val successResponse: ConfigSuccess? = response.body()
                    val name = successResponse?.user?.full_name
                    val phone = successResponse?.user?.username
                    val payments = successResponse?.user?.left_payment
                    val privateConfigs = successResponse?.configs?.costume?.firstOrNull()
                    val generalConfigs = successResponse?.configs?.general?.firstOrNull()
                    val xuiConfigs = successResponse?.configs?.xui?.firstOrNull()
                    val xuiConfigZero = xuiConfigs?.get(0)?.client
                    var countDown = xuiConfigZero?.expiryTime?:0
                    var up = 0L
                    var down = 0L
                    val totalData = xuiConfigZero?.totalGB?:0
                    if(countDown > 0){
                        countDown = getTimeDifference(countDown)
                        binding.counterWang.start(countDown)
                    }else {
                        countDown = countDown.absoluteValue
                        binding.counterWang.updateShow(countDown)

                    }

                    xuiConfigs?.forEach { config ->
                        importBatchConfig(config.link,config.client?.subId)
                        mainStorage.putLong(config.client?.subId,config.client.expiryTime)
                        up += config.client.up
                        down += config.client.down
                    }

                    privateConfigs?.forEach{
                            private->
                        importBatchConfig(private.config)
                    }


                    generalConfigs?.forEach{
                            general->
                        importBatchConfig(general.config)
                    }

                    val tot = convertBytesToReadableSize(up , down , totalData)

                    binding.tvLeftData.text = String.format("%.2f GB", tot)
                    binding.DataProgressBar.apply {
                        progressMax = totalData.toFloat() ?:100f
                        progress = up.toFloat() + down.toFloat()
                    }
                    mainStorage

                    if (tot<=0){
                        binding.fab.visibility = View.GONE
                        binding.btnRequestConfigs.visibility = View.GONE
                        showAlert("حجم", " حجم شما به اتمام رسیده است ")
                    }

                    if (name!=null)
                        storeData?.save("fullName",name)
                    if (phone!=null)
                        storeData?.save("phone",phone)
                    if (payments!=null)
                        storeData?.save("payment",payments.toString())
                    if (successResponse?.user?.status != null) {
                        storeData?.save("user_status",successResponse.user.status)

                    }

                } else {
                    // The API call failed (non-2xx HTTP response)
                    try {
                        // Parse the error body into a FailureResponse object
                        val gson = Gson()
                        val failureResponse: ConfigFailure = gson.fromJson(
                            response.errorBody()!!.string(),
                            ConfigFailure::class.java
                        )
                        if (failureResponse != null) {
                            showAlert(failureResponse.error,failureResponse.message)
                            binding.fab.visibility = View.GONE
                            binding.btnRequestConfigs.visibility = View.GONE
                            storeData?.save("user_status",response.code())
                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(call: Call<ConfigSuccess?>, t: Throwable) {
                //toastResult("لطفا صبر کنید")
                loading.show()
                binding.btnRequestConfigs.visibility = View.VISIBLE
                if (tryToGetInfoFromServer<5){
                    tryToGetInfoFromServer++
                    getNewConfigs()
                }else{
                    loading.dismiss()
                    toastResult("اینترنت مشکل دارد")
                }
            }
        })
    }

    private fun init(){

        api = RetrofitInstance.Instance().create<ApiService>(ApiService::class.java)

        storeData = storeStringPreference(this)

        loading = KProgressHUD.create(this@MainActivity)
            .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
            .setLabel("لطفا صبر کنید")
            .setCancellable(false)
            .setAnimationSpeed(2)
            .setDimAmount(0.5f)
    }
    private fun toastResult(responseS: String) {
        Toast.makeText(this@MainActivity, responseS, Toast.LENGTH_SHORT).show()
    }

    fun showAlert(title:String , message: String , cancellable:Boolean = false) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        .setMessage(message)
        .setPositiveButton("Yes") { dialog, _ ->
            getNewConfigs()
        }
        // Create the dialog
        val dialog = builder.create()

        // Prevent the dialog from being dismissed by back button or outside touch
        dialog.setCancelable(cancellable)
        dialog.setCanceledOnTouchOutside(false)

        dialog.show()
    }

    private fun setupViewModel() {
        mainViewModel.updateListAction.observe(this) { index ->
            if (index >= 0) {
                adapter.notifyItemChanged(index)
            } else {
                adapter.notifyDataSetChanged()
            }
        }
        mainViewModel.updateTestResultAction.observe(this) { setTestState(it) }
        mainViewModel.isRunning.observe(this) { isRunning ->
            adapter.isRunning = isRunning
            if (isRunning) {
                binding.fab.setImageResource(R.drawable.ic_stop_24dp)
                binding.fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_active))
                setTestState(getString(R.string.connection_connected))
                binding.layoutTest.isFocusable = true
            } else {
                binding.fab.setImageResource(R.drawable.ic_play_24dp)
                binding.fab.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_fab_inactive))
                setTestState(getString(R.string.connection_not_connected))
                binding.layoutTest.isFocusable = false
            }
        }
        mainViewModel.startListenBroadcast()
    }

    private fun copyAssets() {
        val extFolder = Utils.userAssetPath(this)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geo = arrayOf("geosite.dat", "geoip.dat")
                assets.list("")
                        ?.filter { geo.contains(it) }
                        ?.filter { !File(extFolder, it).exists() }
                        ?.forEach {
                            val target = File(extFolder, it)
                            assets.open(it).use { input ->
                                FileOutputStream(target).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            Log.i(ANG_PACKAGE, "Copied from apk assets folder to ${target.absolutePath}")
                        }
            } catch (e: Exception) {
                Log.e(ANG_PACKAGE, "asset copy failed", e)
            }
        }
    }

//    private fun migrateLegacy() {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val result = AngConfigManager.migrateLegacyConfig(this@MainActivity)
//            if (result != null) {
//                launch(Dispatchers.Main) {
//                    if (result) {
//                        toast(getString(R.string.migration_success))
//                        mainViewModel.reloadServerList()
//                    } else {
//                        toast(getString(R.string.migration_fail))
//                    }
//                }
//            }
//        }
//    }

    fun startV2Ray() {
        if (mainStorage?.decodeString(MmkvManager.KEY_SELECTED_SERVER).isNullOrEmpty()) {
            return
        }
        V2RayServiceManager.startV2Ray(this)
    }

    fun restartV2Ray() {
        if (mainViewModel.isRunning.value == true) {
            Utils.stopVService(this)
        }
        Observable.timer(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    startV2Ray()
                }
    }

    public override fun onResume() {
        super.onResume()
        mainViewModel.reloadServerList()
    }

    public override fun onPause() {
        super.onPause()
    }


    /**
     * import config from qrcode
     */
    fun importQRcode(forConfig: Boolean): Boolean {
//        try {
//            startActivityForResult(Intent("com.google.zxing.client.android.SCAN")
//                    .addCategory(Intent.CATEGORY_DEFAULT)
//                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), requestCode)
//        } catch (e: Exception) {
        RxPermissions(this)
                .request(Manifest.permission.CAMERA)
                .subscribe {
                    if (it)
                        if (forConfig)
                            scanQRCodeForConfig.launch(Intent(this, ScannerActivity::class.java))
                        else
                            scanQRCodeForUrlToCustomConfig.launch(Intent(this, ScannerActivity::class.java))
                    else
                        toast(R.string.toast_permission_denied)
                }
//        }
        return true
    }

    private val scanQRCodeForConfig = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            importBatchConfig(it.data?.getStringExtra("SCAN_RESULT"))
        }
    }

    private val scanQRCodeForUrlToCustomConfig = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            importConfigCustomUrl(it.data?.getStringExtra("SCAN_RESULT"))
        }
    }

    /**
     * import config from clipboard
     */
    fun importClipboard()
            : Boolean {
        try {
            val clipboard = Utils.getClipboard(this)
            importBatchConfig(clipboard)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun importBatchConfig(server: String?, subid: String? = "") {
        val subid2 = if(subid.isNullOrEmpty()){
            mainViewModel.subscriptionId
        }else{
            subid
        }
        val append = subid.isNullOrEmpty()

        var count = AngConfigManager.importBatchConfig(server, subid2, append)
        if (count <= 0) {
            count = AngConfigManager.importBatchConfig(Utils.decode(server!!), subid2, append)
        }
        if (count <= 0) {
            count = AngConfigManager.appendCustomConfigServer(server, subid2)
        }
        if (count > 0) {
            toast(R.string.toast_success)
            mainViewModel.reloadServerList()
        } else {
            toast(R.string.toast_failure)
        }
    }

    fun importConfigCustomClipboard()
            : Boolean {
        try {
            val configText = Utils.getClipboard(this)
            if (TextUtils.isEmpty(configText)) {
                toast(R.string.toast_none_data_clipboard)
                return false
            }
            importCustomizeConfig(configText)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * import config from local config file
     */
    fun importConfigCustomLocal(): Boolean {
        try {
            showFileChooser()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun importConfigCustomUrlClipboard()
            : Boolean {
        try {
            val url = Utils.getClipboard(this)
            if (TextUtils.isEmpty(url)) {
                toast(R.string.toast_none_data_clipboard)
                return false
            }
            return importConfigCustomUrl(url)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * import config from url
     */
    fun importConfigCustomUrl(url: String?): Boolean {
        try {
            if (!Utils.isValidUrl(url)) {
                toast(R.string.toast_invalid_url)
                return false
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val configText = try {
                    Utils.getUrlContentWithCustomUserAgent(url)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ""
                }
                launch(Dispatchers.Main) {
                    importCustomizeConfig(configText)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * import config from sub
     */
    fun importConfigViaSub()
            : Boolean {
        try {
            toast(R.string.title_sub_update)
            MmkvManager.decodeSubscriptions().forEach {
                if (TextUtils.isEmpty(it.first)
                        || TextUtils.isEmpty(it.second.remarks)
                        || TextUtils.isEmpty(it.second.url)
                ) {
                    return@forEach
                }
                if (!it.second.enabled) {
                    return@forEach
                }
                val url = Utils.idnToASCII(it.second.url)
                if (!Utils.isValidUrl(url)) {
                    return@forEach
                }
                Log.d(ANG_PACKAGE, url)
                lifecycleScope.launch(Dispatchers.IO) {
                    var configText = try {
                        Utils.getUrlContentWithCustomUserAgent(url)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ""
                    }
                    if(configText.isEmpty()) {
                        configText = try {
                            val httpPort = Utils.parseInt(settingsStorage?.decodeString(AppConfig.PREF_HTTP_PORT), AppConfig.PORT_HTTP.toInt())
                            Utils.getUrlContentWithCustomUserAgent(url, httpPort)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ""
                        }
                    }
                    if(configText.isEmpty()) {
                        launch(Dispatchers.Main) {
                            toast("\"" + it.second.remarks + "\" " + getString(R.string.toast_failure))
                        }
                        return@launch
                    }
                    launch(Dispatchers.Main) {
                        importBatchConfig(configText, it.first)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * show file chooser
     */
    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        try {
            chooseFileForCustomConfig.launch(Intent.createChooser(intent, getString(R.string.title_file_chooser)))
        } catch (ex: ActivityNotFoundException) {
            toast(R.string.toast_require_file_manager)
        }
    }

    private val chooseFileForCustomConfig = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val uri = it.data?.data
        if (it.resultCode == RESULT_OK && uri != null) {
            readContentFromUri(uri)
        }
    }

    /**
     * read content from uri
     */
    private fun readContentFromUri(uri: Uri) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        RxPermissions(this)
                .request(permission)
                .subscribe {
                    if (it) {
                        try {
                            contentResolver.openInputStream(uri).use { input ->
                                importCustomizeConfig(input?.bufferedReader()?.readText())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else
                        toast(R.string.toast_permission_denied)
                }
    }

    /**
     * import customize config
     */
    fun importCustomizeConfig(server: String?) {
        try {
            if (server == null || TextUtils.isEmpty(server)) {
                toast(R.string.toast_none_data)
                return
            }
            mainViewModel.appendCustomConfigServer(server)
            mainViewModel.reloadServerList()
            toast(R.string.toast_success)
            //adapter.notifyItemInserted(mainViewModel.serverList.lastIndex)
        } catch (e: Exception) {
            ToastCompat.makeText(this, "${getString(R.string.toast_malformed_josn)} ${e.cause?.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            return
        }
    }

    fun setTestState(content: String?) {
        binding.tvTestState.text = content
    }

//    val mConnection = object : ServiceConnection {
//        override fun onServiceDisconnected(name: ComponentName?) {
//        }
//
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            sendMsg(AppConfig.MSG_REGISTER_CLIENT, "")
//        }
//    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_BUTTON_B) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}
