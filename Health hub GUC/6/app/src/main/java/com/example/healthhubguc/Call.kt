package com.example.healthhubguc

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.rtp.AudioCodec
import android.net.rtp.AudioGroup
import android.net.rtp.AudioStream
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class Call : AppCompatActivity() {

    private var m_AudioGroup: AudioGroup? = null
    private var m_AudioStream: AudioStream? = null
    private lateinit var srcIP: TextView
    private lateinit var srcPort: TextView
    private lateinit var connect: ImageButton
    private lateinit var disconnect: ImageButton

    private var permissionGranted = false
    private val REQUEST_CODE = 100

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        showDialog()

        // Request audio permissions
        checkPermissions()
        if (!permissionGranted) return

        // Initialize UI elements
        srcIP = findViewById(R.id.dynamicIpText)
        srcPort = findViewById(R.id.dynamicPortText)
        connect = findViewById(R.id.connectBtn)
        disconnect = findViewById(R.id.disconnectBtn)

        // Set initial button states
        disconnect.isEnabled = false

        // Display the local IP address
        val localIpAddress = getLocalIpAddress()
        if (localIpAddress != null) {
            srcIP.text = localIpAddress
        } else {
            Toast.makeText(this, "Unable to fetch local IP address.", Toast.LENGTH_SHORT).show()
        }

        try {
            val policy = android.os.StrictMode.ThreadPolicy.Builder().permitAll().build()
            android.os.StrictMode.setThreadPolicy(policy)

            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

            m_AudioGroup = AudioGroup()
            m_AudioGroup!!.mode = AudioGroup.MODE_NORMAL

            val localIp = InetAddress.getByName(localIpAddress)
            m_AudioStream = AudioStream(localIp)
            val localPort = m_AudioStream!!.localPort
            srcPort.text = localPort.toString()

            m_AudioStream!!.codec = AudioCodec.PCMU
            m_AudioStream!!.mode = AudioStream.MODE_NORMAL

            // Connect button action
            connect.setOnClickListener {
                val remoteAddress = findViewById<EditText>(R.id.destIpText).text.toString()
                val remotePortText = findViewById<EditText>(R.id.destPortText).text.toString()

                if (remoteAddress.isEmpty() || remotePortText.isEmpty() || remotePortText.toIntOrNull() == null) {
                    Toast.makeText(this, "Please enter valid destination IP and port.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                try {
                    val remotePort = remotePortText.toInt()
                    val remoteInetAddress = InetAddress.getByName(remoteAddress)

                    // Log the attempt to connect
                    Log.d("CallActivity", "Attempting to connect to remote IP: $remoteAddress, Port: $remotePort")

                    // Ensure AudioStream is initialized before calling associate or join
                    m_AudioStream?.apply {
                        associate(remoteInetAddress, remotePort)
                        join(m_AudioGroup)
                    }

                    connect.isEnabled = false
                    disconnect.isEnabled = true
                    Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()

                } catch (e: Exception) {
                    Log.e("CallError", "Error associating or joining audio stream: ${e.localizedMessage}")
                    Toast.makeText(this, "Failed to connect: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }

            // Disconnect button action
            disconnect.setOnClickListener {
                try {
                    if (m_AudioStream == null) {
                        Toast.makeText(this@Call, "Already disconnected.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    m_AudioStream?.apply {
                        join(null)  // Detach from the group
                        release()   // Release the stream
                    }
                    m_AudioGroup?.clear()
                    m_AudioGroup = null
                    connect.isEnabled = true
                    disconnect.isEnabled = false
                    Toast.makeText(this@Call, "Disconnected successfully.", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("CallError", "Error disconnecting audio stream: ${e.localizedMessage}")
                    Toast.makeText(this@Call, "Failed to disconnect: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }




        } catch (e: Exception) {
            Log.e("CallError", e.toString())
            e.printStackTrace()
        }
    }

    private fun checkPermissions() {
        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionGranted = true
        } else {
            Toast.makeText(this, "Audio permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            if (m_AudioStream == null) return
            m_AudioStream?.join(null)  // Detach from the group
        } catch (e: Exception) {
            Log.e("CallError", "Error detaching AudioStream in onPause: ${e.localizedMessage}")
        }
    }



    private fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in Collections.list(interfaces)) {
                val addresses = networkInterface.inetAddresses
                for (address in Collections.list(addresses)) {
                    if (!address.isLoopbackAddress && address is InetAddress) {
                        val ip = address.hostAddress
                        if (ip.indexOf(':') == -1) { // Exclude IPv6
                            return ip
                        }
                    }
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showDialog() {
        val alertDialogBuilder = MaterialAlertDialogBuilder(this@Call)
        alertDialogBuilder.setTitle("Help")
        alertDialogBuilder.setIcon(R.drawable.help)
        alertDialogBuilder.setMessage(
            """
            1. Both sides must grant the required permissions.
            2. Ensure both devices are on the same network.
            3. Swap your local IP and ports as displayed on the screen.
            4. Click the green button to connect.
            5. Click the red button to disconnect.
            """.trimIndent()
        )
        alertDialogBuilder.setNegativeButton("Dismiss") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        alertDialogBuilder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (m_AudioStream == null) return
            m_AudioStream?.apply {
                join(null)  // Detach from the AudioGroup
                release()   // Release resources
            }
            m_AudioStream = null
            m_AudioGroup?.clear()
            m_AudioGroup = null
        } catch (e: Exception) {
            Log.e("CallError", "Error during cleanup in onDestroy: ${e.localizedMessage}")
        }
    }


}
