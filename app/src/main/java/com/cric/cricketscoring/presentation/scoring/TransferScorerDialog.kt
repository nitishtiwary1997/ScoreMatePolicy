package com.cric.cricketscoring.presentation.scoring

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cric.cricketscoring.ui.theme.EmeraldPrimary
import com.cric.cricketscoring.ui.theme.LocalAppColors

@Composable
fun TransferScorerDialog(
    onDismiss: () -> Unit,
    onTransfer: (mobileNumber: String, role: String) -> Unit
) {
    val c = LocalAppColors.current
    var mobileNumber by remember { mutableStateOf("+91") }
    var selectedRole by remember { mutableStateOf("Editor") } // Default to Editor

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        titleContentColor = c.textPrimary,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        title = {
            Column {
                Text(
                    text = "Transfer Scoring Rights",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Temporarily assign editing or viewing rights",
                    fontSize = 11.sp,
                    color = c.textSecondary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Mobile Number input
                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Scorer's Mobile Number", color = c.textSecondary) },
                    placeholder = { Text("+91 98765 43210", color = c.textTertiary) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = c.textPrimary,
                        unfocusedTextColor = c.textPrimary,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = c.outline,
                        focusedLabelColor = EmeraldPrimary,
                        cursorColor = EmeraldPrimary
                    )
                )

                // Role selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Permission Type",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Editor", "Viewer").forEach { role ->
                            val selected = selectedRole == role
                            Surface(
                                selected = selected,
                                onClick = { selectedRole = role },
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) EmeraldPrimary else c.surface2,
                                border = BorderStroke(1.dp, if (selected) EmeraldPrimary else c.outline),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = role,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selected) Color.Black else c.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mobileNumber.isNotBlank()) {
                        onTransfer(mobileNumber.trim(), selectedRole)
                        onDismiss()
                    }
                },
                enabled = mobileNumber.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black,
                    disabledContainerColor = c.surface2,
                    disabledContentColor = c.textTertiary
                )
            ) {
                Text("Transfer Permission", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = c.textSecondary)
            }
        }
    )
}
