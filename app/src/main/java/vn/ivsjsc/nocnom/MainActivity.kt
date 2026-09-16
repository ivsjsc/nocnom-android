package vn.ivsjsc.nocnom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import vn.ivsjsc.nocnom.ui.NocnomApp
import vn.ivsjsc.nocnom.ui.theme.NocnomTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NocnomTheme {
                NocnomApp()
            }
        }
    }
}
