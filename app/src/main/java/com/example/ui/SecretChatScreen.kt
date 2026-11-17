package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.EnhancedEncryption
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SecretChatThemeTokens

@Composable
fun SecretChatRoute(viewModel: SecretChatViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  SecretChatScreen(
    uiState = uiState,
    onPasswordChange = viewModel::onPasswordChange,
    onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
    onClearPassword = viewModel::onClearPassword,
    onMessageInputChange = viewModel::onMessageInputChange,
    onClearMessageInput = viewModel::onClearMessageInput,
    onPasteClick = {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      val clipText =
        clipboard?.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)
          ?.toString()
      viewModel.onPasteFromClipboard(clipText)
    },
    onEncryptClick = viewModel::onEncryptClick,
    onDecryptClick = viewModel::onDecryptClick,
    onCopyClick = {
      viewModel.onCopyResult { text ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Secret chat result", text))
      }
    },
    onSwapResultToInput = viewModel::onSwapResultToInput,
  )
}

@Composable
fun SecretChatScreen(
  uiState: SecretChatUiState,
  onPasswordChange: (String) -> Unit,
  onTogglePasswordVisibility: () -> Unit,
  onClearPassword: () -> Unit,
  onMessageInputChange: (String) -> Unit,
  onClearMessageInput: () -> Unit,
  onPasteClick: () -> Unit,
  onEncryptClick: () -> Unit,
  onDecryptClick: () -> Unit,
  onCopyClick: () -> Unit,
  onSwapResultToInput: () -> Unit,
) {
  val extendedColors = SecretChatThemeTokens.extendedColors
  val focusManager = LocalFocusManager.current
  val keyboardController = LocalSoftwareKeyboardController.current
  val scrollState = rememberScrollState()

  val monoFieldTextStyle =
    TextStyle(
      fontFamily = JetBrainsMonoFontFamily,
      fontSize = 15.sp,
      lineHeight = 22.sp,
      fontWeight = FontWeight.Normal,
    )

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    contentWindowInsets = WindowInsets.safeDrawing,
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors =
                listOf(
                  MaterialTheme.colorScheme.background,
                  MaterialTheme.colorScheme.surfaceVariant,
                )
            )
          )
          .padding(innerPadding)
          .imePadding()
          .verticalScroll(scrollState)
          .padding(horizontal = 16.dp, vertical = 20.dp),
      contentAlignment = Alignment.TopCenter,
    ) {
      Surface(
        modifier = Modifier.fillMaxWidth().widthIn(max = 640.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, extendedColors.cardBorder),
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Classic Academic Header
          AcademicHeader()

          // Form Body
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
          ) {
            // Option 1: Numeric Password Input
            PasswordSection(
              password = uiState.password,
              isPasswordVisible = uiState.isPasswordVisible,
              monoTextStyle = monoFieldTextStyle,
              onPasswordChange = onPasswordChange,
              onToggleVisibility = onTogglePasswordVisibility,
              onClearPassword = onClearPassword,
              onNext = { focusManager.moveFocus(FocusDirection.Down) },
            )

            // Message Input Area
            MessageInputSection(
              messageInput = uiState.messageInput,
              charCount = uiState.inputCharacterCount,
              tokenCount = uiState.detectedTokenCount,
              monoTextStyle = monoFieldTextStyle,
              onMessageInputChange = onMessageInputChange,
              onPasteClick = onPasteClick,
              onClearMessageInput = onClearMessageInput,
            )

            // Inline Validation / Status Banner
            AnimatedVisibility(
              visible = uiState.statusFeedback != null,
              enter = fadeIn() + expandVertically(),
              exit = fadeOut() + shrinkVertically(),
            ) {
              uiState.statusFeedback?.let { feedback -> StatusBanner(feedback = feedback) }
            }

            // Options 2 & 3: Encrypt & Decrypt Buttons
            ActionButtonsRow(
              onEncryptClick = {
                keyboardController?.hide()
                onEncryptClick()
              },
              onDecryptClick = {
                keyboardController?.hide()
                onDecryptClick()
              },
            )

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Result Output Box
            ResultOutputSection(
              resultOutput = uiState.resultOutput,
              isCopiedFeedbackVisible = uiState.isCopiedFeedbackVisible,
              monoTextStyle = monoFieldTextStyle,
              onCopyClick = onCopyClick,
              onSwapResultToInput = onSwapResultToInput,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AcademicHeader() {
  val extendedColors = SecretChatThemeTokens.extendedColors

  Column(modifier = Modifier.fillMaxWidth().background(extendedColors.headerBackground)) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = extendedColors.onHeader.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, extendedColors.headerAccent.copy(alpha = 0.55f)),
      ) {
        Icon(
          imageVector = Icons.Rounded.EnhancedEncryption,
          contentDescription = stringResource(R.string.cd_app_emblem),
          tint = extendedColors.headerAccent,
          modifier = Modifier.padding(10.dp).size(26.dp),
        )
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = stringResource(R.string.header_title),
          style = MaterialTheme.typography.headlineSmall,
          color = extendedColors.onHeader,
        )
        Text(
          text = stringResource(R.string.header_subtitle),
          style = MaterialTheme.typography.bodySmall,
          color = extendedColors.onHeaderMuted,
        )
      }
    }

    // 4dp Amber Academic Accent Rule
    Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(extendedColors.headerAccent))
  }
}

@Composable
private fun PasswordSection(
  password: String,
  isPasswordVisible: Boolean,
  monoTextStyle: TextStyle,
  onPasswordChange: (String) -> Unit,
  onToggleVisibility: () -> Unit,
  onClearPassword: () -> Unit,
  onNext: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = stringResource(R.string.label_password),
      style = MaterialTheme.typography.titleSmall,
      color = MaterialTheme.colorScheme.onSurface,
    )

    OutlinedTextField(
      value = password,
      onValueChange = onPasswordChange,
      modifier = Modifier.fillMaxWidth().testTag("password_input"),
      textStyle = monoTextStyle.copy(fontSize = 16.sp),
      placeholder = {
        Text(
          text = stringResource(R.string.placeholder_password),
          style = monoTextStyle.copy(fontSize = 14.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Outlined.Key,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
      },
      trailingIcon = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (password.isNotEmpty()) {
            IconButton(
              onClick = onClearPassword,
              modifier = Modifier.minimumInteractiveComponentSize().testTag("clear_password_button"),
            ) {
              Icon(
                imageVector = Icons.Outlined.Clear,
                contentDescription = stringResource(R.string.cd_clear_password),
              )
            }
          }
          IconButton(
            onClick = onToggleVisibility,
            modifier =
              Modifier.minimumInteractiveComponentSize().testTag("toggle_password_visibility_button"),
          ) {
            Icon(
              imageVector =
                if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
              contentDescription = stringResource(R.string.cd_toggle_password_visibility),
            )
          }
        }
      },
      singleLine = true,
      visualTransformation =
        if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      keyboardOptions =
        KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
      keyboardActions = KeyboardActions(onNext = { onNext() }),
      shape = RoundedCornerShape(10.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
  }
}

@Composable
private fun MessageInputSection(
  messageInput: String,
  charCount: Int,
  tokenCount: Int,
  monoTextStyle: TextStyle,
  onMessageInputChange: (String) -> Unit,
  onPasteClick: () -> Unit,
  onClearMessageInput: () -> Unit,
) {
  val extendedColors = SecretChatThemeTokens.extendedColors

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stringResource(R.string.label_message),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (messageInput.isNotEmpty()) {
          OutlinedButton(
            onClick = onClearMessageInput,
            modifier =
              Modifier.minimumInteractiveComponentSize().testTag("clear_message_button"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Icon(
              imageVector = Icons.Outlined.Clear,
              contentDescription = stringResource(R.string.cd_clear_message),
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = stringResource(R.string.action_clear),
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }

        OutlinedButton(
          onClick = onPasteClick,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("paste_button"),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Icon(
            imageVector = Icons.Outlined.ContentPaste,
            contentDescription = stringResource(R.string.cd_paste_clipboard),
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.action_paste),
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }

    OutlinedTextField(
      value = messageInput,
      onValueChange = onMessageInputChange,
      modifier = Modifier.fillMaxWidth().heightIn(min = 128.dp).testTag("message_input"),
      textStyle = monoTextStyle,
      placeholder = {
        Text(
          text = stringResource(R.string.placeholder_message),
          style = monoTextStyle.copy(fontSize = 13.5.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
      },
      minLines = 4,
      maxLines = 8,
      shape = RoundedCornerShape(10.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = extendedColors.inputBackground,
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )

    if (charCount > 0) {
      Text(
        text = "$charCount chars · $tokenCount tokens",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun StatusBanner(feedback: StatusFeedback) {
  val extendedColors = SecretChatThemeTokens.extendedColors
  val isError = feedback is StatusFeedback.Error

  val containerColor =
    if (isError) extendedColors.errorBannerContainer else extendedColors.infoContainer
  val borderColor = if (isError) extendedColors.errorBannerBorder else extendedColors.infoBorder
  val contentColor = if (isError) extendedColors.onErrorBanner else extendedColors.onInfoContainer

  val messageText =
    when (feedback) {
      is StatusFeedback.Info ->
        if (feedback.formatArgs.isEmpty()) {
          stringResource(feedback.messageRes)
        } else {
          stringResource(feedback.messageRes, *feedback.formatArgs.toTypedArray())
        }

      is StatusFeedback.Error ->
        if (feedback.formatArgs.isEmpty()) {
          stringResource(feedback.messageRes)
        } else {
          stringResource(feedback.messageRes, *feedback.formatArgs.toTypedArray())
        }
    }

  Surface(
    modifier =
      Modifier.fillMaxWidth()
        .semantics { liveRegion = LiveRegionMode.Polite }
        .testTag("status_banner"),
    shape = RoundedCornerShape(10.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector =
          if (isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircleOutline,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(18.dp),
      )
      Text(
        text = messageText,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = contentColor,
      )
    }
  }
}

@Composable
private fun ActionButtonsRow(onEncryptClick: () -> Unit, onDecryptClick: () -> Unit) {
  val extendedColors = SecretChatThemeTokens.extendedColors

  BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val useColumn = maxWidth < 310.dp

    if (useColumn) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EncryptButton(modifier = Modifier.fillMaxWidth(), onClick = onEncryptClick)
        DecryptButton(
          modifier = Modifier.fillMaxWidth(),
          containerColor = extendedColors.decryptButtonContainer,
          contentColor = extendedColors.onDecryptButton,
          onClick = onDecryptClick,
        )
      }
    } else {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        EncryptButton(modifier = Modifier.weight(1f), onClick = onEncryptClick)
        DecryptButton(
          modifier = Modifier.weight(1f),
          containerColor = extendedColors.decryptButtonContainer,
          contentColor = extendedColors.onDecryptButton,
          onClick = onDecryptClick,
        )
      }
    }
  }
}

@Composable
private fun EncryptButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  Button(
    onClick = onClick,
    modifier = modifier.heightIn(min = 52.dp).testTag("encrypt_button"),
    shape = RoundedCornerShape(10.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
      ),
  ) {
    Icon(imageVector = Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = stringResource(R.string.action_encrypt),
      style = MaterialTheme.typography.labelLarge,
    )
  }
}

@Composable
private fun DecryptButton(
  modifier: Modifier = Modifier,
  containerColor: androidx.compose.ui.graphics.Color,
  contentColor: androidx.compose.ui.graphics.Color,
  onClick: () -> Unit,
) {
  Button(
    onClick = onClick,
    modifier = modifier.heightIn(min = 52.dp).testTag("decrypt_button"),
    shape = RoundedCornerShape(10.dp),
    colors =
      ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
  ) {
    Icon(
      imageVector = Icons.Rounded.LockOpen,
      contentDescription = null,
      modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = stringResource(R.string.action_decrypt),
      style = MaterialTheme.typography.labelLarge,
    )
  }
}

@Composable
private fun ResultOutputSection(
  resultOutput: String,
  isCopiedFeedbackVisible: Boolean,
  monoTextStyle: TextStyle,
  onCopyClick: () -> Unit,
  onSwapResultToInput: () -> Unit,
) {
  val extendedColors = SecretChatThemeTokens.extendedColors

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = stringResource(R.string.label_result),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
      )

      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (resultOutput.isNotEmpty()) {
          OutlinedButton(
            onClick = onSwapResultToInput,
            modifier =
              Modifier.minimumInteractiveComponentSize().testTag("swap_to_input_button"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Icon(
              imageVector = Icons.Outlined.SwapVert,
              contentDescription = stringResource(R.string.cd_swap_result),
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = stringResource(R.string.action_use_as_input),
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }

        OutlinedButton(
          onClick = onCopyClick,
          modifier = Modifier.minimumInteractiveComponentSize().testTag("copy_button"),
          shape = RoundedCornerShape(8.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          colors =
            if (isCopiedFeedbackVisible) {
              ButtonDefaults.outlinedButtonColors(
                containerColor = extendedColors.copiedContainer,
                contentColor = extendedColors.onCopiedContainer,
              )
            } else {
              ButtonDefaults.outlinedButtonColors()
            },
          border =
            BorderStroke(
              1.dp,
              if (isCopiedFeedbackVisible) extendedColors.copiedBorder
              else MaterialTheme.colorScheme.outline,
            ),
        ) {
          Icon(
            imageVector =
              if (isCopiedFeedbackVisible) Icons.Rounded.Check else Icons.Outlined.ContentCopy,
            contentDescription = stringResource(R.string.cd_copy_result),
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text =
              stringResource(
                if (isCopiedFeedbackVisible) R.string.action_copied else R.string.action_copy
              ),
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }

    OutlinedTextField(
      value = resultOutput,
      onValueChange = {},
      readOnly = true,
      modifier = Modifier.fillMaxWidth().heightIn(min = 128.dp).testTag("result_output"),
      textStyle = monoTextStyle,
      placeholder = {
        Text(
          text = stringResource(R.string.placeholder_result),
          style = monoTextStyle.copy(fontSize = 13.5.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
      },
      minLines = 4,
      maxLines = 8,
      shape = RoundedCornerShape(10.dp),
      colors =
        OutlinedTextFieldDefaults.colors(
          focusedContainerColor = extendedColors.outputBackground,
          unfocusedContainerColor = extendedColors.outputBackground,
          focusedBorderColor = MaterialTheme.colorScheme.outline,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
  }
}
