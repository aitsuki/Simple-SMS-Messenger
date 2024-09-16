package com.simplemobiletools.smsmessenger.dialogs

import android.telephony.SmsManager
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.smsmessenger.activities.SimpleActivity
import com.simplemobiletools.smsmessenger.databinding.DialogGenerateMessagesBinding
import com.simplemobiletools.smsmessenger.helpers.MessagesImporter
import com.simplemobiletools.smsmessenger.helpers.MessagesReader
import com.simplemobiletools.smsmessenger.models.BackupType
import com.simplemobiletools.smsmessenger.models.ImportResult
import com.simplemobiletools.smsmessenger.models.MessagesBackup
import com.simplemobiletools.smsmessenger.models.SmsBackup
import kotlin.random.Random

class GenerateMessagesDialog(private val activity: SimpleActivity) {
    init {
        var ignoreClicks = false
        val binding = DialogGenerateMessagesBinding.inflate(activity.layoutInflater).apply {
            seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    countTextView.text = progress.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, titleText = "短信生成") { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        if (binding.seekbar.progress == 0) {
                            activity.toast("请选要生成的择短信数量")
                            return@setOnClickListener
                        }

                        ignoreClicks = true
                        activity.showLGeneratorLoading()
                        ensureBackgroundThread {
                            val count = binding.seekbar.progress
                            val messagesReader = MessagesReader(activity)
                            val subscriptionId = SmsManager.getDefaultSmsSubscriptionId()
                            val currentDate = System.currentTimeMillis()
                            var smsDate = currentDate
                            val latestDate = messagesReader.getLatestSmsDate()
                            val messages = ArrayList<MessagesBackup>(count)
                            for (i in 1..count) {
                                smsDate -= 1000
                                if (smsDate <= latestDate) break
                                val message = SmsBackup(
                                    subscriptionId = subscriptionId.toLong(),
                                    address = Random.nextInt(10000000, 99999999).toString(),
                                    body = "测试短信, 批次： $latestDate， 序号： $i",
                                    date = smsDate,
                                    dateSent = 0L,
                                    locked = 0,
                                    protocol = null,
                                    read = 1,
                                    status = -1,
                                    type = 2,
                                    serviceCenter = null,
                                    backupType = BackupType.SMS
                                )
                                messages.add(message)
                            }

                            MessagesImporter(activity).restoreMessages(messages) {
                                activity.dismissGeneratorLoading()
                                handleParseResult(it, messages.size)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun handleParseResult(result: ImportResult, size: Int) {
        activity.toast(
            when (result) {
                ImportResult.IMPORT_OK -> "生成${size}条短信成功"
                ImportResult.IMPORT_PARTIAL -> "生成了部分短信"
                ImportResult.IMPORT_FAIL -> "短信生成失败"
                else -> "未知错误"
            }
        )
    }
}
