package com.tukangin.ui.modules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tukangin.modules.tukang.TukangViewModel

@Composable
fun TukangScreen(
    modifier: Modifier = Modifier,
    viewModel: TukangViewModel = hiltViewModel()
) {
    val tukangList by viewModel.tukangList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Daftar Tukang",
            style = MaterialTheme.typography.titleLarge
        )

        when {
            isLoading -> {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "Terjadi kesalahan",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                    items(tukangList) { tukang ->
                        Text(text = "${tukang.name} • ${tukang.serviceCategory}")
                    }
                }
            }
        }
    }
}

