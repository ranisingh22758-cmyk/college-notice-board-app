package com.example.collegenoticeboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.collegenoticeboard.ui.theme.CollegeNoticeBoardTheme
import com.google.firebase.firestore.FirebaseFirestore

data class Notice(
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val category: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CollegeNoticeBoardTheme {
                NoticeBoardScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeBoardScreen() {
    var notices by remember { mutableStateOf(listOf<Notice>()) }
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("notice").get()
            .addOnSuccessListener { result ->
                notices = result.documents.mapNotNull { doc ->
                    try {
                        Notice(
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            date = doc.getString("date") ?: "",
                            category = doc.getString("category") ?: ""
                        )
                    } catch (e: Exception) { null }
                }
                loading = false
            }
            .addOnFailureListener { e ->
                errorMsg = e.message ?: "Error loading"
                loading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("College Notice Board", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMsg.isNotEmpty() -> {
                    Text("Error: $errorMsg", modifier = Modifier.align(Alignment.Center).padding(16.dp))
                }
                notices.isEmpty() -> {
                    Text("No notices found in 'notice' collection", modifier = Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        items(notices) { notice ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    if(notice.category.isNotEmpty()){
                                        Text(text = notice.category.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = notice.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    if(notice.date.isNotEmpty()){
                                        Text(text = notice.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = notice.description, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}