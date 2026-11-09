
package com.yawmi.app

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.glance.appwidget.updateAll
import com.yawmi.app.data.Task
import com.yawmi.app.data.YawmiDatabase
import com.yawmi.app.widget.YawmiWidget
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Ink = Color(0xFF20332F)
private val Muted = Color(0xFF82918B)
private val Teal = Color(0xFF167D75)
private val Canvas = Color(0xFFF3F7F5)
private val Mint = Color(0xFFE1F0EA)
private val White = Color.White

private fun weekStart(date: LocalDate): String {
    val daysSinceFriday = (date.dayOfWeek.value - 5 + 7) % 7
    return date.minusDays(daysSinceFriday.toLong()).toString()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = YawmiDatabase.get(applicationContext).taskDao()
        val today = LocalDate.now()
        val dayKey = today.toString()
        val weekKey = weekStart(today)

        setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl
            ) {
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = Teal,
                        background = Canvas,
                        surface = White
                    )
                ) {
                    val weekly by dao.weeklyTasks(weekKey)
                        .collectAsStateWithLifecycle(initialValue = emptyList())

                    val daily by dao.dailyTasks(dayKey)
                        .collectAsStateWithLifecycle(initialValue = emptyList())

                    val scope = rememberCoroutineScope()

                    var selected by remember { mutableStateOf(0) }
                    var showAdd by remember { mutableStateOf(false) }
                    var showAbout by remember { mutableStateOf(false) }

                    val list = if (selected == 0) daily else weekly
                    val completed = list.count { it.isDone }
                    val progress =
                        if (list.isEmpty()) 0f
                        else completed.toFloat() / list.size

                    LaunchedEffect(dayKey, weekKey) {
                        YawmiWidget().updateAll(applicationContext)
                    }

                    Surface(
                        Modifier.fillMaxSize(),
                        color = Canvas
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                        ) {
                            Spacer(Modifier.height(26.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "يومي",
                                        fontSize = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Ink
                                    )

                                    Text(
                                        today.format(
                                            DateTimeFormatter.ofPattern(
                                                "EEEE، d MMMM",
                                                Locale("ar")
                                            )
                                        ),
                                        color = Muted,
                                        fontSize = 13.sp
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { showAbout = true }
                                    ) {
                                        Icon(
                                            Icons.Rounded.Info,
                                            contentDescription = "حول التطبيق",
                                            tint = Teal
                                        )
                                    }

                                    Box(
                                        Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Mint),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Y",
                                            color = Teal,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 21.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(22.dp))

                            Card(
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF176D67)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(19.dp)) {
                                    Text(
                                        if (selected == 0)
                                            "خطوة صغيرة كل يوم"
                                        else
                                            "أهداف أسبوعك",
                                        color = Color.White.copy(alpha = .75f),
                                        fontSize = 13.sp
                                    )

                                    Spacer(Modifier.height(5.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            if (list.isEmpty())
                                                "ابدأ بإضافة أول مهمة"
                                            else
                                                "أنجزت $completed من ${list.size}",
                                            color = White,
                                            fontSize = 21.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Text(
                                            "${(progress * 100).toInt()}%",
                                            color = White,
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(CircleShape),
                                        color = Color(0xFFBCE6D9),
                                        trackColor = White.copy(alpha = .2f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFFE8EFEC),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(4.dp)
                            ) {
                                TabPill(
                                    "مهام اليوم",
                                    selected == 0,
                                    Modifier.weight(1f)
                                ) {
                                    selected = 0
                                }

                                TabPill(
                                    "خطة الأسبوع",
                                    selected == 1,
                                    Modifier.weight(1f)
                                ) {
                                    selected = 1
                                }
                            }

                            Spacer(Modifier.height(17.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (selected == 0)
                                            "مهام اليوم"
                                        else
                                            "أهداف الأسبوع",
                                        color = Ink,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        if (selected == 0)
                                            "اكتب ما تريد إنجازه اليوم"
                                        else
                                            "خطط لأسبوعك كل يوم جمعة",
                                        color = Muted,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    "${list.size} مهام",
                                    color = Muted,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            if (list.isEmpty()) {
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .background(Mint),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (selected == 0)
                                                    Icons.Rounded.Today
                                                else
                                                    Icons.Rounded.EventNote,
                                                null,
                                                tint = Teal,
                                                modifier = Modifier.size(31.dp)
                                            )
                                        }

                                        Spacer(Modifier.height(13.dp))

                                        Text(
                                            if (selected == 0)
                                                "يوم جديد، فرصة جديدة"
                                            else
                                                "أسبوعك يبدأ بخطة واضحة",
                                            color = Ink,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )

                                        Text(
                                            if (selected == 0)
                                                "أضف أول مهمة وابدأ"
                                            else
                                                "اكتب أهداف الأسبوع التي تريد إنجازها",
                                            color = Muted,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(9.dp),
                                    contentPadding = PaddingValues(bottom = 12.dp)
                                ) {
                                    items(list, key = { it.id }) { task ->
                                        TaskCard(
                                            task,
                                            onToggle = {
                                                scope.launch {
                                                    dao.update(
                                                        task.copy(
                                                            isDone = !task.isDone
                                                        )
                                                    )
                                                    YawmiWidget().updateAll(
                                                        applicationContext
                                                    )
                                                }
                                            },
                                            onDelete = {
                                                scope.launch {
                                                    dao.delete(task.id)
                                                    YawmiWidget().updateAll(
                                                        applicationContext
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { showAdd = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                shape = RoundedCornerShape(17.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Teal
                                )
                            ) {
                                Icon(Icons.Rounded.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (selected == 0)
                                        "إضافة مهمة اليوم"
                                    else
                                        "إضافة هدف للأسبوع",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(14.dp))
                        }
                    }

                    if (showAdd) {
                        AddDialog(
                            selected == 0,
                            onClose = { showAdd = false }
                        ) { title ->
                            scope.launch {
                                dao.insert(
                                    Task(
                                        title = title.trim(),
                                        type = if (selected == 0)
                                            "DAILY"
                                        else
                                            "WEEKLY",
                                        weekStart = weekKey,
                                        dayKey = if (selected == 0)
                                            dayKey
                                        else
                                            ""
                                    )
                                )

                                YawmiWidget().updateAll(applicationContext)
                            }

                            showAdd = false
                        }
                    }

                    if (showAbout) {
                        AboutDialog(
                            onClose = { showAbout = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    text: String,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        color = if (active) White else Color.Transparent,
        shadowElevation = if (active) 1.dp else 0.dp
    ) {
        Box(
            Modifier.padding(vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = if (active) Teal else Muted,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 12.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                task.isDone,
                { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Teal,
                    uncheckedColor = Color(0xFFBAC6C1)
                )
            )

            Text(
                task.title,
                Modifier
                    .weight(1f)
                    .padding(horizontal = 7.dp),
                color = if (task.isDone) Muted else Ink,
                fontSize = 14.sp,
                textDecoration = if (task.isDone)
                    TextDecoration.LineThrough
                else
                    TextDecoration.None,
                fontWeight = if (task.isDone)
                    FontWeight.Normal
                else
                    FontWeight.Medium
            )

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    "حذف",
                    tint = Color(0xFFB7C1BD)
                )
            }
        }
    }
}

@Composable
private fun AddDialog(
    isDaily: Boolean,
    onClose: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                if (isDaily) "مهمة اليوم" else "هدف جديد للأسبوع",
                color = Ink,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        if (isDaily)
                            "ماذا تريد أن تنجز اليوم؟"
                        else
                            "ما الذي تريد إنجازه هذا الأسبوع؟"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) onSave(title)
                },
                enabled = title.isNotBlank()
            ) {
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("إلغاء", color = Muted)
            }
        },
        containerColor = White,
        shape = RoundedCornerShape(23.dp)
    )
}

@Composable
private fun AboutDialog(onClose: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onClose,
        containerColor = White,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(65.dp)
                        .clip(CircleShape)
                        .background(Mint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Y",
                        color = Teal,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "يومي | YAWMI",
                    color = Ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "الإصدار 1.0.0",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "صُمّم هذا التطبيق ليكون عونًا لك على تنظيم يومك، وتحقيق أهدافك، واستثمار وقتك فيما ينفعك.",
                    color = Ink,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "نسأل الله أن يجعله علمًا نافعًا وعملًا صالحًا وصدقةً جارية، وأن ينفع به كل من استخدمه.",
                    color = Teal,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(18.dp))

              

                Text(
                    "© 2026 Ahmed Shaban",
                    color = Muted,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/201501994410")
                    )
                    context.startActivity(intent)
                }
            ) {
                Text(
                    "تواصل عبر واتساب",
                    color = Teal,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("إغلاق", color = Muted)
            }
        }
    )
}
