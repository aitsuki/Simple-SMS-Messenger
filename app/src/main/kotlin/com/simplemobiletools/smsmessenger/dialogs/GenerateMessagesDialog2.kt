package com.simplemobiletools.smsmessenger.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.provider.Telephony
import android.telephony.SmsManager
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.smsmessenger.activities.SimpleActivity
import com.simplemobiletools.smsmessenger.databinding.DialogGenerateMessages2Binding
import com.simplemobiletools.smsmessenger.helpers.MessagesImporter
import com.simplemobiletools.smsmessenger.models.BackupType
import com.simplemobiletools.smsmessenger.models.ImportResult
import com.simplemobiletools.smsmessenger.models.SmsBackup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GenerateMessagesDialog2(private val activity: SimpleActivity) {
    init {
        var ignoreClicks = false
        val binding = DialogGenerateMessages2Binding.inflate(activity.layoutInflater)

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, titleText = "发送/接收短信") { alertDialog ->

                    val date = Calendar.getInstance()
                    binding.date.text = formatDate(date)
                    binding.time.text = formatTime(date)

                    binding.dateContainer.setOnClickListener {
                        DatePickerDialog(
                            activity,
                            { _, year, month, dayOfMonth ->
                                date.set(Calendar.YEAR, year)
                                date.set(Calendar.MONTH, month)
                                date.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                binding.date.text = formatDate(date)
                            }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }

                    binding.timeContainer.setOnClickListener {
                        TimePickerDialog(activity, { _, hourOfDay, minute ->
                            date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            date.set(Calendar.MINUTE, minute)
                            binding.time.text = formatTime(date)
                        }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE), true).show()
                    }


                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        val type = if (binding.typeReceive.isChecked) Telephony.Sms.MESSAGE_TYPE_INBOX else Telephony.Sms.MESSAGE_TYPE_SENT

                        val address = binding.address.text.toString().trim()
                        if (address.isEmpty()) {
                            activity.toast("请输入地址")
                            return@setOnClickListener
                        }

                        val body = binding.body.text.toString().trim()
                        if (body.isEmpty()) {
                            activity.toast("请输入内容")
                            return@setOnClickListener
                        }

                        ignoreClicks = true
                        activity.showLGeneratorLoading()
                        ensureBackgroundThread {
                            val subscriptionId = SmsManager.getDefaultSmsSubscriptionId()
                            val message = SmsBackup(
                                subscriptionId = subscriptionId.toLong(),
                                address = address,
                                body = body,
                                date = if (type == Telephony.Sms.MESSAGE_TYPE_INBOX) date.timeInMillis else 0,
                                dateSent = if (type == Telephony.Sms.MESSAGE_TYPE_SENT) date.timeInMillis else 0,
                                locked = 0,
                                protocol = null,
                                read = 1,
                                status = -1,
                                type = type,
                                serviceCenter = null,
                                backupType = BackupType.SMS
                            )

                            MessagesImporter(activity).restoreMessages(listOf(message)) {
                                activity.dismissGeneratorLoading()
                                handleParseResult(it)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun handleParseResult(result: ImportResult) {
        activity.toast(
            when (result) {
                ImportResult.IMPORT_OK -> "短信生成成功"
                ImportResult.IMPORT_PARTIAL -> "短信生成成功"
                ImportResult.IMPORT_FAIL -> "短信生成失败"
                else -> "未知错误"
            }
        )
    }

    private fun formatDate(calendar: Calendar): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(calendar.timeInMillis)
    }

    private fun formatTime(calendar: Calendar): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.US)
        return formatter.format(calendar.timeInMillis)
    }
}
