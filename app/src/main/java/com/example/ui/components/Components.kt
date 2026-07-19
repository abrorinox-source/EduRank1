package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    title: String,
    subtitle: String,
    userInitials: String = "JD",
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = SlateDark
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Indigo, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "App Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark,
                        modifier = Modifier.testTag("app_bar_title")
                    )
                    Text(
                        text = subtitle.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateLight,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = SlateLight
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(IndigoLight, CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitials,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo
                    )
                }
            }
        },
        modifier = modifier.border(width = (0.5).dp, color = Color(0xFFE2E8F0))
    )
}

@Composable
fun StatusBadge(status: UserStatus) {
    val (bgColor, textColor, text) = when (status) {
        UserStatus.ACTIVE -> Triple(EmeraldLight, Emerald, "Active")
        UserStatus.INACTIVE -> Triple(Color(0xFFF1F5F9), SlateLight, "Inactive")
        UserStatus.PENDING -> Triple(IndigoLight, Indigo, "Pending")
    }
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun SpreadsheetTable(
    students: List<User>,
    groupNameMap: Map<String, String>,
    onStudentClick: (User) -> Unit,
    onQuickAdd: (User) -> Unit,
    onQuickSubtract: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Horizontal scrollable table container to support spreadsheet-feel on mobile screens
            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Column(modifier = Modifier.width(620.dp)) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFF8FAFC))
                            .border(width = (0.5).dp, color = Color(0xFFE2E8F0))
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "#",
                            modifier = Modifier.width(40.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Student Name",
                            modifier = Modifier
                                .width(180.dp)
                                .padding(start = 8.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                        Text(
                            text = "Group",
                            modifier = Modifier.width(110.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight,
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "Pts",
                            modifier = Modifier.width(80.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Status",
                            modifier = Modifier.width(90.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Quick Actions",
                            modifier = Modifier.width(120.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Table Rows
                    students.forEachIndexed { index, student ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onStudentClick(student) }
                                .background(if (index % 2 == 0) Color.White else Color(0xFFFAFAFA))
                                .border(width = (0.5).dp, color = Color(0xFFF1F5F9))
                                .padding(vertical = 8.dp)
                        ) {
                            // Rank Number
                            Text(
                                text = String.format("%02d", index + 1),
                                modifier = Modifier.width(40.dp),
                                fontSize = 12.sp,
                                color = SlateLight,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )

                            // Name & Email Column
                            Column(
                                modifier = Modifier
                                    .width(180.dp)
                                    .padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = student.fullName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = student.email,
                                    fontSize = 10.sp,
                                    color = SlateLight,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Group Column
                            Text(
                                text = groupNameMap[student.groupId] ?: "No Group",
                                modifier = Modifier.width(110.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Points Column
                            Text(
                                text = String.format("%,d", student.points),
                                modifier = Modifier.width(80.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (student.points > 1000) Emerald else SlateDark,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Monospace
                            )

                            // Status Column
                            Box(
                                modifier = Modifier.width(90.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                StatusBadge(student.status)
                            }

                            // Action Buttons Column
                            Row(
                                modifier = Modifier.width(120.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(EmeraldLight, RoundedCornerShape(8.dp))
                                        .clickable { onQuickAdd(student) }
                                        .testTag("quick_add_points_${student.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Points",
                                        tint = Emerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(RoseRedLight, RoundedCornerShape(8.dp))
                                        .clickable { onQuickSubtract(student) }
                                        .testTag("quick_sub_points_${student.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Subtract Points",
                                        tint = RoseRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Table Footer (Count of students)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Showing ${students.size} students",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateLight,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun RankCard(
    rank: Int,
    studentName: String,
    points: Int,
    groupName: String,
    isCurrentUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardColor = if (isCurrentUser) IndigoLight else Color.White
    val borderStroke = if (isCurrentUser) BorderStroke(1.5.dp, Indigo) else BorderStroke(0.5.dp, Color(0xFFE2E8F0))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 3.dp else 1.dp),
        border = borderStroke,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            // Rank Badge
            val rankBg = when (rank) {
                1 -> Color(0xFFFFD700) // Gold
                2 -> Color(0xFFC0C0C0) // Silver
                3 -> Color(0xFFCD7F32) // Bronze
                else -> Color(0xFFF1F5F9)
            }
            val rankText = when (rank) {
                1, 2, 3 -> Color.White
                else -> SlateLight
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(rankBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = rankText
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Student Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Text(
                    text = groupName,
                    fontSize = 11.sp,
                    color = SlateLight,
                    fontStyle = FontStyle.Italic
                )
            }

            // Points Box
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%,d", points),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rank == 1) Color(0xFFD4AF37) else Indigo,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "pts",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateLight
                )
            }
        }
    }
}

@Composable
fun TransactionRow(
    tx: PointTransaction,
    senderName: String,
    receiverName: String,
    isReceived: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color(0xFFF1F5F9)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            val (icon, color, bg) = when (tx.type) {
                TransactionType.MANUAL_ADD -> Triple(Icons.Default.TrendingUp, Emerald, EmeraldLight)
                TransactionType.MANUAL_SUBTRACT -> Triple(Icons.Default.TrendingDown, RoseRed, RoseRedLight)
                TransactionType.TRANSFER -> {
                    if (isReceived) {
                        Triple(Icons.Default.ArrowDownward, Emerald, EmeraldLight)
                    } else {
                        Triple(Icons.Default.ArrowUpward, RoseRed, RoseRedLight)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val title = when (tx.type) {
                    TransactionType.MANUAL_ADD -> "Added Points"
                    TransactionType.MANUAL_SUBTRACT -> "Subtracted Points"
                    TransactionType.TRANSFER -> {
                        if (isReceived) "Received from $senderName" else "Transferred to $receiverName"
                    }
                }
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                if (tx.note.isNotBlank()) {
                    Text(
                        text = "\"${tx.note}\"",
                        fontSize = 11.sp,
                        color = SlateLight,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Timestamp and amount
            Column(horizontalAlignment = Alignment.End) {
                val prefix = when (tx.type) {
                    TransactionType.MANUAL_ADD -> "+"
                    TransactionType.MANUAL_SUBTRACT -> "-"
                    TransactionType.TRANSFER -> if (isReceived) "+" else "-"
                }
                val amountColor = when (tx.type) {
                    TransactionType.MANUAL_ADD -> Emerald
                    TransactionType.MANUAL_SUBTRACT -> RoseRed
                    TransactionType.TRANSFER -> if (isReceived) Emerald else RoseRed
                }

                Text(
                    text = "$prefix${tx.amount}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    fontFamily = FontFamily.Monospace
                )
                
                // Formatted simple date (just hours/mins or simple MM/dd)
                val formatTime = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                val timeStr = formatTime.format(java.util.Date(tx.createdAt))
                Text(
                    text = timeStr,
                    fontSize = 9.sp,
                    color = SlateLight
                )
            }
        }
    }
}

// State Views
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(color = Indigo)
            Text("Loading data...", fontSize = 13.sp, color = SlateLight, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = RoseRed,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = message,
                fontSize = 14.sp,
                color = SlateDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            onRetry?.let {
                Button(
                    onClick = it,
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo)
                ) {
                    Text("Retry", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = SlateLight,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = message,
                fontSize = 13.sp,
                color = SlateLight,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Reusable Confirmation Dialog
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SlateDark) },
        text = { Text(text = message, fontSize = 14.sp, color = SlateLight) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                modifier = Modifier.testTag("dialog_confirm_button")
            ) {
                Text(confirmText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dialog_cancel_button")) {
                Text(cancelText, color = SlateLight)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// Point Adjustment Dialog
@Composable
fun PointAdjustmentDialog(
    studentName: String,
    onConfirm: (amount: Int, type: TransactionType, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var pointsText by remember { mutableStateOf("") }
    var isAddition by remember { mutableStateOf(true) }
    var noteText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adjust Points: $studentName",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = SlateDark
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector Row (Add / Subtract)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    val addColor = if (isAddition) Emerald else Color.Transparent
                    val addText = if (isAddition) Color.White else SlateLight
                    val subColor = if (!isAddition) RoseRed else Color.Transparent
                    val subText = if (!isAddition) Color.White else SlateLight

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(addColor, RoundedCornerShape(10.dp))
                            .clickable { isAddition = true }
                            .testTag("adjustment_add_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add Points", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = addText)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(subColor, RoundedCornerShape(10.dp))
                            .clickable { isAddition = false }
                            .testTag("adjustment_sub_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Subtract Points", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = subText)
                    }
                }

                // Points Input Field
                OutlinedTextField(
                    value = pointsText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() }) {
                            pointsText = it
                            errorMsg = null
                        }
                    },
                    label = { Text("Points amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo,
                        focusedLabelColor = Indigo
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("adjustment_points_input")
                )

                // Note Input Field
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Reason") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo,
                        focusedLabelColor = Indigo
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("adjustment_note_input")
                )

                errorMsg?.let {
                    Text(it, color = RoseRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pts = pointsText.toIntOrNull()
                    if (pts == null || pts <= 0) {
                        errorMsg = "Please enter a valid positive points amount"
                    } else if (noteText.trim().isBlank()) {
                        errorMsg = "Please provide a reason or note"
                    } else {
                        val type = if (isAddition) TransactionType.MANUAL_ADD else TransactionType.MANUAL_SUBTRACT
                        onConfirm(pts, type, noteText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo),
                modifier = Modifier.testTag("adjustment_dialog_confirm")
            ) {
                Text("Apply", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("adjustment_dialog_dismiss")) {
                Text("Cancel", color = SlateLight)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    )
}
