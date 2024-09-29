package com.simplemobiletools.smsmessenger.dialogs

import android.telephony.SmsManager
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.smsmessenger.activities.SimpleActivity
import com.simplemobiletools.smsmessenger.databinding.DialogGenerateMessagesBinding
import com.simplemobiletools.smsmessenger.helpers.MessagesImporter
import com.simplemobiletools.smsmessenger.helpers.SmsSpec
import com.simplemobiletools.smsmessenger.models.BackupType
import com.simplemobiletools.smsmessenger.models.ImportResult
import com.simplemobiletools.smsmessenger.models.MessagesBackup
import com.simplemobiletools.smsmessenger.models.SmsBackup
import java.util.Calendar
import kotlin.random.Random

class GenerateMessagesDialog(private val activity: SimpleActivity) {
    init {
        var ignoreClicks = false
        val binding = DialogGenerateMessagesBinding.inflate(activity.layoutInflater)

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, titleText = "短信生成") { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        val count = binding.count.text.toString().toIntOrNull()

                        if (count == null || count < 1) {
                            activity.toast("请输入要生成的择短信数量")
                            return@setOnClickListener
                        }

                        ignoreClicks = true
                        activity.showLGeneratorLoading()
                        ensureBackgroundThread {
                            val subscriptionId = SmsManager.getDefaultSmsSubscriptionId()
                            val messages = ArrayList<MessagesBackup>(count)
                            for (i in 1..count) {
                                val message = SmsBackup(
                                    subscriptionId = subscriptionId.toLong(),
                                    address = randomAddress(),
                                    body = randomBody(),
                                    date = randomDate(),
                                    dateSent = 0L,
                                    locked = 0,
                                    protocol = null,
                                    read = 1,
                                    status = -1,
                                    type = 1,
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

    private fun randomDate(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, -1)
        val start = cal.timeInMillis
        val now = System.currentTimeMillis()
        return start + (Math.random() * (now - start)).toLong()
    }

    private fun randomAddress(): String {
        val useSpec = Random.nextBoolean()
        if (useSpec) {
            val useLike = Random.nextBoolean()
            if (useLike) {
                val index = Random.nextInt(SmsSpec.addressLike.size)
                return getRandomWorks() + "" + SmsSpec.addressLike[index] + "" + getRandomWorks()
            } else {
                val index = Random.nextInt(SmsSpec.addresses.size)
                return SmsSpec.addresses[index]
            }
        }
        return Random.nextInt(10000000, 99999999).toString()
    }

    private fun randomBody(): String {
        val useSpec = Random.nextBoolean()
        if (useSpec) {
            val index = Random.nextInt(SmsSpec.bodyLike.size)
            val like = SmsSpec.bodyLike[index]
            return getRandomWorks(7) + " $like " + getRandomWorks(2)
        } else {
            return getRandomWorks(10)
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

    private fun getRandomWorks(count: Int = 1): String {
        val sb = StringBuilder()
        repeat(count) {
            val index = Random.nextInt(randomWords.size)
            sb.append(randomWords[index]).append(" ")
        }
        return sb.trim().toString()
    }

    private val randomWords = arrayOf(
        "hieromaterialist",
        "officefication",
        "payar",
        "pedotic",
        "emptacity",
        "claustracy",
        "gnarably",
        "barbuous",
        "suffer",
        "cubitot",
        "aheadetic",
        "betteracy",
        "cephaloheadarium",
        "turboacity",
        "cosmkin",
        "ampleveryone",
        "elseard",
        "frigth",
        "monstrhislet",
        "article",
        "egyracious",
        "morph",
        "kiloitive",
        "cosmot",
        "deictfaction",
        "pingbehavioria",
        "tortaster",
        "processite",
        "ohorium",
        "kakoism",
        "blastoon",
        "comization",
        "cale",
        "reasonage",
        "pushism",
        "everular",
        "stationaire",
        "culinmost",
        "environmentalable",
        "digitdom",
        "herpacle",
        "doloroar",
        "chooseture",
        "emetous",
        "siphoess",
        "hypic",
        "beautiful",
        "templesque",
        "optoain",
        "nutriite",
        "opical",
        "punct",
        "oileous",
        "single",
        "sitot",
        "regiony",
        "hibern",
        "caloture",
        "perisive",
        "pylian",
        "obsine",
        "particularitive",
        "expectenne",
        "sonism",
        "laminity",
        "scansling",
        "civil",
        "have",
        "value",
        "theast",
        "scendwise",
        "sy",
        "metrment",
        "cusacle",
        "guesssive",
        "unitor",
        "calorisure",
        "manuon",
        "monstrat",
        "plattical",
        "general",
        "governmentosity",
        "setior",
        "serfier",
        "realityern",
        "bolafic",
        "sourceery",
        "explainery",
        "cancerast",
        "negeer",
        "whateverery",
        "tersical",
        "crutless",
        "tornory",
        "ptohardous",
        "audienceify",
        "valeatic",
        "countryive",
        "falcship",
        "heartial",
    )
}
