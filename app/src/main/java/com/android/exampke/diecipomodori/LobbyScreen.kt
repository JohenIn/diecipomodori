package com.android.exampke.diecipomodori

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.android.exampke.diecipomodori.model.MyDb

@Composable
fun LobbyScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "TOMATOMATOMATO",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        Button(
            onClick = { navController.navigate("game") },
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(text = "Play Game")
        }
        val context = LocalContext.current
        val db = remember { MyDb.getDatabase(context) }
        val list by db.userDao().getAll().collectAsStateWithLifecycle(emptyList())
        val maxScore = list.maxOfOrNull { it.score ?: 0 } ?: 0

        Text(
            text =  if (maxScore == 0) "게임 좀 하세요" else "최고 점수: $maxScore",
            modifier = Modifier.align(Alignment.BottomCenter),
        )

    }
}