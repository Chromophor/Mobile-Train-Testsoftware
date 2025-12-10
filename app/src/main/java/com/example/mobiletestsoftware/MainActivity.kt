package com.example.mobiletestsoftware

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.mobiletestsoftware.ui.theme.MobileTestsoftwareTheme

//my imports
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket

class MainActivity : ComponentActivity() {


    // UDP Broadcast Port (zu den Pis)
    private val UDP_PORT = 5005

    // TCP Port, auf dem die Android App auf ACK wartet
    private val TCP_PORT = 6000

    // eigene IP Adresse
    //private val IP_SELF = InetAddress.getLocalHost().hostAddress

    private lateinit var statusView: TextView
    private lateinit var btnSend: Button
    private lateinit var btnSend1: Button
    private lateinit var btnInitialize: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusView = findViewById(R.id.statusView)
        btnSend = findViewById(R.id.btnSend)
        btnSend1 = findViewById(R.id.btnSend1)
        btnInitialize = findViewById(R.id.btnInitialize)

        // TCP Listener starten
        startTcpListener()

        btnInitialize.setOnClickListener {
            sendUdpBroadcast("Mobile Testapplication")
        }

        // Button zum Senden des Befehls "Schalte W05" (Weiche am Ablaufberg)
        btnSend.setOnClickListener {
            sendUdpBroadcast("1 5 2 1 8 W05")
        }
    }

    private fun sendUdpBroadcast(message: String) {
        Thread {
            try {
                val socket = DatagramSocket()
                socket.broadcast = true

                val data = message.toByteArray()
                val broadcastAddress = InetAddress.getByName("255.255.255.255")

                val packet = DatagramPacket(data, data.size, broadcastAddress, UDP_PORT)
                socket.send(packet)
                socket.close()

                runOnUiThread {
                    statusView.text = "UDP gesendet: $message"
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    statusView.text = "Fehler beim UDP-Senden: ${e.message}"
                }
            }
        }.start()
    }

    private fun startTcpListener() {
        Thread {
            try {
                val serverSocket = ServerSocket(TCP_PORT)
                runOnUiThread { statusView.text = "TCP Listener aktiv (Port $TCP_PORT)" }

                while (true) {
                    val client = serverSocket.accept()
                    val input = client.getInputStream().bufferedReader().readLine()

                    runOnUiThread {
                        statusView.text = "ACK erhalten: $input"
                    }

                    client.close()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    statusView.text = "TCP Fehler: ${e.message}"
                }
            }
        }.start()
    }

}