package com.yawmi.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.unit.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.yawmi.app.data.YawmiDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TaskIdKey = ActionParameters.Key<Long>("task_id")

private fun weekStart(date: LocalDate): String {
    val daysSinceFriday = (date.dayOfWeek.value - 5 + 7) % 7
    return date.minusDays(daysSinceFriday.toLong()).toString()
}

class YawmiWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = LocalDate.now()
        val dao = YawmiDatabase.get(context).taskDao()

        val daily = withContext(Dispatchers.IO) {
            dao.dailyTasksNow(today.toString())
        }

        val weekly = withContext(Dispatchers.IO) {
            dao.weeklyTasksNow(weekStart(today))
        }

        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(Color(0xFFF5F8F6)))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "يومي",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF176D67)),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    today.format(DateTimeFormatter.ofPattern("EEEE، d MMMM", Locale("ar"))),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF75857E)),
                        fontSize = 11.sp
                    )
                )

                Spacer(GlanceModifier.height(10.dp))

                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        "مهام اليوم",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF20332F)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        "${daily.count { it.isDone }}/${daily.size}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF176D67)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Spacer(GlanceModifier.height(5.dp))

                if (daily.isEmpty()) {
                    Text(
                        "لا توجد مهام اليوم. أضف أول مهمة!",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF75857E)),
                            fontSize = 12.sp
                        )
                    )
                } else {
                    Column {
                        daily.take(5).forEach { task ->
                            Row(
                                modifier = GlanceModifier.fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable(
                                        actionRunCallback<ToggleTaskAction>(
                                            actionParametersOf(TaskIdKey to task.id)
                                        )
                                    )
                            ) {
                                Text(
                                    if (task.isDone) "☑" else "□",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF167D75)),
                                        fontSize = 17.sp
                                    )
                                )
                                Text(
                                    task.title,
                                    modifier = GlanceModifier.padding(start = 7.dp),
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = ColorProvider(
                                            if (task.isDone) Color(0xFF89968F)
                                            else Color(0xFF20332F)
                                        ),
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        if (daily.size > 5) {
                            Text(
                                "+ ${daily.size - 5} مهام أخرى",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF75857E)),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(GlanceModifier.height(8.dp))

                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        "خطة الأسبوع",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF20332F)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        "${weekly.count { it.isDone }}/${weekly.size}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF176D67)),
                            fontSize = 11.sp
                        )
                    )
                }

                Text(
                    if (weekly.isEmpty()) "أضف أهداف أسبوعك يوم الجمعة"
                    else weekly.take(2).joinToString("  •  ") { it.title },
                    maxLines = 2,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF75857E)),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

class YawmiWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YawmiWidget()
}

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val id = parameters[TaskIdKey] ?: return
        val dao = YawmiDatabase.get(context).taskDao()
        val task = dao.byId(id) ?: return
        dao.update(task.copy(isDone = !task.isDone))
        YawmiWidget().updateAll(context)
    }
}
