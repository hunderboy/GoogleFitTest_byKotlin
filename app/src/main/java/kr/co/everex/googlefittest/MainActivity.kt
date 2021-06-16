package kr.co.everex.googlefittest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import kr.co.everex.googlefittest.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity()   {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity : "

    // 앱 권한 허용 코드
    val PERMISSION_REQUEST_CODE = 1000
    val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1

    // 구글 피트니스 옵션
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view) // 뷰 바인딩 적용 완료

        binding.button.setOnClickListener(){
            acceptUser()
        }
    }
    private fun acceptUser() {

        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)


        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                account,
                fitnessOptions
            )
        } else {
            accessGoogleFit()
        }

    }


    /**
     * 구글 핏에 접근
     */
    private fun accessGoogleFit() {
        val end = LocalDateTime.now()
        val start = end.minusYears(1)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        Fitness.getHistoryClient(this, account)
            .readData(readRequest)
            .addOnSuccessListener { response ->
                // Use response data here
                Log.e(TAG, "OnSuccess()")
            }
            .addOnFailureListener { e -> Log.d(TAG, "OnFailure()", e) }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> accessGoogleFit()
                else -> {
                    // Result wasn't from Google Fit
                }
            }
            else -> {
                // Permission not granted
            }
        }
    }



    public override fun onResume() {
        super.onResume()  // Always call the superclass method first
        /**
         * 앱 권한 체크
         */
        if (checkPermission()) { // 이미 앱권한 모두 얻은 경우
            Toast.makeText(this, "권한 설정 완료", Toast.LENGTH_SHORT).show()
        } else {    // 앱권한 얻어야 함
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
//                    Manifest.permission.ACTIVITY_RECOGNITION
                    Manifest.permission.ACTIVITY_RECOGNITION
                ),
                PERMISSION_REQUEST_CODE
            )
        }

    }

    /**
     * 앱 권한 체크 함수
     */
    private fun checkPermission(): Boolean {
        val result1 = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACTIVITY_RECOGNITION
        )

        return result1 == 0
    }

    /**
     * 권한 요청 결과
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) { PERMISSION_REQUEST_CODE -> {

            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // DoNothing
            } else {
                Toast.makeText(this, "권한을 설정해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        }
    }

}