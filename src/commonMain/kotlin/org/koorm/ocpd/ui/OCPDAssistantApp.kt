@file:OptIn(ExperimentalMaterial3Api::class)

package org.koorm.ocpd.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koorm.ocpd.core.OCPDAssistantManager
import org.koorm.ocpd.models.*

@Composable
fun OCPDAssistantApp() {
    val assistant = remember { OCPDAssistantManager() }
    var selectedTab by remember { mutableStateOf(0) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        "🧠 OCPD Assistant",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )

            // Tab Navigation
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Tasks") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Schedule") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Insights") }
                )
            }

            // Content
            when (selectedTab) {
                0 -> TasksScreen(assistant)
                1 -> ScheduleScreen(assistant)
                2 -> InsightsScreen(assistant)
            }
        }
    }
}

@Composable
fun TasksScreen(assistant: OCPDAssistantManager) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var tasks by remember { mutableStateOf(assistant.getAllTasks()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Add Task Button
        Button(
            onClick = { showAddTaskDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Add New Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks List
        LazyColumn {
            items(tasks) { task ->
                TaskCard(
                    task = task,
                    onComplete = { isGoodEnough ->
                        assistant.markTaskCompleted(task.id, isGoodEnough)
                        tasks = assistant.getAllTasks()
                    },
                    onStuckMode = {
                        assistant.enableStuckMode(task.id)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, description, priority, category ->
                assistant.createTask(title, description, priority, category)
                tasks = assistant.getAllTasks()
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onComplete: (Boolean) -> Unit,
    onStuckMode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                PriorityChip(priority = task.priority)
            }

            if (task.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Estimated: ${task.estimatedDuration} minutes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (task.subtasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Subtasks:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                task.subtasks.forEach { subtask ->
                    Text(
                        text = "• ${subtask.title}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onComplete(false) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Complete")
                }

                OutlinedButton(
                    onClick = { onComplete(true) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Good Enough")
                }

                OutlinedButton(
                    onClick = onStuckMode,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Stuck?")
                }
            }
        }
    }
}

@Composable
fun PriorityChip(priority: Priority) {
    val (color, text) = when (priority) {
        Priority.URGENT -> MaterialTheme.colorScheme.error to "URGENT"
        Priority.HIGH -> MaterialTheme.colorScheme.primary to "HIGH"
        Priority.MEDIUM -> MaterialTheme.colorScheme.secondary to "MEDIUM"
        Priority.LOW -> MaterialTheme.colorScheme.outline to "LOW"
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, String, Priority, TaskCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var category by remember { mutableStateOf(TaskCategory.PERSONAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title, description, priority, category)
                    }
                }
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ScheduleScreen(assistant: OCPDAssistantManager) {
    val schedule = remember { assistant.getTodaySchedule() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Today's Schedule",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(schedule.timeBlocks) { timeBlock ->
                TimeBlockCard(timeBlock = timeBlock)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TimeBlockCard(timeBlock: TimeBlock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = timeBlock.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${timeBlock.startTime} - ${timeBlock.endTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Type: ${timeBlock.type}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InsightsScreen(assistant: OCPDAssistantManager) {
    val weeklyReport = remember { assistant.generateWeeklyInsights() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Behavioral Insights",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Weekly Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Completion Rate: ${(weeklyReport.completionRate * 100).toInt()}%")
                Text("Average Mood: ${weeklyReport.averageMood.toInt()}/10")
                Text("Average Energy: ${weeklyReport.averageEnergy.toInt()}/10")

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Key Insights:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                weeklyReport.keyInsights.forEach { insight ->
                    Text("• $insight", modifier = Modifier.padding(start = 8.dp))
                }

                if (weeklyReport.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Recommendations:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    weeklyReport.recommendations.forEach { recommendation ->
                        Text("• $recommendation", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}
