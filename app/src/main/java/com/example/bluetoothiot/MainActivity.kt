package com.example.bluetoothiot

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bluetoothiot.ui.theme.BluetoothiotTheme

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private lateinit var enableBtLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableBtLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth Not Enabled", Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.BLUETOOTH_CONNECT] == true) {
                Toast.makeText(this, "Bluetooth Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        setContent {
            BluetoothiotTheme {
                MainScreen(bluetoothAdapter, enableBtLauncher, requestPermissionLauncher)
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MainScreen(
    bluetoothAdapter: BluetoothAdapter?,
    enableBtLauncher: ActivityResultLauncher<Intent>,
    requestPermissionLauncher: ActivityResultLauncher<Array<String>>
) {
    val context = LocalContext.current
    var pairedDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    var discoveredDevices by remember { mutableStateOf(emptyList<BluetoothDevice>()) }
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // Check and request Bluetooth permissions
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    // Check if Bluetooth is enabled
    if (bluetoothAdapter?.isEnabled == false) {
        LaunchedEffect(Unit) {
            enableBtLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    val scanner = bluetoothAdapter?.bluetoothLeScanner
    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null && device !in discoveredDevices) {
                discoveredDevices = discoveredDevices + device
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text("Ayusynk", modifier = Modifier.padding(top = 16.dp))

        Button(onClick = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED && bluetoothAdapter?.isEnabled == true
            ) {
                pairedDevices = bluetoothAdapter.bondedDevices?.toList() ?: emptyList()
                discoveredDevices = emptyList()
                scanner?.startScan(scanCallback)
                Handler(Looper.getMainLooper()).postDelayed({ scanner?.stopScan(scanCallback) }, 5000)
            } else {
                Toast.makeText(
                    context,
                    "Bluetooth permissions not granted or Bluetooth not enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }) {
            Text("Scan for Devices")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pairedDevices.isNotEmpty() || discoveredDevices.isNotEmpty()) {
            Text("Devices:")
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(pairedDevices + discoveredDevices) { device ->
                    val isActive = discoveredDevices.contains(device)
                    DeviceItem(device, selectedDevice, isActive) {
                        selectedDevice = it
                        showDialog = true
                    }
                }
            }
        } else {
            Text("No Devices Found", modifier = Modifier.padding(top = 16.dp))
        }

        // Alert Dialog for Confirmation
        if (showDialog && selectedDevice != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Pair Device") },
                text = { Text("Do you want to pair with ${selectedDevice?.name ?: "Unknown"}?") },
                confirmButton = {
                    Button(onClick = {
                        selectedDevice?.let { device ->
                            // Redirect to DeviceActivity immediately when Pair is clicked
                            val intent = Intent(context, DeviceActivity::class.java).apply {
                                putExtra("DEVICE_ADDRESS", device.address)
                                putExtra("DEVICE_NAME", device.name)
                            }
                            context.startActivity(intent)

                            // Still attempt to pair in the background
                            try {
                                val method = device.javaClass.getMethod("createBond")
                                method.invoke(device)
                            } catch (e: Exception) {
                                // Pairing errors will be handled in DeviceActivity if needed
                            }
                        }
                        showDialog = false
                    }) {
                        Text("Pair")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DeviceItem(
    device: BluetoothDevice,
    selectedDevice: BluetoothDevice?,
    isActive: Boolean,
    onSelect: (BluetoothDevice) -> Unit
) {
    val context = LocalContext.current
    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.BLUETOOTH_CONNECT
    ) == PackageManager.PERMISSION_GRANTED

    val isSelected = device == selectedDevice

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(if (isActive) Color.Green else if (isSelected) Color.LightGray else Color.Transparent)
            .clickable { onSelect(device) }
            .padding(8.dp)
    ) {
        Text(text = "Name: ${if (hasPermission) device.name ?: "Unknown" else "Permission Required"}")
        Text(text = "Address: ${if (hasPermission) device.address else "Permission Required"}")
    }
}