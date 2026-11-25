package com.example.progr3ssapp.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.progr3ssapp.model.AuthProfile
import com.example.progr3ssapp.network.RetrofitClient
import com.example.progr3ssapp.ui.theme.AppColors
import com.example.progr3ssapp.utils.ProgressUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

// ==========================================
// KÖZÖS FELÜLETI ELEMEK
// ==========================================

val CardShape = RoundedCornerShape(16.dp)
val FieldShape = RoundedCornerShape(12.dp)

// Felső sáv: vissza gomb, cím, opcionális akciók
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            RoundIconButton(text = "←", onClick = onBack)
            Spacer(Modifier.width(10.dp))
        } else {
            Spacer(Modifier.width(6.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }
        actions()
    }
}

// Kerek ikon gomb (szöveges szimbólummal, külön ikonkönyvtár nélkül)
@Composable
fun RoundIconButton(
    text: String,
    onClick: () -> Unit,
    background: Color = AppColors.Surface,
    contentColor: Color = AppColors.TextPrimary,
    size: Dp = 38.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// Kártya a sötét felülethez
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    val base = modifier
        .fillMaxWidth()
        .clip(CardShape)
        .background(AppColors.Surface)
    Box(
        modifier = (if (onClick != null) base.clickable(onClick = onClick) else base)
            .padding(padding)
    ) {
        content()
    }
}

// Címke a mezők fölött (a terv szerint kis szürke felirat)
@Composable
fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = AppColors.TextSecondary,
        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
    )
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(modifier) {
        if (label != null) FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it, color = AppColors.TextSecondary) } },
            isError = isError,
            supportingText = supportingText?.let {
                { Text(it, color = if (isError) AppColors.Error else AppColors.TextSecondary) }
            },
            visualTransformation =
                if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            enabled = enabled,
            readOnly = readOnly,
            trailingIcon = trailing,
            shape = FieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.Field,
                unfocusedContainerColor = AppColors.Field,
                disabledContainerColor = AppColors.Field,
                errorContainerColor = AppColors.Field,
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Outline,
                errorBorderColor = AppColors.Error,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                disabledTextColor = AppColors.TextSecondary,
                cursorColor = AppColors.Primary
            )
        )
    }
}

// Kattintható „mező” – pl. idő- vagy dátumválasztóhoz
@Composable
fun PickerField(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    trailing: String = "▾",
    enabled: Boolean = true
) {
    Column(modifier) {
        if (label != null) FieldLabel(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FieldShape)
                .background(AppColors.Field)
                .border(1.dp, AppColors.Outline, FieldShape)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text,
                color = AppColors.TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(trailing, color = AppColors.TextSecondary)
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    color: Color = AppColors.Primary
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.height(50.dp),
        shape = FieldShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = color.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = AppColors.TextPrimary
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = FieldShape,
        border = BorderStroke(1.dp, AppColors.Outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AppColors.Surface,
            contentColor = color
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

// Választható „chip” (ismétlődés, státusz, kategória)
@Composable
fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(FieldShape)
            .background(if (selected) AppColors.Primary else AppColors.Field)
            .border(
                1.dp,
                if (selected) AppColors.Primary else AppColors.Outline,
                FieldShape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else AppColors.TextPrimary,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// Kész / nincs kész jelölő kör
@Composable
fun StatusCircle(
    completed: Boolean,
    onClick: (() -> Unit)? = null,
    size: Dp = 30.dp
) {
    val base = Modifier
        .size(size)
        .clip(CircleShape)
    val styled = if (completed) {
        base.background(AppColors.Success)
    } else {
        base.border(2.dp, AppColors.TextSecondary, CircleShape)
    }
    Box(
        modifier = if (onClick != null) styled.clickable(onClick = onClick) else styled,
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// Kategória ikon (emoji) lekerekített négyzetben
@Composable
fun CategoryBadge(categoryName: String?, size: Dp = 42.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.SurfaceHigh),
        contentAlignment = Alignment.Center
    ) {
        Text(categoryEmoji(categoryName), fontSize = (size.value * 0.45f).sp)
    }
}

// A backend kategórianevéhez illő emoji
fun categoryEmoji(name: String?): String {
    val n = name?.lowercase().orEmpty()
    return when {
        listOf("water", "drink", "hydrat").any { it in n } -> "💧"
        listOf("fit", "sport", "exercise", "run", "physical", "workout").any { it in n } -> "🏃"
        listOf("health", "wellness").any { it in n } -> "❤️"
        listOf("mind", "meditat", "mental", "spirit").any { it in n } -> "🧘"
        listOf("learn", "study", "educat", "read", "knowledge").any { it in n } -> "📚"
        listOf("product", "work", "career").any { it in n } -> "💼"
        listOf("social", "relation", "family", "friend").any { it in n } -> "🤝"
        listOf("financ", "money", "saving").any { it in n } -> "💰"
        listOf("sleep", "rest").any { it in n } -> "😴"
        listOf("nutri", "food", "diet", "eat").any { it in n } -> "🥗"
        listOf("creat", "art", "hobby", "music").any { it in n } -> "🎨"
        listOf("journal", "write").any { it in n } -> "✏️"
        else -> "⭐"
    }
}

@Composable
fun SectionHeader(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = AppColors.TextSecondary
        )
    }
}

@Composable
fun ErrorText(message: String?, modifier: Modifier = Modifier) {
    if (message.isNullOrBlank()) return
    Text(
        message,
        color = AppColors.Error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
fun CenteredLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.Primary)
    }
}

// Lebegő kerek gomb (FAB)
@Composable
fun AccentFab(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(AppColors.Accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
    }
}

// Haladás sáv
@Composable
fun ProgressBar(fraction: Float, modifier: Modifier = Modifier, color: Color = AppColors.Accent) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(AppColors.SurfaceHigh)
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}

// ==========================================
// PROFILKÉP (külső könyvtár nélkül töltjük le)
// ==========================================

@Composable
fun Avatar(profile: AuthProfile?, size: Dp = 56.dp, fallbackText: String = "?") {
    val url = RetrofitClient.absoluteUrl(profile?.profileImageUrl)
    val base64 = profile?.profileImageBase64

    val bitmap by produceState<ImageBitmap?>(initialValue = null, url, base64) {
        value = withContext(Dispatchers.IO) { loadBitmap(url, base64) }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(AppColors.Primary),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp,
                contentDescription = "Profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                fallbackText.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp
            )
        }
    }
}

private fun loadBitmap(url: String?, base64: String?): ImageBitmap? {
    try {
        if (!base64.isNullOrBlank()) {
            val clean = base64.substringAfter("base64,")
            val bytes = Base64.decode(clean, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let {
                return it.asImageBitmap()
            }
        }
        if (!url.isNullOrBlank()) {
            val request = Request.Builder().url(url).build()
            RetrofitClient.httpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val bytes = resp.body?.bytes() ?: return null
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }
        }
    } catch (e: Exception) {
        return null
    }
    return null
}

// ==========================================
// HALADÁS RÖGZÍTÉSE (Add Progress) PÁRBESZÉDABLAK
// ==========================================

@Composable
fun AddProgressDialog(
    habitName: String,
    saving: Boolean,
    error: String?,
    goalMinutes: Int = 0,
    alreadyLogged: Int = 0,
    onDismiss: () -> Unit,
    onSave: (loggedMinutes: Int?, notes: String, completed: Boolean) -> Unit
) {
    var minutes by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var completed by rememberSaveable { mutableStateOf(false) }

    // Élő előnézet: eddigi + most beírt percek a célhoz képest
    val total = alreadyLogged + (minutes.toIntOrNull() ?: 0)
    val percent = ProgressUtils.percent(total, goalMinutes)

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        containerColor = AppColors.Surface,
        title = {
            Column {
                Text("Add Progress", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
                Text(habitName, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = minutes,
                    onValueChange = { v ->
                        if (v.all { it.isDigit() } && v.length <= 4) {
                            minutes = v
                            // A cél elérésekor automatikusan készre jelöljük
                            val newTotal = alreadyLogged + (v.toIntOrNull() ?: 0)
                            completed = goalMinutes > 0 && newTotal >= goalMinutes
                        }
                    },
                    label = "Logged time (minutes)",
                    placeholder = if (goalMinutes > 0) "Goal: $goalMinutes min" else "e.g. 30",
                    keyboardType = KeyboardType.Number,
                    enabled = !saving
                )
                if (goalMinutes > 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text(
                                "$total / $goalMinutes min",
                                color = AppColors.TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Text("$percent%", color = AppColors.Accent, fontWeight = FontWeight.Bold)
                        }
                        ProgressBar(percent / 100f)
                    }
                }
                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (optional)",
                    placeholder = "How did it go?",
                    singleLine = false,
                    minLines = 2,
                    enabled = !saving
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(enabled = !saving) { completed = !completed }
                ) {
                    Checkbox(
                        checked = completed,
                        onCheckedChange = { completed = it },
                        enabled = !saving,
                        colors = CheckboxDefaults.colors(checkedColor = AppColors.Success)
                    )
                    Text("Mark as completed", color = AppColors.TextPrimary)
                }
                ErrorText(error)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(minutes.toIntOrNull(), notes.trim(), completed) },
                enabled = !saving
            ) {
                Text(if (saving) "Saving..." else "Save", color = AppColors.PrimarySoft)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text("Cancel", color = AppColors.TextSecondary)
            }
        }
    )
}

// Megerősítő párbeszédablak (törlés, kijelentkezés)
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Surface,
        title = { Text(title, color = AppColors.TextPrimary) },
        text = { Text(message, color = AppColors.TextSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = if (destructive) AppColors.Error else AppColors.PrimarySoft)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.TextSecondary)
            }
        }
    )
}
